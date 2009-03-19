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

    private String dispatchTargetURI = null;


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
            dispatchTargetURI = origRequest.getRequestURI();
        } else if (servletRequest instanceof HttpServletRequest) {
            dispatchTargetURI =
                ((HttpServletRequest)servletRequest).getRequestURI();
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
        if (dispatchTargetURI == null) {
            log.severe("Unable to determine target of zero-arg dispatch");
        } else {
            origRequest.setAttribute(Globals.DISPATCHER_TYPE_ATTR,
                                     DispatcherType.ASYNC);
            ApplicationDispatcher dispatcher = (ApplicationDispatcher)
                servletRequest.getRequestDispatcher(dispatchTargetURI);
            if (dispatcher != null) {
                origRequest.setOkToReinitializeAsync();
                origRequest.setAsyncStarted(false);
                pool.execute(new Handler(dispatcher, servletRequest,
                                         servletResponse));
            } else {
                log.warning("Unable to acquire RequestDispatcher for " +
                            dispatchTargetURI);
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
            origRequest.setOkToReinitializeAsync();
            origRequest.setAsyncStarted(false);
            pool.execute(new Handler(dispatcher, servletRequest,
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
            origRequest.setOkToReinitializeAsync();
            origRequest.setAsyncStarted(false);
            pool.execute(new Handler(dispatcher, servletRequest,
                                     servletResponse));
        } else {
            log.warning("Unable to acquire RequestDispatcher for " + path +
                        "in servlet context " + context.getContextPath());
        }
    }


    public void complete() {
        if (!origRequest.isAsyncStarted()) {
            throw new IllegalStateException("Request not in async mode");
        }

        origRequest.asyncComplete();
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
            dispatchTargetURI = origRequest.getRequestURI();
        } else if (servletRequest instanceof HttpServletRequest) {
            dispatchTargetURI =
                ((HttpServletRequest)servletRequest).getRequestURI();
        } else {
            log.warning("Unable to determine target of " +
                        "zero-argument dispatch");
        }
    }


    static class Handler implements Runnable {

        private final ApplicationDispatcher dispatcher;
        private final ServletRequest request;
        private final ServletResponse response;

        Handler(ApplicationDispatcher dispatcher, ServletRequest request,
                ServletResponse response) {
            this.dispatcher = dispatcher;
            this.request = request;
            this.response = response;
        }
       
        public void run() {
            try {
                dispatcher.dispatch(request, response, DispatcherType.ASYNC);
            } catch (Exception e) {
                log.log(Level.WARNING, "Error during ASYNC dispatch", e);
            }
        }
    }

}
