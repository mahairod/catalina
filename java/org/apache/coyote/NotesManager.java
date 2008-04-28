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

package org.apache.coyote;

/**
 * Simple API used to implement notes support in Request/Response object.
 *
 * @author Jeanfrancois Arcand
 */
public interface NotesManager<E> {

   
    /**
     * Add a note. 
     */
    public void setNote(int key, E value);


    /**
     * Return the note associated with key,
     */
    public E getNote(int key);
    
    
    /**
     * Remove the note associated with the key token.
     */
    public E removeNote(int key);
    
}
