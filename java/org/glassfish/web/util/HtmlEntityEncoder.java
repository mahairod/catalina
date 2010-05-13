/*
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.glassfish.web.util;

import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class encodes HTML display content for preventing XSS.
 */
public class HtmlEntityEncoder {

    private static final Logger log = Logger.getLogger(
        HtmlEntityEncoder.class.getName());

    //Array containing the safe characters set.
    protected BitSet safeCharacters = new BitSet(256);

    public HtmlEntityEncoder() {
        for (char i = 'a'; i <= 'z'; i++) {
            addSafeCharacter(i);
        }
        for (char i = 'A'; i <= 'Z'; i++) {
            addSafeCharacter(i);
        }
        for (char i = '0'; i <= '9'; i++) {
            addSafeCharacter(i);
        }
        
        // Grizzly UEncode includes ) ( -
        addSafeCharacter('$');
        addSafeCharacter('_');
        addSafeCharacter('.');

        addSafeCharacter('!');
        addSafeCharacter('*');
        addSafeCharacter('\\');
        addSafeCharacter(',');

        // unsafe chars for XSS
        // CR 6944384: < > " ' % ; ) ( & + - 
    }

    public void addSafeCharacter(char c) {
        safeCharacters.set(c);
    }

    public String encode(Object obj) {
        if (obj == null) {
            return null;
        } else {
            return encode(obj.toString());
        }
    }

    public String encode(String s) {
        if (s == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(s.length());

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (safeCharacters.get(c)) {
                sb.append(c);
            } else if (Character.isWhitespace(c)) {
                sb.append("&#").append((int)c).append(";");
            } else if (Character.isISOControl(c)) {
                // ignore
            } else if (Character.isHighSurrogate(c)) {
                if (i + 1 < s.length() && Character.isLowSurrogate(s.charAt(i + 1))) {
                    int codePoint = Character.toCodePoint(c, s.charAt(i + 1));
                    if (Character.isDefined(codePoint)) {
                        sb.append("&#").append(codePoint).append(";");
                    }
                }
                // else ignore this pair of chars
                i++;
            } else if (Character.isDefined(c)) {
                switch(c) {
                    case '&':
                        sb.append("&amp;");
                        break;
                    case '<':
                        sb.append("&lt;");
                        break;
                    case '>':
                        sb.append("&gt;");
                        break;
                    case '"':
                        sb.append("&quot;");
                        break;
                    default:
                        sb.append("&#").append((int)c).append(";");
                        break;
                }
            }
        }
        return sb.toString();
    }
}
