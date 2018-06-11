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
