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
package org.eclipse.che.api.project.server.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/** @author gazarenkov */
@Singleton
public class SimpleProjectHandlerRegistry
    implements ProjectHandlerRegistry {

  private final Map<String, CreateProjectHandler> createProjectHandlers = new HashMap<>();
  private final Map<String, PostImportProjectHandler> postImportProjectHandlers = new HashMap<>();
  private final Map<String, GetItemHandler> getItemHandlers = new HashMap<>();
  private final Map<String, ProjectInitHandler> projectInitHandlers = new HashMap<>();

  @Inject
  public SimpleProjectHandlerRegistry(Set<ProjectHandler> projectHandlers) {
    projectHandlers.forEach(this::register);
  }

  public void register(@NotNull ProjectHandler handler) {
    if (handler instanceof CreateProjectHandler) {
      createProjectHandlers.put(handler.getProjectType(), (CreateProjectHandler) handler);
    } else if (handler instanceof GetItemHandler) {
      getItemHandlers.put(handler.getProjectType(), (GetItemHandler) handler);
    } else if (handler instanceof PostImportProjectHandler) {
      postImportProjectHandlers.put(handler.getProjectType(), (PostImportProjectHandler) handler);
    } else if (handler instanceof ProjectInitHandler) {
      projectInitHandlers.put(handler.getProjectType(), (ProjectInitHandler) handler);
    }
  }

  public Optional<CreateProjectHandler> getCreateHandler(String projectType) {
    return Optional.ofNullable(createProjectHandlers.get(projectType));
  }

  public Optional<GetItemHandler> getGetItemHandler(String projectType) {
    return Optional.ofNullable(getItemHandlers.get(projectType));
  }

  public Optional<PostImportProjectHandler> getPostImportHandler(String projectType) {
    return Optional.ofNullable(postImportProjectHandlers.get(projectType));
  }

  public Optional<ProjectInitHandler> getProjectInitHandler(String projectType) {
    return Optional.ofNullable(projectInitHandlers.get(projectType));
  }
}
