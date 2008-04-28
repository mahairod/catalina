/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.appserv.web.taglibs.cache;

import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import com.sun.enterprise.web.logging.pwc.LogDomains; 

import com.sun.appserv.util.cache.Cache;

/**
 * FlushTag is a JSP tag that is used with the CacheTag. The FlushTag
 * allows you to invalidate a complete cache or a particular cache element
 * identified by the key.
 *
 * Usage Example:
 * <%@ taglib prefix="ias" uri="Sun ONE Application Server Tags" %> 
 * <ias:flush key="<%= cacheKey %>" />
 */
public class FlushTag extends TagSupport
{
    /**
     * The key for the cache entry that needs to be flushed.
     */
    private String _key;

    /**
     * This specifies the scope of the cache that needs to be flushed.
     */
    private int _scope = PageContext.APPLICATION_SCOPE;

    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null;

    /**
     * This indicates whether debug logging is on or not
     */
    private static boolean _debugLog;

    /**
     * The resource bundle containing the localized message strings.
     */
    private static ResourceBundle _rb = null;

    // ---------------------------------------------------------------------
    // Constructor and initialization

    /**
     * Default constructor that simply gets a handle to the web container
     * subsystem's logger.
     */
    public FlushTag() {
        super();
         if (_logger == null) {
             _logger = LogDomains.getLogger(LogDomains.PWC_LOGGER);
             _rb = _logger.getResourceBundle();
             _debugLog = _logger.isLoggable(Level.FINE);
         }
    }

    // ---------------------------------------------------------------------
    // Tag logic

    /**
     * doStartTag is called when the flush tag is encountered. By
     * the time this is called, the tag attributes are already set.
     *
     * @throws JspException the standard exception thrown
     * @return SKIP_BODY since the tag should be empty
     */
    public int doStartTag()
        throws JspException
    {
        // get the cache from the specified scope
        Cache cache = CacheUtil.getCache(pageContext, _scope);

        // generate the cache key using the user specified key.
   
        if (_key != null) {
            String key = CacheUtil.generateKey(_key, pageContext);

            // remove the entry for the key
            cache.remove(key);

            if (_debugLog)
                _logger.fine("FlushTag: clear ["+ key +"]");
        } else {
            // clear the entire cache
            cache.clear();

            if (_debugLog)
                _logger.fine("FlushTag: clear cache");
        }

        return SKIP_BODY;
    }

    /**
     * doEndTag just resets all the valiables in case the tag is reused
     *
     * @throws JspException the standard exception thrown
     * @return always returns EVAL_PAGE since we want the entire jsp evaluated
     */
    public int doEndTag()
        throws JspException
    {
        _key = null;
        _scope = PageContext.APPLICATION_SCOPE;

        return EVAL_PAGE;
    }

    // ---------------------------------------------------------------------
    // Attribute setters

    /**
     * This is set a key for the cache element that needs to be cleared
     */
    public void setKey(String key) {
        if (key != null && key.length() > 0)
            _key = key;
    }

    /**
     * Sets the scope of the cache.
     *
     * @param scope the scope of the cache
     *
     * @throws IllegalArgumentException if the specified scope is different
     * from request, session, and application
     */
    public void setScope(String scope) {
        _scope = CacheUtil.convertScope(scope);
    }
}
