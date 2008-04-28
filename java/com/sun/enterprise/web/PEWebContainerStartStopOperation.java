/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * PEWebContainerStartStopOperation.java
 *
 * Created on September 24, 2003, 3:47 PM
 * This class is a no-op implementation for stop instance operation
 * HERCULES:add
 */

package com.sun.enterprise.web;

import java.util.ArrayList;

/**
 *
 * @author lwhite
 */
public class PEWebContainerStartStopOperation implements WebContainerStartStopOperation {
    
    /**
     * The embedded Catalina object.
     */
    protected EmbeddedWebContainer _embedded = null;
    
    /** Creates a new instance of PEWebContainerStartStopOperation */
    public PEWebContainerStartStopOperation() {
    }    
    
    /** Creates a new instance of PEWebContainerStartStopOperation */
    public PEWebContainerStartStopOperation(EmbeddedWebContainer embedded) {
        _embedded = embedded;
    }    
    
    public void doPostStop(ArrayList list) {
        //deliberate no-op
    }
    
    public ArrayList doPreStop() {
        //deliberate no-op
        return null;
    }
    
    public void init(EmbeddedWebContainer embedded) {
        _embedded = embedded;
    }
    
}


