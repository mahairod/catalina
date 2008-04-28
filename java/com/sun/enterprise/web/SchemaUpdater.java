/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * SchemaUpdater.java
 *
 * Created on May 10, 2005, 4:23 PM
 */

package com.sun.enterprise.web;

import java.io.IOException;
import java.sql.*;

/**
 *
 * @author  lwhite
 */
public interface SchemaUpdater {
    
    public void doSchemaCheck() throws IOException;
    
    public boolean doTablesExist() throws SQLException, ClassNotFoundException;   
    
}
