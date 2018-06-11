/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.jasper.runtime;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.el.VariableResolver;


/**
 * <p>This is the implementation of VariableResolver in JSP 2.0,
 * using ELResolver in JSP2.1.
 * It looks up variable references in the PageContext, and also
 * recognizes references to implicit objects.
 * 
 * @author Kin-man Chung
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class VariableResolverImpl
    implements VariableResolver
{
    private PageContext pageContext;

    //-------------------------------------
    /**
     * Constructor
     **/
    public VariableResolverImpl (PageContext pageContext) {
        this.pageContext = pageContext;
    }
  
    //-------------------------------------
    /**
     * Resolves the specified variable within the given context.
     * Returns null if the variable is not found.
     **/
    public Object resolveVariable (String pName)
            throws javax.servlet.jsp.el.ELException {

        ELContext elContext = pageContext.getELContext();
        ELResolver elResolver = elContext.getELResolver();
        try {
            return elResolver.getValue(elContext, null, pName);
        } catch (javax.el.ELException ex) {
            throw new javax.servlet.jsp.el.ELException();
        }
    }
}
