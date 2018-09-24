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
package org.eclipse.che.api.git;

import static java.nio.file.Files.getLastModifiedTime;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.fs.server.WsPathUtils.SEPARATOR;
import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.ADDED;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.MODIFIED;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.NOT_MODIFIED;
import static org.eclipse.che.api.project.server.VcsStatusProvider.VcsStatus.UNTRACKED;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.shared.FileChangedEventDto;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.api.git.shared.StatusChangedEventDto;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VcsStatusProvider;
import org.eclipse.che.api.project.server.impl.RootDirPathProvider;
import org.eclipse.che.api.project.server.notification.ProjectCreatedEvent;
import org.eclipse.che.api.project.server.notification.ProjectDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Git implementation of {@link VcsStatusProvider} based on a {@link Map} which contains statuses of
 * all workspace projects. The map is updated by Git events and by checking the file's modification
 * time to update the map if the files were changed bypassing the file-watchers e.g. the file wasn't
 * opened neither in the project explorer tree, neither in the editor, but was changed in the
 * terminal.
 *
 * @author Igor Vinokur
 */
public class CachedGitStatusProvider implements VcsStatusProvider {

  private static final Logger LOG = LoggerFactory.getLogger(CachedGitStatusProvider.class);

  private final GitConnectionFactory gitConnectionFactory;
  private final PathTransformer pathTransformer;
  private final ProjectManager projectManager;
  private final RootDirPathProvider rootDirPathProvider;
  private final EventService eventService;
  private final Map<String, Status> statusCache;
  private final Map<String, FileTime> projectFiles;

  @Inject
  public CachedGitStatusProvider(
      GitConnectionFactory gitConnectionFactory,
      PathTransformer pathTransformer,
      ProjectManager projectManager,
      RootDirPathProvider rootDirPathProvider,
      EventService eventService) {
    this.gitConnectionFactory = gitConnectionFactory;
    this.pathTransformer = pathTransformer;
    this.projectManager = projectManager;
    this.rootDirPathProvider = rootDirPathProvider;
    this.eventService = eventService;
    this.statusCache = new HashMap<>();
    this.projectFiles = new HashMap<>();
  }

  @PostConstruct
  private void postConstruct() {
    subscribeToEvents();
    collectProjectFiles(rootDirPathProvider.get());
  }

  private void collectProjectFiles(String root) {
    try {
      Set<Path> filePaths =
          Files.walk(Paths.get(root)).filter(Files::isRegularFile).collect(toSet());
      for (Path path : filePaths) {
        String filePath = path.toString();
        projectFiles.put(filePath, getLastModifiedTime(Paths.get(filePath)));
      }
    } catch (IOException exception) {
      LOG.error(exception.getMessage());
    }
  }

  private void subscribeToEvents() {
    eventService.subscribe(
        event -> statusCache.put(event.getProjectName(), event.getStatus()),
        StatusChangedEventDto.class);

    eventService.subscribe(
        event -> {
          String filePath = event.getPath();
          FileChangedEventDto.Status status = event.getStatus();
          Status statusDto = newDto(Status.class);
          if (status == FileChangedEventDto.Status.ADDED) {
            statusDto.setAdded(singletonList(filePath));
          } else if (status == FileChangedEventDto.Status.MODIFIED) {
            statusDto.setModified(singletonList(filePath));
          } else if (status == FileChangedEventDto.Status.UNTRACKED) {
            statusDto.setModified(singletonList(filePath));
          }

          updateCachedStatus(
              Paths.get(filePath).getParent().getFileName().toString(),
              singletonList(filePath),
              statusDto);

          try {
            projectFiles.put(
                filePath, getLastModifiedTime(Paths.get(rootDirPathProvider.get() + filePath)));
          } catch (IOException exception) {
            LOG.error(exception.getMessage());
          }
        },
        FileChangedEventDto.class);

    eventService.subscribe(
        event -> collectProjectFiles(pathTransformer.transform(event.getProjectPath()).toString()),
        ProjectCreatedEvent.class);

    eventService.subscribe(
        event -> {
          String projectFsPath = pathTransformer.transform(event.getProjectPath()).toString();
          projectFiles.keySet().removeIf(file -> file.startsWith(projectFsPath));
          statusCache.remove(
              event.getProjectPath().substring(event.getProjectPath().lastIndexOf('/') + 1));
        },
        ProjectDeletedEvent.class);
  }

  @Override
  public String getVcsName() {
    return GitProjectType.TYPE_ID;
  }

