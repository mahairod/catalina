/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina;


/**
 * Interface defining a listener for significant events related to a
 * specific servlet instance, rather than to the Wrapper component that
 * is managing that instance.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:17 $
 */

public interface InstanceListener {


    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event InstanceEvent that has occurred
     */
    public void instanceEvent(InstanceEvent event);


}
