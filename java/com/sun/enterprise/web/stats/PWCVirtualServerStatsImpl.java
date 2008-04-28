/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web.stats;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.Statistic;
import com.sun.logging.LogDomains;
import com.sun.enterprise.web.VirtualServer;
import com.sun.enterprise.admin.monitor.stats.StringStatistic;
import com.sun.enterprise.admin.monitor.stats.StringStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.PWCVirtualServerStats;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;

/** 
 * Class representing Virtual Server stats in PE.
 */
public class PWCVirtualServerStatsImpl implements PWCVirtualServerStats {

    private static final Logger _logger = LogDomains.getLogger(
                                                    LogDomains.WEB_LOGGER);

    private long startTime;
    private GenericStatsImpl baseStatsImpl;
    private StringStatistic idStats;
    private StringStatistic modeStats;
    private StringStatistic hostsStats;
    private StringStatistic interfacesStats;

    /*
     * Constructor.
     */
    public PWCVirtualServerStatsImpl(VirtualServer vs) {

        initializeStatistics(vs);

        baseStatsImpl = new GenericStatsImpl(
            com.sun.enterprise.admin.monitor.stats.PWCVirtualServerStats.class,
            this);
    }

    /** 
     * Returns the virtual server ID.
     *
     * @return Virtual server ID
     */    
    public StringStatistic getId() {
        return idStats;
    }

    /** 
     * Returns the virtual server mode.
     *
     * @return Virtual server mode
     */    
    public StringStatistic getMode() {
        return modeStats;
    }

    /** 
     * Returns the host names of this virtual server
     *
     * @return Host names of this virtual server
     */    
    public StringStatistic getHosts() {
        return hostsStats;
    }
    
    /** 
     * Returns the interfaces of this virtual server
     *
     * @return Interfaces of this virtual server
     */    
    public StringStatistic getInterfaces() {
        return interfacesStats;
    }

    public Statistic[] getStatistics() {
        return baseStatsImpl.getStatistics();
    }
    
    public String[] getStatisticNames() {
        return baseStatsImpl.getStatisticNames();
    }

    public Statistic getStatistic(String str) {
        return baseStatsImpl.getStatistic(str);
    }

    /**
     * Initializes the stats from the given virtual server
     *
     * @param vs Virtual server from which to derive stats
     */
    private void initializeStatistics(VirtualServer vs) {

        startTime = System.currentTimeMillis();

        // ID
        idStats = new StringStatisticImpl(
                                vs.getID(),
                                "Id",
                                "String",
                                "Virtual Server ID",
                                startTime,
                                startTime);

        // Mode
        modeStats = new StringStatisticImpl(
                                vs.isActive() ? "active" : "unknown",
                                "Mode",
                                "unknown/active",
                                "Virtual Server mode",
                                startTime,
                                startTime);

        // Hosts
        String hosts = null;
        String[] aliases = vs.findAliases();
        if (aliases != null) {
            for (int i=0; i<aliases.length; i++) {
                if (hosts == null) {
                    hosts = aliases[i];
                } else {
                    hosts += ", " + aliases[i];
                }
            }
        }
        hostsStats = new StringStatisticImpl(
                                hosts,
                                "Hosts",
                                "String",
                                "The software virtual hostnames serviced by "
                                + "this Virtual Server",
                                startTime,
                                startTime);

        // Interfaces
        interfacesStats = new StringStatisticImpl(
                                "0.0.0.0", // XXX FIX
                                "Interfaces",
                                "String",
                                "The interfaces for which this Virtual Server "
                                + "has been configured",
                                startTime,
                                startTime);
    }


}
