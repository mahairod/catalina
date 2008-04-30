/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web;

import java.util.Timer;

public class WebContainerTimer extends Timer {

    public WebContainerTimer() {
        this(false);
    }

    public WebContainerTimer(boolean isDeamon) {
        super("web-container-timer", isDeamon);
    }
}


