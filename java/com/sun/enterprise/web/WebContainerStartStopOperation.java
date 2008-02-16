/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * WebContainerStartStopOperation.java
 * This interface provides for a pluggable
 * implementation of functionality during the
 * stopping of an instance - for PE this is a no-op
 * for EE there is a real implementation
 * HERCULES:add
 *
 * Created on September 24, 2003, 3:38 PM
 */

package com.sun.enterprise.web;

import java.util.ArrayList;

/**
 *
 * @author  lwhite
 */
public interface WebContainerStartStopOperation {
    
    public ArrayList doPreStop();
    
    public void doPostStop(ArrayList list);
    
    public void init(EmbeddedWebContainer embedded);
}
