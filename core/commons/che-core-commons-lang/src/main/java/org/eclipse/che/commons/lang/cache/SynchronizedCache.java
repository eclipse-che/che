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
package org.eclipse.che.commons.lang.cache;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Synchronized cache.
 *
 * @see Cache
 * @deprecated Use Guava cache or other cache implementations
 */
@Deprecated
public final class SynchronizedCache<K, V> implements Cache<K, V> {
    private final Cache<K, V> delegate;

    public SynchronizedCache(Cache<K, V> cache) {
        delegate = cache;
    }

    @Override
    public synchronized V get(K key) {
        return delegate.get(key);
    }

    @Override
    public synchronized V put(K key, V value) {
        return delegate.put(key, value);
    }

    @Override
    public synchronized V remove(K key) {
        return delegate.remove(key);
    }

    @Override
    public synchronized boolean contains(K key) {
        return delegate.contains(key);
    }

    @Override
    public synchronized void clear() {
        delegate.clear();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        return delegate.iterator();
    }
}
