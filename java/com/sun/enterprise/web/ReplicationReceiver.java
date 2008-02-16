/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
/*
 * ReplicationReceiver.java
 *
 * Created on January 30, 2006, 11:27 AM
 *
 */

package com.sun.enterprise.web;

import org.apache.catalina.LifecycleException;

/**
 *
 * @author Larry White
 */
public interface ReplicationReceiver { 
    
    public void init();

    public void stop() throws LifecycleException;
    
}
