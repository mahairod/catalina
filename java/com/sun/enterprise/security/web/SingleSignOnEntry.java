/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.security.web;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Container;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Session;
import org.apache.catalina.SessionEvent;
import org.apache.catalina.SessionListener;
import org.apache.catalina.ValveContext;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.Realm;

/**
 * A private class representing entries in the cache of authenticated users.
 */
public class SingleSignOnEntry {

    public String id = null;

    public String authType = null;

    public String password = null;

    public Principal principal = null;

    public Session sessions[] = new Session[0];

    public String username = null;

    public String realmName = null;

    public long lastAccessTime;

    public SingleSignOnEntry(String id, Principal principal, String authType,
                             String username, String password,
                             String realmName) {
        super();
        this.id = id;
        this.principal = principal;
        this.authType = authType;
        this.username = username;
        this.password = password;
        this.realmName = realmName;
        this.lastAccessTime = System.currentTimeMillis();
    }

    /**
     * Gets the id of this SSO entry.
     */
    public String getId() {
        return id;
    }

    /**
     * Adds the given session to this SingleSignOnEntry if it does not
     * already exist.
     * 
     * @return true if the session was added, false otherwise
     */
    public synchronized boolean addSession(SingleSignOn sso, Session session) {
        for (int i = 0; i < sessions.length; i++) {
            if (session == sessions[i])
                return false;
        }
        Session results[] = new Session[sessions.length + 1];
        System.arraycopy(sessions, 0, results, 0, sessions.length);
        results[sessions.length] = session;
        sessions = results;
        session.addSessionListener(sso);

        return true;
    }

    public synchronized void removeSession(Session session) {
        Session[] nsessions = new Session[sessions.length - 1];
        for (int i = 0, j = 0; i < sessions.length; i++) {
            if (session == sessions[i])
                continue;
            nsessions[j++] = sessions[i];
        }
        sessions = nsessions;
    }

    public synchronized Session[] findSessions() {
        return (this.sessions);
    }

}
