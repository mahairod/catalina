/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.naming.factory;

import org.apache.naming.EjbRef;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Object factory for EJBs.
 * 
 * @author Jacek Laskowski
 * @author Remy Maucherat
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:29:07 $
 */
public class OpenEjbFactory implements ObjectFactory {


    // -------------------------------------------------------------- Constants


    protected static final String DEFAULT_OPENEJB_FACTORY = 
        "org.openejb.client.LocalInitialContextFactory";


    // -------------------------------------------------- ObjectFactory Methods


    /**
     * Crete a new EJB instance using OpenEJB.
     * 
     * @param obj The reference object describing the DataSource
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment)
        throws Exception {

        Object beanObj = null;

        if (obj instanceof EjbRef) {

            Reference ref = (Reference) obj;

            String factory = DEFAULT_OPENEJB_FACTORY;
            RefAddr factoryRefAddr = ref.get("openejb.factory");
            if (factoryRefAddr != null) {
                // Retrieving the OpenEJB factory
                factory = factoryRefAddr.getContent().toString();
            }

            Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, factory);

            RefAddr linkRefAddr = ref.get("openejb.link");
            if (linkRefAddr != null) {
                String ejbLink = linkRefAddr.getContent().toString();
                beanObj = (new InitialContext(env)).lookup(ejbLink);
            }

        }

        return beanObj;

    }


}
