/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package com.sun.enterprise.web.deploy;

import org.apache.catalina.deploy.SecurityCollection;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.SecurityRole;
import com.sun.enterprise.web.WebModule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
/**
 * Decorator of class <code>org.apache.catalina.deploy.SecurityConstraint</code>
 *
 * @author Jean-Francois Arcand
 */
public class SecurityConstraintDecorator 
                    extends org.apache.catalina.deploy.SecurityConstraint {

    private SecurityConstraint securityConstraint;
    
    public SecurityConstraintDecorator(SecurityConstraint securityConstraint,
                                       WebModule webModule){
        this.securityConstraint = securityConstraint;
        
        if (securityConstraint.getAuthorizationConstraint() != null){
            setAuthConstraint(true);
            Enumeration enumeration = securityConstraint
                            .getAuthorizationConstraint().getSecurityRoles();

            SecurityRole securityRole;
            while (enumeration.hasMoreElements()){
                securityRole = (SecurityRole)enumeration.nextElement();
                super.addAuthRole(securityRole.getName());
                if ( !securityRole.getName().equals("*")){
                    webModule.addSecurityRole(securityRole.getName());
                }
            }
            setDisplayName(securityConstraint.getAuthorizationConstraint().getName());
        }
 
        if (securityConstraint.getUserDataConstraint() != null){
            setUserConstraint(securityConstraint.getUserDataConstraint()
                                                    .getTransportGuarantee());
        }
        
    }

}



