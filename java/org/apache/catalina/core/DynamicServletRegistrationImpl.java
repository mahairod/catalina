/*
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.core;

import java.util.*;
import javax.servlet.*;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.util.StringManager;

public class DynamicServletRegistrationImpl
    extends ServletRegistrationImpl
    implements ServletRegistration.Dynamic {

    /**
     * Constructor
     */
    protected DynamicServletRegistrationImpl(StandardWrapper wrapper,
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

    public void setServletSecurity(ServletSecurityElement constraint) {
        // TBD
    }

    public void setMultipartConfig(MultipartConfigElement multipartConfig) {
        // TBD
    }

    public void setRunAsRole(String roleName) {
        // TBD
    }

}

