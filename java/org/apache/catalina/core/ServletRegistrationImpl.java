/*
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 */

package org.apache.catalina.core;

import javax.servlet.ServletRegistration;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.apache.catalina.LogFacade;

public class ServletRegistrationImpl implements ServletRegistration {


    protected StandardWrapper wrapper;
    protected StandardContext ctx;

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    /**
     * Constructor
     */
    public ServletRegistrationImpl(StandardWrapper wrapper,
                                      StandardContext ctx) {
        this.wrapper = wrapper;
        this.ctx = ctx;
    }

    public String getName() {
        return wrapper.getName();
    }

    public StandardContext getContext() {
        return ctx;
    }

    public StandardWrapper getWrapper() {
        return wrapper;
    }

    public String getClassName() {
        return wrapper.getServletClassName();
    }

    public String getJspFile() {
        return wrapper.getJspFile();
    }

    public boolean setInitParameter(String name, String value) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(LogFacade.SERVLET_REGISTRATION_ALREADY_INIT),
                                              new Object[] {"init parameter", wrapper.getName(),
                                                            ctx.getName()});
            throw new IllegalStateException(msg);
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
            String msg = MessageFormat.format(rb.getString(LogFacade.SERVLET_REGISTRATION_ALREADY_INIT),
                                              new Object[] {"mapping", wrapper.getName(),
                                                            ctx.getName()});
            throw new IllegalStateException(msg);
        }

        if (urlPatterns == null || urlPatterns.length == 0) {
            String msg = MessageFormat.format(rb.getString(LogFacade.SERVLET_REGISTRATION_MAPPING_URL_PATTERNS_EXCEPTION),
                                              new Object[] {wrapper.getName(), ctx.getName()});
            throw new IllegalArgumentException(msg);
        }

        return ctx.addServletMapping(wrapper.getName(), urlPatterns);
    }

    public Collection<String> getMappings() {
        return wrapper.getMappings();
    }

    public String getRunAsRole() {
        return wrapper.getRunAs();
    }
}

