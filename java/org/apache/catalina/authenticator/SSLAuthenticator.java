/*
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 *
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.authenticator;


import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.deploy.LoginConfig;
import org.glassfish.logging.annotation.LogMessageInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;


/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation of authentication
 * that utilizes SSL certificates to identify client users.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2007/04/17 21:33:22 $
 */

public class SSLAuthenticator
    extends AuthenticatorBase {


    @LogMessageInfo(
            message = "Looking up certificates",
            level = "INFO"
    )
    public static final String LOOK_UP_CERTIFICATE_INFO = "AS-WEB-CORE-00017";

    @LogMessageInfo(
            message = "No certificates included with this request",
            level = "INFO"
    )
    public static final String NO_CERTIFICATE_INCLUDED_INFO = "AS-WEB-CORE-00018";

    @LogMessageInfo(
            message = "No client certificate chain in this request",
            level = "WARNING"
    )
    public static final String NO_CLIENT_CERTIFICATE_CHAIN = "AS-WEB-CORE-00019";

    @LogMessageInfo(
            message = "Cannot authenticate with the provided credentials",
            level = "WARNING"
    )
    public static final String CANNOT_AUTHENTICATE_WITH_CREDENTIALS = "AS-WEB-CORE-00020";


    // ------------------------------------------------------------- Properties


    /**
     * Descriptive information about this implementation.
     */
    protected static final String info =
        "org.apache.catalina.authenticator.SSLAuthenticator/1.0";


    /**
     * Return descriptive information about this Valve implementation.
     */
    @Override
    public String getInfo() {

        return (this.info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Authenticate the user by checking for the existence of a certificate
     * chain, and optionally asking a trust manager to validate that we trust
     * this user.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param config Login configuration describing how authentication
     * should be performed
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public boolean authenticate(HttpRequest request,
                                HttpResponse response,
                                LoginConfig config)
        throws IOException {

        // Have we already authenticated someone?
        Principal principal =
            ((HttpServletRequest) request.getRequest()).getUserPrincipal();
        if (principal != null) {
            if (debug >= 1) {
                String msg = MessageFormat.format(rb.getString(SingleSignOn.PRINCIPAL_BEEN_AUTHENTICATED_INFO),
                                                  principal.getName());
                log(msg);
            }
            return (true);
        }

        // Retrieve the certificate chain for this client
        HttpServletResponse hres =
            (HttpServletResponse) response.getResponse();
        if (debug >= 1)
            log(rb.getString(LOOK_UP_CERTIFICATE_INFO));

        X509Certificate certs[] = (X509Certificate[])
            request.getRequest().getAttribute(Globals.CERTIFICATES_ATTR);
        if ((certs == null) || (certs.length < 1)) {
            certs = (X509Certificate[])
                request.getRequest().getAttribute(Globals.SSL_CERTIFICATE_ATTR);
        }
        if ((certs == null) || (certs.length < 1)) {
            if (debug >= 1)
                log(rb.getString(NO_CERTIFICATE_INCLUDED_INFO));
            // BEGIN S1AS 4878272
            hres.sendError(HttpServletResponse.SC_BAD_REQUEST);
            response.setDetailMessage(rb.getString(NO_CLIENT_CERTIFICATE_CHAIN));
            // END S1AS 4878272
            return (false);
        }

        // Authenticate the specified certificate chain
        principal = context.getRealm().authenticate(certs);
        if (principal == null) {
            if (debug >= 1)
                log("Realm.authenticate() returned false");
            // BEGIN S1AS 4878272
            hres.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            response.setDetailMessage(rb.getString(CANNOT_AUTHENTICATE_WITH_CREDENTIALS));
            // END S1AS 4878272
            return (false);
        }

        // Cache the principal (if requested) and record this authentication
        register(request, response, principal, Constants.CERT_METHOD,
                 null, null);
        String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
        if (ssoId != null) {
            getSession(request, true);
        }

        return (true);

    }

    @Override
    protected String getAuthMethod() {
        return HttpServletRequest.CLIENT_CERT_AUTH;
    }
}
