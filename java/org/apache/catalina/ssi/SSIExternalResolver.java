/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.catalina.ssi;


import java.io.IOException;
import java.util.Collection;
import java.util.Date;
/**
 * Interface used by SSIMediator to talk to the 'outside world' ( usually a
 * servlet )
 * 
 * @author Dan Sandberg
 * @version $Revision: 1.4 $, $Date: 2007/05/05 05:32:19 $
 */
public interface SSIExternalResolver {
    /**
     * Adds any external variables to the variableNames collection.
     * 
     * @param variableNames
     *            the collection to add to
     */
    public void addVariableNames(Collection variableNames);


    public String getVariableValue(String name);


    /**
     * Set the named variable to the specified value. If value is null, then
     * the variable will be removed ( ie. a call to getVariableValue will
     * return null )
     * 
     * @param name
     *            of the variable
     * @param value
     *            of the variable
     */
    public void setVariableValue(String name, String value);


    /**
     * Returns the current date. This is useful for putting the SSI stuff in a
     * regression test. Since you can make the current date a constant, it
     * makes testing easier since the output won't change.
     * 
     * @return the data
     */
    public Date getCurrentDate();


    public long getFileSize(String path, boolean virtual) throws IOException;


    public long getFileLastModified(String path, boolean virtual)
            throws IOException;


    public String getFileText(String path, boolean virtual) throws IOException;


    public void log(String message, Throwable throwable);
}
