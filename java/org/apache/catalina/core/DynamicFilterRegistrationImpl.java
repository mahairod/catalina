/*
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
    DynamicFilterRegistrationImpl(FilterDef filterDef,
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

