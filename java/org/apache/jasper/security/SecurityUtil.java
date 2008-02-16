/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.jasper.security;

import org.apache.jasper.Constants;

/**
 * Util class for Security related operations.
 *
 * @author Jean-Francois Arcand
 */

public final class SecurityUtil{
    
    private static boolean packageDefinitionEnabled =  
         System.getProperty("package.definition") == null ? false : true;
    
    /**
     * Return the <code>SecurityManager</code> only if Security is enabled AND
     * package protection mechanism is enabled.
     */
    public static boolean isPackageProtectionEnabled(){
        if (packageDefinitionEnabled && Constants.IS_SECURITY_ENABLED){
            return true;
        }
        return false;
    }
    
    
}
