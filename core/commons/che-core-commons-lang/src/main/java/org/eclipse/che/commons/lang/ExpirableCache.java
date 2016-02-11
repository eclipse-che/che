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
package org.eclipse.che.commons.lang;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @deprecated Use Guava cache or other cache implementations
 */
@Deprecated
public class ExpirableCache<K, V> {
    private final int                cacheSize;
    private final long               expiredAfter;
    private final int                queryCountBeforeCleanup;
    private final Map<K, MyEntry<V>> map;

    private int queryCount;

    public ExpirableCache(long expiredAfter, int cacheSize) {
        this.expiredAfter = expiredAfter;
        this.cacheSize = cacheSize;
        queryCountBeforeCleanup = 500;
        map = new LinkedHashMap<K, MyEntry<V>>(this.cacheSize + 1, 1.1f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, MyEntry<V>> eldest) {
                return size() > ExpirableCache.this.cacheSize;
            }
        };
    }

    public V get(K key) {
        if (++queryCount >= queryCountBeforeCleanup) {
            cleanup();
        }
        MyEntry<V> myEntry = map.get(key);
        if (myEntry != null) {
            if (System.currentTimeMillis() - myEntry.created < expiredAfter) {
                return myEntry.value;
            } else {
                map.remove(key);
            }
        }
        return null;
    }

    public void put(K key, V value) {
        if (++queryCount >= queryCountBeforeCleanup) {
            cleanup();
        }
        MyEntry<V> myEntry = map.get(key);
        if (myEntry != null) {
            myEntry.created = System.currentTimeMillis();
            myEntry.value = value;
        } else {
            map.put(key, new MyEntry<V>(value));
        }
    }

    @SuppressWarnings("unchecked")
    private void cleanup() {
        Object[] keys = map.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            K key = (K)keys[i];
            MyEntry<V> myEntry = map.get(key);
            if (myEntry != null) {
                if (System.currentTimeMillis() - myEntry.created > expiredAfter) {
                    map.remove(key);
                }
            }
        }
        queryCount = 0;
    }

    /** @return - number of cached entries. */
    public int getCacheSize() {
        return map.size();
    }

    private static class MyEntry<V> {
        V    value;
        long created;


        MyEntry(V value) {
            this.value = value;
            this.created = System.currentTimeMillis();
        }
    }
}
