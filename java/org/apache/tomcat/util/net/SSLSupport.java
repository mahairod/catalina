/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.tomcat.util.net;

import java.io.*;
import java.net.*;

/* SSLSupport

   Interface for SSL-specific functions

   @author EKR
*/

public interface SSLSupport {
    /**
     * The Request attribute key for the cipher suite.
     */
    public static final String CIPHER_SUITE_KEY = "javax.servlet.request.cipher_suite";

    /**
     * The Request attribute key for the key size.
     */
    public static final String KEY_SIZE_KEY = "javax.servlet.request.key_size";

    /**
     * The Request attribute key for the client certificate chain.
     */
    public static final String CERTIFICATE_KEY = "javax.servlet.request.X509Certificate";

    /**
     * The Request attribute key for the session id.
     * This one is a Tomcat extension to the Servlet spec.
     */
    public static final String SESSION_ID_KEY = "javax.servlet.request.ssl_session";

    /**
     * A mapping table to determine the number of effective bits in the key
     * when using a cipher suite containing the specified cipher name.  The
     * underlying data came from the TLS Specification (RFC 2246), Appendix C.
     */
     static final CipherData ciphers[] = {
        new CipherData("_WITH_NULL_", 0),
        new CipherData("_WITH_IDEA_CBC_", 128),
        new CipherData("_WITH_RC2_CBC_40_", 40),
        new CipherData("_WITH_RC4_40_", 40),
        new CipherData("_WITH_RC4_128_", 128),
        new CipherData("_WITH_DES40_CBC_", 40),
        new CipherData("_WITH_DES_CBC_", 56),
        new CipherData("_WITH_3DES_EDE_CBC_", 168)
    };

    /**
     * The cipher suite being used on this connection.
     */
    public String getCipherSuite() throws IOException;

    /**
     * The client certificate chain (if any).
     */
    public Object[] getPeerCertificateChain()
        throws IOException;

    /**
     * The client certificate chain (if any).
     * @param force If <code>true</code>, then re-negotiate the 
     *              connection if necessary.
     */
    public Object[] getPeerCertificateChain(boolean force)
        throws IOException;

    /**
     * Get the keysize.
     *
     * What we're supposed to put here is ill-defined by the
     * Servlet spec (S 4.7 again). There are at least 4 potential
     * values that might go here:
     *
     * (a) The size of the encryption key
     * (b) The size of the MAC key
     * (c) The size of the key-exchange key
     * (d) The size of the signature key used by the server
     *
     * Unfortunately, all of these values are nonsensical.
     **/
    public Integer getKeySize()
        throws IOException;

    /**
     * The current session Id.
     */
    public String getSessionId()
        throws IOException;
    /**
     * Simple data class that represents the cipher being used, along with the
     * corresponding effective key size.  The specified phrase must appear in the
     * name of the cipher suite to be recognized.
     */
    
    final class CipherData {
    
        public String phrase = null;
    
        public int keySize = 0;
    
        public CipherData(String phrase, int keySize) {
            this.phrase = phrase;
            this.keySize = keySize;
        }
    
    }
    
}

