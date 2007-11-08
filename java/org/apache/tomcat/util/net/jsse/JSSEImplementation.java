/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.tomcat.util.net.jsse;

// START SJSAS 6439313
import javax.net.ssl.SSLEngine;
// END SJSAS 6439313
import org.apache.tomcat.util.compat.JdkCompat;
import org.apache.tomcat.util.net.SSLImplementation;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.ServerSocketFactory;
import java.io.*;
import java.net.*;

/* JSSEImplementation:

   Concrete implementation class for JSSE

   @author EKR
*/
        
public class JSSEImplementation extends SSLImplementation
{
    static final String JSSE14Factory = 
        "org.apache.tomcat.util.net.jsse.JSSE14Factory";
    static final String SSLSocketClass = "javax.net.ssl.SSLSocket";

    /* SJSAS 6439313
    static com.sun.org.apache.commons.logging.Log logger = 
        com.sun.org.apache.commons.logging.LogFactory.getLog(JSSEImplementation.class);
    */

    private JSSEFactory factory;

    public JSSEImplementation() throws ClassNotFoundException {
        // Check to see if JSSE is floating around somewhere
        Class.forName(SSLSocketClass);
        if( JdkCompat.isJava14() ) {
            try {
                Class factcl = Class.forName(JSSE14Factory);           
                factory = (JSSEFactory)factcl.newInstance();
            } catch(Exception ex) {
                /* SJSAS 6439313
                factory = new JSSE13Factory();
                 
                if(logger.isDebugEnabled()) {
                    logger.debug("Error getting factory: " + JSSE14Factory, ex);
                }*/
                // START SJSAS 6439313
                throw new RuntimeException(ex);
                // END SJSAS 6439313
            }
        } else {
            /* SJSAS 6439313
            factory = new JSSE13Factory();
             **/
            // START SJSAS 6439313
            throw new RuntimeException("JDK 1.3 not supported");
            // END SJSAS 6439313
        }
    }


    public String getImplementationName(){
      return "JSSE";
    }
      
    public ServerSocketFactory getServerSocketFactory()  {
        ServerSocketFactory ssf = factory.getSocketFactory();
        return ssf;
    } 

    public SSLSupport getSSLSupport(Socket s) {
        SSLSupport ssls = factory.getSSLSupport(s);
        return ssls;
    }

    // START SJSAS 6439313    
    public SSLSupport getSSLSupport(SSLEngine sslEngine) {
        return factory.getSSLSupport(sslEngine);
    }
    // END SJSAS 6439313    
}
