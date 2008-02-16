/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.authenticator;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.ArrayList;
import javax.servlet.http.Cookie;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Session;


/**
 * Object that saves the critical information from a request so that
 * form-based authentication can reproduce it once the user has been
 * authenticated.
 * <p>
 * <b>IMPLEMENTATION NOTE</b> - It is assumed that this object is accessed
 * only from the context of a single thread, so no synchronization around
 * internal collection classes is performed.
 * <p>
 * <b>FIXME</b> - Currently, this object has no mechanism to save or
 * restore the data content of the request, although it does save
 * request parameters so that a POST transaction can be faithfully
 * duplicated.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:28 $
 */

public final class SavedRequest {


    /**
     * The set of Cookies associated with this Request.
     */
    private ArrayList cookies = new ArrayList();

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public Iterator getCookies() {
        return (cookies.iterator());
    }


    /**
     * The set of Headers associated with this Request.  Each key is a header
     * name, while the value is a ArrayList containing one or more actual
     * values for this header.  The values are returned as an Iterator when
     * you ask for them.
     */
    private HashMap headers = new HashMap();

    public void addHeader(String name, String value) {
        ArrayList values = (ArrayList) headers.get(name);
        if (values == null) {
            values = new ArrayList();
            headers.put(name, values);
        }
        values.add(value);
    }

    public Iterator getHeaderNames() {
        return (headers.keySet().iterator());
    }

    public Iterator getHeaderValues(String name) {
        ArrayList values = (ArrayList) headers.get(name);
        if (values == null)
            return ((new ArrayList()).iterator());
        else
            return (values.iterator());
    }


    /**
     * The set of Locales associated with this Request.
     */
    private ArrayList locales = new ArrayList();

    public void addLocale(Locale locale) {
        locales.add(locale);
    }

    public Iterator getLocales() {
        return (locales.iterator());
    }


    /**
     * The request method used on this Request.
     */
    private String method = null;

    public String getMethod() {
        return (this.method);
    }

    public void setMethod(String method) {
        this.method = method;
    }



    /**
     * The set of request parameters associated with this Request.  Each
     * entry is keyed by the parameter name, pointing at a String array of
     * the corresponding values.
     */
    private HashMap parameters = new HashMap();

    public void addParameter(String name, String values[]) {
        parameters.put(name, values);
    }

    public Iterator getParameterNames() {
        return (parameters.keySet().iterator());
    }

    public String[] getParameterValues(String name) {
        return ((String[]) parameters.get(name));
    }


    /**
     * The query string associated with this Request.
     */
    private String queryString = null;

    public String getQueryString() {
        return (this.queryString);
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }


    /**
     * The request URI associated with this Request.
     */
    private String requestURI = null;

    public String getRequestURI() {
        return (this.requestURI);
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }


}
