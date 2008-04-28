/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * MonitorUtil.java
 *
 * Created on May 7, 2004, 2:09 PM
 */

package com.sun.enterprise.web;

import java.util.logging.*;
import com.sun.logging.*;
import org.apache.catalina.Context;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import com.sun.enterprise.admin.monitor.stats.WebModuleStats;
import com.sun.enterprise.admin.monitor.registry.MonitoringRegistry;

/**
 *
 * @author  lwhite
 */
public final class MonitorUtil {
    
    private static final Logger _logger;

    static {
        _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    }    
    
    /**
     * Resets the stats of all web modules.
     */ 
    public static void resetMonitorStats(EmbeddedWebContainer embedded,
                                         MonitoringRegistry monitoringRegistry) {

        Engine[] engines = embedded.getEngines();            
        for(int h=0; h<engines.length; h++) {
            Container engine = (Container) engines[h];            
            Container[] hosts = engine.findChildren();
            for(int i=0; i<hosts.length; i++) {
                Container nextHost = hosts[i];
                Container [] webModules = nextHost.findChildren();
                for (int j=0; j<webModules.length; j++) {
                    WebModule webModule = (WebModule) webModules[j];
                    if (!webModule.hasWebXml()) {
                        // Ad-hoc module
                        continue;
                    }
                    /*
                     * Standalone webmodules are loaded with the application
                     * name set to the string "null"
                     */
                    String j2eeApp = webModule.getJ2EEApplication();
                    if ("null".equalsIgnoreCase(j2eeApp)) {
                        j2eeApp = null;
                    }
                    WebModuleStats stats =
                        monitoringRegistry.getWebModuleStats(
                            j2eeApp,
                            webModule.getModuleName(),
                            webModule.getPath(),
                            nextHost.getName());
                    if (stats != null) {
                        try {
                            stats.reset();
                        } catch (Throwable th) {
                            _logger.log(Level.SEVERE,
                                        "Error resetting WebModuleStats", th);
                        }                
                    }
                }
            }
        }                 
    }  
    
}
