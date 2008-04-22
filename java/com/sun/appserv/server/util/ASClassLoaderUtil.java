/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.appserv.server.util;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
//import com.sun.enterprise.config.ConfigContext;
//import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.deployment.util.FileUtil;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModulesRegistry;
//import com.sun.enterprise.server.ApplicationServer;
//import com.sun.enterprise.server.PELaunch;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.web.WebDeployer;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.component.Habitat;

public class ASClassLoaderUtil {

    private static final Logger _logger = Logger.getAnonymousLogger();

    private static String sharedClasspathForWebModule = null;
    
    //The new ClassLoader Hierarchy would be enabled only when this system 
    //property is set. 
    private static final String USE_NEW_CLASSLOADER_PROPERTY 
                                    = "com.sun.aas.useNewClassLoader";
    
    /**
     * Gets the classpath associated with a web module, suffixing libraries defined 
     * [if any] for the application
     * @param habitat
     * @param moduleId Module id of the web module
     * @param delegate
     * @return A <code>File.pathSeparator</code> separated list of classpaths
     * for the passed in web module, including the module specified "libraries"
     * defined for the web module.
     */
    public static String getWebModuleClassPath(Habitat habitat,
            String moduleId, boolean delegate) {
        
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "ASClassLoaderUtil.getWebModuleClassPath " +
            		"for module Id : " + moduleId);
        }

        synchronized(ASClassLoaderUtil.class) {
            if (sharedClasspathForWebModule == null) {
            	final StringBuilder tmpString = new StringBuilder();
                
                if (Boolean.getBoolean(USE_NEW_CLASSLOADER_PROPERTY)) {
                    final List<String> tmpList = new ArrayList<String>();
    	            tmpList.addAll(getSharedClasspath());
                    //include addon jars as well now that they are not part of shared classpath. 
    	            tmpList.addAll(getAddOnsClasspath());
                
    	            for(final String s:tmpList){
    	                tmpString.append(s);
    	                tmpString.append(File.pathSeparatorChar);
                    }
                } else {
    	            tmpString.append(FileUtil.getAbsolutePath(System.getProperty("java.class.path")));
                    tmpString.append(File.pathSeparatorChar);

    	        }

                WebDeployer webDeployer = habitat.getComponent(WebDeployer.class);
                ModuleDefinition[] moduleDefs = webDeployer.getMetaData().getPublicAPIs();
                if (moduleDefs != null) {
                    for (ModuleDefinition moduleDef : moduleDefs) {
                        URI[] uris = moduleDef.getLocations();
                        for (URI uri : uris) {
                            tmpString.append(uri.getPath());
                            tmpString.append(File.pathSeparator);
                        }
                    }
                }     
    	        //set sharedClasspathForWebModule so that it doesn't need to be recomputed
    	        //for every other invocation
    	        sharedClasspathForWebModule = tmpString.toString();
            }
        }

        StringBuilder classpath = new StringBuilder(sharedClasspathForWebModule);
            
        if (delegate) {
            addLibrariesFromLibs(classpath, habitat);
            addLibrariesForWebModule(classpath, habitat, moduleId);
        } else {
            addLibrariesForWebModule(classpath, habitat, moduleId);
            addLibrariesFromLibs(classpath, habitat);
        }
              
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Final classpath: " + classpath.toString());    
        }
        
        return classpath.toString();
        
    }

    private static void addLibrariesForWebModule(StringBuilder sb,
            Habitat habitat, String moduleId) {
       if (moduleId != null) {
            final String specifiedLibraries = getLibrariesForWebModule(moduleId);
            final URL[] libs = getLibraries(specifiedLibraries);
            if (libs != null)  {
                for (final URL u : libs) {
                    sb.append(u);
                    sb.append(File.pathSeparator);
                }
            }
        }
    }

    /**
     * Add Libraries from lib and domain_root lib.
     */
    private static void addLibrariesFromLibs(StringBuilder sb, Habitat habitat) {
        ModulesRegistry mreg = habitat.getComponent(ModulesRegistry.class);
        if (mreg != null) {
            ClassLoader cl = mreg.getParentClassLoader();
            if (cl instanceof URLClassLoader) {
                URLClassLoader urlCl = (URLClassLoader)cl;
                URL[] urls = urlCl.getURLs();
                if (urls != null) {
                    for (URL u : urls) {
                        sb.append(u);
                        sb.append(File.pathSeparator);
                    }
                }
            }
        }
    }
 
    /**
     * Gets the deploy-time "libraries" attribute specified for module
     * @param the module type
     * @param moduleId The module id of the web module
     * @return A comma separated list representing the libraries
     * specified by the deployer.
     */    
    public static <T> String getLibrariesForModule(Class<T> type, String moduleId) {
        
        T app = ConfigBeansUtilities.getModule(type, moduleId);
        if (app==null) return null;
        
        String librariesStr=null;
        try {
            Method m = type.getMethod("getLibraries");
            if (m!=null) {
                librariesStr = (String) m.invoke(app);
            }
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "app = " +  app + " library = " + librariesStr);
            }
            
        } catch(Exception e) {
            _logger.log(Level.SEVERE, "Cannot get libraries for module " + moduleId, e);
        }
        return librariesStr;
        
    }
    
    /**
     * Gets the deploy-time "libraries" attribute specified for a web module (.war file)
     * @param moduleId The module id of the web module
     * @return A comma separated list representing the libraries
     * specified by the deployer.
     */
    public static String getLibrariesForWebModule(String moduleId) {
            
        String librariesStr = ConfigBeansUtilities.getLibraries(moduleId);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "moduleId = " +  moduleId + " library = " + librariesStr);
        }
        
        return librariesStr;
        
    }
    
    /**
     * Utility method to obtain a resolved list of URLs representing the 
     * libraries specified for an application using the libraries 
     * application deploy-time attribute 
     * @param librariesStr The deploy-time libraries attribute as specified by 
     * the deployer for an application
     * @return A list of URLs representing the libraries specified for 
     * the application
     */
    public static URL[] getLibraries(String librariesStr) {
        if(librariesStr == null)
            return null;
        
        String [] librariesStrArray = librariesStr.split(",");
        if(librariesStrArray == null)
            return null;
        
        final URL [] urls = new URL[librariesStrArray.length];
        //Using the string from lib and applibs requires admin which is 
        //built after appserv-core.
        final String appLibsDir = System.getProperty(
                        SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) 
                        + File.separator + "lib" 
                        + File.separator  + "applibs";
        
        int i=0;
        for(final String libraryStr:librariesStrArray){
            try {
                File f = new File(libraryStr);
                if(!f.isAbsolute())
                    f = new File(appLibsDir, libraryStr);
                URL url = f.toURL();
                urls[i++] = url;
            } catch (MalformedURLException malEx) {
                _logger.log(Level.WARNING,
                        "loader.cannot_convert_classpath_into_url",
                        libraryStr);
                _logger.log(Level.WARNING,"loader.exception", malEx);
            }
        }
        return urls;
    }
    
    /**
     * Returns the shared class loader
     * @return ClassLoader
     */
    public static synchronized ClassLoader getSharedClassLoader() {
        //XXX return ApplicationServer.getServerContext().getSharedClassLoader();
        return null;
    }
    
    public static ClassLoader getSharedChain(){
        // not yet implemented
        return null;
    }    
    
    public static synchronized List<String> getSharedClasspath() {
        // not yet implemented
        return null;
    }
    
    public static synchronized List<String> getAddOnsClasspath() {
        // not yet implemented
    	return null;
    }
    
}
