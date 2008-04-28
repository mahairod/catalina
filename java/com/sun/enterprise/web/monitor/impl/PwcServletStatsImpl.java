/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.enterprise.web.monitor.impl;

import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.management.ObjectName;
import javax.management.MBeanServerFactory;
import javax.management.MBeanServer;
import com.sun.enterprise.web.monitor.PwcServletStats;


public class PwcServletStatsImpl implements PwcServletStats {
    
    private transient MBeanServer server;
    private ObjectName servletObjName;

    
    /** 
     * Constructor.
     *
     * The ObjectName of the Servlet MBean follows this pattern:
     *
     *   <domain>:j2eeType=Servlet,name=<servlet/jsp name>, 
     *   WebModule=<webmodule name>,J2EEApplication=<application name>,
     *   J2EEServer=<server name> 
     *
     * Example: com.sun.appserv:j2eeType=Servlet,name=default,
     *          WebModule=//server/,J2EEApplication=null,J2EEServer=server
     *
     * @param domain Domain in which the Servlet MBean is registered
     * @param vsId Id of the virtual-server for which servlet monitoring
     *        is being enabled
     * @param contextPath Context path of the webmodule
     * @param servletName Name of the Servlet/JSP
     * @param appName Name of the J2EE App to which the web module belongs, or
     *        null if web module is standalone
     * @param serverName Name of the server instance
     */
    public PwcServletStatsImpl(String domain,
                               String vsId,
                               String contextPath,
                               String servletName,
                               String appName,
                               String serverName) {
        
        // Get an instance of the MBeanServer
        ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if(servers != null && !servers.isEmpty())
            server = (MBeanServer)servers.get(0);
        else
            server = MBeanServerFactory.createMBeanServer();

        // Construct the ObjectName of the Servlet MBean
        String objNameStr = domain
                + ":j2eeType=Servlet"
                + ",name=" + servletName
                + ",WebModule=" + createTomcatWebModuleName(vsId, contextPath)
                + ",J2EEApplication=" + appName
                + ",J2EEServer=" + serverName;
        try {
            servletObjName = new ObjectName(objNameStr);
        } catch (Throwable t) {
            MonitorUtil.log(Level.SEVERE,
                            "pwc.monitoring.objectNameCreationError",
                            new Object[] { objNameStr },
                            t);
        }
    }
    
    
    /**
     * Gets the maximum request processing time of the servlet being
     * monitored.
     *
     * @return Maximum request processing time
     */
    public long getMaxTimeMillis() {
        return getLongValue(queryStatistic(servletObjName, "maxTimeMillis"));
    }

    
    /**
     * Gets the minimum request processing time of the servlet being monitored.
     *
     * @return Minimum request processing time
     */
    public long getMinTimeMillis() {
        return getLongValue(queryStatistic(servletObjName, "minTimeMillis"));
    }


    /**
     * Gets the total execution time of the service method of the servlet being
     * monitored.
     *
     * @return Execution time of the servlet's service method
     */
    public long getProcessingTimeMillis() {
        return getLongValue(queryStatistic(servletObjName,
                                           "processingTimeMillis"));
    }

    
    /**
     * Gets the number of requests processed by the servlet being monitored.
     *
     * @return Number of processed requests
     */
    public int getRequestCount() {
        return getIntValue(queryStatistic(servletObjName, "requestCount"));
    }

    
    /** 
     * Gets the number of requests processed by the servlet being monitored
     * that resulted in errors.
     *
     * @return Error count
     */
    public int getErrorCount() {
        return getIntValue(queryStatistic(servletObjName, "errorCount"));
    }


    /*
     * Queries the MBeanServer for an attribute.
     *
     * @param on The ObjectName of the MBean being queried
     * @param attrName The name of the attribute whose value is to be
     *        returned
     *
     * @return The value corresponding to the given attribute name
     */
    private Object queryStatistic(ObjectName on, String attrName) {

        Object resultObj = null;
        try {
            resultObj = server.getAttribute(on, attrName);
        } catch (Throwable t) {
            MonitorUtil.log(Level.SEVERE,
                            "pwc.monitoring.queryError",
                            new Object[] { attrName, on },
                            t);
        }

        return resultObj;
    }

    
    /*
     * Constructs the WebModule component of the Servlet MBean ObjectName.
     *
     * @param vsId The id of the virtual server on which the webmodule has been
     *        deployed
     * @param contextPath The contextPath of the webmodule
     *
     * @return WebModule component of the Servlet MBean ObjectName
     */ 
    private String createTomcatWebModuleName(String vsId, String contextPath) {

        final String PREFIX = "//";
        String tcWebModuleName;

        if ((contextPath != null) && (!contextPath.equals(""))) {
            tcWebModuleName = PREFIX + vsId + contextPath;
        } else {
            tcWebModuleName = PREFIX + vsId + "/";
        }

        return tcWebModuleName;
    }

	
    private long getLongValue(Object resultObj) {

        long result = 0;

        if (resultObj != null) {
            Long countObj = (Long)resultObj;
            result = countObj.longValue();
        }

        return result;
    }
	

    private int getIntValue(Object resultObj) {

        int result = 0;

        if (resultObj != null) {
            Integer countObj = (Integer)resultObj;
            result = countObj.intValue();
        }

        return result;
    }


    /*
     * Restores this object's state from a stream.
     *
     * @param ois Stream from which to restore this object's state
     */
    private void readObject(ObjectInputStream ois)
            throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject();

        // Get an instance of the MBeanServer
        ArrayList servers = MBeanServerFactory.findMBeanServer(null);
        if (servers != null && !servers.isEmpty()) {
            server = (MBeanServer)servers.get(0);
        } else {
            server = MBeanServerFactory.createMBeanServer();
        }
    }
}
