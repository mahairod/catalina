/*
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 */

package org.apache.catalina.util;

import java.util.*;

/**
 * Adapter class which wraps an <tt>Iterable</tt> over an
 * <tt>Enumeration</tt>, to support foreach-style iteration over the
 * <tt>Enumeration</tt>.
 */
public final class IterableAdapter<T> implements Iterable<T> {

    // The Enumeration over which to iterate
    private Enumeration<T> en;

    /**
     * Constructor
     *
     * @param en the Enumeration over which to iterate
     */
    public IterableAdapter(Enumeration<T> en) {
        this.en = en;
    }

    public Iterator<T> iterator() {

        return new Iterator<T>() {

            public boolean hasNext() {
                return en.hasMoreElements();
            }

            public T next() {
                return en.nextElement();
            }

            public void remove() {
                throw new UnsupportedOperationException(
                    "remove not supported");
            }
        };
    }
}
