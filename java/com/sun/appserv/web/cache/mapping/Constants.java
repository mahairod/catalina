/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package com.sun.appserv.web.cache.mapping;

public class Constants {
    /** field scope 
     */
    public static final int SCOPE_CONTEXT_ATTRIBUTE = 1;
    public static final int SCOPE_REQUEST_HEADER = 2; 
    public static final int SCOPE_REQUEST_PARAMETER = 3; 
    public static final int SCOPE_REQUEST_COOKIE = 4; 
    public static final int SCOPE_REQUEST_ATTRIBUTE = 5; 
    public static final int SCOPE_SESSION_ATTRIBUTE = 6; 
    public static final int SCOPE_SESSION_ID = 7; 

    // field match expression 
    public static final int MATCH_EQUALS = 1; 
    public static final int MATCH_GREATER = 2; 
    public static final int MATCH_LESSER = 3; 
    public static final int MATCH_NOT_EQUALS = 4; 
    public static final int MATCH_IN_RANGE = 5;
}
