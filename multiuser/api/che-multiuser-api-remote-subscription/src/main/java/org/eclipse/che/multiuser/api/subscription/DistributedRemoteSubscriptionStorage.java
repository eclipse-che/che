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
package org.eclipse.che.multiuser.api.subscription;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.notification.RemoteSubscriptionContext;
import org.eclipse.che.api.core.notification.RemoteSubscriptionStorage;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.blocks.ReplicatedHashMap;
import org.slf4j.Logger;

/**
 * Replicated map-based implementation of {@link RemoteSubscriptionStorage}
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class DistributedRemoteSubscriptionStorage implements RemoteSubscriptionStorage {

  private static final Logger LOG = getLogger(DistributedRemoteSubscriptionStorage.class);

  private static final String CHANNEL_NAME = "RemoteSubscriptionChannel";

  private ReplicatedHashMap<CompositeKey, RemoteSubscriptionContext> subscriptions;

  @Inject
  public DistributedRemoteSubscriptionStorage(@Named("jgroups.config.file") String confFile)
      throws Exception {
    try {
      Channel channel = new JChannel(confFile);
      channel.connect(CHANNEL_NAME);
      subscriptions = new ReplicatedHashMap<>(channel);
      subscriptions.start(5000);
    } catch (Exception e) {
      LOG.error("Unable to create distributed event subscriptions map.", e);
      throw e;
    }
  }

  @Override
  public Set<RemoteSubscriptionContext> getByMethod(String method) {
    return subscriptions
        .entrySet()
        .stream()
        .filter(e -> e.getKey().method.equals(method))
        .map(Entry::getValue)
        .collect(Collectors.toSet());
  }

  @Override
  public void addSubscription(String method, RemoteSubscriptionContext remoteSubscriptionContext) {
    subscriptions.put(
        new CompositeKey(method, remoteSubscriptionContext.getEndpointId()),
        remoteSubscriptionContext);
  }

  @Override
  public void removeSubscription(String method, String endpointId) {
    subscriptions.remove(new CompositeKey(method, endpointId));
  }

  static class CompositeKey implements Serializable {

    private final String method;
    private final String endpointId;

    CompositeKey(String method, String endpointTd) {
      this.method = method;
      this.endpointId = endpointTd;
    }

    @Override
    public boolean equals(Object o) {
      if (o != null && o instanceof CompositeKey) {
        CompositeKey s = (CompositeKey) o;
        return method.equals(s.method) && endpointId.equals(s.endpointId);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(method, endpointId);
    }
  }
}
