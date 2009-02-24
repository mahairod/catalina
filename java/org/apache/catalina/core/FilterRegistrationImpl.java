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

public class FilterRegistrationImpl implements FilterRegistration {

    private static final StringManager sm =
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


    public boolean setDescription(String description) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("filterRegistration.alreadyInitialized",
                             "description", filterDef.getFilterName(),
                             ctx.getName()));
        }

        if (!isProgrammatic) {
            return false;
        } else {
            filterDef.setDescription(description);
            return true;
        }
    }


    public boolean setInitParameter(String name, String value) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("filterRegistration.alreadyInitialized",
                             "init parameter", filterDef.getFilterName(),
                             ctx.getName()));
        }

        return filterDef.setInitParameter(name, value, false);
    }


    public boolean setInitParameters(Map<String, String> initParameters) {
        return filterDef.setInitParameters(initParameters);
    }


    public boolean setAsyncSupported(boolean isAsyncSupported) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("filterRegistration.alreadyInitialized",
                             "async-supported", filterDef.getFilterName(),
                             ctx.getName()));
        }

        if (!isProgrammatic) {
            return false;
        } else {
            filterDef.setIsAsyncSupported(isAsyncSupported);
            return true;
        }
    }


    public boolean addMappingForServletNames(
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
            fmap.setDispatcherTypes(dispatcherTypes);

            ctx.addFilterMap(fmap, isMatchAfter);
        }

        return true;
    }


    public boolean addMappingForUrlPatterns(
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
            fmap.setDispatcherTypes(dispatcherTypes);

            ctx.addFilterMap(fmap, isMatchAfter);
        }

        return true;
    }

}

