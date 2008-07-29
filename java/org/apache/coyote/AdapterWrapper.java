/*
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 *
 *
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.coyote;


/**
 * The org.apache.coyoteAdapter implementation which deletes all methods to 
 * com.sun.grizzly.tcp.Adapter.
 *
 * @author Amy Roh
 */
public class AdapterWrapper implements Adapter {
   
    private com.sun.grizzly.tcp.Adapter adapter;
    
    public AdapterWrapper(com.sun.grizzly.tcp.Adapter adapter) {
        this.adapter = adapter;
    }
    
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
    public void service(Request req, Response res) throws Exception {
        adapter.service(new com.sun.grizzly.tcp.Request(), 
                        new com.sun.grizzly.tcp.Response());
    }
    
    
    // START GlassFish Issue 798
    /** 
     * Finish the response and recycle the request/response tokens. Base on
     * the connection header, the underlying socket transport will be closed
     */   
    public void afterService(Request req, Response res) throws Exception {
        adapter.afterService(new com.sun.grizzly.tcp.Request(), 
                             new com.sun.grizzly.tcp.Response());
    }
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
    public void fireAdapterEvent(String type, Object data) {
        adapter.fireAdapterEvent(type, data);
    }
    // END SJSAS 6349248    
}
