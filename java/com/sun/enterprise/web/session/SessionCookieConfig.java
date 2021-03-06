/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web.session;

import java.net.URLEncoder;
import org.apache.catalina.Globals;


/**
 * Representation of the session cookie configuration element for a web 
 * application.
 *
 * This configuration is not specified as part of the standard deployment
 * descriptor but as part of the iAS 7.0's "extended" web application
 * deployment descriptor - ias-web.xml.
 */

public final class SessionCookieConfig {

    // ----------------------------------------------------- Manifest Constants

    /**
     * The default value for the session tracking cookie's comment.
     */
    public static final String SESSION_COOKIE_DEFAULT_COMMENT =
        URLEncoder.encode("Sun ONE Application Server Session Tracking Cookie");

    /**
     * The value that allows the JSESSIONID cookie's secure attribute to
     * be configured based on the connection i.e. secure if HTTPS.
     */
    public static final String DYNAMIC_SECURE = "dynamic";

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new SessionCookieConfig with default properties.
     */
    public SessionCookieConfig() {
        super();
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The name of the cookie used for session tracking.
     *
     * Default value is JSESSIONID
     */
    private String _name = Globals.SESSION_COOKIE_NAME;

    /**
     * The pathname that is set when the cookie is created.
     *
     * The default value is the context path at which the web application
     * is installed.  The browser will send the cookie if the pathname for the
     * request contains this pathname. If set to / (slash), the browser will
     * send the cookie to all URLs.
     */
    private String _path = null;

    /**
     * The expiration time in seconds after which the browser expires
     * the cookie.
     *
     * The default value is -1 (never expire).
     */
    private int _maxAge = -1;

    /**
     * The domain for which the cookie is valid.
     */
    private String _domain = null;

    /**
     * The comment that identifies the session tracking cookie in the
     * browser's cookie file. Applications may choose to provide a more
     * specific name for this cookie.
     */
    private String _comment = SESSION_COOKIE_DEFAULT_COMMENT;

    /**
     * When set to "dynamic", the cookie is marked as secure only if the
     * connection on which the request was received is secure. To override this
     * behaviour, the value of this property can be set to "true" or "false". 
     * If set to "true", user agents will use secure means to contact the
     * origin server when sending back the cookie regardless of whether the
     * connection on which the request was received is secure. If set to 
     * "false", user agents do not have to use secure means to contact the
     * origin server when sending back the cookie regardless of whether the
     * connection on which the request was received is secure.
     */
    private String _secure = DYNAMIC_SECURE;

    /**
     * Construct a new SessionCookieConfig with the specified properties.
     *
     * @param name    The name of the cookie used for session tracking
     * @param path    The pathname that is set when the cookie is created
     * @param maxAge  The expiration time (in seconds) of the session cookie
     *                (-1 indicates 'never expire')
     * @param domain  The domain for which the cookie is valid
     * @param comment The comment that identifies the session tracking cookie
     *                in the cookie file.
     */
    public SessionCookieConfig(String name, String path, int maxAge,
                               String domain, String comment) {
        super();
        setName(name);
        setPath(path);
        setMaxAge(maxAge);
        setDomain(domain);
        setComment(comment);
    }

    // ------------------------------------------------------------- Properties

    /**
     * Set the name of the session tracking cookie (currently not supported).
     */
    public void setName(String name) {
        _name = name;
    }

    /**
     * Return the name of the session tracking cookie.
     */
    public String getName() {
        return _name;
    }

    /**
     * Set the path to use when creating the session tracking cookie.
     */
    public void setPath(String path) {
        _path = path;
    }

    /**
     * Return the path that is set when the session tracking cookie is
     * created.
     */
    public String getPath() {
        return _path;
    }

    /**
     * Set the expiration time for the session cookie.
     */
    public void setMaxAge(int maxAge) {
        _maxAge = maxAge;
    }

    /**
     * Return the expiration time for the session cookie.
     */
    public int getMaxAge() {
        return _maxAge;
    }

    /**
     * Set the domain for which the cookie is valid.
     */
    public void setDomain(String domain) {
        _domain = domain;
    }

    /**
     * Return the domain for which the cookie is valid.
     */
    public String getDomain() {
        return _domain;
    }

    /**
     * Set the comment that identifies the session cookie.
     */
    public void setComment(String comment) {
        _comment = comment;
        if (comment != null)
            _comment = URLEncoder.encode(comment);
    }

    /**
     * Return the URLEncoded form of the comment that identifies the session
     * cookie.
     */
    public String getComment() {
        return _comment;
    }

    /**
     * Set whether the cookie is marked Secure or not.
     * @param secure Valid values are "dynamic", "true" or "false"
     */
    public void setSecure(String secure) throws IllegalArgumentException {
        if ((secure == null) || (!secure.equalsIgnoreCase("true") &&
                !secure.equalsIgnoreCase("false") &&
                !secure.equalsIgnoreCase(SessionCookieConfig.DYNAMIC_SECURE))) {
            throw new IllegalArgumentException();
        }
        _secure = secure;
    }

    /**
     * Return whether the cookie is to be marked Secure or not.
     * @returns "dynamic", "true" or "false"
     */
    public String getSecure() {
        return _secure;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Return a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("SessionCookieConfig[");
        sb.append("name=");
        sb.append(_name);
        if (_path != null) {
            sb.append(", path=");
            sb.append(_path);
        }
        sb.append(", maxAge=");
        sb.append(_maxAge);
        if (_domain != null) {
            sb.append(", domain=");
            sb.append(_domain);
        }
        if (_comment != null) {
            sb.append(", comment=");
            sb.append(_comment);
        }
        sb.append("]");
        return (sb.toString());

    }
}
