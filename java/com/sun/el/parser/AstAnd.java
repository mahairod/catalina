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
public final class AstAnd extends BooleanNode {
    public AstAnd(int id) {
        super(id);
    }

    public Object getValue(EvaluationContext ctx)
            throws ELException {
        Object obj = children[0].getValue(ctx);
        Boolean b = coerceToBoolean(obj);
        if (!b.booleanValue()) {
            return b;
        }
        obj = children[1].getValue(ctx);
        b = coerceToBoolean(obj);
        return b;
    }
}
