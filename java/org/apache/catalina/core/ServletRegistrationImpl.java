/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

import org.apache.catalina.util.StringManager;

import javax.servlet.ServletRegistration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ServletRegistrationImpl implements ServletRegistration {

    protected static final StringManager sm =
        StringManager.getManager(Constants.Package);

    protected StandardWrapper wrapper;
    protected StandardContext ctx;

    /**
     * Constructor
     */
    protected ServletRegistrationImpl(StandardWrapper wrapper,
                                      StandardContext ctx) {
        this.wrapper = wrapper;
        this.ctx = ctx;
    }

    public String getName() {
        return wrapper.getName();
    }

    public StandardContext getContext() {
        return ctx;
    }

    public StandardWrapper getWrapper() {
        return wrapper;
    }

    public String getClassName() {
        return wrapper.getServletClassName();
    }

    public String getJspFile() {
        return wrapper.getJspFile();
    }

    public boolean setInitParameter(String name, String value) {
        if (ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("servletRegistration.alreadyInitialized",
                             "init parameter", wrapper.getName(),
                             ctx.getName()));
        }
        return wrapper.setInitParameter(name, value, false);
    }

    public String getInitParameter(String name) {
        return wrapper.getInitParameter(name);
    }

    public Set<String> setInitParameters(Map<String, String> initParameters) {
        return wrapper.setInitParameters(initParameters);
    }

    public Map<String, String> getInitParameters() {
        return wrapper.getInitParameters();
    }

    public Set<String> addMapping(String... urlPatterns) {
        if (!ctx.isEmbedded() && ctx.isContextInitializedCalled()) {
            throw new IllegalStateException(
                sm.getString("servletRegistration.alreadyInitialized",
                             "mapping", wrapper.getName(), ctx.getName()));
        }

        if (urlPatterns == null || urlPatterns.length == 0) {
            throw new IllegalArgumentException(
                sm.getString(
                    "servletRegistration.mappingWithNullOrEmptyUrlPatterns",
                    wrapper.getName(), ctx.getName()));
        }

        return ctx.addServletMapping(wrapper.getName(), urlPatterns);
    }

    public Collection<String> getMappings() {
        return wrapper.getMappings();
    }

    public String getRunAsRole() {
        return wrapper.getRunAs();
    }
}

