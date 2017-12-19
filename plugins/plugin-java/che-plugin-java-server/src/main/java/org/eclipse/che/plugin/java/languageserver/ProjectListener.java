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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.languageserver.service.LanguageServiceUtils;
import org.eclipse.che.api.project.server.notification.ProjectCreatedEvent;
import org.eclipse.che.api.project.server.notification.ProjectDeletedEvent;
import org.eclipse.che.jdt.ls.extension.api.dto.UpdateWorkspaceParameters;

import javax.annotation.PostConstruct;
import java.util.Collections;

import static java.util.Collections.singletonList;

/** @author Anatolii Bazko */
@Singleton
public class ProjectListener {
  private final JavaLanguageServerExtensionService service;
  private final EventService eventService;

  @Inject
  public ProjectListener(JavaLanguageServerExtensionService service, EventService eventService) {
    this.service = service;
    this.eventService = eventService;
  }

  @PostConstruct
  protected void initializeListeners() {
    eventService.subscribe(
        new EventSubscriber<ProjectCreatedEvent>() {
          @Override
          public void onEvent(ProjectCreatedEvent event) {
            String projectUri = LanguageServiceUtils.prefixURI(event.getProjectPath());

            UpdateWorkspaceParameters updateWorkspaceParameters = new UpdateWorkspaceParameters();
            updateWorkspaceParameters.setAddedProjectsUri(singletonList(projectUri));

            service.updateWorkspace(updateWorkspaceParameters);
          }
        });

    eventService.subscribe(
        new EventSubscriber<ProjectDeletedEvent>() {
          @Override
          public void onEvent(ProjectDeletedEvent event) {
            String projectUri = LanguageServiceUtils.prefixURI(event.getProjectPath());

            UpdateWorkspaceParameters updateWorkspaceParameters = new UpdateWorkspaceParameters();
            updateWorkspaceParameters.setRemovedProjectsUri(singletonList(projectUri));

            service.updateWorkspace(updateWorkspaceParameters);
          }
        });
  }
}
