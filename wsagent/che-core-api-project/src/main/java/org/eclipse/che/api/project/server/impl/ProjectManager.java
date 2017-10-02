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

import static java.io.File.separator;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
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
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.NewProjectConfigImpl;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.WorkspaceProjectsSyncer;
import org.eclipse.che.api.project.server.api.ProjectConfigRegistry;
import org.eclipse.che.api.project.server.api.ProjectHandlerRegistry;
import org.eclipse.che.api.project.server.api.ProjectQualifier;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.NewProjectConfig;

@Singleton
public class ProjectManager implements org.eclipse.che.api.project.server.api.ProjectManager {

  private final FsManager fsManager;
  private final WorkspaceProjectsSyncer syncer;
  private final ProjectQualifier projectQualifier;
  private final ProjectConfigRegistry projectConfigs;
  private final ProjectHandlerRegistry projectHandlers;
  private final ProjectImportManager projectImportManager;

  @Inject
  public ProjectManager(
      FsManager fsManager,
      ProjectConfigRegistry projectConfigs,
      ProjectHandlerRegistry projectHandlers,
      WorkspaceProjectsSyncer syncer,
      ProjectQualifier projectQualifier,
      ProjectImportManager projectImportManager) {
    this.fsManager = fsManager;
    this.syncer = syncer;
    this.projectConfigs = projectConfigs;
    this.projectHandlers = projectHandlers;
    this.projectQualifier = projectQualifier;
    this.projectImportManager = projectImportManager;
  }

  @Override
  public boolean isRegistered(String wsPath) {
    return projectConfigs.isRegistered(wsPath);
  }

  @Override
  public Optional<RegisteredProject> get(String wsPath) {
    return projectConfigs.get(wsPath);
  }

  public RegisteredProject getOrNull(String wsPath) {
    return projectConfigs.getOrNull(wsPath);
  }

  @Override
  public Optional<RegisteredProject> getClosest(String wsPath) {
    while (!wsPath.isEmpty()) {
      Optional<RegisteredProject> registeredProject = projectConfigs.get(wsPath);
      if (registeredProject.isPresent()) {
        return registeredProject;
      } else {
        wsPath = wsPath.substring(0, wsPath.lastIndexOf(separator));
      }
    }

    return empty();
  }

  @Override
  public RegisteredProject getClosestOrNull(String wsPath) {
    while (!wsPath.isEmpty()) {
      RegisteredProject registeredProject = projectConfigs.getOrNull(wsPath);
      if (registeredProject != null) {
        return registeredProject;
      } else {
        wsPath = wsPath.substring(0, wsPath.lastIndexOf(separator));
      }
    }

    return null;
  }

  @Override
  public Set<RegisteredProject> getAll() {
    return projectConfigs.getAll();
  }

  @Override
  public Set<RegisteredProject> getAll(String wsPath) {
    return projectConfigs.getAll(wsPath);
  }

  @Override
  public Set<RegisteredProject> createAll(Map<ProjectConfig, Map<String, String>> projectConfigs)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
          BadRequestException {
    Set<RegisteredProject> projects = new HashSet<>();

    for (Entry<ProjectConfig, Map<String, String>> entry : projectConfigs.entrySet()) {
      ProjectConfig projectConfig = entry.getKey();
      Map<String, String> options = entry.getValue();
      RegisteredProject registeredProject = create(projectConfig, options);
      projects.add(registeredProject);
    }

    return projects;
  }

  @Override
  public RegisteredProject create(ProjectConfig projectConfig, Map<String, String> options)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
          BadRequestException {
    String path = projectConfig.getPath();
    if (path == null) {
      throw new BadRequestException("Path is not defined.");
    }

    String parentWsPath = path.substring(0, path.lastIndexOf(separator));
    if (!fsManager.isRoot(parentWsPath) || !fsManager.existsAsDirectory(parentWsPath)) {
      throw new NotFoundException("The parent '" + parentWsPath + "' does not exist.");
    }

    String type = projectConfig.getType();
    if (type == null) {
      throw new ConflictException("Project type is not defined: " + path);
    }

    if (projectConfigs.get(path).isPresent()) {
      throw new ConflictException("Project config already exists for: " + path);
    }

    Optional<CreateProjectHandler> cphOptional = projectHandlers.getCreateHandler(type);

    if (cphOptional.isPresent()) {
      CreateProjectHandler generator = cphOptional.get();
      Map<String, AttributeValue> valueMap = new HashMap<>();
      Map<String, List<String>> attributes = projectConfig.getAttributes();
      if (attributes != null) {
        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
          valueMap.put(entry.getKey(), new AttributeValue(entry.getValue()));
        }
      }

      generator.onCreateProject(path, valueMap, options == null ? new HashMap<>() : options);
    } else {
      fsManager.createDirectory(path);
    }

