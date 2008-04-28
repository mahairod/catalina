/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package com.sun.enterprise.web.deploy;

import org.apache.catalina.deploy.SecurityCollection;
import com.sun.enterprise.deployment.web.WebResourceCollection;

import org.apache.catalina.util.RequestUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Decorator of class <code>org.apache.catalina.deploy.SecurityCollection</code>
 *
 * @author Jean-Francois Arcand
 */

public class SecurityCollectionDecorator extends SecurityCollection {

   private WebResourceCollection decoree;
   
   public SecurityCollectionDecorator(WebResourceCollection decoree){
        this.decoree = decoree;
        
        Enumeration enumeration = decoree.getUrlPatterns();
        while(enumeration.hasMoreElements()){
            addPattern( (String)enumeration.nextElement() );
        }
        
        enumeration = decoree.getHttpMethods();
        while(enumeration.hasMoreElements()){
            addMethod( (String)enumeration.nextElement() );
        }                
   }


    /**
     * Return the description of this web resource collection.
     */
    public String getDescription() {
        return decoree.getDescription();
    }



    /**
     * Return the name of this web resource collection.
     */
    public String getName() {
        return decoree.getName();
    }


    // --------------------------------------------------------- Public Methods

}