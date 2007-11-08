/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.tomcat.util.net;

import java.io.*;
import java.net.*;

/**
 * Default server socket factory. Doesn't do much except give us
 * plain ol' server sockets.
 *
 * @author db@eng.sun.com
 * @author Harish Prabandham
 */

// Default implementation of server sockets.

//
// WARNING: Some of the APIs in this class are used by J2EE. 
// Please talk to harishp@eng.sun.com before making any changes.
//
class DefaultServerSocketFactory extends ServerSocketFactory {

    DefaultServerSocketFactory () {
        /* NOTHING */
    }

    public ServerSocket createSocket (int port)
    throws IOException {
        return  new ServerSocket (port);
    }

    public ServerSocket createSocket (int port, int backlog)
    throws IOException {
        return new ServerSocket (port, backlog);
    }

    public ServerSocket createSocket (int port, int backlog,
        InetAddress ifAddress)
    throws IOException {
        return new ServerSocket (port, backlog, ifAddress);
    }
 
    public Socket acceptSocket(ServerSocket socket)
 	throws IOException {
 	return socket.accept();
    }
 
    public void handshake(Socket sock)
 	throws IOException {
 	; // NOOP
    }
 	    
    
    // START SJSAS 6439313
    public void init() throws IOException{
        ;
    }
    // END SJSAS 6439313      
 }
