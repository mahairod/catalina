

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */


package org.apache.catalina.loader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.security.SecurityUtil;
import org.apache.catalina.util.StringManager;
import org.apache.naming.JndiPermission;
import org.apache.naming.resources.Resource;
import org.apache.naming.resources.ResourceAttributes;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.compat.JdkCompat;
import com.sun.org.apache.commons.beanutils.MappedPropertyDescriptor;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;
import com.sun.org.apache.commons.logging.LogConfigurationException;

// START OF IASRI 4709374
import com.sun.appserv.server.util.PreprocessorUtil;
// END OF IASRI 4709374

// START SJSAS 6258619
import com.sun.appserv.ClassLoaderUtil;
// END SJSAS 6258619

// START SJSAS 6344989
import com.sun.appserv.BytecodePreprocessor;
// END SJSAS 6344989

/**
 * Specialized web application class loader.
 * <p>
 * This class loader is a full reimplementation of the 
 * <code>URLClassLoader</code> from the JDK. It is desinged to be fully
 * compatible with a normal <code>URLClassLoader</code>, although its internal
 * behavior may be completely different.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - This class loader faithfully follows 
 * the delegation model recommended in the specification. The system class 
 * loader will be queried first, then the local repositories, and only then 
 * delegation to the parent class loader will occur. This allows the web 
 * application to override any shared class except the classes from J2SE.
 * Special handling is provided from the JAXP XML parser interfaces, the JNDI
 * interfaces, and the classes from the servlet API, which are never loaded 
 * from the webapp repository.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - Due to limitations in Jasper 
 * compilation technology, any repository which contains classes from 
 * the servlet API will be ignored by the class loader.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - The class loader generates source
 * URLs which include the full JAR URL when a class is loaded from a JAR file,
 * which allows setting security permission at the class level, even when a
 * class is contained inside a JAR.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - Local repositories are searched in
 * the order they are added via the initial constructor and/or any subsequent
 * calls to <code>addRepository()</code> or <code>addJar()</code>.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - No check for sealing violations or
 * security is made unless a security manager is present.
 *
 * @author Remy Maucherat
 * @author Craig R. McClanahan
 * @version $Revision: 1.34 $ $Date: 2007/05/25 17:59:42 $
 */
