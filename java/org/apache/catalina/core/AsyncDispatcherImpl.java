/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.core;

import java.util.concurrent.*;
import javax.servlet.*;

class AsyncDispatcherImpl implements AsyncDispatcher {

    // Thread pool for async dispatches
    private static final ExecutorService pool =
        Executors.newCachedThreadPool();

    // RequestDispatcher delegate
    private final ApplicationDispatcher dispatcher;

    /**
     * Constructor
     *
     * @param dispatcher the delegate RequestDispatcher that does the actual
     * forward
     */
    public AsyncDispatcherImpl(ApplicationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void forward(ServletRequest request, ServletResponse response) {
        pool.execute(new Handler(dispatcher, request, response));
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
                // RD.forward without committing the response
                dispatcher.forward(request, response, false);
            } catch (Exception e) {
                // Log warning
            }
        }
    }
}
