/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.tomcat.util.log;

import org.apache.tomcat.util.log.*;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.StringWriter;

import java.util.*;

import com.sun.org.apache.commons.logging.*;

/**
 *  Log using common-logging.
 *
 * @author Costin Manolache
 */
public  class CommonLogHandler extends LogHandler {

    private Hashtable loggers=new Hashtable();
    
    /**
     * Prints log message and stack trace.
     * This method should be overriden by real logger implementations
     *
     * @param	prefix		optional prefix. 
     * @param	message		the message to log. 
     * @param	t		the exception that was thrown.
     * @param	verbosityLevel	what type of message is this?
     * 				(WARNING/DEBUG/INFO etc)
     */
    public void log(String prefix, String msg, Throwable t,
		    int verbosityLevel)
    {
        if( prefix==null ) prefix="tomcat";

        com.sun.org.apache.commons.logging.Log l=(com.sun.org.apache.commons.logging.Log)loggers.get( prefix );
        if( l==null ) {
            l=LogFactory.getLog( prefix );
            loggers.put( prefix, l );
        }
        
	if( verbosityLevel > this.level ) return;

        if( t==null ) {
            if( verbosityLevel == Log.FATAL )
                l.fatal(msg);
            else if( verbosityLevel == Log.ERROR )
                l.error( msg );
            else if( verbosityLevel == Log.WARNING )
                l.warn( msg );
            else if( verbosityLevel == Log.INFORMATION)
                l.info( msg );
            else if( verbosityLevel == Log.DEBUG )
                l.debug( msg );
        } else {
            if( verbosityLevel == Log.FATAL )
                l.fatal(msg, t);
            else if( verbosityLevel == Log.ERROR )
                l.error( msg, t );
            else if( verbosityLevel == Log.WARNING )
                l.warn( msg, t );
            else if( verbosityLevel == Log.INFORMATION)
                l.info( msg, t );
            else if( verbosityLevel == Log.DEBUG )
                l.debug( msg, t );
        }
    }

    /**
     * Flush the log. 
     */
    public void flush() {
	// Nothing - commons logging doesn't have the notion
    }

    /**
     * Close the log. 
     */
    public synchronized void close() {
	// Nothing - commons logging doesn't have the notion
    }
    
}
