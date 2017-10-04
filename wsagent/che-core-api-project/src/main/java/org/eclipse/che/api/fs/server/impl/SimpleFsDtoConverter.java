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
package org.eclipse.che.api.fs.server.impl;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.fs.server.FsDtoConverter;
import org.eclipse.che.api.fs.server.FsPaths;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.shared.dto.ItemReference;

@Singleton
public class SimpleFsDtoConverter implements FsDtoConverter {

  private final FsPaths fsPaths;
  private final ProjectManager projectManager;
  private final ExecutiveFsManager executiveFsManager;

  @Inject
  public SimpleFsDtoConverter(
      FsPaths fsPaths, ProjectManager projectManager, ExecutiveFsManager executiveFsManager) {
    this.fsPaths = fsPaths;
    this.projectManager = projectManager;
    this.executiveFsManager = executiveFsManager;
  }

  @Override
  public ItemReference asDto(String wsPath) throws NotFoundException {
    if (!executiveFsManager.exists(wsPath)) {
      throw new NotFoundException("Can't find item " + wsPath);
    }

    File file = fsPaths.toFsPath(wsPath).toFile();
    String name = file.getName();
    String projectPath =
        projectManager
            .getClosest(wsPath)
            .orElseThrow(() -> new NotFoundException("Can't find project for item " + wsPath))
            .getPath();

    String type;
    if (projectManager.isRegistered(wsPath)) {
      type = "project";
    } else if (executiveFsManager.isDirectory(wsPath)) {
      type = "folder";
    } else {
      type = "file";
    }
    long lastModified = executiveFsManager.lastModified(wsPath);

    ItemReference itemReference =
        newDto(ItemReference.class)
            .withName(name)
            .withPath(wsPath)
            .withProject(projectPath)
            .withType(type)
            .withModified(lastModified);

    if (executiveFsManager.isFile(wsPath)) {
      itemReference.withContentLength(executiveFsManager.length(wsPath));
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
