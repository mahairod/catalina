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
import com.sun.enterprise.admin.monitor.stats.PWCFileCacheStats;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatistic;
import com.sun.enterprise.admin.monitor.stats.MutableCountStatisticImpl;
import com.sun.enterprise.admin.monitor.stats.GenericStatsImpl;
import com.sun.enterprise.admin.monitor.stats.CountStatisticImpl;


/**
 * <code>FileCache</code> monitoring support.
 *
 * @author Jeanfrancois Arcand
 */
public class PWCFileCacheStatsImpl implements PWCFileCacheStats {
    private final static Logger logger
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    
    private GenericStatsImpl baseStatsImpl;
    private ObjectName fileCacheName;
    private MBeanServer server;
    
    private MutableCountStatistic flagEnabled;    
    private MutableCountStatistic secondsMaxAge;
    private MutableCountStatistic countEntries;
    private MutableCountStatistic maxEntries;
    private MutableCountStatistic countOpenEntries;
    private MutableCountStatistic maxOpenEntries;
    private MutableCountStatistic sizeHeapCache;
    private MutableCountStatistic maxHeapCacheSize;
    private MutableCountStatistic sizeMmapCache;
    private MutableCountStatistic maxMmapCacheSize;
    private MutableCountStatistic countHits;
    private MutableCountStatistic countMisses;
    private MutableCountStatistic countInfoHits;
    private MutableCountStatistic countInfoMisses;
    private MutableCountStatistic countContentHits;
    private MutableCountStatistic countContentMisses;
    
    
    public PWCFileCacheStatsImpl(String domain) {
        
        baseStatsImpl = new GenericStatsImpl(
            com.sun.enterprise.admin.monitor.stats.PWCFileCacheStats.class,
            this);
        
        // get an instance of the MBeanServer
        ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if(!servers.isEmpty())
            server = (MBeanServer)servers.get(0);
        else
            server = MBeanServerFactory.createMBeanServer();
        
        String objNameStr = domain + ":type=PWCFileCache,*";
        try {
            fileCacheName = new ObjectName(objNameStr);
        } catch (Throwable t) {
            String msg = logger.getResourceBundle().getString(
                                    "webcontainer.objectNameCreationError");
            msg = MessageFormat.format(msg, new Object[] { objNameStr });
            logger.log(Level.SEVERE, msg, t);
        }

        // initialize all the MutableStatistic Classes
        initializeStatistics();
    }
    
     
    /** 
     * Returns flag indicating whether file cache has been enabled
     * @return 1 if file cache has been enabled, 0 otherwise
     */
    public CountStatistic getFlagEnabled() {
        flagEnabled.setCount(
            StatsUtil.getMaxStatistic(server, fileCacheName,"flagEnabled"));
        return (CountStatistic)flagEnabled.unmodifiableView();        
    }
    
    
    /** 
     * Return the maximum age of a valid cache entry
     * @return cache entry maximum age
     */
    public CountStatistic getSecondsMaxAge() {
        secondsMaxAge.setCount(
          StatsUtil.getMaxStatistic(server, fileCacheName,"secondsMaxAge"));
        return (CountStatistic)secondsMaxAge.unmodifiableView();        
    }
    
    
    /** 
     * Return the number of current cache entries.  
     */
    public CountStatistic getCountEntries() {
        countEntries.setCount(getAggregateLong("countEntries"));
        return (CountStatistic)countEntries.unmodifiableView();        
    }
    
    
    /** 
     * Return the maximum number of cache entries
     */
    public CountStatistic getMaxEntries() {
        maxEntries.setCount(
                StatsUtil.getMaxStatistic(server, fileCacheName,"maxEntries"));
        return (CountStatistic)maxEntries.unmodifiableView();        
    }
    
    
    /** 
     * Return the number of current open cache entries
     * @return open cache entries
     */
    public CountStatistic getCountOpenEntries() {
        countOpenEntries.setCount(getAggregateLong("countOpenEntries"));
        return (CountStatistic)countOpenEntries.unmodifiableView();        
    }
    
    
    /** 
     * The Maximum number of open cache entries
     */
    public CountStatistic getMaxOpenEntries() {
        maxOpenEntries.setCount(
            StatsUtil.getMaxStatistic(server, fileCacheName,"maxOpenEntries"));
        return (CountStatistic)maxOpenEntries.unmodifiableView();        
    }
    
    
    /** 
     * The  Heap space used for cache
     * @return heap size
     */
    public CountStatistic getSizeHeapCache() {
        sizeHeapCache.setCount(
            StatsUtil.getMaxLongStatistic(server, 
                fileCacheName,"sizeHeapCache"));
        return (CountStatistic)sizeHeapCache.unmodifiableView();        
    }
    
    
    /** 
     * Return he Maximum heap space used for cache
     */
    public CountStatistic getMaxHeapCacheSize() {
        maxHeapCacheSize.setCount(
            StatsUtil.getMaxLongStatistic(server, 
                fileCacheName,"maxHeapCacheSize"));
        return (CountStatistic)maxHeapCacheSize.unmodifiableView();        
    }
    
    
    /** 
     * Return he size of Mapped memory used for caching
     */
    public CountStatistic getSizeMmapCache() {
        sizeMmapCache.setCount(
          StatsUtil.getMaxLongStatistic(server, 
                fileCacheName,"sizeMmapCache"));
        return (CountStatistic)sizeMmapCache.unmodifiableView();        
    }
    
    
    /** 
     * Return the Maximum Memory Map size to be used for caching
     */
    public CountStatistic getMaxMmapCacheSize() {
        maxMmapCacheSize.setCount(
           StatsUtil.getMaxLongStatistic(server, 
                fileCacheName,"maxMmapCacheSize"));
        return (CountStatistic)maxMmapCacheSize.unmodifiableView();        
    }
    
    
    /** 
     * Return he Number of cache lookup hits
     */
    public CountStatistic getCountHits() {
        countHits.setCount(getAggregateLong("countHits"));
        return (CountStatistic)countHits.unmodifiableView();        
    }
    
    
    /** 
     * Return the Number of cache lookup misses
     */
    public CountStatistic getCountMisses() {
        countMisses.setCount(getAggregateLong("countMisses"));
        return (CountStatistic)countMisses.unmodifiableView();        
    }
    
    
    /** 
     * Return the Number of hits on cached file info
     */
    public CountStatistic getCountInfoHits() {
        countInfoHits.setCount(getAggregateLong("countInfoHits"));
        return (CountStatistic)countInfoHits.unmodifiableView();        
    }
    
    
    /** 
     * The Number of misses on cached file info
     * @return misses on cache file info
     */
    public CountStatistic getCountInfoMisses() {
        countInfoMisses.setCount(getAggregateLong("countInfoMisses"));
        return (CountStatistic)countInfoMisses.unmodifiableView();        
    }
    
    
    /** 
     * Return the Number of hits on cached file content
     */
    public CountStatistic getCountContentHits() {
        countContentHits.setCount(getAggregateLong("countContentHits"));
        return (CountStatistic)countContentHits.unmodifiableView();        
    }
    
    
    /** 
     * Return the Number of misses on cached file content
     */
    public CountStatistic getCountContentMisses() {
        countContentMisses.setCount(getAggregateLong("countContentMisses"));
        return (CountStatistic)countContentMisses.unmodifiableView();        
    }

    
    /** 
     * This is an implementation of the mandatory JSR77 Stats
     * interface method.
     * Here we simply delegate it to the GenericStatsImpl object
     * that we have
     */
    public Statistic[] getStatistics() {
        return baseStatsImpl.getStatistics();
    }

    
    public Statistic getStatistic( String str ) {
        return baseStatsImpl.getStatistic( str );
    }

    
    public String[] getStatisticNames() {
        return baseStatsImpl.getStatisticNames();
    }

   
    /**
     * This method initialize statistics.
     */
    private void initializeStatistics() {
        CountStatistic cs = null;
        
        //enabled?
        cs = new CountStatisticImpl("FlagEnabled");
        flagEnabled = new MutableCountStatisticImpl( cs );

        //seconds Max Age
        cs = new CountStatisticImpl("SecondsMaxAge");
        secondsMaxAge = new MutableCountStatisticImpl( cs );

        //count entries
        cs = new CountStatisticImpl("CountEntries");
        countEntries = new MutableCountStatisticImpl( cs );

        //maxEntries
        cs = new CountStatisticImpl("MaxEntries");
        maxEntries = new MutableCountStatisticImpl( cs );

        //Open Entries
        cs = new CountStatisticImpl("CountOpenEntries");
        countOpenEntries = new MutableCountStatisticImpl( cs );

        //Max Open Entries
        cs = new CountStatisticImpl("MaxOpenEntries");
        maxOpenEntries = new MutableCountStatisticImpl( cs );

        // heap cache size
        cs = new CountStatisticImpl("SizeHeapCache");
        sizeHeapCache = new MutableCountStatisticImpl( cs );

        //max heap cache size
        cs = new CountStatisticImpl("MaxHeapCacheSize");
        maxHeapCacheSize = new MutableCountStatisticImpl( cs );

        //Mmap cache size
        cs = new CountStatisticImpl("SizeMmapCache");
        sizeMmapCache = new MutableCountStatisticImpl( cs );

        //Max Mmap cache size
        cs = new CountStatisticImpl("MaxMmapCacheSize");
        maxMmapCacheSize = new MutableCountStatisticImpl( cs );

        //count hits
        cs = new CountStatisticImpl("CountHits");
        countHits = new MutableCountStatisticImpl( cs );

        //count Misses
        cs = new CountStatisticImpl("CountMisses");
        countMisses = new MutableCountStatisticImpl( cs );

        //count Info Hits
        cs = new CountStatisticImpl("CountInfoHits");
        countInfoHits = new MutableCountStatisticImpl( cs );

        //count Info Misses
        cs = new CountStatisticImpl("CountInfoMisses");
        countInfoMisses = new MutableCountStatisticImpl( cs );

        //content hits
        cs = new CountStatisticImpl("CountContentHits");
        countContentHits = new MutableCountStatisticImpl( cs );

        //content misses
        cs = new CountStatisticImpl("CountContentMisses");
        countContentMisses = new MutableCountStatisticImpl( cs );

    }
    
    
    /**
     * Get Aggregated int statistics.
     */
    private final int getAggregateInt(String attribute){
        return StatsUtil.getAggregateStatistic(server,fileCacheName,attribute);
    }
    
    
    /**
     * Get Aggregated long statistics.
     */
    private final long getAggregateLong(String attribute){
        return StatsUtil.
                getAggregateLongStatistic(server,fileCacheName,attribute);
    }
}
