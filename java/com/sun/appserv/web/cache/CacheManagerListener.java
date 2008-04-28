/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.appserv.web.cache;

/**
 * listener for the CacheManager events
 */
public interface CacheManagerListener {
   /**
    * cache manager was enabled
    */
   public void cacheManagerEnabled(); 

   /**
    * cache manager was disabled
    */
   public void cacheManagerDisabled(); 
}
