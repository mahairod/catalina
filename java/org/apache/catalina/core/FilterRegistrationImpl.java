/*
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.core;

import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.util.StringManager;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

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


    public String getName() {
        return filterDef.getFilterName();
    }


    public FilterDef getFilterDefinition() {
        return filterDef;
    }


    public String getClassName() {
        return filterDef.getFilterClassName();
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


    public String getInitParameter(String name) {
        return filterDef.getInitParameter(name);
    }


    public Set<String> setInitParameters(Map<String, String> initParameters) {
        return filterDef.setInitParameters(initParameters);
    }


    public Map<String, String> getInitParameters() {
        return filterDef.getInitParameters();
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
            fmap.setDispatcherTypes(dispatcherTypes);

            ctx.addFilterMap(fmap, isMatchAfter);
        }
    }


    public Collection<String> getServletNameMappings() {
        return ctx.getServletNameFilterMappings(getName());
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
            fmap.setDispatcherTypes(dispatcherTypes);

            ctx.addFilterMap(fmap, isMatchAfter);
        }
    }


    public Collection<String> getUrlPatternMappings() {
        return ctx.getUrlPatternFilterMappings(getName());
    }
}

