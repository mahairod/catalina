/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina;

import org.apache.catalina.core.StandardServer;


/**
 * <p><strong>ServerFactory</strong> allows the registration of the
 * (singleton) <code>Server</code> instance for this JVM, so that it
 * can be accessed independently of any existing reference to the
 * component hierarchy.  This is important for administration tools
 * that are built around the internal component implementation classes.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:20 $
 */

public class ServerFactory {


    // ------------------------------------------------------- Static Variables


    /**
     * The singleton <code>Server</code> instance for this JVM.
     */
    private static Server server = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Return the singleton <code>Server</code> instance for this JVM.
     */
    public static Server getServer() {
        if( server==null )
            server=new StandardServer();
        return (server);

    }


    /**
     * Set the singleton <code>Server</code> instance for this JVM.  This
     * method must <strong>only</strong> be called from a constructor of
     * the (singleton) <code>Server</code> instance that is created for
     * this execution of Catalina.
     *
     * @param theServer The new singleton instance
     */
    public static void setServer(Server theServer) {

        if (server == null)
            server = theServer;

    }


}
