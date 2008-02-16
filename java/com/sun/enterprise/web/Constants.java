/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package com.sun.enterprise.web;


/**
 * Static constants for this package.
 */

public final class Constants {

    public static final String Package = "com.sun.enterprise.web";

    /**
     * Path to web application context.xml configuration file.
     */

    public static final String WEB_CONTEXT_XML = "META-INF/context.xml";

    /**
     * The default location of the global context.xml file.
     *
     * Path to global context.xml (relative to instance root).
     */
    public static final String DEFAULT_CONTEXT_XML = "config/context.xml";

    /**
     * The default web application's deployment descriptor location.
     *
     * This path is relative to catalina.home i.e. the instance root directory.
     */
    public static final String DEFAULT_WEB_XML = "config/default-web.xml";

    /**
     * The system-assigned default web module's name/identifier.
     *
     * This has to be the same value as is in j2ee/WebModule.cpp.
     */
    public static final String DEFAULT_WEB_MODULE_NAME = "__default-web-module";

    /**
     * The separator character between an application name and the web
     * module name within the application.
     */
    public static final String NAME_SEPARATOR = ":";

    /**
     * The string to prefix to the name of the web module when a web module
     * is designated to be the default-web-module for a virtual server.
     *
     * This serves as a way to differentiate the web module from the
     * variant that is deployed as a 'default web module' at a context root
     * of "".
     */
    public static final String DEFAULT_WEB_MODULE_PREFIX = "__default-";

    /**
     * Name of the class that implements invocation/security/transaction
     * manager hooks for web application events.
     */
    public static final String J2EE_INSTANCE_LISTENER =
        "com.sun.web.server.J2EEInstanceListener";

    /**
     * The Apache Jasper JSP servlet class name.
     */
    public static final String APACHE_JSP_SERVLET_CLASS =
        "org.apache.jasper.servlet.JspServlet";

    public static final String JSP_URL_PATTERN="*.jsp";


    public static final String REQUEST_START_TIME_NOTE =
        "com.sun.enterprise.web.request.startTime";

    public static final String ACCESS_LOG_PROPERTY = "accesslog";

    public static final String ACCESS_LOG_BUFFER_SIZE_PROPERTY =
        "accessLogBufferSize";

    public static final String ACCESS_LOG_WRITE_INTERVAL_PROPERTY =
        "accessLogWriteInterval";

    public static final String ACCESS_LOGGING_ENABLED = "accessLoggingEnabled";

    public static final String SSO_ENABLED = "sso-enabled";
}
