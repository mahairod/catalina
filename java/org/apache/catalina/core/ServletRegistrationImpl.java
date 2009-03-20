/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.core;

import java.util.*;
import javax.servlet.*;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.util.StringManager;

public class ServletRegistrationImpl implements ServletRegistration {

    private static final StringManager sm =
        StringManager.getManager(Constants.Package);

    private StandardWrapper wrapper;
    private StandardContext ctx;

    /*
     * true if this ServletRegistration was obtained through programmatic
     * registration (i.e., via a call to ServletContext#addServlet), and
     * false if it represents a servlet declared in web.xml or a web.xml
     * fragment
     */
    private boolean isProgrammatic;


    /**
     * Constructor
     */
    ServletRegistrationImpl(StandardWrapper wrapper, StandardContext ctx,
                            boolean isProgrammatic) {
        this.wrapper = wrapper;
        this.ctx = ctx;
        this.isProgrammatic = isProgrammatic;
    }


    public boolean setDescription(String description) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("servletRegistration.alreadyInitialized",
                             "description", wrapper.getName(),
                             ctx.getName()));
        }

        if (!isProgrammatic) {
            return false;
        } else {
            wrapper.setDescription(description);
            return true;
        }
    }


    public boolean setInitParameter(String name, String value) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("servletRegistration.alreadyInitialized",
                             "init parameter", wrapper.getName(),
                             ctx.getName()));
        }

        return wrapper.setInitParameter(name, value, false);
    }


    public boolean setInitParameters(Map<String, String> initParameters) {
        return wrapper.setInitParameters(initParameters);
    }


    public boolean setLoadOnStartup(int loadOnStartup) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("servletRegistration.alreadyInitialized",
                             "load-on-startup", wrapper.getName(),
                             ctx.getName()));
        }

        if (!isProgrammatic) {
            return false;
        } else {
            wrapper.setLoadOnStartup(loadOnStartup);
            return true;
        }
    }


    public boolean setAsyncSupported(boolean isAsyncSupported) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("servletRegistration.alreadyInitialized",
                             "async-supported", wrapper.getName(),
                             ctx.getName()));
        }

        if (!isProgrammatic) {
            return false;
        } else {
            wrapper.setIsAsyncSupported(isAsyncSupported);
            return true;
        }
    }


    public boolean addMapping(String... urlPatterns) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("servletRegistration.alreadyInitialized",
                             "mapping", wrapper.getName(), ctx.getName()));
        }

        if (urlPatterns == null || urlPatterns.length == 0) {
            throw new IllegalArgumentException(
                sm.getString(
                    "servletRegistration.mappingWithNullOrEmptyUrlPatterns",
                    wrapper.getName(), ctx.getName()));
        }

        for (String urlPattern : urlPatterns) {
            ctx.addServletMapping(urlPattern, wrapper.getName());
        }

        return true;
    }

}

