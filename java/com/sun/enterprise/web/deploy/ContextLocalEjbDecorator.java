/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package com.sun.enterprise.web.deploy;

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import org.apache.catalina.deploy.ContextLocalEjb;


/**
 * Decorator of class <code>org.apache.catalina.deploy.ContextLocalEjb</code>
 *
 * @author Jean-Francois Arcand
 */

public class ContextLocalEjbDecorator extends ContextLocalEjb{
                                          
     
    private EjbReferenceDescriptor decoree;
    
    public ContextLocalEjbDecorator(EjbReferenceDescriptor decoree){
        this.decoree = decoree;
    }

    // ------------------------------------------------------------- Properties


    public String getDescription() {
        return decoree.getDescription();
    }


    public String getHome() {
        return decoree.getEjbHomeInterface();
    }

  
    public String getLink() {
        return decoree.getLinkName();
    }

 
    public String getLocal() {
        return decoree.getEjbInterface();
    }

 
    public String getName() {
        return decoree.getName();
    }


    public String getType() {
        return decoree.getType();
    }

 



}
