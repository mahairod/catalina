/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * SessionLocker.java
 *
 * Created on January 18, 2006, 4:39 PM
 */

package org.apache.catalina;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;

/**
 *
 * @author  Administrator
 */
public interface SessionLocker {
    
    public void init(Context context);
    
    public boolean lockSession(ServletRequest req) throws ServletException;
    
    public void unlockSession(ServletRequest req);
    
}
