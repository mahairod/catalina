/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */

package com.sun.appserv.server;

/**
 * Exception thrown by application server lifecycle modules and subsystems. These exceptions
 * are generally considered fatal to the operation of application server.
 */
public final class ServerLifecycleException extends Exception {

    /**
     * Construct a new LifecycleException with no other information.
     */
    public ServerLifecycleException() {
        super();
    }

    /**
     * Construct a new LifecycleException for the specified message.
     *
     * @param message Message describing this exception
     */
    public ServerLifecycleException(String message) {
        super(message);
    }

    /**
     * Construct a new LifecycleException for the specified throwable.
     *
     * @param throwable Throwable that caused this exception
     */
    public ServerLifecycleException(Throwable rootCause) {
        super(rootCause);
    }

    /**
     * Construct a new LifecycleException for the specified message
     * and throwable.
     *
     * @param message Message describing this exception
     * @param rootCause Throwable that caused this exception
     */
    public ServerLifecycleException(String message, Throwable rootCause) {
        super(message, rootCause);
    }
}
