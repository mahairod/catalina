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
import java.security.AccessControlException;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.Principal;
import java.text.MessageFormat;
import javax.transaction.Transaction;
import javax.servlet.ServletRequest; // IASRI 4713234
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.Realm;
import org.apache.catalina.InstanceEvent;
import org.apache.catalina.InstanceListener;
import org.apache.catalina.Context;
import org.apache.jasper.servlet.JspServlet;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.coyote.tomcat5.CoyoteRequestFacade;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;

import com.sun.enterprise.container.common.spi.JavaEETransactionManager;
import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.*;
// IASRI 4688449
import com.sun.enterprise.security.integration.AppServSecurityContext;
import com.sun.enterprise.security.integration.RealmInitializer;
import com.sun.enterprise.security.integration.SecurityConstants;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.web.WebComponentInvocation;
import com.sun.enterprise.web.WebModule;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
//END OF IASRI 4660742
import org.jvnet.hk2.annotations.Service;

/**
 * This class implements the Tomcat InstanceListener interface and
 * handles the INIT,DESTROY and SERVICE, FILTER events.
 * @author Vivek Nagar
 * @author Tony Ng
 */
@Service
public final class J2EEInstanceListener implements InstanceListener {

    // START OF IASRI 4660742
    protected static Logger _logger=LogDomains.getLogger(LogDomains.WEB_LOGGER);
    // END OF IASRI 4660742
    protected static ResourceBundle _rb = _logger.getResourceBundle();

    private static final HashSet beforeEvents = new HashSet(4);
    private static final HashSet afterEvents = new HashSet(4);
    
    
    static {
        beforeEvents.add(InstanceEvent.BEFORE_SERVICE_EVENT);
        beforeEvents.add(InstanceEvent.BEFORE_FILTER_EVENT);
        beforeEvents.add(InstanceEvent.BEFORE_INIT_EVENT);
        beforeEvents.add(InstanceEvent.BEFORE_DESTROY_EVENT);

        afterEvents.add(InstanceEvent.AFTER_SERVICE_EVENT);
        afterEvents.add(InstanceEvent.AFTER_FILTER_EVENT);
        afterEvents.add(InstanceEvent.AFTER_INIT_EVENT);
        afterEvents.add(InstanceEvent.AFTER_DESTROY_EVENT);
    }

    private InvocationManager im;
    private JavaEETransactionManager tm;
    private InjectionManager injectionMgr;
    private boolean initialized = false;
    
    private AppServSecurityContext securityContext;
    
    public J2EEInstanceListener() {
    }

