/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.deploy;

import java.io.Serializable;


/**
 * <p>Representation of a message destination reference for a web application,
 * as represented in a <code>&lt;message-destination-ref&gt;</code> element
 * in the deployment descriptor.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:41 $
 * @since Tomcat 5.0
 */

public class MessageDestinationRef implements Serializable {


    // ------------------------------------------------------------- Properties


    /**
     * The description of this destination ref.
     */
    private String description = null;

    public String getDescription() {
        return (this.description);
    }

    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * The link of this destination ref.
     */
    private String link = null;

    public String getLink() {
        return (this.link);
    }

    public void setLink(String link) {
        this.link = link;
    }


    /**
     * The name of this destination ref.
     */
    private String name = null;

    public String getName() {
        return (this.name);
    }

    public void setName(String name) {
        this.name = name;
    }


    /**
     * The type of this destination ref.
     */
    private String type = null;

    public String getType() {
        return (this.type);
    }

    public void setType(String type) {
        this.type = type;
    }


    /**
     * The usage of this destination ref.
     */
    private String usage = null;

    public String getUsage() {
        return (this.usage);
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("MessageDestination[");
        sb.append("name=");
        sb.append(name);
        if (link != null) {
            sb.append(", link=");
            sb.append(link);
        }
        if (type != null) {
            sb.append(", type=");
            sb.append(type);
        }
        if (usage != null) {
            sb.append(", usage=");
            sb.append(usage);
        }
        if (description != null) {
            sb.append(", description=");
            sb.append(description);
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
