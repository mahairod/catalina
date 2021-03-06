/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 */

package com.sun.enterprise.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import org.apache.catalina.Container;
import org.apache.catalina.Manager;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Session;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.session.PersistentManagerBase;
import org.apache.catalina.session.StandardSession;

/**
 *
 * @author Larry White
 */
public class PESessionLockingStandardPipeline extends WebPipeline {

    /**
     * The logger to use for logging ALL web container related messages.
     */
    protected static final Logger _logger =
        LogDomains.getLogger(LogDomains.WEB_LOGGER);
    
    /** 
     * creates an instance of PESessionLockingStandardPipeline
     * @param container
     */       
    public PESessionLockingStandardPipeline(Container container) {
        super(container);
    }    
    
    /**
     * Cause the specified request and response to be processed by the Valves
     * associated with this pipeline, until one of these valves causes the
     * response to be created and returned.  The implementation must ensure
     * that multiple simultaneous requests (on different threads) can be
     * processed through the same Pipeline without interfering with each
     * other's control flow.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception is thrown
     */
    public void invoke(Request request, Response response)
        throws IOException, ServletException {
        
        Session sess = this.lockSession(request);
        ((StandardContext) container).beginPipelineInvoke(sess);
        try {
            super.invoke(request, response);
        } finally {
            this.unlockSession(request);
            ((StandardContext) container).endPipelineInvoke();
        }
    }    
    
    /** 
     * get the session associated with this request
     * @param request
     */    
    protected Session getSession(Request request) {
        ServletRequest servletReq = request.getRequest();
        HttpServletRequest httpReq = 
            (HttpServletRequest) servletReq;
        HttpSession httpSess = httpReq.getSession(false);
        if(httpSess == null)
            //need to null out session
            //httpReq.setSession(null);
            return null;
        String id = httpSess.getId();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("SESSION_ID=" + id);
        }
        Manager mgr = this.getContainer().getManager();
        Session sess = null;
        try {
            sess = mgr.findSession(id);
        } catch (java.io.IOException ex) {}
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("RETRIEVED_SESSION=" + sess);
        }
        return sess;
    }          
    
    /** 
     * lock the session associated with this request
     * this will be a foreground lock
     * checks for background lock to clear
     * and does a decay poll loop to wait until
     * it is clear; after 5 times it takes control for 
     * the foreground
     *
     * @param request
     *
     * @return the session that's been locked
     */     
    protected Session lockSession(Request request) throws ServletException {
        Session sess = this.getSession(request);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN LOCK_SESSION: sess =" + sess);
        }
        // Now lock the session
        if(sess != null) {
            long pollTime = 200L;
            int maxNumberOfRetries = 7;
            int tryNumber = 0;
            boolean keepTrying = true;
            boolean lockResult = false;
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("locking session: sess =" + sess);
            }
            StandardSession haSess = (StandardSession) sess;
            // Try to lock up to maxNumberOfRetries times.
            // Poll and wait starting with 200 ms.
            while(keepTrying) {
                lockResult = haSess.lockForeground();
                if(lockResult) {
                    keepTrying = false;
                    break;
                }
                tryNumber++;
                if(tryNumber < maxNumberOfRetries) {
                    pollTime = pollTime * 2L;
                    threadSleep(pollTime);
                } else {
                    // Tried to wait and lock maxNumberOfRetries times.
                    // Unlock the background so we can take over.
                    _logger.warning("this should not happen-breaking background lock: sess =" + sess);
                    haSess.unlockBackground();
                }              
            }
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("finished locking session: sess =" + sess);
                _logger.finest("LOCK = " + haSess.getSessionLock());
            }
        }

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
    protected void unlockSession(Request request) {
        Session sess = this.getSession(request);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN UNLOCK_SESSION: sess = " + sess);
        }
        // Now unlock the session
        if(sess != null) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("unlocking session: sess =" + sess);
            }
            StandardSession haSess = (StandardSession) sess;
            haSess.unlockForeground();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("finished unlocking session: sess =" + sess);
                _logger.finest("LOCK = " + haSess.getSessionLock());
            }
        }        
    }     
}
