/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.glassfish.web.loader.util;

//import com.sun.enterprise.server.PELaunch;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <code>ASURLClassLoader</code> represents a individual component
 * in the <code>ASClassLoaderChain</code>.
 * 
 * Since it is a subclass of <code>URLClassLoader</code> it can contain a 
 * search path of one or more URLs 
 * 
 * @author Harsha RA, Sivakumar Thyagarajan
 */

public class ASURLClassLoader extends URLClassLoader {
    /** List of URLs that this classloader checks for a class */
    private URL[] classLoaderList = new URL[]{}; 
    /** Name of this classloader - primarily used in the string 
        representation of the classloader - for debugging */
    private String nameOfCL = null;
    /** The classloader chain this classloader is a component of */
    private ClassLoader parentChain = null;
    /** toString() representation of parent - to prevent recursion */
    private String parentToString = null;
    
    //A local cache for keeping track of all classes that were not found 
    //using this classloader
    private Set<String> notFoundClasses = 
        Collections.synchronizedSet(new HashSet<String>());
    
    //A local cache for keeping track of all classes that were loaded 
    //using this classloader
    private java.util.Hashtable<String,Class> loadedClasses =
        new java.util.Hashtable<String,Class>();
    
/*
    public ASURLClassLoader(URL[] urls) {
        super(urls); //Parent: System ClassLoader.
        this.classLoaderList = urls;
    }
*/

    public ASURLClassLoader(URL[] urls, ClassLoader parent) {
        //The parent of the classloader chain component
        //is either the SystemClassLoader (if shared chain is null!) 
        //or the Shared Chain.
        //super(urls, PELaunch.getSharedChain());
        super(urls, ASClassLoaderUtil.getSharedChain()!=null ?ASClassLoaderUtil.getSharedChain()
                : ClassLoader.getSystemClassLoader());
        this.parentChain = parent;
        
        if(this.parentChain instanceof ClassLoaderChain) {
            //Computed earlier to prevent recursion in toString of this 
            //class
            this.parentToString = 
                         ((ClassLoaderChain)this.parentChain).getName();
        } else {
            this.parentToString = this.parentChain.toString();
        }
        this.classLoaderList = urls;
    }
    
    public void setName(String n) {
        this.nameOfCL = n;
    }
    
    public String getName() {
        return this.nameOfCL;
    }
    
   /**
    * The loading strategy is:
    * - check self first and
    * - then delegate to parent chain if one exists. This is done to prevent recursion
    *   while referring to the chain. One issue with this approach is that when class load
    *   requests come via this component classloader, it might override a duplicate class in 
    *   a peer present earlier in the chain. 
    */
    @Override
    public Class<?> loadClass(String name, boolean resolve) 
                                            throws ClassNotFoundException {
        try {
            //perf optimization
            //check notFoundClasses cache first
            if (notFoundClasses.contains(name)) {
                throw new ClassNotFoundException(name);
            }
            
            Class cls = null;
            
            //perf optimization
            //check loadedClasses cache first
            cls = loadedClasses.get(name);
            if (cls != null) {
                return cls;
            }

            //Hands over to URLClassLoader.loadClass. First attempt to delegate
            //to parent and then check for the class in the 
            //<code>classLoaderList</code>.
            cls = super.loadClass(name, resolve);
            if (cls != null) {
                loadedClasses.put(name, cls);
                return cls;
            }
            //Class not in this chain component.
            throw new ClassNotFoundException(name);
        } catch(ClassNotFoundException e) {
            //If the parent is a ClassLoaderChain, check in the ClassLoaderChain
            //ignoring this classloader component instance. Failure to do this 
            //would result in an infinte recursive call.
            if ((parentChain != null) && 
                             (parentChain instanceof ClassLoaderChain)) {
                ClassLoaderChain chain = (ClassLoaderChain)parentChain;
                //Ask parent chain to load class, skipping self
                Class c = chain.loadClass(name,this);
                //Resolve if required.
                if (c != null) {
                    if(resolve) {
                        resolveClass(c);
                    }
                    loadedClasses.put(name, c);
                    return c;
                }
            }
            
            //perf optimization
            if (!(notFoundClasses.contains(name))) {
                    notFoundClasses.add(name);
            }
            throw e;
        }

    }

    @Override
    public String toString() {
        String s = this.nameOfCL + " parentCL :: " + 
                             this.parentToString + " URLs :: \n";
        for(URL u:classLoaderList){
            s += ", " + u;
        }
        s +="\n";
        return s;
    }
}
