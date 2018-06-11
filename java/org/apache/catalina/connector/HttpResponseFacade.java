/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.connector;


import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.util.StringManager;


/**
 * Facade class that wraps a Catalina-internal <b>HttpResponse</b>
 * object.  All methods are delegated to the wrapped response.
 *
 * @author Remy Maucherat
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2006/08/14 20:45:37 $
 */

public final class HttpResponseFacade
    extends ResponseFacade
    implements HttpServletResponse {


    // ----------------------------------------------------- Constants


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a wrapper for the specified response.
     *
     * @param response The response to be wrapped
     */
    public HttpResponseFacade(HttpResponse response) {
        super(response);
    }


    // -------------------------------------------- HttpServletResponse Methods


    public void addCookie(Cookie cookie) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            return;

        ((HttpServletResponse) response).addCookie(cookie);

    }


    public boolean containsHeader(String name) {
        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletResponse) response).containsHeader(name);
    }


    public String encodeURL(String url) {
        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletResponse) response).encodeURL(url);
    }


    public String encodeRedirectURL(String url) {
        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletResponse) response).encodeRedirectURL(url);
    }


    public String encodeUrl(String url) {
        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletResponse) response).encodeURL(url);
    }


    public String encodeRedirectUrl(String url) {
        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }
        return ((HttpServletResponse) response).encodeRedirectURL(url);
    }


    public void sendError(int sc, String msg)
        throws IOException {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        resp.setAppCommitted(true);

        ((HttpServletResponse) response).sendError(sc, msg);

    }


    public void sendError(int sc)
        throws IOException {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        resp.setAppCommitted(true);

        ((HttpServletResponse) response).sendError(sc);

    }


    public void sendRedirect(String location)
        throws IOException {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            throw new IllegalStateException
                (/*sm.getString("responseBase.reset.ise")*/);

        resp.setAppCommitted(true);

        ((HttpServletResponse) response).sendRedirect(location);

    }


    public void setDateHeader(String name, long date) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            return;

        ((HttpServletResponse) response).setDateHeader(name, date);

    }


    public void addDateHeader(String name, long date) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            return;

        ((HttpServletResponse) response).addDateHeader(name, date);

    }


    public void setHeader(String name, String value) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            return;

        ((HttpServletResponse) response).setHeader(name, value);

    }


    public void addHeader(String name, String value) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            return;

        ((HttpServletResponse) response).addHeader(name, value);

    }


    public void setIntHeader(String name, int value) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            return;

        ((HttpServletResponse) response).setIntHeader(name, value);

    }


    public void addIntHeader(String name, int value) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            return;

        ((HttpServletResponse) response).addIntHeader(name, value);

    }


    public void setStatus(int sc) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            return;

        ((HttpServletResponse) response).setStatus(sc);

    }


    public void setStatus(int sc, String s) {

        // Disallow operation if the object has gone out of scope
        if (response == null) {
            throw new IllegalStateException(
                sm.getString("object.invalidScope"));
        }

        if (isCommitted())
            return;

        ((HttpServletResponse) response).setStatus(sc, s);

    }


}
