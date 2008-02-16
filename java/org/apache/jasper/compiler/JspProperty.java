/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
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
