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
package org.eclipse.che.commons.lang.concurrent;

import com.google.common.util.concurrent.Striped;

import java.util.concurrent.locks.ReadWriteLock;

/**
 * Helper class to use striped locks in try-with-resources construction.
 * </p>
 * Examples of usage:
 * <pre class="code"><code class="java">
 *     StripedLocks stripedLocks = new StripedLocks(16);
 *     try (CloseableLock lock = stripedLocks.acquireWriteLock(myKey)) {
 *         syncedObject.write();
 *     }
 *
 *     try (CloseableLock lock = stripedLocks.acquireReadLock(myKey)) {
 *         syncedObject.read();
 *     }
 *
 *     try (CloseableLock lock = stripedLocks.acquireWriteAllLock(myKey)) {
 *         for (ObjectToSync objectToSync : allObjectsToSync) {
 *             objectToSync.write();
 *         }
 *     }
 * </pre>
 *
 * @author Alexander Garagatyi
 * @author Sergii Leschenko
 */
// TODO consider usage of plain map with locks instead of Guava's Striped
public class StripedLocks {
    private final Striped<ReadWriteLock> striped;

    public StripedLocks(int stripesCount) {
        striped = Striped.readWriteLock(stripesCount);
    }

    /**
     * Acquire read lock for provided key.
     */
    public CloseableLock acquireReadLock(String key) {
        return new ReadLock(key);
    }

    /**
     * Acquire write lock for provided key.
     */
    public CloseableLock acquireWriteLock(String key) {
        return new WriteLock(key);
    }

    /**
     * Acquire write lock for all possible keys.
     */
    public CloseableLock acquireWriteAllLock() {
        return new WriteAllLock();
    }

    /**
     * Represents read lock for the provided key.
     * Can be used as {@link AutoCloseable} to release lock.
     */
    private class ReadLock implements CloseableLock {
        private String key;

        private ReadLock(String key) {
            this.key = key;
            striped.get(key).readLock().lock();
        }

        @Override
        public void close() {
            striped.get(key).readLock().unlock();
        }
    }

    /**
     * Represents write lock for the provided key.
     * Can be used as {@link AutoCloseable} to release lock.
     */
    private class WriteLock implements CloseableLock {
        private String key;

        private WriteLock(String key) {
            this.key = key;
            striped.get(key).writeLock().lock();
        }

        @Override
        public void close() {
            striped.get(key).writeLock().unlock();
        }
    }

    /**
     * Represents write lock for all possible keys.
     * Can be used as {@link AutoCloseable} to release locks.
     */
    private class WriteAllLock implements CloseableLock {
        private WriteAllLock() {
            for (int i = 0; i < striped.size(); i++) {
                striped.getAt(i).writeLock().lock();
            }
        }

        @Override
        public void close() {
            for (int i = 0; i < striped.size(); i++) {
                striped.getAt(i).writeLock().unlock();
            }
        }
    }
}
