/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

/*
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.enterprise.web.session;

/**
 * Represents each of the persistence mechanisms supported by the session
 * managers.
 */
public final class PersistenceType {

    // ------------------------------------------------------- Static Variables

    /**
     * Memory based persistence for sessions (i.e. none);
     */
    public static final PersistenceType MEMORY =
        new PersistenceType("memory");

    /**
     * File based persistence for sessions.
     */
    public static final PersistenceType FILE =
        new PersistenceType("file");

    /**
     * Custom/user implemented session manager.
     */
    public static final PersistenceType CUSTOM =
        new PersistenceType("custom");
    
    /**
     * old iWS 6.0 style session manager.
     */
    public static final PersistenceType S1WS60 =
        new PersistenceType("s1ws60");

    /**
     * old iWS 6.0 style
     * MMapSessionManager.
     */
    public static final PersistenceType MMAP =
        new PersistenceType("mmap");

    /**
     * JDBC based persistence for sessions.
     */
    public static final PersistenceType JDBC =
        new PersistenceType("jdbc");   
    
    /**
     * HADB based persistence for sessions.
     */
    public static final PersistenceType HA =
        new PersistenceType("ha");     
    
    /**
     * SJSWS replicated persistence for sessions.
     */
    public static final PersistenceType REPLICATED =
        new PersistenceType("replicated");

    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor that sets its type to the specified string.
     */
    private PersistenceType(String type) {
        _type = type;
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The persistence type specifier.
     */
    private String _type = null;

    // ------------------------------------------------------------- Properties
    
    /**
     * Returns a string describing the persistence mechanism that the
     * object represents.
     */
    public String getType() {
        return _type;
    }

    // --------------------------------------------------------- Static Methods

    /**
     * Parse the specified string and return the corresponding instance
     * of this class that represents the persistence type specified
     * in the string.
     */
    public static PersistenceType parseType(String type) {
        // Default persistence type is MEMORY
        PersistenceType pType = MEMORY;
        if (type != null) {
            if (type.equalsIgnoreCase(FILE.getType()))
                pType = FILE;
            else if (type.equalsIgnoreCase(CUSTOM.getType()))
                pType = CUSTOM;
            else if (type.equalsIgnoreCase(S1WS60.getType()))
                pType = S1WS60;
            else if (type.equalsIgnoreCase(MMAP.getType()))
                pType = MMAP;
            else if (type.equalsIgnoreCase(JDBC.getType()))
                pType = JDBC;            
            else if (type.equalsIgnoreCase(HA.getType()))
                pType = HA;
            else if (type.equalsIgnoreCase(REPLICATED.getType()))
                pType = REPLICATED; 
        }
        return pType;
    }
    
    /**
     * Parse the specified string and return the corresponding instance
     * of this class that represents the persistence type specified
     * in the string.  Default back into passed-in parameter
     */
    public static PersistenceType parseType(String type, PersistenceType defaultType) {
        // Default persistence type is defaultTypee
        PersistenceType pType = defaultType;
        if (type != null) {
            if (type.equalsIgnoreCase(MEMORY.getType()))
                pType = MEMORY;            
            else if (type.equalsIgnoreCase(FILE.getType()))
                pType = FILE;
            else if (type.equalsIgnoreCase(CUSTOM.getType()))
                pType = CUSTOM;
            else if (type.equalsIgnoreCase(S1WS60.getType()))
                pType = S1WS60;
            else if (type.equalsIgnoreCase(MMAP.getType()))
                pType = MMAP;
            else if (type.equalsIgnoreCase(JDBC.getType()))
                pType = JDBC;            
            else if (type.equalsIgnoreCase(HA.getType()))
                pType = HA;
            else if (type.equalsIgnoreCase(REPLICATED.getType()))
                pType = REPLICATED;    
        }
        return pType;
    }    

}

