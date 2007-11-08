/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.tomcat.util.net;

import org.apache.tomcat.util.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 */
public class TcpConnection  { // implements Endpoint {
    /**
     * Maxium number of times to clear the socket input buffer.
     */
    static  int MAX_SHUTDOWN_TRIES=20;

    public TcpConnection() {
    }

    // -------------------- Properties --------------------

    PoolTcpEndpoint endpoint;
    Socket socket;

    public static void setMaxShutdownTries(int mst) {
	MAX_SHUTDOWN_TRIES = mst;
    }
    public void setEndpoint(PoolTcpEndpoint endpoint) {
	this.endpoint = endpoint;
    }

    public PoolTcpEndpoint getEndpoint() {
	return endpoint;
    }

    public void setSocket(Socket socket) {
	this.socket=socket;
    }

    public Socket getSocket() {
	return socket;
    }

    public void recycle() {
        endpoint = null;
        socket = null;
    }

    // Another frequent repetition
    public static int readLine(InputStream in, byte[] b, int off, int len)
	throws IOException
    {
	if (len <= 0) {
	    return 0;
	}
	int count = 0, c;

	while ((c = in.read()) != -1) {
	    b[off++] = (byte)c;
	    count++;
	    if (c == '\n' || count == len) {
		break;
	    }
	}
	return count > 0 ? count : -1;
    }

    
    // Usefull stuff - avoid having it replicated everywhere
    public static void shutdownInput(Socket socket)
	throws IOException
    {
	try {
	    InputStream is = socket.getInputStream();
	    int available = is.available ();
	    int count=0;
	    
	    // XXX on JDK 1.3 just socket.shutdownInput () which
	    // was added just to deal with such issues.
	    
	    // skip any unread (bogus) bytes
	    while (available > 0 && count++ < MAX_SHUTDOWN_TRIES) {
		is.skip (available);
		available = is.available();
	    }
	}catch(NullPointerException npe) {
	    // do nothing - we are just cleaning up, this is
	    // a workaround for Netscape \n\r in POST - it is supposed
	    // to be ignored
	}
    }
}


