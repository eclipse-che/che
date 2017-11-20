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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.io.File.separator;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.fs.server.WsPathUtils.parentOf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.model.workspace.config.SourceStorage;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.ProjectImporter;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.shared.NewProjectConfig;

@Singleton
public class ProjectImportManager {

  private final FsManager fsManager;
  private final ProjectSynchronizer projectSynchronizer;
  private final ProjectConfigRegistry projectConfigRegistry;
  private final ProjectImporterRegistry projectImporterRegistry;
  private final ProjectHandlerRegistry projectHandlerRegistry;

  @Inject
  public ProjectImportManager(
      FsManager fsManager,
      ProjectConfigRegistry projectConfigs,
      ProjectSynchronizer projectSynchronizer,
      ProjectImporterRegistry projectImporterRegistry,
      ProjectHandlerRegistry projectHandlerRegistry) {
    this.fsManager = fsManager;
    this.projectSynchronizer = projectSynchronizer;
    this.projectConfigRegistry = projectConfigs;
    this.projectImporterRegistry = projectImporterRegistry;
    this.projectHandlerRegistry = projectHandlerRegistry;
  }

  public Set<RegisteredProject> doImport(
      Set<? extends NewProjectConfig> newProjectConfigs,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException, BadRequestException {
    for (NewProjectConfig projectConfig : newProjectConfigs) {
      String wsPath = projectConfig.getPath();
      if (isNullOrEmpty(wsPath)) {
        throw new BadRequestException("Path for new project should be defined");
      }

      if (projectConfigRegistry.getOrNull(wsPath) != null && !rewrite) {
        throw new ConflictException("Project already registered: " + wsPath);
      }
    }

    Set<RegisteredProject> importedProjects = new HashSet<>();

    for (NewProjectConfig projectConfig : newProjectConfigs) {
      try {
        RegisteredProject project = doImport(projectConfig, rewrite, consumer);
        importedProjects.add(project);
      } catch (ServerException
          | ForbiddenException
          | UnauthorizedException
          | ConflictException
          | NotFoundException e) {
        for (RegisteredProject importedProject : importedProjects) {
          String path = importedProject.getPath();
          fsManager.delete(path);
          projectConfigRegistry.remove(path);
        }

        throw e;
      }
    }
    return importedProjects;
  }

  public RegisteredProject doImport(
      NewProjectConfig projectConfig, boolean rewrite, BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException, BadRequestException {
    String wsPath = projectConfig.getPath();
    if (isNullOrEmpty(wsPath)) {
      throw new BadRequestException("Path for new project should be defined");
    }

    if (projectConfigRegistry.getOrNull(wsPath) != null && !rewrite) {
      throw new ConflictException("Project already registered: " + wsPath);
    }

    fsManager.delete(wsPath);
    projectConfigRegistry.remove(wsPath);

    if (isNullOrEmpty(projectConfig.getType())) {
      projectConfig.setType(BaseProjectType.ID);
    }

    try {
      SourceStorage sourceStorage = projectConfig.getSource();
      if (sourceStorage != null && !isNullOrEmpty(sourceStorage.getLocation())) {
        return doImportInternally(wsPath, sourceStorage, consumer);
      } else {
        String projectWsPath = projectConfig.getPath();
        if (projectWsPath == null) {
          throw new BadRequestException("Path is not defined.");
        }

        String projectParentWsPath = parentOf(projectWsPath);
        if (!fsManager.existsAsDir(projectParentWsPath)) {
          throw new NotFoundException("The parent '" + projectParentWsPath + "' does not exist.");
        }

        String type = projectConfig.getType();
        if (type == null) {
          throw new ConflictException("Project type is not defined: " + projectWsPath);
        }

        if (projectConfigRegistry.get(projectWsPath).isPresent()) {
          throw new ConflictException("Project config already exists for: " + projectWsPath);
        }

        Optional<CreateProjectHandler> cphOptional = projectHandlerRegistry.getCreateHandler(type);

        if (cphOptional.isPresent()) {
          CreateProjectHandler generator = cphOptional.get();
          Map<String, AttributeValue> valueMap = new HashMap<>();
          Map<String, List<String>> attributes = projectConfig.getAttributes();
          if (attributes != null) {
            for (Entry<String, List<String>> entry : attributes.entrySet()) {
              valueMap.put(entry.getKey(), new AttributeValue(entry.getValue()));
            }
          }

          Map<String, String> options =
              projectConfig.getOptions() == null ? new HashMap<>() : projectConfig.getOptions();

          generator.onCreateProject(projectWsPath, valueMap, options);
        } else {
          fsManager.createDir(projectWsPath);
        }

        RegisteredProject project = projectConfigRegistry.put(projectConfig, true, false);
        projectSynchronizer.synchronize();
        List<String> types = new ArrayList<>(project.getMixins());
        types.add(project.getType());

        for (String item : types) {
          Optional<ProjectInitHandler> hOptional =
              projectHandlerRegistry.getProjectInitHandler(item);
          if (hOptional.isPresent()) {
            hOptional.get().onProjectInitialized(project.getBaseFolder());
          }
        }

        return project;
      }
    } catch (ServerException
        | ForbiddenException
        | UnauthorizedException
        | ConflictException
        | NotFoundException e) {
      fsManager.delete(wsPath);
      projectConfigRegistry.remove(wsPath);
      projectSynchronizer.synchronize();

      throw e;
    }
  }

  public Set<RegisteredProject> doImport(
      Map<String, SourceStorage> projectLocations,
      boolean rewrite,
      BiConsumer<String, String> jsonRpcConsumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException {
    for (Entry<String, SourceStorage> entry : projectLocations.entrySet()) {
      String wsPath = entry.getKey();

      String parentWsPath = wsPath.substring(0, wsPath.lastIndexOf(separator));
      if (!fsManager.existsAsDir(parentWsPath)) {
        throw new NotFoundException("Project parent does not exist: " + parentWsPath);
      }

      if (fsManager.exists(wsPath) && !rewrite) {
        throw new ConflictException("Project already exists: " + wsPath);
      }

      String type = entry.getValue().getType();
      if (projectImporterRegistry.isRegistered(type)) {
        throw new NotFoundException("No corresponding importer found: " + type);
      }
    }

    Set<RegisteredProject> importedProjects = new HashSet<>();
    for (Entry<String, SourceStorage> entry : projectLocations.entrySet()) {
      String wsPath = entry.getKey();
      SourceStorage sourceStorage = entry.getValue();

      try {
        RegisteredProject project = doImport(wsPath, sourceStorage, rewrite, jsonRpcConsumer);
        importedProjects.add(project);
      } catch (ServerException
          | ForbiddenException
          | UnauthorizedException
          | ConflictException
          | NotFoundException e) {
        for (RegisteredProject importedProject : importedProjects) {
          String path = importedProject.getPath();
          fsManager.delete(path);
          projectConfigRegistry.remove(path);
        }
        projectSynchronizer.synchronize();

        throw e;
      }
    }

    return importedProjects;
  }

  public RegisteredProject doImport(
      String wsPath,
      SourceStorage sourceStorage,
      boolean rewrite,
      BiConsumer<String, String> jsonRpcConsumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException {
    String type = sourceStorage.getType();

    String parentWsPath = parentOf(wsPath);
    if (!fsManager.existsAsDir(parentWsPath)) {
      throw new NotFoundException("Project parent does not exist: " + parentWsPath);
    }

    if (fsManager.exists(wsPath) && !rewrite) {
      throw new ConflictException("Project already exists: " + wsPath);
    }

    if (!projectImporterRegistry.isRegistered(type)) {
      throw new NotFoundException("No corresponding importer found: " + type);
    }
    try {
      return doImportInternally(wsPath, sourceStorage, jsonRpcConsumer);
    } catch (ServerException
        | ForbiddenException
        | UnauthorizedException
        | ConflictException
        | NotFoundException e) {
      fsManager.delete(wsPath);
      projectConfigRegistry.remove(wsPath);
      projectSynchronizer.synchronize();

      throw e;
    }
  }

  private RegisteredProject doImportInternally(
      String wsPath, SourceStorage sourceStorage, BiConsumer<String, String> jsonRpcConsumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException {
    String type = sourceStorage.getType();
    ProjectImporter importer = projectImporterRegistry.getOrNull(type);

    fsManager.createDir(wsPath);

    try {
      importer.doImport(sourceStorage, wsPath, jsonRpcConsumer(wsPath, jsonRpcConsumer));
    } catch (IOException e) {
      throw new ServerException(e);
    }

    if (projectSynchronizer
        .getAll()
        .stream()
        .anyMatch(it -> Objects.equals(it.getPath(), wsPath))) {
      Set<ProjectConfig> newProjectConfigs =
          projectSynchronizer
              .getAll()
              .stream()
              .filter(it -> wsPath.startsWith(it.getPath()))
              .collect(toSet());

      for (ProjectConfig newProjectConfig : newProjectConfigs) {
        projectConfigRegistry.put(newProjectConfig, true, false);
      }

      return projectConfigRegistry
          .get(wsPath)
          .orElseThrow(() -> new ServerException("Unexpected error"));
    }

    String name = wsPath.substring(wsPath.lastIndexOf(separator));
    NewProjectConfigImpl newProjectConfig =
        new NewProjectConfigImpl(wsPath, name, BaseProjectType.ID, sourceStorage);
    RegisteredProject registeredProject = projectConfigRegistry.put(newProjectConfig, true, false);
    projectSynchronizer.synchronize();
    return registeredProject;
  }

  private Supplier<LineConsumer> jsonRpcConsumer(
      String wsPath, BiConsumer<String, String> consumer) {
    return () ->
        new LineConsumer() {
          @Override
          public void writeLine(String line) throws IOException {
            String projectName = wsPath.substring(wsPath.lastIndexOf(separator));
            consumer.accept(projectName, line);
          }

          @Override
          public void close() throws IOException {}
        };
  }
}
