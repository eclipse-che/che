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
package org.eclipse.che.ide.ext.git.client;

import static org.eclipse.che.api.git.shared.Constants.EVENT_GIT_FILE_CHANGED;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.git.shared.FileChangedEventDto;
import org.eclipse.che.api.git.shared.RepositoryDeletedEventDto;
import org.eclipse.che.api.git.shared.RepositoryInitializedEventDto;
import org.eclipse.che.api.git.shared.StatusChangedEventDto;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;

/**
 * Receives git events from server side.
 *
 * <p>To avoid a risk of IDE slow down this handler bypasses event bus because of heavy load.
 *
 * @author Igor Vinokur
 * @author Mykola Morhun
 */
@Singleton
public class GitEventsHandler implements GitEventSubscribable {

  private static final String EVENT_GIT_STATUS_CHANGED = "event/git/status-changed";
  private static final String EVENT_GIT_CHECKOUT = "event/git-checkout";
  private static final String EVENT_GIT_REPOSITORY_INITIALIZED = "event/git/initialized";
  private static final String EVENT_GIT_REPOSITORY_DELETED = "event/git/deleted";

  private final Set<GitEventsSubscriber> subscribers = new HashSet<>();

  @Inject
  public GitEventsHandler(RequestHandlerConfigurator configurator) {
    configureHandlers(configurator);
  }

  private void configureHandlers(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(EVENT_GIT_FILE_CHANGED)
        .paramsAsDto(FileChangedEventDto.class)
        .noResult()
        .withBiConsumer(this::onFileChangedHandler);

    configurator
        .newConfiguration()
        .methodName(EVENT_GIT_STATUS_CHANGED)
        .paramsAsDto(StatusChangedEventDto.class)
        .noResult()
        .withBiConsumer(this::onStatusChangedHandler);

    configurator
        .newConfiguration()
        .methodName(EVENT_GIT_CHECKOUT)
        .paramsAsDto(GitCheckoutEventDto.class)
        .noResult()
        .withBiConsumer(this::onCheckoutHandler);

    configurator
        .newConfiguration()
        .methodName(EVENT_GIT_REPOSITORY_INITIALIZED)
        .paramsAsDto(RepositoryInitializedEventDto.class)
        .noResult()
        .withBiConsumer(this::onRepositoryInitializedHandler);

    configurator
        .newConfiguration()
        .methodName(EVENT_GIT_REPOSITORY_DELETED)
        .paramsAsDto(RepositoryDeletedEventDto.class)
        .noResult()
        .withBiConsumer(this::onRepositoryDeletedHandler);
  }

  private void onFileChangedHandler(String endpointId, FileChangedEventDto fileChangedEventDto) {
    for (GitEventsSubscriber subscriber : subscribers) {
      subscriber.onFileChanged(endpointId, fileChangedEventDto);
    }
  }

  private void onStatusChangedHandler(
      String endpointId, StatusChangedEventDto statusChangedEventDto) {
    for (GitEventsSubscriber subscriber : subscribers) {
      subscriber.onGitStatusChanged(endpointId, statusChangedEventDto);
    }
  }

  private void onCheckoutHandler(String endpointId, GitCheckoutEventDto gitCheckoutEventDto) {
    for (GitEventsSubscriber subscriber : subscribers) {
      subscriber.onGitCheckout(endpointId, gitCheckoutEventDto);
    }
  }

  private void onRepositoryInitializedHandler(
      String endpointId, RepositoryInitializedEventDto repositoryInitializedEvent) {
    for (GitEventsSubscriber subscriber : subscribers) {
      subscriber.onGitRepositoryInitialized(endpointId, repositoryInitializedEvent);
    }
  }

  private void onRepositoryDeletedHandler(
      String endpointId, RepositoryDeletedEventDto repositoryDeletedEvent) {
    for (GitEventsSubscriber subscriber : subscribers) {
      subscriber.onGitRepositoryDeleted(endpointId, repositoryDeletedEvent);
    }
  }

  @Override
  public void addSubscriber(GitEventsSubscriber subscriber) {
    subscribers.add(subscriber);
  }

  @Override
  public void removeSubscriber(GitEventsSubscriber subscriber) {
    subscribers.remove(subscriber);
  }
}
