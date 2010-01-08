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

package org.apache.catalina.authenticator;

import org.apache.catalina.Session;
import org.apache.catalina.session.StandardSession;

import java.security.Principal;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A private class representing entries in the cache of authenticated users.
 */
public class SingleSignOnEntry {

    private static final Logger log = Logger.getLogger(
        SingleSignOnEntry.class.getName());

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


    /**
     * Returns true if this SingleSignOnEntry does not have any sessions
     * associated with it, and false otherwise.
     *
     * @return true if this SingleSignOnEntry does not have any sessions
     * associated with it, and false otherwise
     */
    public synchronized boolean isEmpty() {
        return (sessions.length == 0);
    }


    /**
     * Expires all sessions associated with this SingleSignOnEntry
     *
     * @param reverse the reverse map from which to remove the sessions as
     * they are being expired
     */
    public synchronized void expireSessions(HashMap reverse) {
        for (int i = 0; i < sessions.length; i++) {
            if (log.isLoggable(Level.FINE)) {
                log.fine(" Invalidating session " + sessions[i]);
            }

            // Remove from reverse cache first to avoid recursion
            synchronized (reverse) {
                reverse.remove(sessions[i]);
            }
        
            //6406580 START
            /*
            // Invalidate this session
            sessions[i].expire();
             */
            // Invalidate this session
            // if it is not already invalid(ated)
            if( ((StandardSession)sessions[i]).getIsValid() ) {
                sessions[i].expire();
            }
            //6406580 END
        }
    }
}
