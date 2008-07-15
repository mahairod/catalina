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

package org.glassfish.web.valve;

import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.CometEvent;
import org.apache.catalina.Valve;

/**
 * Tomcat-style wrapper valve around GlassFish-style valve.
 *
 * This allows Tomcat- and GlassFish-style valves to be added to a 
 * pipeline in arbitrary order.
 *
 * @author jluehe
 */
public class TomcatValveAdapter implements Valve {

    // The next valve in the pipeline to be invoked
    private Valve next = null;

    // The wrapped GlassFish-style valve to which to delegate
    private GlassFishValve gfValve;

    /**
     * Constructor.
     *
     * @param gfValve The GlassFish-style valve to wrap
     */
    public TomcatValveAdapter(GlassFishValve gfValve) {
        this.gfValve = gfValve;
    }

    public String getInfo() {
        return gfValve.getInfo();
    }

    public Valve getNext() {
        return next;
    }

    public void setNext(Valve valve) {
        this.next = valve;
    }

    public void backgroundProcess() {
        // Deliberate no-op
    }

    /**
     * Delegates to the invoke() and postInvoke() methods of the wrapped
     * GlassFish-style valve.
     */
    public void invoke(org.apache.catalina.connector.Request request,
                       org.apache.catalina.connector.Response response)
            throws IOException, ServletException {
        int rc = gfValve.invoke(request, response);
        if (rc != GlassFishValve.INVOKE_NEXT) {
            return;
        }
        getNext().invoke(request, response);
        gfValve.postInvoke(request, response);
    }

    public void event(org.apache.catalina.connector.Request request,
                      org.apache.catalina.connector.Response response,
                      CometEvent event)
            throws IOException, ServletException {
        // Deliberate no-op
    }

}
