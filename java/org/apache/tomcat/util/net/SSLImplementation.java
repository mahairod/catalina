/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.tomcat.util.net;

import java.net.Socket;
// START SJSAS 6439313
import javax.net.ssl.SSLEngine;
// END SJSAS 6439313

/* SSLImplementation:

   Abstract factory and base class for all SSL implementations.

   @author EKR
*/
abstract public class SSLImplementation {
    private static com.sun.org.apache.commons.logging.Log logger =
        com.sun.org.apache.commons.logging.LogFactory.getLog(SSLImplementation.class);

    // The default implementations in our search path
    private static final String JSSEImplementationClass=
	"org.apache.tomcat.util.net.jsse.JSSEImplementation";
    
    private static final String[] implementations=
    {        
        JSSEImplementationClass
    };

    public static SSLImplementation getInstance() throws ClassNotFoundException
    {
	for(int i=0;i<implementations.length;i++){
	    try {
               SSLImplementation impl=
		    getInstance(implementations[i]);
		return impl;
	    } catch (Exception e) {
		if(logger.isTraceEnabled()) 
		    logger.trace("Error creating " + implementations[i],e);
	    }
	}

	// If we can't instantiate any of these
	throw new ClassNotFoundException("Can't find any SSL implementation");
    }

    public static SSLImplementation getInstance(String className)
	throws ClassNotFoundException
    {
	if(className==null) return getInstance();

	try {
	    // Workaround for the J2SE 1.4.x classloading problem (under Solaris).
	    // Class.forName(..) fails without creating class using new.
	    // This is an ugly workaround. 
	    if( JSSEImplementationClass.equals(className) ) {
		return new org.apache.tomcat.util.net.jsse.JSSEImplementation();
	    }
	    Class clazz=Class.forName(className);
	    return (SSLImplementation)clazz.newInstance();
	} catch (Exception e){
	    if(logger.isDebugEnabled())
		logger.debug("Error loading SSL Implementation "
			     +className, e);
	    throw new ClassNotFoundException("Error loading SSL Implementation "
				      +className+ " :" +e.toString());
	}
    }

    abstract public String getImplementationName();
    abstract public ServerSocketFactory getServerSocketFactory();
    abstract public SSLSupport getSSLSupport(Socket sock);
    // START SJSAS 6439313
    public abstract SSLSupport getSSLSupport(SSLEngine sslEngine);
    // END SJSAS 6439313
}    
