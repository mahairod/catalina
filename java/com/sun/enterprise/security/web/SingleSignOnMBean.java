/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.security.web;

/**
 * MBean interface exposing stats about Single Sign-On
 */
public interface SingleSignOnMBean {

    /**
     * Gets the number of sessions participating in SSO
     *
     * @return Number of sessions participating in SSO
     */
    public int getActiveSessionCount();

    
    /**
     * Gets the number of SSO cache hits
     *
     * @return Number of SSO cache hits
     */    
    public int getHitCount();

    
    /**
     * Gets the number of SSO cache misses
     *
     * @return Number of SSO cache misses
     */    
    public int getMissCount();
    
}
