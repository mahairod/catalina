/*
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 */

package org.apache.catalina.core;

import org.apache.catalina.LogFacade;
import org.apache.catalina.deploy.FilterDef;

import javax.servlet.FilterRegistration;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;


public class DynamicFilterRegistrationImpl
    extends FilterRegistrationImpl
    implements FilterRegistration.Dynamic {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    /**
     * Constructor
     */
    public DynamicFilterRegistrationImpl(FilterDef filterDef,
            StandardContext ctx) {
        super(filterDef, ctx);
    }

    public void setAsyncSupported(boolean isAsyncSupported) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(LogFacade.DYNAMIC_FILTER_REGISTRATION_ALREADY_INIT),
                                              new Object[] {"async-supported", filterDef.getFilterName(),
                                                            ctx.getName()});
            throw new IllegalStateException(msg);
        }

        filterDef.setIsAsyncSupported(isAsyncSupported);
    }
}

