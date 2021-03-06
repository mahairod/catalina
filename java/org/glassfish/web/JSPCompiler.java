/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim -
 *             :set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *             1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *             2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *             3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 */

/* 
 * @author byron.nevins@sun.com
 */

package org.glassfish.web;
  
import java.io.*;
import java.util.*;
import java.util.logging.*;
import org.apache.jasper.JspC;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.web.InitializationParameter;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.runtime.web.WebProperty;
import com.sun.enterprise.deployment.runtime.web.JspConfig;
import org.glassfish.web.loader.util.ASClassLoaderUtil;
import org.glassfish.deployment.common.DeploymentException;
import com.sun.enterprise.deployment.backend.DeploymentLogger;
import com.sun.enterprise.server.ServerContext;

public final class JSPCompiler {
    private ServerContext serverContext;

	public static void compile(File inWebDir, File outWebDir,
                               WebBundleDescriptor wbd, ServerContext serverContext)
            throws DeploymentException {
            //to resolve ambiguity
        final String amb = null;
		compile(inWebDir, outWebDir, wbd, amb, serverContext);
	}

    public static void compile(File inWebDir, File outWebDir,
                               WebBundleDescriptor wbd, List classpathList,
                               ServerContext serverContext)
        throws DeploymentException {
        String classpath = null;
        if (classpathList != null) {
			classpath = getClasspath(classpathList);
		}
        compile(inWebDir, outWebDir, wbd, classpath, serverContext);
    }
    

	////////////////////////////////////////////////////////////////////////////
	
