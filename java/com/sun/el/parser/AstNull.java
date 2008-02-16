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
public final class AstNull extends SimpleNode {
    public AstNull(int id) {
        super(id);
    }

    public Class getType(EvaluationContext ctx)
            throws ELException {
        return null;
    }

    public Object getValue(EvaluationContext ctx)
            throws ELException {
        return null;
    }
}
