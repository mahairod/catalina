/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * ShutdownCleanupCapable.java
 *
 * Created on February 28, 2003, 9:24 AM
 */

package com.sun.enterprise.web;

import java.sql.Connection;

/**
 *
 * @author  lwhite
 */
public interface ShutdownCleanupCapable {
    
    public int doShutdownCleanup();
    
    public void doCloseCachedConnection();
    
    public void putConnection(Connection conn);    
    
}
