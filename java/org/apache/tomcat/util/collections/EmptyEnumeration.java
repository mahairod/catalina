/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.tomcat.util.collections;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class EmptyEnumeration implements Enumeration {

    static EmptyEnumeration staticInstance=new EmptyEnumeration();

    public EmptyEnumeration() {
    }

    public static Enumeration getEmptyEnumeration() {
	return staticInstance;
    }
    
    public Object nextElement( ) {
	throw new NoSuchElementException( "EmptyEnumeration");
    }

    public boolean hasMoreElements() {
	return false;
    }
    
}
