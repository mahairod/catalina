/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package com.sun.enterprise.web.deploy;


import org.apache.catalina.deploy.ContextResource;
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;

import java.io.Serializable;


/**
 * Decorator of class <code>org.apache.catalina.deploy.ContextResource</code>
 *
 * @author Jean-Francois Arcand
 */

public class ContextResourceDecorator extends ContextResource {

    private ResourceReferenceDescriptor decoree;
    
    public ContextResourceDecorator(ResourceReferenceDescriptor decoree){
        this.decoree = decoree;
    }

    // ------------------------------------------------------------- Properties


    public String getAuth() {
        return decoree.getAuthorization();
    }

    public String getDescription() {
        return decoree.getDescription();
    }

 
    public String getName() {
        return decoree.getName();
    }

    public String getScope() {
        return decoree.getSharingScope();
    }

 
    public String getType() {
        return decoree.getType();
    }

 
}
