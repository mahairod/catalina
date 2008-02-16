/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package com.sun.enterprise.web.stats;

import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.MessageFormat;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.sun.logging.LogDomains;

/**
 * Utility class for retrieving and manipulating stats.
 */
public final class StatsUtil {

    private static final Logger _logger =
        LogDomains.getLogger(LogDomains.WEB_LOGGER);


    /**
     * Queries the MBean with the given object name for the value of the
     * attribute with the given name.
     *
     * @param server MBean server
     * @param on MBean object name
     * @param attrName Attribute name
     *
     * @return Attribute value
     */
    static Object getStatistic(MBeanServer server, ObjectName on,
                                 String attrName) {

        Object resultObj = null;

        try {
            resultObj = server.getAttribute(on, attrName);
        } catch (Throwable t) {
            String msg = _logger.getResourceBundle().getString(
                                            "webcontainer.mbeanQueryError");
            msg = MessageFormat.format(msg, new Object[] { attrName, on });
            _logger.log(Level.WARNING, msg, t);
        }

        return resultObj;
    }

  
    /**
     * Queries the first MBeans corresponding to the given (wildcard)
     * object name for the value of the attribute with the given name, and
     * returns it.
     *
     * This method assumes that the given attribute name has the same value
     * for all MBeans corresponding to the given wildcard object name.
     *
     * @param server MBean server
     * @param on MBean object name
     * @param attrName Attribute name
     *
     * @return Attribute values
     */
    static int getConstant(MBeanServer server, ObjectName on,
                           String attrName) {

	int result = 0;

        Iterator iter = server.queryNames(on, null).iterator();
        if (iter.hasNext()) {
            Object obj = StatsUtil.getStatistic(server,
                                                (ObjectName) iter.next(),
                                                attrName);
            result = getIntValue(obj);
        }

        return result;
    }


    /**
     * Queries the MBeans corresponding to the given (wildcard) object name
     * for the value of the attribute with the given name, and returns the
     * aggregated attribute values.
     *
     * @param server MBean server
     * @param on MBean object name
     * @param attrName Attribute name
     *
     * @return Aggregated attribute values
     */
    static int getAggregateStatistic(MBeanServer server, ObjectName on,
                                     String attrName) {

	int result = 0;

        Iterator iter = server.queryNames(on, null).iterator();
        while (iter.hasNext()) {
            Object obj = StatsUtil.getStatistic(server,
                                                (ObjectName) iter.next(),
                                                attrName);
            if (obj != null) {
                result += getIntValue(obj);
            }
        }

        return result;
    }
    
    
    /**
     * Queries the MBeans corresponding to the given (wildcard) object name
     * for the value of the attribute with the given name, and returns the
     * aggregated attribute values.
     *
     * @param server MBean server
     * @param on MBean object name
     * @param attrName Attribute name
     *
     * @return Aggregated attribute values
     */
    static long getAggregateLongStatistic(MBeanServer server, ObjectName on,
                                          String attrName) {

	long result = 0;

        Iterator iter = server.queryNames(on, null).iterator();
        while (iter.hasNext()) {
            Object obj = StatsUtil.getStatistic(server,
                                                (ObjectName) iter.next(),
                                                attrName);
            if (obj != null) {
                result += getLongValue(obj);
            }
        }

        return result;
    }   


    /**
     * Queries the MBeans corresponding to the given (wildcard) object name
     * for the value of the attribute with the given name, and returns the
     * largest attribute value.
     *
     * @param server MBean server
     * @param on MBean object name
     * @param attrName Attribute name
     *
     * @return Largest attribute value
     */
    static int getMaxStatistic(MBeanServer server, ObjectName on,
                               String attrName) {

	int max = 0;

        Iterator iter = server.queryNames(on, null).iterator();
        while (iter.hasNext()) {
            Object obj = StatsUtil.getStatistic(server,
                                                (ObjectName) iter.next(),
                                                attrName);
            int result = getIntValue(obj);
            if (result > max) {
                max = result;
            }
        }

        return max;
    }


    /**
     * Queries the MBeans corresponding to the given (wildcard) object name
     * for the value of the attribute with the given name, and returns the
     * average attribute value.
     *
     * @param server MBean server
     * @param on MBean object name
     * @param attrName Attribute name
     *
     * @return Average attribute value
     */
    static int getAverageStatistic(MBeanServer server, ObjectName on,
                                   String attrName) {

        int total = 0;
        int num = 0;

        Iterator iter = server.queryNames(on, null).iterator();
        while (iter.hasNext()) {
            Object obj = StatsUtil.getStatistic(server,
                                                (ObjectName) iter.next(),
                                                attrName);
            if (obj != null) {
                total += getIntValue(obj);
                num++;
            }
        }

        return total/num;
    }


    static int getIntValue(Object resultObj) {

        int result = 0;

        if (resultObj instanceof Integer) {
            Integer countObj = (Integer)resultObj;
            result = countObj.intValue();
        }

        return result;
    }


    static long getLongValue(Object resultObj) {

        long result = 0;

        if (resultObj instanceof Long) {
            result = ((Long)resultObj).longValue();
        } else if (resultObj instanceof Integer) {
            result = ((Integer)resultObj).intValue();
        }

        return result;
    }
    
    
    /**
     * Queries the MBeans corresponding to the given (wildcard) object name
     * for the value of the attribute with the given name, and returns the
     * largest attribute value.
     *
     * @param server MBean server
     * @param on MBean object name
     * @param attrName Attribute name
     *
     * @return Largest attribute value
     */
    static long getMaxLongStatistic(MBeanServer server, ObjectName on,
                                    String attrName) {

        long max = 0;

        Iterator iter = server.queryNames(on, null).iterator();
        while (iter.hasNext()) {
            Object obj = StatsUtil.getStatistic(server,
                                                (ObjectName) iter.next(),
                                                attrName);
            long result = getLongValue(obj);
            if (result > max) {
                max = result;
            }
        }

        return max;
    }
}
