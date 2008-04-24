/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 */

package com.sun.enterprise.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.Container;
import org.apache.catalina.Globals;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.core.StandardPipeline;
//import com.sun.web.security.RealmAdapter;

/**
 * Pipeline whose invoke logic checks if a given request path represents
 * an ad-hoc path: If so, this pipeline delegates the request to the
 * ad-hoc pipeline of its associated web module. Otherwise, this pipeline
 * processes the request.
 */
public class WebPipeline extends StandardPipeline {

    private WebModule webModule;
    
    /** 
     * creates an instance of WebPipeline
     * @param container
     */       
    public WebPipeline(Container container) {
        super(container);
        if(container instanceof WebModule) {
            this.webModule = (WebModule)container;
        }
    }    

    /**
     * Processes the specified request, and produces the appropriate
     * response, by invoking the first valve (if any) of this pipeline, or
     * the pipeline's basic valve.
     *
     * If the request path to process identifies an ad-hoc path, the
     * web module's ad-hoc pipeline is invoked.
     *
     * @param request The request to process
     * @param response The response to return
     */
    public void invoke(Request request, Response response)
            throws IOException, ServletException {

        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        if (webModule != null &&
                webModule.getAdHocServletName(hreq.getServletPath()) != null) {
            webModule.getAdHocPipeline().invoke(request, response);
        } else {
            /*
            RealmAdapter realmAdapter = (RealmAdapter)webModule.getRealm();
            if (realmAdapter != null &&
                    realmAdapter.isSecurityExtensionEnabled()){
                doChainInvoke(request, response);
            } else {
            */
                doInvoke(request, response);
            /*
            }
            */
        }
    }    
}
