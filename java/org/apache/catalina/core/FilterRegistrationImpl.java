/*
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 */

package org.apache.catalina.core;

import org.apache.catalina.LogFacade;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

public class FilterRegistrationImpl implements FilterRegistration {

    protected FilterDef filterDef;
    protected StandardContext ctx;

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

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
            String msg = MessageFormat.format(rb.getString(LogFacade.FILTER_REGISTRATION_ALREADY_INIT),
                                              new Object[] {"init parameter", filterDef.getFilterName(),
                                                            ctx.getName()});
            throw new IllegalStateException(msg);
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
            String msg = MessageFormat.format(rb.getString(LogFacade.FILTER_REGISTRATION_ALREADY_INIT),
                                              new Object[] {"servlet-name mapping", filterDef.getFilterName(),
                                                            ctx.getName()});
            throw new IllegalStateException(msg);
        }

        if ((servletNames==null) || (servletNames.length==0)) {
            String msg = MessageFormat.format(rb.getString(LogFacade.FILTER_REGISTRATION_MAPPING_SERVLET_NAME_EXCEPTION),
                                              new Object[]  {filterDef.getFilterName(), ctx.getName()});
            throw new IllegalArgumentException(msg);
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
            String msg = MessageFormat.format(rb.getString(LogFacade.FILTER_REGISTRATION_ALREADY_INIT),
                                              new Object[] {"url-pattern mapping", filterDef.getFilterName(),
                                                            ctx.getName()});
            throw new IllegalStateException(msg);
        }

        if ((urlPatterns==null) || (urlPatterns.length==0)) {
            String msg = MessageFormat.format(rb.getString(LogFacade.FILTER_REGISTRATION_MAPPING_URL_PATTERNS_EXCEPTION),
                                              new Object[] {filterDef.getFilterName(), ctx.getName()});
            throw new IllegalArgumentException(msg);
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

