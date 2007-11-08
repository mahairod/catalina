/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.coyote;


/**
 * Adapter. This represents the entry point to a coyote-based servlet
 * container.
 *
 * @author Remy Maucherat
 */
public interface Adapter {
    
    // START SJSAS 6349248
    public final static String 
            CONNECTION_PROCESSING_STARTED = "connectionProcessingStarted";
    
    public final static String 
            CONNECTION_PROCESSING_COMPLETED = "connectionProcessingCompleted";
    
    public final static String 
            REQUEST_PROCESSING_STARTED = "requestProcessingStarted";
    
    public final static String 
            REQUEST_PROCESSING_COMPLETED = "requestProcessingCompleted";
    // END SJSAS 6349248
   
    /** 
     * Call the service method, and notify all listeners
     *
     * @exception Exception if an error happens during handling of
     *   the request. Common errors are:
     *   <ul><li>IOException if an input/output error occurs and we are
     *   processing an included servlet (otherwise it is swallowed and
     *   handled by the top level error handler mechanism)
     *       <li>ServletException if a servlet throws an exception and
     *  we are processing an included servlet (otherwise it is swallowed
     *  and handled by the top level error handler mechanism)
     *  </ul>
     *  Tomcat should be able to handle and log any other exception ( including
     *  runtime exceptions )
     */
    public void service(Request req, Response res)
	throws Exception;
    
    
    // START GlassFish Issue 798
    /** 
     * Finish the response and recycle the request/response tokens. Base on
     * the connection header, the underlying socket transport will be closed
     */   
    public void afterService(Request req, Response res) throws Exception;
    // END GlassFish Issue 798


    // START SJSAS 6349248   
    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Adapter.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param data Event data
     */
    public void fireAdapterEvent(String type, Object data);
    // END SJSAS 6349248    
}
