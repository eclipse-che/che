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
package org.eclipse.che.api.fs.server.impl;

import static org.eclipse.che.api.fs.server.WsPathUtils.nameOf;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ErrorCodes;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.rest.shared.dto.ExtendedError;
import org.eclipse.che.api.fs.server.FsDtoConverter;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.ProjectDtoConverter;
import org.eclipse.che.api.project.shared.dto.ItemReference;

@Singleton
public class SimpleFsDtoConverter implements FsDtoConverter {

  private final ProjectManager projectManager;
  private final FsManager fsManager;

  @Inject
  public SimpleFsDtoConverter(ProjectManager projectManager, FsManager fsManager) {
    this.projectManager = projectManager;
    this.fsManager = fsManager;
  }

  @Override
  public ItemReference asDto(String wsPath) throws NotFoundException {
    if (!fsManager.exists(wsPath)) {
      throw new NotFoundException(
          newDto(ExtendedError.class)
              .withMessage("Can't find item " + wsPath)
              .withErrorCode(ErrorCodes.ITEM_NOT_FOUND));
    }

    String name = nameOf(wsPath);

    Long length;
    if (fsManager.isFile(wsPath)) {
      length = fsManager.length(wsPath);
    } else {
      length = null;
    }

    ProjectConfig project = projectManager.getClosest(wsPath).orElse(null);

    String type;
    if (projectManager.isRegistered(wsPath)) {
      type = "project";
    } else if (fsManager.isDir(wsPath)) {
      type = "folder";
    } else {
      type = "file";
    }

    ItemReference itemReference =
        newDto(ItemReference.class).withName(name).withPath(wsPath).withType(type);

    if (project != null) {
      itemReference.withProject(project.getPath());
      if (wsPath.equals(project.getPath())) {
        itemReference.setProjectConfig(ProjectDtoConverter.asDto(project));
      }
    }

    if (length != null) {
      itemReference.withContentLength(length);
    }

    return itemReference;
  }

  @Override
  public List<ItemReference> asDto(List<String> wsPaths) throws NotFoundException {
    List<ItemReference> result = new LinkedList<>();
    for (String path : wsPaths) {
      result.add(asDto(path));
    }
    return result;
  }

  @Override
  public Set<ItemReference> asDto(Set<String> wsPaths) throws NotFoundException {
    Set<ItemReference> result = new HashSet<>();
    for (String path : wsPaths) {
      result.add(asDto(path));
    }
    return result;
  }
}
