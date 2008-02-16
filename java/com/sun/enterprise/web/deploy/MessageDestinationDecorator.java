/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package com.sun.enterprise.web.deploy;


import org.apache.catalina.deploy.MessageDestination;
import com.sun.enterprise.deployment.MessageDestinationDescriptor;


/**
 * Decorator of class <code>org.apache.catalina.deploy.MessageDestination</code>
 *
 * @author Jean-Francois Arcand
 */

public class MessageDestinationDecorator extends MessageDestination {

    private MessageDestinationDescriptor decoree;
    
    public MessageDestinationDecorator(MessageDestinationDescriptor decoree){
        this.decoree = decoree;
    }
    // ------------------------------------------------------------- Properties


    /**
     * The description of this destination.
     */
    private String description = null;

    public String getDescription() {
        return decoree.getDescription();
    }

 
    public String getDisplayName() {
        return decoree.getDisplayName();
    }

  
    public String getLargeIcon() {
        return decoree.getLargeIconUri();
    }

  
    public String getName() {
        return decoree.getName();
    }

    public String getSmallIcon() {
        return decoree.getSmallIconUri();
    }


}
