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
package org.eclipse.che.api.project.server.type;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.impl.ProjectDtoConverter;
import org.eclipse.che.api.project.shared.dto.ProjectTypeDto;
import org.eclipse.che.dto.server.DtoFactory;

/** @author gazarenkov */
@Singleton
public class ProjectTypeCache {

  private FsManager fsManager;
  ProjectTypeRegistry projectTypeRegistry;

  @Inject
  public ProjectTypeCache(ProjectTypeRegistry projectTypeRegistry, FsManager fsManager) {
    this.projectTypeRegistry = projectTypeRegistry;
    this.fsManager = fsManager;
  }

  @PostConstruct
  void init() throws NotFoundException, ServerException, ConflictException {
    for (ProjectTypeDef projectType : projectTypeRegistry.getProjectTypes()) {
      ProjectTypeDto projectTypeDto = ProjectDtoConverter.asDto(projectType);
      String json = DtoFactory.getInstance().toJson(projectTypeDto);
      fsManager.createFile("/.che/tmp/projectTypes/" + projectType.getId(), json, false, true);
    }
  }
}
