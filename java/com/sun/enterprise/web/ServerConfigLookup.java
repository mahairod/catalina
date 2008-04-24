/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 */

package com.sun.enterprise.web;

import com.sun.enterprise.config.serverbeans.AvailabilityService;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ManagerProperties;
import com.sun.enterprise.config.serverbeans.SessionConfig;
import com.sun.enterprise.config.serverbeans.SessionManager;
import com.sun.enterprise.config.serverbeans.SessionProperties;
import com.sun.enterprise.config.serverbeans.StoreProperties;

public class ServerConfigLookup {

    private Config configBean;

    public ServerConfigLookup(Config configBean) {
        this.configBean = configBean;
    }

    /**
     * Get the availability-service element from domain.xml.
     * return null if not found
     */     
    protected AvailabilityService getAvailabilityService() {
        if (configBean == null) {
            return null;
        }

        return configBean.getAvailabilityService();
    }    

    /**
     * Get the session manager bean from domain.xml
     * return null if not defined or other problem
     */  
    public SessionManager getInstanceSessionManager() { 
        if (configBean == null) {
            return null;
        }
        
        com.sun.enterprise.config.serverbeans.WebContainer webContainerBean 
            = configBean.getWebContainer();
        if (webContainerBean == null) {
            return null;
        }
        
        SessionConfig sessionConfigBean = webContainerBean.getSessionConfig();
        if (sessionConfigBean == null) {
            return null;
        }
        
        return sessionConfigBean.getSessionManager();
    }    
    
    /**
     * Get the manager properties bean from domain.xml
     * return null if not defined or other problem
     */  
    public ManagerProperties getInstanceSessionManagerManagerProperties() {
        
        SessionManager smBean = getInstanceSessionManager();
        if (smBean == null) {
            return null;
        }

        return smBean.getManagerProperties();
    } 
    
    /**
     * Get the store properties bean from domain.xml
     * return null if not defined or other problem
     */  
    public StoreProperties getInstanceSessionManagerStoreProperties() {
        
        SessionManager smBean = getInstanceSessionManager();
        if (smBean == null) {
            return null;
        }

        return smBean.getStoreProperties();
    } 

    /**
     * Get the session properties bean from server.xml
     * return null if not defined or other problem
     */      
    public SessionProperties getInstanceSessionProperties() { 
        if (configBean == null) {
            return null;
        }
        
        com.sun.enterprise.config.serverbeans.WebContainer webContainerBean 
            = configBean.getWebContainer();
        if (webContainerBean == null) {
            return null;
        }
        
        SessionConfig sessionConfigBean = webContainerBean.getSessionConfig();
        if(sessionConfigBean == null) {
            return null;
        }
        
        return sessionConfigBean.getSessionProperties();
    }        
}
