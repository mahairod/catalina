/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.tomcat.util.net.jsse;

import java.net.Socket;
import javax.net.ssl.SSLSocket;
// START SJSAS 6439313
import javax.net.ssl.SSLEngine;
// END SJSAS 6439313
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.ServerSocketFactory;

/**
 * Implementation class for JSSEFactory for JSSE 1.1.x (that ships with the
 * 1.4 JVM).
 *
 * @author Bill Barker
 */
// START SJSAS 6240885
//class JSSE14Factory implements JSSEFactory {
public class JSSE14Factory implements JSSEFactory {
// END SJSAS 6240885

    // START SJSAS 6240885
    // 
    //JSSE14Factory() {
    public JSSE14Factory() {
    // END SJSAS 6240885
    }

    public ServerSocketFactory getSocketFactory() {
	return new JSSE14SocketFactory();
    }
    
    
    public SSLSupport getSSLSupport(Socket socket) {
        return new JSSE14Support((SSLSocket)socket);
    }

    // START SJSAS 6439313
    public SSLSupport getSSLSupport(SSLEngine sslEngine) {
        return new JSSE14Support(sslEngine);
    }
    // END SJSAS 6439313
}
