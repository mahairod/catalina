/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.naming.resources.jndi;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.io.IOException;
import java.util.Hashtable;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.naming.resources.DirContextURLStreamHandler;

/**
 * Stream handler to a JNDI directory context.
 * 
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @version $Revision: 1.2 $
 */
public class Handler 
    extends DirContextURLStreamHandler {
    
    
    // ----------------------------------------------------------- Constructors
    
    
    public Handler() {
    }
    
    
}
