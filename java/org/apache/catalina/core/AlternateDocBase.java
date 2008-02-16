/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.catalina.core;

import java.util.ArrayList;
import javax.naming.directory.DirContext;
import org.apache.naming.resources.ProxyDirContext;

public class AlternateDocBase {

    enum UrlPatternType { EXACT, WILDCARD, EXTENSION };

    private String pattern;
    private UrlPatternType patternType;
    private int patternSlashCount;
    
    private String docBase;
    private String basePath;
    private String patternSuffix;
    private String wildcardPath;
    private ProxyDirContext resources;
    private DirContext webappResources;


    /**
     * Sets the url pattern of this AlternateDocBase.
     *
     * @param urlPattern The url pattern of this AlternateDocBase
     */
    public void setUrlPattern(String urlPattern) {
        this.pattern = urlPattern;

        this.patternSlashCount = getSlashCount(urlPattern);
        if (urlPattern.endsWith("/*")) {
            this.patternType = UrlPatternType.WILDCARD;
            this.wildcardPath = urlPattern.substring(
                0, urlPattern.length()-1);
        } else if (urlPattern.startsWith("*.")) {
            this.patternType = UrlPatternType.EXTENSION;
            this.patternSuffix = urlPattern.substring(1);
        } else {
            this.patternType = UrlPatternType.EXACT;
        }
    }


    /**
     * Gets the url pattern of this AlternateDocBase.
     *
     * @return The url pattern of this AlternativeDocBase
     */
    public String getUrlPattern() {
        return pattern;
    }


    /**
     * Gets the url pattern type (exact, wildcard, extension) of this
     * AlternateDocBase.
     *
     * @return The url pattern type (exact, wildcard, extension) of this
     * AlternativeDocBase
     */
    public UrlPatternType getUrlPatternType() {
        return patternType;
    }


    /**
     * Gets the number of slashes in the url pattern of this AlternateDocBase.
     *
     * @return Number of slashes in the url pattern of this AlternateDocBase.
     */
    public int getUrlPatternSlashCount() {
        return patternSlashCount;
    }


    /**
     * Gets the extension suffix of the url pattern of this AlternateDocBase.
     *
     * @return The extension suffix of the url pattern of this
     * AlternateDocBase, or null if the url pattern is not of type 'extension'
     */
    public String getUrlPatternSuffix() {
        return this.patternSuffix;
    }


    /**
     * Gets the wildcard path of this AlternateDocBase (this is the path
     * specified by the wildcard pattern, minus the trailing '*').
     *
     * @return The wildcard path of this AlternateDocBase, or null if the
     * pattern associated with this AlternateDocBase is not a wildcard pattern
     */ 
    public String getUrlPatternWildcardPath() {
        return this.wildcardPath;
    }


    /**
     * Sets the (possibly relative) doc base path of this AlternateDocBase.
     *
     * @param docBase The (possibly relative) doc base path of this
     * AlternateDocBase
     */
    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }


    /**
     * Gets the (possibly relative) doc base path of this AlternateDocBase.
     *
     * @return The (possibly relative) doc base path of this AlternativeDocBase
     */
    public String getDocBase() {
        return docBase;
    }


    /**
     * Sets the absolute doc base path of this AlternateDocBase.
     *
     * @param basePath The absolute doc base path of this AlternateDocBase
     */
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }


    /**
     * Gets the absolute doc base path of this AlternateDocBase.
     *
     * @return The absolute doc base path of this AlternativeDocBase
     */
    public String getBasePath() {
        return basePath;
    }


    /**
     * Sets the proxied resources of this AlternateDocBase.
     *
     * @param resources The proxied resources of this AlternateDocBase
     */
    public void setResources(ProxyDirContext resources) {
        this.resources = resources;
    }


    /**
     * Gets the proxied resources of this AlternateDocBase.
     *
     * @return The proxied resources of this AlternateDocBase
     */
    public ProxyDirContext getResources() {
        return resources;
    }


    /**
     * Sets the non-proxied resources of this AlternateDocBase.
     *
     * @param resources The non-proxied resources of this AlternateDocBase
     */
    public void setWebappResources(DirContext webappResources) {
        this.webappResources = webappResources;
    }


    /**
     * Gets the non-proxied resources of this AlternateDocBase.
     *
     * @return The non-proxied resources of this AlternateDocBase
     */
    public DirContext getWebappResources() {
        return webappResources;
    }


    /**
     * Attempts to match the given request path against one of the given
     * alternate doc bases.
     *
     * @return The alternate doc base whose url pattern matches the given path,
     * or null if no matching alternate doc base could be found
     */
    public static AlternateDocBase findMatch(
                    String path,
                    ArrayList<AlternateDocBase> alternateDocBases) {

        if (alternateDocBases == null) {
            return null;
        }

        AlternateDocBase match = null;
        AlternateDocBase wildcardMatch = null;
        AlternateDocBase extensionMatch = null;
        int maxSlashCountMatch = 0;

        int pathSlashCount = getSlashCount(path);

        for (int i=0; i<alternateDocBases.size(); i++) {

            AlternateDocBase alternateDocBase = alternateDocBases.get(i);
            String pattern = alternateDocBase.getUrlPattern();
            int patternSlashCount = alternateDocBase.getUrlPatternSlashCount();
            AlternateDocBase.UrlPatternType type =
                alternateDocBase.getUrlPatternType();
            String wildcardPath = alternateDocBase.getUrlPatternWildcardPath();

            if (type == UrlPatternType.EXACT && path.equals(pattern)) {
                // Exact match found
                match = alternateDocBase;
                break;
            } else if (type == UrlPatternType.WILDCARD
                    && pathSlashCount >= patternSlashCount
                    && patternSlashCount > maxSlashCountMatch
                    && path.startsWith(wildcardPath)) {
                // We've found a longer wildcard match
                wildcardMatch = alternateDocBase;
                maxSlashCountMatch = patternSlashCount;
            } else if (type == UrlPatternType.EXTENSION
                    && path.endsWith(alternateDocBase.getUrlPatternSuffix())) {
                extensionMatch = alternateDocBase;
            }
        }

        if (match == null) {
            if (wildcardMatch != null) {
                match = wildcardMatch;
            } else {
                match = extensionMatch;
            }
        }

        return match;
    }


    /**
     * Determines the number for forward slashes in the given string.
     *
     * @param s The string whose forward slashes to count
     *
     * @return The number of forward slashes
     */
    private static int getSlashCount(String s) {

        int count = 0;

        if (s != null) {
            int index = s.indexOf('/');
            while (index >= 0) {
                count++;
                index = s.indexOf('/', index+1);
            }
        }

        return count;
    }

}
