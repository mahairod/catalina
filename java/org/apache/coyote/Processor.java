/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.coyote;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Processor.
 *
 * @author Remy Maucherat
 */
public interface Processor {


    public void setAdapter(Adapter adapter);


    public Adapter getAdapter();


    // START OF SJSAS 6231069
    //  public void process(InputStream input, OutputStream output)
    public boolean process(InputStream input, OutputStream output)
        throws Exception;
    // END OF SJSAS 6231069


}
