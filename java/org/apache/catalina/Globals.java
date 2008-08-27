/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 *
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




package org.apache.catalina;

// START SJSAS
import org.apache.catalina.servlets.DefaultServlet;
// END SJSAS

/**
 * Global constants that are applicable to multiple packages within Catalina.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.12 $ $Date: 2007/06/18 23:14:22 $
 */

public final class Globals {

    /**
     * The servlet context attribute under which we store the alternate
     * deployment descriptor for this web application 
     */
    public static final String ALT_DD_ATTR = 
        "org.apache.catalina.deploy.alt_dd";

    /**
     * The request attribute under which we store the array of X509Certificate
     * objects representing the certificate chain presented by our client,
     * if any.
     */
    public static final String CERTIFICATES_ATTR =
        "javax.servlet.request.X509Certificate";

    /**
     * SSL Certificate Request Attributite.
     */
    public static final String SSL_CERTIFICATE_ATTR = "com.sun.grizzly.tcp.request.X509Certificate";

    /**
     * The request attribute under which we store the name of the cipher suite
     * being used on an SSL connection (as an object of type
     * java.lang.String).
     */
    public static final String CIPHER_SUITE_ATTR =
        "javax.servlet.request.cipher_suite";


    /**
     * The servlet context attribute under which we store the class loader
     * used for loading servlets (as an object of type java.lang.ClassLoader).
     */
    public static final String CLASS_LOADER_ATTR =
        "org.apache.catalina.classloader";

    /**
     * Request dispatcher state.
     */
    public static final String DISPATCHER_TYPE_ATTR = 
        "org.apache.catalina.core.DISPATCHER_TYPE";

    /**
     * Request dispatcher path.
     */
    public static final String DISPATCHER_REQUEST_PATH_ATTR = 
        "org.apache.catalina.core.DISPATCHER_REQUEST_PATH";

    /**
     * The JNDI directory context which is associated with the context. This
     * context can be used to manipulate static files.
     */
    public static final String RESOURCES_ATTR =
        "org.apache.catalina.resources";
    public static final String ALTERNATE_RESOURCES_ATTR =
        "org.apache.catalina.alternateResources";


    /**
     * The servlet context attribute under which we store the class path
     * for our application class loader (as an object of type String),
     * delimited with the appropriate path delimiter for this platform.
     */
    public static final String CLASS_PATH_ATTR =
        "org.apache.catalina.jsp_classpath";


    /**
     * The request attribute under which we forward a Java exception
     * (as an object of type Throwable) to an error page.
     */
    public static final String EXCEPTION_ATTR =
        "javax.servlet.error.exception";


    /**
     * The request attribute under which we forward the request URI
     * (as an object of type String) of the page on which an error occurred.
     */
    public static final String EXCEPTION_PAGE_ATTR =
        "javax.servlet.error.request_uri";


    /**
     * The request attribute under which we forward a Java exception type
     * (as an object of type Class) to an error page.
     */
    public static final String EXCEPTION_TYPE_ATTR =
        "javax.servlet.error.exception_type";


    /**
     * The request attribute under which we forward an HTTP status message
     * (as an object of type STring) to an error page.
     */
    public static final String ERROR_MESSAGE_ATTR =
        "javax.servlet.error.message";


    /**
     * The request attribute under which the Invoker servlet will store
     * the invoking servlet path, if it was used to execute a servlet
     * indirectly instead of through a servlet mapping.
     */
    public static final String INVOKED_ATTR =
        "org.apache.catalina.INVOKED";


    /**
     * The request attribute under which we expose the value of the
     * <code>&lt;jsp-file&gt;</code> value associated with this servlet,
     * if any.
     */
    public static final String JSP_FILE_ATTR =
        "org.apache.catalina.jsp_file";


    /**
     * The request attribute under which we store the key size being used for
     * this SSL connection (as an object of type java.lang.Integer).
     */
    public static final String KEY_SIZE_ATTR =
        "javax.servlet.request.key_size";


    /**
     * The servlet context attribute under which the managed bean Registry
     * will be stored for privileged contexts (if enabled).
     */
    public static final String MBEAN_REGISTRY_ATTR =
        "org.apache.catalina.Registry";


    /**
     * The servlet context attribute under which the MBeanServer will be stored
     * for privileged contexts (if enabled).
     */
    public static final String MBEAN_SERVER_ATTR =
        "org.apache.catalina.MBeanServer";


    /**
     * The request attribute under which we store the servlet name on a
     * named dispatcher request.
     */
    public static final String NAMED_DISPATCHER_ATTR =
        "org.apache.catalina.NAMED";


    /**
     * The request attribute under which the request URI of the included
     * servlet is stored on an included dispatcher request.
     */
    public static final String INCLUDE_REQUEST_URI_ATTR =
        "javax.servlet.include.request_uri";


    /**
     * The request attribute under which the context path of the included
     * servlet is stored on an included dispatcher request.
     */
    public static final String INCLUDE_CONTEXT_PATH_ATTR =
        "javax.servlet.include.context_path";


    /**
     * The request attribute under which the path info of the included
     * servlet is stored on an included dispatcher request.
     */
    public static final String INCLUDE_PATH_INFO_ATTR =
        "javax.servlet.include.path_info";