    RegisteredProject project = projectConfigs.put(projectConfig, true, false);
    syncer.sync();
    fireInitHandlers(project);

    return project;
  }

  @Override
  public Set<RegisteredProject> updateAll(Set<ProjectConfig> projectConfigs)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
          BadRequestException {
    Set<RegisteredProject> projects = new HashSet<>();

    for (ProjectConfig projectConfig : projectConfigs) {
      RegisteredProject registeredProject = update(projectConfig);
      projects.add(registeredProject);
    }

    return projects;
  }

  @Override
  public RegisteredProject update(ProjectConfig projectConfig)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
          BadRequestException {
    String wsPath = projectConfig.getPath();
    if (wsPath == null) {
      throw new BadRequestException("Project workspace path is not defined");
    }

    if (!fsManager.existsAsDirectory(wsPath)) {
      throw new NotFoundException("Directory does not exist: " + wsPath);
    }

    RegisteredProject project = projectConfigs.put(projectConfig, true, false);
    syncer.sync();
    fireInitHandlers(project);

    return project;
  }

  @Override
  public Set<RegisteredProject> deleteAll(Set<String> wsPaths)
      throws ServerException, ForbiddenException, NotFoundException, ConflictException {
    Set<RegisteredProject> projects = new HashSet<>();

    for (String wsPath : wsPaths) {
      delete(wsPath).ifPresent(projects::add);
    }

    return projects;
  }

  @Override
  public Optional<RegisteredProject> delete(String wsPath)
      throws ServerException, ForbiddenException, NotFoundException, ConflictException {
    fsManager.deleteDirectory(wsPath);
    Optional<RegisteredProject> registeredProjectOptional = projectConfigs.remove(wsPath);
    syncer.sync();

    return registeredProjectOptional;
  }

  @Override
  public Set<RegisteredProject> deleteAll()
      throws ServerException, ForbiddenException, ConflictException {
    Set<RegisteredProject> deleted = new HashSet<>();
    for (RegisteredProject registeredProject : projectConfigs.getAll()) {
      String path = registeredProject.getPath();
      try {
        delete(path).ifPresent(deleted::add);
      } catch (NotFoundException e) {
        throw new ServerException(e);
      }
    }
    return deleted;
  }

  @Override
  public RegisteredProject copy(String srcWsPath, String srcDstPath, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException {
    if (!fsManager.existsAsDirectory(srcWsPath)) {
      throw new NotFoundException("Project directory does not exist or is a file: " + srcWsPath);
    }

    String parentWsPath = srcDstPath.substring(0, srcDstPath.lastIndexOf(separator));
    if (!fsManager.existsAsDirectory(parentWsPath)) {
      throw new NotFoundException(
          "Destination parent does not exist or is a file: " + parentWsPath);
    }

    if (!overwrite && fsManager.exists(srcDstPath)) {
      throw new ConflictException("Destination item exists but overwrite is false: " + srcDstPath);
    }

    fsManager.copyDirectoryQuietly(srcWsPath, srcDstPath);

    RegisteredProject oldProjectConfig =
        projectConfigs
            .get(srcWsPath)
            .orElseThrow(() -> new ServerException("Project is not registered"));

    String newProjectName = srcDstPath.substring(srcDstPath.lastIndexOf(separator));
    NewProjectConfig newProjectConfig =
        new NewProjectConfigImpl(
            srcDstPath,
            oldProjectConfig.getType(),
            oldProjectConfig.getMixins(),
            newProjectName,
            oldProjectConfig.getDescription(),
            oldProjectConfig.getAttributes(),
            emptyMap(),
            oldProjectConfig.getSource());

    RegisteredProject copiedProject = projectConfigs.put(newProjectConfig, true, false);
    syncer.sync();
    fireInitHandlers(copiedProject);
    return copiedProject;
  }

  @Override
  public RegisteredProject setType(String wsPath, String type, boolean asMixin)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException {

    RegisteredProject project =
        get(wsPath).orElseThrow(() -> new NotFoundException("Can't find project"));

    List<String> mixins = project.getMixins();
    if (asMixin) {
      if (!mixins.contains(type)) {
        mixins.add(type);
      }
    }

    NewProjectConfig conf =
        new NewProjectConfigImpl(
            project.getPath(),
            type,
            mixins,
            project.getName(),
            project.getDescription(),
            project.getAttributes(),
            null,
            project.getSource());

    return update(conf);
  }

  @Override
  public RegisteredProject removeType(String wsPath, String type)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException {

    RegisteredProject project =
        get(wsPath).orElseThrow(() -> new NotFoundException("Can't find project"));

    List<String> mixins = project.getMixins();

    if (mixins.contains(type)) {
      mixins.remove(type);

      return update(
          new NewProjectConfigImpl(
              project.getPath(),
              project.getType(),
              mixins,
              project.getName(),
              project.getDescription(),
              project.getAttributes(),
              null,
              project.getSource()));
    }

    if (project.getType().equals(type) && !project.isDetected()) {
      return update(
          new NewProjectConfigImpl(
              project.getPath(),
              BaseProjectType.ID,
              mixins,
              project.getName(),
              project.getDescription(),
              project.getAttributes(),
              null,
              project.getSource()));
    }

    if (project.getType().equals(type) && project.isDetected()) {
      return projectConfigs.removeOrNull(project.getPath());
    }

    return project;
  }

  @Override
  public RegisteredProject move(String srcWsPath, String dstWsPath, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException {

    if (!fsManager.existsAsDirectory(srcWsPath)) {
      throw new NotFoundException("Project directory does not exist or is a file: " + srcWsPath);
    }

    String parent = dstWsPath.substring(0, dstWsPath.lastIndexOf(separator));
    if (!fsManager.existsAsDirectory(parent)) {
      throw new NotFoundException("Destination parent does not exist or is a file: " + parent);
    }

    if (!overwrite && fsManager.existsAsDirectory(dstWsPath)) {
      throw new ConflictException("Destination item exists but overwrite is false: " + dstWsPath);
    }

    fsManager.moveDirectory(srcWsPath, dstWsPath);

    RegisteredProject oldProjectConfig =
        projectConfigs
            .remove(srcWsPath)
            .orElseThrow(() -> new ServerException("Project is not registered"));

    String newProjectName = dstWsPath.substring(dstWsPath.lastIndexOf(separator));
    NewProjectConfig newProjectConfig =
        new NewProjectConfigImpl(
            dstWsPath,
            oldProjectConfig.getType(),
            oldProjectConfig.getMixins(),
            newProjectName,
            oldProjectConfig.getDescription(),
            oldProjectConfig.getAttributes(),
            emptyMap(),
            oldProjectConfig.getSource());

    RegisteredProject movedProject = projectConfigs.put(newProjectConfig, true, false);
    syncer.sync();
    fireInitHandlers(movedProject);
    return movedProject;
  }

  @Override
  public RegisteredProject doImport(
      NewProjectConfig newProjectConfig, boolean rewrite, BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException, BadRequestException {
    return projectImportManager.doImport(newProjectConfig, rewrite, consumer);
  }

  @Override
  public Set<RegisteredProject> doImport(
      Set<? extends NewProjectConfig> newProjectConfigs,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException, BadRequestException {
    return projectImportManager.doImport(newProjectConfigs, rewrite, consumer);
  }

  @Override
  public RegisteredProject doImport(
      String wsPath,
      SourceStorage sourceStorage,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException {
    return projectImportManager.doImport(wsPath, sourceStorage, rewrite, consumer);
  }

  @Override
  public Set<RegisteredProject> doImport(
      Map<String, SourceStorage> projectLocations,
      boolean rewrite,
      BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException {
    return projectImportManager.doImport(projectLocations, rewrite, consumer);
  }

  @Override
  public ProjectTypeResolution qualify(String path, String projectTypeId)
      throws ServerException, NotFoundException {
    return projectQualifier.qualify(path, projectTypeId);
  }

  @Override
  public List<ProjectTypeResolution> qualify(String path)
      throws ServerException, NotFoundException {
    return projectQualifier.qualify(path);
  }

  private void fireInitHandlers(RegisteredProject registeredProject)
      throws ServerException, ForbiddenException, ConflictException, NotFoundException {
    List<String> types = new ArrayList<>(registeredProject.getMixins());
    types.add(registeredProject.getType());

    for (String item : types) {
      Optional<ProjectInitHandler> hOptional = projectHandlers.getProjectInitHandler(item);
      if (hOptional.isPresent()) {
        hOptional.get().onProjectInitialized(registeredProject.getBaseFolder());
      }
    }
  }
}
