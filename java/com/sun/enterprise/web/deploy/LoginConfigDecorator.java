/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package com.sun.enterprise.web.deploy;

import org.apache.catalina.deploy.LoginConfig;


import org.apache.catalina.util.RequestUtil;
import java.io.Serializable;

import com.sun.enterprise.deployment.web.LoginConfiguration;

/**
 * Decorator of class <code>org.apache.catalina.deploy.LoginConfig</code>
 *
 * @author Jean-Francois Arcand
 */

public class LoginConfigDecorator extends LoginConfig {


    // ----------------------------------------------------------- Constructors

    private LoginConfiguration decoree;
    
    private String errorPage;
    
    private String loginPage;
    
    public LoginConfigDecorator(LoginConfiguration decoree){
        this.decoree = decoree;
        
        String errorPage = RequestUtil.URLDecode(decoree.getFormErrorPage());
        if (!errorPage.startsWith("/")){
            errorPage = "/" + errorPage;
        }
        setErrorPage(errorPage);
        
        String loginPage = RequestUtil.URLDecode(decoree.getFormLoginPage());
        if (!loginPage.startsWith("/")){
            loginPage = "/" + loginPage;
        }     
        setLoginPage(loginPage);
        setAuthMethod(decoree.getAuthenticationMethod());
        setRealmName(decoree.getRealmName());
    }


}
