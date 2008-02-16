/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web;

import java.util.Map;

/**
 * Implementation of AdHocServletInfo interface providing information 
 * specific to the ad-hoc servlet responsible for servicing HTTP requests
 * for EJB webservice endpoints.
 *
 * @author Jan Luehe
 */
public class EjbWebServiceServletInfo implements AdHocServletInfo {

    public static final String EJB_SERVLET_NAME = "EjbWebServiceServlet";

    /**
     * Returns the class type of the servlet that should be created to process
     * requests.  Note that the class must represent a subclass of HttpServlet.
     *
     * @return The servlet class
     */
    public Class getServletClass() {
        try {
            return Class.forName("com.sun.enterprise.webservice.EjbWebServiceServlet");
        } catch (ClassNotFoundException ex) {
        }
        return null;
    }
    
    /**
     * Returns the name of the servlet that the container should assign when it
     * adds a servlet to a web module.
     *
     * @return The servlet name
     */
    public String getServletName() {
        return EJB_SERVLET_NAME;
    }
    
    /**
     * Returns a Map containing name and value pairs to be used in preparing
     * the init params in the servlet's ServletConfig object.
     *
     * @return Map containing the servlet init parameters
     */
    public Map getServletInitParams() {
        return null;
    }

}
