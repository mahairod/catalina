/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.core;

import java.util.*;
import javax.servlet.*;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.util.StringManager;

public class FilterRegistrationImpl extends FilterRegistration {

    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);

    private FilterDef filterDef;
    private StandardContext ctx;

    /*
     * true if this ServletRegistration was obtained through programmatic
     * registration (i.e., via a call to ServletContext#addServlet), and
     * false if it represents a servlet declared in web.xml or a web.xml
     * fragment
     */
    private boolean isProgrammatic;


    /**
     * Constructor
     */
    FilterRegistrationImpl(FilterDef filterDef, StandardContext ctx,
                           boolean isProgrammatic) {
        this.filterDef = filterDef;
        this.ctx = ctx;
        this.isProgrammatic = isProgrammatic;
    }


    public void setDescription(String description) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("filterRegistration.alreadyInitialized",
                             "description", filterDef.getFilterName(),
                             ctx.getName()));
        }

        super.setDescription(description);
        filterDef.setDescription(description);
    }


    public void setInitParameter(String name, String value) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("filterRegistration.alreadyInitialized",
                             "init parameter", filterDef.getFilterName(),
                             ctx.getName()));
        }

        if (null != value) {
            filterDef.addInitParameter(name, value);
        } else {
            filterDef.removeInitParameter(name);
        }
    }


    public void setAsyncSupported(boolean isAsyncSupported) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("filterRegistration.alreadyInitialized",
                             "async-supported", filterDef.getFilterName(),
                             ctx.getName()));
        }

        super.setAsyncSupported(isAsyncSupported);
        filterDef.setIsAsyncSupported(isAsyncSupported);
    }


    public void addMappingForServletNames(
            EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
            String... servletNames) {

        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("filterRegistration.alreadyInitialized",
                             "servlet-name mapping",
                             filterDef.getFilterName(),
                             ctx.getName()));
        }

        if ((servletNames==null) || (servletNames.length==0)) {
            throw new IllegalArgumentException(
                sm.getString(
                    "filterRegistration.mappingWithNullOrEmptyServletNames",
                    filterDef.getFilterName(), ctx.getName()));
        }

        for (String servletName : servletNames) {
            FilterMap fmap = new FilterMap();
            fmap.setFilterName(filterDef.getFilterName());
            fmap.setServletName(servletName);
            for (DispatcherType dispatcherType : dispatcherTypes) {
                fmap.setDispatcher(dispatcherType);
            }
            ctx.addFilterMap(fmap, isMatchAfter);
        }
    }


    public void addMappingForUrlPatterns(
            EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
            String... urlPatterns) {

        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("filterRegistration.alreadyInitialized",
                             "url-pattern mapping", filterDef.getFilterName(),
                             ctx.getName()));
        }

        if ((urlPatterns==null) || (urlPatterns.length==0)) {
            throw new IllegalArgumentException(
                sm.getString(
                    "filterRegistration.mappingWithNullOrEmptyUrlPatterns",
                    filterDef.getFilterName(), ctx.getName()));
        }

        for (String urlPattern : urlPatterns) {
            FilterMap fmap = new FilterMap();
            fmap.setFilterName(filterDef.getFilterName());
            fmap.setURLPattern(urlPattern);
            for (DispatcherType dispatcherType : dispatcherTypes) {
                fmap.setDispatcher(dispatcherType);
            }
            ctx.addFilterMap(fmap, isMatchAfter);
        }
    }

}

