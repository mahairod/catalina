/*
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.logging.annotation.LogMessageInfo;

public class ServletRegistrationImpl implements ServletRegistration {


    protected StandardWrapper wrapper;
    protected StandardContext ctx;

    private static final ResourceBundle rb = StandardServer.log.getResourceBundle();

    @LogMessageInfo(
        message = "Unable to configure {0} for servlet {1} of servlet context {2}, " +
                   "because this servlet context has already been initialized",
        level = "WARNING"
    )
    public static final String SERVLET_REGISTRATION_ALREADY_INIT = "AS-WEB-CORE-00053";

    @LogMessageInfo(
        message = "Unable to configure mapping for servlet {0} of servlet context {1}, because URL patterns are null or empty",
        level = "WARNING"
    )
    public static final String SERVLET_REGISTRATION_MAPPING_URL_PATTERNS_EXCEPTION = "AS-WEB-CORE-00054";

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
            String msg = MessageFormat.format(rb.getString(SERVLET_REGISTRATION_ALREADY_INIT),
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
            String msg = MessageFormat.format(rb.getString(SERVLET_REGISTRATION_ALREADY_INIT),
                                              new Object[] {"mapping", wrapper.getName(),
                                                            ctx.getName()});
            throw new IllegalStateException(msg);
        }

        if (urlPatterns == null || urlPatterns.length == 0) {
            String msg = MessageFormat.format(rb.getString(SERVLET_REGISTRATION_MAPPING_URL_PATTERNS_EXCEPTION),
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

