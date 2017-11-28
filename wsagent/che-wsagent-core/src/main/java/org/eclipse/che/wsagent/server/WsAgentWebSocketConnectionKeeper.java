/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.wsagent.server;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.eclipse.che.wsagent.shared.Constants.WS_AGENT_TRACK_CONNECTION_CLEANUP_PERIOD_MINUTES;
import static org.eclipse.che.wsagent.shared.Constants.WS_AGENT_TRACK_CONNECTION_HEARTBEAT;
import static org.eclipse.che.wsagent.shared.Constants.WS_AGENT_TRACK_CONNECTION_PERIOD_MILLISECONDS;
import static org.eclipse.che.wsagent.shared.Constants.WS_AGENT_TRACK_CONNECTION_SUBSCRIBE;
import static org.eclipse.che.wsagent.shared.Constants.WS_AGENT_TRACK_CONNECTION_UNSUBSCRIBE;

import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.commons.schedule.ScheduleRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends requests to interested clients to confirm that connection isn't lost.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class WsAgentWebSocketConnectionKeeper {
  private static final Logger LOG = LoggerFactory.getLogger(WsAgentWebSocketConnectionKeeper.class);
  private final ScheduledExecutorService heartBeatScheduler = Executors.newScheduledThreadPool(1);
  private final Set<String> endpointIds = newConcurrentHashSet();
  private final Set<String> unresponsiveEndpointIds = newConcurrentHashSet();
  private ScheduledFuture<?> keepingInTouchFuture;
  private RequestTransmitter transmitter;
  private RequestHandlerConfigurator configurator;

  @Inject
  public WsAgentWebSocketConnectionKeeper(
      RequestTransmitter transmitter, RequestHandlerConfigurator configurator) {
    this.transmitter = transmitter;
    this.configurator = configurator;
  }

  @PostConstruct
  public void initialize() {
    configurator
        .newConfiguration()
        .methodName(WS_AGENT_TRACK_CONNECTION_SUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::add);

    configurator
        .newConfiguration()
        .methodName(WS_AGENT_TRACK_CONNECTION_UNSUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::remove);

    keepingInTouchFuture =
        heartBeatScheduler.scheduleAtFixedRate(
            this::sendRequest, 0, WS_AGENT_TRACK_CONNECTION_PERIOD_MILLISECONDS, MILLISECONDS);
  }

  private void sendRequest() {
    if (endpointIds.isEmpty()) {
      return;
    }

    Set<String> endpointsIdsCopy = copyOf(endpointIds);
    endpointsIdsCopy.forEach(
        endpointId ->
            transmitter
                .newRequest()
                .endpointId(endpointId)
                .methodName(WS_AGENT_TRACK_CONNECTION_HEARTBEAT)
                .paramsAsBoolean(true)
                .sendAndReceiveResultAsBoolean(3 * WS_AGENT_TRACK_CONNECTION_PERIOD_MILLISECONDS)
                .onTimeout(() -> unresponsiveEndpointIds.add(endpointId))
                .onSuccess(aBoolean -> unresponsiveEndpointIds.remove(endpointId))
                .onFailure(
                    jsonRpcError -> {
                      unresponsiveEndpointIds.add(endpointId);
                      LOG.error(
                          format(
                              "Tracking connection request to client with endpointId %s fail, the reason is %s",
                              endpointId, jsonRpcError.getMessage()));
                    }));
  }

  @ScheduleRate(period = WS_AGENT_TRACK_CONNECTION_CLEANUP_PERIOD_MINUTES, unit = MINUTES)
  private void cleanUpEnpointIds() {
    if (unresponsiveEndpointIds.isEmpty()) {
      return;
    }

    Set<String> unresponsiveEndpointIdsCopy = copyOf(unresponsiveEndpointIds);
    unresponsiveEndpointIds.clear();
    endpointIds.removeAll(unresponsiveEndpointIdsCopy);
  }

  @PreDestroy
  public void unInstall() {
    keepingInTouchFuture.cancel(true);
    endpointIds.clear();
    unresponsiveEndpointIds.clear();
  }
}
