/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.core;

import java.util.*;
import javax.servlet.*;
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.StringManager;

public class ServletRegistrationImpl implements ServletRegistration {

    private static final StringManager sm =
        StringManager.getManager(Constants.Package);

    private Wrapper wrapper;
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
    ServletRegistrationImpl(Wrapper wrapper, StandardContext ctx,
                            boolean isProgrammatic) {
        this.wrapper = wrapper;
        this.ctx = ctx;
        this.isProgrammatic = isProgrammatic;
    }


    public void setDescription(String description) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("servletRegistration.alreadyInitialized",
                             "description", wrapper.getName(),
                             ctx.getName()));
        }

        wrapper.setDescription(description);
    }


    public void setInitParameter(String name, String value) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("servletRegistration.alreadyInitialized",
                             "init parameter", wrapper.getName(),
                             ctx.getName()));
        }

        if (null != value) {
            wrapper.addInitParameter(name, value);
        } else {
            wrapper.removeInitParameter(name);
        }
    }


    public void setInitParameters(Map<String, String> initParameters) {
        if (null == initParameters) {
            throw new IllegalArgumentException("Null init parameters");
        }
        for (Map.Entry<String, String> e : initParameters.entrySet()) {
            setInitParameter(e.getKey(), e.getValue());
        }
    }


    public void setLoadOnStartup(int loadOnStartup) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("servletRegistration.alreadyInitialized",
                             "load-on-startup", wrapper.getName(),
                             ctx.getName()));
        }

        wrapper.setLoadOnStartup(loadOnStartup);
    }


    public void setAsyncSupported(boolean isAsyncSupported) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("servletRegistration.alreadyInitialized",
                             "async-supported", wrapper.getName(),
                             ctx.getName()));
        }

        wrapper.setIsAsyncSupported(isAsyncSupported);
    }


    public void addMapping(String... urlPatterns) {
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
    }

}

