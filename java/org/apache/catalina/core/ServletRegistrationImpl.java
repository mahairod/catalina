/*
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.core;

import java.util.*;
import javax.servlet.*;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.util.StringManager;

public class ServletRegistrationImpl implements ServletRegistration {

    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);

    protected StandardWrapper wrapper;
    protected StandardContext ctx;

    /**
     * Constructor
     */
    ServletRegistrationImpl(StandardWrapper wrapper, StandardContext ctx) {
        this.wrapper = wrapper;
        this.ctx = ctx;
    }


    public String getName() {
        return wrapper.getName();
    }


    public StandardWrapper getWrapper() {
        return wrapper;
    }


    public String getClassName() {
        return wrapper.getServletClassName();
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


    public String getInitParameter(String name) {
        return wrapper.getInitParameter(name);
    }


    public Set<String> setInitParameters(Map<String, String> initParameters) {
        return wrapper.setInitParameters(initParameters);
    }


    public Map<String, String> getInitParameters() {
        return wrapper.getInitParameters();
    }


    public Set<String> addMapping(String... urlPatterns) {
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

        return ctx.addServletMapping(wrapper.getName(), urlPatterns);
    }

    public Collection<String> getMappings() {
        return wrapper.getMappings();
    }
}

