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

/**
 * SLRUCache that loads value for key if it is not cached yet.
 *
 * @see SLRUCache
 * @deprecated Use Guava cache or other cache implementations
 */
@Deprecated
public abstract class LoadingValueSLRUCache<K, V> extends SLRUCache<K, V> {
    /**
     * @param protectedSize
     *         size of protected area.
     * @param probationarySize
     *         size of probationary area.
     */
    public LoadingValueSLRUCache(int protectedSize, int probationarySize) {
        super(protectedSize, probationarySize);
    }

    @Override
    public V get(K key) {
        V value = super.get(key);
        if (value != null) {
            return value;
        }
        value = loadValue(key);
        put(key, value);
        return value;
    }

    /**
     * Load value in implementation specific way.
     *
     * @param key
     *         key
     * @return value
     * @throws RuntimeException
     *         if failed to load value
     */
    protected abstract V loadValue(K key) throws RuntimeException;
}
