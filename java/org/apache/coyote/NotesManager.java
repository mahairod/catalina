/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
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
