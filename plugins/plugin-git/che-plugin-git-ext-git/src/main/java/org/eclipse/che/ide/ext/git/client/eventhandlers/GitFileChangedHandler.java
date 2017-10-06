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
package org.eclipse.che.ide.ext.git.client.eventhandlers;

import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.project.shared.dto.event.GitChangeEventDto;

/**
 * Receives events about changes in files under git and notifies all subscribers.
 *
 * @author Igor Vinokur
 * @author Mykola Morhun
 */
@Singleton
public class GitFileChangedHandler {

  private static final String EVENT_GIT_FILE_CHANGED = "event/git-change";

  public interface GitFileChangesSubscriber {
    void onFileUnderGitChanged(String endpointId, GitChangeEventDto dto);
  }

  private final Set<GitFileChangesSubscriber> subscribers = new HashSet<>();

  @Inject
  public GitFileChangedHandler(RequestHandlerConfigurator configurator) {
    configureHandler(configurator);
  }

  private void configureHandler(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(EVENT_GIT_FILE_CHANGED)
        .paramsAsDto(GitChangeEventDto.class)
        .noResult()
        .withBiConsumer(this::apply);
  }

  private void apply(String endpointId, GitChangeEventDto dto) {
    for (GitFileChangesSubscriber subscriber : subscribers) {
      subscriber.onFileUnderGitChanged(endpointId, dto);
    }
  }

  public void subscribe(GitFileChangesSubscriber subscriber) {
    subscribers.add(subscriber);
  }

  public void unsubscribe(GitFileChangesSubscriber subscriber) {
    subscribers.remove(subscriber);
  }
}
