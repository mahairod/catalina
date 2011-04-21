/*
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 */

package org.apache.catalina.core;

import javax.servlet.MultipartConfigElement;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import java.util.Collections;
import java.util.Set;

public class DynamicServletRegistrationImpl
    extends ServletRegistrationImpl
    implements ServletRegistration.Dynamic {

    /**
     * Constructor
     */
    public DynamicServletRegistrationImpl(StandardWrapper wrapper,
            StandardContext ctx) {
        super(wrapper, ctx);
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

    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        Set<String> emptySet = Collections.emptySet();
        return Collections.unmodifiableSet(emptySet);
    }

    public void setMultipartConfig(MultipartConfigElement mpConfig) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("servletRegistration.alreadyInitialized",
                             "multipart-config", wrapper.getName(),
                             ctx.getName()));
        }

        wrapper.setMultipartLocation(mpConfig.getLocation());
        wrapper.setMultipartMaxFileSize(mpConfig.getMaxFileSize());
        wrapper.setMultipartMaxRequestSize(mpConfig.getMaxRequestSize());
        wrapper.setMultipartFileSizeThreshold(
            mpConfig.getFileSizeThreshold());
    }

    public void setRunAsRole(String roleName) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("servletRegistration.alreadyInitialized",
                             "run-as", wrapper.getName(), ctx.getName()));
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

