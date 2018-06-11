/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.jasper.runtime;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;

/**
 * Concrete implementation of {@link javax.el.ELContext}.
 * ELContext's constructor is protected to control creation of ELContext
 * objects through their appropriate factory methods.  This version of
 * ELContext forces construction through JspApplicationContextImpl.
 *
 * @author Mark Roth
 * @author Kin-man Chung
 */
public class ELContextImpl 
    extends ELContext
{
    /**
     * Constructs a new ELContext associated with the given ELResolver.
     */
    public ELContextImpl(ELResolver resolver) {
        this.resolver = resolver;
    }

    public ELResolver getELResolver() {
        return resolver;
    }

    public void setFunctionMapper(FunctionMapper fnMapper) {
        functionMapper = fnMapper;
    }

    public FunctionMapper getFunctionMapper() {
        return functionMapper;
    }

    public void setVariableMapper(VariableMapper varMapper) {
        variableMapper = varMapper;
    }

    public VariableMapper getVariableMapper() {
        return variableMapper;
    }

    private FunctionMapper functionMapper;
    private VariableMapper variableMapper;
    private ELResolver resolver;
}
