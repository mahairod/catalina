/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.jasper.runtime;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.tagext.JspFragment;

/**
 * Helper class from which all Jsp Fragment helper classes extend.
 * This class allows for the emulation of numerous fragments within
 * a single class, which in turn reduces the load on the class loader
 * since there are potentially many JspFragments in a single page.
 * <p>
 * The class also provides various utility methods for JspFragment
 * implementations.
 *
 * @author Mark Roth
 */
public abstract class JspFragmentHelper 
    extends JspFragment 
{
    
    protected int discriminator;
    protected JspContext jspContext;
    protected PageContext _jspx_page_context;
    protected JspTag parentTag;

    public JspFragmentHelper( int discriminator, JspContext jspContext, 
        JspTag parentTag ) 
    {
        this.discriminator = discriminator;
        this.jspContext = jspContext;
        this._jspx_page_context = null;
        if( jspContext instanceof PageContext ) {
            _jspx_page_context = (PageContext)jspContext;
        }
        this.parentTag = parentTag;
    }
    
    public JspContext getJspContext() {
        return this.jspContext;
    }
    
    public JspTag getParentTag() {
        return this.parentTag;
    }
    
}
