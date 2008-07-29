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
 * Protocol implementation of com.sun.grizzly.tcp.ProtocolHandler which
 * is a wrapper around org.apache.coyote.ProtocolHandler
 * All com.sun.grizzly.tcp.ProtocolHandler methods delegate to the adaptee,
 * org.apache.coyote.ProtocolHandler after doing the required conversion of arguments
 *
 * @author Amy Roh
 */

public class ProtocolHandlerAdapter implements com.sun.grizzly.tcp.ProtocolHandler {

    protected org.apache.coyote.ProtocolHandler adaptee;
    
    protected com.sun.grizzly.tcp.Adapter adapter;
    
    public ProtocolHandlerAdapter(org.apache.coyote.ProtocolHandler adaptee) {
        this.adaptee = adaptee;
    }

    /**
     * Pass config info.
     */
    public void setAttribute(String name, Object value) {
        adaptee.setAttribute(name, value);
    }
  
    public Object getAttribute(String name) {
        return adaptee.getAttribute(name);
    }
  
    /**
     * The adapter, used to call the connector.
     */
    public void setAdapter(com.sun.grizzly.tcp.Adapter adapter) {
        this.adapter = adapter;
        adaptee.setAdapter(new AdapterWrapper(adapter));
    }
  
    public com.sun.grizzly.tcp.Adapter getAdapter() {
        return adapter;
    }
  
    /**
     * Init the protocol.
     */
    public void init() throws Exception {
        adaptee.init();
    }
  
    /**
     * Start the protocol.
     */
    public void start() throws Exception {
        adaptee.start();
    }

    public void destroy() throws Exception {
        adaptee.destroy();
    }
    
}