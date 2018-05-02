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
package org.eclipse.che.api.project.server.impl;

import static java.io.File.separator;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static org.eclipse.che.api.fs.server.WsPathUtils.isRoot;
import static org.eclipse.che.api.fs.server.WsPathUtils.nameOf;

import java.util.ArrayList;
import java.util.Collections;
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
import org.eclipse.che.api.fs.server.WsPathUtils;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.ProjectQualifier;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.NewProjectConfig;

/**
 * Performs project related operations after project registry is synchronized and method parameters
 * are validated.
 */
@Singleton
public class ExecutiveProjectManager implements ProjectManager {

  private final FsManager fsManager;
  private final ProjectQualifier projectQualifier;
  private final ProjectConfigRegistry projectConfigRegistry;
  private final ProjectHandlerRegistry projectHandlerRegistry;
  private final ProjectImportManager projectImportManager;

  @Inject
  public ExecutiveProjectManager(
      FsManager fsManager,
      ProjectConfigRegistry projectConfigRegistry,
      ProjectHandlerRegistry projectHandlerRegistry,
      ProjectQualifier projectQualifier,
      ProjectImportManager projectImportManager) {
    this.fsManager = fsManager;
    this.projectConfigRegistry = projectConfigRegistry;
    this.projectHandlerRegistry = projectHandlerRegistry;
    this.projectQualifier = projectQualifier;
    this.projectImportManager = projectImportManager;
  }

  @Override
  public boolean isRegistered(String wsPath) {
    return projectConfigRegistry.isRegistered(wsPath);
  }

  @Override
  public synchronized Optional<RegisteredProject> get(String wsPath) {
    return projectConfigRegistry.get(wsPath);
  }

  public RegisteredProject getOrNull(String wsPath) {
    return projectConfigRegistry.getOrNull(wsPath);
  }

  @Override
  public Optional<RegisteredProject> getClosest(String wsPath) {
    while (!isRoot(wsPath)) {
      Optional<RegisteredProject> registeredProject = projectConfigRegistry.get(wsPath);
      if (registeredProject.isPresent()) {
        return registeredProject;
      } else {
        wsPath = WsPathUtils.parentOf(wsPath);
      }
    }

    return empty();
  }

  @Override
  public RegisteredProject getClosestOrNull(String wsPath) {
    return getClosest(wsPath).orElse(null);
  }

  @Override
  public Set<RegisteredProject> getAll() {
    return projectConfigRegistry.getAll();
  }

  @Override
  public Set<RegisteredProject> getAll(String wsPath) {
    return projectConfigRegistry.getAll(wsPath);
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

    return Collections.unmodifiableSet(projects);
  }

  @Override
  public RegisteredProject create(ProjectConfig projectConfig, Map<String, String> options)
      throws ConflictException, ForbiddenException, ServerException, NotFoundException,
          BadRequestException {
    String wsPath = projectConfig.getPath();
    String type = projectConfig.getType();
    Optional<CreateProjectHandler> cphOptional = projectHandlerRegistry.getCreateHandler(type);

    if (cphOptional.isPresent()) {
      CreateProjectHandler generator = cphOptional.get();
      Map<String, AttributeValue> valueMap = new HashMap<>();
      Map<String, List<String>> attributes = projectConfig.getAttributes();
      if (attributes != null) {
        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
          valueMap.put(entry.getKey(), new AttributeValue(entry.getValue()));
        }
      }

      generator.onCreateProject(wsPath, valueMap, options == null ? new HashMap<>() : options);
    } else {
      fsManager.createDir(wsPath);
    }

    RegisteredProject project = projectConfigRegistry.put(projectConfig, true, false);
    fireInitHandlers(project);

