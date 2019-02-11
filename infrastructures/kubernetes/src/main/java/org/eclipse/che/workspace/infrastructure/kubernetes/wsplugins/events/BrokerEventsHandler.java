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
package org.eclipse.che.workspace.infrastructure.kubernetes.wsplugins.events;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.wsplugins.model.ChePlugin;
import org.eclipse.che.api.workspace.shared.dto.event.BrokerLogEvent;
import org.eclipse.che.api.workspace.shared.dto.event.BrokerStatusChangedEvent;
import org.eclipse.che.api.workspace.shared.dto.event.RuntimeLogEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;

/**
 * Converts events received from JSON_RPC channels to {@link EventService} events.
 *
 * @author Oleksandr Garagatyi
 */
public class BrokerEventsHandler {

  static final String NO_ERROR_NO_TOOLING_ERROR_TEMPLATE =
      "Received event from plugin broker for workspace %s:%s with empty error and brokering result";

  private static final Logger LOG = getLogger(BrokerEventsHandler.class);

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final EventService eventService;

  @Inject
  public BrokerEventsHandler(EventService eventService) {
    this.eventService = eventService;
  }

  public void handle(BrokerLogEvent brokerLogEvent) {
    eventService.publish(
        DtoFactory.newDto(RuntimeLogEvent.class)
            .withRuntimeId(brokerLogEvent.getRuntimeId())
            .withText(brokerLogEvent.getText())
            .withTime(brokerLogEvent.getTime()));
  }

  public void handle(BrokerStatusChangedEvent event) {
    // Tooling has fields that can't be parsed by DTO and JSON_RPC framework works with DTO only
    String encodedTooling = event.getTooling();
    RuntimeIdentity runtimeId = event.getRuntimeId();
    if (event.getStatus() == null || runtimeId == null || runtimeId.getWorkspaceId() == null) {
      LOG.error("Broker event skipped due to illegal content: {}", event);
      return;
    }
    BrokerEvent brokerEvent = new BrokerEvent(event, null);
    if (isNullOrEmpty(event.getError())) {
      if (isNullOrEmpty(encodedTooling)) {
        brokerEvent.withError(
            format(
                NO_ERROR_NO_TOOLING_ERROR_TEMPLATE,
                runtimeId.getOwnerId(),
                runtimeId.getWorkspaceId()));
      } else {
        try {
          brokerEvent.withTooling(parseTooling(encodedTooling));
        } catch (IOException e) {
          brokerEvent.withError(e.getMessage());
        }
      }
    }
    eventService.publish(brokerEvent);
  }

  private List<ChePlugin> parseTooling(String toolingString) throws IOException {
    try {
      return objectMapper.readValue(toolingString, new TypeReference<List<ChePlugin>>() {});
    } catch (IOException e) {
      String errMessage = "Parsing Che plugin broker event failed. Error: " + e.getMessage();
      LOG.error(errMessage, e);
      throw new IOException(errMessage, e);
    }
  }
}
