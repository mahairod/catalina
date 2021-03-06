/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.el.parser;

import javax.el.ELException;

import com.sun.el.lang.EvaluationContext;

/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 */
public final class AstChoice extends SimpleNode {
    public AstChoice(int id) {
        super(id);
    }

    public Class getType(EvaluationContext ctx)
            throws ELException {
        Object val = this.getValue(ctx);
        return (val != null) ? val.getClass() : null;
    }

    public Object getValue(EvaluationContext ctx)
            throws ELException {
        Object obj0 = this.children[0].getValue(ctx);
        Boolean b0 = coerceToBoolean(obj0);
        return this.children[((b0.booleanValue() ? 1 : 2))].getValue(ctx);
    }

    public boolean isReadOnly(EvaluationContext ctx)
            throws ELException {
        Object obj0 = this.children[0].getValue(ctx);
        Boolean b0 = coerceToBoolean(obj0);
        return this.children[((b0.booleanValue() ? 1 : 2))].isReadOnly(ctx);
    }

    public void setValue(EvaluationContext ctx, Object value)
            throws ELException {
        Object obj0 = this.children[0].getValue(ctx);
        Boolean b0 = coerceToBoolean(obj0);
        this.children[((b0.booleanValue()? 1: 2))].setValue(ctx, value);
    }

    public Object invoke(EvaluationContext ctx,
                         Class[] paramTypes,
                         Object[] paramValues)
            throws ELException {
        Object obj0 = this.children[0].getValue(ctx);
        Boolean b0 = coerceToBoolean(obj0);
        return this.children[((b0.booleanValue() ? 1 : 2))]
            .invoke(ctx, paramTypes, paramValues);
    }

}
