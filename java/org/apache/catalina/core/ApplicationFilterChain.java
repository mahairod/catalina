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
import java.security.Principal;
import java.security.PrivilegedActionException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Globals;
import org.apache.catalina.InstanceEvent;
import org.apache.catalina.security.SecurityUtil;
import org.apache.catalina.util.InstanceSupport;
import org.apache.catalina.util.StringManager;

/**
 * Implementation of <code>javax.servlet.FilterChain</code> used to manage
 * the execution of a set of filters for a particular request.  When the
 * set of defined filters has all been executed, the next call to
 * <code>doFilter()</code> will execute the servlet's <code>service()</code>
 * method itself.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.6 $ $Date: 2006/11/21 17:39:39 $
 */

final class ApplicationFilterChain implements FilterChain {


    // -------------------------------------------------------------- Constants


    public static final int INCREMENT = 10;


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new chain instance with no defined filters.
     */
    public ApplicationFilterChain() {

        super();

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Filters.
     */
    private ApplicationFilterConfig[] filters = 
        new ApplicationFilterConfig[0];


    /**
     * The int which is used to maintain the current position 
     * in the filter chain.
     */
    private int pos = 0;


    /**
     * The int which gives the current number of filters in the chain.
     */
    private int n = 0;


    /**
     * The servlet instance to be executed by this chain.
     */
    private Servlet servlet = null;


    /**
     * The string manager for our package.
     */
    private static final StringManager sm =
      StringManager.getManager(Constants.Package);


    /**
     * The InstanceSupport instance associated with our Wrapper (used to
     * send "before filter" and "after filter" events.
     */
    private InstanceSupport support = null;

    
    /**
     * Static class array used when the SecurityManager is turned on and 
     * <code>doFilter</code is invoked.
     */
    private static Class[] classType = new Class[]{ServletRequest.class, 
                                                   ServletResponse.class,
                                                   FilterChain.class};
                                                   
    /**
     * Static class array used when the SecurityManager is turned on and 
     * <code>service</code is invoked.
     */                                                 
    private static Class[] classTypeUsedInService = new Class[]{
                                                         ServletRequest.class,
                                                         ServletResponse.class};

    // ---------------------------------------------------- FilterChain Methods


    /**
     * Invoke the next filter in this chain, passing the specified request
     * and response.  If there are no more filters in this chain, invoke
     * the <code>service()</code> method of the servlet itself.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void doFilter(ServletRequest request, ServletResponse response)
        throws IOException, ServletException {

        if (Globals.IS_SECURITY_ENABLED) {
            final ServletRequest req = request;
            final ServletResponse res = response;
            try {
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedExceptionAction() {
                        public Object run() 
                            throws ServletException, IOException {
                            internalDoFilter(req,res);
                            return null;
                        }
                    }
                );
            } catch( PrivilegedActionException pe) {
                Exception e = pe.getException();
                if (e instanceof ServletException)
                    throw (ServletException) e;
                else if (e instanceof IOException)
                    throw (IOException) e;
                else if (e instanceof RuntimeException)
                    throw (RuntimeException) e;
                else
                    throw new ServletException(e.getMessage(), e);
            }
        } else {
            internalDoFilter(request,response);
        }
    }

    private void internalDoFilter(ServletRequest request, 
                                  ServletResponse response)
        throws IOException, ServletException {

        // Call the next filter if there is one
        if (pos < n) {
            ApplicationFilterConfig filterConfig = filters[pos++];
            Filter filter = null;
            try {
                filter = filterConfig.getFilter();
                support.fireInstanceEvent(InstanceEvent.BEFORE_FILTER_EVENT,
                                          filter, request, response);
                
                if( SecurityUtil.isPackageProtectionEnabled() ) {
                    final ServletRequest req = request;
                    final ServletResponse res = response;
                    Principal principal = 
                        ((HttpServletRequest) req).getUserPrincipal();
                    Object[] filterType = new Object[3];

                    filterType[0] = req;
                    filterType[1] = res;
                    filterType[2] = this;
                    SecurityUtil.doAsPrivilege
                        ("doFilter", filter, classType, filterType);

                    filterType = null;
                } else {  
                    filter.doFilter(request, response, this);
                }

                support.fireInstanceEvent(InstanceEvent.AFTER_FILTER_EVENT,
                                          filter, request, response);
            } catch (IOException e) {
                if (filter != null)
                    support.fireInstanceEvent(InstanceEvent.AFTER_FILTER_EVENT,
                                              filter, request, response, e);
                throw e;
            } catch (ServletException e) {
                if (filter != null)
                    support.fireInstanceEvent(InstanceEvent.AFTER_FILTER_EVENT,
                                              filter, request, response, e);
                throw e;
            } catch (RuntimeException e) {
                if (filter != null)
                    support.fireInstanceEvent(InstanceEvent.AFTER_FILTER_EVENT,
                                              filter, request, response, e);
                throw e;
            } catch (Throwable e) {
                if (filter != null)
                    support.fireInstanceEvent(InstanceEvent.AFTER_FILTER_EVENT,
                                              filter, request, response, e);
                throw new ServletException
                  (sm.getString("filterChain.filter"), e);
            }
            return;
        }

        // We fell off the end of the chain -- call the servlet instance
        /* IASRI 4665318
        try {
            support.fireInstanceEvent(InstanceEvent.BEFORE_SERVICE_EVENT,
                                      servlet, request, response);
            if ((request instanceof HttpServletRequest) &&
                (response instanceof HttpServletResponse)) {
                    
                // START SJS WS 7.0 6236329
                //if( System.getSecurityManager() != null) {
                if ( SecurityUtil.executeUnderSubjectDoAs() ){
                // END OF SJS WS 7.0 6236329
                    final ServletRequest req = request;
                    final ServletResponse res = response;
                    Principal principal = 
                        ((HttpServletRequest) req).getUserPrincipal();

                    Object[] serviceType = new Object[2];
                    serviceType[0] = req;
                    serviceType[1] = res;
                    
                    SecurityUtil.doAsPrivilege("service",
                                               servlet,
                                               classTypeUsedInService, 
                                               serviceType,
                                               principal);                                                   
                    serviceType = null;
                } else {  
                    servlet.service((HttpServletRequest) request,
                                    (HttpServletResponse) response);
                }
            } else {
                servlet.service(request, response);
            }
            support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                      servlet, request, response);
        } catch (IOException e) {
            support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                      servlet, request, response, e);
            throw e;
        } catch (ServletException e) {
            support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                      servlet, request, response, e);
            throw e;
        } catch (RuntimeException e) {
            support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                      servlet, request, response, e);
            throw e;
        } catch (Throwable e) {
            support.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                      servlet, request, response, e);
            throw new ServletException
              (sm.getString("filterChain.servlet"), e);
        }

        */
        // START IASRI 4665318
        servletService(request, response, servlet, support);
        // END IASRI 4665318
    }


