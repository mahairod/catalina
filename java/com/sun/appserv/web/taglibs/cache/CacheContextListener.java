/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.appserv.web.taglibs.cache;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.sun.appserv.util.cache.Cache;
import com.sun.appserv.web.cache.CacheManager;

/** 
 * CacheContextListener implements the ServletContextListener interface
 * in order to be notified when the context is created and destroyed. 
 * It is used to create the cache and add it as a context attribute.
 */
public class CacheContextListener implements ServletContextListener
{
    /**
     * Public constructor taking no arguments according to servlet spec
     */
    public CacheContextListener() {}

    /**
     * This is called when the context is created.
     */
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        // see if a cache manager is already created and set in the context
        CacheManager cm = (CacheManager)context.getAttribute(CacheManager.CACHE_MANAGER_ATTR_NAME);

        // create a new cachemanager if one is not present and use it
        // to create a new cache
        if (cm == null)
            cm = new CacheManager();

        Cache cache = null;
        try {
            cache = cm.createCache();
        } catch (Exception ex) {}

        // set the cache as a context attribute
        if (cache != null)
            context.setAttribute(Constants.JSPTAG_CACHE_KEY, cache);
    }

    /**
     * This is called when the context is shutdown.
     */
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        // Remove the cache from context and clear the cache
        Cache cache = (Cache)context.getAttribute(Constants.JSPTAG_CACHE_KEY);

        if (cache != null) {
            context.removeAttribute(Constants.JSPTAG_CACHE_KEY);
            cache.clear();
        }
    }
}
