/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.connector;


import java.security.Principal;
import java.util.Locale;
import java.util.Enumeration;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.session.StandardSessionFacade;
import org.apache.catalina.util.StringManager;


/**
 * Facade class that wraps a Catalina-internal <b>HttpRequest</b>
 * object.  All methods are delegated to the wrapped request.
 *
 * @author Remy Maucherat
 * @version $Revision: 1.3 $ $Date: 2006/08/14 20:45:37 $
 */

public final class HttpRequestFacade
    extends RequestFacade
    implements HttpServletRequest {


    // ----------------------------------------------------- Constants


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a wrapper for the specified request.
     *
     * @param request The request to be wrapped
     */
    public HttpRequestFacade(HttpRequest request) {
        super(request);
    }


    // --------------------------------------------- HttpServletRequest Methods


    public String getAuthType() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getAuthType();
    }


    public Cookie[] getCookies() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getCookies();
    }


    public long getDateHeader(String name) {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getDateHeader(name);
    }


    public String getHeader(String name) {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getHeader(name);
    }


    public Enumeration getHeaders(String name) {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getHeaders(name);
    }


    public Enumeration getHeaderNames() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getHeaderNames();
    }


    public int getIntHeader(String name) {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getIntHeader(name);
    }


    public String getMethod() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getMethod();
    }


    public String getPathInfo() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getPathInfo();
    }


    public String getPathTranslated() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getPathTranslated();
    }


    public String getContextPath() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getContextPath();
    }


    public String getQueryString() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getQueryString();
    }


    public String getRemoteUser() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getRemoteUser();
    }


    public boolean isUserInRole(String role) {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).isUserInRole(role);
    }


    public java.security.Principal getUserPrincipal() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getUserPrincipal();
    }


    public String getRequestedSessionId() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getRequestedSessionId();
    }


    public String getRequestURI() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getRequestURI();
    }


    public StringBuffer getRequestURL() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getRequestURL();
    }


    public String getServletPath() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).getServletPath();
    }


    public HttpSession getSession(boolean create) {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        HttpSession session =
            ((HttpServletRequest) request).getSession(create);
        if (session == null)
            return null;
        else
            return new StandardSessionFacade(session);
    }


    public HttpSession getSession() {
        return getSession(true);
    }


    public boolean isRequestedSessionIdValid() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).isRequestedSessionIdValid();
    }


    public boolean isRequestedSessionIdFromCookie() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).isRequestedSessionIdFromCookie();
    }


    public boolean isRequestedSessionIdFromURL() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).isRequestedSessionIdFromURL();
    }


    public boolean isRequestedSessionIdFromUrl() {
        // Disallow operation if the object has gone out of scope
        if (request == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletRequest) request).isRequestedSessionIdFromURL();
    }


}
