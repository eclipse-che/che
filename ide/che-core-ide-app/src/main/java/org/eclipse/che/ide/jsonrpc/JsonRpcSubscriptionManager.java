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
package org.eclipse.che.ide.jsonrpc;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;

/**
 * Client side class simplifies subscription management by providing a more convenient API for
 * initiating subscription and reacting on events that comes from server side.
 */
public abstract class JsonRpcSubscriptionManager {
  private static final Logger LOGGER = getLogger(JsonRpcSubscriptionManager.class);

  private final RequestTransmitter requestTransmitter;
  private final RequestHandlerConfigurator requestHandlerConfigurator;

  private final Map<String, Set<Consumer>> methodsToConsumers = new ConcurrentHashMap<>();

  private final String endpointId;

  public JsonRpcSubscriptionManager(
      RequestTransmitter requestTransmitter,
      RequestHandlerConfigurator requestHandlerConfigurator,
      String endpointId) {
    this.requestTransmitter = requestTransmitter;
    this.requestHandlerConfigurator = requestHandlerConfigurator;
    this.endpointId = endpointId;
  }

  /**
   * Subscribe to a specific events
   *
   * @param name name of a subscription
   * @param subCtx subscription context, specific information (e.g. file path)
   * @param eventClass class of event that server will send
   * @param eventConsumer consumer that will accept events send by server
   * @param <E> type that represents class of events
   */
  public synchronized <E> void subscribe(
      String name, Object subCtx, Class<E> eventClass, Consumer<E> eventConsumer) {
    subscribeInternally(name, subCtx, eventClass, eventConsumer);
  }

  /**
   * Subscribe to a specific events
   *
   * @param name name of a subscription
   * @param eventClass class of event that server will send
   * @param eventConsumer consumer that will accept events send by server
   * @param <E> type that represents class of events
   */
  public synchronized <E> void subscribe(
      String name, Class<E> eventClass, Consumer<E> eventConsumer) {
    subscribeInternally(name, null, eventClass, eventConsumer);
  }

  private <E> void subscribeInternally(
      String name, Object subCtx, Class<E> eventClass, Consumer<E> eventConsumer) {

    Set<Consumer> consumerActions = methodsToConsumers.get(name);
    if (consumerActions == null) {
      requestHandlerConfigurator
          .newConfiguration()
          .methodName("publish/" + name)
          .paramsAsDto(eventClass)
          .noResult()
          .withConsumer(
              event -> {
                for (Consumer consumer :
                    methodsToConsumers.computeIfAbsent(name, __ -> new ConcurrentHashSet<>())) {
                  consumer.accept(event);
                }
              });
    } else {
      consumerActions.add(eventConsumer);
    }

    if (subCtx == null) {
      requestTransmitter
          .newRequest()
          .endpointId(endpointId)
          .methodName("subscribe/" + name)
          .noParams()
          .sendAndSkipResult();
    } else {
      requestTransmitter
          .newRequest()
          .endpointId(endpointId)
          .methodName("subscribe/" + name)
          .paramsAsDto(subCtx)
          .sendAndSkipResult();
    }
  }

  public synchronized void unsubscribe(String name, Object subCtx) {
    requestTransmitter
        .newRequest()
        .endpointId(endpointId)
        .methodName("unsubscribe/" + name)
        .paramsAsDto(subCtx)
        .sendAndSkipResult();
  }
}
