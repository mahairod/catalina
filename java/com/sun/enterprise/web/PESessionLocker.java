/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * PESessionLocker.java
 *
 * Created on January 18, 2006, 4:46 PM
 */

package com.sun.enterprise.web;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import org.apache.catalina.Context;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.SessionLocker;
import org.apache.catalina.session.BaseSessionLocker;
import org.apache.catalina.session.StandardSession;


/**
 *
 * @author lwhite
 */
public class PESessionLocker extends BaseSessionLocker implements SessionLocker {
    
    /** Creates a new instance of PESessionLocker */
    public PESessionLocker() {
    }
    
    /** Creates a new instance of PESessionLocker */
    public PESessionLocker(Context ctx) {
        this();
        _context = ctx;
    }    
    
    /** 
     * lock the session associated with this request
     * this will be a foreground lock
     * checks for background lock to clear
     * and does a decay poll loop to wait until
     * it is clear; after 5 times it takes control for 
     * the foreground
     * @param request
     */     
    public boolean lockSession(ServletRequest request) throws ServletException {
        boolean result = false;
        Session sess = this.getSession(request);
        //now lock the session
        if(sess != null) {
            long pollTime = 200L;
            int maxNumberOfRetries = 7;
            int tryNumber = 0;
            boolean keepTrying = true;
            boolean lockResult = false;
            StandardSession stdSess = (StandardSession) sess;
            //try to lock up to maxNumberOfRetries times
            //poll and wait starting with 200 ms
            while(keepTrying) {
                lockResult = stdSess.lockForeground();
                if(lockResult) {
                    keepTrying = false;
                    result = true;
                    break;
                }
                tryNumber++;
                if(tryNumber < maxNumberOfRetries) {
                    pollTime = pollTime * 2L;
                    threadSleep(pollTime);
                } else {
                    //tried to wait and lock maxNumberOfRetries times; throw an exception
                    //throw new ServletException("unable to acquire session lock");
                    //instead of above; unlock the background so we can take over
                    stdSess.unlockBackground();
                }              
            }
        }
        return result;
    }
    
    private Session getSession(ServletRequest request) {
        javax.servlet.http.HttpServletRequest httpReq = 
            (javax.servlet.http.HttpServletRequest) request;
        javax.servlet.http.HttpSession httpSess = httpReq.getSession(false);
        if(httpSess == null) {
            return null;
        }
        String id = httpSess.getId();
        Manager mgr = _context.getManager();
        Session sess = null;
        try {
            sess = mgr.findSession(id);
        } catch (java.io.IOException ex) {}

        return sess;
    }     
    
    protected void threadSleep(long sleepTime) {

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            ;
        }

    } 
    
    /** 
     * unlock the session associated with this request
     * @param request
     */     
    public void unlockSession(ServletRequest request) {
        Session sess = this.getSession(request);
        //now unlock the session
        if(sess != null) {
            StandardSession stdSess = (StandardSession) sess;
            stdSess.unlockForeground();
        }        
    }    
    
}
