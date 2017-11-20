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
package org.eclipse.che.plugin.java.server;

import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.io.File;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.notification.ProjectCreatedEvent;
import org.eclipse.che.api.project.server.notification.ProjectItemModifiedEvent;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.jdt.core.resources.ResourceChangedEvent;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Evgen Vidolob */
@Singleton
public class ProjectListeners {

  private static final Logger LOG = LoggerFactory.getLogger(ProjectListeners.class);

  private final File workspace;
  private final ProjectManager projectRegistry;
  private final ProjectTypeRegistry projectTypeRegistry;
  private final PathTransformer pathTransformer;

  @Inject
  public ProjectListeners(
      @Named("che.user.workspaces.storage") String workspacePath,
      EventService eventService,
      ProjectManager projectRegistry,
      ProjectTypeRegistry projectTypeRegistry,
      PathTransformer pathTransformer) {
    this.projectRegistry = projectRegistry;
    this.projectTypeRegistry = projectTypeRegistry;
    workspace = new File(workspacePath);
    this.pathTransformer = pathTransformer;
    eventService.subscribe(new ProjectCreated());
    eventService.subscribe(
        new EventSubscriber<ProjectItemModifiedEvent>() {
          @Override
          public void onEvent(ProjectItemModifiedEvent event) {
            handleEvent(event);
          }
        });
  }

  public void handleEvent(ProjectItemModifiedEvent event) {
    final String eventPath = event.getPath();
    if (!isJavaProject(event.getProject())) {
      return;
    }
    try {
      JavaModelManager.getJavaModelManager()
          .deltaState
          .resourceChanged(new ResourceChangedEvent(workspace, event));
    } catch (Throwable t) {
      // catch all exceptions that may be happened
      LOG.error("Can't update java model in " + eventPath, t);
    }
    if (event.getType() == ProjectItemModifiedEvent.EventType.UPDATED) {
      ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
      ITextFileBuffer fileBuffer =
          manager.getTextFileBuffer(new Path(eventPath), LocationKind.IFILE);
      if (fileBuffer != null) {
        try {
          fileBuffer.revert(new NullProgressMonitor());
        } catch (CoreException e) {
          LOG.error("Can't read file content: " + eventPath, e);
        }
      }
    }
  }

  private boolean isJavaProject(String projectPath) {
    try {
      String wsPath = absolutize(projectPath);
      ProjectConfig project =
          projectRegistry
              .get(wsPath)
              .orElseThrow(() -> new NotFoundException("Can't find project"));
      String type = project.getType();
      return projectTypeRegistry.getProjectType(type).isTypeOf("java");
    } catch (NotFoundException e) {
      LOG.error("Can't find project " + projectPath, e);
      return false;
    }
  }

  private class ProjectCreated implements EventSubscriber<ProjectCreatedEvent> {

    @Override
    public void onEvent(ProjectCreatedEvent event) {
      if (!isJavaProject(event.getProjectPath())) {
        return;
      }
      try {
        JavaModelManager.getJavaModelManager()
            .deltaState
            .resourceChanged(new ResourceChangedEvent(workspace, event));
      } catch (Throwable t) {
        // catch all exceptions that may be happened
        LOG.error("Can't update java model " + event.getProjectPath(), t);
      }
    }
  }
}
