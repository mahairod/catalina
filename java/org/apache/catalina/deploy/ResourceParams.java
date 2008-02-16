/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.deploy;

import java.util.Hashtable;
import java.io.Serializable;

/**
 * Representation of additional parameters which will be used to initialize
 * external resources defined in the web application deployment descriptor.
 *
 * @author Remy Maucherat
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:42 $
 */

public class ResourceParams implements Serializable {


    // ------------------------------------------------------------- Properties


    /**
     * The name of this resource parameters. Must be the name of the resource
     * in the java: namespace.
     */
    private String name = null;

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }

    private Hashtable resourceParams = new Hashtable();

    public void addParameter(String name, String value) {
        resourceParams.put(name, value);
    }

    public Hashtable getParameters() {
        return resourceParams;
    }

    // --------------------------------------------------------- Public Methods


    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ResourceParams[");
        sb.append("name=");
        sb.append(name);
        sb.append(", parameters=");
        sb.append(resourceParams.toString());
        sb.append("]");
        return (sb.toString());

    }


    // -------------------------------------------------------- Package Methods


    /**
     * The NamingResources with which we are associated (if any).
     */
    protected NamingResources resources = null;

    public NamingResources getNamingResources() {
        return (this.resources);
    }

    void setNamingResources(NamingResources resources) {
        this.resources = resources;
    }


}
