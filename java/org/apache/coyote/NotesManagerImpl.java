/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
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


