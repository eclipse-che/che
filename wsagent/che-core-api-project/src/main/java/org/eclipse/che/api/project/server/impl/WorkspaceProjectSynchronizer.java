/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.impl;

import static org.eclipse.che.api.project.server.impl.ProjectDtoConverter.asDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.notification.ProjectCreatedEvent;
import org.eclipse.che.api.project.server.notification.ProjectDeletedEvent;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.api.workspace.server.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class WorkspaceProjectSynchronizer {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceProjectSynchronizer.class);

  private final HttpJsonRequestFactory httpJsonRequestFactory;
  private final RemoteProjects remoteProjects;
  private final ProjectManager projectManager;
  private final WorkspaceSyncCommunication workspaceSyncCommunication;

  private final String apiEndpoint;
  private final String workspaceId;

  private EventService eventService;

  @Inject
  public WorkspaceProjectSynchronizer(
      @Named("che.api") String apiEndpoint,
      HttpJsonRequestFactory httpJsonRequestFactory,
      RemoteProjects remoteProjects,
      ProjectManager projectManager,
      WorkspaceSyncCommunication workspaceSyncCommunication,
      EventService eventService)
      throws ServerException {
    this.apiEndpoint = apiEndpoint;
    this.httpJsonRequestFactory = httpJsonRequestFactory;
    this.workspaceSyncCommunication = workspaceSyncCommunication;
    this.remoteProjects = remoteProjects;
    this.projectManager = projectManager;
    this.eventService = eventService;

    this.workspaceId = System.getenv("CHE_WORKSPACE_ID");

    LOG.info("Workspace ID: " + workspaceId);
    LOG.info("API Endpoint: " + apiEndpoint);
  }

  @PostConstruct
  protected void initializeListeners() {
    eventService.subscribe(
        new EventSubscriber<ProjectCreatedEvent>() {
          @Override
          public void onEvent(ProjectCreatedEvent event) {
            synchronize();
          }
        });

    eventService.subscribe(
        new EventSubscriber<ProjectDeletedEvent>() {
          @Override
          public void onEvent(ProjectDeletedEvent event) {
            synchronize();
          }
        });
  }

  private void synchronize() {

    try {
      Set<ProjectConfig> remote = remoteProjects.getAll();

      // check on removed
      List<ProjectConfig> removed = new ArrayList<>();
      for (ProjectConfig r : remote) {
        if (!projectManager.get(r.getPath()).isPresent()) {
          removed.add(r);
        }
      }

      for (ProjectConfig r : removed) {
        remove(r);
      }

      // update or add
      for (RegisteredProject project : projectManager.getAll()) {

        if (!project.isSynced() && !project.isDetected()) {

          final ProjectConfig config =
              new NewProjectConfigImpl(
                  project.getPath(),
                  project.getType(),
                  project.getMixins(),
                  project.getName(),
                  project.getDescription(),
                  project.getPersistableAttributes(),
                  null,
                  project.getSource());

          boolean found = false;
          for (ProjectConfig r : remote) {
            if (r.getPath().equals(project.getPath())) {
              update(config);
              found = true;
            }
          }

          if (!found) {
            add(config);
          }

          project.setSynced(true);
        }
      }

      workspaceSyncCommunication.synchronizeWorkspace();
    } catch (ServerException e) {
      LOG.error("Error synchronizing projects", e);
    }
  }

  private void add(ProjectConfig project) throws ServerException {
    final UriBuilder builder =
        UriBuilder.fromUri(apiEndpoint)
            .path(WorkspaceService.class)
            .path(WorkspaceService.class, "addProject");
    final String href = builder.build(workspaceId).toString();
    try {
      httpJsonRequestFactory.fromUrl(href).usePostMethod().setBody(asDto(project)).request();
    } catch (IOException | ApiException e) {
      throw new ServerException(e.getMessage());
    }
  }

  private void update(ProjectConfig project) throws ServerException {

    final UriBuilder builder =
        UriBuilder.fromUri(apiEndpoint)
            .path(WorkspaceService.class)
            .path(WorkspaceService.class, "updateProject");
    final String href =
        builder.build(new String[] {workspaceId, project.getPath()}, false).toString();
    try {
      httpJsonRequestFactory.fromUrl(href).usePutMethod().setBody(asDto(project)).request();
    } catch (IOException | ApiException e) {
      throw new ServerException(e.getMessage());
    }
  }

  private void remove(ProjectConfig project) throws ServerException {

    final UriBuilder builder =
        UriBuilder.fromUri(apiEndpoint)
            .path(WorkspaceService.class)
            .path(WorkspaceService.class, "deleteProject");

    final String href =
        builder.build(new String[] {workspaceId, project.getPath()}, false).toString();
    try {
      httpJsonRequestFactory.fromUrl(href).useDeleteMethod().request();
    } catch (IOException | ApiException e) {
      throw new ServerException(e.getMessage());
    }
  }
}
