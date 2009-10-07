/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.connector;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.catalina.Globals;
import org.apache.catalina.connector.*;
import org.apache.catalina.core.*;
import org.apache.catalina.util.StringManager;

public class AsyncContextImpl implements AsyncContext {

    /* 
     * Event notification types for async mode
     */
    static enum AsyncEventType { COMPLETE, TIMEOUT, ERROR }

    private static final Logger log =
        Logger.getLogger(AsyncContextImpl.class.getName());

    // Default timeout for async operations
    private static final long DEFAULT_ASYNC_TIMEOUT_MILLIS = 30000L;

    // Thread pool for async dispatches
    private static final ExecutorService pool =
        Executors.newCachedThreadPool();

    private static final StringManager STRING_MANAGER =
        StringManager.getManager(Constants.Package);

    // The original (unwrapped) request
    private Request origRequest;

    // The possibly wrapped request passed to ServletRequest.startAsync
    private ServletRequest servletRequest;

    // The possibly wrapped response passed to ServletRequest.startAsync    
    private ServletResponse servletResponse;

    private boolean isOriginalRequestAndResponse = false;

    // The target of zero-argument async dispatches
    private String zeroArgDispatchTarget = null;

    // defaults to false
    private AtomicBoolean isDispatchInProgress = new AtomicBoolean(); 

    private long asyncTimeoutMillis = DEFAULT_ASYNC_TIMEOUT_MILLIS;

    private LinkedList<AsyncListenerHolder> asyncListenerHolders =
        new LinkedList<AsyncListenerHolder>();

    /**
     * Constructor
     *
     * @param origRequest the original (unwrapped) request
     * @param servletRequest the possibly wrapped request passed to
     * ServletRequest.startAsync
     * @param servletResponse the possibly wrapped response passed to
     * ServletRequest.startAsync
     * @param isOriginalRequestAndResponse true if the zero-arg version of
     * startAsync was called, false otherwise
     */
    public AsyncContextImpl(Request origRequest,
                            ServletRequest servletRequest,
                            Response origResponse,
                            ServletResponse servletResponse,
                            boolean isOriginalRequestAndResponse) {
        this.origRequest = origRequest;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.isOriginalRequestAndResponse = isOriginalRequestAndResponse;
        if (!isOriginalRequestAndResponse &&
                (servletRequest instanceof HttpServletRequest)) {
            zeroArgDispatchTarget = getZeroArgDispatchTarget(
                (HttpServletRequest)servletRequest);
        } else {
            zeroArgDispatchTarget = getZeroArgDispatchTarget(origRequest);
        }
    }

    @Override
    public ServletRequest getRequest() {
        return servletRequest;
    }

    Request getOriginalRequest() {
        return origRequest;
    }

    @Override
    public ServletResponse getResponse() {
        return servletResponse;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        return isOriginalRequestAndResponse;
    }

    @Override
    public void dispatch() {
        if (zeroArgDispatchTarget == null) {
            log.severe("Unable to determine target of zero-arg dispatch");
            return;
        }
        ApplicationDispatcher dispatcher = (ApplicationDispatcher)
            servletRequest.getRequestDispatcher(zeroArgDispatchTarget);
        if (dispatcher != null) {
            if (isDispatchInProgress.compareAndSet(false, true)) {
                origRequest.setAttribute(Globals.DISPATCHER_TYPE_ATTR,
                                         DispatcherType.ASYNC);
                origRequest.setOkToReinitializeAsync(true);
                origRequest.setAsyncStarted(false);
                pool.execute(new Handler(this, dispatcher));
            } else {
                throw new IllegalStateException(
                    STRING_MANAGER.getString("async.dispatchInProgress"));
            }
        } else {
            // Should never happen, because any unmapped paths will be 
            // mapped to the DefaultServlet
            log.warning("Unable to acquire RequestDispatcher for " +
                        zeroArgDispatchTarget);
        }
    } 

