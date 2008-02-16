/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.naming;

/**
 * Naming MBean interface.
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 1.2 $
 */

public interface NamingServiceMBean {
    
    
    // -------------------------------------------------------------- Constants
    
    
    /**
     * Status constants.
     */
    public static final String[] states = 
    {"Stopped", "Stopping", "Starting", "Started"};
    
    
    public static final int STOPPED  = 0;
    public static final int STOPPING = 1;
    public static final int STARTING = 2;
    public static final int STARTED  = 3;
    
    
    /**
     * Component name.
     */
    public static final String NAME = "Apache JNDI Naming Service";
    
    
    /**
     * Object name.
     */
    public static final String OBJECT_NAME = ":service=Naming";
    
    
    // ------------------------------------------------------ Interface Methods
    
    
    /**
     * Retruns the JNDI component name.
     */
    public String getName();
    
    
    /**
     * Returns the state.
     */
    public int getState();
    
    
    /**
     * Returns a String representation of the state.
     */
    public String getStateString();
    
    
    /**
     * Start the servlet container.
     */
    public void start()
        throws Exception;
    
    
    /**
     * Stop the servlet container.
     */
    public void stop();
    
    
    /**
     * Destroy servlet container (if any is running).
     */
    public void destroy();
    
    
}
