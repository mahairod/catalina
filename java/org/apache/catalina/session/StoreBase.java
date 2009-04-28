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




package org.apache.catalina.session;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.*;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.Store;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
//HERCULES:added
import org.apache.catalina.Session;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//HERCULES:end added

/**
 * Abstract implementation of the Store interface to
 * support most of the functionality required by a Store.
 *
 * @author Bip Thelin
 * @version $Revision: 1.8 $, $Date: 2007/05/05 05:32:19 $
 */

public abstract class StoreBase
    implements Lifecycle, Store {

    // ----------------------------------------------------- Instance Variables

    /**
     * The descriptive information about this implementation.
     */
    /* SJSAS
    protected static String info = "StoreBase/1.0";
    */
    // START SJSAS
    // If this variable were static, it would need to be protected from 
    // manipulation by malicious code by making it final. However, this
    // variable must not be final, because it is assigned a different value 
    // in some of the EE subclasses. Therefore, turning it into an instance
    // variable.
    protected String info = "StoreBase/1.0";
    // END SJSAS

    /**
     * Name to register for this Store, used for logging.
     */
    /* SJSAS 
    protected static String storeName = "StoreBase";
    */
    // START SJSAS
    // If this variable were static, it would need to be protected from 
    // manipulation by malicious code by making it final. However, this
    // variable must not be final, because it is assigned a different value 
    // in some of the EE subclasses. Therefore, turning it into an instance
    // variable.
    protected String storeName = "StoreBase";
    // END SJSAS

    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;

    /**
     * Has this component been started yet?
     */
    protected boolean started = false;

    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm = StringManager.getManager(
                                                    Constants.Package);

    /**
     * The Manager with which this JDBCStore is associated.
     */
    protected Manager manager;

    // ------------------------------------------------------------- Properties

    /**
     * Return the info for this Store.
     */
    public String getInfo() {
        return(info);
    }


    /**
     * Return the name for this Store, used for logging.
     */
    public String getStoreName() {
        return(storeName);
    }


    /**
     * Set the debugging detail level for this Store.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {
        this.debug = debug;
    }

    /**
     * Return the debugging detail level for this Store.
     */
    public int getDebug() {
        return(this.debug);
    }


    /**
     * Set the Manager with which this Store is associated.
     *
     * @param manager The newly associated Manager
     */
    public void setManager(Manager manager) {
        Manager oldManager = this.manager;
        this.manager = manager;
        support.firePropertyChange("manager", oldManager, this.manager);
    }

    /**
     * Return the Manager with which the Store is associated.
     */
    public Manager getManager() {
        return(this.manager);
    }


    // --------------------------------------------------------- Public Methods

    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }


    /**
     * Gets the (possibly empty) list of lifecycle listeners associated
     * with this Store.
     */
    public List<LifecycleListener> findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to add
     */
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }

    /**
     * Add a property change listener to this component.
     *
     * @param listener a value of type 'PropertyChangeListener'
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
        
    /**
    * Serialize a session into an output stream.  
    *
    * @param sess
    *   The session to be serialized
    *
    * @param oos
    *   The output stream the session should be written to
    * Hercules: added method
    */
    public void writeSession(Session sess, ObjectOutputStream oos)
        throws IOException {
      if ( sess == null )  {
        return;
      }
      
      oos.writeObject(sess);
    } 
    
    /**
    * Create a session object from an input stream.
    *
    * @param manager
    *   The manager that will own this session
    *
    * @param ois
    *   The input stream containing the serialized session
    *
    * @return 
    *   The resulting session object
    * Hercules: added method
    */
    public Session readSession(Manager manager, ObjectInputStream ois)
            throws ClassNotFoundException, IOException {

        StandardSession sess = StandardSession.deserialize(ois, manager);
        sess.setManager(manager);
      
        return sess;
    }
    
    /**
    * public wrapper for processExpires()
    * don't want to make processExpires() public
    * called from manager background thread
    *
    * Hercules: added method
    */    
    public void doProcessExpires() {
        this.processExpires();
    }
    
    /**
    * no-op method - sub classes will
    * implement to remove a session from the store
    * cache
    * Hercules: added method
    */    
    public void removeFromStoreCache(String id) {
        //do nothing
    }     
    

    // --------------------------------------------------------- Protected Methods

    /**
     * Called by our background reaper thread to check if Sessions
     * saved in our store are subject of being expired. If so expire
     * the Session and remove it from the Store.
     *
     */
    public void processExpires() {
        String[] keys = null;

        if(!started) {
            return;
        }

        try {
            keys = keys();
        } catch (IOException e) {
            log (e.toString());
            e.printStackTrace();
            return;
        }

        for (int i = 0; i < keys.length; i++) {
            try {
                StandardSession session = (StandardSession) load(keys[i]);
                if (session == null) {
                    continue;
                }
                if (session.isValid()) {
                    continue;
                }
                if ( ( (PersistentManagerBase) manager).isLoaded( keys[i] )) {
                    // recycle old backup session
                    session.recycle();
                } else {
                    // expire swapped out session
                    session.expire();
                }
                remove(session.getIdInternal());
            } catch (IOException e) {
                log (e.toString());
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                log (e.toString());
                e.printStackTrace();
            }
        }
    }

    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {
        Logger logger = null;
        Container container = manager.getContainer();

        if (container != null) {
            logger = container.getLogger();
        }

        if (logger != null) {
            logger.log(getStoreName()+"[" + container.getName() + "]: "
                       + message);
        } else {
            String containerName = null;
            if (container != null) {
                containerName = container.getName();
            }
            System.out.println(getStoreName()+"[" + containerName
                               + "]: " + message);
        }
    }
    
    /**
     * Load and return the Session associated with the specified session
     * identifier from this Store, without removing it.  If there is no
     * such stored Session, return <code>null</code>.
     *
     * @param id Session identifier of the session to load
     * @param version The requested session version
     *
     * @exception ClassNotFoundException if a deserialization error occurs
     * @exception IOException if an input/output error occurs
     */
    public Session load(String id, String version)
            throws ClassNotFoundException, IOException {
        return load(id);
    }    

    // --------------------------------------------------------- Thread Methods


    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {
        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                (sm.getString(getStoreName()+".alreadyStarted"));
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {
        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                (sm.getString(getStoreName()+".notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

    }


}
