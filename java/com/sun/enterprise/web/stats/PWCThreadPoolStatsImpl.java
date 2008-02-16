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
import com.sun.enterprise.admin.monitor.stats.StringStatistic;
import com.sun.enterprise.admin.monitor.stats.StringStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.PWCThreadPoolStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;

/**
 * @author Jan Luehe
 */
public final class  PWCThreadPoolStatsImpl implements PWCThreadPoolStats {

    private static final Logger _logger
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);

    private GenericStatsImpl baseStatsImpl;

    private MBeanServer server;
    private ObjectName threadPoolName;

    private StringStatistic id;
    private MutableCountStatistic countThreadsIdle;
    private MutableCountStatistic countThreads;
    private MutableCountStatistic maxThreads;
    private MutableCountStatistic countQueued;
    private MutableCountStatistic peakQueued;
    private MutableCountStatistic maxQueued;
	

    public PWCThreadPoolStatsImpl(String domain) {
       
        baseStatsImpl = new GenericStatsImpl(
            com.sun.enterprise.admin.monitor.stats.PWCThreadPoolStats.class,
            this);
        
        // get an instance of the MBeanServer
        ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if(!servers.isEmpty())
            server = (MBeanServer)servers.get(0);
        else
            server = MBeanServerFactory.createMBeanServer();
        
        String objNameStr = domain + ":type=Selector,*";
        try {
            threadPoolName = new ObjectName(objNameStr);
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
     * Gets the ID of the thread pool.
     *
     * @return ID of the thread pool
     */
    public StringStatistic getId() {
        return id;
    }
    

    /** 
     * Gets the number of threads that are currently idle.
     *
     * @return Number of currently idle threads
     */    
    public CountStatistic getCountThreadsIdle() {
        countThreadsIdle.setCount(
            StatsUtil.getAggregateStatistic(server, threadPoolName,
                                            "countThreadsIdleStats"));
        return (CountStatistic)countThreadsIdle.unmodifiableView();
    }

    
    /** 
     * Gets the current number of threads.
     *
     * @return Current number of threads
     */    
    public CountStatistic getCountThreads() {
        countThreads.setCount(
            StatsUtil.getAggregateStatistic(server, threadPoolName,
                                            "countThreadsStats"));
        return (CountStatistic)countThreads.unmodifiableView();
    }
    

    /** 
     * Gets the maximum number of threads allowed in the thread pool.
     *
     * @return Maximum number of threads allowed in the thread pool
     */    
    public CountStatistic getMaxThreads() {
        maxThreads.setCount(StatsUtil.getAggregateStatistic(server,
                                                            threadPoolName,
                                                            "maxThreadsStats"));
        return (CountStatistic)maxThreads.unmodifiableView();
    }

    
    /** 
     * Gets the current number of requests waiting for a thread.
     *
     * @return Number of requests waiting for a thread
     */    
    public CountStatistic getCountQueued() {
        return null;
    }

    
    /** 
     * Gets the largest number of requests that were ever queued up
     * simultaneously for the use of a thread since the server was started.
     *
     * @return Largest number of requests that were ever queued up waiting 
     * for a thread
     */    
    public CountStatistic getPeakQueued() {
        return null;
    }

    
    /** 
     * Gets the maximum number of requests that may be queued up
     *
     * @return Maximum number of requests that may be queued up
     */    
    public CountStatistic getMaxQueued() {
        return null;
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
        
        long startTime = System.currentTimeMillis();
        id = new StringStatisticImpl("",
                                     "id",
                                     "String",
                                     "ID of the thread pool",
                                     startTime,
                                     startTime);

        CountStatistic c = new CountStatisticImpl("CountThreadsIdle");
        countThreadsIdle = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountThreads");
        countThreads = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("MaxThreads");
        maxThreads = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("CountQueued");
        countQueued = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("PeakQueued");
        peakQueued = new MutableCountStatisticImpl(c);

        c = new CountStatisticImpl("MaxQueued");
        maxQueued = new MutableCountStatisticImpl(c);
    }
	
}
