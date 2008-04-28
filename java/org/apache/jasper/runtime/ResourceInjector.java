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

package org.apache.jasper.runtime;

import javax.servlet.ServletContext;
import javax.servlet.jsp.tagext.JspTag;

/**
 * Interface for injecting injectable resources into tag handler instances.
 *
 * @author Jan Luehe
 */
public interface ResourceInjector {

    /**
     * Associates this ResourceInjector with the component environment of the
     * given servlet context.
     *
     * @param servletContext The servlet context 
     */
    public void setContext(ServletContext servletContext);


    /**
     * Injects the injectable resources from the component environment 
     * associated with this ResourceInjector into the given tag handler
     * instance. 
     *
     * @param handler The tag handler instance to be injected
     *
     * @throws Exception if an error occurs during injection
     */
    public void inject(JspTag handler) throws Exception;

}
