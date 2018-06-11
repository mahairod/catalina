/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.startup;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.catalina.Engine;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.util.StringManager;


/**
 * Startup event listener for a <b>Engine</b> that configures the properties
 * of that Engine, and the associated defined contexts.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:28:07 $
 */

public final class EngineConfig
    implements LifecycleListener {


    // ----------------------------------------------------- Instance Variables


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The Engine we are associated with.
     */
    private Engine engine = null;


    /**
     * The string resources for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


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


    // --------------------------------------------------------- Public Methods


    /**
     * Process the START event for an associated Engine.
     *
     * @param event The lifecycle event that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event) {

        // Identify the engine we are associated with
        try {
            engine = (Engine) event.getLifecycle();
            if (engine instanceof StandardEngine) {
                int engineDebug = ((StandardEngine) engine).getDebug();
                if (engineDebug > this.debug)
                    this.debug = engineDebug;
            }
        } catch (ClassCastException e) {
            log(sm.getString("engineConfig.cce", event.getLifecycle()), e);
            return;
        }

        // Process the event that has occurred
        if (event.getType().equals(Lifecycle.START_EVENT))
            start();
        else if (event.getType().equals(Lifecycle.STOP_EVENT))
            stop();

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Log a message on the Logger associated with our Engine (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

        Logger logger = null;
        if (engine != null)
            logger = engine.getLogger();
        if (logger != null)
            logger.log("EngineConfig: " + message);
        else
            System.out.println("EngineConfig: " + message);

    }


    /**
     * Log a message on the Logger associated with our Engine (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

        Logger logger = null;
        if (engine != null)
            logger = engine.getLogger();
        if (logger != null)
            logger.log("EngineConfig: " + message, throwable);
        else {
            System.out.println("EngineConfig: " + message);
            System.out.println("" + throwable);
            throwable.printStackTrace(System.out);
        }

    }


    /**
     * Process a "start" event for this Engine.
     */
    private void start() {

        if (debug > 0)
            log(sm.getString("engineConfig.start"));

    }


    /**
     * Process a "stop" event for this Engine.
     */
    private void stop() {

        if (debug > 0)
            log(sm.getString("engineConfig.stop"));

    }


}
