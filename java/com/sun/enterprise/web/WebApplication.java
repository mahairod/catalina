/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 */

package com.sun.enterprise.web;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.Container;
import org.apache.coyote.tomcat5.CoyoteAdapter;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.v3.common.Result;
import com.sun.logging.LogDomains;
import com.sun.enterprise.v3.services.impl.EndpointRegistrationException;
import com.sun.enterprise.v3.services.impl.GrizzlyService;
import com.sun.enterprise.v3.services.impl.RequestDispatcher;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.web.loader.WebappClassLoader;

import java.util.List;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebApplication implements ApplicationContainer<WebBundleDescriptor> {

    private static final String ADMIN_VS = "__asadmin";
    final Logger logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);

    final WebContainer container;
    final WebModuleConfig wmInfo;
    final GrizzlyService grizzlyAdapter;

    public WebApplication(WebContainer container, WebModuleConfig config, GrizzlyService grizzlyAdapter) {
        this.container = container;
        this.wmInfo = config;
        this.grizzlyAdapter = grizzlyAdapter;
    }


    public boolean start(ClassLoader cl) {
        wmInfo.setAppClassLoader(cl);
        String vsIDs = wmInfo.getVirtualServers();
        List<String> vsList = StringUtils.parseStringList(vsIDs, " ,");
        return start(vsList);
    }

    boolean start(List<String> vsList) {

        final String contextRoot = wmInfo.getDescriptor().getContextRoot();
        boolean loadToAll = (vsList == null) || (vsList.size() == 0);

        // TODO : dochez : add action report here...
        List<Result<WebModule>> results = container.loadWebModule(wmInfo, "null");
        if (results==null) {
            logger.log(Level.SEVERE, "Unknown error, loadWebModule returned null, file a bug");
            return false;
        }

        logger.info("Loading application " + wmInfo.getDescriptor().getName()
                + " at " + wmInfo.getDescriptor().getContextRoot());
        for (Result<com.sun.enterprise.web.WebModule> result : results) {
            // result will be null if the web module was merely resumed
            if (result != null) { 
                if (result.isSuccess()) {
                    VirtualServer vs = (VirtualServer) result.result().getParent();
                    final Collection<String> c = new HashSet<String>();
                    c.add(vs.getID());
                    if (loadToAll || vsList.contains(vs.getName())
                            || isAliasMatched(vsList,vs)) {
                        for (int port : vs.getPorts()) {
                            CoyoteAdapter adapter =
                                container.adapterMap.get(Integer.valueOf(port));
                            //@TODO change EndportRegistrationException processing if required
                            try {
                                grizzlyAdapter.registerEndpoint(contextRoot,
                                    adapter.getPort(), c, adapter, this);
                            } catch(EndpointRegistrationException e) {
                                logger.log(Level.WARNING,
                                           "Error while deploying", e);
                            }
                        }
                    }
                } else {
                    logger.log(Level.SEVERE, "Error while deploying",
                               result.exception());
                    return false;
                }
            }
        }

        return true;
    }

    public boolean stop() {
        return stop(null);
    }

    boolean stop(List<String> targets) {

        boolean isLeftOver = false;
        boolean unloadFromAll = (targets == null) || (targets.size() == 0);
        final String ctxtRoot = getDescriptor().getContextRoot();

        Container[] hosts = container.engine.findChildren();
        for (int i = 0; i < hosts.length; i++) {
            StandardHost vs = (StandardHost) hosts[i];

            if (unloadFromAll && ADMIN_VS.equals(vs.getName())) {
                // Do not unload from __asadmin
                continue;
            }

            StandardContext ctxt = (StandardContext) vs.findChild(ctxtRoot);
            if (ctxt != null) {
                if (unloadFromAll
                        || targets.contains(vs.getName())
                        || isAliasMatched(targets, vs)) {
                    vs.removeChild(ctxt);
                    try {
                        ctxt.destroy();
                    } catch (Exception ex) {
                        logger.log(Level.WARNING,
                                "Unable to destroy web module "
                                        + ctxt, ex);
                    }
                    logger.info("Undeployed web module " + ctxt
                            + " from virtual server "
                            + vs.getName());
                    // ToDo : dochez : not good, we unregister from everywhere...
                    //@TODO change EndportRegistrationException processing if required
                    try {
                        grizzlyAdapter.unregisterEndpoint(ctxtRoot, this);
                    } catch (EndpointRegistrationException e) {
                        logger.log(Level.WARNING, "Error while undeploying", e);
                    }
                } else {
                    isLeftOver = true;
                }
            }
        }

        if ((unloadFromAll || !isLeftOver)
                && (getClassLoader() instanceof WebappClassLoader)) {
            try {
                ((WebappClassLoader) getClassLoader()).stop();
            } catch (Exception e) {
                logger.log(Level.WARNING,
                           "Unable to stop classloader for " + this, e);
            }
        }

        return true;
    }

    /**
     * Returns the class loader associated with this application
     *
     * @return ClassLoader for this app
     */
    public ClassLoader getClassLoader() {
        return wmInfo.getAppClassLoader();
    }

    WebContainer getContainer() {
        return container;
    }

    /**
     * Returns the deployment descriptor associated with this application
     *
     * @return deployment descriptor if they exist or null if not
     */
    public WebBundleDescriptor getDescriptor() {
        return wmInfo.getDescriptor();
    }

    /**
     * Suspends this application on all virtual servers.
     */
    public boolean suspend() {
        return suspend(null);
    }

    /**
     * Suspends this application on the specified virtual servers.
     *
     * @param the virtual servers on which to suspend this application
     */
    public boolean suspend(List<String> vsList) {
        return container.suspendWebModule(
            wmInfo.getDescriptor().getContextRoot(), "null", vsList);
    }

    /**
     * Resumes this application on all virtual servers.
     */
    public boolean resume() {
        return resume(null);
    }

    /**
     * Resumes this application on the specified virtual servers.
     *
     * @param the virtual servers on which to suspend this application
     */
    public boolean resume(List<String> vsList) {
        // WebContainer.loadWebModule(), which is called by start(), 
        // already checks if the web module has been suspended, and if so,
        // just resumes it and returns
        return start(vsList);
    }

    /*
     * @return true if the list of target virtual server names matches an
     * alias name of the given virtual server, and false otherwise
     */
    private boolean isAliasMatched(List targets, StandardHost vs){

        String[] aliasNames = vs.getAliases();
        for (int i=0; i<aliasNames.length; i++) {
            if (targets.contains(aliasNames[i]) ){
                return true;
            }
        }

        return false;
    }

    /**
     * Deploy on aliases as well as host.
     */
    private boolean verifyAlias(List vsList,VirtualServer vs){
        for(int i=0; i < vs.getAliases().length; i++){
            if (vsList.contains(vs.getAliases()[i]) ){
                return true;
            }
        }
        return false;
    }    
}