    // -------------------------------------------------------- Package Methods



    /**
     * Add a filter to the set of filters that will be executed in this chain.
     *
     * @param filterConfig The FilterConfig for the servlet to be executed
     */
    void addFilter(ApplicationFilterConfig filterConfig) {

        if (n == filters.length) {
            ApplicationFilterConfig[] newFilters =
                new ApplicationFilterConfig[n + INCREMENT];
            System.arraycopy(filters, 0, newFilters, 0, n);
            filters = newFilters;
        }
        filters[n++] = filterConfig;

    }


    /**
     * Release references to the filters and wrapper executed by this chain.
     */
    void release() {

        n = 0;
        pos = 0;
        servlet = null;
        support = null;

    }


    /**
     * Set the servlet that will be executed at the end of this chain.
     *
     * @param wrapper The Wrapper for the servlet to be executed
     */
    void setServlet(Servlet servlet) {

        this.servlet = servlet;

    }


    /**
     * Set the InstanceSupport object used for event notifications
     * for this filter chain.
     *
     * @param support The InstanceSupport object for our Wrapper
     */
    void setSupport(InstanceSupport support) {

        this.support = support;

    }


    // START IASRI 4665318

    static void servletService(ServletRequest request, 
                               ServletResponse response,
                               Servlet serv, InstanceSupport supp)
        throws IOException, ServletException {
        try {
            supp.fireInstanceEvent(InstanceEvent.BEFORE_SERVICE_EVENT,
                                   serv, request, response);
            if ((request instanceof HttpServletRequest) &&
                (response instanceof HttpServletResponse)) {
                    
                if ( SecurityUtil.executeUnderSubjectDoAs() ){
                    final ServletRequest req = request;
                    final ServletResponse res = response;
                    Principal principal = 
                        ((HttpServletRequest) req).getUserPrincipal();

                    Object[] serviceType = new Object[2];
                    serviceType[0] = req;
                    serviceType[1] = res;
                    
                    SecurityUtil.doAsPrivilege("service",
                                               serv,
                                               classTypeUsedInService, 
                                               serviceType,
                                               principal);                                                   
                    serviceType = null;
                } else {  
                    serv.service((HttpServletRequest) request,
                                 (HttpServletResponse) response);
                }
            } else {
                serv.service(request, response);
            }
            supp.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                   serv, request, response);
        } catch (IOException e) {
            supp.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                   serv, request, response, e);
            throw e;
        } catch (ServletException e) {
            supp.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                   serv, request, response, e);
            throw e;
        } catch (RuntimeException e) {
            supp.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                   serv, request, response, e);
            throw e;
        } catch (Throwable e) {
            supp.fireInstanceEvent(InstanceEvent.AFTER_SERVICE_EVENT,
                                   serv, request, response, e);
            throw new ServletException
              (sm.getString("filterChain.servlet"), e);
        }

    }
    // END IASRI 4665318
}
