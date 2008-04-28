/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package com.sun.enterprise.web.stats;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.MessageFormat;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.Statistic;
import com.sun.logging.LogDomains;
import com.sun.enterprise.admin.monitor.stats.PWCKeepAliveStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;

/**
 * @author Jan Luehe
 */
public final class PWCKeepAliveStatsImpl implements PWCKeepAliveStats {

    private static final Logger _logger
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);

    private GenericStatsImpl baseStatsImpl;

    private MBeanServer server;
    private ObjectName keepAliveName;

    private MutableCountStatistic countConnections;
    private MutableCountStatistic maxConnections;
    private MutableCountStatistic countHits;
    private MutableCountStatistic countFlushes;
    private MutableCountStatistic countRefusals;
    private MutableCountStatistic countTimeouts;
    private MutableCountStatistic secondsTimeouts;
	

    public PWCKeepAliveStatsImpl(String domain) {
       
        baseStatsImpl = new GenericStatsImpl(
            com.sun.enterprise.admin.monitor.stats.PWCKeepAliveStats.class,
            this);
        
        // get an instance of the MBeanServer
        ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if(!servers.isEmpty())
            server = (MBeanServer)servers.get(0);
        else
            server = MBeanServerFactory.createMBeanServer();
        
        String objNameStr = domain + ":type=PWCKeepAlive,*";
        try {
            keepAliveName = new ObjectName(objNameStr);
        } catch (Throwable t) {
            String msg = _logger.getResourceBundle().getString(
                                    "webcontainer.objectNameCreationError");
            msg = MessageFormat.format(msg, new Object[] { objNameStr });
            _logger.log(Level.SEVERE, msg, t);
        }

        // initialize all the MutableStatistic Classes
        initializeStatistics();
    }


    /**
     * Gets the number of connections in keep-alive mode.
     * 
     * @return Number of connections in keep-alive mode
     */    
    public CountStatistic getCountConnections() {
        countConnections.setCount(
            StatsUtil.getAggregateStatistic(server, keepAliveName,
                                            "countConnections"));
        return (CountStatistic)countConnections.unmodifiableView();
    }
    

    /** 
     * Gets the maximum number of concurrent connections in keep-alive mode.
     *
     * @return Maximum number of concurrent connections in keep-alive mode
     */    
    public CountStatistic getMaxConnections() {
        maxConnections.setCount(
            StatsUtil.getConstant(server, keepAliveName, "maxConnections"));
        return (CountStatistic)maxConnections.unmodifiableView();
    }

    
    /** 
     * Gets the number of requests received by connections in keep-alive mode.
     *
     * @return Number of requests received by connections in keep-alive mode.
     */    
    public CountStatistic getCountHits() {
        countHits.setCount(
            StatsUtil.getAggregateStatistic(server, keepAliveName,
                                            "countHits"));
        return (CountStatistic)countHits.unmodifiableView();
    }

    
    /** 
     * Gets the number of keep-alive connections that were closed
     *
     * @return Number of keep-alive connections that were closed
     */    
    public CountStatistic getCountFlushes() {
        countFlushes.setCount(
            StatsUtil.getAggregateStatistic(server, keepAliveName,
                                            "countFlushes"));
        return (CountStatistic)countFlushes.unmodifiableView();
    }

    
    /** 
     * Gets the number of keep-alive connections that were rejected.
     *
     * @return Number of keep-alive connections that were rejected.
     */    
    public CountStatistic getCountRefusals() {
        countRefusals.setCount(
            StatsUtil.getAggregateStatistic(server, keepAliveName,
                                            "countRefusals"));
        return (CountStatistic)countRefusals.unmodifiableView();
    }

    
    /** 
     * Gets the number of keep-alive connections that timed out.
     *
     * @return Number of keep-alive connections that timed out.
     */    
    public CountStatistic getCountTimeouts() {
        countTimeouts.setCount(
            StatsUtil.getAggregateStatistic(server, keepAliveName,
                                            "countTimeouts"));
        return (CountStatistic)countTimeouts.unmodifiableView();
    }

    
    /** 
     * Gets the number of seconds before a keep-alive connection that has
     * been idle times out and is closed.
     *
     * @return Keep-alive timeout in number of seconds
     */    
    public CountStatistic getSecondsTimeouts() {
        secondsTimeouts.setCount(
            StatsUtil.getConstant(server, keepAliveName, "secondsTimeouts"));
        return (CountStatistic)secondsTimeouts.unmodifiableView();
    }
    
    
    /**
     * This method can be used to retrieve all the Statistics, exposed
     * by this implementation of Stats
     * @return Statistic[]
     */
    public Statistic[] getStatistics() {
        return baseStatsImpl.getStatistics();
    }
    

    /**
     * Queries for a statistic with the given name.
     *
     * @name Name of the statistic to query for
     *
     * @return Statistic for the given name
     */ 
    public Statistic getStatistic(String name) {
        return baseStatsImpl.getStatistic(name);
    }

    
    /**
     * Gets array of all statistic names exposed by this implementation of
     * <codeStats</code>
     *
     * @return Array of statistic names
     */ 
    public String[] getStatisticNames() {
        return baseStatsImpl.getStatisticNames();
    }

    
    private void initializeStatistics() {
        
        CountStatistic c = null;

        c = new CountStatisticImpl("CountConnections");
        countConnections = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("MaxConnections");
        maxConnections = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountHits");
        countHits = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountFlushes");
        countFlushes = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountRefusals");
        countRefusals = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountTimeouts");
        countTimeouts = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("SecondsTimeouts");
        secondsTimeouts = new MutableCountStatisticImpl(c);
    }

}
