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
package org.eclipse.che.api.project.server.impl;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.fs.server.WsPathUtils.isRoot;

import com.google.common.collect.ImmutableSet;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.WsPathUtils;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.api.project.shared.dto.RegisteredProjectDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PersistingProjectRegistry implements ProjectConfigRegistry {

  private static final Logger LOG = LoggerFactory.getLogger(PersistingProjectRegistry.class);

  private final Map<String, RegisteredProject> projects = new ConcurrentHashMap<>();

  private final RegisteredProjectFactory registeredProjectFactory;
  private final FsManager fsManager;
  private final FileCache cache;

  @Inject
  public PersistingProjectRegistry(
      RegisteredProjectFactory registeredProjectFactory, FsManager fsManager) {
    this.registeredProjectFactory = registeredProjectFactory;
    this.fsManager = fsManager;
    this.cache = new FileCache();
  }

  @Override
  public Set<RegisteredProject> getAll() {

    return ImmutableSet.copyOf(cache.getAll(""));
  }

  @Override
  public Set<RegisteredProject> getAll(String wsPath) {

    return ImmutableSet.copyOf(cache.getAll(wsPath));
  }

  @Override
  public Optional<RegisteredProject> get(String wsPath) {

    return Optional.ofNullable(getOrNull(wsPath));
  }

  @Override
  public RegisteredProject getOrNull(String wsPath) {
    return projects.containsKey(wsPath) ? projects.get(wsPath) : cache.get(wsPath);
  }

  @Override
  public synchronized RegisteredProject put(
      ProjectConfig config, boolean updated, boolean detected) {
    String wsPath = config.getPath();
    RegisteredProject project = registeredProjectFactory.create(wsPath, config, updated, detected);
    projects.put(wsPath, project);

    cache.put(project);

    return project;
  }

  @Override
  public synchronized RegisteredProject putIfAbsent(
      String wsPath, boolean updated, boolean detected) {
    RegisteredProject registeredProject = projects.get(wsPath);
    if (registeredProject != null) {
      return registeredProject;
    }

    RegisteredProject project = registeredProjectFactory.create(wsPath, null, updated, detected);
    projects.put(wsPath, project);

    cache.put(project);

    return project;
  }

  @Override
  public Optional<RegisteredProject> remove(String wsPath) {

    cache.remove(wsPath);
    return Optional.ofNullable(projects.remove(wsPath));
  }

  @Override
  public RegisteredProject removeOrNull(String wsPath) {

    cache.remove(wsPath);
    return projects.remove(wsPath);
  }

  @Override
  public boolean isRegistered(String path) {
    return projects.containsKey(path) || cache.contains(path);
  }

  @Override
  public Optional<RegisteredProject> getClosest(String wsPath) {
    while (!isRoot(wsPath)) {

      if (projects.containsKey(wsPath)) {
        // get from local map
        return Optional.of(projects.get(wsPath));
      } else if (cache.contains(wsPath)) {
        // get from fs cache
        return Optional.of(cache.get(wsPath));
      }
      wsPath = WsPathUtils.parentOf(wsPath);
    }

    return empty();
  }

  private class FileCache {

    private static final String ROOT = "/.che/tmp/projectConfig";

    public void put(RegisteredProject project) {

      String json =
          DtoFactory.getInstance().toJson(ProjectDtoConverter.asRegisteredProjectDto(project));
      try {

        fsManager.createFile(filePath(project.getPath()), json, true, true);

      } catch (Exception e) {
        LOG.error(e.getMessage());
      }
    }

    public RegisteredProject get(String wsPath) {
      try {

        String json = fsManager.readAsString(filePath(wsPath));
        return DtoFactory.getInstance().createDtoFromJson(json, RegisteredProjectDto.class);

      } catch (Exception e) {
        LOG.error(e.getMessage());
        return null;
      }
    }

    public RegisteredProject remove(String wsPath) {
      RegisteredProject p = null;
      try {

        p = get(wsPath);
        fsManager.delete(filePath(wsPath), true);

      } catch (Exception e) {
        LOG.error(e.getMessage());
      }
      return p;
    }

    public Set<RegisteredProject> getAll(String wsPath) {

      Set<RegisteredProject> projects = new HashSet<>();
      String fsPath = fsManager.toIoFile(ROOT + wsPath).getPath();
      // Check if there are child projects (if so there is a folder)
      // this check is needed since otherwise Files.walk throws excepton
      if (Files.isDirectory(Paths.get(fsPath))) {
        try {
          Set<Path> paths =
              Files.walk(Paths.get(fsPath)).filter(Files::isRegularFile).collect(toSet());

          for (Path p : paths) {
            String json = new String(Files.readAllBytes(p));

            projects.add(
                DtoFactory.getInstance().createDtoFromJson(json, RegisteredProjectDto.class));
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      return projects;
    }

    public boolean contains(String wsPath) {
      return fsManager.exists(filePath(wsPath));
    }

    private String filePath(String wsPath) {
      return ROOT + wsPath + ".json";
    }
  }
}
