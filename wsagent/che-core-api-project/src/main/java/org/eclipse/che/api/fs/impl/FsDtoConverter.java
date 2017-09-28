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
package org.eclipse.che.api.fs.impl;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.project.server.api.ProjectManager;
import org.eclipse.che.api.project.shared.dto.ItemReference;

@Singleton
public class FsDtoConverter implements org.eclipse.che.api.fs.api.FsDtoConverter {

  private final PathResolver pathResolver;
  private final ProjectManager projectManager;
  private final FsManager fsManager;

  @Inject
  public FsDtoConverter(PathResolver pathResolver, ProjectManager projectManager, FsManager fsManager) {
    this.pathResolver = pathResolver;
    this.projectManager = projectManager;
    this.fsManager = fsManager;
  }

  @Override
  public ItemReference asDto(String wsPath) throws NotFoundException {
    File file = pathResolver.toFsPath(wsPath).toFile();

    String name = file.getName();
    String project = projectManager.getClosest(wsPath).orElseThrow(exception()).getName();
    String type = fsManager.isFile(wsPath) ? "file" : "folder";
    long lastModified = fsManager.lastModified(wsPath);

    ItemReference itemReference =
        newDto(ItemReference.class)
            .withName(name)
            .withPath(wsPath)
            .withProject(project)
            .withType(type)
            .withModified(lastModified);

    if (fsManager.isFile(wsPath)) {
      itemReference.withContentLength(fsManager.length(wsPath));
    }

    return itemReference;
  }

  @Override
  public List<ItemReference> asDto(List<String> paths) throws NotFoundException {
    List<ItemReference> result = new LinkedList<>();
    for (String path : paths) {
      result.add(asDto(path));
    }
    return result;
  }

  @Override
  public Set<ItemReference> asDto(Set<String> paths) throws NotFoundException {
    Set<ItemReference> result = new HashSet<>();
    for (String path : paths) {
      result.add(asDto(path));
    }
    return result;
  }

  private Supplier<NotFoundException> exception() {
    return () -> new NotFoundException("Did find the project that the item belongs to");
  }
}
