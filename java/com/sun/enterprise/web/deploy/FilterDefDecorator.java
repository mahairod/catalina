/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package com.sun.enterprise.web.deploy;

import com.sun.enterprise.deployment.web.ServletFilter;
import com.sun.enterprise.deployment.web.InitializationParameter;

import org.apache.catalina.deploy.FilterDef;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.io.Serializable;


/**
 * Decorator of class <code>org.apache.catalina.deploy.FilterDef</code>
 *
 * @author Jean-Francois Arcand
 */

public class FilterDefDecorator extends FilterDef{

    /**
     * The set of initialization parameters for this filter, keyed by
     * parameter name.
     */
    private Map parameters = null;
                                    
    private ServletFilter decoree;
    
    public FilterDefDecorator(ServletFilter decoree){
        this.decoree = decoree;
        Vector initParams = decoree.getInitializationParameters();
        InitializationParameter initParam; 
        for (int i=0; i < initParams.size(); i++){
           initParam = (InitializationParameter)initParams.get(i);
           addInitParameter( initParam.getName(),initParam.getValue() );              
        }  
    }



    // ------------------------------------------------------------- Properties


    public String getDescription() {
        return decoree.getDescription();
    }

    public String getDisplayName() {
        return decoree.getDisplayName();
    }
 
 
    public String getFilterClass() {
        return decoree.getClassName();
    }
  
    public String getFilterName() {
        return decoree.getName();
    }

    public String getLargeIcon() {
        return decoree.getLargeIconUri();
    }


    public String getSmallIcon() {
        return decoree.getSmallIconUri();
    }



   
}
