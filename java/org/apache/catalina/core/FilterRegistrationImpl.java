/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.core;

import java.util.*;
import javax.servlet.*;
import org.apache.catalina.deploy.FilterDef;

public class FilterRegistrationImpl extends FilterRegistration {

    private StandardContext ctx;
    private FilterDef filterDef;


    /**
     * Constructor
     */
    FilterRegistrationImpl(StandardContext ctx, String filterName,
                           String className) {
        this.ctx = ctx;
        filterDef = new FilterDef();
        ctx.addFilterDef(filterDef);

        filterDef.setFilterName(filterName);
        filterDef.setFilterClass(className);
    }


    public void setDescription(String description) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException("ServletContext.already initialized");
        }
        super.setDescription(description);
        filterDef.setDescription(description);
    }


    public void setInitParameter(String name, String value) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException("ServletContext.already initialized");
        }
        if (null != value) {
            filterDef.addInitParameter(name, value);
        } else {
            filterDef.removeInitParameter(name);
        }
    }


    public void setAsyncSupported(boolean isAsyncSupported) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException("ServletContext.already initialized");
        }
        super.setAsyncSupported(isAsyncSupported);
        filterDef.setIsAsyncSupported(isAsyncSupported);
    }

}