public class WebappClassLoader
    extends URLClassLoader
    implements Reloader, Lifecycle
 {

    private static com.sun.org.apache.commons.logging.Log log=
        com.sun.org.apache.commons.logging.LogFactory.getLog( WebappClassLoader.class );

    protected class PrivilegedFindResource
        implements PrivilegedAction {

        private File file;
        private String path;

        PrivilegedFindResource(File file, String path) {
            this.file = file;
            this.path = path;
        }

        public Object run() {
            return findResourceInternal(file, path);
        }

    }


    // ------------------------------------------------------- Static Variables


    /**
     * The set of trigger classes that will cause a proposed repository not
     * to be added if this class is visible to the class loader that loaded
     * this factory class.  Typically, trigger classes will be listed for
     * components that have been integrated into the JDK for later versions,
     * but where the corresponding JAR files are required to run on
     * earlier versions.
     */
    private static final String[] triggers = {
        "javax.servlet.Servlet"                     // Servlet API
    };


    /**
     * Set of package names which are not allowed to be loaded from a webapp
     * class loader without delegating first.
     */
    private static final String[] packageTriggers = {
        "javax",                                     // Java extensions
        // START PE 4985680 
        "sun",                                       // Sun classes
        // END PE 4985680
        "org.xml.sax",                               // SAX 1 & 2
        "org.w3c.dom",                               // DOM 1 & 2
        "org.apache.xerces",                         // Xerces 1 & 2
        "org.apache.xalan",                          // Xalan
        "org.apache.taglibs.standard",               // JSTL (Java EE 5)
        "com.sun.faces",                             // JSF (Java EE 5)
        "org.apache.commons.logging"                 // Commons logging
    };
    
    // START PE 4985680    
    /**
     * List of packages that may always be overridden, regardless of whether
     * they belong to a protected namespace (i.e., a namespace that may never be
     * overridden by a webapp)  
     */
    private ArrayList overridablePackages;
   // END PE 4985680


    /**
     * The string manager for this package.
     */
    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new ClassLoader with no defined repositories and no
     * parent ClassLoader.
     */
    public WebappClassLoader() {
        super(new URL[0]);
        init();
    }


    /**
     * Construct a new ClassLoader with no defined repositories and no
     * parent ClassLoader.
     */
    public WebappClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
        init();
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Associated directory context giving access to the resources in this
     * webapp.
     */
    protected DirContext resources = null;


    /**
     * The cache of ResourceEntry for classes and resources we have loaded,
     * keyed by resource name.
     */
    protected HashMap resourceEntries = new HashMap();


    /**
     * The list of not found resources.
     */
    protected HashMap notFoundResources = new HashMap();


    /**
     * The debugging detail level of this component.
     */
    protected int debug = 0;


    /**
     * Should this class loader delegate to the parent class loader
     * <strong>before</strong> searching its own repositories (i.e. the
     * usual Java2 delegation model)?  If set to <code>false</code>,
     * this class loader will search its own repositories first, and
     * delegate to the parent only if the class or resource is not
     * found locally.
     */
    protected boolean delegate = false;


    /**
     * Last time a JAR was accessed.
     */
    protected long lastJarAccessed = 0L;


    /**
     * The list of local repositories, in the order they should be searched
     * for locally loaded classes or resources.
     */
    protected String[] repositories = new String[0];


     /**
      * Repositories URLs, used to cache the result of getURLs.
      */
     protected URL[] repositoryURLs = null;


    /**
     * Repositories translated as path in the work directory (for Jasper
     * originally), but which is used to generate fake URLs should getURLs be
     * called.
     */
    protected File[] files = new File[0];


    /**
     * The list of JARs, in the order they should be searched
     * for locally loaded classes or resources.
     */
    protected JarFile[] jarFiles = new JarFile[0];


    /**
     * The list of JARs, in the order they should be searched
     * for locally loaded classes or resources.
     */
    protected File[] jarRealFiles = new File[0];


    /**
     * The path which will be monitored for added Jar files.
     */
    protected String jarPath = null;


    /**
     * The list of JARs, in the order they should be searched
     * for locally loaded classes or resources.
     */
    protected String[] jarNames = new String[0];


    /**
     * The list of JARs last modified dates, in the order they should be
     * searched for locally loaded classes or resources.
     */
    protected long[] lastModifiedDates = new long[0];


    /**
     * The list of resources which should be checked when checking for
     * modifications.
     */
    protected String[] paths = new String[0];


    /**
     * A list of read File and Jndi Permission's required if this loader
     * is for a web application context.
     */
    private ArrayList permissionList = new ArrayList();


    /**
     * Path where resources loaded from JARs will be extracted.
     */
    private File loaderDir = null;


    /**
     * The PermissionCollection for each CodeSource for a web
     * application context.
     */
    private HashMap loaderPC = new HashMap();


    /**
     * Instance of the SecurityManager installed.
     */
    private SecurityManager securityManager = null;


    /**
     * The parent class loader.
     */
    private ClassLoader parent = null;


    /**
     * The system class loader.
     */
    private ClassLoader system = null;


    /**
     * Has this component been started?
     */
    protected boolean started = false;


    /**
     * Has external repositories.
     */
    protected boolean hasExternalRepositories = false;


    /**
     * All permission.
     */
    private Permission allPermission = new java.security.AllPermission();

    // START SJSAS 6344989
    /**
     * List of byte code pre-processors per webapp class loader.
     */
    ArrayList<BytecodePreprocessor> byteCodePreprocessors =
            new ArrayList<BytecodePreprocessor>();
    // END SJSAS 6344989

    private boolean useMyFaces;

    // ------------------------------------------------------------- Properties
    
    // START PE 4985680
    /**
     * Adds the given package name to the list of packages that may always be
     * overriden, regardless of whether they belong to a protected namespace
     */
    public void addOverridablePackage(String packageName){
        if (overridablePackages == null){
            overridablePackages = new ArrayList();
        }
        overridablePackages.add(packageName);
    }
    // END PE 4985680


    /**
     * Get associated resources.
     */
    public DirContext getResources() {

        return this.resources;

    }


    /**
     * Set associated resources.
     */
    public void setResources(DirContext resources) {

        this.resources = resources;

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


    /**
     * Return the "delegate first" flag for this class loader.
     */
    public boolean getDelegate() {

        return (this.delegate);

    }


    /**
     * Set the "delegate first" flag for this class loader.
     *
     * @param delegate The new "delegate first" flag
     */
    public void setDelegate(boolean delegate) {

        this.delegate = delegate;

    }


    /**
     * If there is a Java SecurityManager create a read FilePermission
     * or JndiPermission for the file directory path.
     *
     * @param path file directory path
     */
    public void addPermission(String path) {
        if (path == null) {
            return;
        }

        if (securityManager != null) {
            Permission permission = null;
            if( path.startsWith("jndi:") || path.startsWith("jar:jndi:") ) {
                if (!path.endsWith("/")) {
                    path = path + "/";
                }
                permission = new JndiPermission(path + "*");
                addPermission(permission);
            } else {
                if (!path.endsWith(File.separator)) {
                    permission = new FilePermission(path, "read");
                    addPermission(permission);
                    path = path + File.separator;
                }
                permission = new FilePermission(path + "-", "read");
                addPermission(permission);
            }
        }
    }


    /**
     * If there is a Java SecurityManager create a read FilePermission
     * or JndiPermission for URL.
     *
     * @param url URL for a file or directory on local system
     */
    public void addPermission(URL url) {
        if (url != null) {
            addPermission(url.toString());
        }
    }


    /**
     * If there is a Java SecurityManager create a Permission.
     *
     * @param url URL for a file or directory on local system
     */
    public void addPermission(Permission permission) {
        if ((securityManager != null) && (permission != null)) {
            permissionList.add(permission);
        }
    }


    /**
     * Return the JAR path.
     */
    public String getJarPath() {

        return this.jarPath;

    }


    /**
     * Change the Jar path.
     */
    public void setJarPath(String jarPath) {

        this.jarPath = jarPath;

    }


    /**
     * Change the work directory.
     */
    public void setWorkDir(File workDir) {
        this.loaderDir = new File(workDir, "loader");
    }


    public void setUseMyFaces(boolean useMyFaces) {
        this.useMyFaces = useMyFaces;
        if (useMyFaces) {
            addOverridablePackage("javax.faces");
            addOverridablePackage("com.sun.faces");
        }
    }


    // ------------------------------------------------------- Reloader Methods


    /**
     * Add a new repository to the set of places this ClassLoader can look for
     * classes to be loaded.
     *
     * @param repository Name of a source of classes to be loaded, such as a
     *  directory pathname, a JAR file pathname, or a ZIP file pathname
     *
     * @exception IllegalArgumentException if the specified repository is
     *  invalid or does not exist
     */
    public void addRepository(String repository) {

        // Ignore any of the standard repositories, as they are set up using
        // either addJar or addRepository
        if (repository.startsWith("/WEB-INF/lib")
            || repository.startsWith("/WEB-INF/classes"))
            return;

        // Add this repository to our underlying class loader
        try {
            URL url = new URL(repository);
            super.addURL(url);
            hasExternalRepositories = true;
        } catch (MalformedURLException e) {
            IllegalArgumentException iae = new IllegalArgumentException
                ("Invalid repository: " + repository);
            iae.initCause(e);
            throw iae;
        }

    }


    /**
     * Add a new repository to the set of places this ClassLoader can look for
     * classes to be loaded.
     *
     * @param repository Name of a source of classes to be loaded, such as a
     *  directory pathname, a JAR file pathname, or a ZIP file pathname
     *
     * @exception IllegalArgumentException if the specified repository is
     *  invalid or does not exist
     */
    synchronized void addRepository(String repository, File file) {

        // Note : There should be only one (of course), but I think we should
        // keep this a bit generic

        if (repository == null)
            return;

        if (log.isTraceEnabled())
            log.trace("addRepository(" + repository + ")");

        int i;

        // Add this repository to our internal list
        String[] result = new String[repositories.length + 1];
        for (i = 0; i < repositories.length; i++) {
            result[i] = repositories[i];
        }
        result[repositories.length] = repository;
        repositories = result;

        // Add the file to the list
        File[] result2 = new File[files.length + 1];
        for (i = 0; i < files.length; i++) {
            result2[i] = files[i];
        }
        result2[files.length] = file;
        files = result2;

    }


    synchronized public void addJar(String jar, JarFile jarFile, File file)
        throws IOException {

        if (jar == null)
            return;
        if (jarFile == null)
            return;
        if (file == null)
            return;

        if (log.isTraceEnabled())
            log.trace("addJar(" + jar + ")");

        int i;

        if ((jarPath != null) && (jar.startsWith(jarPath))) {

            String jarName = jar.substring(jarPath.length());
            while (jarName.startsWith("/"))
                jarName = jarName.substring(1);

            String[] result = new String[jarNames.length + 1];
            for (i = 0; i < jarNames.length; i++) {
                result[i] = jarNames[i];
            }
            result[jarNames.length] = jarName;
            jarNames = result;

        }

        try {

            // Register the JAR for tracking

            long lastModified =
                ((ResourceAttributes) resources.getAttributes(jar))
                .getLastModified();

            String[] result = new String[paths.length + 1];
            for (i = 0; i < paths.length; i++) {
                result[i] = paths[i];
            }
            result[paths.length] = jar;
            paths = result;

            long[] result3 = new long[lastModifiedDates.length + 1];
            for (i = 0; i < lastModifiedDates.length; i++) {
                result3[i] = lastModifiedDates[i];
            }
            result3[lastModifiedDates.length] = lastModified;
            lastModifiedDates = result3;

        } catch (NamingException e) {
            // Ignore
        }

        // If the JAR currently contains invalid classes, don't actually use it
        // for classloading
        if (!validateJarFile(file))
            return;

        JarFile[] result2 = new JarFile[jarFiles.length + 1];
        for (i = 0; i < jarFiles.length; i++) {
            result2[i] = jarFiles[i];
        }
        result2[jarFiles.length] = jarFile;
        jarFiles = result2;

        // Add the file to the list
        File[] result4 = new File[jarRealFiles.length + 1];
        for (i = 0; i < jarRealFiles.length; i++) {
            result4[i] = jarRealFiles[i];
        }
        result4[jarRealFiles.length] = file;
        jarRealFiles = result4;
    }


    /**
     * Return a String array of the current repositories for this class
     * loader.  If there are no repositories, a zero-length array is
     * returned.For security reason, returns a clone of the Array (since 
     * String are immutable).
     */
    public String[] findRepositories() {

        return ((String[])repositories.clone());

    }


    /**
     * Have one or more classes or resources been modified so that a reload
     * is appropriate?
     */
    public boolean modified() {

        if (log.isTraceEnabled())
            log.trace("modified()");

        // Checking for modified loaded resources
        int length = paths.length;

        // A rare race condition can occur in the updates of the two arrays
        // It's totally ok if the latest class added is not checked (it will
        // be checked the next time
        int length2 = lastModifiedDates.length;
        if (length > length2)
            length = length2;

        for (int i = 0; i < length; i++) {
            try {
                long lastModified =
                    ((ResourceAttributes) resources.getAttributes(paths[i]))
                    .getLastModified();
                if (lastModified != lastModifiedDates[i]) {
                    if( log.isTraceEnabled() ) 
                        log.trace("  Resource '" + paths[i]
                                  + "' was modified; Date is now: "
                                  + new java.util.Date(lastModified) + " Was: "
                                  + new java.util.Date(lastModifiedDates[i]));
                    return (true);
                }
            } catch (NamingException e) {
                log.error("    Resource '" + paths[i] + "' is missing");
                return (true);
            }
        }

        length = jarNames.length;

        // Check if JARs have been added or removed
        if (getJarPath() != null) {

            try {
                NamingEnumeration enumeration = resources.listBindings(getJarPath());
                int i = 0;
                while (enumeration.hasMoreElements() && (i < length)) {
                    NameClassPair ncPair = (NameClassPair) enumeration.nextElement();
                    String name = ncPair.getName();
                    // Ignore non JARs present in the lib folder
// START OF IASRI 4657979
                    if (!name.endsWith(".jar") && !name.endsWith(".zip"))
// END OF IASRI 4657979
                        continue;
                    if (!name.equals(jarNames[i])) {
                        // Missing JAR
                        log.info("    Additional JARs have been added : '" 
                                 + name + "'");
                        return (true);
                    }
                    i++;
                }
                if (enumeration.hasMoreElements()) {
                    while (enumeration.hasMoreElements()) {
                        NameClassPair ncPair = 
                            (NameClassPair) enumeration.nextElement();
                        String name = ncPair.getName();
                        // Additional non-JAR files are allowed
// START OF IASRI 4657979
                        if (name.endsWith(".jar") || name.endsWith(".zip")) {
// END OF IASRI 4657979
                            // There was more JARs
                            log.info("    Additional JARs have been added");
                            return (true);
                        }
                    }
                } else if (i < jarNames.length) {
                    // There was less JARs
                    log.info("    Additional JARs have been added");
                    return (true);
                }
            } catch (NamingException e) {
                if (log.isTraceEnabled())
                    log.trace("    Failed tracking modifications of '"
                        + getJarPath() + "'");
            } catch (ClassCastException e) {
                log.error("    Failed tracking modifications of '"
                          + getJarPath() + "' : " + e.getMessage());
            }

        }

        // No classes have been modified
        return (false);

    }


    /**
     * Render a String representation of this object.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("WebappClassLoader\r\n");
        sb.append("  delegate: ");
        sb.append(delegate);
        sb.append("\r\n");
        sb.append("  repositories:\r\n");
        if (repositories != null) {
            for (int i = 0; i < repositories.length; i++) {
                sb.append("    ");
                sb.append(repositories[i]);
                sb.append("\r\n");
            }
        }
        if (this.parent != null) {
            sb.append("----------> Parent Classloader:\r\n");
            sb.append(this.parent.toString());
            sb.append("\r\n");
        }
        return (sb.toString());

    }


    // ---------------------------------------------------- ClassLoader Methods


    /**
     * Find the specified class in our local repositories, if possible.  If
     * not found, throw <code>ClassNotFoundException</code>.
     *
     * @param name Name of the class to be loaded
     *
     * @exception ClassNotFoundException if the class was not found
     */
    public Class findClass(String name) throws ClassNotFoundException {

        if (log.isTraceEnabled())
            log.trace("    findClass(" + name + ")");

        // (1) Permission to define this class when using a SecurityManager
        // START PE 4989455
        //if (securityManager != null) {
        if (SecurityUtil.isPackageProtectionEnabled()){    
        // END PE 4989455
            int i = name.lastIndexOf('.');
            if (i >= 0) {
                try {
                    if (log.isTraceEnabled())
                        log.trace("      securityManager.checkPackageDefinition");
                    securityManager.checkPackageDefinition(name.substring(0,i));
                } catch (Exception se) {
                    if (log.isTraceEnabled())
                        log.trace("      -->Exception-->ClassNotFoundException", se);
                    throw new ClassNotFoundException(name, se);
                }
            }
        }

        // Ask our superclass to locate this class, if possible
        // (throws ClassNotFoundException if it is not found)
        Class clazz = null;
        try {
            if (log.isTraceEnabled())
                log.trace("      findClassInternal(" + name + ")");
            try {
                ResourceEntry entry = findClassInternal(name);
                // Create the code source object
                CodeSource codeSource =
                    new CodeSource(entry.codeBase, entry.certificates);
                synchronized (this) {
                    if (entry.loadedClass == null) {
                        /* START GlassFish [680]
                        clazz = defineClass(name, entry.binaryContent, 0,
                                entry.binaryContent.length, 
                                codeSource);
                        */
                        // START GlassFish [680]
                        // We use a temporary byte[] so that we don't change
                        // the content of entry in case bytecode
                        // preprocessing takes place.
                        byte[] binaryContent = entry.binaryContent;
                        if (!byteCodePreprocessors.isEmpty()) {
                            // ByteCodePreprpcessor expects name as
                            // java/lang/Object.class
                            String resourceName =
                                name.replace('.', '/') + ".class";
                            for(BytecodePreprocessor preprocessor : byteCodePreprocessors) {
                                binaryContent = preprocessor.preprocess(
                                    resourceName, binaryContent);
                            }
                        }
                        clazz = defineClass(name, binaryContent, 0,
                                binaryContent.length,
                                codeSource);
                        // END GlassFish [680]
                        entry.loadedClass = clazz;
                        entry.binaryContent = null;
                        entry.source = null;
                        entry.codeBase = null;
                        entry.manifest = null;
                        entry.certificates = null;
                    } else {
                        clazz = entry.loadedClass;
                    }
                }
            } catch(ClassNotFoundException cnfe) {
                if (!hasExternalRepositories) {
                    throw cnfe;
                }
            } catch (UnsupportedClassVersionError ucve) {
                throw new UnsupportedClassVersionError(
                    sm.getString("webappLoader.unsupportedVersion", name,
                                 getJavaVersion()));
            } catch(AccessControlException ace) {
                throw new ClassNotFoundException(name, ace);
            } catch (RuntimeException e) {
                if (log.isTraceEnabled())
                    log.trace("      -->RuntimeException Rethrown", e);
                throw e;
            }
            if ((clazz == null) && hasExternalRepositories) {
                try {
                    clazz = super.findClass(name);
                } catch(AccessControlException ace) {
                    throw new ClassNotFoundException(name, ace);
                } catch (RuntimeException e) {
                    if (log.isTraceEnabled())
                        log.trace("      -->RuntimeException Rethrown", e);
                    throw e;
                }
            }
            if (clazz == null) {
                if (log.isTraceEnabled())
                    log.trace("    --> Returning ClassNotFoundException");
                throw new ClassNotFoundException(name);
            }
        } catch (ClassNotFoundException e) {
            if (log.isTraceEnabled())
                log.trace("    --> Passing on ClassNotFoundException");
            throw e;
        }

        // Return the class we have located
        if (log.isTraceEnabled())
            log.trace("      Returning class " + clazz);
        if ((log.isTraceEnabled()) && (clazz != null))
            log.trace("      Loaded by " + clazz.getClassLoader());
        return (clazz);

    }


    /**
     * Find the specified resource in our local repository, and return a
     * <code>URL</code> refering to it, or <code>null</code> if this resource
     * cannot be found.
     *
     * @param name Name of the resource to be found
     */
    public URL findResource(final String name) {

        if (log.isTraceEnabled())
            log.trace("    findResource(" + name + ")");

        URL url = null;

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry == null) {
            entry = findResourceInternal(name, name);
        }
        if (entry != null) {
            url = entry.source;
        }

        if ((url == null) && hasExternalRepositories)
            url = super.findResource(name);

        if (log.isTraceEnabled()) {
            if (url != null)
                log.trace("    --> Returning '" + url.toString() + "'");
            else
                log.trace("    --> Resource not found, returning null");
        }
        return (url);

    }


    /**
     * Return an enumeration of <code>URLs</code> representing all of the
     * resources with the given name.  If no resources with this name are
     * found, return an empty enumeration.
     *
     * @param name Name of the resources to be found
     *
     * @exception IOException if an input/output error occurs
     */
    public Enumeration findResources(String name) throws IOException {

        if (log.isTraceEnabled())
            log.trace("    findResources(" + name + ")");

        Vector result = new Vector();

        int jarFilesLength = jarFiles.length;
        int repositoriesLength = repositories.length;

        int i;

        // Looking at the repositories
        for (i = 0; i < repositoriesLength; i++) {
            try {
                String fullPath = repositories[i] + name;
                resources.lookup(fullPath);
                // Note : Not getting an exception here means the resource was
                // found
                try {
                    result.addElement(getURI(new File(files[i], name)));
                } catch (MalformedURLException e) {
                    // Ignore
                }
            } catch (NamingException e) {
            }
        }

        // Looking at the JAR files
        synchronized (jarFiles) {
            if (openJARs()) {
                for (i = 0; i < jarFilesLength; i++) {
                    JarEntry jarEntry = jarFiles[i].getJarEntry(name);
                    if (jarEntry != null) {
                        try {
                            String jarFakeUrl = getURI(jarRealFiles[i]).toString();
                            jarFakeUrl = "jar:" + jarFakeUrl + "!/" + name;
                            result.addElement(new URL(jarFakeUrl));
                        } catch (MalformedURLException e) {
                            // Ignore
                        }
                    }
                }
            }
        }

        // Adding the results of a call to the superclass
        if (hasExternalRepositories) {

            Enumeration otherResourcePaths = super.findResources(name);

            while (otherResourcePaths.hasMoreElements()) {
                result.addElement(otherResourcePaths.nextElement());
            }

        }

        return result.elements();

    }


    /**
     * Find the resource with the given name.  A resource is some data
     * (images, audio, text, etc.) that can be accessed by class code in a
     * way that is independent of the location of the code.  The name of a
     * resource is a "/"-separated path name that identifies the resource.
     * If the resource cannot be found, return <code>null</code>.
     * <p>
     * This method searches according to the following algorithm, returning
     * as soon as it finds the appropriate URL.  If the resource cannot be
     * found, returns <code>null</code>.
     * <ul>
     * <li>If the <code>delegate</code> property is set to <code>true</code>,
     *     call the <code>getResource()</code> method of the parent class
     *     loader, if any.</li>
     * <li>Call <code>findResource()</code> to find this resource in our
     *     locally defined repositories.</li>
     * <li>Call the <code>getResource()</code> method of the parent class
     *     loader, if any.</li>
     * </ul>
     *
     * @param name Name of the resource to return a URL for
     */
    public URL getResource(String name) {

        if (log.isTraceEnabled())
            log.trace("getResource(" + name + ")");
        URL url = null;

        /*
         * (1) Delegate to parent if requested, or if the requested resource
         * belongs to one of the packages that are part of the Java EE platform
         */
        if (delegate
                || name.startsWith("javax")
                || name.startsWith("sun")
                || (name.startsWith("com/sun/faces")
                    && !name.startsWith("com/sun/faces/extensions"))
                || name.startsWith("org/apache/taglibs/standard")) {
            if (log.isTraceEnabled())
                log.trace("  Delegating to parent classloader " + parent);
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            url = loader.getResource(name);
            if (url != null) {
                if (log.isTraceEnabled())
                    log.trace("  --> Returning '" + url.toString() + "'");
                return (url);
            }
        }

        // (2) Search local repositories
        url = findResource(name);
        if (url != null) {
            // Locating the repository for special handling in the case 
            // of a JAR
            ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
            try {
                String repository = entry.codeBase.toString();
                if ((repository.endsWith(".jar"))
                        && !(name.endsWith(".class"))
                        && !(name.endsWith(".jar"))) {
                    // Copy binary content to the work directory if not present
                    File resourceFile = new File(loaderDir, name);
                    url = resourceFile.toURL();
                }
            } catch (Exception e) {
                // Ignore
            }
            if (log.isTraceEnabled())
                log.trace("  --> Returning '" + url.toString() + "'");
            return (url);
        }

        // (3) Delegate to parent unconditionally if not already attempted
        if( !delegate ) {
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            url = loader.getResource(name);
            if (url != null) {
                if (log.isTraceEnabled())
                    log.trace("  --> Returning '" + url.toString() + "'");
                return (url);
            }
        }

        // (4) Resource was not found
        if (log.isTraceEnabled())
            log.trace("  --> Resource not found, returning null");
        return (null);

    }


    /**
     * Find the resource with the given name, and return an input stream
     * that can be used for reading it.  The search order is as described
     * for <code>getResource()</code>, after checking to see if the resource
     * data has been previously cached.  If the resource cannot be found,
     * return <code>null</code>.
     *
     * @param name Name of the resource to return an input stream for
     */
    public InputStream getResourceAsStream(String name) {

        if (log.isTraceEnabled())
            log.trace("getResourceAsStream(" + name + ")");
        InputStream stream = null;

        // (0) Check for a cached copy of this resource
        stream = findLoadedResource(name);
        if (stream != null) {
            if (log.isTraceEnabled())
                log.trace("  --> Returning stream from cache");
            return (stream);
        }

        /*
         * (1) Delegate to parent if requested, or if the requested resource
         * belongs to one of the packages that are part of the Java EE platform
         */
        if (delegate
                || name.startsWith("javax")
                || name.startsWith("sun")
                || (name.startsWith("com/sun/faces")
                    && !name.startsWith("com/sun/faces/extensions"))
                || name.startsWith("org/apache/taglibs/standard")) {
            if (log.isTraceEnabled())
                log.trace("  Delegating to parent classloader " + parent);
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            stream = loader.getResourceAsStream(name);
            if (stream != null) {
                // FIXME - cache???
                if (log.isTraceEnabled())
                    log.trace("  --> Returning stream from parent");
                return (stream);
            }
        }

        // (2) Search local repositories
        if (log.isTraceEnabled())
            log.trace("  Searching local repositories");
        URL url = findResource(name);
        if (url != null) {
            // FIXME - cache???
            if (log.isTraceEnabled())
                log.trace("  --> Returning stream from local");
            stream = findLoadedResource(name);
            try {
                if (hasExternalRepositories && (stream == null))
                    stream = url.openStream();
            } catch (IOException e) {
                ; // Ignore
            }
            if (stream != null)
                return (stream);
        }

        // (3) Delegate to parent unconditionally
        if (!delegate) {
            if (log.isTraceEnabled())
                log.trace("  Delegating to parent classloader unconditionally " + parent);
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            stream = loader.getResourceAsStream(name);
            if (stream != null) {
                // FIXME - cache???
                if (log.isTraceEnabled())
                    log.trace("  --> Returning stream from parent");
                return (stream);
            }
        }

        // (4) Resource was not found
        if (log.isTraceEnabled())
            log.trace("  --> Resource not found, returning null");
        return (null);

    }


    /**
     * Load the class with the specified name.  This method searches for
     * classes in the same manner as <code>loadClass(String, boolean)</code>
     * with <code>false</code> as the second argument.
     *
     * @param name Name of the class to be loaded
     *
     * @exception ClassNotFoundException if the class was not found
     */
    public Class loadClass(String name) throws ClassNotFoundException {

        if (log.isTraceEnabled())
            log.trace("loadClass(" + name + ")");

        Class clazz = null;

        // Don't load classes if class loader is stopped
        if (!started) {
            log.info(sm.getString("webappClassLoader.stopped"));
            throw new ThreadDeath(); 
        }

        // (0) Check our previously loaded local class cache
        clazz = findLoadedClass0(name);
        if (clazz != null) {
            if (log.isTraceEnabled())
                log.trace("  Returning class from cache");
            return (clazz);
        }

        // (0.1) Check our previously loaded class cache
        clazz = findLoadedClass(name);
        if (clazz != null) {
            if (log.isTraceEnabled())
                log.trace("  Returning class from cache");
            return (clazz);
        }

        // START PE 4985680
        // (0.2) Try loading the class with the system class loader, to prevent
        //       the webapp from overriding J2SE classes
        /*
        try {
            clazz = system.loadClass(name);
            if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        */
        // END PE 4985680

        // (0.5) Permission to access this class when using a SecurityManager
        // START PE 4989455
        //if (securityManager != null) {
        if (SecurityUtil.isPackageProtectionEnabled()){    
        // END PE 4989455
            int i = name.lastIndexOf('.');
            if (i >= 0) {
                try {
                    securityManager.checkPackageAccess(name.substring(0,i));
                } catch (SecurityException se) {
                    String error = "Security Violation, attempt to use " +
                        "Restricted Class: " + name;
                    log.info(error, se);
                    throw new ClassNotFoundException(error, se);
                }
            }
        }

        // START PE 4985680
        // boolean delegateLoad = delegate ||filter(name);
        boolean delegateLoad = delegate;
        // END PE 4985680

        // (1) Delegate to our parent if requested
        if (delegateLoad) {
            if (log.isTraceEnabled())
                log.trace("  Delegating to parent classloader1 " + parent);
            ClassLoader loader = parent;
            if (loader == null) 
                loader = system;

            // START PE 4985680
            try {
                clazz = loader.loadClass(name);
                if (clazz != null) {
                    if (log.isTraceEnabled())
                        log.trace("  Loading class from parent");
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                ;
            }
            // END PE 4985680    
        }

        // (2) Search local repositories
        // START PE 4985680
        boolean filterCoreClasses = filter(name);
        if ( !filterCoreClasses ) {
            // (2) Search local repositories
        // END PE 4985680    
            if (log.isTraceEnabled())
                log.trace("  Searching local repositories");
            try {
                clazz = findClass(name);
                if (clazz != null) {
                    if (log.isTraceEnabled())
                        log.trace("  Loading class from local repository");
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                ;
            }
        }

        // START PE 4985680
        // (3) Delegate to parent unconditionally
        /*
        if (!delegateLoad) {
            if (log.isTraceEnabled())
                log.trace("  Delegating to parent classloader at end: " + parent);
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = loader.loadClass(name);
        */

        // (3) Delegate to system unconditionally
        if (log.isTraceEnabled())
            log.trace("  Delegating to system classloader at end: " + parent);

        try {
            clazz = system.loadClass(name);
        // END PE 4985680
            if (clazz != null) {
                if (log.isTraceEnabled())
                // START PE 4985680
                    //log.debug("  Loading class from parent");
                    log.trace("  Loading class from system");
                // END PE 4985680
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            ;
        }

        // START PE 4985680
        // (4) Delegate to parent finally if the class wasn't found 
        try {
            if (log.isTraceEnabled())
                log.trace("  Delegating to parent classloader " + parent);
            ClassLoader loader = parent;
            if (loader != null) {

                clazz = parent.loadClass(name);
                if (log.isTraceEnabled())
                    log.trace("  Loading class from parent");
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            ;
        }

        // if filter(..) return true and if we did not find
        // the class using the parent/system classloader,
        // then give a chance to this classloader.
        if ( filterCoreClasses ) {
            if (log.isTraceEnabled())
                log.trace("  Searching local repositories");
            clazz = findClass(name);
            if (clazz != null) {
                if (log.isTraceEnabled())
                    log.trace("  Loading class from local repository");
                return (clazz);
            }
        }
        // END PE 4985680

        throw new ClassNotFoundException(name);
    }


    /**
     * Load the class with the specified name, searching using the following
     * algorithm until it finds and returns the class.  If the class cannot
     * be found, returns <code>ClassNotFoundException</code>.
     * <ul>
     * <li>Call <code>findLoadedClass(String)</code> to check if the
     *     class has already been loaded.  If it has, the same
     *     <code>Class</code> object is returned.</li>
     * <li>If the <code>delegate</code> property is set to <code>true</code>,
     *     call the <code>loadClass()</code> method of the parent class
     *     loader, if any.</li>
     * <li>Call <code>findClass()</code> to find this class in our locally
     *     defined repositories.</li>
     * <li>Call the <code>loadClass()</code> method of our parent
     *     class loader, if any.</li>
     * </ul>
     * If the class was found using the above steps, and the
     * <code>resolve</code> flag is <code>true</code>, this method will then
     * call <code>resolveClass(Class)</code> on the resulting Class object.
     *
     * @param name Name of the class to be loaded
     * @param resolve If <code>true</code> then resolve the class
     *
     * @exception ClassNotFoundException if the class was not found
     */
    public Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException {

        Class clazz = loadClass(name);
        if (clazz != null) {
            if (resolve) {
                resolveClass(clazz);
            }
            return (clazz);
        }

        throw new ClassNotFoundException(name);
    }


    /**
     * Get the Permissions for a CodeSource.  If this instance
     * of WebappClassLoader is for a web application context,
     * add read FilePermission or JndiPermissions for the base
     * directory (if unpacked),
     * the context URL, and jar file resources.
     *
     * @param CodeSource where the code was loaded from
     * @return PermissionCollection for CodeSource
     */
    protected PermissionCollection getPermissions(CodeSource codeSource) {

        String codeUrl = codeSource.getLocation().toString();
        PermissionCollection pc;
        if ((pc = (PermissionCollection)loaderPC.get(codeUrl)) == null) {
            pc = super.getPermissions(codeSource);
            if (pc != null) {
                Iterator perms = permissionList.iterator();
                while (perms.hasNext()) {
                    Permission p = (Permission)perms.next();
                    pc.add(p);
                }
                loaderPC.put(codeUrl,pc);
            }
        }
        return (pc);

    }


    /**
     * Returns the search path of URLs for loading classes and resources.
     * This includes the original list of URLs specified to the constructor,
     * along with any URLs subsequently appended by the addURL() method.
     * @return the search path of URLs for loading classes and resources.
     */
    public URL[] getURLs() {

        if (repositoryURLs != null) {
            return repositoryURLs;
        }

        URL[] external = super.getURLs();

        int filesLength = files.length;
        int jarFilesLength = jarRealFiles.length;
        int length = filesLength + jarFilesLength + external.length;
        int i;

        try {

            URL[] urls = new URL[length];
            for (i = 0; i < length; i++) {
                if (i < filesLength) {
                    urls[i] = getURL(files[i]);
                } else if (i < filesLength + jarFilesLength) {
                    urls[i] = getURL(jarRealFiles[i - filesLength]);
                } else {
                    urls[i] = external[i - filesLength - jarFilesLength];
                }
            }

            repositoryURLs = urls;

        } catch (MalformedURLException e) {
            repositoryURLs = new URL[0];
        }

        return repositoryURLs;

    }


    // ------------------------------------------------------ Lifecycle Methods


    private void init() {

        this.parent = getParent();

        /* SJSAS 6317864
        system = getSystemClassLoader();
        */
        // START SJSAS 6317864
        system = this.getClass().getClassLoader();
        // END SJSAS 6317864
        securityManager = System.getSecurityManager();

        if (securityManager != null) {
            refreshPolicy();
        }

        addOverridablePackage("com.sun.faces.extensions");
    }


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {
    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this 
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {
        return new LifecycleListener[0];
    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {
    }


    /**
     * Start the class loader.
     *
     * @exception LifecycleException if a lifecycle error occurs
     */
    public void start() throws LifecycleException {

        started = true;

    }


    /**
     * Stop the class loader.
     *
     * @exception LifecycleException if a lifecycle error occurs
     */
    public void stop() throws LifecycleException {

        // START GlassFish Issue 587
        purgeELBeanClasses();
        // END GlassFish Issue 587

        // Clearing references should be done before setting started to
        // false, due to possible side effects
        clearReferences();

        // START SJSAS 6258619
        ClassLoaderUtil.releaseLoader(this);
        // END SJSAS 6258619

        // START GlassFish 2840
        Provider[] providers = Security.getProviders();
        if (providers != null) {
            for (Provider provider : providers) {
                if (provider.getClass().getClassLoader() == this) {
                    Security.removeProvider(provider.getName());
                }
            }
        }
        // END GlassFish 2840

        started = false;

        int length = files.length;
        for (int i = 0; i < length; i++) {
            files[i] = null;
        }

        length = jarFiles.length;
        for (int i = 0; i < length; i++) {
            try {
                if (jarFiles[i] != null) {
                    jarFiles[i].close();
                }
            } catch (IOException e) {
                // Ignore
            }
            jarFiles[i] = null;
        }

        notFoundResources.clear();
        resourceEntries.clear();
        resources = null;
        repositories = null;
        repositoryURLs = null;
        files = null;
        jarFiles = null;
        jarRealFiles = null;
        jarPath = null;
        jarNames = null;
        lastModifiedDates = null;
        paths = null;
        hasExternalRepositories = false;
        parent = null;

        permissionList.clear();
        loaderPC.clear();

        if (loaderDir != null) {
            deleteDir(loaderDir);
        }

    }


    /**
     * Used to periodically signal to the classloader to release 
     * JAR resources.
     */
    public void closeJARs(boolean force) {
        if (jarFiles.length > 0) {
            synchronized (jarFiles) {
                if (force || (System.currentTimeMillis() 
                              > (lastJarAccessed + 90000))) {
                    for (int i = 0; i < jarFiles.length; i++) {
                        try {
                            if (jarFiles[i] != null) {
                                jarFiles[i].close();
                                jarFiles[i] = null;
                            }
                        } catch (IOException e) {
                            if (log.isDebugEnabled()) {
                                log.warn("Failed to close JAR", e);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Clear references.
     */
    protected void clearReferences() {

        // Unregister any JDBC drivers loaded by this classloader
        Enumeration drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = (Driver) drivers.nextElement();
            if (driver.getClass().getClassLoader() == this) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    log.warn("SQL driver deregistration failed", e);
                }
            }
        }
        
        // Null out any static or final fields from loaded classes,
        // as a workaround for apparent garbage collection bugs
        Iterator loadedClasses = ((HashMap) resourceEntries.clone()).values().
                                    iterator();
        while (loadedClasses.hasNext()) {
            ResourceEntry entry = (ResourceEntry) loadedClasses.next();
            if (entry.loadedClass != null) {
                Class clazz = entry.loadedClass;
                try {
                    Field[] fields = clazz.getDeclaredFields();
                    for (int i = 0; i < fields.length; i++) {
                        Field field = fields[i];
                        int mods = field.getModifiers();
                        if (field.getType().isPrimitive() 
                                || (field.getName().indexOf("$") != -1)) {
                            continue;
                        }
                        if (Modifier.isStatic(mods)) {
                            try {
                                field.setAccessible(true);
                                if (Modifier.isFinal(mods)) {
                                    if (!((field.getType().getName().startsWith("java."))
                                            || (field.getType().getName().startsWith("javax.")))) {
                                        nullInstance(field.get(null));
                                    }
                                } else {
                                    field.set(null, null);
                                    if (log.isDebugEnabled()) {
                                        log.debug("Set field " + field.getName() 
                                                + " to null in class " + clazz.getName());
                                    }
                                }
                            } catch (Throwable t) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Could not set field " + field.getName() 
                                            + " to null in class " + clazz.getName(), t);
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    if (log.isDebugEnabled()) {
                        log.debug("Could not clean fields for class " + clazz.getName(), t);
                    }
                }
            }
        }

        // START SJSAS 6390584
        com.sun.org.apache.commons.beanutils.PropertyUtils.clearDescriptors();
        // END SJSAS 6390584
        
        // Clear the IntrospectionUtils cache.
        IntrospectionUtils.clear();
       
        MappedPropertyDescriptor.clear();

        // START S1AS 5032338
        //com.sun.org.apache.commons.logging.LogFactory.release(this);
        // Clear the classloader reference in commons-logging.
        LogFactory.release(this);
        // END S1AS 5032338
        
        // Clear the classloader reference in the VM's bean introspector
        java.beans.Introspector.flushCaches();

    }


    protected void nullInstance(Object instance) {
        if (instance == null) {
            return;
        }
        Field[] fields = instance.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            int mods = field.getModifiers();
            if (field.getType().isPrimitive() 
                    || (field.getName().indexOf("$") != -1)) {
                continue;
            }
            try {
                field.setAccessible(true);
                if (Modifier.isStatic(mods) && Modifier.isFinal(mods)) {
                    // Doing something recursively is too risky
                    continue;
                } else {
                    Object value = field.get(instance);
                    if (null != value) {
                        Class valueClass = value.getClass();
                        if (!loadedByThisOrChild(valueClass)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Not setting field " + field.getName() +
                                        " to null in object of class " + 
                                        instance.getClass().getName() +
                                        " because the referenced object was of type " +
                                        valueClass.getName() + 
                                        " which was not loaded by this WebappClassLoader.");
                            }
                        } else {
                            field.set(instance, null);
                            if (log.isDebugEnabled()) {
                                log.debug("Set field " + field.getName() 
                                        + " to null in class " + instance.getClass().getName());
                        }
                    }
                }
                }
            } catch (Throwable t) {
                if (log.isDebugEnabled()) {
                    log.debug("Could not set field " + field.getName() 
                            + " to null in object instance of class " 
                            + instance.getClass().getName(), t);
                }
            }
        }
    }
    

    /**
     * Determine whether a class was loaded by this class loader or one of
     * its child class loaders.
     */
    protected boolean loadedByThisOrChild(Class clazz) {
        boolean result = false;
        for (ClassLoader classLoader = clazz.getClassLoader();
                null != classLoader; classLoader = classLoader.getParent()) {
            if (classLoader.equals(this)) {
                result = true;
                break;
            }
        }
        return result;
    }  
    // ------------------------------------------------------ Protected Methods


    /**
     * Used to periodically signal to the classloader to release JAR resources.
     */
    protected boolean openJARs() {
        if (started && (jarFiles.length > 0)) {
            lastJarAccessed = System.currentTimeMillis();
            if (jarFiles[0] == null) {
                for (int i = 0; i < jarFiles.length; i++) {
                    try {
                        jarFiles[i] = new JarFile(jarRealFiles[i]);
                    } catch (IOException e) {
                        if (log.isDebugEnabled()) {
                            log.warn("Failed to open JAR", e);
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Find specified class in local repositories.
     *
     * @return the loaded class, or null if the class isn't found
     */
    protected ResourceEntry findClassInternal(String name)
        throws ClassNotFoundException {

        if (!validate(name))
            throw new ClassNotFoundException(name);

        String tempPath = name.replace('.', '/');
        String classPath = tempPath + ".class";

        ResourceEntry entry = findResourceInternal(name, classPath);

        if (entry == null)
               throw new ClassNotFoundException(name);

        Class clazz = entry.loadedClass;
        if (clazz != null)
            return entry;

        synchronized (this) {
            if (entry.binaryContent == null && entry.loadedClass == null)
                throw new ClassNotFoundException(name);
        }

        // Looking up the package
        String packageName = null;
        int pos = name.lastIndexOf('.');
        if (pos != -1)
            packageName = name.substring(0, pos);

        Package pkg = null;

        if (packageName != null) {

// START OF IASRI 4717252
          synchronized (loaderPC) {
// END OF IASRI 4717252
            pkg = getPackage(packageName);

            // Define the package (if null)
            if (pkg == null) {
                if (entry.manifest == null) {
                    definePackage(packageName, null, null, null, null, null,
                                  null, null);
                } else {
                    definePackage(packageName, entry.manifest, entry.codeBase);
                }
            }
// START OF IASRI 4717252
          }
// END OF IASRI 4717252
        }

        if (securityManager != null) {

            // Checking sealing
            if (pkg != null) {
                boolean sealCheck = true;
                if (pkg.isSealed()) {
                    sealCheck = pkg.isSealed(entry.codeBase);
                } else {
                    sealCheck = (entry.manifest == null)
                        || !isPackageSealed(packageName, entry.manifest);
                }
                if (!sealCheck)
                    throw new SecurityException
                        ("Sealing violation loading " + name + " : Package "
                         + packageName + " is sealed.");
            }

        }

        return entry;

    }

    /**
     * Find specified resource in local repositories. This block
     * will execute under an AccessControl.doPrivilege block.
     *
     * @return the loaded resource, or null if the resource isn't found
     */
    private ResourceEntry findResourceInternal(File file, String path){
        ResourceEntry entry = new ResourceEntry();
        try {
            entry.source = getURI(new File(file, path));
            entry.codeBase = getURL(new File(file, path));
        } catch (MalformedURLException e) {
            return null;
        }   
        return entry;
    }
    

    /**
     * Attempts to find the specified resource in local repositories.
     *
     * @return the loaded resource, or null if the resource isn't found
     */
    protected ResourceEntry findResourceInternal(String name, String path) {

        if (!started) {
            log.info(sm.getString("webappClassLoader.stopped"));
            return null;
        }

        if ((name == null) || (path == null)) {
            return null;
        }

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry != null) {
            return entry;
        } else if (notFoundResources.containsKey(name)) {
            return null;
        }

        entry = findResourceInternalFromRepositories(name, path);
        if (entry == null) {
            synchronized (jarFiles) {
                entry = findResourceInternalFromJars(name, path);
            }
        }

        if (entry == null) {
            synchronized (notFoundResources) {
                notFoundResources.put(name, name);
            }
            return null;
        }

        // Add the entry in the local resource repository
        synchronized (resourceEntries) {
            // Ensures that all the threads which may be in a race to load
            // a particular class all end up with the same ResourceEntry
            // instance
            ResourceEntry entry2 = (ResourceEntry) resourceEntries.get(name);
            if (entry2 == null) {
                resourceEntries.put(name, entry);
            } else {
                entry = entry2;
            }
        }

        return entry;
    }


    /**
     * Attempts to load the requested resource from this classloader's
     * internal repositories.
     *
     * @return The requested resource, or null if not found
     */
    private ResourceEntry findResourceInternalFromRepositories(String name,
                                                               String path) {

        ResourceEntry entry = null;
        int contentLength = -1;
        InputStream binaryStream = null;
        int repositoriesLength = repositories.length;
        Resource resource = null;

        for (int i=0; (entry == null) && (i < repositoriesLength); i++) {

            try {

                String fullPath = repositories[i] + path;

                Object lookupResult = resources.lookup(fullPath);
                if (lookupResult instanceof Resource) {
                    resource = (Resource) lookupResult;
                }

                // Note : Not getting an exception here means the resource was
                // found
                 if (securityManager != null) {
                    PrivilegedAction dp =
                        new PrivilegedFindResource(files[i], path);
                    entry = (ResourceEntry)AccessController.doPrivileged(dp);
                 } else {
                    entry = findResourceInternal(files[i], path);
                 }

                ResourceAttributes attributes =
                    (ResourceAttributes) resources.getAttributes(fullPath);
                contentLength = (int) attributes.getContentLength();
                entry.lastModified = attributes.getLastModified();

                if (resource != null) {

                    try {
                        binaryStream = resource.streamContent();
                    } catch (IOException e) {
                        return null;
                    }

                    // Register the full path for modification checking
                    // Note: Only syncing on a 'constant' object is needed
                    synchronized (allPermission) {

                        int j;

                        long[] result2 = 
                            new long[lastModifiedDates.length + 1];
                        for (j = 0; j < lastModifiedDates.length; j++) {
                            result2[j] = lastModifiedDates[j];
                        }
                        result2[lastModifiedDates.length] = entry.lastModified;
                        lastModifiedDates = result2;

                        String[] result = new String[paths.length + 1];
                        for (j = 0; j < paths.length; j++) {
                            result[j] = paths[j];
                        }
                        result[paths.length] = fullPath;
                        paths = result;

                    }
                }
            } catch (NamingException e) {
            }
        }

        if (entry != null) {
            readEntryData(entry, name, binaryStream, contentLength, null);
        }

        return entry;
    }


    /**
     * Attempts to load the requested resource from this classloader's
     * JAR files.
     *
     * @return The requested resource, or null if not found
     */
    private ResourceEntry findResourceInternalFromJars(String name,
                                                       String path) {

        ResourceEntry entry = null;
        JarEntry jarEntry = null;
        int contentLength = -1;
        InputStream binaryStream = null;

        if (!openJARs()){
            return null;
        }

        int jarFilesLength = jarFiles.length;

        for (int i=0; (entry == null) && (i < jarFilesLength); i++) {
            jarEntry = jarFiles[i].getJarEntry(path);

            if (jarEntry != null) {

                entry = new ResourceEntry();
                try {
                    entry.codeBase = getURL(jarRealFiles[i]);
                    String jarFakeUrl = getURI(jarRealFiles[i]).toString();
                    jarFakeUrl = "jar:" + jarFakeUrl + "!/" + path;
                    entry.source = new URL(jarFakeUrl);
                    entry.lastModified = jarRealFiles[i].lastModified();
                } catch (MalformedURLException e) {
                    return null;
                }

                contentLength = (int) jarEntry.getSize();
                try {
                    entry.manifest = jarFiles[i].getManifest();
                    binaryStream = jarFiles[i].getInputStream(jarEntry);
                } catch (IOException e) {
                    return null;
                }

                // Extract resources contained in JAR to the workdir
                if (!(path.endsWith(".class"))) {
                    byte[] buf = new byte[1024];
                    File resourceFile = new File
                        (loaderDir, jarEntry.getName());
                    if (!resourceFile.exists()) {
                        Enumeration entries = jarFiles[i].entries();
                        while (entries.hasMoreElements()) {
                            JarEntry jarEntry2 = 
                                (JarEntry) entries.nextElement();
                            if (!(jarEntry2.isDirectory()) 
                                && (!jarEntry2.getName().endsWith(".class"))) {
                                resourceFile = new File
                                    (loaderDir, jarEntry2.getName());
                                resourceFile.getParentFile().mkdirs();
                                FileOutputStream os = null;
                                InputStream is = null;
                                try {
                                    is = jarFiles[i].getInputStream(jarEntry2);
                                    os = new FileOutputStream(resourceFile);
                                    while (true) {
                                        int n = is.read(buf);
                                        if (n <= 0) {
                                            break;
                                        }
                                        os.write(buf, 0, n);
                                    }
                                } catch (IOException e) {
                                    // Ignore
                                } finally {
                                    try {
                                        if (is != null) {
                                            is.close();
                                        }
                                    } catch (IOException e) {
                                    }
                                    try {
                                        if (os != null) {
                                            os.close();
                                        }
                                    } catch (IOException e) {
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (entry != null) {
            readEntryData(entry, name, binaryStream, contentLength, jarEntry);
        }

        return entry;
    }


    /**
     * Reads the resource's binary data from the given input stream.
     */
    private void readEntryData(ResourceEntry entry,
                               String name,
                               InputStream binaryStream,
                               int contentLength,
                               JarEntry jarEntry) {

        if (binaryStream == null) {
            return;
        }

        byte[] binaryContent = new byte[contentLength];

        try {
            int pos = 0;

            while (true) {
                int n = binaryStream.read(binaryContent, pos,
                                          binaryContent.length - pos);
                if (n <= 0)
                    break;
                pos += n;
            }
            binaryStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // START OF IASRI 4709374
        // Preprocess the loaded byte code if bytecode preprocesser is
        // enabled
        if (PreprocessorUtil.isPreprocessorEnabled()) {
            binaryContent =
                PreprocessorUtil.processClass(name, binaryContent);
        }
        // END OF IASRI 4709374

        entry.binaryContent = binaryContent;

        // The certificates are only available after the JarEntry 
        // associated input stream has been fully read
        if (jarEntry != null) {
            entry.certificates = jarEntry.getCertificates();
        }
    }


    /**
     * Returns true if the specified package name is sealed according to the
     * given manifest.
     */
    protected boolean isPackageSealed(String name, Manifest man) {

        String path = name + "/";
        Attributes attr = man.getAttributes(path);
        String sealed = null;
        if (attr != null) {
            sealed = attr.getValue(Name.SEALED);
        }
        if (sealed == null) {
            if ((attr = man.getMainAttributes()) != null) {
                sealed = attr.getValue(Name.SEALED);
            }
        }
        return "true".equalsIgnoreCase(sealed);

    }


    /**
     * Finds the resource with the given name if it has previously been
     * loaded and cached by this class loader, and return an input stream
     * to the resource data.  If this resource has not been cached, return
     * <code>null</code>.
     *
     * @param name Name of the resource to return
     */
    protected InputStream findLoadedResource(String name) {

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry != null) {
            if (entry.binaryContent != null)
                return new ByteArrayInputStream(entry.binaryContent);
        }
        return (null);

    }


    /**
     * Finds the class with the given name if it has previously been
     * loaded and cached by this class loader, and return the Class object.
     * If this class has not been cached, return <code>null</code>.
     *
     * @param name Name of the resource to return
     */
    protected Class findLoadedClass0(String name) {

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry != null) {
            return entry.loadedClass;
        }
        return (null);  // FIXME - findLoadedResource()

    }


    /**
     * Refresh the system policy file, to pick up eventual changes.
     */
    protected void refreshPolicy() {

        try {
            // The policy file may have been modified to adjust 
            // permissions, so we're reloading it when loading or 
            // reloading a Context
            Policy policy = Policy.getPolicy();
            policy.refresh();
        } catch (AccessControlException e) {
            // Some policy files may restrict this, even for the core,
            // so this exception is ignored
        }

    }


    /**
     * Filter classes.
     * 
     * @param name class name
     * @return true if the class should be filtered
     */
    protected boolean filter(String name) {

        if (name == null)
            return false;

        // START PE 4985680
        // Special case for performance reason.
        if (name.startsWith("java."))
            return true;
        // END PE 4985680

        // Looking up the package
        String packageName = null;
        int pos = name.lastIndexOf('.');
        if (pos != -1)
            packageName = name.substring(0, pos);
        else
            return false;
        
        if (overridablePackages != null){
            for (int i = 0; i < overridablePackages.size(); i++) {
                if (packageName.
                        startsWith((String)overridablePackages.get(i)))
                    return false;
            }
        }

        for (int i = 0; i < packageTriggers.length; i++) {
            if (packageName.startsWith(packageTriggers[i]))
                return true;
        }

        return false;

    }


    /**
     * Validate a classname. As per SRV.9.7.2, we must restict loading of 
     * classes from J2SE (java.*) and classes of the servlet API 
     * (javax.servlet.*). That should enhance robustness and prevent a number
     * of user error (where an older version of servlet.jar would be present
     * in /WEB-INF/lib).
     * 
     * @param name class name
     * @return true if the name is valid
     */
    protected boolean validate(String name) {

        if (name == null)
            return false;
        if (name.startsWith("java."))
            return false;

        return true;

    }


    /**
     * Check the specified JAR file, and return <code>true</code> if it does
     * not contain any of the trigger classes.
     *
     * @param jarFile The JAR file to be checked
     *
     * @exception IOException if an input/output error occurs
     */
    private boolean validateJarFile(File jarfile)
        throws IOException {

        if (triggers == null)
            return (true);
        JarFile jarFile = new JarFile(jarfile);
        for (int i = 0; i < triggers.length; i++) {
            Class clazz = null;
            try {
                if (parent != null) {
                    clazz = parent.loadClass(triggers[i]);
                } else {
                    clazz = Class.forName(triggers[i]);
                }
            } catch (Throwable t) {
                clazz = null;
            }
            if (clazz == null)
                continue;
            String name = triggers[i].replace('.', '/') + ".class";
            if (log.isTraceEnabled())
                log.trace(" Checking for " + name);
            JarEntry jarEntry = jarFile.getJarEntry(name);
            if (jarEntry != null) {
                log.info("validateJarFile(" + jarfile + 
                    ") - jar not loaded. See Servlet Spec 2.3, "
                    + "section 9.7.2. Offending class: " + name);
                jarFile.close();
                return (false);
            }
        }
        jarFile.close();
        return (true);

    }


    /**
     * Get URL.
     */
    protected URL getURL(File file)
        throws MalformedURLException {

        File realFile = file;
        try {
            realFile = realFile.getCanonicalFile();
        } catch (IOException e) {
            // Ignore
        }
        return realFile.toURL();

    }


    /**
     * Get URL.
     */
    protected URL getURI(File file)
        throws MalformedURLException {

        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
            // Ignore
        }

        return file.toURI().toURL();

    }


    /**
     * Delete the specified directory, including all of its contents and
     * subdirectories recursively.
     *
     * @param dir File object representing the directory to be deleted
     */
    protected static void deleteDir(File dir) {

        String files[] = dir.list();
        if (files == null) {
            files = new String[0];
        }
        for (int i = 0; i < files.length; i++) {
            File file = new File(dir, files[i]);
            if (file.isDirectory()) {
                deleteDir(file);
            } else {
                file.delete();
            }
        }
        dir.delete();

    }

    // START SJSAS 6344989
    public void addByteCodePreprocessor(BytecodePreprocessor preprocessor) {
        byteCodePreprocessors.add(preprocessor);
    }
    // END SJSAS 6344989


    // START GlassFish Issue 587
    /*
     * Purges all bean classes that were loaded by this WebappClassLoader
     * from the caches maintained by javax.el.BeanELResolver, in order to
     * avoid this WebappClassLoader from leaking.
     */
    private void purgeELBeanClasses() {

        Field fieldlist[] = javax.el.BeanELResolver.class.getDeclaredFields();
        for (int i = 0; i < fieldlist.length; i++) {
            Field fld = fieldlist[i];
            if (fld.getName().equals("properties")) {
                purgeELBeanClasses(fld);
            }
        }
    }

    /*
     * Purges all bean classes that were loaded by this WebappClassLoader
     * from the cache represented by the given reflected field.
     *
     * @param fld The reflected field from which to remove the bean classes
     * that were loaded by this WebappClassLoader
     */
    private void purgeELBeanClasses(final Field fld) {

	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
            AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        fld.setAccessible(true);
                        return null;
                    }
            });
        } else {
            fld.setAccessible(true);
        }

        Map m = null;
        try {
            m = (Map) fld.get(null);
        } catch (IllegalAccessException iae) {
            log.warn("Unable to purge bean classes from BeanELResolver", iae);
            return;
        }

        if (m.size() == 0) {
            return;
        }

        Iterator<Class> iter = m.keySet().iterator();
        while (iter.hasNext()) {
            Class mbeanClass = iter.next();
            if (this.equals(mbeanClass.getClassLoader())) {
                iter.remove();
            }    
        }
    }
    // END GlassFish Issue 587


    private String getJavaVersion() {

        String version = null;

	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
            version = (String) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return System.getProperty("java.version");
                    }
            });
        } else {
            version = System.getProperty("java.version");
        }

        return version;
    }

}

