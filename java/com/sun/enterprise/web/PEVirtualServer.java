/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web;


/**
 * Standard implementation of a virtual server (aka virtual host) in
 * the iPlanet Application Server.
 */

public final class PEVirtualServer extends VirtualServer {

    // ------------------------------------------------------------ Constructor

    /**
     * Default constructor that simply gets a handle to the web container 
     * subsystem's logger.
     */
    public PEVirtualServer() {
        super();      
    }
}
