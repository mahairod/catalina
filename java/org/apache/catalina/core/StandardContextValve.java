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

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.naming.NamingException;

import com.sun.grizzly.util.buf.CharChunk;
import com.sun.grizzly.util.buf.MessageBytes;

import org.apache.naming.ContextBindings;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.valves.ValveBase;
// START GlassFish 1343
import org.glassfish.web.valve.GlassFishValve;
// END GlassFish 1343

/**
 * Valve that implements the default basic behavior for the
 * <code>StandardContext</code> container implementation.
 * <p>
 * <b>USAGE CONSTRAINT</b>:  This implementation is likely to be useful only
 * when processing HTTP requests.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.19 $ $Date: 2007/05/05 05:31:54 $
 */

final class StandardContextValve
    extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardContextValve/1.0";


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    private StandardContext context = null;


    private static Logger log = Logger.getLogger(
        StandardContextValve.class.getName());


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Cast to a StandardContext right away, as it will be needed later.
     * 
     * @see org.apache.catalina.Contained#setContainer(org.apache.catalina.Container)
     */
    public void setContainer(Container container) {
        super.setContainer(container);
        context = (StandardContext) container;
    }


    /**
     * Select the appropriate child Wrapper to process this request,
     * based on the specified request URI.  If no matching Wrapper can
     * be found, return an appropriate HTTP error.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     * @param valveContext Valve context used to forward to the next Valve
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    @Override
    public int invoke(Request request, Response response)
        throws IOException, ServletException {

        Wrapper wrapper = preInvoke(request, response);
        if (wrapper == null) {
            return END_PIPELINE;
        }

        /* GlassFish 1343
        wrapper.getPipeline().invoke(request, response);
        */
        // START GlassFish 1343
        if (wrapper.getPipeline().hasNonBasicValves() ||
                wrapper.hasCustomPipeline()) {
            wrapper.getPipeline().invoke(request, response);
        } else {
            GlassFishValve basic = wrapper.getPipeline().getBasic();
            if (basic != null) {
                basic.invoke(request, response);
                basic.postInvoke(request, response);
            }
        }
        // END GlassFish 1343

        return END_PIPELINE;
    } 


    /**
     * Tomcat style invocation.
     */
    @Override
    public void invoke(org.apache.catalina.connector.Request request,
                       org.apache.catalina.connector.Response response)
            throws IOException, ServletException {

        Wrapper wrapper = preInvoke(request, response);
        if (wrapper == null) {
            return;
        }

        /* GlassFish 1343
        wrapper.getPipeline().invoke(request, response);
        */
        // START GlassFish 1343
        if (wrapper.getPipeline().hasNonBasicValves() ||
                wrapper.hasCustomPipeline()) {
            wrapper.getPipeline().invoke(request, response);
        } else {
            GlassFishValve basic = wrapper.getPipeline().getBasic();
            if (basic != null) {
                basic.invoke(request, response);
                basic.postInvoke(request, response);
            }
        }
        // END GlassFish 1343

        postInvoke(request, response);
    }


    @Override
    public void postInvoke(Request request, Response response)
        throws IOException, ServletException {

        Object[] listeners = 
            ((Context) container).getApplicationEventListeners();

        if ((listeners != null) && (listeners.length > 0)) {
            // create post-service event
            ServletRequestEvent event = new ServletRequestEvent
                (((StandardContext) container).getServletContext(), 
                request.getRequest());
            for (int i = 0; i < listeners.length; i++) {
                int j = (listeners.length - 1) - i;
                if (listeners[j] == null ||
                        !(listeners[j] instanceof ServletRequestListener)) {
                    continue;
                }
                ServletRequestListener listener =
                    (ServletRequestListener) listeners[j];
                // START SJSAS 6329662
                container.fireContainerEvent(
                    ContainerEvent.BEFORE_REQUEST_DESTROYED,
                    listener);
                // END SJSAS 6329662
                try {
                    listener.requestDestroyed(event);
                } catch (Throwable t) {
                    log(sm.getString(
                        "standardContextValve.requestListener.requestDestroyed",
                        listener.getClass().getName()),
                        t);
                    ServletRequest sreq = request.getRequest();
                    sreq.setAttribute(RequestDispatcher.ERROR_EXCEPTION, t);
                // START SJSAS 6329662
                } finally {
                    container.fireContainerEvent(
                        ContainerEvent.AFTER_REQUEST_DESTROYED,
                        listener);
                // END SJSAS 6329662
                }
            }
        }

    }


    /**
     * Report a "not found" error for the specified resource.  FIXME:  We
     * should really be using the error reporting settings for this web
     * application, but currently that code runs at the wrapper level rather
     * than the context level.
     *
     * @param response The response we are creating
     */
    private void notFound(HttpServletResponse response) {

        try {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalStateException e) {
            ;
        } catch (IOException e) {
            ;
        }

    }


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
            logger.log("StandardContextValve[" + container.getName() + "]: "
                       + message);
        else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();
            System.out.println("StandardContextValve[" + containerName
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
            logger.log("StandardContextValve[" + container.getName() + "]: "
                       + message, throwable);
        else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();
            System.out.println("StandardContextValve[" + containerName
                               + "]: " + message);
            System.out.println("" + throwable);
            throwable.printStackTrace(System.out);
        }

    }


    private Wrapper preInvoke(Request request, Response response) {

        // Disallow any direct access to resources under WEB-INF or META-INF
        HttpRequest hreq = (HttpRequest) request;
        // START CR 6415120
        if (request.getCheckRestrictedResources()) {
        // END CR 6415120
        MessageBytes requestPathMB = hreq.getRequestPathMB();
        if ((requestPathMB.startsWithIgnoreCase("/META-INF/", 0))
            || (requestPathMB.equalsIgnoreCase("/META-INF"))
            || (requestPathMB.startsWithIgnoreCase("/WEB-INF/", 0))
            || (requestPathMB.equalsIgnoreCase("/WEB-INF"))) {
            String requestURI = hreq.getDecodedRequestURI();
            notFound((HttpServletResponse) response.getResponse());
            return null;
        }
        // START CR 6415120
        }
        // END CR 6415120

        // Wait if we are reloading
        boolean reloaded = false;
        while (((StandardContext) container).getPaused()) {
            reloaded = true;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                ;
            }
        }

        // Reloading will have stopped the old webappclassloader and
        // created a new one
        if (reloaded &&
                context.getLoader() != null &&
                context.getLoader().getClassLoader() != null) {
            Thread.currentThread().setContextClassLoader(
                    context.getLoader().getClassLoader());
        }

        // Select the Wrapper to be used for this Request
        Wrapper wrapper = request.getWrapper();
        if (wrapper == null) {
            String requestURI = hreq.getDecodedRequestURI();
            notFound((HttpServletResponse) response.getResponse());
            return null;
        } else if (wrapper.isUnavailable()) {
            // May be as a result of a reload, try and find the new wrapper
            wrapper = (Wrapper) container.findChild(wrapper.getName());
            if (wrapper == null) {
                String requestURI = hreq.getDecodedRequestURI();
                notFound((HttpServletResponse) response.getResponse());
                return null;
            }
        }
        
        Object instances[] = 
            ((Context) container).getApplicationEventListeners();

        ServletRequestEvent event = null;

        if ((instances != null) 
                && (instances.length > 0)) {
            event = new ServletRequestEvent
                (((StandardContext) container).getServletContext(), 
                 request.getRequest());
            // create pre-service event
            for (int i = 0; i < instances.length; i++) {
                if (instances[i] == null)
                    continue;
                if (!(instances[i] instanceof ServletRequestListener))
                    continue;
                ServletRequestListener listener =
                    (ServletRequestListener) instances[i];
                // START SJSAS 6329662
                container.fireContainerEvent(
                    ContainerEvent.BEFORE_REQUEST_INITIALIZED,
                    listener);
                // END SJSAS 6329662
                try {
                    listener.requestInitialized(event);
                } catch (Throwable t) {
                    log(sm.getString(
                        "standardContextValve.requestListener.requestInit",
                        instances[i].getClass().getName()),
                        t);
                    ServletRequest sreq = request.getRequest();
                    sreq.setAttribute(RequestDispatcher.ERROR_EXCEPTION, t);
                    return null;
                // START SJSAS 6329662
                } finally {
                    container.fireContainerEvent(
                        ContainerEvent.AFTER_REQUEST_INITIALIZED,
                        listener);
                // END SJSAS 6329662
                }
            }
        }

        return wrapper;
    }
}
