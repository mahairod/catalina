/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web;

import java.util.Map;

/**
 * Specifies behavior that the requester of an ad hoc path must implement.
 * <p>
 * The web container will call back to these methods when it needs to create an
 * instance of the servlet to support the path.
 *
 * @author Tim Quinn
 */
public interface AdHocServletInfo {
    
    /**
     * Returns the class type of the servlet that should be created to process
     * requests.  Note that the class must represent a subclass of HttpServlet.
     *
     * @return The servlet class
     */
    public Class getServletClass();
    
    /**
     * Returns the name of the servlet that the container should assign when it
     * adds a servlet to a web module.
     *
     * @return The servlet name
     */
    public String getServletName();
    
    /**
     * Returns a Map containing name and value pairs to be used in preparing
     * the init params in the servlet's ServletConfig object.
     *
     * @return Map containing the servlet init parameters
     */
    public Map<String,String> getServletInitParams();
    
}
