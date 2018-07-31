/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.type;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Set;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set {@link BaseProjectType} for all sub-projects.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class InitBaseProjectTypeHandler implements ProjectInitHandler {
  private static final Logger LOG = LoggerFactory.getLogger(InitBaseProjectTypeHandler.class);

  private final ProjectManager projectManager;

  @Inject
  public InitBaseProjectTypeHandler(ProjectManager projectManager) {
    this.projectManager = projectManager;
  }

  @Override
  public String getProjectType() {
    return BaseProjectType.ID;
  }

  @Override
  public void onProjectInitialized(String wsPath)
      throws ServerException, ForbiddenException, ConflictException, NotFoundException {
    Set<RegisteredProject> projects = projectManager.getAll(wsPath);
    for (RegisteredProject project : projects) {
      if (project.isDetected()) {
        try {
          projectManager.setType(project.getPath(), BaseProjectType.ID, false);
        } catch (BadRequestException e) {
          LOG.error("Can't initialize project properly: {}", wsPath, e);
        }
      }
    }
  }
}
