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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.annotation.PostConstruct;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.languageserver.LanguageServiceUtils;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.notification.PreProjectDeletedEvent;
import org.eclipse.che.api.project.server.notification.ProjectCreatedEvent;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateWorkspaceParameters;

/**
 * Monitors projects activity and updates jdt.ls workspace.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class ProjectsListener {
  private final EventService eventService;
  private final ProjectManager projectManager;
  private final WorkspaceSynchronizer workspaceSynchronizer;

  @Inject
  public ProjectsListener(
      EventService eventService,
      ProjectManager projectManager,
      WorkspaceSynchronizer workspaceSynchronizer) {
    this.eventService = eventService;
    this.projectManager = projectManager;
    this.workspaceSynchronizer = workspaceSynchronizer;
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
  }

  private void onProjectCreated(ProjectCreatedEvent event) {
    if (!isProjectRegistered(event.getProjectPath())) {
      return;
    }

    String projectUri = LanguageServiceUtils.prefixURI(event.getProjectPath());
    UpdateWorkspaceParameters params =
        new UpdateWorkspaceParameters(singletonList(projectUri), emptyList());
    workspaceSynchronizer.syncronizerWorkspaceAsync(params);
  }

  private void onPreProjectDeleted(PreProjectDeletedEvent event) {
    if (!isProjectRegistered(event.getProjectPath())) {
      return;
    }

    String projectUri = LanguageServiceUtils.prefixURI(event.getProjectPath());
    UpdateWorkspaceParameters params =
        new UpdateWorkspaceParameters(emptyList(), singletonList(projectUri));
    workspaceSynchronizer.syncronizerWorkspaceAsync(params);
  }

  private boolean isProjectRegistered(String path) {
    return projectManager.isRegistered(path);
  }
}
