/*
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.core;

import java.util.*;
import javax.servlet.*;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.util.StringManager;

public class FilterRegistrationImpl implements FilterRegistration {

    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);

    protected FilterDef filterDef;
    protected StandardContext ctx;

    /**
     * Constructor
     */
    FilterRegistrationImpl(FilterDef filterDef, StandardContext ctx) {
        this.filterDef = filterDef;
        this.ctx = ctx;
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


    public Set<String> setInitParameters(Map<String, String> initParameters) {
        return filterDef.setInitParameters(initParameters);
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

