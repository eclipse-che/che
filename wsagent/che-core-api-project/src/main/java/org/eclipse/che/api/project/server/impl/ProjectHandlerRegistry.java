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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.handlers.PostImportProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;

/** @author gazarenkov */
@Singleton
public class ProjectHandlerRegistry {

  private final Map<String, CreateProjectHandler> createProjectHandlers = new HashMap<>();
  private final Map<String, PostImportProjectHandler> postImportProjectHandlers = new HashMap<>();
  private final Map<String, ProjectInitHandler> projectInitHandlers = new HashMap<>();

  @Inject
  public ProjectHandlerRegistry(Set<ProjectHandler> projectHandlers) {
    projectHandlers.forEach(this::register);
  }

  public void register(@NotNull ProjectHandler handler) {
    if (handler instanceof CreateProjectHandler) {
      createProjectHandlers.put(handler.getProjectType(), (CreateProjectHandler) handler);

    } else if (handler instanceof PostImportProjectHandler) {
      postImportProjectHandlers.put(handler.getProjectType(), (PostImportProjectHandler) handler);

    } else if (handler instanceof ProjectInitHandler) {
      projectInitHandlers.put(handler.getProjectType(), (ProjectInitHandler) handler);
    }
  }

  public Optional<CreateProjectHandler> getCreateHandler(String projectType) {
    return Optional.ofNullable(createProjectHandlers.get(projectType));
  }

  public Optional<PostImportProjectHandler> getPostImportHandler(String projectType) {
    return Optional.ofNullable(postImportProjectHandlers.get(projectType));
  }

  public Optional<ProjectInitHandler> getProjectInitHandler(String projectType) {
    return Optional.ofNullable(projectInitHandlers.get(projectType));
  }
}
