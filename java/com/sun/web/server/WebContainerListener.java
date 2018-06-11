/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