	public static void compile(File inWebDir, File outWebDir,
                               WebBundleDescriptor wbd, String classpath,
                               ServerContext serverContext)
            throws DeploymentException {
		JspC jspc = new JspC();

        if (classpath != null && classpath.length() >0) {
		    jspc.setClassPath(classpath);
        }
        
        // START SJSAS 6311155
        String appName = wbd.getApplication().getName();
        boolean delegate = true;
        com.sun.enterprise.deployment.runtime.web.ClassLoader clBean =
                wbd.getSunDescriptor().getClassLoader();
        if (clBean != null) {
            String value = clBean.getAttributeValue(
                    com.sun.enterprise.deployment.runtime.web.ClassLoader.DELEGATE);
            delegate = ConfigBeansUtilities.toBoolean(value);
        }

        String sysClassPath = ASClassLoaderUtil.getWebModuleClassPath(
            serverContext.getDefaultHabitat(), appName, delegate);
        jspc.setSystemClassPath(sysClassPath);
        // END SJSAS 6311155

		verify(inWebDir, outWebDir);

		configureJspc(jspc, wbd);
		jspc.setOutputDir(outWebDir.getAbsolutePath());
		jspc.setUriroot(inWebDir.getAbsolutePath());
		jspc.setCompile(true);
		logger.info(startMessage);

		try {
			jspc.execute();
		} 
		catch (Exception je) {
			throw new DeploymentException("JSP Compilation Error: " + je, je);
		}
		finally {
			// bnevins 9-9-03 -- There may be no jsp files in this web-module
			// in such a case the code above will create a useless, and possibly
			// problematic empty directory.	 If the directory is empty -- delete
			// the directory.
			
			String[] files = outWebDir.list();
			
			if(files == null || files.length <= 0)
				outWebDir.delete();
			
			logger.info(finishMessage);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	
	private static void verify(File inWebDir, File outWebDir) throws DeploymentException {
		// inWebDir must exist, outWebDir must either exist or be creatable
		if (!FileUtils.safeIsDirectory(inWebDir)) {
			throw new DeploymentException("inWebDir is not a directory: " + inWebDir);
		}
	 
		if (!FileUtils.safeIsDirectory(outWebDir)) {
			outWebDir.mkdirs();
		
			if (!FileUtils.safeIsDirectory(outWebDir)) {
				throw new DeploymentException("outWebDir is not a directory, and it can't be created: " + outWebDir);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	
	private static String getClasspath(List paths) {
		if(paths == null)
			return null;
		
		String classpath = null;

		StringBuffer sb = new StringBuffer();
		boolean first = true;

		for (Iterator it = paths.iterator(); it.hasNext(); ) {
			String path = (String)it.next();

			if (first) 
				first = false;
			else 
				sb.append(File.pathSeparatorChar);

			sb.append(path);
		}

		if (sb.length() > 0) 
			classpath = sb.toString();

		return classpath;	
	}	

	////////////////////////////////////////////////////////////////////////////

    /*
     * Configures the given JspC instance with the jsp-config properties
	 * specified in the sun-web.xml of the web module represented by the
	 * given WebBundleDescriptor.
	 *
	 * @param jspc JspC instance to be configured
	 * @param wbd WebBundleDescriptor of the web module whose sun-web.xml
     * is used to configure the given JspC instance
	 */
        private static void configureJspc(JspC jspc, WebBundleDescriptor wbd) {

	        SunWebApp sunWebApp = wbd.getSunDescriptor();
	        if (sunWebApp == null) {
         	    return;
            }

            // START SJSAS 6384538
            if (sunWebApp.sizeWebProperty() > 0) {
                WebProperty[] props = sunWebApp.getWebProperty();
                for (int i = 0; i < props.length; i++) {
                    String pName = props[i].getAttributeValue("name");
                    String pValue = props[i].getAttributeValue("value");
                    if (pName == null || pValue == null) {
                        throw new IllegalArgumentException(
                            "Missing sun-web-app property name or value");
                    }
                    if ("enableTldValidation".equals(pName)) {
                        jspc.setIsValidationEnabled(
                            Boolean.valueOf(pValue).booleanValue());
                    }
                }
            }
            // END SJSAS 6384538

            // START SJSAS 6170435
            /*
             * Configure JspC with the init params of the JspServlet
             */
            Set set = wbd.getWebComponentDescriptors();         
            if (!set.isEmpty()) {
                Iterator<WebComponentDescriptor> iterator = set.iterator();
                while (iterator.hasNext()) {
                    WebComponentDescriptor webComponentDesc = iterator.next();
                    if ("jsp".equals(webComponentDesc.getCanonicalName())) {
                        Enumeration<InitializationParameter> en
                            = webComponentDesc.getInitializationParameters();
                        if (en != null) {
                            while (en.hasMoreElements()) {
                                InitializationParameter initP = en.nextElement();
                                configureJspc(jspc,
                                              initP.getName(),
                                              initP.getValue());
                            }
                        }
                        break;
                    }
                }
            }
            // END SJSAS 6170435

            /*
             * Configure JspC with jsp-config properties from sun-web.xml,
             * which override JspServlet init params of the same name.
             */
            JspConfig jspConfig = sunWebApp.getJspConfig();
            if (jspConfig == null) {
                return;
            }
            WebProperty[] props = jspConfig.getWebProperty();
            for (int i=0; props!=null && i<props.length; i++) {
                configureJspc(jspc,
                              props[i].getAttributeValue("name"),
                              props[i].getAttributeValue("value"));
            }
        }


        /*
         * Configures the given JspC instance with the given property name
         * and value.
         *
         * @jspc The JspC instance to configure
         * @pName The property name
         * @pValue The property value
         */
        private static void configureJspc(JspC jspc, String pName,
                                          String pValue) {

            if (pName == null || pValue == null) {
                throw new IllegalArgumentException(
                    "Null property name or value");
            }

            if ("xpoweredBy".equals(pName)) {
                jspc.setXpoweredBy(Boolean.valueOf(pValue).booleanValue());
            } else if ("classdebuginfo".equals(pName)) {
                jspc.setClassDebugInfo(Boolean.valueOf(pValue).booleanValue());
            } else if ("enablePooling".equals(pName)) {
                jspc.setPoolingEnabled(Boolean.valueOf(pValue).booleanValue());
            } else if ("ieClassId".equals(pName)) {
                jspc.setIeClassId(pValue);
            } else if ("trimSpaces".equals(pName)) {
                jspc.setTrimSpaces(Boolean.valueOf(pValue).booleanValue());
            } else if ("genStrAsCharArray".equals(pName)) {
                jspc.setGenStringAsCharArray(
                    Boolean.valueOf(pValue).booleanValue());
            } else if ("errorOnUseBeanInvalidClassAttribute".equals(pName)) {
                jspc.setErrorOnUseBeanInvalidClassAttribute(
                    Boolean.valueOf(pValue).booleanValue());
            } else if ("ignoreJspFragmentErrors".equals(pName)) {
                jspc.setIgnoreJspFragmentErrors(
                    Boolean.valueOf(pValue).booleanValue());
            }
        }


	////////////////////////////////////////////////////////////////////////////
    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(JSPCompiler.class);    	
	private static final String startMessage =
        localStrings.getLocalString("org.glassfish.web.start_jspc", "Beginning JSP Precompile...");
	private static final String finishMessage =
        localStrings.getLocalString("org.glassfish.web.finish_jspc", "Finished JSP Precompile");
	private static final Logger logger = DeploymentLogger.get();


	////////////////////////////////////////////////////////////////////////////

}
