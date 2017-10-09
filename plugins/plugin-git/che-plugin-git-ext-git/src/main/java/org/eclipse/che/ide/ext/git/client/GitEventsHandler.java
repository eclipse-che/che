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
package org.eclipse.che.ide.ext.git.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.git.shared.FileChangedEventDto;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusChangedEventDto;
import org.eclipse.che.api.project.shared.dto.event.GitCheckoutEventDto;

/**
 * Receives git events from server side.
 *
 * @author Igor Vinokur
 * @author Mykola Morhun
 */
@Singleton
public class GitEventsHandler implements GitEventSubscribable {

  private static final String EVENT_GIT_FILE_CHANGED = "event/git-change";
  private static final String EVENT_GIT_STATUS_CHANGED = "event/git/statusChanged";
  private static final String EVENT_GIT_CHECKOUT = "event/git-checkout";

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
  }

  private void onFileChangedHandler(String endpointId, FileChangedEventDto fileChangedEventDto) {
    for (GitEventsSubscriber subscriber : subscribers) {
      subscriber.onFileUnderGitChanged(endpointId, fileChangedEventDto);
    }
  }

  private void onStatusChangedHandler(String endpointId, StatusChangedEventDto statusChangedEventDto) {
    for (GitEventsSubscriber subscriber : subscribers) {
      subscriber.onGitStatusChanged(endpointId, statusChangedEventDto);
    }
  }

  private void onCheckoutHandler(String endpointId, GitCheckoutEventDto gitCheckoutEventDto) {
    for (GitEventsSubscriber subscriber : subscribers) {
      subscriber.onGitCheckout(endpointId, gitCheckoutEventDto);
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
