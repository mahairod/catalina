/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.el.parser;

import java.util.Collection;
import java.util.Map;

import javax.el.ELException;

import com.sun.el.lang.EvaluationContext;

/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 */
public final class AstEmpty extends SimpleNode {
    public AstEmpty(int id) {
        super(id);
    }

    public Class getType(EvaluationContext ctx)
            throws ELException {
        return Boolean.class;
    }

    public Object getValue(EvaluationContext ctx)
            throws ELException {
        Object obj = this.children[0].getValue(ctx);
        if (obj == null) {
            return Boolean.TRUE;
        } else if (obj instanceof String) {
            return Boolean.valueOf(((String) obj).length() == 0);
        } else if (obj instanceof Object[]) {
            return Boolean.valueOf(((Object[]) obj).length == 0);
        } else if (obj instanceof Collection) {
            return Boolean.valueOf(((Collection) obj).isEmpty());
        } else if (obj instanceof Map) {
            return Boolean.valueOf(((Map) obj).isEmpty());
        }
        return Boolean.FALSE;
    }
}
