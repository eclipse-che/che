/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.notification;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.function.BiPredicate;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.dto.EventSubscription;

@Singleton
public class RemoteSubscriptionManager {

  private final EventService eventService;
  private final RequestTransmitter requestTransmitter;
  private final RemoteSubscriptionStorage remoteSubscriptionStorage;

  @Inject
  public RemoteSubscriptionManager(
      EventService eventService,
      RequestTransmitter requestTransmitter,
      RemoteSubscriptionStorage remoteSubscriptionStorage) {
    this.eventService = eventService;
    this.requestTransmitter = requestTransmitter;
    this.remoteSubscriptionStorage = remoteSubscriptionStorage;
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
            remoteSubscriptionStorage
                .getByMethod(method)
                .stream()
                .filter(context -> biPredicate.test(event, context.getScope()))
                .forEach(context -> transmit(context.getEndpointId(), method, event)),
        eventType);
  }

  private void consumeSubscriptionRequest(String endpointId, EventSubscription eventSubscription) {
    remoteSubscriptionStorage.addSubscription(
        eventSubscription.getMethod(),
        new RemoteSubscriptionContext(endpointId, eventSubscription.getScope()));
  }

  private void consumeUnSubscriptionRequest(
      String endpointId, EventSubscription eventSubscription) {
    remoteSubscriptionStorage.removeSubscription(eventSubscription.getMethod(), endpointId);
  }

  private <T> void transmit(String endpointId, String method, T event) {
    requestTransmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName(method)
        .paramsAsDto(event)
        .sendAndSkipResult();
  }
}
