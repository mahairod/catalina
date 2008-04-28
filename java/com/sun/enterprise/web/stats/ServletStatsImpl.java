/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web.stats;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.TimeStatistic;
import com.sun.logging.LogDomains;
import com.sun.enterprise.admin.monitor.stats.ServletStats;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.web.monitor.PwcServletStats;


public class ServletStatsImpl implements ServletStats {
    
    private static final Logger _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);

    private GenericStatsImpl baseStatsImpl;
    private MutableCountStatistic maxTimeMillis;
    private MutableCountStatistic processingTimeMillis;
    private TimeStatistic serviceTimeMillis;
    private MutableCountStatistic requestCount;
    private MutableCountStatistic errorCount;
    private PwcServletStats pwcServletStats;

    
    /**
     * Constructor.
     *
     * @param pwcServletStats PwcServletStats instance to which to delegate
     */
    public ServletStatsImpl(PwcServletStats pwcServletStats) {

        this.pwcServletStats = pwcServletStats;

        baseStatsImpl = new GenericStatsImpl(
            com.sun.enterprise.admin.monitor.stats.ServletStats.class, this);

        // initialize all the MutableStatistic Classes
        initializeStatistics();
    }
    
    
    /**
     * The maximum processing time of a servlet request
     * @return CountStatistic
     */
    public CountStatistic getMaxTime() {
        maxTimeMillis.setCount(pwcServletStats.getMaxTimeMillis());
        return (CountStatistic)maxTimeMillis.unmodifiableView();
    }

    
    /**
     * Gets the total execution time of the servlet's service method.
     *
     * @return Total execution time of the servlet's service method
     */
    public CountStatistic getProcessingTime() {
        processingTimeMillis.setCount(pwcServletStats.getProcessingTimeMillis());
        return (CountStatistic)processingTimeMillis.unmodifiableView();
    }
    

    /**
     * Gets the execution time of the servlet's service method as a
     * TimeStatistic.
     *
     * @return Execution time of the servlet's service method
     */
    public TimeStatistic getServiceTime() {
        return serviceTimeMillis;
    }


    /**
     * Number of requests processed by this servlet.
     * @return CountStatistic
     */
    public CountStatistic getRequestCount() {
        requestCount.setCount(pwcServletStats.getRequestCount());
        return (CountStatistic)requestCount.unmodifiableView();
    }

    
    /** 
     * The errorCount represents the number of cases where the response
     * code was >= 400
     * @return CountStatistic
     */
    public CountStatistic getErrorCount() {
        errorCount.setCount(pwcServletStats.getErrorCount());
        return (CountStatistic)errorCount.unmodifiableView();
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
     * queries for a Statistic by name.
     * @return  Statistic
     */ 
    public Statistic getStatistic(String str) {
        return baseStatsImpl.getStatistic(str);
    }

    
    /**
     * returns an array of names of all the Statistics, that can be
     * retrieved from this implementation of Stats
     * @return  String[]
     */ 
    public String[] getStatisticNames() {
        return baseStatsImpl.getStatisticNames();
    }

    
    private void initializeStatistics() {
        
       // Initialize the MutableCountStatistic for ErrorCount
        CountStatistic c = new CountStatisticImpl("ErrorCount");
        errorCount = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for MaxTime
        c = new CountStatisticImpl("MaxTime", "milliseconds");
        maxTimeMillis = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for ProcessingTime
        c = new CountStatisticImpl("ProcessingTime", "milliseconds");
        processingTimeMillis = new MutableCountStatisticImpl(c);
        
        // Initialize the MutableCountStatistic for RequestCount
        c = new CountStatisticImpl("RequestCount");
        requestCount = new MutableCountStatisticImpl(c);

        // Initialize the MutableTimeStatistic for ServiceTime
        serviceTimeMillis = new ServletTimeStatisticImpl("ServiceTime",
                                                         "milliseconds",
                                                         pwcServletStats);
    }
    
}