    return project;
  }

  @Override
  public synchronized Set<RegisteredProject> updateAll(Set<ProjectConfig> projectConfigs)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
          BadRequestException {
    Set<RegisteredProject> projects = new HashSet<>();

    for (ProjectConfig projectConfig : projectConfigs) {
      RegisteredProject registeredProject = update(projectConfig);
      projects.add(registeredProject);
    }

    return Collections.unmodifiableSet(projects);
  }

  @Override
  public synchronized RegisteredProject update(ProjectConfig projectConfig)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException,
          BadRequestException {
    RegisteredProject project = projectConfigRegistry.put(projectConfig, true, false);
    fireInitHandlers(project);

    return project;
  }

  @Override
  public synchronized Set<RegisteredProject> deleteAll(Set<String> wsPaths)
      throws ServerException, ForbiddenException, NotFoundException, ConflictException {
    Set<RegisteredProject> projects = new HashSet<>();

    for (String wsPath : wsPaths) {
      delete(wsPath).ifPresent(projects::add);
    }

    return Collections.unmodifiableSet(projects);
  }

  @Override
  public synchronized Optional<RegisteredProject> delete(String wsPath)
      throws ServerException, ForbiddenException, NotFoundException, ConflictException {
    fsManager.delete(wsPath);

    projectConfigRegistry
        .getAll(wsPath)
        .stream()
        .map(RegisteredProject::getPath)
        .forEach(projectConfigRegistry::remove);

    return projectConfigRegistry.remove(wsPath);
  }

  @Override
  public synchronized Set<RegisteredProject> deleteAll()
      throws ServerException, ForbiddenException, ConflictException {
    Set<RegisteredProject> deleted = new HashSet<>();
    for (RegisteredProject registeredProject : projectConfigRegistry.getAll()) {
      String path = registeredProject.getPath();
      try {
        delete(path).ifPresent(deleted::add);
      } catch (NotFoundException e) {
        throw new ServerException(e);
      }
    }
    return Collections.unmodifiableSet(deleted);
  }

  @Override
  public synchronized RegisteredProject copy(String srcWsPath, String dstWsPath, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException {
    fsManager.copy(srcWsPath, dstWsPath);

    RegisteredProject oldProjectConfig =
        projectConfigRegistry.get(srcWsPath).orElseThrow(IllegalStateException::new);

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

    RegisteredProject copiedProject = projectConfigRegistry.put(newProjectConfig, true, false);
    fireInitHandlers(copiedProject);
    return copiedProject;
  }

  @Override
  public synchronized RegisteredProject setType(String wsPath, String type, boolean asMixin)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException {

    RegisteredProject registeredProject = projectConfigRegistry.getOrNull(wsPath);

    if (registeredProject == null) {
      NewProjectConfig newProjectConfig =
          new NewProjectConfigImpl(
              wsPath, type, new ArrayList<>(), nameOf(wsPath), nameOf(wsPath), null, null, null);
      return projectConfigRegistry.put(newProjectConfig, true, true);
    }

    List<String> newMixins = registeredProject.getMixins();
    String newType = registeredProject.getType();
    if (asMixin) {
      if (!newMixins.contains(type)) {
        newMixins.add(type);
      }
    } else {
      newType = type;
    }

    NewProjectConfig newProjectConfig =
        new NewProjectConfigImpl(
            registeredProject.getPath(),
            newType,
            newMixins,
            registeredProject.getName(),
            registeredProject.getDescription(),
            registeredProject.getAttributes(),
            null,
            registeredProject.getSource());

    return projectConfigRegistry.put(newProjectConfig, true, registeredProject.isDetected());
  }

  @Override
  public synchronized RegisteredProject removeType(String wsPath, String type)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException {

    RegisteredProject project =
        get(wsPath).orElseThrow(() -> new NotFoundException("Can't find project"));

    List<String> mixins = project.getMixins();

    if (mixins.contains(type)) {
      mixins.remove(type);

      NewProjectConfigImpl projectConfig =
          new NewProjectConfigImpl(
              project.getPath(),
              project.getType(),
              mixins,
              project.getName(),
              project.getDescription(),
              project.getAttributes(),
              null,
              project.getSource());

      projectConfigRegistry.put(projectConfig, true, false);

      return projectConfigRegistry.getOrNull(wsPath);
    }

    if (project.getType().equals(type) && !project.isDetected()) {

      NewProjectConfigImpl projectConfig =
          new NewProjectConfigImpl(
              project.getPath(),
              BaseProjectType.ID,
              project.getMixins(),
              project.getName(),
              project.getDescription(),
              project.getAttributes(),
              null,
              project.getSource());

      projectConfigRegistry.put(projectConfig, true, false);

      return projectConfigRegistry.getOrNull(wsPath);
    }

    if (project.getType().equals(type) && project.isDetected()) {
      return projectConfigRegistry.removeOrNull(project.getPath());
    }

    return project;
  }

  @Override
  public synchronized RegisteredProject move(String srcWsPath, String dstWsPath, boolean overwrite)
      throws ServerException, NotFoundException, ConflictException, ForbiddenException {
    RegisteredProject oldProjectConfig =
        projectConfigRegistry.remove(srcWsPath).orElseThrow(IllegalStateException::new);

    fsManager.move(srcWsPath, dstWsPath);

    String dstName = nameOf(dstWsPath);
    NewProjectConfig newProjectConfig =
        new NewProjectConfigImpl(
            dstWsPath,
            oldProjectConfig.getType(),
            oldProjectConfig.getMixins(),
            dstName,
            oldProjectConfig.getDescription(),
            oldProjectConfig.getAttributes(),
            emptyMap(),
            oldProjectConfig.getSource());

    RegisteredProject movedProject = projectConfigRegistry.put(newProjectConfig, true, false);
    fireInitHandlers(movedProject);
    return movedProject;
  }

  @Override
  public RegisteredProject doImport(
      NewProjectConfig projectConfig, boolean rewrite, BiConsumer<String, String> consumer)
      throws ServerException, ForbiddenException, UnauthorizedException, ConflictException,
          NotFoundException, BadRequestException {
    return projectImportManager.doImport(projectConfig, rewrite, consumer);
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
  public ProjectTypeResolution verify(String wsPath, String projectTypeId)
      throws ServerException, NotFoundException {
    return projectQualifier.qualify(wsPath, projectTypeId);
  }

  @Override
  public List<ProjectTypeResolution> recognize(String wsPath)
      throws ServerException, NotFoundException {
    return projectQualifier.qualify(wsPath);
  }

  private void fireInitHandlers(RegisteredProject registeredProject)
      throws ServerException, ForbiddenException, ConflictException, NotFoundException {
    List<String> types = new ArrayList<>(registeredProject.getMixins());
    types.add(registeredProject.getType());

    for (String item : types) {
      Optional<ProjectInitHandler> hOptional = projectHandlerRegistry.getProjectInitHandler(item);
      if (hOptional.isPresent()) {
        hOptional.get().onProjectInitialized(registeredProject.getBaseFolder());
      }
    }
  }
}
