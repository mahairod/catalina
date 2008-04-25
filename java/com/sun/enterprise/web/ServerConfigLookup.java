/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 */

package com.sun.enterprise.web;

import org.jvnet.hk2.config.ConfigBeanProxy;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.config.serverbeans.AvailabilityService;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ExtensionModule;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.ManagerProperties;
import com.sun.enterprise.config.serverbeans.Property;
import com.sun.enterprise.config.serverbeans.SessionConfig;
import com.sun.enterprise.config.serverbeans.SessionManager;
import com.sun.enterprise.config.serverbeans.SessionProperties;
import com.sun.enterprise.config.serverbeans.StoreProperties;
import com.sun.enterprise.config.serverbeans.WebContainerAvailability;
import com.sun.enterprise.web.session.PersistenceType;
import com.sun.logging.LogDomains;

public class ServerConfigLookup {

    protected static final Logger _logger = LogDomains.getLogger(
            LogDomains.WEB_LOGGER);

    /**
     * The property name in domain.xml to obtain
     * the EE builder path - this property is not expected
     * now to change and if it ever did, then the directory
     * and package structure for the builder classes would
     * have to change also
     */  
    private static final String EE_BUILDER_PATH_PROPERTY_NAME =
        "ee-builder-path";      
  
    /**
     * The default path to the EE persistence strategy builders 
     */ 
    private static final String DEFAULT_EE_BUILDER_PATH =
        "com.sun.enterprise.ee.web.initialization";

    private Config configBean;

    /**
     * Constructor
     */
    public ServerConfigLookup(Config configBean) {
        this.configBean = configBean;
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
        if (sessionConfigBean == null) {
            return null;
        }
        
        return sessionConfigBean.getSessionProperties();
    }

    /**
     * Get the EE_BUILDER_PATH from server.xml.
     * this defaults to EE_BUILDER_PATH but can be modified
     * this is the fully qualified path to the EE builders
     */
    public String getEEBuilderPathFromConfig() {
        return getWebContainerAvailabilityPropertyString(
            EE_BUILDER_PATH_PROPERTY_NAME, DEFAULT_EE_BUILDER_PATH);
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
     * Get the availability-enabled from domain.xml.
     * return false if not found
     */   
    public boolean getAvailabilityEnabledFromConfig() {
        AvailabilityService as = getAvailabilityService();
        if (as == null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("AvailabilityService was not defined - check domain.xml");
            }
            return false;
        }        

        Boolean bool = toBoolean(as.getAvailabilityEnabled());
        if (bool == null) {
            return false;
        } else {
            return bool.booleanValue();
        }       
    }

    /**
     * Get the web-container-availability element from domain.xml.
     * return null if not found
     */     
    private WebContainerAvailability getWebContainerAvailability() {
        AvailabilityService availabilityServiceBean = this.getAvailabilityService();
        if (availabilityServiceBean == null) {
            return null;
        }

        return availabilityServiceBean.getWebContainerAvailability();
    } 
    
    /**
     * Get the String value of the property under web-container-availability 
     * element from domain.xml whose name matches propName
     * return null if not found
     * @param propName
     */     
    protected String getWebContainerAvailabilityPropertyString(
                String propName) {
        return getWebContainerAvailabilityPropertyString(propName, null);
    }

    /**
     * Get the String value of the property under web-container-availability 
     * element from domain.xml whose name matches propName
     * return defaultValue if not found
     * @param propName
     */    
    protected String getWebContainerAvailabilityPropertyString(
                String propName,
                String defaultValue) {
        WebContainerAvailability wcAvailabilityBean = getWebContainerAvailability();
        if (wcAvailabilityBean == null) {
            return defaultValue;
        }

        List<Property> props = wcAvailabilityBean.getProperty();
        if (props == null) {
            return defaultValue;
        }

        for (Property prop : props) {
            String name = prop.getName();
            String value = prop.getValue();
            if (name.equalsIgnoreCase(propName)) {
                return value;
            }
        }

        return defaultValue;
    } 


    /**
     * Get the availability-enabled for the web container from domain.xml.
     * return inherited global availability-enabled if not found
     */   
    public boolean getWebContainerAvailabilityEnabledFromConfig() {
        boolean globalAvailabilityEnabled = getAvailabilityEnabledFromConfig();
        WebContainerAvailability was = getWebContainerAvailability();
        if (was == null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("WebContainerAvailability not defined - check domain.xml");
            }
            return globalAvailabilityEnabled;
        }
        
