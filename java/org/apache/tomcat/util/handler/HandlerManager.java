/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.tomcat.util.handler;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.tomcat.util.IntrospectionUtils;

/** Hooks support. Hooks implement a chain-of-command pattern, and
 * are commonly used in most web servers as a mechanism of extensibility.
 *
 * The HandlerManager class will provide support for registering and maintaining
 * a list of modules implementing each hook.
 *
 * Each hook will have a name and an id. Name-based operations are slower, it is
 * recomended you use them only in non-critical code. The name-id association is
 * handled by NoteManager.
 *
 *
 *  @todo Merging. It is possible to have different hooks on different contexts
 *        ( server, context, servlet ), we need to merge them.
 */
public class HandlerManager {
    // hook name -> hook[]
    private Hashtable hookMap;
    private TcHandler hooks[][];

    public HandlerManager() {
    }

    /** Add a new handler.
     */
    public void addHandler( String name, TcHandler hook ) {
        
    }

    public TcHandler[] getHandlers( int hookId ) {
        return hooks[hookId];
    }

    public TcHandler[] getHandlers( String name ) {
        return getHandlers( 0 );
    }

    public Hashtable getHookMap() {
        return hookMap;
    }
}
