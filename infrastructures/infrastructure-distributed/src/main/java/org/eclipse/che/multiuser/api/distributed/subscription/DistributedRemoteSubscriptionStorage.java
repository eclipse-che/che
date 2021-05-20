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
package org.eclipse.che.multiuser.api.distributed.subscription;

import static org.slf4j.LoggerFactory.getLogger;

import com.google.inject.Singleton;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.notification.RemoteSubscriptionContext;
import org.eclipse.che.api.core.notification.RemoteSubscriptionStorage;
import org.jgroups.JChannel;
import org.jgroups.blocks.ReplicatedHashMap;
import org.jgroups.blocks.locking.LockService;
import org.slf4j.Logger;

/**
 * Replicated map-based implementation of {@link RemoteSubscriptionStorage}
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@Singleton
public class DistributedRemoteSubscriptionStorage implements RemoteSubscriptionStorage {

  private static final Logger LOG = getLogger(DistributedRemoteSubscriptionStorage.class);

  private static final String CHANNEL_NAME = "RemoteSubscriptionChannel";

  private final ReplicatedHashMap<String, Set<RemoteSubscriptionContext>> subscriptions;
  private final LockService lockService;
  private final JChannel channel;

  @Inject
  public DistributedRemoteSubscriptionStorage(@Named("jgroups.config.file") String confFile)
      throws Exception {
    try {
      channel = new JChannel(confFile);
      this.lockService = new LockService(channel);
      channel.connect(CHANNEL_NAME);
      subscriptions = new ReplicatedHashMap<>(channel);
      subscriptions.setBlockingUpdates(true);
      subscriptions.start(5000);
    } catch (Exception e) {
      LOG.error("Unable to create distributed event subscriptions map.", e);
      throw e;
    }
  }

  @Override
  public Set<RemoteSubscriptionContext> getByMethod(String method) {
    return subscriptions.getOrDefault(method, Collections.emptySet());
  }

  @Override
  public void addSubscription(String method, RemoteSubscriptionContext remoteSubscriptionContext) {
    Lock lock = lockService.getLock(method);
    lock.lock();
    try {
      Set<RemoteSubscriptionContext> existing =
          subscriptions.getOrDefault(method, ConcurrentHashMap.newKeySet(1));
      existing.add(remoteSubscriptionContext);
      subscriptions.put(method, existing);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void removeSubscription(String method, String endpointId) {
    Lock lock = lockService.getLock(method);
    lock.lock();
    try {
      Set<RemoteSubscriptionContext> existing = subscriptions.get(method);
      if (existing == null) {
        return;
      }
      existing.removeIf(
          remoteSubscriptionContext ->
              Objects.equals(remoteSubscriptionContext.getEndpointId(), endpointId));
      if (existing.isEmpty()) {
        subscriptions.remove(method);
      } else {
        subscriptions.put(method, existing);
      }
    } finally {
      lock.unlock();
    }
  }

  /** Stops remote subscription storage. */
  public void shutdown() {
    try {
      channel.close();
    } catch (RuntimeException ex) {
      LOG.error("Failed to stop remote subscription storage. Cause: " + ex.getMessage());
    }
  }
}