    /**
     * The request attribute under which the servlet path of the included
     * servlet is stored on an included dispatcher request.
     */
    public static final String INCLUDE_SERVLET_PATH_ATTR =
        "javax.servlet.include.servlet_path";


    /**
     * The request attribute under which the query string of the included
     * servlet is stored on an included dispatcher request.
     */
    public static final String INCLUDE_QUERY_STRING_ATTR =
        "javax.servlet.include.query_string";


    /**
     * The request attribute under which the original request URI is stored
     * on an forwarded dispatcher request.
     */
    public static final String FORWARD_REQUEST_URI_ATTR =
        "javax.servlet.forward.request_uri";
    
    
    /**
     * The request attribute under which the original context path is stored
     * on an forwarded dispatcher request.
     */
    public static final String FORWARD_CONTEXT_PATH_ATTR =
        "javax.servlet.forward.context_path";


    /**
     * The request attribute under which the original path info is stored
     * on an forwarded dispatcher request.
     */
    public static final String FORWARD_PATH_INFO_ATTR =
        "javax.servlet.forward.path_info";


    /**
     * The request attribute under which the original servlet path is stored
     * on an forwarded dispatcher request.
     */
    public static final String FORWARD_SERVLET_PATH_ATTR =
        "javax.servlet.forward.servlet_path";


    /**
     * The request attribute under which the original query string is stored
     * on an forwarded dispatcher request.
     */
    public static final String FORWARD_QUERY_STRING_ATTR =
        "javax.servlet.forward.query_string";
    
    
    /**
     * The request attribute under which we forward a servlet name to
     * an error page.
     */
    public static final String SERVLET_NAME_ATTR =
        "javax.servlet.error.servlet_name";


    /**
     * The name of the cookie used to pass the session identifier back
     * and forth with the client.
     */
    public static final String SESSION_COOKIE_NAME = "JSESSIONID";


    /**
     * The name of the path parameter used to pass the session identifier
     * back and forth with the client.
     */
    public static final String SESSION_PARAMETER_NAME = "jsessionid";


    /**
     * The request attribute under which we forward an HTTP status code
     * (as an object of type Integer) to an error page.
     */
    public static final String STATUS_CODE_ATTR =
        "javax.servlet.error.status_code";


    /**
     * The subject under which the AccessControlContext is running.
     */
    public static final String SUBJECT_ATTR =
        "javax.security.auth.subject";

    
    /**
     * The servlet context attribute under which we record the set of
     * welcome files (as an object of type String[]) for this application.
     */
    public static final String WELCOME_FILES_ATTR =
        "org.apache.catalina.WELCOME_FILES";


    /**
     * The servlet context attribute under which we store a temporary
     * working directory (as an object of type File) for use by servlets
     * within this web application.
     */
    public static final String WORK_DIR_ATTR =
        "javax.servlet.context.tempdir";


    // START SJSAS
    /**
     * The class name of the default servlet
     */
    public static final String DEFAULT_SERVLET_CLASS_NAME =
        DefaultServlet.class.getName();
    // END SJSAS


    /**
     * Has security been turned on?
     */
    public static final boolean IS_SECURITY_ENABLED = 
        (System.getSecurityManager() != null);


    // START GlassFish 740
    public static final String JSP_PROPERTY_GROUPS_CONTEXT_ATTRIBUTE =
        "com.sun.jsp.propertyGroups";

    public static final String WEB_XML_VERSION_CONTEXT_ATTRIBUTE =
        "com.sun.servlet.webxml.version";
    // END GlassFish 740

    // START GlassFish 747
    public static final String JSP_TLD_URI_TO_LOCATION_MAP =
        "com.sun.jsp.tldUriToLocationMap";
    // END GlassFish 747

    // START GlassFish 896
    public static final String SESSION_TRACKER =
        "com.sun.enterprise.http.sessionTracker";    
    // END GlassFish 896

    /**
     * The name of the cookie used to carry a session's version info
     */
    public static final String SESSION_VERSION_COOKIE_NAME =
        "JSESSIONIDVERSION";

    /**
     * The name of the path parameter used to carry a session's version info
     */
    public static final String SESSION_VERSION_PARAMETER_NAME =
        "jsessionidversion";

    public static final String SESSION_VERSION_PARAMETER =
        ";" + SESSION_VERSION_PARAMETER_NAME + "=";

    public static final String SESSION_VERSIONS_REQUEST_ATTRIBUTE =
        "com.sun.enterprise.http.sessionVersions";

    public static final String WRAPPED_REQUEST =
        "__javax.security.auth.message.request";
    
    public static final String WRAPPED_RESPONSE =
        "__javax.security.auth.message.response"; 
    
    
    /**
     * The servlet context attribute under which we store a flag used
     * to mark this request as having been processed by the SSIServlet.
     * We do this because of the pathInfo mangling happening when using
     * the CGIServlet in conjunction with the SSI servlet. (value stored
     * as an object of type String)
     */
     public static final String SSI_FLAG_ATTR =
         "org.apache.catalina.ssi.SSIServlet";

    /**
     * Request path.
     */
    public static final String CONSTRAINT_URI =
        "org.apache.catalina.CONSTRAINT_URI";
}
