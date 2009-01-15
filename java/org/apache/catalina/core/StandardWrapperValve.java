/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 *
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




package org.apache.catalina.core;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.PropertyUtils;

import com.sun.grizzly.util.buf.MessageBytes;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.valves.ValveBase;

/**
 * Valve that implements the default basic behavior for the
 * <code>StandardWrapper</code> container implementation.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.10 $ $Date: 2007/05/05 05:31:54 $
 */

final class StandardWrapperValve
    extends ValveBase {

    private static Logger log = Logger.getLogger(
        StandardWrapperValve.class.getName());

    // ----------------------------------------------------- Instance Variables


    // Some JMX statistics. This vavle is associated with a StandardWrapper.
    // We exponse the StandardWrapper as JMX ( j2eeType=Servlet ). The fields
    // are here for performance.
    private long processingTimeMillis;
    private long maxTimeMillis;
    private volatile long minTimeMillis = Long.MAX_VALUE;
    private AtomicInteger requestCount = new AtomicInteger(0);
    private int errorCount;


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // --------------------------------------------------------- Public Methods


    /**
     * Invoke the servlet we are managing, respecting the rules regarding
     * servlet lifecycle and SingleThreadModel support.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    @Override
    public int invoke(Request request, Response response)
            throws IOException, ServletException {

        // Initialize local variables we may need
        boolean unavailable = false;
        Throwable throwable = null;
        // This should be a Request attribute...
        long t1=System.currentTimeMillis();
        requestCount.incrementAndGet();
        StandardWrapper wrapper = (StandardWrapper) getContainer();
        HttpRequest hrequest = (HttpRequest) request;
        Servlet servlet = null;
        /*
         * Create a request facade such that if the request was received
         * at the root context, and the root context is mapped to a
         * default-web-module, the default-web-module mapping is masked from
         * the application code to which the request facade is being passed.
         * For example, the request.facade's getContextPath() method will 
         * return "/", rather than the context root of the default-web-module,
         * in this case.
         */
        HttpServletRequest hreq = (HttpServletRequest) request.getRequest(true);
        HttpServletResponse hres =
            (HttpServletResponse) response.getResponse();

        // Check for the application being marked unavailable
        if (!((Context) wrapper.getParent()).getAvailable()) {
            /* S1AS 4878272
            hres.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                           sm.getString("standardContext.isUnavailable"));
            */
            // BEGIN S1AS 4878272
            hres.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setDetailMessage(sm.getString("standardContext.isUnavailable"));
            // END S1AS 4878272
            unavailable = true;
        }

        // Check for the servlet being marked unavailable
        if (!unavailable && wrapper.isUnavailable()) {
            log(sm.getString("standardWrapper.isUnavailable",
                             wrapper.getName()));
            if (hres == null) {
                ;       // NOTE - Not much we can do generically
            } else {
                long available = wrapper.getAvailable();
                if ((available > 0L) && (available < Long.MAX_VALUE)) {
                    hres.setDateHeader("Retry-After", available);
                    /* S1AS 4878272
                    hres.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                               sm.getString("standardWrapper.isUnavailable",
                                            wrapper.getName()));
                    */
                    // BEGIN S1AS 4878272
                    hres.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    response.setDetailMessage(
                                sm.getString("standardWrapper.isUnavailable",
                                             wrapper.getName()));
                    // END S1AS 4878272
                } else if (available == Long.MAX_VALUE) {
                    /* S1AS 4878272
                    hres.sendError(HttpServletResponse.SC_NOT_FOUND,
                               sm.getString("standardWrapper.notFound",
                                            wrapper.getName()));
                    */
                    // BEGIN S1AS 4878272
                    hres.sendError(HttpServletResponse.SC_NOT_FOUND);
                    response.setDetailMessage(
                                    sm.getString("standardWrapper.notFound",
                                                 wrapper.getName()));
                    // END S1AS 4878272
                }
            }
            unavailable = true;
        }

        // Allocate a servlet instance to process this request
        try {
            if (!unavailable) {
                servlet = wrapper.allocate();
            }
        } catch (UnavailableException e) {
            if (e.isPermanent()) {
                /* S1AS 4878272
                hres.sendError(HttpServletResponse.SC_NOT_FOUND,
                           sm.getString("standardWrapper.notFound",
                                        wrapper.getName()));
                */
                // BEGIN S1AS 4878272
                hres.sendError(HttpServletResponse.SC_NOT_FOUND);
                response.setDetailMessage(
                                sm.getString("standardWrapper.notFound",
                                             wrapper.getName()));
                // END S1AS 4878272
            } else {
                hres.setDateHeader("Retry-After", e.getUnavailableSeconds());
                /* S1AS 4878272
                hres.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                           sm.getString("standardWrapper.isUnavailable",
                                        wrapper.getName()));
                */
                // BEGIN S1AS 4878272
                hres.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setDetailMessage(
                                sm.getString("standardWrapper.isUnavailable",
                                             wrapper.getName()));
                // END S1AS 4878272
            }
        } catch (ServletException e) {
            log(sm.getString("standardWrapper.allocateException",
                             wrapper.getName()),
                StandardWrapper.getRootCause(e));
            throwable = e;
            exception(request, response, e);
            servlet = null;
        } catch (Throwable e) {
            log(sm.getString("standardWrapper.allocateException",
                             wrapper.getName()), e);
            throwable = e;
            exception(request, response, e);
            servlet = null;
        }

        // Acknowlege the request
        try {
            response.sendAcknowledgement();
        } catch (IOException e) {
            log(sm.getString("standardWrapper.acknowledgeException",
                             wrapper.getName()), e);
            throwable = e;
            exception(request, response, e);
        } catch (Throwable e) {
            log(sm.getString("standardWrapper.acknowledgeException",
                             wrapper.getName()), e);
            throwable = e;
            exception(request, response, e);
            servlet = null;
        }
        MessageBytes requestPathMB = null;
        if (hreq != null) {
            requestPathMB = hrequest.getRequestPathMB();
        }
        hreq.setAttribute
            (ApplicationFilterFactory.DISPATCHER_TYPE_ATTR,
            DispatcherType.REQUEST);
        hreq.setAttribute
            (ApplicationFilterFactory.DISPATCHER_REQUEST_PATH_ATTR,
            requestPathMB);
        // Create the filter chain for this request
        ApplicationFilterFactory factory =
            ApplicationFilterFactory.getInstance();
        ApplicationFilterChain filterChain =
            factory.createFilterChain((ServletRequest) request,
                                      wrapper, servlet);

        // Call the filter chain for this request
        // NOTE: This also calls the servlet's service() method
        try {
            String jspFile = wrapper.getJspFile();
            if (jspFile != null) {
                hreq.setAttribute(Globals.JSP_FILE_ATTR, jspFile);
            } 
            /* IASRI 4665318
            if ((servlet != null) && (filterChain != null)) {
                filterChain.doFilter(hreq, hres);
            }
            */
            // START IASRI 4665318
            if (servlet != null) {
                if (filterChain != null) {
                    filterChain.setRequest(request);
                    filterChain.setWrapper(wrapper);
                    filterChain.doFilter(hreq, hres);
                } else {
                    ApplicationFilterChain.servletService(
                        hreq, hres, servlet, wrapper, request);

                }
            }
            // END IASRI 4665318
        } catch (ClientAbortException e) {
            throwable = e;
            exception(request, response, e);
        } catch (IOException e) {
            log(sm.getString("standardWrapper.serviceException",
                             wrapper.getName()), e);
            throwable = e;
            exception(request, response, e);
        } catch (UnavailableException e) {
            log(sm.getString("standardWrapper.serviceException",
                             wrapper.getName()), e);
            //            throwable = e;
            //            exception(request, response, e);
            wrapper.unavailable(e);
            long available = wrapper.getAvailable();
            if ((available > 0L) && (available < Long.MAX_VALUE)) {
                hres.setDateHeader("Retry-After", available);
                /* S1AS 4878272
                hres.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                           sm.getString("standardWrapper.isUnavailable",
                                        wrapper.getName()));
                */
                // BEGIN S1AS 4878272
                hres.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.setDetailMessage(
                                sm.getString("standardWrapper.isUnavailable",
                                             wrapper.getName()));
                // END S1AS 4878272
            } else if (available == Long.MAX_VALUE) {
                /* S1AS 4878272
                hres.sendError(HttpServletResponse.SC_NOT_FOUND,
                            sm.getString("standardWrapper.notFound",
                                         wrapper.getName()));
                */
                // BEGIN S1AS 4878272
                hres.sendError(HttpServletResponse.SC_NOT_FOUND);
                response.setDetailMessage(
                                sm.getString("standardWrapper.notFound",
                                             wrapper.getName()));
                // END S1AS 4878272
            }
            // Do not save exception in 'throwable', because we
            // do not want to do exception(request, response, e) processing
        } catch (ServletException e) {
            Throwable rootCause = StandardWrapper.getRootCause(e);
            if (!(rootCause instanceof ClientAbortException)) {
                log(sm.getString("standardWrapper.serviceException",
                                 wrapper.getName()), rootCause);
            }
            throwable = e;
            exception(request, response, e);
        } catch (Throwable e) {
            log(sm.getString("standardWrapper.serviceException",
                             wrapper.getName()), e);
            throwable = e;
            exception(request, response, e);
        }

        // Release the filter chain (if any) for this request
        try {
            if (filterChain != null)
                filterChain.release();
        } catch (Throwable e) {
            log(sm.getString("standardWrapper.releaseFilters",
                             wrapper.getName()), e);
            if (throwable == null) {
                throwable = e;
                exception(request, response, e);
            }
        }

        // Deallocate the allocated servlet instance
        try {
            if (servlet != null) {
                wrapper.deallocate(servlet);
            }
        } catch (Throwable e) {
            log(sm.getString("standardWrapper.deallocateException",
                             wrapper.getName()), e);
            if (throwable == null) {
                throwable = e;
                exception(request, response, e);
            }
        }

        // If this servlet has been marked permanently unavailable,
        // unload it and release this instance
        try {
            if ((servlet != null) &&
                (wrapper.getAvailable() == Long.MAX_VALUE)) {
                wrapper.unload();
            }
        } catch (Throwable e) {
            log(sm.getString("standardWrapper.unloadException",
                             wrapper.getName()), e);
            if (throwable == null) {
                throwable = e;
                exception(request, response, e);
            }
        }
        long t2=System.currentTimeMillis();

        long time=t2-t1;
        processingTimeMillis += time;
        if( time > maxTimeMillis) maxTimeMillis = time;
        if( time < minTimeMillis) minTimeMillis = time;

        return END_PIPELINE;
    }


    /**
     * Tomcat style invocation.
     */
    @Override
    public void invoke(org.apache.catalina.connector.Request request,
                       org.apache.catalina.connector.Response response)
            throws IOException, ServletException {

        invoke((Request) request, (Response) response);

        return;
    }


    // -------------------------------------------------------- Private Methods


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

        org.apache.catalina.Logger logger = null;
        if (container != null)
            logger = container.getLogger();
        if (logger != null)
            logger.log("StandardWrapperValve[" + container.getName() + "]: "
                       + message);
        else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();
            System.out.println("StandardWrapperValve[" + containerName
                               + "]: " + message);
        }

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

        org.apache.catalina.Logger logger = null;
        if (container != null)
            logger = container.getLogger();
        if (logger != null)
            logger.log("StandardWrapperValve[" + container.getName() + "]: "
                       + message, throwable);
        else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();
            System.out.println("StandardWrapperValve[" + containerName
                               + "]: " + message);
            System.out.println("" + throwable);
            throwable.printStackTrace(System.out);
        }

    }


    /**
     * Handle the specified ServletException encountered while processing
     * the specified Request to produce the specified Response.  Any
     * exceptions that occur during generation of the exception report are
     * logged and swallowed.
     *
     * @param request The request being processed
     * @param response The response being generated
     * @param exception The exception that occurred (which possibly wraps
     *  a root cause exception
     */
    private void exception(Request request, Response response,
                           Throwable exception) {
        ServletRequest sreq = request.getRequest();
        sreq.setAttribute(Globals.EXCEPTION_ATTR, exception);

        ServletResponse sresponse = response.getResponse();
        
        /* GlassFish 6386229
        if (sresponse instanceof HttpServletResponse)
            ((HttpServletResponse) sresponse).setStatus
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        */
        // START GlassFish 6386229
        ((HttpServletResponse) sresponse).setStatus
            (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        // END GlassFish 6386229
    }

    public long getProcessingTimeMillis() {
        return processingTimeMillis;
    }

    public void setProcessingTimeMillis(long processingTimeMillis) {
        this.processingTimeMillis = processingTimeMillis;
    }

    public long getMaxTimeMillis() {
        return maxTimeMillis;
    }

    public void setMaxTimeMillis(long maxTimeMillis) {
        this.maxTimeMillis = maxTimeMillis;
    }

    public long getMinTimeMillis() {
        return minTimeMillis;
    }

    public void setMinTimeMillis(long minTimeMillis) {
        this.minTimeMillis = minTimeMillis;
    }

    public int getRequestCount() {
        return requestCount.get();
    }

    public void setRequestCount(int count) {
        this.requestCount.set(count);
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    // Don't register in JMX

    public ObjectName createObjectName(String domain, ObjectName parent)
            throws MalformedObjectNameException {
        return null;
    }
}
