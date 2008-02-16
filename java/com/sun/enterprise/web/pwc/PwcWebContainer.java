/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web.pwc;

import org.apache.catalina.startup.Embedded;
import org.apache.catalina.Logger;
import org.apache.catalina.Engine;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.Container;

/*
 * Represents the production web container
 */
public class PwcWebContainer implements PwcWebContainerLifecycle {

    /**
     * The parent/top-level container in <code>_embedded</code> for virtual
     * servers.
     */
    private Engine _engine = null;

   /**
     * The embedded Catalina object.
     */
    private Embedded _embedded = null;


    /**
     * Has this component been started yet?
     */
    private boolean _started = false;

    public void onInitialization(String rootDir, String instanceName,
                                 boolean useNaming, Logger logger,
                                 String embeddedClassName) 
        throws Exception {
        Class c = Class.forName(embeddedClassName);
        _embedded = (Embedded) c.newInstance();
        _embedded.setUseNaming(useNaming);
        _embedded.setLogger(logger);
        _engine = _embedded.createEngine();
        _embedded.addEngine(_engine);
    }

    public void onStartup() 
        throws Exception {
        _started = true;

        _embedded.start();

    }

    public void onReady() 
        throws Exception {
    }

 
    public void onShutdown() 
        throws Exception {
    }


    public void onTermination() 
        throws Exception {
        _started = false;
        _embedded.stop();
    }

    public Engine getEngine() {
        return _engine;
    }

    public Embedded getEmbedded() {
        return _embedded;
    }

}
