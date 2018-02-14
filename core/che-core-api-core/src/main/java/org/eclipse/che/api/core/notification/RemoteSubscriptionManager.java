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

import static java.util.Collections.emptySet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.dto.EventSubscription;

@Singleton
public class RemoteSubscriptionManager {
  private final Map<String, Set<SubscriptionContext>> subscriptionContexts =
      new ConcurrentHashMap<>();

  private final EventService eventService;
  private final RequestTransmitter requestTransmitter;

  @Inject
  public RemoteSubscriptionManager(
      EventService eventService, RequestTransmitter requestTransmitter) {
    this.eventService = eventService;
    this.requestTransmitter = requestTransmitter;
  }

  @Inject
  private void configureSubscription(RequestHandlerConfigurator requestHandlerConfigurator) {
    requestHandlerConfigurator
        .newConfiguration()
        .methodName("subscribe")
        .paramsAsDto(EventSubscription.class)
        .noResult()
        .withBiConsumer(this::consumeSubscriptionRequest);

    requestHandlerConfigurator
        .newConfiguration()
        .methodName("unSubscribe")
        .paramsAsDto(EventSubscription.class)
        .noResult()
        .withBiConsumer(this::consumeUnSubscriptionRequest);
  }

  public <T> void register(
      String method, Class<T> eventType, BiPredicate<T, Map<String, String>> biPredicate) {
    eventService.subscribe(
        event ->
            subscriptionContexts
                .getOrDefault(method, emptySet())
                .stream()
                .filter(context -> biPredicate.test(event, context.scope))
                .forEach(context -> transmit(context.endpointId, method, event)),
        eventType);
  }

  private void consumeSubscriptionRequest(String endpointId, EventSubscription eventSubscription) {
    subscriptionContexts
        .computeIfAbsent(eventSubscription.getMethod(), k -> ConcurrentHashMap.newKeySet(1))
        .add(new SubscriptionContext(endpointId, eventSubscription.getScope()));
  }

  private void consumeUnSubscriptionRequest(
      String endpointId, EventSubscription eventSubscription) {
    subscriptionContexts
        .getOrDefault(eventSubscription.getMethod(), emptySet())
        .removeIf(
            subscriptionContext -> Objects.equals(subscriptionContext.endpointId, endpointId));
  }

  private <T> void transmit(String endpointId, String method, T event) {
    requestTransmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName(method)
        .paramsAsDto(event)
        .sendAndSkipResult();
  }

  private class SubscriptionContext {
    private final String endpointId;
    private final Map<String, String> scope;

    private SubscriptionContext(String endpointId, Map<String, String> scope) {
      this.endpointId = endpointId;
      this.scope = scope;
    }
  }
}
