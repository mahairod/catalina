/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.jasper.tagplugins.jstl;

import org.apache.jasper.compiler.tagplugin.*;

public final class Otherwise implements TagPlugin {

    public void doTag(TagPluginContext ctxt) {

	// See When.java for the reason whey "}" is need at the beginng and
	// not at the end.
	ctxt.generateJavaSource("} else {");
	ctxt.generateBody();
    }
}
