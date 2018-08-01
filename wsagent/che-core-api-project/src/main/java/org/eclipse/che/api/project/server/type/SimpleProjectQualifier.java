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
package org.eclipse.che.api.project.server.type;

import static org.eclipse.che.api.project.server.type.ProjectTypeRegistry.CHILD_TO_PARENT_COMPARATOR;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.fs.server.FsManager;

@Singleton
public class SimpleProjectQualifier implements ProjectQualifier {

  private final ProjectTypeRegistry projectTypeRegistry;
  private final FsManager fsManager;
  private final ProjectTypeResolver projectTypeResolver;

  @Inject
  public SimpleProjectQualifier(
      ProjectTypeRegistry projectTypeRegistry,
      FsManager fsManager,
      ProjectTypeResolver projectTypeResolver) {
    this.projectTypeRegistry = projectTypeRegistry;
    this.fsManager = fsManager;
    this.projectTypeResolver = projectTypeResolver;
  }

  @Override
  public ProjectTypeResolution qualify(String wsPath, String projectTypeId)
      throws ServerException, NotFoundException {

    ProjectTypeDef projectType = projectTypeRegistry.getProjectType(projectTypeId);
    if (projectType == null) {
      throw new NotFoundException("Project type required");
    }

    if (!fsManager.existsAsDir(wsPath)) {
      throw new NotFoundException("Item is not a directory or does not exist " + wsPath);
    }

    return projectTypeResolver.resolve(projectType, wsPath);
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
