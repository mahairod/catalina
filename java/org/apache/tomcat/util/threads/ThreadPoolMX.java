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



package org.apache.tomcat.util.threads;

import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
