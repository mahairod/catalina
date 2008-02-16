/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.realm;


/**
 * Manifest constants for this Java package.
 *
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:52 $
 */

public final class Constants {

    public static final String Package = "org.apache.catalina.realm";
    
        // Authentication methods for login configuration
    public static final String FORM_METHOD = "FORM";

    // Form based authentication constants
    public static final String FORM_ACTION = "/j_security_check";

    // User data constraints for transport guarantee
    public static final String NONE_TRANSPORT = "NONE";
    public static final String INTEGRAL_TRANSPORT = "INTEGRAL";
    public static final String CONFIDENTIAL_TRANSPORT = "CONFIDENTIAL";

}
