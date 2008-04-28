/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.appserv.web.cache.mapping;

import java.text.MessageFormat;

import java.util.logging.Logger;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;

import com.sun.enterprise.web.logging.pwc.LogDomains;

public class Field {

    // PWC_LOGGER
    private static Logger _logger;
    /**
     * The resource bundle containing the localized message strings.
     */
    private static ResourceBundle _rb = null;

    // field name and scope 
    protected String name; 

    // scope defs in Constants
    protected int scope; 

    /**
     * create a new cache field, given a string representation of the scope
     * @param name name of this field
     * @param scope scope of this field
     */
    public Field (String name, String scope) throws IllegalArgumentException {
        // web container logger
        _logger = LogDomains.getLogger(LogDomains.PWC_LOGGER);
        _rb = _logger.getResourceBundle();

        this.name = name;
        this.scope = parseScope(scope);
    }

    /**
     * set the associated name
     * @param name name of this field
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * set the associated scope
     * @param scope scope of this field
     */
    public void setScope(int scope) {
        this.scope = scope;
    }

    /**
     * set the associated scope
     * @param scope scope of this field
     */
    private int parseScope(String value) throws IllegalArgumentException {
        int scope;
        if ("context.attribute".equals(value))
            scope = Constants.SCOPE_CONTEXT_ATTRIBUTE;
        else if ("request.header".equals(value))
            scope = Constants.SCOPE_REQUEST_HEADER;
        else if ("request.parameter".equals(value))
            scope = Constants.SCOPE_REQUEST_PARAMETER;
        else if ("request.cookie".equals(value))
            scope = Constants.SCOPE_REQUEST_COOKIE;
        else if ("request.attribute".equals(value))
            scope = Constants.SCOPE_REQUEST_ATTRIBUTE;
        else if ("session.attribute".equals(value))
            scope = Constants.SCOPE_SESSION_ATTRIBUTE;
        else if ("session.id".equals(value))
            scope = Constants.SCOPE_SESSION_ID;
        else  {
            String msg = _rb.getString("cache.mapping.incorrectScope");
            Object[] params = { value, name };
            msg = MessageFormat.format(msg, params);

            throw new IllegalArgumentException(msg);
        }
        return scope;
    }

    /**
     * get the associated name
     * @return the name of this field
     */
    public String getName() {
        return name;
    }

    /**
     * get the associated scope
     * @return the scope of this field
     */
    public int getScope() {
        return scope;
    }

    /** get the field value by looking up in the given scope
     *  @param context <code>ServletContext</code> underlying web app context
     *  @param request <code>HttpServletRequest</code>
     *  @return field value in the scope
     */
    public Object getValue(ServletContext context, 
                           HttpServletRequest request) {

        Object value = null;
        switch (scope) {
            case Constants.SCOPE_CONTEXT_ATTRIBUTE:
                    value = context.getAttribute(name);
                    break;
            case Constants.SCOPE_REQUEST_HEADER:
                    value = request.getHeader(name);
                    break;
            case Constants.SCOPE_REQUEST_PARAMETER:
                    value = request.getParameter(name);
                    break;
            case Constants.SCOPE_REQUEST_COOKIE:
                    Cookie cookies[] = request.getCookies();
                    for (int i = 0; i < cookies.length; i++) {
                        if (name.equals(cookies[i].getName())) {
                            value = cookies[i].getValue();
                            break;
                        }
                    }
                    break;
            case Constants.SCOPE_REQUEST_ATTRIBUTE:
                    value = request.getAttribute(name);
                    break;
            case Constants.SCOPE_SESSION_ID:
                    {
                        HttpSession session = request.getSession(false);
                        if (session != null) {
                            value = session.getId();
                        }
                    }
                    break;
            case Constants.SCOPE_SESSION_ATTRIBUTE:
                    {
                        HttpSession session = request.getSession(false);
                        if (session != null) {
                            value = session.getAttribute(name);
                        }
                    }
                    break;
        }
        return value;
    }
}
