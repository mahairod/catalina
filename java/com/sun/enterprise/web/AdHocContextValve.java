/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Logger;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import com.sun.logging.LogDomains;

/**
 * Implementation of StandardContextValve which is added as the base valve
 * to a web module's ad-hoc pipeline.
 *
 * A web module's ad-hoc pipeline is invoked for any of the web module's
 * ad-hoc paths.
 *
 * The AdHocContextValve is responsible for invoking the ad-hoc servlet
 * associated with the ad-hoc path.
 *
 * @author Jan Luehe
 */
public class AdHocContextValve implements Valve {

    private static final Logger LOGGER =
        LogDomains.getLogger(LogDomains.WEB_LOGGER);

    private static final String VALVE_INFO =
        "com.sun.enterprise.web.AdHocContextValve";

    // The web module with which this valve is associated
    private WebModule context;


    /**
     * Constructor.
     */
    public AdHocContextValve(WebModule context) {
        this.context = context;
    }


    /**
     * Returns descriptive information about this valve.
     */
    public String getInfo() {
        return VALVE_INFO;
    }


    /**
     * Processes the given request by passing it to the ad-hoc servlet
     * associated with the request path (which has been determined, by the
     * associated web module, to be an ad-hoc path).
     *
     * @param request The request to process
     * @param response The response to return
     */
    public int invoke(Request request, Response response)
            throws IOException, ServletException {

        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        HttpServletResponse hres = (HttpServletResponse) response.getResponse();

        String adHocServletName =
            context.getAdHocServletName(hreq.getServletPath());

        Wrapper adHocWrapper = (Wrapper) context.findChild(adHocServletName);
        if (adHocWrapper != null) {
            Servlet adHocServlet = null;
            try {
                adHocServlet = adHocWrapper.allocate();
                adHocServlet.service(hreq, hres);
            } catch (Throwable t) {
                hres.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                String msg = LOGGER.getResourceBundle().getString(
                    "webmodule.adHocContextValve.adHocServletServiceError");
                msg = MessageFormat.format(
                            msg,
                            new Object[] { hreq.getServletPath() });
                response.setDetailMessage(msg);
                return END_PIPELINE;
            } finally {
                if (adHocServlet != null) {
                    adHocWrapper.deallocate(adHocServlet);
                }
            }
        } else {
            hres.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String msg = LOGGER.getResourceBundle().getString(
                "webmodule.adHocContextValve.noAdHocServlet");
            msg = MessageFormat.format(
                            msg,
                            new Object[] { hreq.getServletPath() });
            response.setDetailMessage(msg);
            return END_PIPELINE;
        }

        return END_PIPELINE;
    }


    public void postInvoke(Request request, Response response)
            throws IOException, ServletException {
        // Do nothing
    }

}

