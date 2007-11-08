/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.coyote;


/**
 * Abstract the protocol implementation, including threading, etc.
 * Processor is single threaded and specific to stream-based protocols,
 * will not fit Jk protocols like JNI.
 *
 * This is the main interface to be implemented by a coyote connector.
 * (In contrast, Adapter is the main interface to be implemented by a
 * coyote servlet container.)
 *
 * @see Adapter
 *
 * @author Remy Maucherat
 * @author Costin Manolache
 */
public interface ProtocolHandler {


    /**
     * Pass config info.
     */
    public void setAttribute(String name, Object value);


    public Object getAttribute(String name);


    /**
     * The adapter, used to call the connector.
     */
    public void setAdapter(Adapter adapter);


    public Adapter getAdapter();


    /**
     * Init the protocol.
     */
    public void init()
        throws Exception;


    /**
     * Start the protocol.
     */
    public void start()
        throws Exception;


    public void destroy()
        throws Exception;


}
