/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * WebDebugMonitor.java
 *
 * Created on January 14, 2003, 11:16 AM
 */

package com.sun.enterprise.web;

import java.util.*;
import java.util.logging.*;
import com.sun.logging.*;

/**
 *
 * @author  lwhite
 */
public final class WebDebugMonitor {
    
	private static final Logger _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    
    /** Creates a new instance of WebDebugMonitor */
    public WebDebugMonitor() {
    }
    
    HashMap getDebugMonitoringDetails() { 
        HashMap resultMap = new HashMap();
        boolean debugMonitoring = false;
        resultMap.put("debugMonitoring", Boolean.FALSE);
        long debugMonitoringPeriodMS = 30000L;
	try{
            Properties props = System.getProperties();
            String str=props.getProperty("MONITOR_WEB_CONTAINER");
            if(null!=str) {
                if( str.equalsIgnoreCase("TRUE"))
                //if( str.startsWith("TRUE") || str.startsWith("true") )
                    debugMonitoring=true;
            }
            String period=props.getProperty("MONITOR_WEB_TIME_PERIOD_SECONDS");
            if(null!=period) {
                debugMonitoringPeriodMS = (new Long (period).longValue())* 1000;
            }
            resultMap.put("debugMonitoringPeriodMS",
                          Long.valueOf(debugMonitoringPeriodMS));
            resultMap.put("debugMonitoring",
                          Boolean.valueOf(debugMonitoring));

        } catch(Exception e)
        {
            _logger.log(Level.SEVERE,"WebDebugMonitor.getDebugMonitoringDetails(), Exception when trying to get the System properties - ", e.toString());
        }
        return resultMap;
    }        
    
}
