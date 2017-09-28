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
package org.eclipse.che.api.project.server.impl;

import static org.eclipse.che.api.project.server.type.ProjectTypeRegistry.CHILD_TO_PARENT_COMPARATOR;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.fs.api.FsManager;
import org.eclipse.che.api.project.server.type.ProjectTypeDef;
import org.eclipse.che.api.project.server.type.ProjectTypeRegistry;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.server.type.ProjectTypeResolver;

@Singleton
public class ProjectQualifier implements org.eclipse.che.api.project.server.api.ProjectQualifier {

  private final ProjectTypeRegistry projectTypeRegistry;
  private final FsManager fileSystemManager;
  private final ProjectTypeResolver projectTypeResolver;

  @Inject
  public ProjectQualifier(ProjectTypeRegistry projectTypeRegistry, FsManager fileSystemManager,
      ProjectTypeResolver projectTypeResolver) {
    this.projectTypeRegistry = projectTypeRegistry;
    this.fileSystemManager = fileSystemManager;
    this.projectTypeResolver = projectTypeResolver;
  }

  @Override
  public ProjectTypeResolution qualify(String wsPath, String projectTypeId)
      throws ServerException, NotFoundException {

    ProjectTypeDef projectType = projectTypeRegistry.getProjectType(projectTypeId);
    if (projectType == null) {
      throw new NotFoundException("Project Type to estimate needed.");
    }

    if (!fileSystemManager.isDirectory(wsPath)) {
      throw new NotFoundException("Path is not a directory:" + wsPath);
    }

    return projectTypeResolver.resolveSources(projectType, wsPath);
  }

  @Override
  public List<ProjectTypeResolution> qualify(String wsPath)
      throws ServerException, NotFoundException {
    List<ProjectTypeResolution> resolutions = new ArrayList<>();

    for (ProjectType type : projectTypeRegistry.getProjectTypes(CHILD_TO_PARENT_COMPARATOR)) {
      ProjectTypeResolution resolution = qualify(wsPath, type.getId());
      if (resolution.matched()) {
        resolutions.add(resolution);
      }
    }

    return resolutions;
  }
}
