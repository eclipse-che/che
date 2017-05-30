/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs;

/**
 * Advisory file locks. It does not prevent access to the file from other programs.
 * <p/>
 * Usage:
 * <pre>
 *      PathLockFactory lockFactory = ...
 *
 *      public void doSomething(Path path)
 *      {
 *         PathLock exclusiveLock = lockFactory.getLock(path, true).acquire(30000);
 *         try
 *         {
 *            ... // do something
 *         }
 *         finally
 *         {
 *            exclusiveLock.release();
 *         }
 *      }
 * </pre>
 *
 * @author andrew00x>
 */
public final class PathLockFactory {
    private static final int MAX_RECURSIVE_LOCKS = (1 << 10) - 1;
    /** Max number of threads allowed to access file. */
    private final int maxThreads;
    // Tail of the "lock table".
    private final Node tail = new Node(null, 0, null);

    /**
     * @param maxThreads
     *         the max number of threads are allowed to access one file. Typically this parameter should be big enough to
     *         avoid blocking threads that need to obtain NOT exclusive lock.
     */
    public PathLockFactory(int maxThreads) {
        if (maxThreads < 1) {
            throw new IllegalArgumentException();
        }
        this.maxThreads = maxThreads;
    }

    public PathLock getLock(Path path, boolean exclusive) {
        return new PathLock(path, exclusive ? maxThreads : 1);
    }

    private synchronized void acquire(Path path, int permits) {
        while (!tryAcquire(path, permits)) {
            try {
                wait();
            } catch (InterruptedException e) {
                notifyAll();
                throw new RuntimeException(e);
            }
        }
    }

    private synchronized void acquire(Path path, int permits, long timeoutMilliseconds) {
        final long endTime = System.currentTimeMillis() + timeoutMilliseconds;
        long waitTime = timeoutMilliseconds;
        while (!tryAcquire(path, permits)) {
            try {
                wait(waitTime);
            } catch (InterruptedException e) {
                notifyAll();
                throw new RuntimeException(e);
            }
            long now = System.currentTimeMillis();
            if (now >= endTime) {
                throw new RuntimeException(String.format("Get lock timeout for '%s'. ", path));
            }
            waitTime = endTime - now;
        }
    }

    private synchronized void release(Path path, int permits) {
        Node node = tail;
        while (node != null) {
            Node prev = node.prev;
            if (prev == null) {
                break;
            }
            if (prev.path.equals(path)) {
                if (prev.threadDeep == 1) {
                    // If last recursive lock.
                    prev.permits += permits;
                    if (prev.permits >= maxThreads) {
                        // remove
                        node.prev = prev.prev;
                        prev.prev = null;
                    }
                } else {
                    --prev.threadDeep;
                }
            }
            node = node.prev;
        }
        notifyAll();
        //System.err.printf(">>>>> release: %s : %d%n", path, permits);
    }

    private boolean tryAcquire(Path path, int permits) {
        //System.err.printf(">>>>> acquire: %s : %d%n", path, permits);
        Node node = tail.prev;
        final Thread current = Thread.currentThread();
        while (node != null) {
            if (node.path.equals(path)) {
                if (node.threadId == current.getId()) {
                    // Current thread already has direct lock for this path
                    if (node.threadDeep > MAX_RECURSIVE_LOCKS) {
                        throw new Error("Max number of recursive locks exceeded. ");
                    }
                    ++node.threadDeep;
                    return true;
                }
                if (node.permits > permits) {
                    // Lock already exists and current thread is not owner of this lock,
                    // but lock is not exclusive and we can "share" it for other thread.
                    node.permits -= permits; // decrement number of allowed concurrent threads
                    return true;
                }
                // Lock is exclusive or max number of allowed concurrent thread is reached.
                return false;
            } else if ((node.path.isChild(path) || path.isChild(node.path)) && node.permits <= permits) {
                // Found some path which already has lock that prevents us to get required permits.
                // There is two possibilities:
                // 1. Parent of the path we try to lock already locked
                // 2. Child of the path we try to lock already locked
                // Need to check is such lock obtained by current thread or not.
                // If such lock obtained by other thread stop here immediately there is no reasons to continue.
                if (node.threadId != current.getId()) {
                    return false;
                }
            }
            node = node.prev;
        }
        // If we are here there is no lock for path yet.
        tail.prev = new Node(path, maxThreads - permits, tail.prev);
        return true;
    }

    public synchronized void checkClean() {
        assert tail.prev == null;
    }

   /* =============================================== */

    private static class Node {
        final Path path;
        final long threadId = Thread.currentThread().getId();
        int  permits;
        int  threadDeep;
        Node prev;

        Node(Path path, int permits, Node prev) {
            this.path = path;
            this.permits = permits;
            this.prev = prev;
            threadDeep = 1;
        }

        @Override
        public String toString() {
            return "Node{" +
                   "path=" + path +
                   ", threadId=" + threadId +
                   ", permits=" + permits +
                   ", prev=" + prev +
                   '}';
        }
    }

    public final class PathLock {
        private final Path path;
        private final int  permits;

        private PathLock(Path path, int permits) {
            this.path = path;
            this.permits = permits;
        }

        /**
         * Acquire permit for file. Method is blocked until permit available.
         *
         * @return this PathLock instance
         */
        public PathLock acquire() {
            PathLockFactory.this.acquire(path, permits);
            return this;
        }

        /**
         * Acquire permit for file if it becomes available within the given timeout. It is the same as method {@link
         * #acquire()} but with waiting timeout. If waiting timeout reached then PathLockTimeoutException thrown.
         *
         * @param timeoutMilliseconds
         *         maximum time (in milliseconds) to wait for access permit
         * @return this PathLock instance
         * @throws RuntimeException
         *         if waiting timeout reached
         */
        public PathLock acquire(long timeoutMilliseconds) {
            PathLockFactory.this.acquire(path, permits, timeoutMilliseconds);
            return this;
        }

        /** Release file permit. */
        public void release() {
            PathLockFactory.this.release(path, permits);
        }

        /** Returns <code>true</code> if this lock is exclusive and <code>false</code> otherwise. */
        public boolean isExclusive() {
            return permits == PathLockFactory.this.maxThreads;
        }
    }
}
