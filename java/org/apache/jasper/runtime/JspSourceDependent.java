/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.jasper.runtime;

/**
 * Interface for tracking the source files dependencies, for the purpose
 * of compiling out of date pages.  This is used for
 * 1) files that are included by page directives
 * 2) files that are included by include-prelude and include-coda in jsp:config
 * 3) files that are tag files and referenced
 * 4) TLDs referenced
 */

public interface JspSourceDependent {

   /**
    * Returns a list of files names that the current page has a source
    * dependency on.
    */
    /* GlassFish Issue 812
    public java.util.List getDependants();
    */
    // START GlassFish Issue 812
    // FIXME: Use java.lang.Object instead of java.util.List as return type
    // due to weird behavior with Eclipse JDT 3.1 in Java 5 mode
    public Object getDependants();
    // END GlassFish Issue 812
}
