/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web;

import java.util.Timer;

public class WebContainerTimer
	extends Timer
{
	public WebContainerTimer() {
		super("web-container-timer");
	}

	public WebContainerTimer(boolean isDeamon) {
		super(isDeamon);
	}

/*	public void cancel() {
		(new Throwable()).printStackTrace();
	}
*/
}


