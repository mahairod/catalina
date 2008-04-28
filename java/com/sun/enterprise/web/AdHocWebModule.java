/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web;

import org.apache.catalina.core.StandardWrapper;

/**
 *
 * @author Jan Luehe
 */
public class AdHocWebModule extends WebModule {

    /*
     * Constructor
     */
    public AdHocWebModule(WebContainer webcontainer) {
        super(webcontainer);
    }

    public boolean getConfigured() {
        return true;
    }

}
