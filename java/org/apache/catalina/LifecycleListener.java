/*
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.apache.catalina;

import java.util.EventObject;

/**
 * Interface defining a listener for significant events (including "component
 * start" and "component stop" generated by a component that implements the
 * Lifecycle interface.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1.2.1 $ $Date: 2007/08/17 15:46:26 $
 */

public interface LifecycleListener {

    /**
     * Acknowledge the occurrence of the specified event.
     *
     * @param event LifecycleEvent that has occurred
     */
    public void lifecycleEvent(LifecycleEvent event)
        throws LifecycleException;
}
