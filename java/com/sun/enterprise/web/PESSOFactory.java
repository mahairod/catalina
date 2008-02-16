/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * PESSOFactory.java
 *
 * Created on August 24, 2004, 5:11 PM
 */

package com.sun.enterprise.web;

import com.sun.enterprise.security.web.SingleSignOn;

/**
 *
 * @author lwhite
 */
public class PESSOFactory implements SSOFactory {
    
    /** Creates a new instance of PESSOFactory */
    public PESSOFactory() {
    }
    
    /**
     * Create a SingleSignOn valve
     * @param virtualServerName
     */
    public SingleSignOn createSingleSignOnValve(String virtualServerName) {
        return new SingleSignOn();
    }
    
}
