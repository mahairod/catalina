/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.enterprise.util.logging;

import java.util.logging.Level;


/**
 * Class IASLevel
 */
public class IASLevel extends Level
{

    /**
     * Constructor IASLevel
     *
     *
     * @param name
     * @param value
     */
    protected IASLevel(String name, int value) {
        super(name, value);
    }

    /**
     * Field
     */
    public static final IASLevel ALERT = new IASLevel("ALERT", 1100);

    /**
     * Field
     */
    public static final IASLevel FATAL = new IASLevel("FATAL", 1200);
}
