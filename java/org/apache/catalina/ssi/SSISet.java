/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.catalina.ssi;


import java.io.PrintWriter;
/**
 * Implements the Server-side #set command
 * 
 * @author Paul Speed
 * @author Dan Sandberg
 * @author David Becker
 * @version $Revision: 1.4 $, $Date: 2007/05/05 05:32:20 $
 */
public class SSISet implements SSICommand {
    /**
     * @see SSICommand
     */
    public long process(SSIMediator ssiMediator, String commandName,
            String[] paramNames, String[] paramValues, PrintWriter writer)
            throws SSIStopProcessingException {
        long lastModified = 0;
        String errorMessage = ssiMediator.getConfigErrMsg();
        String variableName = null;
        for (int i = 0; i < paramNames.length; i++) {
            String paramName = paramNames[i];
            String paramValue = paramValues[i];
            if (paramName.equalsIgnoreCase("var")) {
                variableName = paramValue;
            } else if (paramName.equalsIgnoreCase("value")) {
                if (variableName != null) {
                    String substitutedValue = ssiMediator
                            .substituteVariables(paramValue);
                    ssiMediator.setVariableValue(variableName,
                            substitutedValue);
                    lastModified = System.currentTimeMillis();
                } else {
                    ssiMediator.log("#set--no variable specified");
                    writer.write(errorMessage);
                    throw new SSIStopProcessingException();
                }
            } else {
                ssiMediator.log("#set--Invalid attribute: " + paramName);
                writer.write(errorMessage);
                throw new SSIStopProcessingException();
            }
        }
        return lastModified;
    }
}
