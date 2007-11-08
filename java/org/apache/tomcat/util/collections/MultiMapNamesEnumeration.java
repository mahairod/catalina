/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.tomcat.util.collections;

import org.apache.tomcat.util.buf.MessageBytes;

import java.io.*;
import java.util.*;
import java.text.*;

/** Enumerate the distinct header names.
    Each nextElement() is O(n) ( a comparation is
    done with all previous elements ).

    This is less frequesnt than add() -
    we want to keep add O(1).
*/
public final class MultiMapNamesEnumeration implements Enumeration {
    int pos;
    int size;
    String next;
    MultiMap headers;

    // toString and unique options are not implemented -
    // we allways to toString and unique.
    
    /** Create a new multi-map enumeration.
     * @param  headers the collection to enumerate 
     * @param  toString convert each name to string 
     * @param  unique return only unique names
     */
    MultiMapNamesEnumeration(MultiMap headers, boolean toString,
			     boolean unique) {
	this.headers=headers;
	pos=0;
	size = headers.size();
	findNext();
    }

    private void findNext() {
	next=null;
	for(  ; pos< size; pos++ ) {
	    next=headers.getName( pos ).toString();
	    for( int j=0; j<pos ; j++ ) {
		if( headers.getName( j ).equalsIgnoreCase( next )) {
		    // duplicate.
		    next=null;
		    break;
		}
	    }
	    if( next!=null ) {
		// it's not a duplicate
		break;
	    }
	}
	// next time findNext is called it will try the
	// next element
	pos++;
    }
    
    public boolean hasMoreElements() {
	return next!=null;
    }

    public Object nextElement() {
	String current=next;
	findNext();
	return current;
    }
}
