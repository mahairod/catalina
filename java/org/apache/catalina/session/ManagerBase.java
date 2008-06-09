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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.*;

import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.management.MBeanServer;

//START OF RIMOD# 4820359
import javax.servlet.http.HttpSession; 
//END OF RIMOD# 4820359

//START OF 6364900
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
//END OF 6364900
import org.apache.catalina.Container;
import org.apache.catalina.DefaultContext;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
//START OF 6364900
import org.apache.catalina.SessionLocker;
//END OF 6364900
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.util.StringManager;
import org.apache.commons.modeler.Registry;

//HERCULES:added
import com.sun.enterprise.util.uuid.UuidGeneratorImpl;
import com.sun.enterprise.util.uuid.UuidGenerator;
//end HERCULES:added


/**
 * Minimal implementation of the <b>Manager</b> interface that supports
 * no session persistence or distributable capabilities.  This class may
 * be subclassed to create more sophisticated Manager implementations.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.23.2.3 $ $Date: 2008/04/17 18:37:20 $
 */

public abstract class ManagerBase implements Manager, MBeanRegistration {
    protected Logger log = Logger.getLogger(ManagerBase.class.getName());

    // ----------------------------------------------------- Instance Variables

    protected DataInputStream randomIS=null;
    protected String devRandomSource="/dev/urandom";

    /**
     * The default message digest algorithm to use if we cannot use
     * the requested one.
     */
    protected static final String DEFAULT_ALGORITHM = "MD5";


    /**
     * The message digest algorithm to be used when generating session
     * identifiers.  This must be an algorithm supported by the
     * <code>java.security.MessageDigest</code> class on your platform.
     */
    protected String algorithm = DEFAULT_ALGORITHM;


    /**
     * The Container with which this Manager is associated.
     */
    protected Container container;


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;


    /**
     * The DefaultContext with which this Manager is associated.
     */
    protected DefaultContext defaultContext = null;
    
    
    /**
     * Return the MessageDigest implementation to be used when
     * creating session identifiers.
     */
    protected MessageDigest digest = null;


    /**
     * The distributable flag for Sessions created by this Manager.  If this
     * flag is set to <code>true</code>, any user attributes added to a
     * session controlled by this Manager must be Serializable.
     */
    protected boolean distributable;


    /**
     * A String initialization parameter used to increase the entropy of
     * the initialization of our random number generator.
     */
    protected String entropy = null;
    
    //START OF 6364900
    /**
     * A SessionLocker used to lock sessions (curently only
     * in the request dispatcher forward/include use case)
     */
    protected SessionLocker sessionLocker = new BaseSessionLocker();
    //END OF 6364900    

    /**
     * The descriptive information string for this implementation.
     */
    private static final String info = "ManagerBase/1.0";


    /**
     * The default maximum inactive interval for Sessions created by
     * this Manager.
     */
    protected int maxInactiveInterval = 60;


    /**
     * The session id length of Sessions created by this Manager.
     */
    protected int sessionIdLength = 16;


    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    protected static final String name = "ManagerBase";


    /**
     * A random number generator to use when generating session identifiers.
     */
    protected Random random = null;
    
    
    /**
     * The Uuid Generator to be used
     * when generating universally unique session identifiers.
     * HERCULES: add
     */
    protected UuidGenerator uuidGenerator = new UuidGeneratorImpl();     


    /**
     * The Java class name of the random number generator class to be used
     * when generating session identifiers.
     */
    protected String randomClass = "java.security.SecureRandom";


    /**
     * The longest time (in seconds) that an expired session had been alive.
     */
    protected int sessionMaxAliveTime;


    /**
     * Average time (in seconds) that expired sessions had been alive.
     */
    protected int sessionAverageAliveTime;


    /**
     * Number of sessions that have expired.
     */
    protected int expiredSessions = 0;


    /**
     * The set of currently active Sessions for this Manager, keyed by
     * session identifier.
     */
    protected HashMap sessions = new HashMap();
    
    // Number of sessions created by this manager
    protected int sessionCounter=0;

    protected int maxActive=0;

    // number of duplicated session ids - anything >0 means we have problems
    protected int duplicates=0;

