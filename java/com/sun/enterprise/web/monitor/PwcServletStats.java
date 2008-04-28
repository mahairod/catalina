/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/*
 * $Id: PwcServletStats.java,v 1.3 2005/12/25 04:27:29 tcfujii Exp $
 * $Date: 2005/12/25 04:27:29 $
 * $Revision: 1.3 $
 *
 */

package com.sun.enterprise.web.monitor;

import java.io.Serializable;

/** 
 * Monitoring interface for servlets.
 */
public interface PwcServletStats extends Serializable {

    /**
     * Gets the number of requests processed by the servlet.
     *
     * @return Number of processed requests
     */
    public int getRequestCount();
    
    /**
     * Gets the total execution time of the servlet's service method.
     *
     * @return Total execution time of the servlet's service method
     */
    public long getProcessingTimeMillis();

    /**
     * Gets the minimum request processing time of the servlet.
     *
     * @return Minimum request processing time
     */
    public long getMinTimeMillis();

    /**
     * Gets the maximum request processing time of the servlet.
     *
     * @return Maximum request processing time
     */
    public long getMaxTimeMillis();

    /** 
     * Gets the number of requests processed by the servlet that have resulted
     * in errors.
     *
     * @return Error count
     */
    public int getErrorCount();
    
}
