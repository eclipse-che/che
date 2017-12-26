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
package org.eclipse.che.plugin.java.languageserver;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.prefixURI;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.PostConstruct;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.project.server.notification.PreProjectDeletedEvent;
import org.eclipse.che.api.project.server.notification.ProjectCreatedEvent;
import org.eclipse.che.api.project.server.notification.ProjectInitializedEvent;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.eclipse.che.jdt.ls.extension.api.dto.JobResult;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateWorkspaceParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors projects activity and updates jdt.ls workspace.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class ProjectsListener {
  private static final Logger LOG = LoggerFactory.getLogger(ProjectsListener.class);

  private final JavaLanguageServerExtensionService service;
  private final EventService eventService;
  private final ExecutorService executorService;

  @Inject
  public ProjectsListener(JavaLanguageServerExtensionService service, EventService eventService) {
    this.service = service;
    this.eventService = eventService;
    this.executorService =
        Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                .setNameFormat("WorkspaceUpdater-%d")
                .setUncaughtExceptionHandler(LoggingUncaughtExceptionHandler.getInstance())
                .setDaemon(true)
                .build());
  }

  @PostConstruct
  protected void initializeListeners() {
    eventService.subscribe(
        new EventSubscriber<ProjectCreatedEvent>() {
          @Override
          public void onEvent(ProjectCreatedEvent event) {
            onProjectCreated(event);
          }
        });

    eventService.subscribe(
        new EventSubscriber<PreProjectDeletedEvent>() {
          @Override
          public void onEvent(PreProjectDeletedEvent event) {
            onPreProjectDeleted(event);
          }
        });

    eventService.subscribe(
        new EventSubscriber<ProjectInitializedEvent>() {
          @Override
          public void onEvent(ProjectInitializedEvent event) {
            onProjectInitializedEvent(event);
          }
        });
  }

  private void onProjectCreated(ProjectCreatedEvent event) {
    String projectUri = prefixURI(event.getProjectPath());

    UpdateWorkspaceParameters params =
        new UpdateWorkspaceParameters(singletonList(projectUri), emptyList());
    doUpdateWorkspaceAsync(params);
  }

  private void onPreProjectDeleted(PreProjectDeletedEvent event) {
    String projectUri = prefixURI(event.getProjectPath());

    UpdateWorkspaceParameters params =
        new UpdateWorkspaceParameters(emptyList(), singletonList(projectUri));
    doUpdateWorkspaceAsync(params);
  }

  private void onProjectInitializedEvent(ProjectInitializedEvent event) {
    String projectUri = prefixURI(event.getProjectPath());

    UpdateWorkspaceParameters params =
        new UpdateWorkspaceParameters(singletonList(projectUri), emptyList());
    doUpdateWorkspaceAsync(params);
  }

  private void doUpdateWorkspaceAsync(UpdateWorkspaceParameters updateWorkspaceParameters) {
    executorService.submit(
        (Runnable)
            () -> {
              LOG.info(
                  "Workspace isb being updated with added projects'{}', removed projects '{}'",
                  updateWorkspaceParameters.getAddedProjectsUri().toString(),
                  updateWorkspaceParameters.getRemovedProjectsUri().toString());

              JobResult jobResult = service.updateWorkspace(updateWorkspaceParameters);

              switch (jobResult.getSeverity()) {
                case ERROR:
                  LOG.error(
                      "Workspace updated. Result code: '{}', message: '{}'",
                      jobResult.getResultCode(),
                      jobResult.getMessage());
                  break;
                case WARNING:
                case CANCEL:
                  LOG.warn(
                      "Workspace updated. Result code: '{}', message: '{}'",
                      jobResult.getResultCode(),
                      jobResult.getMessage());
                  break;
                default:
                  LOG.info(
                      "Workspace updated. Result code: '{}', message: '{}'",
                      jobResult.getResultCode(),
                      jobResult.getMessage());
                  break;
              }
            });
  }
}
