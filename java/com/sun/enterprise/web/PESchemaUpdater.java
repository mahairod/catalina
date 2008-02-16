/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * PESchemaUpdater.java
 *
 * Created on May 10, 2005, 5:01 PM
 */

package com.sun.enterprise.web;

import java.sql.*;

/**
 *
 * @author  lwhite
 */
public class PESchemaUpdater implements SchemaUpdater {
    
    /** Creates a new instance of PESchemaUpdater */
    public PESchemaUpdater() {
    }
    
    public void doSchemaCheck() throws java.io.IOException {
        //deliberate no-op for PE
    }
    
    public boolean doTablesExist() throws SQLException, ClassNotFoundException {
        //deliberate no-op for PE
        return true;
    }
    
}
