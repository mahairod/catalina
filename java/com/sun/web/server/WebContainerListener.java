/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package com.sun.web.server;

import java.util.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;

import org.apache.catalina.*;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;

import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.web.WebComponentInvocation;
import com.sun.enterprise.web.WebModule;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
import java.util.logging.Logger;
//END OF IASRI 4660742

/**
 * This class implements the Tomcat ContainerListener interface and
 * handles Context and Session related events.
 * @author Tony Ng
 */
public final class WebContainerListener 
    implements ContainerListener {

    // START OF IASRI 4660742
    static Logger _logger=LogDomains.getLogger(LogDomains.WEB_LOGGER);
    // END OF IASRI 4660742

    static private HashSet beforeEvents = new HashSet();
    static private HashSet afterEvents = new HashSet();

    static {
        // preInvoke events
        beforeEvents.add(ContainerEvent.BEFORE_CONTEXT_INITIALIZED);
        beforeEvents.add(ContainerEvent.BEFORE_CONTEXT_DESTROYED);
        beforeEvents.add(ContainerEvent.BEFORE_CONTEXT_ATTRIBUTE_ADDED);
        beforeEvents.add(ContainerEvent.BEFORE_CONTEXT_ATTRIBUTE_REMOVED);
        beforeEvents.add(ContainerEvent.BEFORE_CONTEXT_ATTRIBUTE_REPLACED);
        beforeEvents.add(ContainerEvent.BEFORE_REQUEST_INITIALIZED);
        beforeEvents.add(ContainerEvent.BEFORE_REQUEST_DESTROYED);
        beforeEvents.add(ContainerEvent.BEFORE_SESSION_CREATED);
        beforeEvents.add(ContainerEvent.BEFORE_SESSION_DESTROYED);
        beforeEvents.add(ContainerEvent.BEFORE_SESSION_ATTRIBUTE_ADDED);
        beforeEvents.add(ContainerEvent.BEFORE_SESSION_ATTRIBUTE_REMOVED);
        beforeEvents.add(ContainerEvent.BEFORE_SESSION_ATTRIBUTE_REPLACED);
        beforeEvents.add(ContainerEvent.BEFORE_FILTER_INITIALIZED);
        beforeEvents.add(ContainerEvent.BEFORE_FILTER_DESTROYED);

        // postInvoke events
        afterEvents.add(ContainerEvent.AFTER_CONTEXT_INITIALIZED);
        afterEvents.add(ContainerEvent.AFTER_CONTEXT_DESTROYED);
        afterEvents.add(ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_ADDED);
        afterEvents.add(ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_REMOVED);
        afterEvents.add(ContainerEvent.AFTER_CONTEXT_ATTRIBUTE_REPLACED);
        afterEvents.add(ContainerEvent.AFTER_REQUEST_INITIALIZED);
        afterEvents.add(ContainerEvent.AFTER_REQUEST_DESTROYED);
        afterEvents.add(ContainerEvent.AFTER_SESSION_CREATED);
        afterEvents.add(ContainerEvent.AFTER_SESSION_DESTROYED);
        afterEvents.add(ContainerEvent.AFTER_SESSION_ATTRIBUTE_ADDED);
        afterEvents.add(ContainerEvent.AFTER_SESSION_ATTRIBUTE_REMOVED);
        afterEvents.add(ContainerEvent.AFTER_SESSION_ATTRIBUTE_REPLACED);
        afterEvents.add(ContainerEvent.AFTER_FILTER_INITIALIZED);
        afterEvents.add(ContainerEvent.AFTER_FILTER_DESTROYED);
    }

    private InvocationManager invocationMgr;
    private InjectionManager injectionMgr;

    public WebContainerListener(InvocationManager invocationMgr,
            InjectionManager injectionMgr) {
        this.invocationMgr = invocationMgr;
        this.injectionMgr = injectionMgr;
    }

    public void containerEvent(ContainerEvent event) {
        if(_logger.isLoggable(Level.FINEST)) {
	    _logger.log(Level.FINEST,"ContainerEvent: " +
                        event.getType() + "," +
                        event.getContainer() + "," +
                        event.getData());
        }

        String type = event.getType();

        try {
            if (ContainerEvent.AFTER_LISTENER_INSTANTIATED.equals(type)
                    || ContainerEvent.BEFORE_FILTER_INITIALIZED.equals(type)) {
                preInvoke((Context) event.getContainer());
                injectInstance(event);
                postInvoke((Context) event.getContainer());
            }

            if (beforeEvents.contains(type)) {
                preInvoke((Context) event.getContainer());
            } else if (afterEvents.contains(type)) {
                postInvoke((Context) event.getContainer());
            }
        } catch (Exception ex) {
            String msg = _logger.getResourceBundle().getString(
                "web_server.excep_handle_event");
            msg = MessageFormat.format(msg, new Object[] { type });
            throw new RuntimeException(msg, ex);
        } finally {
            if (type.equals(ContainerEvent.AFTER_FILTER_DESTROYED)) {
                preDestroy(event);
            }
        } 
    }

    private void preInvoke(Context ctx) {
        if (ctx instanceof WebModule) {
            WebModule wm = (WebModule)ctx;
            ComponentInvocation inv = new WebComponentInvocation(wm);
            invocationMgr.preInvoke(inv);
        }
    }

    private void postInvoke(Context ctx) {
        if (ctx instanceof WebModule) {
            WebModule wm = (WebModule)ctx;
            ComponentInvocation inv = new WebComponentInvocation(wm);
            invocationMgr.postInvoke(inv);
        }
    }

    /*
     * Injects all injectable resources into the servlet context listener
     * or filter instance associated with the given ContainerEvent.
     *
     * @param event The ContainerEvent to process
     */
    private void injectInstance(ContainerEvent event)
            throws InjectionException {

        if (event.getContainer() instanceof WebModule) {
            WebModule wm = (WebModule)event.getContainer();
            JndiNameEnvironment desc = wm.getWebBundleDescriptor();
            if (desc != null) {
                injectionMgr.injectInstance(event.getData(), desc);
            }
        }
    }

    /**
     * Invokes preDestroy on the instance embedded in the given ContainerEvent.
     *
     * @param event The ContainerEvent to process
     */
    private void preDestroy(ContainerEvent event) {
        try {
            if (event.getContainer() instanceof WebModule) {
                WebModule wm = (WebModule)event.getContainer();
                JndiNameEnvironment desc = wm.getWebBundleDescriptor();
                if (desc != null) {
                    injectionMgr.invokeInstancePreDestroy(event.getData(), desc);
                }
            }
        } catch (InjectionException ie) {
            _logger.log(Level.SEVERE,
                        "web_server.excep_handle_after_event",
                        ie);
        }
    }
}
