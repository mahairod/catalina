/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.naming.factory;

import java.util.Hashtable;
import java.sql.Driver;
import java.sql.DriverManager;
import javax.naming.Name;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.spi.ObjectFactory;
import org.apache.naming.ResourceLinkRef;


/**
 * <p>Object factory for resource links.</p>
 * 
 * @author Remy Maucherat
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:29:07 $
 */

public class ResourceLinkFactory
    implements ObjectFactory {


    // ----------------------------------------------------------- Constructors


    // ------------------------------------------------------- Static Variables


    /**
     * Global naming context.
     */
    private static Context globalContext = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Set the global context (note: can only be used once).
     * 
     * @param newGlobalContext new global context value
     */
    public static void setGlobalContext(Context newGlobalContext) {
        if (globalContext != null)
            return;
        globalContext = newGlobalContext;
    }


    // -------------------------------------------------- ObjectFactory Methods


    /**
     * Create a new DataSource instance.
     * 
     * @param obj The reference object describing the DataSource
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment)
        throws NamingException {
        
        if (!(obj instanceof ResourceLinkRef))
            return null;

        // Can we process this request?
        Reference ref = (Reference) obj;

        String type = ref.getClassName();

        // Read the global ref addr
        String globalName = null;
        RefAddr refAddr = ref.get(ResourceLinkRef.GLOBALNAME);
        if (refAddr != null) {
            globalName = refAddr.getContent().toString();
            Object result = null;
            result = globalContext.lookup(globalName);
            // FIXME: Check type
            return result;
        }

        return (null);

        
    }


}
