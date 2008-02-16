/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */


package org.apache.catalina.logger;


/**
 * Simple implementation of <b>Logger</b> that writes to System.err.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2005/12/08 01:27:46 $
 */

public class SystemErrLogger
    extends LoggerBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information about this implementation.
     */
    protected static final String info =
        "org.apache.catalina.logger.SystemErrLogger/1.0";


    // --------------------------------------------------------- Public Methods


    /**
     * Writes the specified message to a servlet log file, usually an event
     * log.  The name and type of the servlet log is specific to the
     * servlet container.
     *
     * @param msg A <code>String</code> specifying the message to be written
     *  to the log file
     */
    public void log(String msg) {

        System.err.println(msg);

    }


}
