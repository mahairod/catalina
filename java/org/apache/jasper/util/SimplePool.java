/*
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 */

package org.apache.jasper.util;

/**
 * Simple object pool. Based on ThreadPool and few other classes
 *
 * The pool will ignore overflow and return null if empty.
 *
 * @author Gal Shachor
 * @author Costin
 */
public final class SimplePool  {

    private static final int DEFAULT_SIZE=16;

    /*
     * Where the threads are held.
     */
    private Object pool[];

    private int max;
    private int current=-1;

    private Object lock;
    
    public SimplePool() {
	this.max=DEFAULT_SIZE;
	this.pool=new Object[max];
	this.lock=new Object();
    }
    
    public SimplePool(int max) {
	this.max=max;
	this.pool=new Object[max];
	this.lock=new Object();
    }

    /**
     * Adds the given object to the pool, and does nothing if the pool is full
     */
    public void put(Object o) {
	synchronized( lock ) {
	    if( current < (max-1) ) {
		current += 1;
		pool[current] = o;
            }
	}
    }

    /**
     * Get an object from the pool, null if the pool is empty.
     */
    public Object get() {
	Object item = null;
	synchronized( lock ) {
	    if( current >= 0 ) {
		item = pool[current];
		current -= 1;
	    }
	}
	return item;
    }

    /**
     * Return the size of the pool
     */
    public int getMax() {
	return max;
    }
}
