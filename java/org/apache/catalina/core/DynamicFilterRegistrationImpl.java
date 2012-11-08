/*
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 */

package org.apache.catalina.core;

import org.apache.catalina.deploy.FilterDef;
import org.glassfish.logging.annotation.LogMessageInfo;

import javax.servlet.FilterRegistration;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;


public class DynamicFilterRegistrationImpl
    extends FilterRegistrationImpl
    implements FilterRegistration.Dynamic {

    private static final Logger log = StandardServer.log;
    private static final ResourceBundle rb = log.getResourceBundle();

    @LogMessageInfo(
        message = "Unable to configure {0} for filter {1} of servlet context {2}, because this servlet context has already been initialized",
        level = "WARNING"
    )
    public static final String DYNAMIC_FILTER_REGISTRATION_ALREADY_INIT = "AS-WEB-CORE-00041";

    /**
     * Constructor
     */
    public DynamicFilterRegistrationImpl(FilterDef filterDef,
            StandardContext ctx) {
        super(filterDef, ctx);
    }

    public void setAsyncSupported(boolean isAsyncSupported) {
        if (ctx.isContextInitializedCalled()) {
            String msg = MessageFormat.format(rb.getString(DYNAMIC_FILTER_REGISTRATION_ALREADY_INIT),
                                              new Object[] {"async-supported", filterDef.getFilterName(),
                                                            ctx.getName()});
            throw new IllegalStateException(msg);
        }

        filterDef.setIsAsyncSupported(isAsyncSupported);
    }
}

