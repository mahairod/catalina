/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.tomcat.util.threads;

import java.util.*;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;

/**
 * Manageable thread pool. 
 * 
 * @author Costin Manolache
 * @deprecated This was an attempt to introduce a JMX dependency. A better solution
 * was the ThreadPoolListener - which is more powerfull and provides the same
 * features. The class is here for backward compatibility, all the methods are in
 * super().  
 */
public class ThreadPoolMX extends ThreadPool {

    protected String domain; // not used 

    protected String name; // not used

    public ThreadPoolMX() {
        super();
    }

}