    public void instanceEvent(InstanceEvent event) {
        Context context = (Context) event.getWrapper().getParent();
        if (!(context instanceof WebModule)) {
            return;
        }
        WebModule wm = (WebModule)context;
        init(wm);

        String eventType = event.getType();
	if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,"*** InstanceEvent: " + eventType);
        }
        if (beforeEvents.contains(eventType)) {
            handleBeforeEvent(event, eventType);
        } else if (afterEvents.contains(eventType)) {
            handleAfterEvent(event, eventType);
        }
    }

    private synchronized void init(WebModule wm) {
        if (initialized) {
            return;
        }
        ServerContext serverContext = wm.getServerContext();
        if (serverContext == null) {
            String msg = _rb.getString("webmodule.noservercontext");
            msg = MessageFormat.format(msg, new Object[] { wm.getName() });
            throw new IllegalStateException(msg);
        }
        im = serverContext.getDefaultHabitat().getByContract(
                InvocationManager.class);
        tm = serverContext.getDefaultHabitat().getByContract(
                JavaEETransactionManager.class);
        injectionMgr = serverContext.getDefaultHabitat().getByContract(
                InjectionManager.class);
        initialized = true;
        
        securityContext = serverContext.getDefaultHabitat().getByContract(AppServSecurityContext.class);
        if (securityContext != null) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Obtained securityContext implementation class " + securityContext);
            }
        } else {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Failed to obtain securityContext implementation class ");
            }
        }
    }

    private void handleBeforeEvent(InstanceEvent event, String eventType) {
        Context context = (Context) event.getWrapper().getParent();
        if (!(context instanceof WebModule)) {
            return;
        }
        WebModule wm = (WebModule)context;

        Object instance = null;
        if (eventType.equals(InstanceEvent.BEFORE_FILTER_EVENT)) {

            instance = event.getFilter();
        } else {
            instance = event.getServlet();
        }            

        // set security context
        // BEGIN IASRI 4688449
        //try {
        Realm ra = context.getRealm();
        /** IASRI 4713234
        if (ra != null) {
            HttpServletRequest request = 
                (HttpServletRequest) event.getRequest();
            if (request != null && request.getUserPrincipal() != null) {
                WebPrincipal prin = 
                    (WebPrincipal) request.getUserPrincipal();
                // ra.authenticate(prin);
                
                // It is inefficient to call authenticate just to set
                // sec.ctx.  Instead, WebPrincipal modified to keep the
                // previously created secctx, and set it here directly.

                SecurityContext.setCurrent(prin.getSecurityContext());
            }
        }
        **/
        // START OF IASRI 4713234
        if (ra != null) {

            ServletRequest request = 
		(ServletRequest) event.getRequest();
            if (request != null && request instanceof HttpServletRequest) {

                HttpServletRequest hreq = (HttpServletRequest)request;
		HttpServletRequest base = hreq;
		
		Principal prin = hreq.getUserPrincipal();
		Principal basePrincipal = prin; 
		
		boolean wrapped = false;

		while (prin != null && base != null) {
		    
		    if (base instanceof ServletRequestWrapper) {
			// unwarp any wrappers to find the base object
			ServletRequest sr = 
			    ((ServletRequestWrapper) base).getRequest();

			if (sr instanceof HttpServletRequest) {

			    base = (HttpServletRequest) sr;
			    wrapped = true;
			    continue;
			} 
		    }

		    if (wrapped) {
			basePrincipal = base.getUserPrincipal();
		    } 

		    else if (base instanceof CoyoteRequestFacade) {
			// try to avoid the getUnWrappedCoyoteRequest call
			// when we can identify see we have the texact class.
			if (base.getClass() != CoyoteRequestFacade.class) {
			    basePrincipal = ((CoyoteRequestFacade)base).
				getUnwrappedCoyoteRequest().getUserPrincipal();
			}
		    } else {
			basePrincipal = base.getUserPrincipal();
		    }

		    break;
		}

		if (prin != null && prin == basePrincipal && 
                        prin.getClass().getName().equals(SecurityConstants.WEB_PRINCIPAL_CLASS)) {
                    securityContext.setSecurityContextWithPrincipal(prin);
		} else if (prin != basePrincipal) {
		    
		    // the wrapper has overridden getUserPrincipal
		    // reject the request if the wrapper does not have
		    // the necessary permission.

		    checkObjectForDoAsPermission(hreq);
                    securityContext.setSecurityContextWithPrincipal(prin);
                    
		}

	    }
        }
        // END OF IASRI 4713234
        //} catch (Exception ex) {
            /** IASRI 4660742
            ex.printStackTrace();
	          **/
	          // START OF IASRI 4660742
                  //_logger.log(Level.SEVERE,"web_server.excep_handle_before_event",ex);
	          // END OF IASRI 4660742
        //}
        // END IASRI 4688449

        ComponentInvocation inv = new WebComponentInvocation(wm, instance);
        try {
            im.preInvoke(inv);
            if (eventType.equals(InstanceEvent.BEFORE_SERVICE_EVENT)) {
                // enlist resources with TM for service method
                Transaction tran = null;
                if ((tran = tm.getTransaction()) != null) {
                    inv.setTransaction(tran);
                }
                tm.enlistComponentResources();
            } else if (eventType.equals(InstanceEvent.BEFORE_INIT_EVENT)) {
                
                // Perform any required resource injection on the servlet 
                // instance.  This needs to be done after the invocation of
                // preInvoke, but before any of the servlet's application
                // code is executed.

                JndiNameEnvironment desc = wm.getWebBundleDescriptor();

                // There won't be a corresponding J2EE naming environment
                // descriptor for certain internal servlets so just skip
                // injection for those ones.
                if( desc != null
                        && instance.getClass() != DefaultServlet.class
                        && instance.getClass() != JspServlet.class) {
                    injectionMgr.injectInstance(instance, desc);
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(
                _logger.getResourceBundle().getString(
                    "web_server.excep_handle_before_event"),
                ex);
        }
    }


    private static javax.security.auth.AuthPermission doAsPrivilegedPerm =
 	new javax.security.auth.AuthPermission("doAsPrivileged");

 
    private static void checkObjectForDoAsPermission(final Object o)
            throws AccessControlException{

	if (System.getSecurityManager() != null) {
	    AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    ProtectionDomain pD = o.getClass().getProtectionDomain();
		    Policy p = Policy.getPolicy();
		    if (!p.implies(pD,doAsPrivilegedPerm)) {
			throw new AccessControlException
			    ("permission required to override getUserPrincipal",
			     doAsPrivilegedPerm);
		    }
		    return null;
		}
	    });
	}
    }

    private void handleAfterEvent(InstanceEvent event, String eventType) {

        Context context = (Context) event.getWrapper().getParent();
        if (!(context instanceof WebModule)) {
            return;
        }

        WebModule wm = (WebModule)context;
        
        Object instance = null;
        if (eventType.equals(InstanceEvent.AFTER_FILTER_EVENT)) {
            instance = event.getFilter();
        } else {
            instance = event.getServlet();
        }

        ComponentInvocation inv = new WebComponentInvocation(wm, instance);
        try {
            im.postInvoke(inv);
        } catch (Exception ex) {
            throw new RuntimeException(
                _logger.getResourceBundle().getString(
                    "web_server.excep_handle_after_event"),
                ex);
        } finally {
            if (eventType.equals(InstanceEvent.AFTER_DESTROY_EVENT)) {
                tm.componentDestroyed(instance, inv);                
                JndiNameEnvironment desc = wm.getWebBundleDescriptor();
                if (desc != null
                        && instance.getClass() != DefaultServlet.class
                        && instance.getClass() != JspServlet.class) {
                    try {
                        injectionMgr.invokeInstancePreDestroy(instance, desc);
                    } catch (InjectionException ie) {
                        _logger.log(Level.SEVERE,
                                    "web_server.excep_handle_after_event",
                                    ie);
                    }
                }
            }
            if (eventType.equals(InstanceEvent.AFTER_FILTER_EVENT) ||
                eventType.equals(InstanceEvent.AFTER_SERVICE_EVENT)) {
                // check it's top level invocation
                // BEGIN IASRI# 4646060
                if (im.getCurrentInvocation() == null) {
                // END IASRI# 4646060
                    try {
                        // clear security context
                        Realm ra = context.getRealm();
                        if (ra != null && (ra instanceof RealmInitializer)) {
                            //((RealmInitializer)ra).logout();
                            securityContext.setCurrentSecurityContext(null);
                        }
                    } catch (Exception ex) {
                        /** IASRI 4660742
                        ex.printStackTrace();
                        **/
                        // START OF IASRI 4660742
                            _logger.log(Level.SEVERE,
                                        "web_server.excep_handle_after_event",
                                        ex);
                        // END OF IASRI 4660742
                    }
                    try {
                        if (tm.getTransaction() != null) {
                            tm.rollback();
                        }
                        tm.cleanTxnTimeout();
                    } catch (Exception ex) {}
                }
                tm.componentDestroyed(instance, inv);
            }
        }
    }
}

