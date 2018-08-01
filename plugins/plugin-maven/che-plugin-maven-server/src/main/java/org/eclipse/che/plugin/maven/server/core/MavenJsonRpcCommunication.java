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
package org.eclipse.che.plugin.maven.server.core;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_OUTPUT_PERCENT_METHOD;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_OUTPUT_PERCENT_UNDEFINED_METHOD;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_OUTPUT_START_STOP_METHOD;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_OUTPUT_SUBSCRIBE;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_OUTPUT_TEXT_METHOD;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_OUTPUT_UNSUBSCRIBE;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.MAVEN_OUTPUT_UPDATE_METHOD;
import static org.eclipse.che.plugin.maven.shared.event.MavenOutputEvent.TYPE.PERCENT;
import static org.eclipse.che.plugin.maven.shared.event.MavenOutputEvent.TYPE.PERCENT_UNDEFINED;
import static org.eclipse.che.plugin.maven.shared.event.MavenOutputEvent.TYPE.START_STOP;

import com.google.inject.Singleton;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.maven.shared.dto.PercentMessageDto;
import org.eclipse.che.plugin.maven.shared.dto.PercentUndefinedMessageDto;
import org.eclipse.che.plugin.maven.shared.dto.ProjectsUpdateMessage;
import org.eclipse.che.plugin.maven.shared.dto.StartStopNotification;
import org.eclipse.che.plugin.maven.shared.dto.TextMessageDto;
import org.eclipse.che.plugin.maven.shared.event.MavenOutputEvent;
import org.eclipse.che.plugin.maven.shared.event.MavenPercentMessageEvent;
import org.eclipse.che.plugin.maven.shared.event.MavenPercentUndefinedEvent;
import org.eclipse.che.plugin.maven.shared.event.MavenStartStopEvent;
import org.eclipse.che.plugin.maven.shared.event.MavenTextMessageEvent;
import org.eclipse.che.plugin.maven.shared.event.MavenUpdateEvent;

/** Send maven events using JSON RPC to the clients. */
@Singleton
public class MavenJsonRpcCommunication implements EventSubscriber<MavenOutputEvent> {
  private final Set<String> endpointIds = newConcurrentHashSet();
  private EventService eventService;
  private RequestTransmitter transmitter;

  @Inject
  public MavenJsonRpcCommunication(EventService eventService, RequestTransmitter transmitter) {
    this.eventService = eventService;
    this.transmitter = transmitter;
  }

  @PostConstruct
  private void subscribe() {
    eventService.subscribe(this);
  }

  @PreDestroy
  private void unsubscribe() {
    eventService.unsubscribe(this);
  }

  @Inject
  private void configureHandlers(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(MAVEN_OUTPUT_SUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::add);

    configurator
        .newConfiguration()
        .methodName(MAVEN_OUTPUT_UNSUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::remove);
  }

  @Override
  public void onEvent(MavenOutputEvent event) {
    switch (event.getType()) {
      case TEXT:
        sendTextNotification((MavenTextMessageEvent) event);
        break;
      case UPDATE:
        sendUpdateNotification((MavenUpdateEvent) event);
        break;
      case START_STOP:
        sendStartStopNotification((MavenStartStopEvent) event);
        break;
      case PERCENT:
        senPercentNotification((MavenPercentMessageEvent) event);
        break;
      case PERCENT_UNDEFINED:
        sendPercentUndefinedNotification((MavenPercentUndefinedEvent) event);
        break;
    }
  }

  private void senPercentNotification(MavenPercentMessageEvent event) {
    PercentMessageDto percentMessageDto =
        DtoFactory.newDto(PercentMessageDto.class).withPercent(event.getPercent());
    percentMessageDto.setType(PERCENT);

    endpointIds.forEach(
        it ->
            transmitter
                .newRequest()
                .endpointId(it)
                .methodName(MAVEN_OUTPUT_PERCENT_METHOD)
                .paramsAsDto(percentMessageDto)
                .sendAndSkipResult());
  }

  private void sendPercentUndefinedNotification(MavenPercentUndefinedEvent event) {
    PercentUndefinedMessageDto percentUndefinedMessageDto =
        DtoFactory.newDto(PercentUndefinedMessageDto.class);
    percentUndefinedMessageDto.setPercentUndefined(event.isPercentUndefined());
    percentUndefinedMessageDto.setType(PERCENT_UNDEFINED);

    endpointIds.forEach(
        it ->
            transmitter
                .newRequest()
                .endpointId(it)
                .methodName(MAVEN_OUTPUT_PERCENT_UNDEFINED_METHOD)
                .paramsAsDto(percentUndefinedMessageDto)
                .sendAndSkipResult());
  }

  private void sendStartStopNotification(MavenStartStopEvent event) {
    StartStopNotification startEventDto = DtoFactory.newDto(StartStopNotification.class);
    startEventDto.setStart(event.isStart());
    startEventDto.setType(START_STOP);

    endpointIds.forEach(
        it ->
            transmitter
                .newRequest()
                .endpointId(it)
                .methodName(MAVEN_OUTPUT_START_STOP_METHOD)
                .paramsAsDto(startEventDto)
                .sendAndSkipResult());
  }

  private void sendUpdateNotification(MavenUpdateEvent event) {
    ProjectsUpdateMessage updateEventDto = DtoFactory.newDto(ProjectsUpdateMessage.class);
    List<String> updatedPaths = event.getUpdatedProjects();
    List<String> removedPaths = event.getRemovedProjects();
    updateEventDto.setUpdatedProjects(updatedPaths);
    updateEventDto.setDeletedProjects(removedPaths);

    updateEventDto.setType(MavenOutputEvent.TYPE.UPDATE);

    endpointIds.forEach(
        it ->
            transmitter
                .newRequest()
                .endpointId(it)
                .methodName(MAVEN_OUTPUT_UPDATE_METHOD)
                .paramsAsDto(updateEventDto)
                .sendAndSkipResult());
  }

  private void sendTextNotification(MavenTextMessageEvent event) {
    TextMessageDto notification =
        DtoFactory.newDto(TextMessageDto.class).withText(event.getMessage());
    notification.setType(MavenOutputEvent.TYPE.TEXT);

    endpointIds.forEach(
        it ->
            transmitter
                .newRequest()
                .endpointId(it)
                .methodName(MAVEN_OUTPUT_TEXT_METHOD)
                .paramsAsDto(notification)
                .sendAndSkipResult());
  }
}
