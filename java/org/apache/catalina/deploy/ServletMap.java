/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.deploy;

/**
 * Class representing a servlet mapping containing multiple URL patterns.
 * See Servlet 2.5, SRV.18.0.3 ("Multiple Occurrences of Servlet Mappings")
 * for details.
 */
public class ServletMap {
    String servletName;
    String[] urlPatterns = new String[0];
    
    public void setServletName(String name) {
        servletName = name;
    }

    public void addURLPattern(String pattern) {
        String[] results = new String[urlPatterns.length + 1];
        System.arraycopy(urlPatterns, 0, results, 0, urlPatterns.length);
        results[urlPatterns.length] = pattern;
        urlPatterns = results;
    }

    public String getServletName() {
        return servletName;
    }
        
    public String[] getUrlPatterns() {
        return urlPatterns;
    }
}
