/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 *
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




package org.apache.catalina;


import java.util.EventObject;


/**
 * General event for notifying listeners of significant changes on a Container.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2007/03/22 18:04:03 $
 */

public final class ContainerEvent extends EventObject {

    public static final String PRE_DESTROY = "predestroy";

    public static final String BEFORE_CONTEXT_INITIALIZED
        = "beforeContextInitialized";

    public static final String AFTER_CONTEXT_INITIALIZED
        = "afterContextInitialized";
    
    public static final String BEFORE_CONTEXT_DESTROYED
        = "beforeContextDestroyed";

    public static final String AFTER_CONTEXT_DESTROYED
        = "afterContextDestroyed";

    public static final String BEFORE_CONTEXT_ATTRIBUTE_ADDED
        = "beforeContextAttributeAdded";

    public static final String AFTER_CONTEXT_ATTRIBUTE_ADDED
        = "afterContextAttributeAdded";

    public static final String BEFORE_CONTEXT_ATTRIBUTE_REMOVED
        = "beforeContextAttributeRemoved";

    public static final String AFTER_CONTEXT_ATTRIBUTE_REMOVED
        = "afterContextAttributeRemoved";

    public static final String BEFORE_CONTEXT_ATTRIBUTE_REPLACED
        = "beforeContextAttributeReplaced";

    public static final String AFTER_CONTEXT_ATTRIBUTE_REPLACED
        = "afterContextAttributeReplaced";

    public static final String BEFORE_REQUEST_INITIALIZED
        = "beforeRequestInitialized";

    public static final String AFTER_REQUEST_INITIALIZED
        = "afterRequestInitialized";

    public static final String BEFORE_REQUEST_DESTROYED
        = "beforeRequestDestroyed";

    public static final String AFTER_REQUEST_DESTROYED
        = "afterRequestDestroyed";

    public static final String BEFORE_SESSION_CREATED
        = "beforeSessionCreated";

    public static final String AFTER_SESSION_CREATED
        = "afterSessionCreated";

    public static final String BEFORE_SESSION_DESTROYED
        = "beforeSessionDestroyed";

    public static final String AFTER_SESSION_DESTROYED
        = "afterSessionDestroyed";

    public static final String BEFORE_SESSION_ATTRIBUTE_ADDED
        = "beforeSessionAttributeAdded";

    public static final String AFTER_SESSION_ATTRIBUTE_ADDED
        = "afterSessionAttributeAdded";

    public static final String BEFORE_SESSION_ATTRIBUTE_REMOVED
        = "beforeSessionAttributeRemoved";

    public static final String AFTER_SESSION_ATTRIBUTE_REMOVED
        = "afterSessionAttributeRemoved";

    public static final String BEFORE_SESSION_ATTRIBUTE_REPLACED
        = "beforeSessionAttributeReplaced";

    public static final String AFTER_SESSION_ATTRIBUTE_REPLACED
        = "afterSessionAttributeReplaced";

    public static final String BEFORE_SESSION_VALUE_UNBOUND
        = "beforeSessionValueUnbound";

    public static final String AFTER_SESSION_VALUE_UNBOUND
        = "afterSessionValueUnbound";

    public static final String BEFORE_FILTER_INITIALIZED
        = "beforeFilterInitialized";

    public static final String AFTER_FILTER_INITIALIZED
        = "afterFilterInitialized";

    public static final String BEFORE_FILTER_DESTROYED
        = "beforeFilterDestroyed";

    public static final String AFTER_FILTER_DESTROYED
        = "afterFilterDestroyed";


    /**
     * The Container on which this event occurred.
     */
    private Container container = null;


    /**
     * The event data associated with this event.
     */
    private Object data = null;


    /**
     * The event type this instance represents.
     */
    private String type = null;


    /**
     * Construct a new ContainerEvent with the specified parameters.
     *
     * @param container Container on which this event occurred
     * @param type Event type
     * @param data Event data
     */
    public ContainerEvent(Container container, String type, Object data) {

        super(container);
        this.container = container;
        this.type = type;
        this.data = data;

    }


    /**
     * Return the event data of this event.
     */
    public Object getData() {

        return (this.data);

    }


    /**
     * Return the Container on which this event occurred.
     */
    public Container getContainer() {

        return (this.container);

    }


    /**
     * Return the event type of this event.
     */
    public String getType() {

        return (this.type);

    }


    /**
     * Return a string representation of this event.
     */
    public String toString() {

        return ("ContainerEvent['" + getContainer() + "','" +
                getType() + "','" + getData() + "']");

    }


}
