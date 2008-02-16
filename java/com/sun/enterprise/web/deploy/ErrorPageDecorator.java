/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package com.sun.enterprise.web.deploy;


import org.apache.catalina.deploy.ErrorPage;


import org.apache.catalina.util.RequestUtil;
import com.sun.enterprise.deployment.ErrorPageDescriptor;


/**
 * Decorator of class <code>org.apache.catalina.deploy.ErrorPage</code>
 *
 * @author Jean-Francois Arcand
 */

public class ErrorPageDecorator extends ErrorPage {

    private ErrorPageDescriptor decoree;
    
    private String location;
    
    public ErrorPageDecorator(ErrorPageDescriptor decoree){
        this.decoree = decoree;
        setErrorCode(decoree.getErrorCode());
         String  exceptionType = decoree.getExceptionType();
        if (exceptionType.equals("")){
            setExceptionType(null);
        } else {
            setExceptionType(exceptionType);
        }
        
        setLocation(RequestUtil.URLDecode(decoree.getLocation()));
    }
}
