/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.tomcat.util.collections;

import org.apache.tomcat.util.buf.MessageBytes;

import java.io.*;
import java.util.*;
import java.text.*;

/** Enumerate the values for a (possibly ) multiple
 *    value element.
 */
class MultiMapValuesEnumeration implements Enumeration {
    int pos;
    int size;
    MessageBytes next;
    MultiMap headers;
    String name;

    MultiMapValuesEnumeration(MultiMap headers, String name,
			      boolean toString) {
        this.name=name;
	this.headers=headers;
	pos=0;
	size = headers.size();
	findNext();
    }

    private void findNext() {
	next=null;
	for( ; pos< size; pos++ ) {
	    MessageBytes n1=headers.getName( pos );
	    if( n1.equalsIgnoreCase( name )) {
		next=headers.getValue( pos );
		break;
	    }
	}
	pos++;
    }
    
    public boolean hasMoreElements() {
	return next!=null;
    }

    public Object nextElement() {
	MessageBytes current=next;
	findNext();
	return current.toString();
    }
}
