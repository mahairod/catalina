/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web.jsp;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.jsp.tagext.JspTag;

import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.StandardContext;
import org.apache.jasper.runtime.ResourceInjector;

import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.deployment.JndiNameEnvironment;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.web.WebModule;
import com.sun.logging.LogDomains; 

/**
 * SJSAS implementation of the org.apache.jasper.runtime.ResourceInjector
 * interface.
 *
 * @author Jan Luehe
 */
public class ResourceInjectorImpl implements ResourceInjector {

    protected static final Logger _logger = LogDomains.getLogger(
            LogDomains.WEB_LOGGER);
    protected static final ResourceBundle _rb = _logger.getResourceBundle(); 

    private InjectionManager injectionMgr;
    private JndiNameEnvironment desc;

    /**
     * Associates this ResourceInjector with the component environment of the
     * given servlet context.
     *
     * @param servletContext The servlet context 
     */
    public void setContext(ServletContext servletContext) {

        if (!(servletContext instanceof ApplicationContextFacade)) {
            return;
        }

        final ApplicationContextFacade contextFacade =
            (ApplicationContextFacade) servletContext;

        StandardContext context = null;

        if (System.getSecurityManager() != null) {
            context = (StandardContext) AccessController.doPrivileged(
                    new PrivilegedAction() {
                public Object run() {
                    return contextFacade.getUnwrappedContext();
                }
            });
        } else {
            context = contextFacade.getUnwrappedContext();
        }

        if (context != null && context instanceof WebModule) {
            WebModule wm = (WebModule)context;
            ServerContext serverContext = wm.getServerContext();
            if (serverContext == null) {
                throw new IllegalStateException(
                        _rb.getString("resource.injector.noservercontext"));
            }
            if (injectionMgr == null) {
                injectionMgr = serverContext.getDefaultHabitat().getByContract(
                         InjectionManager.class);
            }
            desc = wm.getWebBundleDescriptor();
        }
    }

   
    /**
     * Injects the injectable resources from the component environment 
     * associated with this ResourceInjectorImpl into the given 
     * tag handler instance. 
     *
     * @param handler The tag handler instance to be injected
     *
     * @throws Exception if an error occurs during injection
     */
    public void inject(JspTag handler) throws Exception {

        if( desc != null ) {
            injectionMgr.injectInstance(handler, desc);
        }
    }

}
