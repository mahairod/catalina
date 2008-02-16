/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.appserv.web.cache;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/** CacheKeyGenerator: a helper interface to generate the key that is 
 *  used to cache this request.
 */
public interface CacheKeyGenerator {
    
    /** getCacheKey: generate the key to be used to cache the response.
     *  @param context the web application context
     *  @param request incoming <code>HttpServletRequest</code>
     *  @return key string used to access the cache entry.
     *  if the return value is null, a default key is used.
     */
    public String getCacheKey(ServletContext context,
                                HttpServletRequest request);
}
