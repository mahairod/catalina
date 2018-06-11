/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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


import java.io.IOException;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.deploy.LoginConfig;

import org.apache.catalina.realm.GenericPrincipal;


/**
 * An <b>Authenticator</b> and <b>Valve</b> implementation that checks
 * only security constraints not involving user authentication.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2007/03/05 20:42:26 $
 */

public final class NonLoginAuthenticator
    extends AuthenticatorBase {


    // ----------------------------------------------------- Instance Variables

    //START SJSAS 6202703
    /**
     * Principal name of nonlogin principal.
     */
    private static final String NONLOGIN_PRINCIPAL_NAME = "nonlogin-principal";
    
    /**
     * A dummy principal that is set to request in authenticate method.
     */
    private final GenericPrincipal NONLOGIN_PRINCIPAL =
        new GenericPrincipal(NONLOGIN_PRINCIPAL_NAME, (String) null, 
                            (java.util.List) null);
    //END SJSAS 6202703
    
    /**
     * Descriptive information about this implementation.
     */
    private static final String info =
        "org.apache.catalina.authenticator.NonLoginAuthenticator/1.0";


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (this.info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Authenticate the user making this request, based on the specified
     * login configuration.  Return <code>true</code> if any specified
     * constraint has been satisfied, or <code>false</code> if we have
     * created a response challenge already.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param login Login configuration describing how authentication
     *              should be performed
     *
     * @exception IOException if an input/output error occurs
     */
    public boolean authenticate(HttpRequest request,
                                HttpResponse response,
                                LoginConfig config)
        throws IOException {

        if (debug >= 1)
            log("User authentication is not required");
        
        //START SJSAS 6202703
        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        if (hreq.getUserPrincipal() == null) {
            request.setUserPrincipal(NONLOGIN_PRINCIPAL);
        }
        //END SJSAS 6202703
        return (true);


    }


}
