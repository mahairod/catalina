/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package org.apache.tomcat.util.http.mapper;

import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;

/**
 * Mapping data.
 *
 * @author Remy Maucherat
 */
public class MappingData {

    public Object host = null;
    public Object context = null;
    public Object wrapper = null;
    public boolean jspWildCard = false;
    // START GlassFish 1024
    public boolean isDefaultContext = false;
    // END GlassFish 1024

    public MessageBytes contextPath = MessageBytes.newInstance();
    public MessageBytes requestPath = MessageBytes.newInstance();
    public MessageBytes wrapperPath = MessageBytes.newInstance();
    public MessageBytes pathInfo = MessageBytes.newInstance();

    public MessageBytes redirectPath = MessageBytes.newInstance();

    public void recycle() {
        host = null;
        context = null;
        wrapper = null;
        pathInfo.recycle();
        requestPath.recycle();
        wrapperPath.recycle();
        contextPath.recycle();
        redirectPath.recycle();
        jspWildCard = false;
        // START GlassFish 1024
        isDefaultContext = false;
        // END GlassFish 1024
    }

}
