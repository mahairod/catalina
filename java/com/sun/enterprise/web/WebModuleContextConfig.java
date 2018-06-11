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

package com.sun.enterprise.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;  

import org.apache.catalina.Authenticator;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.core.StandardEngine;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.deployment.ResourcePrincipal;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.runtime.common.DefaultResourcePrincipal;
import com.sun.enterprise.deployment.runtime.common.ResourceRef;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.web.ContextParameter;
import com.sun.logging.LogDomains;

/**
 * Startup event listener for a <b>Context</b> that configures the properties
 * of that Context, and the associated defined servlets.
 *
 * @author Jean-Francois Arcand
 */

public class WebModuleContextConfig extends ContextConfig {

    private static final Logger logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    
    protected static final ResourceBundle _rb = logger.getResourceBundle();

    public final static int CHILDREN = 0;
    public final static int SERVLET_MAPPINGS = 1;
    public final static int LOCAL_EJBS = 2;
    public final static int EJBS = 3;
    public final static int ENVIRONMENTS = 4;
    public final static int ERROR_PAGES = 5;
    public final static int FILTER_DEFS = 6;
    public final static int FILTER_MAPS = 7;
    public final static int APPLICATION_LISTENERS = 8;
    public final static int RESOURCES = 9;
    public final static int APPLICATION_PARAMETERS = 10;
    public final static int MESSAGE_DESTINATIONS = 11;
    public final static int MESSAGE_DESTINATION_REFS = 12;
    public final static int MIME_MAPPINGS = 13;
   
    
    /**
     * The <code>File</code> reffering to the default-web.xml 
     */
    protected File file; 
        
    
    /**
     * The DOL object representing the web.xml content.
    */
    private WebBundleDescriptor webBundleDescriptor;   

    
    /**
     * Customized <code>ContextConfig</code> which use the DOL for deployment.
     */
    public WebModuleContextConfig(){
    }
    
    
    /**
     * Set the DOL object associated with this class.
     */
    public void setDescriptor(WebBundleDescriptor wbd){
        webBundleDescriptor = wbd;
    }
    
    
    /**
     * Process the START event for an associated Context.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        // Identify the context we are associated with
        try {
            context = (Context) event.getLifecycle();
        } catch (ClassCastException e) {
            return;
        }

        // Called from ContainerBase.addChild() -> StandardContext.start()
        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT)) 
            start();
        else if (event.getType().equals(Lifecycle.STOP_EVENT))
            stop();
        else if (event.getType().equals(Lifecycle.INIT_EVENT)) {
            super.init();
            configureResource();
        }

    }
    
    
    protected synchronized void configureResource() {
        
        try {
            ApplicationParameter[] appParams = 
                    context.findApplicationParameters();
            ContextParameter contextParam;
            for (int i=0; i<appParams.length; i++) {
                contextParam = new EnvironmentProperty(appParams[i].getName(),
                    appParams[i].getValue(), appParams[i].getDescription());
                webBundleDescriptor.addContextParameter(contextParam);
            }

            ContextEnvironment[] envs = context.findEnvironments();
            EnvironmentProperty envEntry;
            for (int i=0; i<envs.length; i++) {
                envEntry = new EnvironmentProperty(
                        envs[i].getName(), envs[i].getValue(),
                        envs[i].getDescription(), envs[i].getType()); 
                if (envs[i].getValue()!=null) {
                    envEntry.setValue(envs[i].getValue());
                }
                webBundleDescriptor.addEnvironmentProperty(envEntry);
            }

            ContextResource[] resources = context.findResources();
            ResourceReferenceDescriptor resourceReference;
            SunWebApp iasBean = webBundleDescriptor.getSunDescriptor();
            ResourceRef[] rr = iasBean.getResourceRef();
            DefaultResourcePrincipal drp;
            ResourcePrincipal rp;
            
            for (int i=0; i<resources.length; i++) {
                resourceReference = new ResourceReferenceDescriptor(
                        resources[i].getName(), resources[i].getDescription(),
                        resources[i].getType());
                resourceReference.setJndiName(resources[i].getName());
                if (rr!=null) {
                    for (int j=0; j<rr.length; j++) {
                        if (resources[i].getName().equals(rr[j].getResRefName())) {
                            resourceReference.setJndiName(rr[i].getJndiName());
                            drp = rr[i].getDefaultResourcePrincipal();
                            if (drp!=null) {
                                rp = new ResourcePrincipal(drp.getName(), drp.getPassword());
                                resourceReference.setResourcePrincipal(rp);
                            }
                        }
                    }
                }
                resourceReference.setAuthorization(resources[i].getAuth());
                webBundleDescriptor.addResourceReferenceDescriptor(resourceReference);
            }
        
            /* XXX
            Switch sw = Switch.getSwitch();
            sw.getNamingManager().bindObjects(webBundleDescriptor);
            sw.setDescriptorFor(context, webBundleDescriptor);
            */
            
        } catch (Exception exception) { 
            context.setAvailable(false);
            String msg = _rb.getString("webcontainer.webModuleDisabled");
            msg = MessageFormat.format(msg,
                                       new Object[] { context.getName() });
            logger.log(Level.SEVERE, msg, exception);
        }
    
    }


    /**
     * Process a "start" event for this Context - in background
     */
    protected synchronized void start() {
        
        try{
            TomcatDeploymentConfig.configureWebModule((WebModule)context,
                                                      webBundleDescriptor);
        } catch (Throwable t){
            context.setAvailable(false);
            String msg = _rb.getString(
                "webModuleContextConfig.webModuleDisabled");
            msg = MessageFormat.format(msg,
                                       new Object[] { context.getName() });
            logger.log(Level.SEVERE, msg, t);
        }

        context.setConfigured(false);
        ok = true;

        authenticatorConfig();
        // XXX realm not ready yet
        //if (ok) {
            managerConfig();
        //}
        
        //if (ok) {
            context.setConfigured(true);
        //} else {
        /*    context.setConfigured(false);
            logger.log(Level.SEVERE,
                       "webModuleContextConfig.webModuleDisabledNoException",
                       new Object[] { context.getName() });
        }*/
    }
    
    
    /**
     * Always sets up an Authenticator regardless of any security constraints.
     */
    protected synchronized void authenticatorConfig() {
        
        LoginConfig loginConfig = context.getLoginConfig();
        if (loginConfig == null) {
            loginConfig = new LoginConfig("NONE", null, null, null);
            context.setLoginConfig(loginConfig);
        }

        // Has an authenticator been configured already?
        if (context instanceof Authenticator)
            return;
        if (context instanceof ContainerBase) {
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            if (pipeline != null) {
                Valve basic = pipeline.getBasic();
                if ((basic != null) && (basic instanceof Authenticator))
                    return;
                Valve valves[] = pipeline.getValves();
                for (int i = 0; i < valves.length; i++) {
                    if (valves[i] instanceof Authenticator)
                        return;
                }
            }
        } else {
            return;     // Cannot install a Valve even if it would be needed
        }

        // Has a Realm been configured for us to authenticate against?
        /* START IASRI 4856062
        if (context.getRealm() == null) {
        */
        // BEGIN IASRI 4856062
        Realm rlm = context.getRealm();
        if (rlm == null) {
        // END IASRI 4856062
            logger.log(Level.SEVERE, "webModuleContextConfig.missingRealm");
            ok = false;
            return;
        }

        // BEGIN IASRI 4856062
        // If a realm is available set its name in the Realm(Adapter)
        rlm.setRealmName(loginConfig.getRealmName(),
                         loginConfig.getAuthMethod());

        // END IASRI 4856062

        /*
         * First check to see if there is a custom mapping for the login
         * method. If so, use it. Otherwise, check if there is a mapping in
         * org/apache/catalina/startup/Authenticators.properties.
         */
        Valve authenticator = null;
        if (customAuthenticators != null) {
            authenticator = (Valve)
                customAuthenticators.get(loginConfig.getAuthMethod());
        }
        if (authenticator == null) {
            // Load our mapping properties if necessary
            if (authenticators == null) {
                try {
                    InputStream is=this.getClass().getClassLoader().getResourceAsStream("org/apache/catalina/startup/Authenticators.properties");
                    if( is!=null ) {
                        authenticators = new Properties();
                        authenticators.load(is);
                    } else {
                        logger.log(Level.SEVERE, "webModuleContextConfig.authenticatorResources");
                        ok=false;
                        return;
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "webModuleContextConfig.authenticatorResources", e);
                    ok = false;
                    return;
                }
            }

            // Identify the class name of the Valve we should configure
            String authenticatorName = null;

            // BEGIN RIMOD 4808402
            // If login-config is given but auth-method is null, use NONE
            // so that NonLoginAuthenticator is picked
            String authMethod = loginConfig.getAuthMethod();
            if (authMethod == null) {
                authMethod = "NONE";
            }
            authenticatorName = authenticators.getProperty(authMethod);
            // END RIMOD 4808402
            /* RIMOD 4808402
            authenticatorName =
                    authenticators.getProperty(loginConfig.getAuthMethod());
            */

            if (authenticatorName == null) {
                logger.log(Level.SEVERE, "webModuleContextConfig.authenticatorMissing",
                           loginConfig.getAuthMethod());
                ok = false;
                return;
            }

            // Instantiate and install an Authenticator of the requested class
            try {
                Class authenticatorClass = Class.forName(authenticatorName);
                authenticator = (Valve) authenticatorClass.newInstance();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "webModuleContextConfig.authenticatorInstantiate", authenticatorName);
                logger.log(Level.SEVERE, "webModuleContextConfig.authenticatorInstantiate", t);
                ok = false;
            }
        }

        if (authenticator != null && context instanceof ContainerBase) {
            Pipeline pipeline = ((ContainerBase) context).getPipeline();
            if (pipeline != null) {
                ((ContainerBase) context).addValve(authenticator);
                if (logger.isLoggable(Level.FINEST)) {
                    logger.log(Level.FINEST, "webModuleContextConfig.authenticatorConfigured",
                               loginConfig.getAuthMethod());
                }
            }
        }
    }
    
    
    /**
     * Process the default configuration file, if it exists.
     * The default config must be read with the container loader - so
     * container servlets can be loaded
     */
    protected void defaultConfig() {
        ;
    }
}
