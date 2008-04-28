/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.enterprise.web.logging.pwc;

import java.util.logging.Logger;

/**
 * Class LogDomains
 */
public class LogDomains
{

    /**
     * DOMAIN_ROOT the prefix for the logger name. This is public only
     * so it can be accessed w/in the ias package space.
     */
    public static final String DOMAIN_ROOT = "javax.";

    /**
     * PACKAGE_ROOT the prefix for the packages where logger resource 
     * bundles reside. This is public only so it can be accessed w/in 
     * the ias package space.
     */
    public static final String PACKAGE_ROOT = "com.sun.";

    /**
     * RESOURCE_BUNDLE the name of the logging resource bundles.
     */
    public static final String RESOURCE_BUNDLE = "LogStrings";

    /**
     * Package where the resource bundle is located
     */
    public static final String PACKAGE = "com.sun.enterprise.web.logging.pwc.";
    /**
     * Field
     */
    public static final String PWC_LOGGER = DOMAIN_ROOT + "enterprise.system.container.web.pwc";

    /**
     * Method getLogger
     *
     *
     * @param name
     *
     * @return
     */
    public static Logger getLogger(String name) {
        return Logger.getLogger(name, PACKAGE + RESOURCE_BUNDLE);
    }
}
