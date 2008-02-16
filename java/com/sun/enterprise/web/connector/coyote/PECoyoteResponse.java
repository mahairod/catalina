/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web.connector.coyote;

import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import org.apache.coyote.tomcat5.CoyoteResponse;
import com.sun.enterprise.web.pwc.PwcWebModule;
import com.sun.enterprise.web.logging.pwc.LogDomains;

/**
 * Customized version of the Tomcat 5 CoyoteResponse
 */
public class PECoyoteResponse extends CoyoteResponse {

    private static final Logger logger = LogDomains.getLogger(LogDomains.PWC_LOGGER);

    
    /*
     * Constructor.
     */
    public PECoyoteResponse(boolean chunkingDisabled) {
	super(chunkingDisabled);
    }


    // START GlassFish 898
    /**
     * Gets the string representation of the given cookie.
     *
     * @param cookie The cookie whose string representation to get
     *
     * @return The cookie's string representation
     */
    /*protected String getCookieString(Cookie cookie) {

        PwcWebModule wm = (PwcWebModule) getContext();
        boolean encodeCookies = false;
        if (wm != null && wm.getEncodeCookies()) {
            encodeCookies = true;
        }

        return getCookieString(cookie, encodeCookies);
    }*/
    // END GlassFish 898
}
