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
 * Simple API to handle notes with Request/Response object.
 *
 * @author Jeanfrancois Arcand
 */
public class NotesManagerImpl implements NotesManager<Object>{
    private Object notes[] = new Object[Constants.MAX_NOTES];
    
    /**
     * Add a note.
     */
    public void setNote(int key, Object value){
        notes[key] = value;
    }
    
    
    /**
     * Return the note associated with key,
     */
    public Object getNote(int key){
        return notes[key];
    }
    
    
    /**
     * Remove the note associated with the key token.
     */
    public Object removeNote(int key){
        Object o =  notes[key];
        notes[key] = null;
        return o;
    }
    
}


