/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * BaseSessionLocker.java
 *
 * Created on January 18, 2006, 4:46 PM
 */

package org.apache.catalina.session;

import javax.servlet.ServletRequest;
import javax.servlet.ServletException;
import org.apache.catalina.Context;
import org.apache.catalina.SessionLocker;

/**
 *
 * @author lwhite
 */
public class BaseSessionLocker implements SessionLocker {
    
    /** Creates a new instance of BaseSessionLocker */
    public BaseSessionLocker() {
    }
    
    public void init(Context context) {
        _context = context;
    }
    
    public boolean lockSession(ServletRequest req) throws ServletException {
        return true;
    }
    
    public void unlockSession(ServletRequest req) {
    }
    
    protected Context _context = null;
    
}
