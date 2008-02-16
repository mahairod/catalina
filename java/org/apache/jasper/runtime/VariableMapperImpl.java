/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.jasper.runtime;

import java.util.HashMap;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.VariableMapper;
import javax.el.ValueExpression;


/**
 * <p>This is the implementation of VariableMapper.
 * The compiler creates an empty variable mapper when an ELContext is created.
 * The variable mapper will be updated by tag handlers, if necessary.
 * 
 * @author Kin-man Chung
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 **/

public class VariableMapperImpl extends VariableMapper
{
    //-------------------------------------
    /**
     * Constructor
     **/
    public VariableMapperImpl () {
        map = new HashMap();
    }
  
    //-------------------------------------
    /**
     * Resolves the specified variable within the given context.
     * Returns null if the variable is not found.
     **/
    public ValueExpression resolveVariable (String variable) {
        return (ValueExpression) map.get(variable);
    }

    public ValueExpression setVariable(String variable,
                                       ValueExpression expression) {
        ValueExpression prev = null;
        if (expression == null) {
            map.remove(variable);
        } else {
            prev = (ValueExpression) map.get(variable);
            map.put(variable, expression);
        }
        return prev;
    }

    private HashMap map;
}
