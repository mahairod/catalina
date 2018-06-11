/*
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 */

package org.apache.catalina.core;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import java.util.Collections;
import java.util.Set;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.text.MessageFormat;

import org.glassfish.logging.annotation.LogMessageInfo;

public class DynamicServletRegistrationImpl
    extends ServletRegistrationImpl
    implements ServletRegistration.Dynamic {

    private static final Logger log = StandardServer.log;
    private static final ResourceBundle rb = log.getResourceBundle();

    @LogMessageInfo(
        message = "Unable to configure {0} for servlet {1} of servlet context {2}, " +
                  "because this servlet context has already been initialized",
        level = "WARNING"
    )
    public static final String DYNAMIC_SERVLET_REGISTRATION_ALREADY_INIT = "AS-WEB-CORE-00042";

    /**
     * Constructor
     */
    public DynamicServletRegistrationImpl(StandardWrapper wrapper,
            StandardContext ctx) {
        super(wrapper, ctx);
    }

    public void setLoadOnStartup(int loadOnStartup) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(DYNAMIC_SERVLET_REGISTRATION_ALREADY_INIT),
                                              new Object[] {"load-on-startup", wrapper.getName(), ctx.getName()});
            throw new IllegalStateException(msg);
        }

        wrapper.setLoadOnStartup(loadOnStartup);
    }

    public void setAsyncSupported(boolean isAsyncSupported) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(DYNAMIC_SERVLET_REGISTRATION_ALREADY_INIT),
                                              new Object[] {"load-on-startup", wrapper.getName(), ctx.getName()});
            throw new IllegalStateException(msg);
        }

        wrapper.setIsAsyncSupported(isAsyncSupported);
    }

    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        Set<String> emptySet = Collections.emptySet();
        return Collections.unmodifiableSet(emptySet);
    }

    public void setMultipartConfig(MultipartConfigElement mpConfig) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(DYNAMIC_SERVLET_REGISTRATION_ALREADY_INIT),
                    new Object[] {"multipart-config", wrapper.getName(), ctx.getName()});
            throw new IllegalStateException(msg);
        }

        wrapper.setMultipartLocation(mpConfig.getLocation());
        wrapper.setMultipartMaxFileSize(mpConfig.getMaxFileSize());
        wrapper.setMultipartMaxRequestSize(mpConfig.getMaxRequestSize());
        wrapper.setMultipartFileSizeThreshold(
            mpConfig.getFileSizeThreshold());
    }

    public void setRunAsRole(String roleName) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(DYNAMIC_SERVLET_REGISTRATION_ALREADY_INIT),
                    new Object[] {"run-as", wrapper.getName(), ctx.getName()});
            throw new IllegalStateException(msg);
        }

        wrapper.setRunAs(roleName);
    }

    protected void setServletClassName(String className) {
        wrapper.setServletClassName(className);
    }

    protected void setServletClass(Class <? extends Servlet> clazz) {
        wrapper.setServletClass(clazz);
    }

}

