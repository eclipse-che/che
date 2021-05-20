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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.api.workspace.shared.dto.event.BrokerLogEvent;
import org.eclipse.che.api.workspace.shared.dto.event.BrokerStatusChangedEvent;
import org.eclipse.che.api.workspace.shared.dto.event.RuntimeLogEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;

/**
 * Configure JSON_RPC consumers of Che plugin broker events. Also converts {@link
 * BrokerStatusChangedEvent} to {@link BrokerEvent}.
 *
 * <p>This API is in <b>Beta</b> and is subject to changes or removal.
 *
 * @author Oleksander Garagatyi
 */
@Beta
@Singleton
public class BrokerService {

  private static final Logger LOG = getLogger(BrokerService.class);

  public static final String BROKER_RESULT_METHOD = "broker/result";
  public static final String BROKER_STATUS_CHANGED_METHOD = "broker/statusChanged";
  public static final String BROKER_LOG_METHOD = "broker/log";

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final EventService eventService;

  @Inject
  public BrokerService(EventService eventService) {
    this.eventService = eventService;
  }

  @Inject
  public void configureMethods(RequestHandlerConfigurator requestHandler) {
    requestHandler
        .newConfiguration()
        .methodName(BROKER_STATUS_CHANGED_METHOD)
        .paramsAsDto(BrokerStatusChangedEvent.class)
        .noResult()
        .withConsumer(this::handle);

    requestHandler
        .newConfiguration()
        .methodName(BROKER_RESULT_METHOD)
        .paramsAsDto(BrokerStatusChangedEvent.class)
        .noResult()
        .withConsumer(this::handle);

    requestHandler
        .newConfiguration()
        .methodName(BROKER_LOG_METHOD)
        .paramsAsDto(BrokerLogEvent.class)
        .noResult()
        .withConsumer(this::handle);
  }

  private void handle(BrokerLogEvent brokerLogEvent) {
    eventService.publish(
        DtoFactory.newDto(RuntimeLogEvent.class)
            .withRuntimeId(brokerLogEvent.getRuntimeId())
            .withText(brokerLogEvent.getText())
            .withTime(brokerLogEvent.getTime()));
  }

  private void handle(BrokerStatusChangedEvent event) {
    String encodedTooling = event.getTooling();
    RuntimeIdentity runtimeId = event.getRuntimeId();
    if (event.getStatus() == null || runtimeId == null || runtimeId.getWorkspaceId() == null) {
      LOG.error("Broker event skipped due to illegal content: {}", event);
      return;
    }
    eventService.publish(new BrokerEvent(event, parseTooling(encodedTooling)));
  }

  @Nullable
  private List<ChePlugin> parseTooling(String toolingString) {
    if (!isNullOrEmpty(toolingString)) {
      try {
        List<ChePlugin> plugins =
            objectMapper.readValue(toolingString, new TypeReference<List<ChePlugin>>() {});
        // when id of plugin is not set, we can compose it from publisher, name and version
        return plugins.stream().map(this::composePluginIdWhenNull).collect(Collectors.toList());
      } catch (IOException e) {
        LOG.error("Parsing Che plugin broker event failed. Error: " + e.getMessage(), e);
      }
    }
    return null;
  }

  private ChePlugin composePluginIdWhenNull(ChePlugin plugin) {
    if (isNullOrEmpty(plugin.getId())
        && !isNullOrEmpty(plugin.getPublisher())
        && !isNullOrEmpty(plugin.getName())
        && !isNullOrEmpty(plugin.getVersion())) {
      plugin.setId(plugin.getPublisher() + "/" + plugin.getName() + "/" + plugin.getVersion());
    }
    return plugin;
  }
}
