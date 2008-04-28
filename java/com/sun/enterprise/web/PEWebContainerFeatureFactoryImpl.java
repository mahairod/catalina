/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web;

import com.sun.enterprise.web.pluggable.WebContainerFeatureFactory;
import com.sun.enterprise.admin.monitor.stats.WebModuleStats;
import com.sun.enterprise.web.stats.WebModuleStatsImpl;
//import com.sun.enterprise.web.WebContainerAdminEventProcessor;
//import com.sun.enterprise.web.PEWebContainerAdminEventProcessor;
import com.sun.enterprise.web.WebContainerStartStopOperation;
import com.sun.enterprise.web.PEWebContainerStartStopOperation;
import com.sun.enterprise.web.SSOFactory;
import com.sun.enterprise.web.PESSOFactory;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

/**
 * Implementation of WebContainerFeatureFactory which returns web container
 * feature implementations for PE.
 */
@Service(name="com.sun.enterprise.web.PEWebContainerFeatureFactoryImpl")
public class PEWebContainerFeatureFactoryImpl
        implements WebContainerFeatureFactory, PostConstruct {

        
    public void postConstruct() {
    }
    
    public WebModuleStats getWebModuleStats() {
        return new WebModuleStatsImpl();
    }
    
    /*
     public WebContainerAdminEventProcessor getWebContainerAdminEventProcessor() {
        return new PEWebContainerAdminEventProcessor();
    }
     */   

    public WebContainerStartStopOperation getWebContainerStartStopOperation() {
        return new PEWebContainerStartStopOperation();
    }
    
    public HealthChecker getHADBHealthChecker(WebContainer webContainer) {
        return new PEHADBHealthChecker(webContainer);
    }
    
    public ReplicationReceiver getReplicationReceiver(EmbeddedWebContainer embedded) {
        return new PEReplicationReceiver(embedded);
    }    
    
    public SSOFactory getSSOFactory() {
        return new PESSOFactory();
    }    

    public VirtualServer getVirtualServer() {
        return new VirtualServer();
    }
    
    public String getSSLImplementationName(){
        return null;
    }

    /**
     * Gets the default access log file prefix.
     *
     * @return The default access log file prefix
     */
    public String getDefaultAccessLogPrefix() {
        return "_access_log.";
    }

    /**
     * Gets the default access log file suffix.
     *
     * @return The default access log file suffix
     */
    public String getDefaultAccessLogSuffix() {
        return ".txt";
    }

    /**
     * Gets the default datestamp pattern to be applied to access log files.
     *
     * @return The default datestamp pattern to be applied to access log files
     */
    public String getDefaultAccessLogDateStampPattern() {
        return "yyyy-MM-dd";
    }

    /**
     * Returns true if the first access log file and all subsequently rotated
     * ones are supposed to be date-stamped, and false if datestamp is to be
     * added only starting with the first rotation.
     *
     * @return true if first access log file and all subsequently rotated
     * ones are supposed to be date-stamped, and false if datestamp is to be
     * added only starting with the first rotation. 
     */
    public boolean getAddDateStampToFirstAccessLogFile() {
        return true;
    }

    /**
     * Gets the default rotation interval in minutes.
     *
     * @return The default rotation interval in minutes
     */
    public int getDefaultRotationIntervalInMinutes() {
        return 15;
    }
}
