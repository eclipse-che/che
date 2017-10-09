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
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.fs.server.FsDtoConverter;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.shared.dto.ItemReference;

@Singleton
public class SimpleFsDtoConverter implements FsDtoConverter {

  private final ProjectManager projectManager;
  private final FsManager fsManager;
  private final FsOperations fsOperations;
  private final PathTransformer pathTransformer;

  @Inject
  public SimpleFsDtoConverter(
      ProjectManager projectManager,
      FsManager fsManager,
      FsOperations fsOperations,
      PathTransformer pathTransformer) {
    this.projectManager = projectManager;
    this.fsManager = fsManager;
    this.fsOperations = fsOperations;
    this.pathTransformer = pathTransformer;
  }

  @Override
  public ItemReference asDto(String wsPath) throws NotFoundException {
    Path fsPath = pathTransformer.transform(wsPath);

    boolean exists = fsOperations.exists(fsPath);
    if (!exists) {
      throw new NotFoundException("Can't find item " + wsPath);
    }

    File file = fsOperations.toIoFile(fsPath);
    String name = file.getName();
    String projectPath =
        projectManager
            .getClosest(wsPath)
            .orElseThrow(() -> new NotFoundException("Can't find project for item " + wsPath))
            .getPath();

    String type;
    if (projectManager.isRegistered(wsPath)) {
      type = "project";
    } else if (fsManager.isDir(wsPath)) {
      type = "folder";
    } else {
      type = "file";
    }

    long lastModified = fsOperations.lastModified(fsPath);
    ItemReference itemReference =
        newDto(ItemReference.class)
            .withName(name)
            .withPath(wsPath)
            .withProject(projectPath)
            .withType(type)
            .withModified(lastModified);

    boolean isFile = fsOperations.isFile(fsPath);
    if (isFile) {
      long length = fsOperations.length(fsPath);
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
