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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
//FIXME: move this to commons so it can be added back to api
//import com.sun.ejb.spi.io.NonSerializableObjectHandler;

/**
 *
 * @author  Administrator
 */
public interface IOUtilsCaller {
    
    public ObjectInputStream createObjectInputStream(
        InputStream is,
        boolean resolveObject,
        ClassLoader loader) throws Exception;
    
    public ObjectOutputStream createObjectOutputStream(
        OutputStream os,
        boolean replaceObject) throws IOException;  
    
}

