/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
/*
 * PEHADBHealthChecker.java
 *
 * Created on June 9, 2004, 9:52 AM
 */

package com.sun.enterprise.web;

import org.apache.catalina.LifecycleException;
import com.sun.enterprise.web.HealthChecker;


/**
 *
 * @author  lwhite
 */
public class PEHADBHealthChecker implements HealthChecker {

    
    /** Creates a new instance of PEHADBHealthChecker */
    public PEHADBHealthChecker(WebContainer webContainer) {          
    }    
        
    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception IllegalStateException if this component has already been
     *  started
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {
        //no op
    }
        
    
    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception IllegalStateException if this component has not been started
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {
        //no op
    }       
            
}
