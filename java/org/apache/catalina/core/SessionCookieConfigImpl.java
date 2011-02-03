/*
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 */

package org.apache.catalina.core;

import org.apache.catalina.util.StringManager;
import org.apache.catalina.Globals;

import javax.servlet.SessionCookieConfig;

/**
 * Class that may be used to configure various properties of cookies 
 * used for session tracking purposes.
 */
public class SessionCookieConfigImpl implements SessionCookieConfig {

    private static final StringManager sm =
        StringManager.getManager(Constants.Package);

    private String name = Globals.SESSION_COOKIE_NAME;
    private String domain;
    private String path;
    private String comment;
    private boolean httpOnly = true;
    private boolean secure;
    private StandardContext ctx;
    private int maxAge = -1;


    /**
     * Constructor
     */
    public SessionCookieConfigImpl(StandardContext ctx) {
        this.ctx = ctx;
    }


    /**
     * @param name the cookie name to use
     *
     * @throws IllegalStateException if the <tt>ServletContext</tt>
     * from which this <tt>SessionCookieConfig</tt> was acquired has
     * already been initialized
     */
    public void setName(String name) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("sessionCookieConfig.alreadyInitialized",
                             "name", ctx.getName()));
        }

        this.name = name;
        ctx.setSessionCookieName(name);
    }


    /**
     * @return the cookie name set via {@link #setName}, or
     * <tt>JSESSIONID</tt> if {@link #setName} was never called
     */
    public String getName() {
        return name;
    }


    /**
     * @param domain the cookie domain to use
     *
     * @throws IllegalStateException if the <tt>ServletContext</tt>
     * from which this <tt>SessionCookieConfig</tt> was acquired has
     * already been initialized
     */
    public void setDomain(String domain) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("sessionCookieConfig.alreadyInitialized",
                             "dnmain", ctx.getName()));
        }

        this.domain = domain;
    }


    /**
     * @return the cookie domain set via {@link #setDomain}, or
     * <tt>null</tt> if {@link #setDomain} was never called
     */
    public String getDomain() {
        return domain;
    }


    /**
     * @param path the cookie path to use
     *
     * @throws IllegalStateException if the <tt>ServletContext</tt>
     * from which this <tt>SessionCookieConfig</tt> was acquired has
     * already been initialized
     */
    public void setPath(String path) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("sessionCookieConfig.alreadyInitialized",
                             "path", ctx.getName()));
        }

        this.path = path;
    }


    /**
     * @return the cookie path set via {@link #setPath}, or the context
     * path of the <tt>ServletContext</tt> from which this 
     * <tt>SessionCookieConfig</tt> was acquired if {@link #setPath}
     * was never called
     */
    public String getPath() {
        return path;
    }


    /**
     * @param comment the cookie comment to use
     *
     * @throws IllegalStateException if the <tt>ServletContext</tt>
     * from which this <tt>SessionCookieConfig</tt> was acquired has
     * already been initialized
     */
    public void setComment(String comment) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("sessionCookieConfig.alreadyInitialized",
                             "comment", ctx.getName()));
        }

        this.comment = comment;
    }


    /**
     * @return the cookie comment set via {@link #setComment}, or
     * <tt>null</tt> if {@link #setComment} was never called
     */
    public String getComment() {
        return comment;
    }


    /**
     * @param httpOnly true if the session tracking cookies created
     * on behalf of the <tt>ServletContext</tt> from which this
     * <tt>SessionCookieConfig</tt> was acquired shall be marked as
     * <i>HttpOnly</i>, false otherwise
     *
     * @throws IllegalStateException if the <tt>ServletContext</tt>
     * from which this <tt>SessionCookieConfig</tt> was acquired has
     * already been initialized
     */
    public void setHttpOnly(boolean httpOnly) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("sessionCookieConfig.alreadyInitialized",
                             "httpOnly", ctx.getName()));
        }

        this.httpOnly = httpOnly;
    }


    /**
     * @return true if the session tracking cookies created on behalf of the
     * <tt>ServletContext</tt> from which this <tt>SessionCookieConfig</tt>
     * was acquired will be marked as <i>HttpOnly</i>, false otherwise
     */
    public boolean isHttpOnly() {
        return httpOnly;
    }


    /**
     * @param secure true if the session tracking cookies created on
     * behalf of the <tt>ServletContext</tt> from which this
     * <tt>SessionCookieConfig</tt> was acquired shall be marked as
     * <i>secure</i> even if the request that initiated the corresponding
     * session is using plain HTTP instead of HTTPS, and false if they
     * shall be marked as <i>secure</i> only if the request that initiated
     * the corresponding session was also secure
     *
     * @throws IllegalStateException if the <tt>ServletContext</tt>
     * from which this <tt>SessionCookieConfig</tt> was acquired has
     * already been initialized
     */
    public void setSecure(boolean secure) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("sessionCookieConfig.alreadyInitialized",
                             "secure", ctx.getName()));
        }

        this.secure = secure;
    }


    /**
     * @return true if the session tracking cookies created on behalf of the
     * <tt>ServletContext</tt> from which this <tt>SessionCookieConfig</tt>
     * was acquired will be marked as <i>secure</i> even if the request
     * that initiated the corresponding session is using plain HTTP
     * instead of HTTPS, and false if they will be marked as <i>secure</i>
     * only if the request that initiated the corresponding session was
     * also secure
     */
    public boolean isSecure() {
        return secure;
    }


    public void setMaxAge(int maxAge) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("sessionCookieConfig.alreadyInitialized",
                             "maxAge", ctx.getName()));
        }

        this.maxAge = maxAge;
    }


    public int getMaxAge() {
        return maxAge;
    }

}
