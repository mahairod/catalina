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

package org.apache.jasper.compiler;

import java.util.Vector;

public class JspProperty {

    private String isXml;
    private String elIgnored;
    private String scriptingInvalid;
    private String pageEncoding;
    private String trimSpaces;
    private String poundAllowed;
    private Vector includePrelude;
    private Vector includeCoda;

    public JspProperty(String isXml,
                       String elIgnored,
                       String scriptingInvalid,
                       String trimSpaces,
                       String poundAllowed,
                       String pageEncoding,
                       Vector includePrelude,
                       Vector includeCoda) {

        this.isXml = isXml;
        this.elIgnored = elIgnored;
        this.scriptingInvalid = scriptingInvalid;
        this.trimSpaces = trimSpaces;
        this.poundAllowed = poundAllowed;
        this.pageEncoding = pageEncoding;
        this.includePrelude = includePrelude;
        this.includeCoda = includeCoda;
    }

    public String isXml() {
        return isXml;
    }

    public String isELIgnored() {
        return elIgnored;
    }

    public String isScriptingInvalid() {
        return scriptingInvalid;
    }

    public String getPageEncoding() {
        return pageEncoding;
    }

    public String getTrimSpaces() {
        return trimSpaces;
    }

    public String getPoundAllowed() {
        return poundAllowed;
    }

    public Vector getIncludePrelude() {
        return includePrelude;
    }

    public Vector getIncludeCoda() {
        return includeCoda;
    }
}
