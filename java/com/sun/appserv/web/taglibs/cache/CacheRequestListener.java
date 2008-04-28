/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
 
package com.sun.appserv.web.taglibs.cache;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import com.sun.appserv.util.cache.Cache;
import com.sun.appserv.web.cache.CacheManager;

/** 
 * ServletRequestListener which creates a cache for JSP tag body invocations
 * and adds it as a request attribute in response to requestInitialized
 * events, and clears the cache in response to requestDestroyed events.
 */
public class CacheRequestListener implements ServletRequestListener {

    /**
     * No-arg constructor
     */
    public CacheRequestListener() {}


    /** 
     * Receives notification that the request is about to enter the scope
     * of the web application, and adds newly created cache for JSP tag
     * body invocations as a request attribute.
     *
     * @param sre the notification event
     */
    public void requestInitialized(ServletRequestEvent sre) {

        ServletContext context = sre.getServletContext();

        // Check if a cache manager has already been created and set in the
        // context
        CacheManager cm = (CacheManager)
            context.getAttribute(CacheManager.CACHE_MANAGER_ATTR_NAME);

        // Create a new cache manager if one is not present and use it
        // to create a new cache
        if (cm == null) {
            cm = new CacheManager();
        }

        Cache cache = null;
        try {
            cache = cm.createCache();
        } catch (Exception ex) {}

        // Set the cache as a request attribute
        if (cache != null) {
            ServletRequest req = sre.getServletRequest();
            req.setAttribute(Constants.JSPTAG_CACHE_KEY, cache);
        }
    }


    /**
     * Receives notification that the request is about to go out of scope
     * of the web application, and clears the request's cache of JSP tag
     * body invocations (if present).
     *
     * @param sre the notification event
     */
    public void requestDestroyed(ServletRequestEvent sre) {

        // Clear the cache
        ServletRequest req = sre.getServletRequest();
        Cache cache = (Cache) req.getAttribute(Constants.JSPTAG_CACHE_KEY);
        if (cache != null) {
            cache.clear();
        }
    }
}
