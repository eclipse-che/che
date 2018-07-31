/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.che.api.git.shared.RepositoryDeletedEventDto;
import org.eclipse.che.api.git.shared.RepositoryInitializedEventDto;
import org.eclipse.che.api.git.shared.event.GitCheckoutEvent;
import org.eclipse.che.api.git.shared.event.GitEvent;
import org.eclipse.che.api.git.shared.event.GitRepositoryDeletedEvent;
import org.eclipse.che.api.git.shared.event.GitRepositoryInitializedEvent;

@Singleton
public class GitJsonRpcMessenger implements EventSubscriber<GitEvent> {
  private static final String GIT_REPOSITORY_INITIALIZED = "event/git/initialized";
  private static final String GIT_REPOSITORY_DELETED = "event/git/deleted";

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
    } else if (event instanceof GitRepositoryInitializedEvent) {
      handleRepositoryInitializedEvent((GitRepositoryInitializedEvent) event);
    } else if (event instanceof GitRepositoryDeletedEvent) {
      handleGitRepositoryDeletedEvent((GitRepositoryDeletedEvent) event);
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

  private void handleRepositoryInitializedEvent(GitRepositoryInitializedEvent event) {
    RepositoryInitializedEventDto repositoryInitializedEventDto =
        newDto(RepositoryInitializedEventDto.class).withProjectName(event.getProjectName());

    for (String endpointId : endpointIds) {
      transmitter
          .newRequest()
          .endpointId(endpointId)
          .methodName(GIT_REPOSITORY_INITIALIZED)
          .paramsAsDto(repositoryInitializedEventDto)
          .sendAndSkipResult();
    }
  }

  private void handleGitRepositoryDeletedEvent(GitRepositoryDeletedEvent event) {
    RepositoryDeletedEventDto repositoryDeletedEventDto =
        newDto(RepositoryDeletedEventDto.class).withProjectName(event.getProjectName());

    for (String endpointId : endpointIds) {
      transmitter
          .newRequest()
          .endpointId(endpointId)
          .methodName(GIT_REPOSITORY_DELETED)
          .paramsAsDto(repositoryDeletedEventDto)
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
