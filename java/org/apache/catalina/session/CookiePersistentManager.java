/*
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.session;

import org.apache.catalina.Session;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.util.HexUtils;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.servlet.http.*;

/**
 * Session manager for cookie-based persistence, where cookies carry session state.
 *
 * With cookie-based persistence, only session attribute values of type String are supported.
 */

public class CookiePersistentManager extends StandardManager {

    private final Set<String> sessionIds = new HashSet<String>();

    // The name of the cookies that carry session state
    private String cookieName;

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    @Override
    public void add(Session session) {
        synchronized (sessionIds) {
            if (!sessionIds.add(session.getIdInternal())) {
                throw new IllegalArgumentException("Session with id " + session.getIdInternal() +
                        " already present");
            }
            int size = sessionIds.size();
            if (size > maxActive) {
                maxActive = size;
            }
        }
    }

    @Override
    public Session findSession(String id, HttpServletRequest request) throws IOException {
        synchronized (sessionIds) {
            if (!sessionIds.contains(id)) {
                // Session was never created
                return null;
            }
        }
        if (cookieName == null) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        String value = null;
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return parseSession(cookie.getValue());
            }
        }
        return null;
    }

    @Override
    public void clearSessions() {
        synchronized (sessionIds) {
            sessionIds.clear();
        }
    }

    @Override
    public Session[] findSessions() {
        return null;
    }

    @Override
    public void remove(Session session) {
        synchronized (sessionIds) {
            sessionIds.remove(session.getIdInternal());
        }
    }

    @Override
    public Cookie toCookie(Session session) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        if (getContainer() != null) {
            oos = ((StandardContext) getContainer()).createObjectOutputStream(
                    new BufferedOutputStream(baos));
        } else {
            oos = new ObjectOutputStream(new BufferedOutputStream(baos));
        }
        oos.writeObject(session);
        oos.close();
        return new Cookie(cookieName, HexUtils.convert(baos.toByteArray()));
    }

    @Override
    public void checkSessionAttribute(String name, Object value) {
        if (!(value instanceof String)) {
            throw new IllegalArgumentException(
                    sm.getString("standardSession.setAttribute.nonStringValue", name));
        }
    }

    /*
     * Parses the given string into a session, and returns it.
     * *
     * The given string is supposed to contain the serialized representation of a session in Base64-encoded form.
     */
    private Session parseSession(String value) throws IOException {
        ObjectInputStream ois;
        BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(HexUtils.convert(value)));
        if (container != null) {
            ois = ((StandardContext)container).createObjectInputStream(bis);
        } else {
            ois = new ObjectInputStream(bis);
        }
        try {
            return (Session) ois.readObject();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
