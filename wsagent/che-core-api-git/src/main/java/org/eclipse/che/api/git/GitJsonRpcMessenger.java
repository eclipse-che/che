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
package org.eclipse.che.api.git;

import static com.google.common.collect.Sets.newConcurrentHashSet;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.event.GitCheckoutEvent;
import org.eclipse.che.api.git.shared.event.GitCommitEvent;
import org.eclipse.che.api.git.shared.event.GitEvent;
import org.eclipse.che.api.git.shared.event.GitRepositoryDeletedEvent;
import org.eclipse.che.api.git.shared.event.GitRepositoryInitializedEvent;
import org.eclipse.che.api.git.shared.event.GitResetEvent;

@Singleton
public class GitJsonRpcMessenger implements EventSubscriber<GitEvent> {
  private final Map<String, Set<String>> endpointIdsWithWorkspaceIdAndProjectName =
      new ConcurrentHashMap<>();
  private final Set<String> endpointIds = newConcurrentHashSet();

  private final EventService eventService;
  private final RequestTransmitter transmitter;

  @Inject
  public GitJsonRpcMessenger(EventService eventService, RequestTransmitter transmitter) {
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

  @Override
  public void onEvent(GitEvent event) {
    if (event instanceof GitCheckoutEvent) {
      handleCheckoutEvent((GitCheckoutEvent) event);
    } else if (event instanceof GitCommitEvent
        || event instanceof GitResetEvent
        || event instanceof GitRepositoryInitializedEvent
        || event instanceof GitRepositoryDeletedEvent) {
      handleIndexChangedEvent(event);
    }
  }

  private void handleCheckoutEvent(GitCheckoutEvent event) {
    endpointIdsWithWorkspaceIdAndProjectName
        .entrySet()
        .stream()
        .filter(it -> it.getValue().contains(event.getWorkspaceId() + event.getProjectName()))
        .map(Entry::getKey)
        .forEach(
            it ->
                transmitter
                    .newRequest()
                    .endpointId(it)
                    .methodName("git/checkoutOutput")
                    .paramsAsDto(event)
                    .sendAndSkipResult());
  }

  private void handleIndexChangedEvent(GitEvent event) {
    Status status = newDto(Status.class);
    if (event instanceof GitCommitEvent) {
      status = ((GitCommitEvent) event).getStatus();
    } else if (event instanceof GitResetEvent) {
      status = ((GitResetEvent) event).getStatus();
    } else if (event instanceof GitRepositoryInitializedEvent) {
      status = ((GitRepositoryInitializedEvent) event).getStatus();
    }

    for (String endpointId : endpointIds) {
      transmitter
          .newRequest()
          .endpointId(endpointId)
          .methodName("event/git/indexChanged")
          .paramsAsDto(status)
          .sendAndSkipResult();
    }
  }

  @Inject
  private void configureSubscribeHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName("git/checkoutOutput/subscribe")
        .paramsAsString()
        .noResult()
        .withBiConsumer(
            (endpointId, workspaceIdAndProjectName) -> {
              endpointIdsWithWorkspaceIdAndProjectName.putIfAbsent(
                  endpointId, newConcurrentHashSet());
              endpointIdsWithWorkspaceIdAndProjectName
                  .get(endpointId)
                  .add(workspaceIdAndProjectName);
            });

    configurator
        .newConfiguration()
        .methodName("event/git/subscribe")
        .noParams()
        .noResult()
        .withConsumer(endpointIds::add);
  }

  @Inject
  private void configureUnSubscribeHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName("git/checkoutOutput/unsubscribe")
        .paramsAsString()
        .noResult()
        .withBiConsumer(
            (endpointId, workspaceIdAndProjectName) -> {
              endpointIdsWithWorkspaceIdAndProjectName
                  .getOrDefault(endpointId, newConcurrentHashSet())
                  .remove(workspaceIdAndProjectName);
              endpointIdsWithWorkspaceIdAndProjectName.computeIfPresent(
                  endpointId, (key, value) -> value.isEmpty() ? null : value);
            });
  }
}
