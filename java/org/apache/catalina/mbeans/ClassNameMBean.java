/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.catalina.mbeans;


import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;
import com.sun.org.apache.commons.modeler.BaseModelMBean;


/**
 * <p>A convenience base class for <strong>ModelMBean</strong> implementations
 * where the underlying base class (and therefore the set of supported
 * properties) is different for varying implementations of a standard
 * interface.  For Catalina, that includes at least the following:
 * Connector, Logger, Realm, and Valve.  This class creates an artificial
 * MBean attribute named <code>className</code>, which reports the fully
 * qualified class name of the managed object as its value.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2006/03/12 01:27:02 $
 */

public class ClassNameMBean extends BaseModelMBean {


     // ---------------------------------------------------------- Constructors


     /**
      * Construct a <code>ModelMBean</code> with default
      * <code>ModelMBeanInfo</code> information.
      *
      * @exception MBeanException if the initialize of an object
      *  throws an exception
      * @exception RuntimeOperationsException if an IllegalArgumentException
      *  occurs
      */
     public ClassNameMBean()
         throws MBeanException, RuntimeOperationsException {

         super();

     }


     // ------------------------------------------------------------ Properties


     /**
      * Return the fully qualified Java class name of the managed object
      * for this MBean.
      */
     public String getClassName() {

         return (this.resource.getClass().getName());

     }


 }
