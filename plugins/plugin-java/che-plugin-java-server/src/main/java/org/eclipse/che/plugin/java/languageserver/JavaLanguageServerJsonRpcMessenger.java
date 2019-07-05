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
package org.eclipse.che.plugin.java.languageserver;

import static com.google.common.collect.Sets.newConcurrentHashSet;

import com.google.inject.Singleton;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.project.server.notification.ProjectUpdatedEvent;

/**
 * Sends jdt-ls events using JSON RPC to the clients.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class JavaLanguageServerJsonRpcMessenger {
  private static final String EVENT_JDT_PROJECT_UPDATED = "event:jdt:project-updated";
  private static final String EVENT_JDT_SUBSCRIBE = "event:jdt:subscribe";
  private static final String EVENT_JDT_UNSUBSCRIBE = "event:jdt:unsubscribe";

  private final EventService eventService;
  private final RequestTransmitter transmitter;
  private final EventSubscriber<ProjectUpdatedEvent> projectUpdatedEventSubscriber;

  private final Set<String> endpointIds = newConcurrentHashSet();

  @Inject
  public JavaLanguageServerJsonRpcMessenger(
      EventService eventService, RequestTransmitter transmitter) {
    this.eventService = eventService;
    this.transmitter = transmitter;
    this.projectUpdatedEventSubscriber = JavaLanguageServerJsonRpcMessenger.this::onEvent;
  }

  @PostConstruct
  private void subscribe() {
    eventService.subscribe(projectUpdatedEventSubscriber, ProjectUpdatedEvent.class);
  }

  @PreDestroy
  private void unsubscribe() {
    eventService.unsubscribe(projectUpdatedEventSubscriber, ProjectUpdatedEvent.class);
  }

  private void onEvent(ProjectUpdatedEvent event) {
    endpointIds.forEach(
        it ->
            transmitter
                .newRequest()
                .endpointId(it)
                .methodName(EVENT_JDT_PROJECT_UPDATED)
                .paramsAsDto(event.getProjectPath())
                .sendAndSkipResult());
  }

  @Inject
  private void configureSubscribeHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(EVENT_JDT_SUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::add);
  }

  @Inject
  private void configureUnSubscribeHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(EVENT_JDT_UNSUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::remove);
  }
}
