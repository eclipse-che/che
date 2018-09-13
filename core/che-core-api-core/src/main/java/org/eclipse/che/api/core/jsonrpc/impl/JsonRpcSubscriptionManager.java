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
package org.eclipse.che.api.core.jsonrpc.impl;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javax.inject.Inject;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.websocket.impl.WebSocketSessionRegistry;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.slf4j.Logger;

/**
 * Manager is responsible for registration of server side subscription handling over JSON-RPC
 * protocol.
 */
@Singleton
public class JsonRpcSubscriptionManager {
  private static final Logger LOGGER = getLogger(JsonRpcSubscriptionManager.class);

  private final Set<String> possiblyClosedEndpointIds = new HashSet<>();

  private final Set<String> endpointIds = newConcurrentHashSet();
  private final Set<UnSubscribeAction> unSubscribeActions = newConcurrentHashSet();

  private final RequestHandlerConfigurator requestHandlerConfigurator;
  private final RequestTransmitter requestTransmitter;
  private final WebSocketSessionRegistry webSocketSessionRegistry;

  @Inject
  public JsonRpcSubscriptionManager(
      RequestHandlerConfigurator requestHandlerConfigurator,
      RequestTransmitter requestTransmitter,
      WebSocketSessionRegistry webSocketSessionRegistry) {
    this.requestHandlerConfigurator = requestHandlerConfigurator;
    this.requestTransmitter = requestTransmitter;
    this.webSocketSessionRegistry = webSocketSessionRegistry;
  }

  /**
   * Simple task to clear stale subscriptions, we check if endpoint is available if not we firstly
   * put it into the list of possible closed endpoints, the next step we clear the connection and
   * all related subscriptions by running 'onUnsubscribe' actions.
   */
  @ScheduleRate(period = 15)
  private void cleanStaleSubscriptions() {
    Set<String> closedEndpointIds = new HashSet<>();
    for (String possiblyClosedEndpointId : possiblyClosedEndpointIds) {
      if (webSocketSessionRegistry.isClosed(possiblyClosedEndpointId)) {
        closedEndpointIds.add(possiblyClosedEndpointId);
      }
    }
    possiblyClosedEndpointIds.clear();

    for (String closedEndpointId : closedEndpointIds) {
      for (UnSubscribeAction unSubscribeAction : unSubscribeActions) {
        unSubscribeAction.performIfMatches(closedEndpointId);
      }
    }

    for (String endpointId : endpointIds) {
      if (webSocketSessionRegistry.isClosed(endpointId)) {
        possiblyClosedEndpointIds.add(endpointId);
      }
    }
  }

  /**
   * Register a server side subscription handling
   *
   * @param name name of the subscription (e.g. "file/modification")
   * @param eventClass class of the event that should be sent to client side
   * @param subscribeBiConsumer action that is performed when client initiate the subscription
   * @param unSubscribeConsumer action that is performed when client cancels the subscription or the
   *     connection is closed
   * @param <E> type parameter that represents class of the event that will be sent to client die
   */
  public synchronized <C, E> void register(
      String name,
      Class<E> eventClass,
      Consumer<EventTransmitter<E>> subscribeBiConsumer,
      Runnable unSubscribeConsumer) {

    requestHandlerConfigurator
        .newConfiguration()
        .methodName("subscribe/" + name)
        .noParams()
        .noResult()
        .withConsumer(
            endpointId -> {
              endpointIds.add(endpointId);

              subscribeBiConsumer.accept(
                  event ->
                      requestTransmitter
                          .newRequest()
                          .endpointId(endpointId)
                          .methodName("publish/" + name)
                          .paramsAsDto(eventClass)
                          .sendAndSkipResult());

              unSubscribeActions.add(
                  new UnSubscribeAction() {
                    @Override
                    public void performIfMatches(String endpointId1) {
                      if (endpointId1.equals(endpointId)) {
                        unSubscribeConsumer.run();
                      }
                    }

                    @Override
                    public void performIfMatches(String endpointId1, String name1) {
                      if (endpointId1.equals(endpointId) && name1.equals(name)) {
                        unSubscribeConsumer.run();
                      }
                    }
                  });
            });

    requestHandlerConfigurator
        .newConfiguration()
        .methodName("unsubscribe/" + name)
        .noParams()
        .noResult()
        .withConsumer(
            endpointId -> {
              for (UnSubscribeAction unSubscribeAction : unSubscribeActions) {
                unSubscribeAction.performIfMatches(endpointId, name);
              }
            });
  }

  /**
   * Register a server side subscription handling
   *
   * @param name name of the subscription (e.g. "file/modification")
   * @param subCtxClass class of subscription context (e.g. we subscribe to "file/modification" but
   *     we want to track only specific file, we can pass the location in a subscription context
   *     represented by DTO
   * @param eventClass class of the event that should be sent to client side
   * @param subscribeBiConsumer action that is performed when client initiate the subscription
   * @param unSubscribeConsumer action that is performed when client cancels the subscription or the
   *     connection is closed
   * @param <C> type parameter that represents the class of subscription context
   * @param <E> type parameter that represents class of the event that will be sent to client die
   */
  public synchronized <C, E> void register(
      String name,
      Class<C> subCtxClass,
      Class<E> eventClass,
      BiConsumer<C, EventTransmitter<E>> subscribeBiConsumer,
      Consumer<C> unSubscribeConsumer) {

    requestHandlerConfigurator
        .newConfiguration()
        .methodName("subscribe/" + name)
        .paramsAsDto(subCtxClass)
        .noResult()
        .withBiConsumer(
            (endpointId, subCtx) -> {
              endpointIds.add(endpointId);

              subscribeBiConsumer.accept(
                  subCtx,
                  event ->
                      requestTransmitter
                          .newRequest()
                          .endpointId(endpointId)
                          .methodName("publish/" + name)
                          .paramsAsDto(eventClass)
                          .sendAndSkipResult());

              unSubscribeActions.add(
                  new UnSubscribeAction() {
                    @Override
                    public void performIfMatches(String endpointId1) {
                      if (endpointId1.equals(endpointId)) {
                        unSubscribeConsumer.accept(subCtx);
                      }
                    }

                    @Override
                    public void performIfMatches(String endpointId1, String name1, Object subCtx1) {
                      if (endpointId1.equals(endpointId)
                          && name1.equals(name)
                          && subCtx1.equals(subCtx)) {
                        unSubscribeConsumer.accept(subCtx);
                      }
                    }
                  });
            });

    requestHandlerConfigurator
        .newConfiguration()
        .methodName("unsubscribe/" + name)
        .paramsAsDto(subCtxClass)
        .noResult()
        .withBiConsumer(
            (endpointId, subCtx) -> {
              for (UnSubscribeAction unSubscribeAction : unSubscribeActions) {
                unSubscribeAction.performIfMatches(endpointId, name, subCtx);
              }
            });
  }

  public interface EventTransmitter<T> {
    void transmit(T event);
  }

  private interface UnSubscribeAction {
    void performIfMatches(String endpointId);

    default void performIfMatches(String endpointId, String name) {}

    default void performIfMatches(String endpointId, String name, Object subCtx) {}
  }
}
