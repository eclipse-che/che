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

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.notification.SubscriptionContext;
import org.eclipse.che.api.core.notification.SubscriptionStorage;
import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.blocks.ReplicatedHashMap;
import org.slf4j.Logger;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
public class DistributedSubscriptionStorage implements SubscriptionStorage {

  private static final Logger LOG = getLogger(DistributedSubscriptionStorage.class);

  private static final String CHANNEL_NAME = "RemoteSubscriptionChannel";

  private ReplicatedHashMap<String, Set<SubscriptionContext>> subscriptions;

  @Inject
  public DistributedSubscriptionStorage(@Named("jgroups.config.file") String confFile)
      throws Exception {
    if (confFile != null) {
      try {
        Channel channel = new JChannel(confFile);
        channel.connect(CHANNEL_NAME);
        subscriptions = new ReplicatedHashMap<>(channel);
        subscriptions.start(5000);
      } catch (Exception e) {
        LOG.error("Unable to create distributed subscriptions map.", e);
        throw e;
      }
    }
  }

  @Override
  public Set<SubscriptionContext> getByMethod(String method) {
    return subscriptions.getOrDefault(method, Collections.emptySet());
  }

  @Override
  public void addSubscription(String method, SubscriptionContext subscriptionContext) {
    subscriptions
        .computeIfAbsent(method, k -> ConcurrentHashMap.newKeySet(1))
        .add(subscriptionContext);
  }

  @Override
  public void removeSubscription(String method, String endpointId) {
    subscriptions
        .getOrDefault(method, Collections.emptySet())
        .removeIf(
            subscriptionContext -> Objects.equals(subscriptionContext.getEndpointId(), endpointId));
  }
}
