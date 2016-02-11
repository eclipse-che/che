/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.server;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a> */
public abstract class LazyIterator<T> implements Iterator<T> {
    public static final LazyIterator<Object> EMPTY_ITEMS_ITERATOR = new EmptyIterator();

    private static class EmptyIterator extends LazyIterator<Object> {
        @Override
        protected void fetchNext() {
        }

        @Override
        public int size() {
            return 0;
        }
    }

    /** Empty iterator. */
    @SuppressWarnings("unchecked")
    public static <T> LazyIterator<T> emptyIterator() {
        return (LazyIterator<T>)EMPTY_ITEMS_ITERATOR;
    }

    private static class ListWrapper<T> extends LazyIterator<T> {
        private final int         size;
        private final Iterator<T> delegate;

        ListWrapper(List<T> list) {
            size = list.size();
            delegate = list.iterator();
            fetchNext();
        }

        @Override
        protected void fetchNext() {
            next = null;
            while (next == null && delegate.hasNext()) {
                next = delegate.next();
            }
        }

        @Override
        public int size() {
            return size;
        }
    }

    /** Wrapper for List which implements LazyIterator functionality. */
    public static <T> LazyIterator<T> fromList(List<T> list) {
        return new ListWrapper<>(list);
    }

    private static class SingletonIterator<T> extends LazyIterator<T> {
        SingletonIterator(T value) {
            next = value;
        }

        @Override
        protected void fetchNext() {
            next = null;
        }

        @Override
        public int size() {
            return 1;
        }
    }

    /** Singleton iterator. */
    public static <T> LazyIterator<T> singletonIterator(T value) {
        return new SingletonIterator<>(value);
    }

    // -----------------------------------

    protected T next;

    /** To fetch next item and set it in field <code>next</code> */
    protected abstract void fetchNext();

    /** @see java.util.Iterator#hasNext() */
    public boolean hasNext() {
        return next != null;
    }

    /** @see java.util.Iterator#next() */
    public final T next() {
        if (next == null) {
            throw new NoSuchElementException();
        }
        T n = next;
        fetchNext();
        return n;
    }

    /** @see java.util.Iterator#remove() */
    public final void remove() {
        throw new UnsupportedOperationException("remove");
    }

    /**
     * Get total number of items in iterator. If not able determine number of items then -1 will be returned.
     *
     * @return number of items or -1
     */
    public int size() {
        return -1;
    }

    /**
     * Skip specified number of element in collection.
     *
     * @param skip
     *         the number of items to skip
     * @throws NoSuchElementException
     *         if skipped past the last item in the iterator
     */
    public void skip(int skip) throws NoSuchElementException {
        while (skip-- > 0) {
            fetchNext();
            if (next == null) {
                throw new NoSuchElementException();
            }
        }
    }
}
