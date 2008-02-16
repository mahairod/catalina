/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.catalina.ssi;


/**
 * Exception used to tell SSIProcessor that it should stop processing SSI
 * commands. This is used to mimick the Apache behavior in #set with invalid
 * attributes.
 * 
 * @author Paul Speed
 * @author Dan Sandberg
 * @version $Revision: 1.4 $, $Date: 2007/05/05 05:32:20 $
 */
public class SSIStopProcessingException extends Exception {
}
