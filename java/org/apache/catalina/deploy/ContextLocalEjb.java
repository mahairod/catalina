/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.deploy;

import java.io.Serializable;


/**
 * Representation of a local EJB resource reference for a web application, as
 * represented in a <code>&lt;ejb-local-ref&gt;</code> element in the
 * deployment descriptor.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:39 $
 */

public class ContextLocalEjb implements Serializable {


    // ------------------------------------------------------------- Properties


    /**
     * The description of this EJB.
     */
    private String description = null;

    public String getDescription() {
        return (this.description);
    }

    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * The name of the EJB home implementation class.
     */
    private String home = null;

    public String getHome() {
        return (this.home);
    }

    public void setHome(String home) {
        this.home = home;
    }


    /**
     * The link to a J2EE EJB definition.
     */
    private String link = null;

    public String getLink() {
        return (this.link);
    }

    public void setLink(String link) {
        this.link = link;
    }


    /**
     * The name of the EJB local implementation class.
     */
    private String local = null;

    public String getLocal() {
        return (this.local);
    }

    public void setLocal(String local) {
        this.local = local;
    }


    /**
     * The name of this EJB.
     */
    private String name = null;

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * The name of the EJB bean implementation class.
     */
    private String type = null;

    public String getType() {
        return (this.type);
    }

    public void setType(String type) {
        this.type = type;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("ContextLocalEjb[");
        sb.append("name=");
        sb.append(name);
        if (description != null) {
            sb.append(", description=");
            sb.append(description);
        }
        if (type != null) {
            sb.append(", type=");
            sb.append(type);
        }
        if (home != null) {
            sb.append(", home=");
            sb.append(home);
        }
        if (link != null) {
            sb.append(", link=");
            sb.append(link);
        }
        if (local != null) {
            sb.append(", local=");
            sb.append(local);
        }
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
