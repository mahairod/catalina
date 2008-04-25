/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.enterprise.web;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.appserv.ha.util.PersistenceTypeResolver;
import com.sun.logging.LogDomains;
import org.apache.catalina.Context;

public class PersistenceStrategyBuilderFactory {
    
    private static final Logger _logger = LogDomains.getLogger(
            LogDomains.WEB_LOGGER);

    // The path where ee builders reside
    private String _eeBuilderPath = null;

    private ServerConfigLookup serverConfigLookup;

    /**
     * Constructor.
     */
    public PersistenceStrategyBuilderFactory(
            ServerConfigLookup serverConfigLookup) {
        this.serverConfigLookup = serverConfigLookup;
        _eeBuilderPath = serverConfigLookup.getEEBuilderPathFromConfig();
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("_eeBuilderPath = " + _eeBuilderPath);
        }
    }

    /**
     * return the path where the ee builders reside
     * although this method allows this to be configurable
     * via an property in server.xml we do not expose it
     * and it should not be re-configured
     */    
    private String getEEBuilderPath() {
        return _eeBuilderPath;
    }
    
    /**
     * creates the correct implementation of PersistenceStrategyBuilder
     * if an invalid combination is input; an error is logged
     * and MemoryStrategyBuilder is returned
     */    
    public PersistenceStrategyBuilder createPersistenceStrategyBuilder(
            String persistenceType, String frequency, String scope,
            Context ctx) {

        String resolvedPersistenceType = null;
        String resolvedPersistenceFrequency = null;
        String resolvedPersistenceScope = null;

        PersistenceTypeResolver persistenceTypeResolver =
            getPersistenceTypeResolver();
        if (persistenceTypeResolver != null) {
            resolvedPersistenceType =
                persistenceTypeResolver.resolvePersistenceType(persistenceType);
        } else {
            resolvedPersistenceType = persistenceType;
        }

        if (resolvedPersistenceType.equalsIgnoreCase("memory") 
                || resolvedPersistenceType.equalsIgnoreCase("file")) {
            // Deliberately leaving frequency & scope null
        } else {
            resolvedPersistenceFrequency = frequency;
            resolvedPersistenceScope = scope;
        }

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("resolvedPersistenceType = " +
                           resolvedPersistenceType);
            _logger.finest("resolvedPersistenceFrequency = " +
                           resolvedPersistenceFrequency);
            _logger.finest("resolvedPersistenceScope = " +
                           resolvedPersistenceScope);
        }
        
        String passedInPersistenceType = persistenceType;
        PersistenceStrategyBuilder builder =
            new MemoryStrategyBuilder(serverConfigLookup);
        String className = createClassNameFrom(resolvedPersistenceType,
            resolvedPersistenceFrequency, resolvedPersistenceScope);
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest(
                "PersistenceStrategyBuilderFactory>>createPersistenceStrategyBuilder: "
                + "CandidateBuilderClassName = " + className);
        }

        try {
            builder = (PersistenceStrategyBuilder)
                (Class.forName(className)).newInstance();
        } catch (Exception ex) {
            Object[] params = { getApplicationId(ctx), persistenceType,
                                frequency, scope };
            _logger.log(Level.WARNING,
                        "webcontainer.invalidSessionManagerConfig",
                        params);            
        }

        builder.setPersistenceFrequency(frequency);
        builder.setPersistenceScope(scope);
        builder.setPassedInPersistenceType(passedInPersistenceType);

        return builder;
    } 

    /**
     * creates the correct implementation of PersistenceStrategyBuilder
     * if an invalid combination is input; an error is logged
     * and MemoryStrategyBuilder is returned
     *
     * @param persistenceType
     * @param frequency
     * @param scope
     */     
    PersistenceStrategyBuilder createPersistenceStrategyBuilder(String persistenceType, String frequency, String scope) {
        String passedInPersistenceType = persistenceType;
        PersistenceStrategyBuilder builder =
            new MemoryStrategyBuilder(serverConfigLookup);
        String className = createClassNameFrom(persistenceType, frequency,
                                               scope);
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("PersistenceStrategyBuilderFactory>>createPersistenceStrategyBuilder: "
                           + "CandidateBuilderClassName = " + className);
        }

        try {
            builder = (PersistenceStrategyBuilder)
                (Class.forName(className)).newInstance();
        } catch (Exception ex) {
            Object[] params = { persistenceType, frequency, scope };
            _logger.log(Level.WARNING,
                        "webcontainer.invalidSessionManagerConfig",
                        params);            
        }

        builder.setPersistenceFrequency(frequency);
        builder.setPersistenceScope(scope);
        builder.setPassedInPersistenceType(passedInPersistenceType);

        return builder;
    }
    
    private PersistenceTypeResolver getPersistenceTypeResolver() {
        String resolverClassName 
            = "com.sun.enterprise.ee.web.sessmgmt.EEPersistenceTypeResolver";
    
        PersistenceTypeResolver persistenceTypeResolver = null;
        try {
            persistenceTypeResolver = (PersistenceTypeResolver)
                (Class.forName(resolverClassName)).newInstance();
        } catch (Exception ex) {
            _logger.finest("unable to create persistence type resolver");
        } 

        return persistenceTypeResolver;
    }

    /**
     * returns the application id for the module
     *
     * @param ctx the context
     */    
    public String getApplicationId(Context ctx) {
        return ((com.sun.enterprise.web.WebModule)ctx).getID();
    }    
    
    /**
     * returns an appropriately camel-cased String
     * that is a candidate class name for a builder
     * if persistenceType is "memory" or "file" this returns
     * the correct class name and package name for these classes
     * i.e. com.iplanet.ias.web
     * otherwise they are in com.sun.appserv.ee.web.initialization
     */     
    private String createClassNameFrom(String persistenceType,
                                       String frequency,
                                       String scope) {
        StringBuilder sb = new StringBuilder();
        // Using package name will mean this will work
        // even if class is moved to another package
        String pkg = getClass().getPackage().getName();
        if(!(persistenceType.equalsIgnoreCase("memory") 
                || persistenceType.equalsIgnoreCase("file"))) {
            pkg = getEEBuilderPath();
        }
        sb.append(pkg + ".");
        sb.append(camelCase(persistenceType));
        if (frequency != null) {
            sb.append(camelCase(frequency));
        }
        if (scope != null) {
            sb.append(camelCase(scope));
        }
        sb.append("StrategyBuilder");
        return sb.toString();
    }      

    /**
     * this method strips out all non-alpha characters; camelCases the result
     *
     * @param inputString
     */     
    private String camelCase(String inputString) {
        String strippedString = stripNonAlphas(inputString);
        String firstLetter = (strippedString.substring(0, 1)).toUpperCase();
        String remainingPart = 
            (strippedString.substring(1, strippedString.length())).toLowerCase();
        return firstLetter + remainingPart;
    }

    /**
     * this method strips out all non-alpha characters
     *
     * @param inputString
     */     
    private String stripNonAlphas(String inputString) {
        StringBuilder sb = new StringBuilder(50);
        for(int i=0; i<inputString.length(); i++) {
            char nextChar = inputString.charAt(i);
            if(Character.isLetter(nextChar)) {
                sb.append(nextChar);
            }
        }
        return sb.toString();
    }    
}
