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
package org.eclipse.che.api.core.notification;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Singleton;

/**
 * Imnemory implementation of {@link SubscriptionStorage}
 *
 * @author Max Shaposhnik (mshaposh@redhat.com) */
@Singleton
public class InmemorySubscriptionStorage implements SubscriptionStorage {

  private final Map<String, Set<SubscriptionContext>> subscriptions = new ConcurrentHashMap<>();

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
