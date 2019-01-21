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
package org.eclipse.che.plugin.java.languageserver;

import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronizes che and eclipse projects.
 *
 * @author Anatolii Bazko
 */
@Singleton
public class ProjectsSynchronizer {
  private static final Logger LOG = LoggerFactory.getLogger(ProjectsSynchronizer.class);

  private final JavaLanguageServerExtensionService service;
  private final EventService eventService;
  private final ProjectManager projectManager;

  @Inject
  public ProjectsSynchronizer(
      JavaLanguageServerExtensionService service,
      EventService eventService,
      ProjectManager projectManager) {
    this.service = service;
    this.eventService = eventService;
    this.projectManager = projectManager;
  }

  /**
   * Retrieves maven projects from the JDT.LS and creates missed ones.
   *
   * @param rootPath the root pass to synchronize projects. It can be either a project path or any
   *     path inside the project
   */
  public void synchronize(String rootPath) {
    Set<String> mavenProjects;
    try {
      mavenProjects = new HashSet<>(service.getMavenProjects(rootPath, 1, TimeUnit.HOURS));
      for (String mavenProjectPath : mavenProjects) {
        ensureMavenProject(mavenProjectPath);
      }
    } catch (JsonSyntaxException | InterruptedException | ExecutionException | TimeoutException e) {
      LOG.error("Error getting maven projects", e);
    }
  }

  public void ensureMavenProject(String mavenProjectPath) {
    Optional<RegisteredProject> project = projectManager.get(mavenProjectPath);
    if (!project.isPresent()) {
      doCreateProject(mavenProjectPath);
    }
  }

  private void doCreateProject(String projectPath) {
    ProjectConfigImpl projectConfig = new ProjectConfigImpl();
    projectConfig.setSource(new SourceStorageImpl());
    projectConfig.setType("maven");
    projectConfig.setPath(projectPath);
    projectConfig.setName(projectPath.substring(projectPath.lastIndexOf("/") + 1));
    try {
      projectManager.update(projectConfig);
    } catch (ConflictException
        | ForbiddenException
        | NotFoundException
        | BadRequestException
        | ServerException e) {
      LOG.error(String.format("Failed to update project '%s'", projectConfig.getPath()));
    }
  }
}
