/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.tomcat.util.net.jsse;

import java.net.Socket;
// START SJSAS 6439313
import javax.net.ssl.SSLEngine;
// END SJSAS 6439313
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.ServerSocketFactory;

/** 
 * Factory interface to construct components based on the JSSE version
 * in use.
 *
 * @author Bill Barker
 */

interface JSSEFactory {

    /**
     * Returns the ServerSocketFactory to use.
     */
    public ServerSocketFactory getSocketFactory();

    /**
     * returns the SSLSupport attached to this socket.
     */    
    public SSLSupport getSSLSupport(Socket socket);
    
    // START SJSAS 6439313
    /**
     * returns the SSLSupport attached to this SSLEngine.
     */
    public SSLSupport getSSLSupport(SSLEngine sslEngine);
    // END SJSAS 6439313
};
