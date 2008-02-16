/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
 
package com.sun.appserv.web.taglibs.cache;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.sun.appserv.util.cache.Cache;
import com.sun.appserv.web.cache.CacheManager;

/** 
 * HttpSessionListener which creates a cache for JSP tag body invocations
 * and adds it as a session attribute in response to sessionCreated events,
 * and clears the cache in response to sessionDestroyed events.
 */
public class CacheSessionListener implements HttpSessionListener {

    /**
     * No-arg constructor
     */
    public CacheSessionListener() {}


    /** 
     * Receives notification that a session was created, and adds newly
     * created cache for JSP tag body invocations as a session attribute.
     *
     * @param hse the notification event
     */
    public void sessionCreated(HttpSessionEvent hse) {
      
        HttpSession session = hse.getSession();  
        ServletContext context = session.getServletContext();

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

        // Set the cache as a session attribute
        if (cache != null) {
            session.setAttribute(Constants.JSPTAG_CACHE_KEY, cache);
        }
    }


    /** 
     * Receives notification that a session is about to be invalidated, and
     * clears the session's cache of JSP tag body invocations (if present).
     *
     * @param hse the notification event
     */
    public void sessionDestroyed(HttpSessionEvent hse) {

        // Clear the cache
        HttpSession session = hse.getSession();  
        Cache cache = (Cache)session.getAttribute(Constants.JSPTAG_CACHE_KEY);
        if (cache != null) {
            cache.clear();
        }
    }
}
