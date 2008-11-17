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
import java.io.PrintWriter;
import java.util.logging.*;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.InstanceEvent;
//START OF 6364900
import org.apache.catalina.Manager;
//END OF 6364900
//START OF 6364900
import org.apache.catalina.Session;
//END OF 6364900
import org.apache.catalina.Wrapper;
import static org.apache.catalina.InstanceEvent.EventType.AFTER_DISPATCH_EVENT;
import org.apache.catalina.core.StandardWrapper;
//START OF 6364900
import org.apache.catalina.session.StandardSession;
//END OF 6364900
import org.apache.catalina.util.InstanceSupport;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.catalina.security.SecurityUtil;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.ResponseFacade;

/**
 * Standard implementation of <code>RequestDispatcher</code> that allows a
 * request to be forwarded to a different resource to create the ultimate
 * response, or to include the output of another resource in the response
 * from this resource.  This implementation allows application level servlets
 * to wrap the request and/or response objects that are passed on to the
 * called resource, as long as the wrapping classes extend
 * <code>javax.servlet.ServletRequestWrapper</code> and
 * <code>javax.servlet.ServletResponseWrapper</code>.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.16 $ $Date: 2007/02/26 22:57:08 $
 */

final class ApplicationDispatcher
    implements RequestDispatcher {

    protected class PrivilegedForward implements PrivilegedExceptionAction {
        private ServletRequest request;
        private ServletResponse response;

        PrivilegedForward(ServletRequest request, ServletResponse response)
        {
            this.request = request;
            this.response = response;
        }

        public Object run() throws java.lang.Exception {
            doForward(request,response);
            return null;
        }
    }

    protected class PrivilegedInclude implements PrivilegedExceptionAction {
        private ServletRequest request;
        private ServletResponse response;

        PrivilegedInclude(ServletRequest request, ServletResponse response)
        {
            this.request = request;
            this.response = response;
        }

        public Object run() throws ServletException, IOException {
            doInclude(request,response);
            return null;
        }
    }

    /**
     * Used to pass state when the request dispatcher is used. Using instance
     * variables causes threading issues and state is too complex to pass and
     * return single ServletRequest or ServletResponse objects.
     */
    private class State {
        State(ServletRequest request, ServletResponse response,
                boolean including) {
            this.outerRequest = request;
            this.outerResponse = response;
            this.including = including;
        }

        /**
         * The outermost request that will be passed on to the invoked servlet.
         */
        ServletRequest outerRequest = null;


        /**
         * The outermost response that will be passed on to the invoked servlet.
         */
        ServletResponse outerResponse = null;
        
        /**
         * The request wrapper we have created and installed (if any).
         */
        ServletRequest wrapRequest = null;


        /**
         * The response wrapper we have created and installed (if any).
         */
        ServletResponse wrapResponse = null;
        
        /**
         * Are we performing an include() instead of a forward()?
         */
        boolean including = false;
    }

    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class, configured according to the
     * specified parameters.  If both servletPath and pathInfo are
     * <code>null</code>, it will be assumed that this RequestDispatcher
     * was acquired by name, rather than by path.
     *
     * @param wrapper The Wrapper associated with the resource that will
     *  be forwarded to or included (required)
     * @param requestURI The request URI to this resource (if any)
     * @param servletPath The revised servlet path to this resource (if any)
     * @param pathInfo The revised extra path information to this resource
     *  (if any)
     * @param queryString Query string parameters included with this request
     *  (if any)
     * @param name Servlet name (if a named dispatcher was created)
     *  else <code>null</code>
     */
    public ApplicationDispatcher
        (Wrapper wrapper, String requestURI, String servletPath,
         String pathInfo, String queryString, String name) {

        super();

        // Save all of our configuration parameters
        this.wrapper = wrapper;
        this.context = (Context) wrapper.getParent();
        this.requestURI = requestURI;
        this.servletPath = servletPath;
        this.pathInfo = pathInfo;
        this.queryString = queryString;
        this.name = name;

        if (log.isLoggable(Level.FINE))
            log.fine("servletPath=" + this.servletPath + ", pathInfo=" +
                this.pathInfo + ", queryString=" + queryString +
                ", name=" + this.name);

    }


    // ----------------------------------------------------- Instance Variables

    private static Logger log = Logger.getLogger(
        ApplicationDispatcher.class.getName());


    //START OF 6364900
    /**
     * is this dispatch cross context
     */
    private Boolean crossContextFlag = null;
    //END OF 6364900


    /**
     * The Context this RequestDispatcher is associated with.
     */
    private Context context = null;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * Descriptive information about this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.ApplicationDispatcher/1.0";


    /**
     * The servlet name for a named dispatcher.
     */
    private String name = null;


    /**
     * The extra path information for this RequestDispatcher.
     */
    private String pathInfo = null;


    /**
     * The query string parameters for this RequestDispatcher.
     */
    private String queryString = null;


    /**
     * The request URI for this RequestDispatcher.
     */
    private String requestURI = null;


    /**
     * The servlet path for this RequestDispatcher.
     */
    private String servletPath = null;


    /**
     * The StringManager for this package.
     */
    private static final StringManager sm =
      StringManager.getManager(Constants.Package);


    /**
     * The Wrapper associated with the resource that will be forwarded to
     * or included.
     */
    private Wrapper wrapper = null;


    // ------------------------------------------------------------- Properties


    /**
     * Return the descriptive information about this implementation.
     */
    public String getInfo() {

        return (this.info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Forward this request and response to another resource for processing.
     * Any runtime exception, IOException, or ServletException thrown by the
     * called servlet will be propogated to the caller.
     *
     * @param request The servlet request to be forwarded
     * @param response The servlet response to be forwarded
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void forward(ServletRequest request, ServletResponse response)
            throws ServletException, IOException {
        forward(request, response, true);
    }

    void forward(ServletRequest request, ServletResponse response,
                 boolean isCommit)
            throws ServletException, IOException {

        if (Globals.IS_SECURITY_ENABLED) {
            try {
                PrivilegedForward dp = new PrivilegedForward(request,response);
                AccessController.doPrivileged(dp);
                // START SJSAS 6374990
                if (isCommit) {
                    ApplicationDispatcherForward.commit(
                        (HttpServletRequest) request,
                        (HttpServletResponse) response,
                        context, wrapper);
                }
                // END SJSAS 6374990
            } catch (PrivilegedActionException pe) {
                Exception e = pe.getException();
                if (e instanceof ServletException)
                    throw (ServletException) e;
                throw (IOException) e;
            }
        } else {
            doForward(request,response);
            // START SJSAS 6374990
            if (isCommit) {
                ApplicationDispatcherForward.commit(
                    (HttpServletRequest) request,
                    (HttpServletResponse) response,
                    context, wrapper);
            }
            // END SJSAS 6374990
        }
    }

    private void doForward(ServletRequest request, ServletResponse response)
        throws ServletException, IOException
    {
        
        // Reset any output that has been buffered, but keep headers/cookies
        if (response.isCommitted()) {
            if (log.isLoggable(Level.FINE))
                log.fine("  Forward on committed response --> ISE");
            throw new IllegalStateException
                (sm.getString("applicationDispatcher.forward.ise"));
        }
        try {
            response.resetBuffer();
        } catch (IllegalStateException e) {
            if (log.isLoggable(Level.FINE))
                log.fine("  Forward resetBuffer() returned ISE: " + e);
            throw e;
        }

        // Set up to handle the specified request and response
        State state = new State(request, response, false);

        // Identify the HTTP-specific request and response objects (if any)
        HttpServletRequest hrequest = null;
        /* GlassFish 6386229
        if (request instanceof HttpServletRequest)
            hrequest = (HttpServletRequest) request;
        */    
        // START GlassFish 6386229
        hrequest = (HttpServletRequest) request;
        // END GlassFish 6386229
        HttpServletResponse hresponse = null;
        /* GlassFish 6386229
        if (response instanceof HttpServletResponse)
            hresponse = (HttpServletResponse) response;
        */    
        // START GlassFish 6386229
        hresponse = (HttpServletResponse) response;
        // END GlassFish 6386229

        // Handle a non-HTTP forward by passing the existing request/response
        if ((hrequest == null) || (hresponse == null)) {

            if (log.isLoggable(Level.FINE))
                log.fine(" Non-HTTP Forward");
            
            processRequest(hrequest,hresponse,state);

        }

        // Handle an HTTP named dispatcher forward
        else if ((servletPath == null) && (pathInfo == null)) {

            if (log.isLoggable(Level.FINE))
                log.fine(" Named Dispatcher Forward");

            ApplicationHttpRequest wrequest =
                (ApplicationHttpRequest) wrapRequest(state);
            wrequest.setRequestURI(hrequest.getRequestURI());
            wrequest.setContextPath(hrequest.getContextPath());
            wrequest.setServletPath(hrequest.getServletPath());
            wrequest.setPathInfo(hrequest.getPathInfo());
            wrequest.setQueryString(hrequest.getQueryString());
            
            processRequest(request,response,state);

            wrequest.recycle();
            unwrapRequest(state);
        }

        // Handle an HTTP path-based forward
        else {

            if (log.isLoggable(Level.FINE))
                log.fine(" Path Based Forward");

            ApplicationHttpRequest wrequest =
                (ApplicationHttpRequest) wrapRequest(state);
            String contextPath = context.getPath();

            if (hrequest.getAttribute(Globals.FORWARD_REQUEST_URI_ATTR) == null) { 
                wrequest.initSpecialAttributes(false,
                                               hrequest.getRequestURI(),
                                               hrequest.getContextPath(),
                                               hrequest.getServletPath(),
                                               hrequest.getPathInfo(),
                                               hrequest.getQueryString());
            }
 
            wrequest.setContextPath(contextPath);
            wrequest.setRequestURI(requestURI);
            wrequest.setServletPath(servletPath);
            wrequest.setPathInfo(pathInfo);
            if (queryString != null) {
                wrequest.setQueryString(queryString);
                wrequest.setQueryParams(queryString);
            }

            processRequest(request,response,state);

            wrequest.recycle();
            unwrapRequest(state);

        }

        /* SJSAS 6374990
        // This is not a real close in order to support error processing
        if ( log.isDebugEnabled() )
            log.debug(" Disabling the response for futher output");

        if  (response instanceof ResponseFacade) {
            ((ResponseFacade) response).finish();
        } else {
            // Servlet SRV.6.2.2. The Resquest/Response may have been wrapped
            // and may no longer be instance of RequestFacade 
            if (log.isDebugEnabled()){
                log.debug( " The Response is vehiculed using a wrapper: " 
                           + response.getClass().getName() );
            }

            // Close anyway
            try {
                PrintWriter writer = response.getWriter();
                writer.close();
            } catch (IllegalStateException e) {
                try {
                    ServletOutputStream stream = response.getOutputStream();
                    stream.close();
                } catch (IllegalStateException f) {
                    ;
                } catch (IOException f) {
                    ;
                }
            } catch (IOException e) {
                ;
            }
        }
        */

    }

    

    /**
     * Prepare the request based on the filter configuration.
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    private void processRequest(ServletRequest request, 
                                ServletResponse response,
                                State state)
        throws IOException, ServletException {
                
        if (request != null) {
            Integer disInt = (Integer) request.getAttribute
                (ApplicationFilterFactory.DISPATCHER_TYPE_ATTR);
            if (disInt != null) {
                if (disInt.intValue() != ApplicationFilterFactory.ERROR) {
                    state.outerRequest.setAttribute
                        (ApplicationFilterFactory.DISPATCHER_REQUEST_PATH_ATTR,
                        getCombinedPath());
                    state.outerRequest.setAttribute
                        (ApplicationFilterFactory.DISPATCHER_TYPE_ATTR,
                        Integer.valueOf(ApplicationFilterFactory.FORWARD));
                    invoke(state.outerRequest, response, state);
                } else {
                    invoke(state.outerRequest, response, state);
                }
            }
        }

    }
    
    
    /**
     * Combine the servletPath and the pathInfo. If pathInfo is
     * <code>null</code> it is ignored. If servletPath is <code>null</code> then
     * <code>null</code> is returned.
     * @return The combined path with pathInfo appended to servletInfo
     */
    private String getCombinedPath() {
        if (servletPath == null) {
            return null;
        }
        if (pathInfo == null) {
            return servletPath;
        }
        return servletPath + pathInfo;
    }
    

    /**
     * Include the response from another resource in the current response.
     * Any runtime exception, IOException, or ServletException thrown by the
     * called servlet will be propogated to the caller.
     *
     * @param request The servlet request that is including this one
     * @param response The servlet response to be appended to
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void include(ServletRequest request, ServletResponse response)
        throws ServletException, IOException
    {
        if (Globals.IS_SECURITY_ENABLED) {
            try {
                PrivilegedInclude dp = new PrivilegedInclude(request,response);
                AccessController.doPrivileged(dp);
            } catch (PrivilegedActionException pe) {
                Exception e = pe.getException();

                if (e instanceof ServletException)
                    throw (ServletException) e;
                throw (IOException) e;
            }
        } else {
            doInclude(request,response);
        }
    }

    private void doInclude(ServletRequest request, ServletResponse response)
        throws ServletException, IOException
    {

        // Set up to handle the specified request and response
        State state = new State(request, response, true);

        // Create a wrapped response to use for this request
        wrapResponse(state);

        // Handle a non-HTTP include
        /* GlassFish 6386229
        if (!(request instanceof HttpServletRequest) ||
            !(response instanceof HttpServletResponse)) {

            if ( log.isDebugEnabled() )
                log.debug(" Non-HTTP Include");
            request.setAttribute(ApplicationFilterFactory.DISPATCHER_TYPE_ATTR,
                                             Integer.valueOf(ApplicationFilterFactory.INCLUDE));
            request.setAttribute(ApplicationFilterFactory.DISPATCHER_REQUEST_PATH_ATTR, 
                                             //origServletPath);
                                             servletPath);
            try{
                invoke(request, state.outerResponse, state);
            } finally {
                unwrapResponse(state);
            }
        }

        // Handle an HTTP named dispatcher include
        else if (name != null) {
        */
        // START GlassFish 6386229
        // Handle an HTTP named dispatcher include
        if (name != null) {
        // END GlassFish 6386229
            if (log.isLoggable(Level.FINE))
                log.fine("Named Dispatcher Include");

            ApplicationHttpRequest wrequest =
                (ApplicationHttpRequest) wrapRequest(state);
            wrequest.setAttribute(Globals.NAMED_DISPATCHER_ATTR, name);
            if (servletPath != null)
                wrequest.setServletPath(servletPath);
            wrequest.setAttribute(
                ApplicationFilterFactory.DISPATCHER_TYPE_ATTR,
                Integer.valueOf(ApplicationFilterFactory.INCLUDE));
            wrequest.setAttribute(
                ApplicationFilterFactory.DISPATCHER_REQUEST_PATH_ATTR,
                getCombinedPath());
            try{
                invoke(state.outerRequest, state.outerResponse, state);
            } finally {
                wrequest.recycle();
                unwrapRequest(state);
                unwrapResponse(state);
            }

        }

        // Handle an HTTP path based include
        else {

            if (log.isLoggable(Level.FINE))
                log.fine("Path Based Include");

            ApplicationHttpRequest wrequest =
                (ApplicationHttpRequest) wrapRequest(state);
            wrequest.initSpecialAttributes(true, 
                                           requestURI,
                                           context.getPath(),
                                           servletPath,
                                           pathInfo,
                                           queryString);
            wrequest.setQueryParams(queryString);
            
            wrequest.setAttribute(
                ApplicationFilterFactory.DISPATCHER_TYPE_ATTR,
                Integer.valueOf(ApplicationFilterFactory.INCLUDE));
            wrequest.setAttribute(
                ApplicationFilterFactory.DISPATCHER_REQUEST_PATH_ATTR,
                getCombinedPath());
            try{
                invoke(state.outerRequest, state.outerResponse, state);
            } finally {
                wrequest.recycle();
                unwrapRequest(state);
                unwrapResponse(state);
           }

        }

    }


    // -------------------------------------------------------- Private Methods
    
    
    /**
     * Ask the resource represented by this RequestDispatcher to process
     * the associated request, and create (or append to) the associated
     * response.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: This implementation assumes
     * that no filters are applied to a forwarded or included resource,
     * because they were already done for the original request.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    private void invoke(ServletRequest request, ServletResponse response,
        State state) throws IOException, ServletException {
        //START OF 6364900 original invoke has been renamed to doInvoke
        boolean crossContext = false;
        if(crossContextFlag != null && crossContextFlag.booleanValue()) {
            crossContext = true;
        }
        if(crossContext) {
            context.getManager().lockSession(request); 
        }       
        try {
            if(crossContext) {
                context.getManager().preRequestDispatcherProcess(request, response);
            }            
            doInvoke(request, response, crossContext);
            if(crossContext) {
                context.getManager().postRequestDispatcherProcess(request, response);
            }
        } finally {
            if(crossContext) {
                context.getManager().unlockSession(request);
            }
            crossContextFlag = null;
        }
        //END OF 6364900
    }
    
    
    /**
     * Ask the resource represented by this RequestDispatcher to process
     * the associated request, and create (or append to) the associated
     * response.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: This implementation assumes
     * that no filters are applied to a forwarded or included resource,
     * because they were already done for the original request.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param crossContext true if the request dispatch is crossing context
     * boundaries, false otherwise
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    private void doInvoke(ServletRequest request, ServletResponse response,
                          boolean crossContext)
            throws IOException, ServletException {

        // Checking to see if the context classloader is the current context
        // classloader. If it's not, we're saving it, and setting the context
        // classloader to the Context classloader
        ClassLoader oldCCL = null;
        if (crossContext) {
            oldCCL = Thread.currentThread().getContextClassLoader();
            ClassLoader contextClassLoader = context.getLoader().getClassLoader();
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        // Initialize local variables we may need
        HttpServletRequest hrequest = null;
        /* GlassFish 6386229
        if (request instanceof HttpServletRequest)
            hrequest = (HttpServletRequest) request;
        */
        // START GlassFish 6386229
        hrequest = (HttpServletRequest) request;
        // END GlassFish 6386229
        HttpServletResponse hresponse = null;
        /* GlassFish 6386229
        if (response instanceof HttpServletResponse)
            hresponse = (HttpServletResponse) response;
        */
        // START GlassFish 6386229
        hresponse = (HttpServletResponse) response;
        // END GlassFish 6386229
        Servlet servlet = null;
        IOException ioException = null;
        ServletException servletException = null;
        RuntimeException runtimeException = null;
        boolean unavailable = false;
              

        // Check for the servlet being marked unavailable
        if (wrapper.isUnavailable()) {
            log(sm.getString("applicationDispatcher.isUnavailable",
                             wrapper.getName()));
            if (hresponse == null) {
                ;       // NOTE - Not much we can do generically
            } else {
                long available = wrapper.getAvailable();
                if ((available > 0L) && (available < Long.MAX_VALUE))
                    hresponse.setDateHeader("Retry-After", available);
                hresponse.sendError
                    (HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                     sm.getString("applicationDispatcher.isUnavailable",
                                  wrapper.getName()));
            }
            unavailable = true;
        }

        // Allocate a servlet instance to process this request
        try {
            if (!unavailable) {
                //                if (debug >= 2)
                //                    log("  Allocating servlet instance");
                servlet = wrapper.allocate();
                //                if ((debug >= 2) && (servlet == null))
                //                    log("    No servlet instance returned!");
            }
        } catch (ServletException e) {
            log(sm.getString("applicationDispatcher.allocateException",
                             wrapper.getName()),
                             StandardWrapper.getRootCause(e));
            servletException = e;
            servlet = null;
        } catch (Throwable e) {
            log(sm.getString("applicationDispatcher.allocateException",
                             wrapper.getName()), e);
            servletException = new ServletException
                (sm.getString("applicationDispatcher.allocateException",
                              wrapper.getName()), e);
            servlet = null;
        }
                
        // Get the FilterChain Here
        ApplicationFilterFactory factory = ApplicationFilterFactory.getInstance();
        ApplicationFilterChain filterChain = factory.createFilterChain(
            request, wrapper, servlet);

        // START OF S1AS 4703023
        Request origRequest = null;
        // END OF S1AS 4703023

        InstanceSupport support = ((StandardWrapper) wrapper).getInstanceSupport();

        // Call the service() method for the allocated servlet instance
        try {
            String jspFile = wrapper.getJspFile();
            if (jspFile != null) {
                request.setAttribute(Globals.JSP_FILE_ATTR, jspFile);
            } 
            support.fireInstanceEvent(InstanceEvent.EventType.BEFORE_DISPATCH_EVENT,
                                      servlet, request, response);
            // for includes/forwards
            /* IASRI 4665318
            if ((servlet != null) && (filterChain != null)) {
            */
            // START IASRI 4665318
            if (servlet != null) {
            // END IASRI 4665318
                // START OF S1AS 4703023
                origRequest = getCoyoteRequest(request);
                if (origRequest != null) {
                    origRequest.incrementDispatchDepth();
                    if (origRequest.isMaxDispatchDepthReached())
                        throw new ServletException(sm.getString(
                            "applicationDispatcher.maxDispatchDepthReached",
                            new Object[] {
                                Integer.valueOf(origRequest.getMaxDispatchDepth())}));
                }
                // END OF S1AS 4703023 
                /* IASRI 4665318
                filterChain.doFilter(request, response);
                */
                // START IASRI 4665318
                if (filterChain != null) {
                    filterChain.setRequest(origRequest);
                    filterChain.setWrapper((StandardWrapper)wrapper);
                    filterChain.doFilter(request, response);
                } else {
                    ApplicationFilterChain.servletService(
                        request, response, servlet, (StandardWrapper)wrapper,
                        origRequest);
                }
                // END IASRI 4665318
            }
            // Servlet Service Method is called by the FilterChain
            support.fireInstanceEvent(AFTER_DISPATCH_EVENT,
                                      servlet, request, response);
        } catch (ClientAbortException e) {
            support.fireInstanceEvent(AFTER_DISPATCH_EVENT,
                                      servlet, request, response);
            ioException = e;
        } catch (IOException e) {
            support.fireInstanceEvent(AFTER_DISPATCH_EVENT,
                                      servlet, request, response);
            log(sm.getString("applicationDispatcher.serviceException",
                             wrapper.getName()), e);
            ioException = e;
        } catch (UnavailableException e) {
            support.fireInstanceEvent(AFTER_DISPATCH_EVENT,
                                      servlet, request, response);
            log(sm.getString("applicationDispatcher.serviceException",
                             wrapper.getName()), e);
            servletException = e;
            wrapper.unavailable(e);
        } catch (ServletException e) {
            support.fireInstanceEvent(AFTER_DISPATCH_EVENT,
                                      servlet, request, response);
            Throwable rootCause = StandardWrapper.getRootCause(e);
            if (!(rootCause instanceof ClientAbortException)) {
                log(sm.getString("applicationDispatcher.serviceException",
                    wrapper.getName()), rootCause);
            }
            servletException = e;
        } catch (RuntimeException e) {
            support.fireInstanceEvent(AFTER_DISPATCH_EVENT,
                                      servlet, request, response);
            log(sm.getString("applicationDispatcher.serviceException",
                             wrapper.getName()), e);
            runtimeException = e;
        // START OF S1AS 4703023
        } finally {
            if (origRequest != null)
                origRequest.decrementDispatchDepth();
        // END OF S1AS 4703023
        }

        // Release the filter chain (if any) for this request
        try {
            if (filterChain != null)
                filterChain.release();
        } catch (Throwable e) {
            log.log(Level.SEVERE,
                    sm.getString("standardWrapper.releaseFilters",
                                 wrapper.getName()),
                    e);
          //FIXME Exception handling needs to be simpiler to what is in the StandardWrapperValue
        }

        // Deallocate the allocated servlet instance
        try {
            if (servlet != null) {
                wrapper.deallocate(servlet);
            }
        } catch (ServletException e) {
            log(sm.getString("applicationDispatcher.deallocateException",
                             wrapper.getName()), e);
            servletException = e;
        } catch (Throwable e) {
            log(sm.getString("applicationDispatcher.deallocateException",
                             wrapper.getName()), e);
            servletException = new ServletException
                (sm.getString("applicationDispatcher.deallocateException",
                              wrapper.getName()), e);
        }

        // Reset the old context class loader
        if (oldCCL != null)
            Thread.currentThread().setContextClassLoader(oldCCL);

        // Rethrow an exception if one was thrown by the invoked servlet
        if (ioException != null)
            throw ioException;
        if (servletException != null)
            throw servletException;
        if (runtimeException != null)
            throw runtimeException;

    }


    /**
     * Log a message on the Logger associated with our Context (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {
        org.apache.catalina.Logger logger = context.getLogger();
        if (logger != null)
            logger.log("ApplicationDispatcher[" + context.getPath() +
                       "]: " + message);
        else
            System.out.println("ApplicationDispatcher[" +
                               context.getPath() + "]: " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {
        org.apache.catalina.Logger logger = context.getLogger();
        if (logger != null)
            logger.log("ApplicationDispatcher[" + context.getPath() +
                       "] " + message, throwable);
        else {
            System.out.println("ApplicationDispatcher[" +
                               context.getPath() + "]: " + message);
            throwable.printStackTrace(System.out);
        }

    }


    /**
     * Unwrap the request if we have wrapped it.
     */
    private void unwrapRequest(State state) {

        if (state.wrapRequest == null)
            return;

        ServletRequest previous = null;
        ServletRequest current = state.outerRequest;
        while (current != null) {

            // If we run into the container request we are done
            if ((current instanceof org.apache.catalina.Request)
                || (current instanceof RequestFacade))
                break;

            // Remove the current request if it is our wrapper
            if (current == state.wrapRequest) {
                ServletRequest next =
                  ((ServletRequestWrapper) current).getRequest();
                if (previous == null)
                    state.outerRequest = next;
                else
                    ((ServletRequestWrapper) previous).setRequest(next);
                break;
            }

            // Advance to the next request in the chain
            previous = current;
            current = ((ServletRequestWrapper) current).getRequest();

        }

    }


    /**
     * Unwrap the response if we have wrapped it.
     */
    private void unwrapResponse(State state) {

        if (state.wrapResponse == null)
            return;

        ServletResponse previous = null;
        ServletResponse current = state.outerResponse;
        while (current != null) {

            // If we run into the container response we are done
            if ((current instanceof org.apache.catalina.Response)
                || (current instanceof ResponseFacade))
                break;

            // Remove the current response if it is our wrapper
            if (current == state.wrapResponse) {
                ServletResponse next =
                  ((ServletResponseWrapper) current).getResponse();
                if (previous == null)
                    state.outerResponse = next;
                else
                    ((ServletResponseWrapper) previous).setResponse(next);
                break;
            }

            // Advance to the next response in the chain
            previous = current;
            current = ((ServletResponseWrapper) current).getResponse();

        }

    }


    /**
     * Create and return a request wrapper that has been inserted in the
     * appropriate spot in the request chain.
     */
    private ServletRequest wrapRequest(State state) {

        // Locate the request we should insert in front of
        ServletRequest previous = null;
        ServletRequest current = state.outerRequest;
        while (current != null) {
            if ("org.apache.catalina.servlets.InvokerHttpRequest".
                equals(current.getClass().getName()))
                break; // KLUDGE - Make nested RD.forward() using invoker work
            if (!(current instanceof ServletRequestWrapper))
                break;
            if (current instanceof ApplicationHttpRequest)
                break;
            if (current instanceof ApplicationRequest)
                break;
            if (current instanceof org.apache.catalina.Request)
                break;
            previous = current;
            current = ((ServletRequestWrapper) current).getRequest();
        }

        // Instantiate a new wrapper at this point and insert it in the chain
        ServletRequest wrapper = null;
        if ((current instanceof ApplicationHttpRequest) ||
            (current instanceof HttpRequest) ||
            (current instanceof HttpServletRequest)) {
            // Compute a crossContext flag
            HttpServletRequest hcurrent = (HttpServletRequest) current;
            boolean crossContext = 
                !(context.getPath().equals(hcurrent.getContextPath()));
            //START OF 6364900
            crossContextFlag = Boolean.valueOf(crossContext);
            //END OF 6364900
            wrapper = new ApplicationHttpRequest
                (hcurrent, context, crossContext);
        } else {
            wrapper = new ApplicationRequest(current);
        }
        if (previous == null)
            state.outerRequest = wrapper;
        else
            ((ServletRequestWrapper) previous).setRequest(wrapper);
        state.wrapRequest = wrapper;
        return (wrapper);

    }


    /**
     * Create and return a response wrapper that has been inserted in the
     * appropriate spot in the response chain.
     */
    private ServletResponse wrapResponse(State state) {

        // Locate the response we should insert in front of
        ServletResponse previous = null;
        ServletResponse current = state.outerResponse;
        while (current != null) {
            if (!(current instanceof ServletResponseWrapper))
                break;
            if (current instanceof ApplicationHttpResponse)
                break;
            if (current instanceof ApplicationResponse)
                break;
            if (current instanceof org.apache.catalina.Response)
                break;
            previous = current;
            current = ((ServletResponseWrapper) current).getResponse();
        }

        // Instantiate a new wrapper at this point and insert it in the chain
        ServletResponse wrapper = null;
        if ((current instanceof ApplicationHttpResponse) ||
            (current instanceof HttpResponse) ||
            (current instanceof HttpServletResponse))
            wrapper =
                new ApplicationHttpResponse((HttpServletResponse) current,
                                            state.including);
        else
            wrapper = new ApplicationResponse(current, state.including);
        if (previous == null)
            state.outerResponse = wrapper;
        else
            ((ServletResponseWrapper) previous).setResponse(wrapper);
        state.wrapResponse = wrapper;
        return (wrapper);

    }


    // START OF S1AS 4703023
    /**
     * Finds and returns the underlying/original request object.
     *
     * (Doing instanceof in a loop will impact performance)
     */
    private Request getCoyoteRequest(ServletRequest request) {

        Request coyoteRequest = null;
        Object current = request;
        while (current != null) {
            // When we run into the original request object, return it
            if (current instanceof RequestFacade) {
                coyoteRequest = ((RequestFacade)current).getUnwrappedCoyoteRequest();
                break;
            } else if (current instanceof ServletRequestWrapper) {
                current = ((ServletRequestWrapper) current).getRequest();
            } else
                break;
        }
        return coyoteRequest;

    }
    // END OF S1AS 4703023


}
