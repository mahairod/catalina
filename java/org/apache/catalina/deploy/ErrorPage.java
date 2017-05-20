/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
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

package org.apache.catalina.deploy;


import org.apache.catalina.util.RequestUtil;

import java.io.Serializable;


/**
 * Representation of an error page element for a web application,
 * as represented in a <code>&lt;error-page&gt;</code> element in the
 * deployment descriptor.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2005/12/08 01:27:40 $
 */

public class ErrorPage implements Serializable {


    // ----------------------------------------------------- Instance Variables


    /**
     * The error (status) code for which this error page is active.
     */
    private int errorCode = 0;


    /**
     * The exception type for which this error page is active.
     */
    private String exceptionType = null;


    /**
     * The context-relative location to handle this error or exception.
     */
    private String location = null;


    // START SJSAS 6324911
    /**
     * The reason string to be displayed with the error (status) code
     */
    private String reason = null;
    // END SJSAS 6324911


    // ------------------------------------------------------------- Properties


    /**
     * Return the error code.
     */
    public int getErrorCode() {

        return (this.errorCode);

    }


    /**
     * Set the error code.
     *
     * @param errorCode The new error code
     */
    public void setErrorCode(int errorCode) {

        this.errorCode = errorCode;

    }


    /**
     * Set the error code (hack for default XmlMapper data type).
     *
     * @param errorCode The new error code
     */
    public void setErrorCode(String errorCode) {

        try {
            this.errorCode = Integer.parseInt(errorCode);
        } catch (Throwable t) {
            this.errorCode = 0;
        }

    }


    /**
     * Return the exception type.
     */
    public String getExceptionType() {

        return (this.exceptionType);

    }


    /**
     * Set the exception type.
     *
     * @param exceptionType The new exception type
     */
    public void setExceptionType(String exceptionType) {

        this.exceptionType = exceptionType;

    }


    /**
     * Return the location.
     */
    public String getLocation() {

        return (this.location);

    }


    /**
     * Set the location.
     *
     * @param location The new location
     */
    public void setLocation(String location) {

        //        if ((location == null) || !location.startsWith("/"))
        //            throw new IllegalArgumentException
        //                ("Error Page Location must start with a '/'");
        this.location = RequestUtil.urlDecode(location);

    }


    // START SJSAS 6324911
    /**
     * Gets the reason string that is associated with the error (status) code 
     * for which this error page is active.
     *
     * @return The reason string of this error page
     */
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason string to be associated with the error (status) code 
     * for which this error page is active.
     *
     * @param reason The reason string
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
    // END SJSAS 6324911


    // --------------------------------------------------------- Public Methods


    /**
     * Render a String representation of this object.
     */
    public String toString() {

        StringBuilder sb = new StringBuilder("ErrorPage[");
        if (exceptionType == null) {
            sb.append("errorCode=");
            sb.append(errorCode);
        } else {
            sb.append("exceptionType=");
            sb.append(exceptionType);
        }
        sb.append(", location=");
        sb.append(location);
        sb.append("]");
        return (sb.toString());

    }


}
