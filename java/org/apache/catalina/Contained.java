/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina;


/**
 * <p>Decoupling interface which specifies that an implementing class is
 * associated with at most one <strong>Container</strong> instance.</p>
 *
 * @author Craig R. McClanahan
 * @author Peter Donald
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:14 $
 */

public interface Contained {


    //-------------------------------------------------------------- Properties


    /**
     * Return the <code>Container</code> with which this instance is associated
     * (if any); otherwise return <code>null</code>.
     */
    public Container getContainer();


    /**
     * Set the <code>Container</code> with which this instance is associated.
     *
     * @param container The Container instance with which this instance is to
     *  be associated, or <code>null</code> to disassociate this instance
     *  from any Container
     */
    public void setContainer(Container container);


}
