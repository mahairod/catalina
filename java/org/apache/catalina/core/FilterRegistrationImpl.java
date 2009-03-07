/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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


    public boolean setInitParameter(String name, Object value) {
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

