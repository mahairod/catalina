/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.web;

import com.sun.enterprise.v3.deployment.GenericSniffer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.Singleton;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;


/**
 * Implementation of the Sniffer for the web container.
 * 
 * @author Jerome Dochez
 */
@Service(name="web")
@Scoped(Singleton.class)
public class WebSniffer  extends GenericSniffer implements Sniffer {

    private static final String WEB_INF_CLASSES = "WEB-INF/classes";
    private static final String WEB_INF_LIB = "WEB-INF/lib";
    private static final String WAR_EXTENSION = ".war";

    public WebSniffer() {
        super("web", "WEB-INF/web.xml", null);
    }

    /**
     * Returns true if the passed file or directory is recognized by this
     * instance.
     *
     * @param location the file or directory to explore 
     * @param loader class loader for this application
     * @return true if this sniffer handles this application type
     */
    public boolean handles(ReadableArchive location, ClassLoader loader) {
        // first look for WEB-INF/web.xml
        if(super.handles(location, loader)) {
            return true;
        }

        // then look for WEB-INF/classes and WEB-INF/lib
        InputStream is;
        try {
            if (location.exists(WEB_INF_CLASSES)) {
                return true;
            }

            if (location.exists(WEB_INF_LIB)) {
                return true;
            }
        } catch (IOException e) {
            // ignore
        }

        return false;
    }

    final String[] containers = { "com.sun.enterprise.web.WebContainer" };
    public String[] getContainersNames() {
        return containers;
    }    

    /**
     * @return whether this sniffer should be visible to user
     *
     */
    public boolean isUserVisible() {
        return true;
    }
}
