/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.core;

import java.util.*;
import javax.servlet.*;
import org.apache.catalina.Wrapper;

public class ServletRegistrationImpl extends ServletRegistration {

    private StandardContext ctx;
    private Wrapper wrapper;


    /**
     * Constructor
     */
    ServletRegistrationImpl(StandardContext ctx, String servletName,
                            String className) {
        this.ctx = ctx;
        Wrapper wrapper = ctx.createWrapper();
        ctx.addChild(wrapper);

        wrapper.setName(servletName);
        wrapper.setServletClass(className);
    }


    public void setDescription(String description) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException("ServletContext.already initialized");
        }
        super.setDescription(description);
        wrapper.setDescription(description);
    }


    public void setInitParameter(String name, String value) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException("ServletContext.already initialized");
        }
        if (null != value) {
            wrapper.addInitParameter(name, value);
        } else {
            wrapper.removeInitParameter(name);
        }
    }


    public void setLoadOnStartup(int loadOnStartup) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException("ServletContext.already initialized");
        }
        super.setLoadOnStartup(loadOnStartup);
        wrapper.setLoadOnStartup(loadOnStartup);
    }


    public void setAsyncSupported(boolean isAsyncSupported) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException("ServletContext.already initialized");
        }
        super.setAsyncSupported(isAsyncSupported);
        wrapper.setIsAsyncSupported(isAsyncSupported);
    }

}

