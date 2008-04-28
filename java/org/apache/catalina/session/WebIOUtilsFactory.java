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

package org.apache.catalina.session;

/**
 *
 * @author  Administrator
 */
public class WebIOUtilsFactory {
    
    private static final String IO_UTILITY_CLASS_NAME = "com.sun.ejb.base.io.IOUtilsCallerImpl";
    
    /** Creates a new instance of WebCustomObjectStreamFactory */
    public WebIOUtilsFactory() {
    }
    
    public IOUtilsCaller createWebIOUtil() {
        IOUtilsCaller webIOUtil = null;
        try {
            webIOUtil = 
                (IOUtilsCaller) (Class.forName(IO_UTILITY_CLASS_NAME)).newInstance();
        } catch (Exception ex) {
            //FIXME: log error
        }        
        return webIOUtil;
    }
     
    
}
