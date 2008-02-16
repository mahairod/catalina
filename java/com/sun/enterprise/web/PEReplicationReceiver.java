/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
/*
 * PEReplicationReceiver.java
 *
 * Created on January 30, 2006, 11:33 AM
 *
 */

package com.sun.enterprise.web;

import org.apache.catalina.LifecycleException;

/**
 *
 * @author Larry White
 */
public class PEReplicationReceiver implements ReplicationReceiver {
    
    /** Creates a new instance of PEReplicationReceiver */
    public PEReplicationReceiver(EmbeddedWebContainer embedded) {
    }
    
    public void init() {
        //no-op
    }
    
    public void stop() throws LifecycleException {
        //no-op
    }
    
}
