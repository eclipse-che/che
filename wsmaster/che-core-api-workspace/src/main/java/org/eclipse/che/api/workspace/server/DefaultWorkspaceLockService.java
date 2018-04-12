/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import com.google.inject.Singleton;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.eclipse.che.commons.lang.concurrent.StripedLocks;
import org.eclipse.che.commons.lang.concurrent.Unlocker;

/**
 * Default implementation of {@link WorkspaceLockService} that is use {@link StripedLocks}.
 *
 * @author Anton Korneta
 */
@Singleton
public class DefaultWorkspaceLockService implements WorkspaceLockService {
  private final ReadWriteLock delegate;

  public DefaultWorkspaceLockService() {
    this.delegate = new ReentrantReadWriteLock();
  }

  @Override
  public Unlocker readLock() {
    return new LockUnlocker(delegate.readLock());
  }

  @Override
  public Unlocker writeLock() {
    return new LockUnlocker(delegate.writeLock());
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
}
