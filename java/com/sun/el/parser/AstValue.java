/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.el.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.MethodInfo;
import javax.el.PropertyNotFoundException;

import com.sun.el.lang.EvaluationContext;
import com.sun.el.lang.ELSupport;
import com.sun.el.util.MessageFactory;
import com.sun.el.util.ReflectionUtil;

/**
 * @author Jacob Hookom [jacob@hookom.net]
 * @version $Change: 181177 $$DateTime: 2001/06/26 08:45:09 $$Author: kchung $
 */
public final class AstValue extends SimpleNode {

    protected static class Target {
        protected Object base;

        protected Object property;
    }

    public AstValue(int id) {
        super(id);
    }

    public Class getType(EvaluationContext ctx) throws ELException {
        Target t = getTarget(ctx);
        ctx.setPropertyResolved(false);
        Class ret = ctx.getELResolver().getType(ctx, t.base, t.property);
        if (! ctx.isPropertyResolved()) {
            ELSupport.throwUnhandled(t.base, t.property);
        }
        return ret;
    }

    private final Target getTarget(EvaluationContext ctx) throws ELException {
        // evaluate expr-a to value-a
        Object base = this.children[0].getValue(ctx);

        // if our base is null (we know there are more properites to evaluate)
        if (base == null) {
            throw new PropertyNotFoundException(MessageFactory.get(
                    "error.unreachable.base", this.children[0].getImage()));
        }

        // set up our start/end
        Object property = null;
        int propCount = this.jjtGetNumChildren() - 1;
        int i = 1;

        // evaluate any properties before our target
        ELResolver resolver = ctx.getELResolver();
        if (propCount > 1) {
            while (base != null && i < propCount) {
                property = this.children[i].getValue(ctx);
                ctx.setPropertyResolved(false);
                base = resolver.getValue(ctx, base, property);
                if (! ctx.isPropertyResolved()) {
                    ELSupport.throwUnhandled(base, property);
                }
                i++;
            }
            // if we are in this block, we have more properties to resolve,
            // but our base was null
            if (base == null || property == null) {
                throw new PropertyNotFoundException(MessageFactory.get(
                        "error.unreachable.property", property));
            }
        }

        property = this.children[i].getValue(ctx);

        if (property == null) {
            throw new PropertyNotFoundException(MessageFactory.get(
                    "error.unreachable.property", this.children[i]));
        }

        Target t = new Target();
        t.base = base;
        t.property = property;
        return t;
    }

    public Object getValue(EvaluationContext ctx) throws ELException {
        Object base = this.children[0].getValue(ctx);
        int propCount = this.jjtGetNumChildren();
        int i = 1;
        Object property = null;
        ELResolver resolver = ctx.getELResolver();
        while (base != null && i < propCount) {
            property = this.children[i].getValue(ctx);
            if (property == null) {
                return null;
            } else {
                ctx.setPropertyResolved(false);
                base = resolver.getValue(ctx, base, property);
                if (! ctx.isPropertyResolved()) {
                    ELSupport.throwUnhandled(base, property);
                }
            }
            i++;
        }
        return base;
    }

    public boolean isReadOnly(EvaluationContext ctx) throws ELException {
        Target t = getTarget(ctx);
        ctx.setPropertyResolved(false);
        boolean ret = ctx.getELResolver().isReadOnly(ctx, t.base, t.property);
        if (! ctx.isPropertyResolved()) {
            ELSupport.throwUnhandled(t.base, t.property);
        }
        return ret;
    }

    public void setValue(EvaluationContext ctx, Object value)
            throws ELException {
        Target t = getTarget(ctx);
        ctx.setPropertyResolved(false);
        ELResolver elResolver = ctx.getELResolver();
        if (value != null) {
            value = ELSupport.coerceToType(value,
                        elResolver.getType(ctx, t.base, t.property));
        }
        elResolver.setValue(ctx, t.base, t.property, value);
        if (! ctx.isPropertyResolved()) {
            ELSupport.throwUnhandled(t.base, t.property);
        }
    }

    public MethodInfo getMethodInfo(EvaluationContext ctx, Class[] paramTypes)
            throws ELException {
        Target t = getTarget(ctx);
        Method m = ReflectionUtil.getMethod(t.base, t.property, paramTypes);
        return new MethodInfo(m.getName(), m.getReturnType(), m
                .getParameterTypes());
    }

    public Object invoke(EvaluationContext ctx, Class[] paramTypes,
            Object[] paramValues) throws ELException {
        Target t = getTarget(ctx);
        Method m = ReflectionUtil.getMethod(t.base, t.property, paramTypes);
        Object result = null;
        try {
            result = m.invoke(t.base, (Object[]) paramValues);
        } catch (IllegalAccessException iae) {
            throw new ELException(iae);
        } catch (InvocationTargetException ite) {
            throw new ELException(ite.getCause());
        }
        return result;
    }
}
