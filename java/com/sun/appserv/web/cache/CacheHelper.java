/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.appserv.web.cache;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/** CacheHelper interface is an user-extensible interface to customize: 
 *  a) the key generation b) whether to cache the response.
 */
public interface CacheHelper {

    // name of request attributes
    public static final String ATTR_CACHE_MAPPED_SERVLET_NAME = 
                                    "com.sun.appserv.web.cachedServletName";
    public static final String ATTR_CACHE_MAPPED_URL_PATTERN = 
                                    "com.sun.appserv.web.cachedURLPattern";

    public static final int TIMEOUT_VALUE_NOT_SET = -2;

    /** initialize the helper 
     *  @param context the web application context this helper belongs to
     *  @exception Exception if a startup error occurs
     */
    public void init(ServletContext context, Map props) throws Exception;

    /** getCacheKey: generate the key to be used to cache this request 
     *  @param request incoming <code>HttpServletRequest</code> object
     *  @return the generated key for this requested cacheable resource.
     */
    public String getCacheKey(HttpServletRequest request);

    /** isCacheable: is the response to given request cachebale? 
     *  @param request incoming <code>HttpServletRequest</code> object
     *  @return <code>true</code> if the response could be cached. or 
     *  <code>false</code> if the results of this request must not be cached.
     */
    public boolean isCacheable(HttpServletRequest request);

    /** isRefreshNeeded: is the response to given request be refreshed?
     *  @param request incoming <code>HttpServletRequest</code> object
     *  @return <code>true</code> if the response needs to be refreshed.
     *  or return <code>false</code> if the results of this request 
     *  don't need to be refreshed.
     */
    public boolean isRefreshNeeded(HttpServletRequest request);

    /** get timeout for the cached response.
     *  @param request incoming <code>HttpServletRequest</code> object
     *  @return the timeout in seconds for the cached response; a return
     *  value of -1 means the response never expires and a value of -2 indicates
     *  helper cannot determine the timeout (container assigns default timeout)
     */ 
    public int getTimeout(HttpServletRequest request);

    /**
     * Stop the helper from active use
     * @exception Exception if an error occurs
     */
    public void destroy() throws Exception;
}
