/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web.stats;

import com.sun.enterprise.admin.monitor.stats.TimeStatisticImpl;
import com.sun.enterprise.web.monitor.PwcServletStats;

public class ServletTimeStatisticImpl extends TimeStatisticImpl {

    private PwcServletStats servletStats;

    /**
     * Constructor.
     */
    public ServletTimeStatisticImpl(String name, String unit,
                                    PwcServletStats servletStats) {    
        super(name, unit);
        this.servletStats = servletStats;
    }
    
    /**
     * Returns the number of times an operation was invoked 
     *
     * @return long indicating the number of invocations 
     */
    public long getCount() {
        return servletStats.getRequestCount();
    }
    
    /**
     * Returns the maximum amount of time that it took for one invocation of an
     * operation, since measurement started.
     *
     * @return long indicating the maximum time for one invocation
     */
    public long getMaxTime() {
        return servletStats.getMaxTimeMillis();
    }
    
    /**
     * Returns the minimum amount of time that it took for one invocation of an
     * operation, since measurement started.
     *
     * @return long indicating the minimum time for one invocation 
     */
    public long getMinTime() {
        return servletStats.getMinTimeMillis();
    }    

    /**
     * Returns the amount of time that it took for all invocations, 
     * since measurement started.
     *
     * @return long indicating the total time for all invocation 
     */
    public long getTotalTime() {
        return servletStats.getProcessingTimeMillis();
    }
}

