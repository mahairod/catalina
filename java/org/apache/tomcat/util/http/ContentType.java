/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.tomcat.util.http;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

/**
 * Usefull methods for Content-Type processing
 * 
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author Harish Prabandham
 * @author costin@eng.sun.com
 */
public class ContentType {

    // Basically return everything after ";charset="
    // If no charset specified, use the HTTP default (ASCII) character set.
    public static String getCharsetFromContentType(String type) {
        if (type == null) {
            return null;
        }
        int semi = type.indexOf(";");
        if (semi == -1) {
            return null;
        }
        int charsetLocation = type.indexOf("charset=", semi);
        if (charsetLocation == -1) {
            return null;
        }
	String afterCharset = type.substring(charsetLocation + 8);
        // The charset value in a Content-Type header is allowed to be quoted
        // and charset values can't contain quotes.  Just convert any quote
        // chars into spaces and let trim clean things up.
        afterCharset = afterCharset.replace('"', ' ');
        String encoding = afterCharset.trim();
        return encoding;
    }


    // Bad method: the user may set the charset explicitely
    
//     /** Utility method for parsing the mime type and setting
//      *  the encoding to locale. Also, convert from java Locale to mime
//      *  encodings
//      */
//     public static String constructLocalizedContentType(String type,
// 							Locale loc) {
//         // Cut off everything after the semicolon
//         int semi = type.indexOf(";");
//         if (semi != -1) {
//             type = type.substring(0, semi);
//         }

//         // Append the appropriate charset, based on the locale
//         String charset = LocaleToCharsetMap.getCharset(loc);
//         if (charset != null) {
//             type = type + "; charset=" + charset;
//         }

//         return type;
//     }

}
