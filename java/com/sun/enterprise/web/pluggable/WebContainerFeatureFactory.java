/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web.pluggable;

import com.sun.enterprise.admin.monitor.stats.WebModuleStats;
import com.sun.enterprise.web.EmbeddedWebContainer;
import com.sun.enterprise.web.HealthChecker;
import com.sun.enterprise.web.ReplicationReceiver;
import com.sun.enterprise.web.SSOFactory;
import com.sun.enterprise.web.WebContainer;
//import com.sun.enterprise.web.WebContainerAdminEventProcessor;
import com.sun.enterprise.web.WebContainerStartStopOperation;
import com.sun.enterprise.web.VirtualServer;

import org.jvnet.hk2.annotations.Contract;

/**
 * Interface for getting webcontainer specific pluggable features.
 */
@Contract
public interface WebContainerFeatureFactory {

    public WebModuleStats getWebModuleStats();
    
    //public WebContainerAdminEventProcessor getWebContainerAdminEventProcessor();    
    
    public WebContainerStartStopOperation getWebContainerStartStopOperation();
    
    public HealthChecker getHADBHealthChecker(WebContainer webContainer);
    
    public ReplicationReceiver getReplicationReceiver(EmbeddedWebContainer embedded);

    public VirtualServer getVirtualServer();
    
    public SSOFactory getSSOFactory();   
    
    public String getSSLImplementationName();

    /**
     * Gets the default access log file prefix.
     *
     * @return The default access log file prefix
     */
    public String getDefaultAccessLogPrefix();

    /**
     * Gets the default access log file suffix.
     *
     * @return The default access log file suffix
     */
    public String getDefaultAccessLogSuffix();

    /**
     * Gets the default datestamp pattern to be applied to access log files.
     *
     * @return The default datestamp pattern to be applied to access log files
     */
    public String getDefaultAccessLogDateStampPattern();

    /**
     * Returns true if the first access log file and all subsequently rotated
     * ones are supposed to be date-stamped, and false if datestamp is to be
     * added only starting with the first rotation.
     *
     * @return true if first access log file and all subsequently rotated
     * ones are supposed to be date-stamped, and false if datestamp is to be
     * added only starting with the first rotation. 
     */
    public boolean getAddDateStampToFirstAccessLogFile();

    /**
     * Gets the default rotation interval in minutes.
     *
     * @return The default rotation interval in minutes
     */
    public int getDefaultRotationIntervalInMinutes();
}
