/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.catalina.ssi;


import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
/**
 * Implements the Server-side #printenv command
 * 
 * @author Dan Sandberg
 * @author David Becker
 * @version $Revision: 1.3 $, $Date: 2007/02/13 19:16:21 $
 */
public class SSIPrintenv implements SSICommand {
    /**
     * @see SSICommand
     */
    public long process(SSIMediator ssiMediator, String commandName,
            String[] paramNames, String[] paramValues, PrintWriter writer) {
    	long lastModified = 0;
        //any arguments should produce an error
        if (paramNames.length > 0) {
            String errorMessage = ssiMediator.getConfigErrMsg();
            writer.write(errorMessage);
        } else {
            Collection variableNames = ssiMediator.getVariableNames();
            Iterator iter = variableNames.iterator();
            while (iter.hasNext()) {
                String variableName = (String)iter.next();
                String variableValue = ssiMediator
                        .getVariableValue(variableName);
                //This shouldn't happen, since all the variable names must
                // have values
                if (variableValue == null) {
                    variableValue = "(none)";
                }
                writer.write(variableName);
                writer.write('=');
                writer.write(variableValue);
                writer.write('\n');
                lastModified = System.currentTimeMillis();
            }
        }
        return lastModified;
    }
}
