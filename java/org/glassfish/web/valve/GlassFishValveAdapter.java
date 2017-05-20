/*
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 */

package org.glassfish.web.valve;

import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Valve;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Adapter valve for wrapping a GlassFish-style valve that was compiled
 * against the "old" org.apache.catalina.Valve interface from GlassFish
 * releases prior to V3 (which has been renamed to
 * org.glassfish.web.valve.GlassFishValve in GlassFish V3).
 *
 * @author jluehe
 */
public class GlassFishValveAdapter implements GlassFishValve {

    // The wrapped GlassFish-style valve to which to delegate
    private Valve gfValve;

    private Method invokeMethod;
    private Method postInvokeMethod;

    /**
     * Constructor.
     *
     * @param gfValve The GlassFish valve to which to delegate
     */
    public GlassFishValveAdapter(Valve gfValve) throws Exception {
        this.gfValve = gfValve;
        invokeMethod = gfValve.getClass().getMethod("invoke", Request.class,
                                                    Response.class);
        postInvokeMethod = gfValve.getClass().getMethod("postInvoke",
                                                        Request.class,
                                                        Response.class);
    }

    public String getInfo() {
        return gfValve.getInfo();
    }

    /**
     * Delegates to the invoke() of the wrapped GlassFish-style valve.
     */
    public int invoke(Request request,
                      Response response)
                throws IOException, ServletException {
        try {
            return ((Integer) invokeMethod.invoke(gfValve, request, response)).intValue();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Delegates to the postInvoke() of the wrapped GlassFish-style valve.
     */
    public void postInvoke(Request request, Response response)
                throws IOException, ServletException {
        try {
            postInvokeMethod.invoke(gfValve, request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
