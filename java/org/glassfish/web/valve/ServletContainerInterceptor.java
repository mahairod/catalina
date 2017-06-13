/*
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
 *
 */

package org.glassfish.web.valve;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.jvnet.hk2.annotations.Contract;

/**
 * Contract interface for registering ServletContainerInterceptor 
 * to the Web Container. It can be inherited by anyone who want to 
 * extend the Web Container.
 *
 * @author Jeremy_Lv
 *
 */
@Contract
public interface ServletContainerInterceptor {

	/**
	 * User can set some useful informations before 
	 * invoking the Servlet application
	 * @param req
	 * @param res
	 */
	public void preInvoke(Request req, Response res);
	
	/**
	 * User can remove some useful informations after 
	 * invoking the Servlet application
	 * @param req
	 * @param res
	 */
	public void postInvoke(Request req, Response res);
}
