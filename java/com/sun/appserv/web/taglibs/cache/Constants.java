/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.appserv.web.taglibs.cache;

/**
 * Constants used in the Cache tag library.
 */
class Constants
{
    /**
     * The default timeout for cached response.
     */
    public static final int DEFAULT_JSP_CACHE_TIMEOUT = 60;

    /**
     * The context attribute name used to keep store the actual cache.
     */
    public static final String JSPTAG_CACHE_KEY = "com.sun.appserv.web.taglibs.cache.tag_cache";

    /**
     * The request attribute name used to keep track of the cache tag counter.
     * This is used to generate unique keys for every cache tag in a page.
     */
    public static final String JSPTAG_COUNTER_KEY = "com.sun.appserv.web.taglibs.cache.tag_counter";

}
