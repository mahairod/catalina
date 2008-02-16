/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package com.sun.enterprise.web.deploy;

import org.apache.catalina.deploy.MessageDestinationRef;
import com.sun.enterprise.deployment.MessageDestinationReferenceDescriptor;

import java.io.Serializable;


/**
 * Decorator of class <code>org.apache.catalina.deploy.MessageDestinationRef</code>
 *
 * @author Jean-Francois Arcand
 */

public final class MessageDestinationRefDecorator extends MessageDestinationRef {

    private MessageDestinationReferenceDescriptor decoree;
    
    public MessageDestinationRefDecorator(
                                MessageDestinationReferenceDescriptor decoree){
        this.decoree = decoree;
    }

    // ------------------------------------------------------------- Properties

    public String getDescription() {
        return decoree.getDescription();
    }

 
    public String getLink() {
        return decoree.getMessageDestinationLinkName();
    }

 
    public String getName() {
        return decoree.getName();
    }

    public String getType() {
        return decoree.getDestinationType();
    }

 
    public String getUsage() {
        return decoree.getUsage();
    }


}
