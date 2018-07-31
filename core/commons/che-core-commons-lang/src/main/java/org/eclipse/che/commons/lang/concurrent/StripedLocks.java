/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.commons.lang.concurrent;

import com.google.common.util.concurrent.Striped;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Helper class to use striped locks in try-with-resources construction. Examples of usage:
 *
 * <pre>{@code
 * StripedLocks stripedLocks = new StripedLocks(16);
 * try (Unlocker u = stripedLocks.writeLock(myKey)) {
 *     syncedObject.write();
 * }
 *
 * try (Unlocker u = stripedLocks.readLock(myKey)) {
 *     syncedObject.read();
 * }
 *
 * try (Unlocker u = stripedLocks.writeAllLock(myKey)) {
 *     for (ObjectToSync objectToSync : allObjectsToSync) {
 *         objectToSync.write();
 *     }
 * }
 * }</pre>
 *
 * @author Alexander Garagatyi
 * @author Sergii Leschenko
 * @author Yevhenii Voevodin
 */
public class StripedLocks {

  private final Striped<ReadWriteLock> striped;

  public StripedLocks(int stripesCount) {
    striped = Striped.readWriteLock(stripesCount);
  }

  /** Acquire read lock for provided key. */
  public Unlocker readLock(String key) {
    Lock lock = striped.get(key).readLock();
    lock.lock();
    return new LockUnlocker(lock);
  }

  /** Acquire write lock for provided key. */
  public Unlocker writeLock(String key) {
    Lock lock = striped.get(key).writeLock();
    lock.lock();
    return new LockUnlocker(lock);
  }

  /** Acquire write lock for all possible keys. */
  public Unlocker writeAllLock() {
    Lock[] locks = getAllWriteLocks();
    for (Lock lock : locks) {
      lock.lock();
    }
    return new LocksUnlocker(locks);
  }

  private Lock[] getAllWriteLocks() {
    Lock[] locks = new Lock[striped.size()];
    for (int i = 0; i < striped.size(); i++) {
      locks[i] = striped.getAt(i).writeLock();
    }
    return locks;
  }

  private static class LockUnlocker implements Unlocker {

    private final Lock lock;

    private LockUnlocker(Lock lock) {
      this.lock = lock;
    }

    @Override
    public void unlock() {
      lock.unlock();
    }
  }

  private static class LocksUnlocker implements Unlocker {

    private final Lock[] locks;

    private LocksUnlocker(Lock[] locks) {
      this.locks = locks;
    }

    @Override
    public void unlock() {
      for (Lock lock : locks) {
        lock.unlock();
      }
    }
  }
}
