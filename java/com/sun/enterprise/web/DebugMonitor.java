/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * DebugMonitor.java
 *
 * Created on January 14, 2003, 1:10 PM
 */

package com.sun.enterprise.web;

import java.util.logging.*;
import com.sun.logging.*;
import java.util.Hashtable;
import java.util.Enumeration;
import org.apache.catalina.Context;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Manager;
import org.apache.catalina.session.PersistentManagerBase;
import org.apache.catalina.session.StandardManager;

/**
 *
 * @author  Administrator
 */
public final class DebugMonitor extends java.util.TimerTask {
    
    private Hashtable _instances = null;
    private final EmbeddedWebContainer _embedded;
    private static final Logger _logger;
    private WebContainer webContainer = null;
    static
    {
            _logger=LogDomains.getLogger(LogDomains.WEB_LOGGER);
    }    
    
    /** Creates a new instance of DebugMonitor */
    public DebugMonitor()  {
        _embedded   = null;
    }  
    
    /** Creates a new instance of DebugMonitor */
    public DebugMonitor(EmbeddedWebContainer embedded)  {
        _embedded = embedded;
    } 
    
    public String getApplicationId(Context ctx) {
        com.sun.enterprise.web.WebModule wm = 
            (com.sun.enterprise.web.WebModule)ctx;
        return wm.getID();
    }
    
    public String getApplicationName(Context ctx) {
        return ctx.getName();
    }    
    
    public void run() {
        try {
            Engine[] engines = _embedded.getEngines();
            
            for(int h=0; h<engines.length; h++) {
                Container engine = (Container) engines[h];
                Container[] hosts = engine.findChildren();
                for(int i=0; i<hosts.length; i++) {
                    Container nextHost = hosts[i];
                    Container [] webModules = nextHost.findChildren();
                    for (int j=0; j<webModules.length; j++) {
                        Container nextWebModule = webModules[j];
                        Context ctx = (Context)nextWebModule;
                        //this code gets managers
                        String webAppName = this.getApplicationName(ctx);
                        Manager nextManager = nextWebModule.getManager();
                        _logger.finest("webAppName = " + webAppName);
                        
                        if(nextManager instanceof StandardManager) {
                        } else {
                            _logger.log(Level.SEVERE, "MONITORING::" + webAppName + ": " +
                                       ((PersistentManagerBase)nextManager).getMonitorAttributeValues() );        
                        }                        
                        
                    }                    
                }                 
            }
        } catch (Throwable th) {
            _logger.log(Level.SEVERE, "Exception thrown", th);
        }
                
    }    

} 
