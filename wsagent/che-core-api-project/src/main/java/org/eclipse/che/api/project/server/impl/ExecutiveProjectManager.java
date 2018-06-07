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
import static org.eclipse.che.api.fs.server.WsPathUtils.ROOT;
import static org.eclipse.che.api.fs.server.WsPathUtils.nameOf;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;
import org.eclipse.che.api.project.server.notification.PreProjectDeletedEvent;
import org.eclipse.che.api.project.server.notification.ProjectCreatedEvent;
import org.eclipse.che.api.project.server.notification.ProjectDeletedEvent;
import org.eclipse.che.api.project.server.notification.ProjectInitializedEvent;
import org.eclipse.che.api.project.server.notification.ProjectUpdatedEvent;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.project.server.type.BaseProjectType;
import org.eclipse.che.api.project.server.type.ProjectQualifier;
import org.eclipse.che.api.project.server.type.ProjectTypeResolution;
import org.eclipse.che.api.project.shared.NewProjectConfig;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.api.search.server.excludes.HiddenItemPathMatcher;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs project related operations after project registry is synchronized and method parameters
 * are validated.
 */
@Singleton
public class ExecutiveProjectManager implements ProjectManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutiveProjectManager.class);

  private final FsManager fsManager;
  private final ProjectQualifier projectQualifier;
  private final ProjectConfigRegistry projectConfigRegistry;
  private final ProjectHandlerRegistry projectHandlerRegistry;
  private final HiddenItemPathMatcher hiddenItemPathMatcher;
  private final FileWatcherManager fileWatcherManager;
  private final EventService eventService;

  private RemoteProjects remoteProjects;

  @Inject
  public ExecutiveProjectManager(
      FsManager fsManager,
      RemoteProjects remoteProjects,
      ProjectHandlerRegistry projectHandlerRegistry,
      ProjectQualifier projectQualifier,
      EventService eventService,
      RegisteredProjectFactory registeredProjectFactory,
      HiddenItemPathMatcher hiddenItemPathMatcher,
      FileWatcherManager fileWatcherManager) {
    System.out.println("constructing epr secpmd");
    this.fsManager = fsManager;
    this.remoteProjects = remoteProjects;
    this.projectConfigRegistry = new InmemoryProjectRegistry(registeredProjectFactory);
    this.hiddenItemPathMatcher = hiddenItemPathMatcher;
    this.fileWatcherManager = fileWatcherManager;
    this.projectHandlerRegistry = projectHandlerRegistry;
    this.projectQualifier = projectQualifier;
    this.eventService = eventService;
  }

  void initialize()
      throws ServerException, ForbiddenException, ConflictException, NotFoundException {

    LOGGER.info("initializing project manager");
    for (ProjectConfig projectConfig : remoteProjects.getAll()) {
      projectConfigRegistry.put(projectConfig, false, false);
    }
    for (String wsPath : fsManager.getDirWsPaths(ROOT)) {
      if (!hiddenItemPathMatcher.matches(Paths.get(wsPath)) && !isRegistered(wsPath)) {
        RegisteredProject project = projectConfigRegistry.putIfAbsent(wsPath, true, true);
        fireInitHandlers(project);
        eventService.publish(new ProjectInitializedEvent(project.getBaseFolder()));
      }
    }
  }

  void ensureExists(String wsPath) {
    RegisteredProject existing = getOrNull(wsPath);
    if (existing == null) {
      projectConfigRegistry.putIfAbsent(wsPath, true, true);
      eventService.publish(new ProjectCreatedEvent(wsPath));
    } else {
      projectConfigRegistry.put(existing, true, true);
      eventService.publish(new ProjectUpdatedEvent(wsPath));
    }
  }

  void ensureRemoved(String wsPath) {
    projectConfigRegistry
        .get(wsPath)
        .ifPresent(
            project -> {
              eventService.publish(new PreProjectDeletedEvent(wsPath));
              projectConfigRegistry.remove(wsPath);
              eventService.publish(new ProjectDeletedEvent(wsPath));
            });
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
    return projectConfigRegistry.getClosest(wsPath);
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
    eventService.publish(new ProjectCreatedEvent(project.getPath()));

    return project;
  }

  @Override
  public synchronized RegisteredProject update(ProjectConfig projectConfig)
      throws ForbiddenException, ServerException, NotFoundException, ConflictException {
    boolean existed = projectConfigRegistry.get(projectConfig.getPath()).isPresent();
    RegisteredProject project = projectConfigRegistry.put(projectConfig, true, false);
    fireInitHandlers(project);
    if (existed) {
      eventService.publish(new ProjectUpdatedEvent(projectConfig.getPath()));
    } else {
      eventService.publish(new ProjectCreatedEvent(projectConfig.getPath()));
    }

    return project;
  }

  @Override
  public synchronized Optional<RegisteredProject> delete(String wsPath)
      throws ServerException, ForbiddenException, NotFoundException, ConflictException {
    fsManager.delete(wsPath);

    projectConfigRegistry
        .getAll(wsPath)
        .stream()
        .map(ProjectConfig::getPath)
        .map(
            path -> {
              eventService.publish(new PreProjectDeletedEvent(path));
              return path;
            })
        .map(projectConfigRegistry::remove)
        .filter(Optional::isPresent)
        .forEach(
            (config) -> {
              eventService.publish(new ProjectDeletedEvent(config.get().getPath()));
            });
    eventService.publish(new PreProjectDeletedEvent(wsPath));
    Optional<RegisteredProject> deleted = projectConfigRegistry.remove(wsPath);
    eventService.publish(new ProjectDeletedEvent(wsPath));
    return deleted;
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

    ProjectConfig oldProjectConfig =
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
    eventService.publish(new ProjectCreatedEvent(dstWsPath));
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
      RegisteredProject newProject = projectConfigRegistry.put(newProjectConfig, true, true);
      eventService.publish(new ProjectCreatedEvent(wsPath));
      return newProject;
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
    eventService.publish(new ProjectUpdatedEvent(wsPath));
    return projectConfigRegistry.put(newProjectConfig, true, registeredProject.isDetected());
  }

  @Override
  public synchronized RegisteredProject removeType(String wsPath, String type)
      throws ConflictException, NotFoundException, ServerException, BadRequestException,
          ForbiddenException {

    RegisteredProject project =
        projectConfigRegistry
            .get(wsPath)
            .orElseThrow(() -> new NotFoundException("Can't find project"));

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
      eventService.publish(new ProjectUpdatedEvent(wsPath));
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
      eventService.publish(new ProjectUpdatedEvent(wsPath));

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

    eventService.publish(new PreProjectDeletedEvent(srcWsPath));
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
    eventService.publish(new ProjectCreatedEvent(dstWsPath));
    eventService.publish(new ProjectDeletedEvent(srcWsPath));

    return movedProject;
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

  Optional<RegisteredProject> unregister(String wsPath) {
    return projectConfigRegistry.remove(wsPath);
  }
}
