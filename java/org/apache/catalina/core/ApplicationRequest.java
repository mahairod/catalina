/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 *
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




package org.apache.catalina.core;


import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import javax.servlet.DispatcherType;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import org.apache.catalina.Globals;
import org.apache.catalina.Request;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.StringManager;


/**
 * Wrapper around a <code>javax.servlet.ServletRequest</code>
 * that transforms an application request object (which might be the original
 * one passed to a servlet, or might be based on the 2.3
 * <code>javax.servlet.ServletRequestWrapper</code> class)
 * back into an internal <code>org.apache.catalina.Request</code>.
 * <p>
 * <strong>WARNING</strong>:  Due to Java's lack of support for multiple
 * inheritance, all of the logic in <code>ApplicationRequest</code> is
 * duplicated in <code>ApplicationHttpRequest</code>.  Make sure that you
 * keep these two classes in synchronization when making changes!
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.5 $ $Date: 2006/11/02 17:40:12 $
 */

public class ApplicationRequest extends ServletRequestWrapper {


    // ------------------------------------------------------- Static Variables


    /**
     * The set of attribute names that are special for request dispatchers.
     */
    private static final HashSet specials = new HashSet(10);


    static {
        specials.add(Globals.INCLUDE_REQUEST_URI_ATTR);
        specials.add(Globals.INCLUDE_CONTEXT_PATH_ATTR);
        specials.add(Globals.INCLUDE_SERVLET_PATH_ATTR);
        specials.add(Globals.INCLUDE_PATH_INFO_ATTR);
        specials.add(Globals.INCLUDE_QUERY_STRING_ATTR);
        specials.add(Globals.FORWARD_REQUEST_URI_ATTR);
        specials.add(Globals.FORWARD_CONTEXT_PATH_ATTR);
        specials.add(Globals.FORWARD_SERVLET_PATH_ATTR);
        specials.add(Globals.FORWARD_PATH_INFO_ATTR);
        specials.add(Globals.FORWARD_QUERY_STRING_ATTR);
    }


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new wrapped request around the specified servlet request.
     *
     * @param request The servlet request being wrapped
     * @param isFowardDispatch true if this wrapper is being created for a 
     * RD.forward, false otherwise
     */
    public ApplicationRequest(ServletRequest request,
                              boolean isFowardDispatch) {
        super(request);
        setRequest(request);
        this.isForwardDispatch = isForwardDispatch;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The request attributes for this request.  This is initialized from the
     * wrapped request, but updates are allowed.
     */
    protected HashMap attributes = new HashMap();


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);


    /*
     * true if this is a wrapper for RD.forward, false if otherwise (i.e.,
     * this is a wrapper for RD.include)
     */
    private boolean isForwardDispatch = false;


    // ------------------------------------------------- ServletRequest Methods


    /**
     * Override the <code>getAttribute()</code> method of the wrapped request.
     *
     * @param name Name of the attribute to retrieve
     */
    public Object getAttribute(String name) {

        synchronized (attributes) {
            return (attributes.get(name));
        }

    }


    /**
     * Override the <code>getAttributeNames()</code> method of the wrapped
     * request.
     */
    public Enumeration getAttributeNames() {

        synchronized (attributes) {
            return (new Enumerator(attributes.keySet()));
        }

    }


    /**
     * Override the <code>removeAttribute()</code> method of the
     * wrapped request.
     *
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {

        synchronized (attributes) {
            attributes.remove(name);
            if (!isSpecial(name))
                getRequest().removeAttribute(name);
        }

    }


    /**
     * Override the <code>setAttribute()</code> method of the
     * wrapped request.
     *
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set
     */
    public void setAttribute(String name, Object value) {

        synchronized (attributes) {
            attributes.put(name, value);
            if (!isSpecial(name))
                getRequest().setAttribute(name, value);
        }

    }


    // ------------------------------------------ ServletRequestWrapper Methods


    /**
     * Set the request that we are wrapping.
     *
     * @param request The new wrapped request
     */
    public void setRequest(ServletRequest request) {

        super.setRequest(request);

        // Initialize the attributes for this request
        synchronized (attributes) {
            attributes.clear();
            Enumeration names = request.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                Object value = request.getAttribute(name);
                attributes.put(name, value);
            }
        }

    }


    public DispatcherType getDispatcherType() {
        DispatcherType dispatcher = (DispatcherType) getAttribute(
            ApplicationFilterFactory.DISPATCHER_TYPE_ATTR);
        if (dispatcher == null) {
            dispatcher = DispatcherType.REQUEST;
        }
        return dispatcher;
    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Is this attribute name one of the special ones that is added only for
     * included servlets?
     *
     * @param name Attribute name to be tested
     */
    protected static boolean isSpecial(String name) {

        return specials.contains(name);
    }

}
