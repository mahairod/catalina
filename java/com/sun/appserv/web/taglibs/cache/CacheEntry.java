/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.appserv.web.taglibs.cache;

/**
 * Class responsible for caching and expiring the execution result of a JSP
 * fragment.
 */
public class CacheEntry
{
    public static final int NO_TIMEOUT = -1;

    String content;
    volatile long expireTime; 

    /**
     * Constructs a CacheEntry using the response string to be
     * cached and the timeout after which the entry will expire
     */
    public CacheEntry(String response, int timeout) {
        content = response; 
        computeExpireTime(timeout);  
    }

    /**
     * set the real expire time
     * @param expireTime in milli seconds
     */
    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    /**
     * Gets the cached content.
     *
     * @return The cached content
     */
    public String getContent() {
        return this.content;
    }

    /**
     * compute when this entry to be expired based on timeout relative to 
     * current time.
     * @param timeout in seconds
     */
    public void computeExpireTime(int timeout) {
        // timeout is relative to current time
        this.expireTime = (timeout == NO_TIMEOUT) ? timeout :
                          System.currentTimeMillis() + (timeout * 1000);
    }

    /**
     * is this response still valid?
     */
    public boolean isValid() {
        return (expireTime > System.currentTimeMillis() ||
                expireTime == NO_TIMEOUT);
    }

    /** 
     * clear the contents
     */
    public void clear() {
        content = null;
        expireTime = 0L;
    }
}