    protected boolean initialized=false;

    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);

    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    /**
     * Utility class used to call into services from
     * com.sun.ejb.base.io.IOUtils, which provides object input and output
     * streams for the serialization and deserialization of EJB references
     * stored in HTTP sessions
     */
    private static final IOUtilsCaller webUtilsCaller;

    /**
     * Creates the utility class used to call into services from
     * com.sun.ejb.base.io.IOUtils
     */
    static {
        WebIOUtilsFactory factory = new WebIOUtilsFactory();
        webUtilsCaller = factory.createWebIOUtil();            
    }


    // ------------------------------------------------------- Security classes
    private class PrivilegedSetRandomFile implements PrivilegedAction{
        
        public Object run(){               
            FileInputStream fileInputStream = null;
            try {
                File f=new File( devRandomSource );
                if( ! f.exists() ) return null;
                fileInputStream = new FileInputStream(f);
                randomIS= new DataInputStream( fileInputStream );
                randomIS.readLong();
                if( log.isLoggable(Level.FINE))
                    log.fine( "Opening " + devRandomSource );
                return randomIS;
            } catch (IOException ex){
                return null;
            } finally{
                try{
                    if ( fileInputStream != null )
                        fileInputStream.close();
                } catch (IOException ex){
                    ;
                }
            }
        }
    }


    // ------------------------------------------------------------- Properties
    
    /**
     * Return the UuidGenerator for this Manager.
     * HERCULES:added
     */
    public UuidGenerator getUuidGenerator() {

        return (this.uuidGenerator);

    }
    
    /**
     * Set the UuidGenerator for this Manager.
     * HERCULES:added
     */
    public void setUuidGenerator(UuidGenerator aUuidGenerator) {

        uuidGenerator = aUuidGenerator;

    }     

    /**
     * Return the message digest algorithm for this Manager.
     */
    public String getAlgorithm() {

        return (this.algorithm);

    }


    /**
     * Set the message digest algorithm for this Manager.
     *
     * @param algorithm The new message digest algorithm
     */
    public void setAlgorithm(String algorithm) {

        String oldAlgorithm = this.algorithm;
        this.algorithm = algorithm;
        support.firePropertyChange("algorithm", oldAlgorithm, this.algorithm);

    }


    /**
     * Return the Container with which this Manager is associated.
     */
    public Container getContainer() {

        return (this.container);

    }


    /**
     * Set the Container with which this Manager is associated.
     *
     * @param container The newly associated Container
     */
    public void setContainer(Container container) {

        Container oldContainer = this.container;
        this.container = container;
        support.firePropertyChange("container", oldContainer, this.container);
        // TODO: find a good scheme for the log names
        //log=LogFactory.getLog("tomcat.manager." + container.getName());
    }


    /**
     * Return the DefaultContext with which this Manager is associated.
     */
    public DefaultContext getDefaultContext() {

        return (this.defaultContext);

    }


    /**
     * Set the DefaultContext with which this Manager is associated.
     *
     * @param defaultContext The newly associated DefaultContext
     */
    public void setDefaultContext(DefaultContext defaultContext) {

        DefaultContext oldDefaultContext = this.defaultContext;
        this.defaultContext = defaultContext;
        support.firePropertyChange("defaultContext", oldDefaultContext, this.defaultContext);

    }
    
    
    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }

    /** Returns the name of the implementation class.
     */
    public String getClassName() {
        return this.getClass().getName();
    }


    /**
     * Return the MessageDigest object to be used for calculating
     * session identifiers.  If none has been created yet, initialize
     * one the first time this method is called.
     */
    public synchronized MessageDigest getDigest() {

        if (this.digest == null) {
            long t1=System.currentTimeMillis();
            if (log.isLoggable(Level.FINE))
                log.fine(sm.getString("managerBase.getting", algorithm));
            try {
                this.digest = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                log.log(Level.SEVERE,
                        sm.getString("managerBase.digest", algorithm),
                        e);
                try {
                    this.digest = MessageDigest.getInstance(DEFAULT_ALGORITHM);
                } catch (NoSuchAlgorithmException f) {
                    log.log(Level.SEVERE,
                            sm.getString("managerBase.digest",
                                         DEFAULT_ALGORITHM),
                            e);
                    this.digest = null;
                }
            }
            if (log.isLoggable(Level.FINE))
                log.fine(sm.getString("managerBase.gotten"));
            long t2=System.currentTimeMillis();
            if (log.isLoggable(Level.FINE))
                log.fine("getDigest() " + (t2-t1));
        }

        return (this.digest);

    }


    /**
     * Return the distributable flag for the sessions supported by
     * this Manager.
     */
    public boolean getDistributable() {

        return (this.distributable);

    }


    /**
     * Set the distributable flag for the sessions supported by this
     * Manager.  If this flag is set, all user data objects added to
     * sessions associated with this manager must implement Serializable.
     *
     * @param distributable The new distributable flag
     */
    public void setDistributable(boolean distributable) {

        boolean oldDistributable = this.distributable;
        this.distributable = distributable;
        support.firePropertyChange("distributable",
                                   Boolean.valueOf(oldDistributable),
                                   Boolean.valueOf(this.distributable));

    }


    /**
     * Return the entropy increaser value, or compute a semi-useful value
     * if this String has not yet been set.
     */
    public String getEntropy() {

        // Calculate a semi-useful value if this has not been set
        if (this.entropy == null)
            setEntropy(this.toString());

        return (this.entropy);

    }


    /**
     * Set the entropy increaser value.
     *
     * @param entropy The new entropy increaser value
     */
    public void setEntropy(String entropy) {

        String oldEntropy = entropy;
        this.entropy = entropy;
        support.firePropertyChange("entropy", oldEntropy, this.entropy);

    }


    /**
     * Return descriptive information about this Manager implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (this.info);

    }


    /**
     * Same as getMaxInactiveIntervalSeconds
     */
    public int getMaxInactiveInterval() {
        return getMaxInactiveIntervalSeconds();
    }


    /**
     * Return the default maximum inactive interval (in seconds)
     * for Sessions created by this Manager.
     */
    public int getMaxInactiveIntervalSeconds() {

        return (this.maxInactiveInterval);

    }


    /**
     * Same as setMaxInactiveIntervalSeconds
     */
    public void setMaxInactiveInterval(int interval) {
        setMaxInactiveIntervalSeconds(interval);
    }


    /**
     * Set the default maximum inactive interval (in seconds)
     * for Sessions created by this Manager.
     *
     * @param interval The new default value
     */
    public void setMaxInactiveIntervalSeconds(int interval) {

        int oldMaxInactiveInterval = this.maxInactiveInterval;
        this.maxInactiveInterval = interval;
        support.firePropertyChange("maxInactiveInterval",
                                   Integer.valueOf(oldMaxInactiveInterval),
                                   Integer.valueOf(this.maxInactiveInterval));

    }


    /**
     * Gets the session id length (in bytes) of Sessions created by
     * this Manager.
     *
     * @return The session id length
     */
    public int getSessionIdLength() {

        return (this.sessionIdLength);

    }


    /**
     * Sets the session id length (in bytes) for Sessions created by this
     * Manager.
     *
     * @param sessionIdLength The session id length
     */
    public void setSessionIdLength(int idLength) {

        int oldSessionIdLength = this.sessionIdLength;
        this.sessionIdLength = idLength;
        support.firePropertyChange("sessionIdLength",
                                   Integer.valueOf(oldSessionIdLength),
                                   Integer.valueOf(this.sessionIdLength));

    }


    /**
     * Return the descriptive short name of this Manager implementation.
     */
    public String getName() {

        return (name);

    }

    /**
     * Use /dev/random-type special device. This is new code, but may reduce
     * the big delay in generating the random.
     *
     *  You must specify a path to a random generator file. Use /dev/urandom
     *  for linux ( or similar ) systems. Use /dev/random for maximum security
     *  ( it may block if not enough "random" exist ). You can also use
     *  a pipe that generates random.
     *
     *  The code will check if the file exists, and default to java Random
     *  if not found. There is a significant performance difference, very
     *  visible on the first call to getSession ( like in the first JSP )
     *  - so use it if available.
     */
    public void setRandomFile( String s ) {
    // as a hack, you can use a static file - and genarate the same
    // session ids ( good for strange debugging )
        if (Globals.IS_SECURITY_ENABLED){
                randomIS = (DataInputStream)AccessController.doPrivileged(new PrivilegedSetRandomFile());          
            } else {
                FileInputStream fileInputStream = null;
                try{
                    devRandomSource=s;
                    File f=new File( devRandomSource );
                    if( ! f.exists() ) return;
                    fileInputStream = new FileInputStream(f);
                    randomIS= new DataInputStream( fileInputStream);
                    randomIS.readLong();
                    if (log.isLoggable(Level.FINE))
                        log.fine( "Opening " + devRandomSource );
                } catch( IOException ex ) {
                    randomIS=null;
                } finally {
                    try{
                        if ( fileInputStream != null )
                            fileInputStream.close();
                    } catch (IOException ex){
                        ;
                    }
                }
            }
    }

    public String getRandomFile() {
        return devRandomSource;
    }


    /**
     * Return the random number generator instance we should use for
     * generating session identifiers.  If there is no such generator
     * currently defined, construct and seed a new one.
     */
    public synchronized Random getRandom() {
        if (this.random == null) {
            synchronized (this) {
                if (this.random == null) {
                    // Calculate the new random number generator seed
                    long seed = System.currentTimeMillis();
                    long t1 = seed;
                    char entropy[] = getEntropy().toCharArray();
                    for (int i = 0; i < entropy.length; i++) {
                        long update = ((byte) entropy[i]) << ((i % 8) * 8);
                        seed ^= update;
                    }
                    try {
                        // Construct and seed a new random number generator
                        Class clazz = Class.forName(randomClass);
                        this.random = (Random) clazz.newInstance();
                        this.random.setSeed(seed);
                    } catch (Exception e) {
                        // Fall back to the simple case
                        log.log(Level.SEVERE,
                                sm.getString("managerBase.random",
                                             randomClass),
                                e);
                        this.random = new java.util.Random();
                        this.random.setSeed(seed);
                    }
                    long t2=System.currentTimeMillis();
                    if( (t2-t1) > 100 )
                        log.fine(sm.getString("managerBase.seeding",
                                              randomClass) + " " + (t2-t1));
                }
            }
        }

        return (this.random);

    }


    /**
     * Return the random number generator class name.
     */
    public String getRandomClass() {

        return (this.randomClass);

    }


    /**
     * Set the random number generator class name.
     *
     * @param randomClass The new random number generator class name
     */
    public void setRandomClass(String randomClass) {

        String oldRandomClass = this.randomClass;
        this.randomClass = randomClass;
        support.firePropertyChange("randomClass", oldRandomClass,
                                   this.randomClass);

    }


    /**
     * Gets the number of sessions that have expired.
     *
     * @return Number of sessions that have expired
     */
    public int getExpiredSessions() {
        return expiredSessions;
    }


    /**
     * Sets the number of sessions that have expired.
     *
     * @param expiredSessions Number of sessions that have expired
     */
    public void setExpiredSessions(int expiredSessions) {
        this.expiredSessions = expiredSessions;
    }

    //START OF 6364900
    /**
     * set the pluggable sessionLocker for this manager
     * by default it is pre-set to no-op BaseSessionLocker
     */    
    public void setSessionLocker(SessionLocker sessLocker) {
        sessionLocker = sessLocker;
    }
    //END OF 6364900
    
    // --------------------------------------------------------- Public Methods
    public void destroy() {
        if( oname != null )
            Registry.getRegistry().unregisterComponent(oname);
        initialized=false;
        oname = null;
    }
    
    public void init() {
        if( initialized ) return;
        initialized=true;        
        
        if( oname==null ) {
            try {
                StandardContext ctx=(StandardContext)this.getContainer();
                Engine eng=(Engine)ctx.getParent().getParent();
                domain=ctx.getEngineName();
                distributable = ctx.getDistributable();
                StandardHost hst=(StandardHost)ctx.getParent();
                String path = ctx.getEncodedPath();
                if (path.equals("")) {
                    path = "/";
                }   
                oname=new ObjectName(domain + ":type=Manager,path="
                + path + ",host=" + hst.getName());
                Registry.getRegistry().registerComponent(this, oname, null );
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error registering ", e);
            }
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("Registering " + oname );
        }           
    }

    /**
     * Add this Session to the set of active Sessions for this Manager.
     *
     * @param session Session to be added
     */
    public void add(Session session) {

        synchronized (sessions) {
            sessions.put(session.getIdInternal(), session);
            if( sessions.size() > maxActive ) {
                maxActive=sessions.size();
            }
        }
    }
    

    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {

        support.addPropertyChangeListener(listener);

    }


    /**
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id will be assigned by this method, and available via the getId()
     * method of the returned session.  If a new session cannot be created
     * for any reason, return <code>null</code>.
     * Hercules: modified
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     */
    public Session createSession() {
        
        // Recycle or create a Session instance
        Session session = null;
        session = createEmptySession();
        StandardSession sess = (StandardSession) session;
        //always lock
        sess.lockForeground(); 

        // Initialize the properties of the new session and return it
        session.setNew(true);
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());
        session.setMaxInactiveInterval(this.maxInactiveInterval);
        String sessionId = generateSessionId(session);

        session.setId(sessionId);
        sessionCounter++;

        return (session);

    }


    // START S1AS8PE 4817642
    /**
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties, using the specified
     * session id.
     *
     * IMPLEMENTATION NOTE: This method must be kept in sync with the
     * createSession method that takes no arguments.
     *
     * @param sessionId the session id to assign to the new session
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     *
     * @return the new session, or <code>null</code> if a session with the
     * requested id already exists
     */
    public Session createSession(String sessionId) {

        // Recycle or create a Session instance
        Session session = createEmptySession();

        // Initialize the properties of the new session and return it
        session.setNew(true);
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());
        session.setMaxInactiveInterval(this.maxInactiveInterval);

        //START OF 6364900
        StandardSession sess = (StandardSession) session;
        //always lock
        sess.lockForeground();        
        //END OF 6364900        

        session.setId(sessionId);
        sessionCounter++;

        return (session);

    }
    // END S1AS8PE 4817642


    /**
     * Get a session from the recycled ones or create a new empty one.
     * The PersistentManager manager does not need to create session data
     * because it reads it from the Store.
     */
    public Session createEmptySession() {
        return (getNewSession());
    }


    /**
     * Return the active Session, associated with this Manager, with the
     * specified session id (if any); otherwise return <code>null</code>.
     *
     * @param id The session id for the session to be returned
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     * @exception IOException if an input/output error occurs while
     *  processing this request
     */
    public Session findSession(String id) throws IOException {

        if (id == null)
            return (null);
        synchronized (sessions) {
            Session session = (Session) sessions.get(id);
            return (session);
        }

    }
    
    
    /**
     * Finds and returns the session with the given id that also satisfies
     * the given version requirement.
     *
     * This overloaded version of findSession() will be invoked only if
     * isSessionVersioningSupported() returns true. By default, this method
     * delegates to the version of findSession() that does not take any
     * session version number.
     *
     * @param id The session id to match
     * @param version The session version requirement to satisfy
     *
     * @return The session that matches the given id and also satisfies the
     * given version requirement, or null if no such session could be found
     * by this session manager
     *
     * @exception IOException if an IO error occurred
     */
    public Session findSession(String id, String version) throws IOException {

        return findSession(id);
    }


    /**
     * Returns true if this session manager supports session versioning, false
     * otherwise.
     *
     * @return true if this session manager supports session versioning, false
     * otherwise.
     */
    public boolean isSessionVersioningSupported() {
        return false;
    }


    /**
     * clear out the sessions cache
     * HERCULES:added
     */
    public void clearSessions() {
        sessions.clear();
    }    


    /**
     * Return the set of active Sessions associated with this Manager.
     * If this Manager has no active Sessions, a zero-length array is returned.
     */
    public Session[] findSessions() {

        Session results[] = null;
        synchronized (sessions) {
            results = new Session[sessions.size()];
            results = (Session[]) sessions.values().toArray(results);
        }
        return (results);

    }


    /**
     * Remove this Session from the active Sessions for this Manager.
     *
     * @param session Session to be removed
     */
    public void remove(Session session) {

        synchronized (sessions) {
            sessions.remove(session.getIdInternal());
        }

    }


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {

        support.removePropertyChangeListener(listener);

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Gets the utility class used to call into services from
     * com.sun.ejb.base.io.IOUtils.
     */
    protected static IOUtilsCaller getWebUtilsCaller() {
        return webUtilsCaller;
    }


    /**
     * Get new session class to be used in the doLoad() method.
     */
    protected StandardSession getNewSession() {
        return new StandardSession(this);
    }


    protected void getRandomBytes( byte bytes[] ) {
        // Generate a byte array containing a session identifier
        if( devRandomSource!=null && randomIS==null ) {
            setRandomFile( devRandomSource );
        }
        if(randomIS!=null ) {
            try {
                int len=randomIS.read( bytes );
                if( len==bytes.length ) {
                    return;
                }
                if (log.isLoggable(Level.FINE)) {
                    log.fine("Got " + len + " " + bytes.length );
                }
            } catch( Exception ex ) {
            }
            devRandomSource=null;
            randomIS=null;
        }
        Random random = getRandom();
        getRandom().nextBytes(bytes);
    }
    
    
    /**
     * Generate and return a new session identifier.
     * Hercules:added
     */
    protected synchronized String generateSessionId(Object obj) {
        return uuidGenerator.generateUuid(obj);
    }   
    
    /**
     * Generate and return a new session identifier.
     * Hercules:modified
     */
    protected synchronized String generateSessionId() {
        return generateSessionId(new Object());
    }    


    // ------------------------------------------------------ Protected Methods


    /**
     * Retrieve the enclosing Engine for this Manager.
     *
     * @return an Engine object (or null).
     */
    public Engine getEngine() {
        Engine e = null;
        for (Container c = getContainer(); e == null && c != null ; c = c.getParent()) {
            if (c != null && c instanceof Engine) {
                e = (Engine)c;
            }
        }
        return e;
    }


    /**
     * Retrieve the JvmRoute for the enclosing Engine.
     * @return the JvmRoute or null.
     */
    public String getJvmRoute() {
        Engine e = getEngine();
        return e == null ? null : e.getJvmRoute();
    }


    // -------------------------------------------------------- Package Methods


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @deprecated
     */
    protected void log(String message) {
        log.info( message );
    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     * @deprecated
     */
    protected void log(String message, Throwable throwable) {
        log.log(Level.INFO, message, throwable);
    }


    /**
     * Same as setSessionCount
     */
    public void setSessionCounter(int sessionCounter) {
        setSessionCount(sessionCounter);
    }

   
    public void setSessionCount(int sessionCounter) {
        this.sessionCounter = sessionCounter;
    }


    /** 
     * Same as getSessionCount
     */
    public int getSessionCounter() {
        return getSessionCount();
    }


    /** 
     * Total sessions created by this manager.
     *
     * @return sessions created
     */
    public int getSessionCount() {
        return sessionCounter;
    }


    /** 
     * Number of duplicated session IDs generated by the random source.
     * Anything bigger than 0 means problems.
     *
     * @return
     */
    public int getDuplicates() {
        return duplicates;
    }


    public void setDuplicates(int duplicates) {
        this.duplicates = duplicates;
    }


    /** 
     * Returns the number of active sessions
     *
     * @return number of sessions active
     */
    public int getActiveSessions() {
        return sessions.size();
    }


    /**
     * Max number of concurent active sessions
     *
     * @return
     */
    public int getMaxActive() {
        return maxActive;
    }


    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }


    /**
     * Same as getSessionMaxAliveTimeSeconds
     */
    public int getSessionMaxAliveTime() {
        return getSessionMaxAliveTimeSeconds();
    }


    /**
     * Gets the longest time (in seconds) that an expired session had been
     * alive.
     *
     * @return Longest time (in seconds) that an expired session had been
     * alive.
     */
    public int getSessionMaxAliveTimeSeconds() {
        return sessionMaxAliveTime;
    }


    /**
     * Same as setSessionMaxAliveTimeSeconds
     */
    public void setSessionMaxAliveTime(int sessionMaxAliveTime) {
        setSessionMaxAliveTimeSeconds(sessionMaxAliveTime);
    }


    /**
     * Sets the longest time (in seconds) that an expired session had been
     * alive.
     *
     * @param sessionMaxAliveTime Longest time (in seconds) that an expired
     * session had been alive.
     */
    public void setSessionMaxAliveTimeSeconds(int sessionMaxAliveTime) {
        this.sessionMaxAliveTime = sessionMaxAliveTime;
    }


    /**
     * Same as getSessionAverageAliveTimeSeconds
     */
    public int getSessionAverageAliveTime() {
        return getSessionAverageAliveTimeSeconds();
    }


    /**
     * Gets the average time (in seconds) that expired sessions had been
     * alive.
     *
     * @return Average time (in seconds) that expired sessions had been
     * alive.
     */
    public int getSessionAverageAliveTimeSeconds() {
        return sessionAverageAliveTime;
    }


    /**
     * Same as setSessionAverageAliveTimeSeconds
     */
    public void setSessionAverageAliveTime(int sessionAverageAliveTime) {
        setSessionAverageAliveTimeSeconds(sessionAverageAliveTime);
    }


    /**
     * Sets the average time (in seconds) that expired sessions had been
     * alive.
     *
     * @param sessionAverageAliveTime Average time (in seconds) that expired
     * sessions had been alive.
     */
    public void setSessionAverageAliveTimeSeconds(int sessionAverageAliveTime) {
        this.sessionAverageAliveTime = sessionAverageAliveTime;
    }


    /** 
     * For debugging: return a list of all session ids currently active
     *
     */
    public String listSessionIds() {
        StringBuffer sb=new StringBuffer();
        Iterator keys=sessions.keySet().iterator();
        while( keys.hasNext() ) {
            sb.append(keys.next()).append(" ");
        }
        return sb.toString();
    }


    /** 
     * For debugging: get a session attribute
     *
     * @param sessionId
     * @param key
     * @return
     */
    public String getSessionAttribute( String sessionId, String key ) {
        Session s=(Session)sessions.get(sessionId);
        if( s==null ) {
            log.info("Session not found " + sessionId);
            return null;
        }
        Object o=s.getSession().getAttribute(key);
        if( o==null ) return null;
        return o.toString();
    }


    /**
     * Returns information about the session with the given session id.
     * 
     * <p>The session information is organized as a HashMap, mapping 
     * session attribute names to the String representation of their values.
     *
     * @param sessionId Session id
     * 
     * @return HashMap mapping session attribute names to the String
     * representation of their values, or null if no session with the
     * specified id exists, or if the session does not have any attributes
     */
    public HashMap getSession(String sessionId) {
        Session s = (Session) sessions.get(sessionId);
        if (s == null) {
            if (log.isLoggable(Level.INFO)) {
                log.info("Session not found " + sessionId);
            }
            return null;
        }

        Enumeration ee = s.getSession().getAttributeNames();
        if (ee == null || !ee.hasMoreElements()) {
            return null;
        }

        HashMap map = new HashMap();
        while (ee.hasMoreElements()) {
            String attrName = (String) ee.nextElement();
            map.put(attrName, getSessionAttribute(sessionId, attrName));
        }

        return map;
    }


    public void expireSession( String sessionId ) {
        Session s=(Session)sessions.get(sessionId);
        if( s==null ) {
            log.info("Session not found " + sessionId);
            return;
        }
        s.expire();
    }


    public String getLastAccessedTimeMillis( String sessionId ) {
        Session s=(Session)sessions.get(sessionId);
        if( s==null ) {
            log.info("Session not found " + sessionId);
            return "";
        }
        return new Date(s.getLastAccessedTime()).toString();
    }

    
    //PWC Extension
    //START OF RIMOD# 4820359 -- Support for iWS6.0 session managers
    /**
     * Perform any operations when the request is finished.
     */
    public void update(HttpSession session) throws Exception {
        return;
    }
    //END OF RIMOD# 4820359

    // -------------------- JMX and Registration  --------------------
    protected String domain;
    protected ObjectName oname;
    protected MBeanServer mserver;

    public ObjectName getObjectName() {
        return oname;
    }

    public String getDomain() {
        return domain;
    }

    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception {
        oname=name;
        mserver=server;
        domain=name.getDomain();
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }
    
    //START OF 6364900
    public void postRequestDispatcherProcess(ServletRequest request, ServletResponse response) {
        //deliberate no-op
        return;
    }
    
    public void preRequestDispatcherProcess(ServletRequest request, ServletResponse response) {
        //deliberate no-op
        return;
    }    
    
    public boolean lockSession(ServletRequest request) throws ServletException {
        boolean result = false;
        if(sessionLocker != null) {
            result = sessionLocker.lockSession(request);
        }        
        return result;
    }
    
    public void unlockSession(ServletRequest request) {
        if(sessionLocker != null) {
            sessionLocker.unlockSession(request);
        }
    }
    //END OF 6364900    


    /*
     * Releases any resources held by this session manager.
     */
    public void release() {
        clearSessions();
    }
}
