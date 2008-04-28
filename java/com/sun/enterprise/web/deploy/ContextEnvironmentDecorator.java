/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package com.sun.enterprise.web.deploy;

import org.apache.catalina.deploy.ContextEnvironment;

import com.sun.enterprise.deployment.EnvironmentProperty;

import java.io.Serializable;


/**
 * Decorator of class <code>org.apache.catalina.deploy.ContextEnvironment</code>
 *
 * @author Jean-Francois Arcand
 */
public class ContextEnvironmentDecorator extends ContextEnvironment {

    private EnvironmentProperty decoree;
    
    public ContextEnvironmentDecorator(EnvironmentProperty decoree){
        this.decoree = decoree;
    }
      

    // ------------------------------------------------------------- Properties


    public String getDescription() {
        return decoree.getDescription();
    }


    public String getName() {
        return decoree.getName();
    }

  
    public String getType() {
        return decoree.getType();
    }

 
    public String getValue() {
        return decoree.getValue();
    }

 
}
