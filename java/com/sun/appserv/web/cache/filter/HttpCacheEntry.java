/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.appserv.web.cache.filter;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Locale;

/** HttpCacheEntry 
 *  Each entry holds cached (HTTP) response:
 *  a) response bytes b) response headers c) expiryTime 
 *  d) parameterEncoding used e) entryKey this entry represents, 
 *  to match the entry within the hash bucket. 
 *
 *  XXX: should implement methods to enable serialization of cached response?
 */
public class HttpCacheEntry {

    public static final int VALUE_NOT_SET = -1;

    int statusCode;
    String statusMessage;

    HashMap responseHeaders;
    HashMap dateHeaders;
    ArrayList cookies;
    String contentType;
    Locale locale;

    int contentLength;

    // XXX: other cacheable response info 
    byte[] bytes; 

    int timeout;
    volatile long expireTime = 0;

    /**
     * set the real expire time
     * @param expireTime in milli seconds
     */
    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    /**
     * compute when this entry to be expired based on timeout relative to 
     * current time.
     * @param timeout in seconds
     */
    public void computeExpireTime(int timeout) {
        this.timeout = timeout;

        // timeout is relative to current time
        this.expireTime = (timeout == -1) ? timeout :
                          System.currentTimeMillis() + (timeout * 1000);
    }

    /**
     * is this response still valid?
     */
    public boolean isValid() {
        return (expireTime > System.currentTimeMillis() || expireTime == -1);
    }

    /** 
     * clear the contents
     */
    public void clear() {
        bytes = null;
        responseHeaders = null;
        cookies = null;
    }

    /**
     * get the size 
     * @return size of this entry in bytes
     * Note: this is only approximate
     */
    public int getSize() {
        int size = 0;
        if (bytes != null) {
            size = bytes.length;
        }

        // size of response bytes plus headers (each approx 20 chars or 40 bytes)
        return (size + (40 * responseHeaders.size()) );
    }
}
