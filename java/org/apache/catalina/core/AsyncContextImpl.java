/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.core;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.logging.*;
import javax.servlet.*;
import org.apache.catalina.connector.Request;

public class AsyncContextImpl implements AsyncContext {

    private static final Logger log = Logger.getLogger(AsyncContextImpl.class.getName());

    // Thread pool for async dispatches
    private static final ExecutorService pool =
        Executors.newCachedThreadPool();

    // The original (unwrapped) request
    private Request request;

    // The possibly wrapped request passed to ServletRequest.startAsync
    private ServletRequest servletRequest;

    // The possibly wrapped response passed to ServletRequest.startAsync    
    private ServletResponse servletResponse;


    /**
     * Constructor
     *
     * @param request the original (unwrapped) request
     * @param servletRequest the possibly wrapped request passed to
     * ServletRequest.startAsync
     * @param servletResponse the possibly wrapped response passed to
     * ServletRequest.startAsync
     */
    public AsyncContextImpl(Request request, ServletRequest servletRequest,
                            ServletResponse servletResponse) {
        this.request = request;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
    }


    public ServletRequest getRequest() {
        return servletRequest;
    }


    public ServletResponse getResponse() {
        return servletResponse;
    }


    public void forward() {
        // XXX
    } 


    public void forward(String path) {
        RequestDispatcher rd = servletRequest.getRequestDispatcher(path);
        if (rd != null) {
            pool.execute(new Handler(rd, servletRequest, servletResponse));
        }
    }


    public void forward(ServletContext context, String path) {
        RequestDispatcher rd = context.getRequestDispatcher(path);
        if (rd != null) {
            pool.execute(new Handler(rd, servletRequest, servletResponse));
        }
    }


    public void complete() {
        if (!request.isAsyncStarted()) {
            throw new IllegalStateException("Request not in async mode");
        }

        // TBD

        request.complete();
    }


    public void setTimeout(long timeout) {
        // XXX
    }


    public void start(Runnable run) {
        // XXX
    }


    static class Handler implements Runnable {

        private final RequestDispatcher dispatcher;
        private final ServletRequest request;
        private final ServletResponse response;

        Handler(RequestDispatcher dispatcher, ServletRequest request,
                ServletResponse response) {
            this.dispatcher = dispatcher;
            this.request = request;
            this.response = response;
        }
       
        public void run() {
            try {
                dispatcher.forward(request, response);
            } catch (Exception e) {
                // Log warning
            }
        }
    }

}
