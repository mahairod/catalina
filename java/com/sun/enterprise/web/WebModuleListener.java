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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Hashtable;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.ClassFileTransformer;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletContext;

import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.loader.WebappClassLoader;
import org.glassfish.api.web.TldProvider;

import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.runtime.web.WebProperty;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.util.WebValidatorWithCL;
import com.sun.enterprise.deployment.util.WebBundleVisitor;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.web.jsp.ResourceInjectorImpl;
import com.sun.logging.LogDomains;
import com.sun.appserv.web.cache.CacheManager;
import com.sun.appserv.server.util.ASClassLoaderUtil;
import com.sun.appserv.BytecodePreprocessor;
//import com.sun.enterprise.server.PersistenceUnitLoaderImpl;
//import com.sun.enterprise.server.PersistenceUnitLoader;
//import com.sun.enterprise.config.ConfigException;

/**
 * Startup event listener for a <b>Context</b> that configures the properties
 * of that Jsp Servlet from sun-web.xml
 */

final class WebModuleListener
    implements LifecycleListener {

    /**
     * The logger used to log messages
     */
    private static Logger _logger;

    /**
     * This indicates whether debug logging is on or not
     */
    private static boolean _debugLog;

    /**
     * ServletContext attribute constant used for JSF injection integration.
     */
    private static final String JSF_HABITAT_ATTRIBUTE =
            "com.sun.appserv.jsf.habitat";

    /**
     * Descriptor object associated with this web application.
     * Used for loading persistence units.
     */
    private WebBundleDescriptor wbd;

    /**
     * The exploded location for this web module.
     * Note this is not the generated location.
     */
    private String explodedLocation;
    
    private ServerContext serverContext;

    /**
     * Constructor.
     *
     * @param serverContext
     * @param explodedLocation The location where this web module is exploded
     * @param wbd descriptor for this module.
     */
    public WebModuleListener(ServerContext serverContext,
                             String explodedLocation,
                             WebBundleDescriptor wbd) {
        this.serverContext = serverContext;
        this.wbd = wbd;
        this.explodedLocation = explodedLocation;
    }


    /**
     * Process the START event for an associated WebModule
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
            _debugLog = _logger.isLoggable(Level.FINE);
        }

        WebModule webModule;

        // Identify the context we are associated with
        try {
            webModule = (WebModule) event.getLifecycle();
        } catch (ClassCastException e) {
            _logger.log(Level.WARNING, "webmodule.listener.classcastException",
                                        event.getLifecycle());
            return;
        }

        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT)) {
            // post processing DOL object for standalone web module
            if (wbd != null && wbd.getApplication() != null && 
                wbd.getApplication().isVirtual()) {
                wbd.setClassLoader(webModule.getLoader().getClassLoader());
                wbd.visit((WebBundleVisitor) new WebValidatorWithCL());
            }
            
            //loadPersistenceUnits(webModule);
            configureDefaultServlet(webModule);
            configureJspParameters(webModule);
            startCacheManager(webModule);
        } else if (event.getType().equals(Lifecycle.STOP_EVENT)) {
            //unloadPersistenceUnits(webModule);
            stopCacheManager(webModule);
        }
    }

    /*private void loadPersistenceUnits(final WebModule webModule) {
        _logger.logp(Level.FINE, "WebModuleListener", "loadPersistenceUnits",
                "wbd = {0} for {1}", new Object[]{wbd, webModule.getName()});
        if(wbd == null) {
            // for some system app like adminGUI, wbd is null
            return;
        }
        final Application application  = wbd.getApplication();
        // load PUs only for standaalone wars.
        // embedded wars are taken care of in ApplicationLoader.
        if(application != null && application.isVirtual()) {
            try{
                new PersistenceUnitLoaderImpl().load(new ApplicationInfoImpl(
                        explodedLocation, wbd, webModule));
            } catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    private boolean unloadPersistenceUnits(final WebModule webModule) {
        _logger.logp(Level.FINE, "WebModuleListener", "unloadPersistenceUnits",
                "wbd = {0} for {1}", new Object[]{wbd, webModule.getName()});
        if(wbd == null) {
            // for some system app like adminGUI, wbd is null
            return true;
        }
        final Application application  = wbd.getApplication();
        // unload PUs only for standaalone wars.
        // embedded wars are taken care of in ApplicationLoader.
        if(application != null && application.isVirtual()) {
            try{
                new PersistenceUnitLoaderImpl().unload(new ApplicationInfoImpl(
                        explodedLocation, wbd, webModule));
            } catch(Exception e){
                _logger.log(Level.WARNING, e.getMessage(), e);
                return false;
            }
        }
        return true;
    }*/

    /**
     * implementation of
     * {@link com.sun.enterprise.server.PersistenceUnitLoader.ApplicationInfo}.
     *
    private static class ApplicationInfoImpl
            implements PersistenceUnitLoader.ApplicationInfo {
        private WebBundleDescriptor wbd;
        private String location;
        private InstrumentableClassLoader classLoader;
        public ApplicationInfoImpl(String location, WebBundleDescriptor wbd, WebModule wm) {
            this.wbd = wbd;
            this.location = location;
            this.classLoader = new InstrumentableWebappClassLoader(
                    WebappClassLoader.class.cast(wm.getLoader().getClassLoader()));
        }

        public Application getApplication() {
            return wbd.getApplication();
        }

        public InstrumentableClassLoader getClassLoader() {
            return classLoader;
        }

        public String getApplicationLocation() {
            return location;
        }

        /**
         * @return the precise collection of PUs that are referenced by this war
         *
        public Collection<? extends PersistenceUnitDescriptor>
                getReferencedPUs() {
            return wbd.findReferencedPUs();
        }

        /**
         * @return the list of EMFs that have been loaded for this war.
         *
        public Collection<? extends EntityManagerFactory> getEntityManagerFactories() {
            // since we are only responsible for standalone web module,
            // there is no need to search for EMFs in Application object.
            assert(wbd.getApplication().isVirtual());
            return wbd.getEntityManagerFactories();
        }

    } // class ApplicationInfoImpl*/

    //------------------------------------------------------- Private Methods

    /**
     * Configure the jsp config settings for the jspServlet  using the values
     * in sun-web.xml's jsp-config
     */
    private void configureJspParameters(WebModule webModule) {
        // Find tld URL and set it to ServletContext attribute
        Collection<TldProvider> tldProviders =
                serverContext.getDefaultHabitat().getAllByContract(
                TldProvider.class);
        List<URL> tldURLs = new ArrayList<URL>();
        for (TldProvider tldProvider : tldProviders) {
            URL[] urls = tldProvider.getTldURLs();
            if (urls != null && urls.length > 0) {
                for (URL url : urls) {
                    tldURLs.add(url);
                }
            }
        }
        webModule.getServletContext().setAttribute(
                "com.sun.appserv.tld.urls", tldURLs);

        // set habitat for jsf injection
        webModule.getServletContext().setAttribute(
                JSF_HABITAT_ATTRIBUTE,
                serverContext.getDefaultHabitat());

        SunWebApp bean  = webModule.getIasWebAppConfigBean();

        // Find the default jsp servlet
        String name = webModule.findServletMapping(Constants.JSP_URL_PATTERN);
        Wrapper wrapper = (Wrapper)webModule.findChild(name);
        if (wrapper == null)
            return;

        String servletClass = wrapper.getServletClass();
        // If the jsp maps to the default JspServlet, then add 
        // the init parameters
        if (servletClass != null
                && servletClass.equals(Constants.APACHE_JSP_SERVLET_CLASS)) {

            if (webModule.getTldValidation()) {
                wrapper.addInitParameter("enableTldValidation", "true");
            }
            if (bean != null && bean.getJspConfig()  != null) {
                WebProperty[]  props = bean.getJspConfig().getWebProperty();
                for (int i = 0; i < props.length; i++) {
                    String pname = props[i].getAttributeValue("name");
                    String pvalue = props[i].getAttributeValue("value");
                    if (_debugLog) {
                        _logger.fine("jsp-config property for ["
                                     + webModule.getID() + "] is [" + pname
                                     + "] = [" + pvalue + "]");
                    }
                    wrapper.addInitParameter(pname, pvalue);
                }
            }
           
            // Override any log setting with the container wide logging level
            wrapper.addInitParameter("logVerbosityLevel",getJasperLogLevel());

            wrapper.addInitParameter("com.sun.appserv.jsp.resource.injector",
                                     ResourceInjectorImpl.class.getName());

            // START SJSAS 6311155
            String sysClassPath = ASClassLoaderUtil.getWebModuleClassPath(
                    serverContext.getDefaultHabitat(), webModule.getID(),
                    webModule.getLoader().getDelegate());
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine(" sysClasspath for " + webModule.getID() + " is \n" 
                                                               + sysClassPath + "\n");
            }
            wrapper.addInitParameter("com.sun.appserv.jsp.classpath",
                                     sysClassPath);
            // END SJSAS 6311155
        }
    }

    /**
     * Determine the debug setting for JspServlet based on the iAS log
     * level.
     */
    private String getJasperLogLevel() {
        Level level = _logger.getLevel();
        if (level == null )
            return "warning";
        if (level.equals(Level.WARNING))
            return "warning";
        else if (level.equals(Level.FINE))
            return "information";
        else if (level.equals(Level.FINER) || level.equals(Level.FINEST))
            return "debug";
        else 
            return "warning";
    }

    private void startCacheManager(WebModule webModule) {

        SunWebApp bean  = webModule.getIasWebAppConfigBean();

        // Configure the cache, cache-mapping and other settings
        if (bean != null) {
            CacheManager cm = null;
            try {
                cm = CacheModule.configureResponseCache(webModule, bean);
            } catch (Exception ee) {
                _logger.log(Level.WARNING,
                           "webmodule.listener.cachemgrException", ee);
            }
        
            if (cm != null) {
                try {
                    // first start the CacheManager, if enabled
                    cm.start();
                    if (_debugLog) {
                        _logger.fine("Cache Manager started");
                    }
                    // set this manager as a context attribute so that 
                    // caching filters/tags can find it
                    ServletContext ctxt = webModule.getServletContext();
                    ctxt.setAttribute(CacheManager.CACHE_MANAGER_ATTR_NAME, cm);

                } catch (LifecycleException ee) {
                    _logger.log(Level.WARNING, ee.getMessage(),
                                               ee.getThrowable());
                }
            }
        }
    }

    private void stopCacheManager(WebModule webModule) {
        ServletContext ctxt = webModule.getServletContext();
        CacheManager cm = (CacheManager)ctxt.getAttribute(
                                        CacheManager.CACHE_MANAGER_ATTR_NAME);
        if (cm != null) {
            try {
                cm.stop();
                if (_debugLog) {
                    _logger.fine("Cache Manager stopped");
                }
                ctxt.removeAttribute(CacheManager.CACHE_MANAGER_ATTR_NAME);
            } catch (LifecycleException ee) {
                _logger.log(Level.WARNING, ee.getMessage(), ee.getThrowable());
            }
        }
    }


    /**
     * Configures the given web module's DefaultServlet with the 
     * applicable web properties from sun-web.xml.
     */
    private void configureDefaultServlet(WebModule webModule) {

        // Find the DefaultServlet
        Wrapper wrapper = (Wrapper)webModule.findChild("default");
        if (wrapper == null) {
            return;
        }

        String servletClass = wrapper.getServletClass();
        if (servletClass == null
                || !servletClass.equals(Globals.DEFAULT_SERVLET_CLASS_NAME)) {
            return;
        }

        String fileEncoding = webModule.getFileEncoding();
        if (fileEncoding != null) {
            wrapper.addInitParameter("fileEncoding", fileEncoding);
        }
    }
}
