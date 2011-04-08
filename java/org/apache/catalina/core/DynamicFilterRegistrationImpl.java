/*
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 */

package org.apache.catalina.core;

import org.apache.catalina.deploy.FilterDef;

import javax.servlet.FilterRegistration;

public class DynamicFilterRegistrationImpl
    extends FilterRegistrationImpl
    implements FilterRegistration.Dynamic {

    /**
     * Constructor
     */
    public DynamicFilterRegistrationImpl(FilterDef filterDef,
            StandardContext ctx) {
        super(filterDef, ctx);
    }

    public void setAsyncSupported(boolean isAsyncSupported) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("filterRegistration.alreadyInitialized",
                             "async-supported", filterDef.getFilterName(),
                             ctx.getName()));
        }

        filterDef.setIsAsyncSupported(isAsyncSupported);
    }
}

