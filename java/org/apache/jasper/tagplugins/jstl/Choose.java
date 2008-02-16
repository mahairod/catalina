/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.jasper.tagplugins.jstl;

import org.apache.jasper.compiler.tagplugin.*;

public final class Choose implements TagPlugin {

    public void doTag(TagPluginContext ctxt) {

	// Not much to do here, much of the work will be done in the
	// containing tags, <c:when> and <c:otherwise>.

	ctxt.generateBody();
	// See comments in When.java for the reason "}" is generated here.
	ctxt.generateJavaSource("}");
    }
}
