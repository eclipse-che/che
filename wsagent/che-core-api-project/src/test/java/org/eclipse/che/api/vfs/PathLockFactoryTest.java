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

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
public class PathLockFactoryTest extends TestCase {
    private final int  maxThreads = 3;
    private final Path path       = Path.of("/a/b/c"); // Path not need to be real path on file system

    private PathLockFactory pathLockFactory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pathLockFactory = new PathLockFactory(maxThreads);
    }

    public void testLock() throws Exception {
        final AtomicBoolean acquired = new AtomicBoolean(false);
        final CountDownLatch waiter = new CountDownLatch(1);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    pathLockFactory.getLock(path, true).acquire();
                    acquired.set(true);
                } finally {
                    waiter.countDown();
                }
            }
        };
        t.start();
        waiter.await();
        assertTrue(acquired.get());
    }

    public void testConcurrentExclusiveLocks() throws Throwable {
        final AtomicInteger acquired = new AtomicInteger(0);
        final CountDownLatch waiter = new CountDownLatch(3);
        final List<Throwable> errors = new ArrayList<>(3);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                PathLockFactory.PathLock exclusiveLock = pathLockFactory.getLock(path, true);
                try {
                    exclusiveLock.acquire();
                    // Only one thread has exclusive access
                    assertEquals(0, acquired.getAndIncrement());
                    Thread.sleep(100);
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    acquired.getAndDecrement();
                    exclusiveLock.release();
                    waiter.countDown();
                }
            }
        };
        new Thread(task).start();
        new Thread(task).start();
        new Thread(task).start();
        waiter.await();
        assertEquals(0, acquired.get()); // all locks must be released
        if (!errors.isEmpty()) {
            throw errors.get(0);
        }
    }

    public void testLockTimeout() throws Exception {
        final CountDownLatch starter = new CountDownLatch(1);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                PathLockFactory.PathLock exclusiveLock = pathLockFactory.getLock(path, true);
                try {
                    exclusiveLock.acquire();
                    starter.countDown();
                    Thread.sleep(2000); // get lock and sleep
                } catch (InterruptedException ignored) {
                } finally {
                    exclusiveLock.release();
                }
            }
        };
        new Thread(task).start();
        starter.await(); // wait while child thread acquire exclusive lock
        PathLockFactory.PathLock timeoutExclusiveLock = pathLockFactory.getLock(path, true);
        try {
            // Wait lock timeout is much less then sleep time of child thread.
            // Here we must be failed to get exclusive permit.
            timeoutExclusiveLock.acquire(100);
            fail();
        } catch (RuntimeException e) {
            // OK
        }
    }

    public void testConcurrentLocks() throws Throwable {
        final AtomicInteger acquired = new AtomicInteger(0);
        final CountDownLatch starter = new CountDownLatch(1);
        final CountDownLatch waiter = new CountDownLatch(2);
        Runnable task1 = new Runnable() {
            @Override
            public void run() {
                PathLockFactory.PathLock lock = pathLockFactory.getLock(path, false);
                lock.acquire();
                acquired.incrementAndGet();
                starter.countDown();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                } finally {
                    acquired.getAndDecrement();
                    lock.release();
                    waiter.countDown();
                }
            }
        };
        final List<Throwable> errors = new ArrayList<>(1);
        Runnable task2 = new Runnable() {
            @Override
            public void run() {
                PathLockFactory.PathLock exclusiveLock = pathLockFactory.getLock(path, true);
                try {
                    exclusiveLock.acquire();
                    // This thread must be blocked while another thread keeps lock.
                    assertEquals(0, acquired.getAndIncrement());
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    acquired.getAndDecrement();
                    exclusiveLock.release();
                    waiter.countDown();
                }
            }
        };
        new Thread(task1).start();
        starter.await();
        new Thread(task2).start();
        waiter.await();
        assertEquals(0, acquired.get()); // all locks must be released

        if (!errors.isEmpty()) {
            throw errors.get(0);
        }
    }

    public void testHierarchyLock() throws Throwable {
        final AtomicInteger acquired = new AtomicInteger(0);
        final Path parent = path.getParent();
        final CountDownLatch starter = new CountDownLatch(1);
        final CountDownLatch waiter = new CountDownLatch(2);
        Runnable parentTask = new Runnable() {
            @Override
            public void run() {
                PathLockFactory.PathLock lock = pathLockFactory.getLock(parent, true);
                lock.acquire();
                acquired.incrementAndGet();
                starter.countDown();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                } finally {
                    acquired.getAndDecrement();
                    lock.release();
                    waiter.countDown();
                }
            }
        };
        final List<Throwable> errors = new ArrayList<>(1);
        Runnable childTask = new Runnable() {
            @Override
            public void run() {
                PathLockFactory.PathLock lock = pathLockFactory.getLock(path, false);
                try {
                    lock.acquire();
                    // This thread must be blocked while another thread keeps lock.
                    assertEquals(0, acquired.getAndIncrement());
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    lock.release();
                    acquired.getAndDecrement();
                    waiter.countDown();
                }
            }
        };
        new Thread(parentTask).start();
        starter.await();
        new Thread(childTask).start();
        waiter.await();
        assertEquals(0, acquired.get()); // all locks must be released

        if (!errors.isEmpty()) {
            throw errors.get(0);
        }
    }

    public void testLockSameThread() throws Exception {
        final AtomicInteger acquired = new AtomicInteger(0);
        final CountDownLatch waiter = new CountDownLatch(1);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    PathLockFactory.PathLock lock1 = pathLockFactory.getLock(path, true);
                    PathLockFactory.PathLock lock2 = pathLockFactory.getLock(path, true);
                    lock1.acquire();
                    acquired.incrementAndGet();
                    lock2.acquire(1000); // try with timeout.
                    acquired.incrementAndGet();
                } finally {
                    waiter.countDown();
                }
            }
        };
        new Thread(task).start();
        waiter.await();
        assertEquals(2, acquired.get());
    }
}