        Boolean bool = toBoolean(was.getAvailabilityEnabled());
        if (bool == null) {
            return globalAvailabilityEnabled;
        } else {
            return bool.booleanValue();
        }       
    } 
    
    /**
     * Get the availability-enabled for the web container from domain.xml.
     * return inherited global availability-enabled if not found
     */   
    public boolean getWebContainerAvailabilityEnabledFromConfig(boolean inheritedValue) {
        WebContainerAvailability was = getWebContainerAvailability();
        if (was == null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("WebContainerAvailability not defined - check domain.xml");
            }
            return inheritedValue;
        }
        
        Boolean bool = toBoolean(was.getAvailabilityEnabled());
        if (bool == null) {
            return inheritedValue;
        } else {
            return bool.booleanValue();
        }       
    }    

    /**
     * Get the availability-enabled from domain.xml.
     * This takes into account:
     * global
     * web-container-availability
     * web-module (if stand-alone)
     * return false if not found
     */   
    public boolean calculateWebAvailabilityEnabledFromConfig(WebModule ctx) { 
        // global availability from <availability-service> element
        boolean globalAvailability = getAvailabilityEnabledFromConfig();
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("globalAvailability = " + globalAvailability);
        }

        // web container availability from <web-container-availability>
        // sub-element
        boolean webContainerAvailability = 
            getWebContainerAvailabilityEnabledFromConfig(globalAvailability);
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("webContainerAvailability = " + webContainerAvailability);
        }        

        String webModuleAvailabilityString = null;
        J2eeApplication j2eeApp = ctx.getApplicationBean();
        if (j2eeApp == null) {
            // the stand-alone web module case
            ConfigBeanProxy bean = ctx.getBean();
            if (bean != null) {
                if (bean instanceof com.sun.enterprise.config.serverbeans.WebModule) {
                    webModuleAvailabilityString =
                        ((com.sun.enterprise.config.serverbeans.WebModule) bean).getAvailabilityEnabled();
                } else {
                    webModuleAvailabilityString =
                        ((ExtensionModule) bean).getAvailabilityEnabled();
                }
            }
        } else {
            // the j2ee application case
            webModuleAvailabilityString = j2eeApp.getAvailabilityEnabled();
        }

        boolean webModuleAvailability = false;
        Boolean bool = toBoolean(webModuleAvailabilityString);
        if (bool != null) {
            webModuleAvailability = bool.booleanValue();
        }       

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("webModuleAvailability = " + webModuleAvailability);
        }

        return globalAvailability 
                && webContainerAvailability 
                && webModuleAvailability;
    }    

    /**
     * Get the persistenceType from domain.xml.
     * return null if not found
     */
    public PersistenceType getPersistenceTypeFromConfig() {
        String persistenceTypeString = null;      
        PersistenceType persistenceType = null;

        WebContainerAvailability webContainerAvailabilityBean =
            getWebContainerAvailability();
        if (webContainerAvailabilityBean == null) {
            return null;
        }
        persistenceTypeString = webContainerAvailabilityBean.getPersistenceType();

        if (persistenceTypeString != null) {
            persistenceType = PersistenceType.parseType(persistenceTypeString);
        }
        if (persistenceType != null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("SERVER.XML persistenceType= " + persistenceType.getType());
            }
        } else {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("SERVER.XML persistenceType missing");
            }
        }

        return persistenceType;
    }     
    
    /**
     * Get the persistenceFrequency from domain.xml.
     * return null if not found
     */
    public String getPersistenceFrequencyFromConfig() { 
        WebContainerAvailability webContainerAvailabilityBean =
            getWebContainerAvailability();
        if (webContainerAvailabilityBean == null) {
            return null;
        }
        return webContainerAvailabilityBean.getPersistenceFrequency();      
    }
    
    /**
     * Get the persistenceScope from domain.xml.
     * return null if not found
     */
    public String getPersistenceScopeFromConfig() {
        WebContainerAvailability webContainerAvailabilityBean =
            getWebContainerAvailability();
        if (webContainerAvailabilityBean == null) {
            return null;
        }
        return webContainerAvailabilityBean.getPersistenceScope(); 
    }     

    /**
     * convert the input value to the appropriate Boolean value
     * if input value is null, return null
     */     
    protected Boolean toBoolean(String value) {
        if (value == null) return null;
        
        if (value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("on")
                || value.equalsIgnoreCase("1")) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }
}
