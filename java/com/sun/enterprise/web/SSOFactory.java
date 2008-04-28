/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * SSOFactory.java
 *
 * Created on August 25, 2004, 10:13 AM
 */

package com.sun.enterprise.web;

import com.sun.enterprise.security.web.SingleSignOn;

/**
 *
 * @author lwhite
 */
public interface SSOFactory {
    
    /**
     * Create a SingleSignOn valve
     * @param virtualServerName name of virtual server
     */
    public SingleSignOn createSingleSignOnValve(String virtualServerName);
    
    
}