  @Override
  public VcsStatus getStatus(String wsPath) throws ServerException {
    try {
      ProjectConfig project =
          projectManager
              .getClosest(wsPath)
              .orElseThrow(() -> new NotFoundException("Can't find project"));
      wsPath = wsPath.substring(wsPath.startsWith(SEPARATOR) ? 1 : 0);
      String itemPath = wsPath.substring(wsPath.indexOf(SEPARATOR) + 1);

      Status status =
          getStatus(
              project.getName(),
              pathTransformer.transform(project.getPath()).toString(),
              singletonList(itemPath));

      if (status.getUntracked().contains(itemPath)) {
        return UNTRACKED;
      } else if (status.getAdded().contains(itemPath)) {
        return ADDED;
      } else if (status.getModified().contains(itemPath)
          || status.getChanged().contains(itemPath)) {
        return MODIFIED;
      } else {
        return NOT_MODIFIED;
      }
    } catch (GitException | NotFoundException e) {
      throw new ServerException(e.getMessage());
    }
  }

  @Override
  public Map<String, VcsStatus> getStatus(String wsPath, List<String> paths)
      throws ServerException {
    Map<String, VcsStatus> result = new HashMap<>();
    try {
      ProjectConfig project =
          projectManager
              .getClosest(absolutize(wsPath))
              .orElseThrow(() -> new NotFoundException("Can't find project"));

      Status status =
          getStatus(
              project.getName(), pathTransformer.transform(project.getPath()).toString(), paths);

      paths.forEach(
          path -> {
            String itemWsPath = resolve(project.getPath(), path);
            if (status.getUntracked().contains(path)) {
              result.put(itemWsPath, UNTRACKED);
            } else if (status.getAdded().contains(path)) {
              result.put(itemWsPath, ADDED);
            } else if (status.getModified().contains(path) || status.getChanged().contains(path)) {
              result.put(itemWsPath, MODIFIED);
            } else {
              result.put(itemWsPath, NOT_MODIFIED);
            }
          });

    } catch (NotFoundException e) {
      throw new ServerException(e.getMessage());
    }
    return result;
  }

  private Status getStatus(String projectName, String projectFsPath, List<String> paths)
      throws GitException {
    if (statusCache.get(projectName) == null || haveChanges(projectFsPath, paths)) {
      updateCachedStatus(
          projectName, paths, gitConnectionFactory.getConnection(projectFsPath).status(paths));
    }

    return statusCache.get(projectName);
  }

  private boolean haveChanges(String projectPath, List<String> paths) {
    boolean statusChanged = false;

    for (String path : paths) {
      String filePath = resolve(projectPath, path);
      FileTime fileTime = projectFiles.get(filePath);
      try {
        FileTime currentFileTime = getLastModifiedTime(Paths.get(filePath));
        if (fileTime == null || !fileTime.equals(currentFileTime)) {
          projectFiles.put(filePath, currentFileTime);
          statusChanged = true;
        }
      } catch (IOException exception) {
        if (exception instanceof NoSuchFileException) {
          statusChanged = projectFiles.remove(filePath) != null;
        } else {
          LOG.error(exception.getMessage());
        }
      }
    }

    return statusChanged;
  }

  private void updateCachedStatus(String project, List<String> paths, Status changes) {
    Status cachedStatus = statusCache.get(project);

    if (cachedStatus == null) {
      statusCache.put(project, changes);
      return;
    }

    List<String> added =
        cachedStatus.getAdded().stream().filter(path -> !paths.contains(path)).collect(toList());
    added.addAll(changes.getAdded());

    List<String> changed =
        cachedStatus.getChanged().stream().filter(path -> !paths.contains(path)).collect(toList());
    changed.addAll(changes.getChanged());

    List<String> modified =
        cachedStatus.getModified().stream().filter(path -> !paths.contains(path)).collect(toList());
    modified.addAll(changes.getModified());

    List<String> untracked =
        cachedStatus.getModified().stream().filter(path -> !paths.contains(path)).collect(toList());
    untracked.addAll(changes.getUntracked());

    List<String> missing =
        cachedStatus.getMissing().stream().filter(path -> !paths.contains(path)).collect(toList());
    missing.addAll(changes.getMissing());

    List<String> removed =
        cachedStatus.getRemoved().stream().filter(path -> !paths.contains(path)).collect(toList());
    removed.addAll(changes.getRemoved());

    List<String> conflicting =
        cachedStatus
            .getConflicting()
            .stream()
            .filter(path -> !paths.contains(path))
            .collect(toList());
    conflicting.addAll(changes.getConflicting());

    List<String> untrackedFolders =
        cachedStatus
            .getUntrackedFolders()
            .stream()
            .filter(path -> !paths.contains(path))
            .collect(toList());
    untrackedFolders.addAll(changes.getUntrackedFolders());

    Status status = newDto(Status.class);
    status.setAdded(added);
    status.setChanged(changed);
    status.setModified(modified);
    status.setUntracked(untracked);
    status.setMissing(missing);
    status.setRemoved(removed);
    status.setConflicting(conflicting);
    status.setUntrackedFolders(untrackedFolders);

    statusCache.put(project, status);
  }
}
