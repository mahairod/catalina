/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.connector;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.catalina.Globals;
import org.apache.catalina.connector.*;
import org.apache.catalina.core.*;

public class AsyncContextImpl implements AsyncContext {

    private static final Logger log =
        Logger.getLogger(AsyncContextImpl.class.getName());

    // Thread pool for async dispatches
    private static final ExecutorService pool =
        Executors.newCachedThreadPool();

    // The original (unwrapped) request
    private Request origRequest;

    // The possibly wrapped request passed to ServletRequest.startAsync
    private ServletRequest servletRequest;

    // The possibly wrapped response passed to ServletRequest.startAsync    
    private ServletResponse servletResponse;

    private boolean isOriginalRequestAndResponse = false;

    // The target of zero-argument async dispatches
    private String zeroArgDispatchTarget = null;


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


    public ServletRequest getRequest() {
        return servletRequest;
    }


    public ServletResponse getResponse() {
        return servletResponse;
    }


    public boolean hasOriginalRequestAndResponse() {
        return isOriginalRequestAndResponse;
    }


    public void dispatch() {
        if (zeroArgDispatchTarget == null) {
            log.severe("Unable to determine target of zero-arg dispatch");
        } else {
            origRequest.setAttribute(Globals.DISPATCHER_TYPE_ATTR,
                                     DispatcherType.ASYNC);
            ApplicationDispatcher dispatcher = (ApplicationDispatcher)
                servletRequest.getRequestDispatcher(zeroArgDispatchTarget);
            if (dispatcher != null) {
                origRequest.setOkToReinitializeAsync(true);
                origRequest.setAsyncStarted(false);
                pool.execute(new Handler(this, dispatcher, servletRequest,
                                         servletResponse));
            } else {
                log.warning("Unable to acquire RequestDispatcher for " +
                            zeroArgDispatchTarget);
            }
        }
    } 


    public void dispatch(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Null path");
        }
        origRequest.setAttribute(Globals.DISPATCHER_TYPE_ATTR,
                                 DispatcherType.ASYNC);
        ApplicationDispatcher dispatcher = (ApplicationDispatcher)
            servletRequest.getRequestDispatcher(path);
        if (dispatcher != null) {
            origRequest.setOkToReinitializeAsync(true);
            origRequest.setAsyncStarted(false);
            pool.execute(new Handler(this, dispatcher, servletRequest,
                                     servletResponse));
        } else {
            log.warning("Unable to acquire RequestDispatcher for " + path);
        }
    }


    public void dispatch(ServletContext context, String path) {
        if (path == null || context == null) {
            throw new IllegalArgumentException("Null context or path");
        }
        origRequest.setAttribute(Globals.DISPATCHER_TYPE_ATTR,
                                 DispatcherType.ASYNC);
        ApplicationDispatcher dispatcher = (ApplicationDispatcher)
            context.getRequestDispatcher(path);
        if (dispatcher != null) {
            origRequest.setOkToReinitializeAsync(true);
            origRequest.setAsyncStarted(false);
            pool.execute(new Handler(this, dispatcher, servletRequest,
                                     servletResponse));
        } else {
            log.warning("Unable to acquire RequestDispatcher for " + path +
                        "in servlet context " + context.getContextPath());
        }
    }


    public void complete() {
        complete(true);
    }


    private void complete(boolean checkIsAsyncStarted) {
        origRequest.asyncComplete(checkIsAsyncStarted);
    }


    public void start(Runnable run) {
        pool.execute(run);
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

        private final AsyncContextImpl asyncCtxt;
        private final ApplicationDispatcher dispatcher;
        private final ServletRequest request;
        private final ServletResponse response;

        Handler(AsyncContextImpl asyncCtxt, ApplicationDispatcher dispatcher,
                ServletRequest request, ServletResponse response) {
            this.asyncCtxt = asyncCtxt;
            this.dispatcher = dispatcher;
            this.request = request;
            this.response = response;
        }
       
        public void run() {
            try {
                dispatcher.dispatch(request, response, DispatcherType.ASYNC);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error during ASYNC dispatch", e);
            } finally {
                /* 
                 * Close the response after the dispatch target has
                 * completed execution, unless startAsync was called.
                 */
                if (!request.isAsyncStarted()) {
                    asyncCtxt.complete(false);
                }
            }
        }
    }

}