    @Override
    public void dispatch(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Null path");
        }
        ApplicationDispatcher dispatcher = (ApplicationDispatcher)
            servletRequest.getRequestDispatcher(path);
        if (dispatcher != null) {
            if (isDispatchInProgress.compareAndSet(false, true)) {
                origRequest.setAttribute(Globals.DISPATCHER_TYPE_ATTR,
                                         DispatcherType.ASYNC);
                origRequest.setOkToReinitializeAsync(true);
                origRequest.setAsyncStarted(false);
                pool.execute(new Handler(this, dispatcher));
            } else {
                throw new IllegalStateException(
                    STRING_MANAGER.getString("async.dispatchInProgress"));
            }
        } else {
            // Should never happen, because any unmapped paths will be 
            // mapped to the DefaultServlet
            log.warning("Unable to acquire RequestDispatcher for " +
                        path);
        }
    }

    @Override
    public void dispatch(ServletContext context, String path) {
        if (path == null || context == null) {
            throw new IllegalArgumentException("Null context or path");
        }
        ApplicationDispatcher dispatcher = (ApplicationDispatcher)
            context.getRequestDispatcher(path);
        if (dispatcher != null) {
            if (isDispatchInProgress.compareAndSet(false, true)) {
                origRequest.setAttribute(Globals.DISPATCHER_TYPE_ATTR,
                                         DispatcherType.ASYNC);
                origRequest.setOkToReinitializeAsync(true);
                origRequest.setAsyncStarted(false);
                pool.execute(new Handler(this, dispatcher));
            } else {
                throw new IllegalStateException(
                    STRING_MANAGER.getString("async.dispatchInProgress"));
            }
        } else {
            // Should never happen, because any unmapped paths will be 
            // mapped to the DefaultServlet
            log.warning("Unable to acquire RequestDispatcher for " + path +
                        "in servlet context " + context.getContextPath());
        }
    }

    @Override
    public void complete() {
        origRequest.asyncComplete();
    }

    @Override
    public void start(Runnable run) {
        pool.execute(run);
    }

    @Override
    public void addListener(AsyncListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Null listener");
        }

        synchronized(asyncListenerHolders) {
            asyncListenerHolders.add(new AsyncListenerHolder(listener));
        }
    }

    @Override
    public void addListener(AsyncListener listener,
                            ServletRequest servletRequest,
                            ServletResponse servletResponse) {
        if (listener == null || servletRequest == null ||
                servletResponse == null) {
            throw new IllegalArgumentException(
                "Null listener, request, or response");
        }

        synchronized(asyncListenerHolders) {
            asyncListenerHolders.add(new AsyncListenerHolder(
                listener, servletRequest, servletResponse));
        }
    }

    @Override
    public void setTimeout(long timeout) {
        asyncTimeoutMillis = timeout;
        origRequest.setAsyncTimeout(timeout);
    }

    @Override
    public long getTimeout() {
        return asyncTimeoutMillis;
    }

    /*
     * Reinitializes this AsyncContext with the given request and response.
     *
     * @param servletRequest the ServletRequest with which to initialize
     * the AsyncContext
     * @param servletResponse the ServletResponse with which to initialize
     * the AsyncContext
     * @param isOriginalRequestAndResponse true if the zero-arg version of
     * startAsync was called, false otherwise
     */
    void reinitialize(ServletRequest servletRequest,
                      ServletResponse servletResponse,
                      boolean isOriginalRequestAndResponse) {
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.isOriginalRequestAndResponse = isOriginalRequestAndResponse;
        isDispatchInProgress.set(false);
        synchronized(asyncListenerHolders) {
            asyncListenerHolders.clear();
        }
        if (isOriginalRequestAndResponse) {
            zeroArgDispatchTarget = getZeroArgDispatchTarget(origRequest);
        } else if (servletRequest instanceof HttpServletRequest) {
            zeroArgDispatchTarget = getZeroArgDispatchTarget(
                (HttpServletRequest)servletRequest);
        } else {
            log.warning("Unable to determine target of " +
                        "zero-argument dispatch");
        }
    }

    /**
     * Determines the target of a zero-argument async dispatch for the
     * given request.
     *
     * @return the target of the zero-argument async dispatch
     */
    private String getZeroArgDispatchTarget(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder();
        if (req.getServletPath() != null) {
            sb.append(req.getServletPath());
        }
        if (req.getPathInfo() != null) {
            sb.append(req.getPathInfo());
        }
        return sb.toString();
    }

    static class Handler implements Runnable {

        private final AsyncContextImpl asyncContext;
        private final ApplicationDispatcher dispatcher;

        Handler(AsyncContextImpl asyncContext,
                ApplicationDispatcher dispatcher) {
            this.asyncContext = asyncContext;
            this.dispatcher = dispatcher;
        }
       
        public void run() {
            try {
                dispatcher.dispatch(asyncContext.getRequest(),
                    asyncContext.getResponse(), DispatcherType.ASYNC);
                /* 
                 * Close the response after the dispatch target has
                 * completed execution, unless startAsync was called.
                 */
                if (!asyncContext.getRequest().isAsyncStarted()) {
                    asyncContext.complete();
                }
            } catch (Throwable t) {
                asyncContext.notifyAsyncListeners(AsyncEventType.ERROR, t);
                asyncContext.getOriginalRequest().errorDispatchAndComplete(t);
            }
        }
    }

    /*
     * Notifies all AsyncListeners of the given async event type
     */
    void notifyAsyncListeners(AsyncEventType asyncEventType, Throwable t) {
        synchronized(asyncListenerHolders) {
            if (asyncListenerHolders.isEmpty()) {
                return;
            }
            LinkedList<AsyncListenerHolder> clone =
                (LinkedList<AsyncListenerHolder>)
                    asyncListenerHolders.clone();
            for (AsyncListenerHolder asyncListenerHolder : clone) {
                AsyncListener asyncListener =
                    asyncListenerHolder.getAsyncListener();
                AsyncEvent asyncEvent = new AsyncEvent(
                    this, asyncListenerHolder.getRequest(),
                    asyncListenerHolder.getResponse(), t);
                try {
                    switch (asyncEventType) {
                    case COMPLETE:
                        asyncListener.onComplete(asyncEvent);
                        break;
                    case TIMEOUT:
                        asyncListener.onTimeout(asyncEvent);
                        break;
                    case ERROR:
                        asyncListener.onError(asyncEvent);
                        break;
                    }
                } catch (IOException ioe) {
                    log.log(Level.WARNING, "Error invoking AsyncListener",
                            ioe);
                }
            }
        }
    }

    void clear() {
        synchronized(asyncListenerHolders) {
            asyncListenerHolders.clear();
        }
    }

    /**
     * Class holding all the information required for invoking an
     * AsyncListener (including the AsyncListener itself).
     */
    private static class AsyncListenerHolder {

        private AsyncListener listener;
        private ServletRequest request;
        private ServletResponse response;

        public AsyncListenerHolder(AsyncListener listener) {
            this(listener, null, null);
        }

        public AsyncListenerHolder(AsyncListener listener,
                                   ServletRequest request,
                                   ServletResponse response) {
            this.listener = listener;
            this.request = request;
            this.response = response;
        }

        public AsyncListener getAsyncListener() {
            return listener;
        }

        public ServletRequest getRequest() {
            return request;
        }

        public ServletResponse getResponse() {
            return response;
        }
    }

}
