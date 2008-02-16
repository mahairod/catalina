/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.security;

/**
 * Static class used to preload java classes when using the
 * Java SecurityManager so that the defineClassInPackage
 * RuntimePermission does not trigger an AccessControlException.
 *
 * @author Glenn L. Nielsen
 * @author Jean-Francois Arcand
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:54 $
 */

public final class SecurityClassLoad {

    public static void securityClassLoad(ClassLoader loader)
        throws Exception {

        if( System.getSecurityManager() == null ){
            return;
        }
        
        loadCorePackage(loader);
        loadLoaderPackage(loader);
        loadSessionPackage(loader);
        loadUtilPackage(loader);
        loadJavaxPackage(loader);
        loadCoyotePackage(loader);        
        loadHttp11Package(loader);        
    }
    
    
    private final static void loadCorePackage(ClassLoader loader)
        throws Exception {
        String basePackage = "org.apache.catalina.";
        loader.loadClass
            (basePackage +
             "core.ApplicationContextFacade$1");
        loader.loadClass
            (basePackage +
             "core.ApplicationDispatcher$PrivilegedForward");
        loader.loadClass
            (basePackage +
             "core.ApplicationDispatcher$PrivilegedInclude");
        loader.loadClass
            (basePackage +
             "core.ContainerBase$PrivilegedAddChild");
        loader.loadClass
            (basePackage +
             "core.StandardWrapper$1");
    }
    
    
    private final static void loadLoaderPackage(ClassLoader loader)
        throws Exception {
        String basePackage = "org.apache.catalina.";
        loader.loadClass
            (basePackage +
             "loader.WebappClassLoader$PrivilegedFindResource");
    }
    
    
    private final static void loadSessionPackage(ClassLoader loader)
        throws Exception {
        String basePackage = "org.apache.catalina.";
        loader.loadClass
            (basePackage + "session.StandardSession");
        loader.loadClass
            (basePackage +
             "session.StandardSession$1");
        loader.loadClass
            (basePackage +
             "session.StandardManager$PrivilegedDoUnload");
    }
    
    
    private final static void loadUtilPackage(ClassLoader loader)
        throws Exception {
        String basePackage = "org.apache.catalina.";
        loader.loadClass
            (basePackage + "util.URL");
        loader.loadClass(basePackage + "util.Enumerator");
    }
    
    
    private final static void loadJavaxPackage(ClassLoader loader)
        throws Exception {
        loader.loadClass("javax.servlet.http.Cookie");
    }
    

    private final static void loadHttp11Package(ClassLoader loader)
        throws Exception {
        String basePackage = "org.apache.coyote.http11.";
        loader.loadClass(basePackage + "Http11Processor$1");
        loader.loadClass(basePackage + "InternalOutputBuffer$1");
        loader.loadClass(basePackage + "InternalOutputBuffer$2");
    }
    
    
    private final static void loadCoyotePackage(ClassLoader loader)
        throws Exception {
        String basePackage = "org.apache.coyote.tomcat5.";
        loader.loadClass
            (basePackage +
             "CoyoteRequestFacade$GetAttributePrivilegedAction");
        loader.loadClass
            (basePackage +
             "CoyoteRequestFacade$GetParameterMapPrivilegedAction");
        loader.loadClass
            (basePackage +
             "CoyoteRequestFacade$GetRequestDispatcherPrivilegedAction");
        loader.loadClass
            (basePackage +
             "CoyoteRequestFacade$GetParameterPrivilegedAction");
        loader.loadClass
            (basePackage +
             "CoyoteRequestFacade$GetParameterNamesPrivilegedAction");
        loader.loadClass
            (basePackage +
             "CoyoteRequestFacade$GetParameterValuePrivilegedAction");
        loader.loadClass
            (basePackage +
             "CoyoteRequestFacade$GetCharacterEncodingPrivilegedAction");
        loader.loadClass
            (basePackage +
             "CoyoteRequestFacade$GetHeadersPrivilegedAction");
        loader.loadClass
            (basePackage +
             "CoyoteRequestFacade$GetHeaderNamesPrivilegedAction");  
        loader.loadClass
            (basePackage +
             "CoyoteRequestFacade$GetCookiesPrivilegedAction");
        loader.loadClass
            (basePackage +
             "CoyoteRequestFacade$GetLocalePrivilegedAction");
        loader.loadClass
            (basePackage +
             "CoyoteRequestFacade$GetLocalesPrivilegedAction");
        loader.loadClass
            (basePackage +
             "CoyoteResponseFacade$SetContentTypePrivilegedAction");
        loader.loadClass
            (basePackage +
             "CoyoteRequestFacade$GetSessionPrivilegedAction");
        loader.loadClass
            (basePackage +
             "CoyoteResponseFacade$1");
        loader.loadClass
            (basePackage +
             "OutputBuffer$1");
        loader.loadClass
            (basePackage +
             "CoyoteInputStream$1");
        loader.loadClass
            (basePackage +
             "CoyoteInputStream$2");
        loader.loadClass
            (basePackage +
             "CoyoteInputStream$3");
        loader.loadClass
            (basePackage +
             "CoyoteInputStream$4");
        loader.loadClass
            (basePackage +
             "CoyoteInputStream$5");
        loader.loadClass
            (basePackage +
             "InputBuffer$1");
        loader.loadClass
            (basePackage +
             "CoyoteResponse$1");
        loader.loadClass
            (basePackage +
             "CoyoteResponse$2");
        loader.loadClass
            (basePackage +
             "CoyoteResponse$3");
    }

}
