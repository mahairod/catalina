/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.coyote;

import java.util.Locale;

/**
 * Constants.
 *
 * @author Remy Maucherat
 */
public final class Constants {


    // -------------------------------------------------------------- Constants


    public static final String DEFAULT_CHARACTER_ENCODING="ISO-8859-1";


    public static final String LOCALE_DEFAULT = "en";


    public static final Locale DEFAULT_LOCALE = new Locale(LOCALE_DEFAULT, "");

    public static final String SESSION_COOKIE_NAME = "JSESSIONID";

    public static final int MAX_NOTES = 32;


    // Request states
    public static final int STAGE_NEW = 0;
    public static final int STAGE_PARSE = 1;
    public static final int STAGE_PREPARE = 2;
    public static final int STAGE_SERVICE = 3;
    public static final int STAGE_ENDINPUT = 4;
    public static final int STAGE_ENDOUTPUT = 5;
    public static final int STAGE_KEEPALIVE = 6;
    public static final int STAGE_ENDED = 7;


}
