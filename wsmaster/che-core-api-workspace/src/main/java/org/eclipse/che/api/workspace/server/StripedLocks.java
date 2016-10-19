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
package org.eclipse.che.api.workspace.server;

import com.google.common.util.concurrent.Striped;

import java.io.Closeable;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Helper class to use striped locks in try-with-resources construction.
 * </p>
 * Examples of usage:
 * <pre class="code"><code class="java">
 *     StripedLocks stripedLocks = new StripedLocks(16);
 *     try (StripedLocks.WriteLock lock = stripedLocks.acquireWriteLock(myKey)) {
 *         syncedObject.write();
 *     }
 *
 *     try (StripedLocks.ReadLock lock = stripedLocks.acquireReadLock(myKey)) {
 *         syncedObject.read();
 *     }
 *
 *     try (StripedLocks.WriteAllLock lock = stripedLocks.acquireWriteAllLock(myKey)) {
 *         for (ObjectToSync objectToSync : allObjectsToSync) {
 *             objectToSync.write();
 *         }
 *     }
 * </pre>
 *
 * @author Alexander Garagatyi
 */
// TODO consider usage of plain map with locks instead of Guava's Striped
// TODO consider moving to the util module of Che core
public class StripedLocks {
    private final Striped<ReadWriteLock> striped;

    public StripedLocks(int stripesCount) {
        striped = Striped.readWriteLock(stripesCount);
    }

    /**
     * Acquire read lock for provided key.
     */
    public ReadLock acquireReadLock(String key) {
        return new ReadLock(key);
    }

    /**
     * Acquire write lock for provided key.
     */
    public WriteLock acquireWriteLock(String key) {
        return new WriteLock(key);
    }

    /**
     * Acquire write lock for all possible keys.
     */
    public WriteAllLock acquireWriteAllLock() {
        return new WriteAllLock();
    }

    /**
     * Represents read lock for the provided key.
     * Can be used as {@link AutoCloseable} to release lock.
     */
    public class ReadLock implements Closeable {
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
    public class WriteLock implements Closeable {
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
    public class WriteAllLock implements Closeable {
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
