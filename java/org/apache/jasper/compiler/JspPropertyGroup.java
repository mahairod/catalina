/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.jasper.compiler;

public class JspPropertyGroup {

    private String path;
    private String extension;
    private JspProperty jspProperty;

    public JspPropertyGroup(String path, String extension,
                            JspProperty jspProperty) {
        this.path = path;
        this.extension = extension;
        this.jspProperty = jspProperty;
    }

    public String getPath() {
        return path;
    }

    public String getExtension() {
        return extension;
    }

    public JspProperty getJspProperty() {
        return jspProperty;
    }
}
