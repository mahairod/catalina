/*
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package org.apache.tomcat.util.modeler.modules;

import org.apache.tomcat.util.modeler.ManagedBean;
import org.apache.tomcat.util.modeler.Registry;

import javax.management.ObjectName;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MbeansDescriptorsSerSource extends ModelerSource
{
    private static Logger log = Logger.getLogger(MbeansDescriptorsSerSource.class.getName());

    Registry registry;
    String type;
    List<ObjectName> mbeans=new ArrayList<ObjectName>();

    public void setRegistry(Registry reg) {
        this.registry=reg;
    }

    public void setLocation( String loc ) {
        this.location=loc;
    }

    /** Used if a single component is loaded
     *
     * @param type
     */
    public void setType( String type ) {
       this.type=type;
    }

    public void setSource( Object source ) {
        this.source=source;
    }

    public List<ObjectName> loadDescriptors( Registry registry, String location,
            String type, Object source) throws Exception {

        setRegistry(registry);
        setLocation(location);
        setType(type);
        setSource(source);
        execute();
        return mbeans;
    }

    public void execute() throws Exception {
        if( registry==null ) registry=Registry.getRegistry(null, null);
        long t1=System.currentTimeMillis();
        try {
            InputStream stream=null;
            if( source instanceof URL ) {
                stream=((URL)source).openStream();
            }
            if( source instanceof InputStream ) {
                stream=(InputStream)source;
            }
            if( stream==null ) {
                throw new Exception( "Can't process "+ source);
            }
            ObjectInputStream ois=new ObjectInputStream(stream);
            Thread.currentThread().setContextClassLoader(ManagedBean.class.getClassLoader());
            Object obj=ois.readObject();
            //log.log(Level.INFO, "Reading " + obj);
            ManagedBean beans[]=(ManagedBean[])obj;
            // after all are read without error
            for( int i=0; i<beans.length; i++ ) {
                registry.addManagedBean(beans[i]);
            }

        } catch( Exception ex ) {
            log.log(Level.SEVERE, "Error reading descriptors " + source + " " +  ex.toString(),
                    ex);
            throw ex;
        }
        long t2=System.currentTimeMillis();
        log.log(Level.INFO, "Reading descriptors ( ser ) " + (t2-t1));
    }
}
