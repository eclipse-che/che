/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.distributed.lock;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.locks.Lock;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.WorkspaceLockService;
import org.eclipse.che.commons.lang.concurrent.Unlocker;
import org.jgroups.JChannel;
import org.jgroups.blocks.locking.LockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JGroups based implementation of {@link WorkspaceLockService}.
 *
 * @author Anton Korneta
 */
@Singleton
public class JGroupsWorkspaceLockService implements WorkspaceLockService {
  private static final Logger LOG = LoggerFactory.getLogger(JGroupsWorkspaceLockService.class);

  private static final String CHANNEL_NAME = "WorkspaceLocks";

  private final LockService lockService;
  private final JChannel channel;

  @Inject
  public JGroupsWorkspaceLockService(@Named("jgroups.config.file") String confFile) {
    try {
      this.channel = new JChannel(confFile);
      this.lockService = new LockService(channel);
      channel.connect(CHANNEL_NAME);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public Unlocker readLock(String key) {
    // JGroups lock service does not contain an associated pair of read/write locks, that's why
    // there no way provide a reliable version of a read lock.
    return writeLock(key);
  }

  @Override
  public Unlocker writeLock(String key) {
    final Lock lock = lockService.getLock(key);
    lock.lock();
    return new UnlockerImpl(lock);
  }

  private class UnlockerImpl implements Unlocker {
    private final Lock lock;

    public UnlockerImpl(Lock lock) {
      this.lock = lock;
    }

    @Override
    public void unlock() {
      lock.unlock();
    }
  }

  /** Stops the workspace lock service. */
  public void shutdown() {
    try {
      channel.close();
    } catch (RuntimeException ex) {
      LOG.error("Failed to stop workspace locks service. Cause: " + ex.getMessage());
    }
  }
}
