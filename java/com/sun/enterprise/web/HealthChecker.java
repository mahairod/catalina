/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
/*
 * HealthChecker.java
 *
 * Created on May 9, 2004, 12:31 PM
 */

package com.sun.enterprise.web;

import org.apache.catalina.*;

/**
 *
 * @author  lwhite
 */
public interface HealthChecker {
    
    public void start() throws LifecycleException;
    public void stop() throws LifecycleException;
}
