/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 *
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletResponse;

/**
 * Facade for the <b>StandardWrapper</b> object.
 *
 * @author Remy Maucharat
 * @version $Revision: 1.3 $ $Date: 2006/11/13 19:26:30 $
 */
public final class StandardWrapperFacade
    implements ServletConfig {


    // ----------------------------------------------------------- Constructors

    /**
     * Create a new facede around a StandardWrapper.
     */
    public StandardWrapperFacade(StandardWrapper config) {

        super();
        this.config = (ServletConfig) config;
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Wrapped config.
     */
    private ServletConfig config = null;


    /**
     * The context facade object for this wrapper.
     */
    private ServletContext context = null;


    // -------------------------------------------------- ServletConfig Methods


    public String getServletName() {
        return config.getServletName();
    }


    public ServletContext getServletContext() {

        if (context == null) {
            context = config.getServletContext();
            if ((context != null) && (context instanceof ApplicationContext)) {
                context = ((ApplicationContext) context).getFacade();
            }
        }

        return context;
    }


    public String getInitParameter(String name) {
        return config.getInitParameter(name);
    }


    public Enumeration getInitParameterNames() {
        return config.getInitParameterNames();
    }


    public Object getInitAttribute(String name) {
        return config.getInitAttribute(name);
    }


    public Enumeration<String> getInitAttributeNames() {
        return config.getInitAttributeNames();
    }

}
