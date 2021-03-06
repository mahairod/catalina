/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.tomcat.util.net.jsse;

import java.io.*;
import java.net.*;
import java.util.Vector;
import java.security.KeyStore;
import java.security.Security;
import java.security.SecureRandom;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HandshakeCompletedEvent;

import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;

/*
  1. Make the JSSE's jars available, either as an installed
     extension (copy them into jre/lib/ext) or by adding
     them to the Tomcat classpath.
  2. keytool -genkey -alias tomcat -keyalg RSA
     Use "changeit" as password ( this is the default we use )
 */

/**
 * SSL server socket factory. It _requires_ a valid RSA key and
 * JSSE. 
 *
 * @author Harish Prabandham
 * @author Costin Manolache
 * @author Stefan Freyr Stefansson
 * @author EKR -- renamed to JSSESocketFactory
 */
public abstract class JSSESocketFactory
    extends org.apache.tomcat.util.net.ServerSocketFactory
{
    // defaults
    static String defaultProtocol = "TLS";
    static String defaultAlgorithm = "SunX509";
    static boolean defaultClientAuth = false;
    private static final String defaultKeyPass = "changeit";

    protected static final Log log = LogFactory.getLog(JSSESocketFactory.class);

    protected boolean initialized;
    protected boolean clientAuth = false;
    protected SSLServerSocketFactory sslProxy = null;
    protected String[] enabledCiphers;

    public JSSESocketFactory () {
    }
    

    public ServerSocket createSocket (int port)
        throws IOException
    {
        if (!initialized) init();
        ServerSocket socket = sslProxy.createServerSocket(port);
        initServerSocket(socket);
        return socket;
    }
    
    public ServerSocket createSocket (int port, int backlog)
        throws IOException
    {
        if (!initialized) init();
        ServerSocket socket = sslProxy.createServerSocket(port, backlog);
        initServerSocket(socket);
        return socket;
    }
    
    public ServerSocket createSocket (int port, int backlog,
                                      InetAddress ifAddress)
        throws IOException
    {   
        if (!initialized) init();
        ServerSocket socket = sslProxy.createServerSocket(port, backlog,
                                                          ifAddress);
        initServerSocket(socket);
        return socket;
    }
    
    public Socket acceptSocket(ServerSocket socket)
        throws IOException
    {
        SSLSocket asock = null;
        try {
             asock = (SSLSocket)socket.accept();
             asock.setNeedClientAuth(clientAuth);
        } catch (SSLException e){
          throw new SocketException("SSL handshake error" + e.toString());
        }
        return asock;
    }

    public void handshake(Socket sock) throws IOException {
        ((SSLSocket)sock).startHandshake();
    }

    /*
     * Determines the SSL cipher suites to be enabled.
     *
     * @param requestedCiphers Comma-separated list of requested ciphers
     * @param supportedCiphers Array of supported ciphers
     *
     * @return Array of SSL cipher suites to be enabled, or null if none of the
     * requested ciphers are supported
     */
    protected String[] getEnabledCiphers(String requestedCiphers,
                                         String[] supportedCiphers) {

        String[] enabledCiphers = null;

        if (requestedCiphers != null) {
            Vector vec = null;
            String cipher = requestedCiphers;
            int index = requestedCiphers.indexOf(',');
            if (index != -1) {
                int fromIndex = 0;
                while (index != -1) {
                    cipher = requestedCiphers.substring(fromIndex, index).trim();
                    if (cipher.length() > 0) {
                        /*
                         * Check to see if the requested cipher is among the
                         * supported ciphers, i.e., may be enabled
                         */
                        for (int i=0; supportedCiphers != null
                                     && i<supportedCiphers.length; i++) {
                            if (supportedCiphers[i].equals(cipher)) {
                                if (vec == null) {
                                    vec = new Vector();
                                }
                                vec.addElement(cipher);
                                break;
                            }
                        }
                    }
                    fromIndex = index+1;
                    index = requestedCiphers.indexOf(',', fromIndex);
                } // while
                cipher = requestedCiphers.substring(fromIndex);
            }

            if (cipher != null) {
                cipher = cipher.trim();
                if (cipher.length() > 0) {
                    /*
                     * Check to see if the requested cipher is among the
                     * supported ciphers, i.e., may be enabled
                     */
                    for (int i=0; supportedCiphers != null
                                 && i<supportedCiphers.length; i++) {
                        if (supportedCiphers[i].equals(cipher)) {
                            if (vec == null) {
                                vec = new Vector();
                            }
                            vec.addElement(cipher);
                            break;
                        }
                    }
                }
            }           

            if (vec != null) {
                enabledCiphers = new String[vec.size()];
                vec.copyInto(enabledCiphers);
            }
        }

        return enabledCiphers;
    }
     
    /*
     * Gets the SSL server's keystore password.
     */
    protected String getKeystorePassword() {
        String keyPass = (String)attributes.get("keypass");
        if (keyPass == null) {
            keyPass = defaultKeyPass;
        }
        String keystorePass = (String)attributes.get("keystorePass");
        if (keystorePass == null) {
            keystorePass = keyPass;
        }
        return keystorePass;
    }

    /*
     * Gets the SSL server's keystore.
     */
    protected KeyStore getKeystore(String pass)
            throws IOException {

        String keystoreFile = (String)attributes.get("keystore");
        if (log.isDebugEnabled()) {
            log.debug("Keystore file= " + keystoreFile);
        }

        String keystoreType = (String)attributes.get("keystoreType");
        if (log.isDebugEnabled()) {
            log.debug("Keystore type= " + keystoreType);
        }

        return getStore(keystoreType, keystoreFile, pass);
    }

    /*
     * Gets the SSL server's truststore.
     */
    protected KeyStore getTrustStore() throws IOException {

        String truststore = (String)attributes.get("truststore");
        if (log.isDebugEnabled()) {
            log.debug("Truststore file= " + truststore);
        }

        String truststoreType = (String)attributes.get("truststoreType");
        if (log.isDebugEnabled()) {
            log.debug("Truststore type= " + truststoreType);
        }

        String truststorePassword = System.getProperty(
                                    "javax.net.ssl.trustStorePassword");
        if (truststorePassword == null) {
            truststorePassword = getKeystorePassword();
        }

        return getStore(truststoreType, truststore, truststorePassword);
    }

    /*
     * Gets the key- or truststore with the specified type, path, and password.
     */
    private KeyStore getStore(String type, String path, String pass)
            throws IOException {

        KeyStore ks = null;
        InputStream istream = null;
        try {
            ks = KeyStore.getInstance(type);
            File keyStoreFile = new File(path);
            if (!keyStoreFile.isAbsolute()) {
                keyStoreFile = new File(System.getProperty("catalina.base"),
                                        path);
            }
            istream = new FileInputStream(keyStoreFile);

            ks.load(istream, pass.toCharArray());
            istream.close();
            istream = null;
        } catch (FileNotFoundException fnfe) {
            throw fnfe;
        } catch (IOException ioe) {
            throw ioe;      
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new IOException("Exception trying to load keystore " +
                                  path + ": " + ex.getMessage() );
        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (IOException ioe) {
                    // Do nothing
                }
            }
        }

        return ks;
    }

    /**
     * Reads the keystore and initializes the SSL socket factory.
     *
     * Place holder method to initialize the KeyStore, etc.
     */
    /* SJSAS 6439313
    abstract void init() throws IOException ;
     */
    // START SJSAS 6439313
    public abstract void init() throws IOException ;
    // END SJSAS 6439313

    /*
     * Determines the SSL protocol variants to be enabled.
     *
     * @param socket The socket to get supported list from.
     * @param requestedProtocols Comma-separated list of requested SSL
     * protocol variants
     *
     * @return Array of SSL protocol variants to be enabled, or null if none of
     * the requested protocol variants are supported
     */
    abstract protected String[] getEnabledProtocols(SSLServerSocket socket,
                                                    String requestedProtocols);

    /**
     * Set the SSL protocol variants to be enabled.
     * @param socket the SSLServerSocket.
     * @param protocols the protocols to use.
     */
    abstract protected void setEnabledProtocols(SSLServerSocket socket, 
                                            String [] protocols);

    /**
     * Configures the given SSL server socket with the requested cipher suites,
     * protocol versions, and need for client authentication
     */
    private void initServerSocket(ServerSocket ssocket) {

        SSLServerSocket socket = (SSLServerSocket) ssocket;

        if (attributes.get("ciphers") != null) {
            socket.setEnabledCipherSuites(enabledCiphers);
        }

        String requestedProtocols = (String) attributes.get("protocols");
        setEnabledProtocols(socket, getEnabledProtocols(socket, 
                                                         requestedProtocols));

        // we don't know if client auth is needed -
        // after parsing the request we may re-handshake
        socket.setNeedClientAuth(clientAuth);
    }

}
