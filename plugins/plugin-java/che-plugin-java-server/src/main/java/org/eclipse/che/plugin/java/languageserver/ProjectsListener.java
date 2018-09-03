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
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.languageserver.LanguageServiceUtils;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.notification.PreProjectDeletedEvent;
import org.eclipse.che.api.project.server.notification.ProjectCreatedEvent;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateWorkspaceParameters;
import org.eclipse.che.plugin.maven.shared.MavenAttributes;

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
    if (!isJavaProject(event.getProjectPath())) {
      return;
    }

    String projectUri = LanguageServiceUtils.prefixURI(event.getProjectPath());
    UpdateWorkspaceParameters params =
        new UpdateWorkspaceParameters(singletonList(projectUri), emptyList());
    workspaceSynchronizer.syncronizerWorkspaceAsync(params);
  }

  private void onPreProjectDeleted(PreProjectDeletedEvent event) {
    if (!isJavaProject(event.getProjectPath())) {
      return;
    }

    String projectUri = LanguageServiceUtils.prefixURI(event.getProjectPath());
    UpdateWorkspaceParameters params =
        new UpdateWorkspaceParameters(emptyList(), singletonList(projectUri));
    workspaceSynchronizer.syncronizerWorkspaceAsync(params);
  }

  private boolean isJavaProject(String path) {
    Optional<RegisteredProject> project = projectManager.get(path);
    return (project.isPresent()
        && (MavenAttributes.MAVEN_ID.equals(project.get().getType())
            || Constants.JAVAC.equals(project.get().getType())));
  }
}
