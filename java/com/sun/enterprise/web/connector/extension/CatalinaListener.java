/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */
package com.sun.enterprise.web.connector.extension;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Host;

/**
 * Listener used to receive events from Catalina when a <code>Context</code>
 * is removed or when a <code>Host</code> is removed.
 *
 * @author Jean-Francois Arcand
 */
public class CatalinaListener  implements ContainerListener{
    
    public void containerEvent(ContainerEvent event) {    
        if (Container.REMOVE_CHILD_EVENT.equals(event.getType()) ) {
            Context context;
            String contextPath;
            Host host;

            Object container = event.getData();            
            if ( container instanceof Context) {
                context = (Context)container;
                
                if ( context != null 
                        && context.findConstraints().length == 0 
                        && context.findFilterDefs().length == 0 ){
                                
                    contextPath = context.getPath();
                    host = (Host)context.getParent();
                    int[] ports = host.getPorts();
                    for (int i=0; i < ports.length; i++){
                        removeContextPath(ports[i],contextPath); 
                    }
                }
            } 
        }  
    }  
    
    
    /**
     * Remove from the <code>FileCache</code> all entries related to 
     * the <code>Context</code> path.
     * @param port the <code>FileCacheFactory</code> port
     * @param contextPath the <code>Context</code> path
     */
    private void removeContextPath(int port, String contextPath) {        
        ArrayList<GrizzlyConfig> list = 
                GrizzlyConfig.getGrizzlyConfigInstances();
        for(GrizzlyConfig config: list){
            if (config.getPort() == port){
                config.invokeGrizzly("removeCacheEntry",
                        new Object[]{contextPath},
                        new String[]{"java.lang.String"});
            }
        }
    }  
}

