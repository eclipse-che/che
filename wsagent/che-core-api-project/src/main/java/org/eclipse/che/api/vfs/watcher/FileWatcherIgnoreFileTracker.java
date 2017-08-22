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
package org.eclipse.che.api.vfs.watcher;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.lines;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.project.shared.Constants.CHE_DIR;
import static org.eclipse.che.api.vfs.watcher.FileWatcherUtils.toInternalPath;
import static org.eclipse.che.api.vfs.watcher.FileWatcherUtils.toNormalPath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Watches the file which contains exclude patterns for managing of tracking creation, modification
 * and deletion events for corresponding entries.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class FileWatcherIgnoreFileTracker {
  private static final Logger LOG = LoggerFactory.getLogger(FileWatcherIgnoreFileTracker.class);
  private static final String FILE_WATCHER_IGNORE_FILE_NAME = "fileWatcherIgnore";
  private static final String FILE_WATCHER_IGNORE_FILE_PATH =
      "/" + CHE_DIR + "/" + FILE_WATCHER_IGNORE_FILE_NAME;
  private static final String EXCLUDES_SUBSCRIBE = "fileWatcher/excludes/subscribe";
  private static final String EXCLUDES_UNSUBSCRIBE = "fileWatcher/excludes/unsubscribe";
  private static final String EXCLUDES_CHANGED = "fileWatcher/excludes/changed";
  private static final String EXCLUDES_CLEAN_UP = "fileWatcher/excludes/cleanup";
  private static final String ADD_TO_EXCLUDES = "fileWatcher/excludes/addToExcludes";
  private static final String REMOVE_FROM_EXCLUDES = "fileWatcher/excludes/removeFromExcludes";

  private final Set<String> endpointIds = newConcurrentHashSet();
  private final Map<Path, Set<Path>> excludes = new ConcurrentHashMap<>();
  private final RequestTransmitter transmitter;
  private final FileWatcherManager fileWatcherManager;
  private final Provider<ProjectManager> projectManagerProvider;
  private final RequestHandlerConfigurator configurator;
  private final Path root;
  private int fileWatchingOperationID;

  @Inject
  public FileWatcherIgnoreFileTracker(
      FileWatcherManager fileWatcherManager,
      RequestTransmitter transmitter,
      RequestHandlerConfigurator configurator,
      Provider<ProjectManager> projectManagerProvider,
      @Named("che.user.workspaces.storage") File root) {
    this.projectManagerProvider = projectManagerProvider;
    this.transmitter = transmitter;
    this.fileWatcherManager = fileWatcherManager;
    this.configurator = configurator;
    this.root = root.toPath().normalize().toAbsolutePath();
  }

  @PostConstruct
  public void initialize() {
    configureHandlers();
    startTrackingIgnoreFile();
    readExcludesFromIgnoreFiles();
    addFileWatcherExcludesMatcher();
  }

  @PreDestroy
  public void stopWatching() {
    fileWatcherManager.unRegisterByMatcher(fileWatchingOperationID);
  }

  private void configureHandlers() {
    configurator
        .newConfiguration()
        .methodName(EXCLUDES_SUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(
            endpointId -> {
              endpointIds.add(endpointId);
              notifyAboutIgnoreFileChanges();
            });

    configurator
        .newConfiguration()
        .methodName(EXCLUDES_UNSUBSCRIBE)
        .noParams()
        .noResult()
        .withConsumer(endpointIds::remove);

    configurator
        .newConfiguration()
        .methodName(ADD_TO_EXCLUDES)
        .paramsAsListOfString()
        .resultAsBoolean()
        .withFunction((endpointId, pathsToExclude) -> addExcludesToIgnoreFile(pathsToExclude));

    configurator
        .newConfiguration()
        .methodName(REMOVE_FROM_EXCLUDES)
        .paramsAsListOfString()
        .resultAsBoolean()
        .withFunction(
            (endpointId, excludesToRemove) -> removeExcludesFromIgnoreFile(excludesToRemove));
  }

  private void readExcludesFromIgnoreFiles() {
    try {
      projectManagerProvider
          .get()
          .getProjects()
          .stream()
          .map(this::getFileWatcherIgnoreFileLocation)
          .forEach(this::fillUpExcludesFromIgnoreFile);
    } catch (ServerException e) {
      LOG.debug("Can not fill up file watcher excludes: " + e.getLocalizedMessage());
    }
  }

  private String getFileWatcherIgnoreFileLocation(RegisteredProject project) {
    FolderEntry baseFolder = project.getBaseFolder();
    return baseFolder == null
        ? ""
        : baseFolder.getPath().toString() + FILE_WATCHER_IGNORE_FILE_PATH;
  }

  private void startTrackingIgnoreFile() {
    fileWatcherManager.addIncludeMatcher(getIgnoreFileMatcher());
    fileWatchingOperationID =
        fileWatcherManager.registerByMatcher(
            getCheDirectoryMatcher(),
            getCreateConsumer(),
            getModifyConsumer(),
            getDeleteConsumer());
  }

  private PathMatcher getCheDirectoryMatcher() {
    return path -> isDirectory(path) && CHE_DIR.equals(path.getFileName().toString());
  }

  private PathMatcher getIgnoreFileMatcher() {
    return path ->
        !isDirectory(path)
            && FILE_WATCHER_IGNORE_FILE_NAME.equals(path.getFileName().toString())
            && CHE_DIR.equals(path.getParent().getFileName().toString());
  }

  private Consumer<String> getCreateConsumer() {
    return getModifyConsumer();
  }

  private Consumer<String> getModifyConsumer() {
    return location -> {
      Path path = toNormalPath(root, location);
      if (getIgnoreFileMatcher().matches(path)) {
        fillUpExcludesFromIgnoreFile(location);
        notifyAboutIgnoreFileChanges();
      }
    };
  }

  private Consumer<String> getDeleteConsumer() {
    return location -> {
      Path path = toNormalPath(root, location);
      if (getIgnoreFileMatcher().matches(path)) {
        Path projectPath = path.getParent().getParent();
        excludes.remove(projectPath);
        notifyAboutIgnoreFileChanges();
      }
    };
  }

  private void addFileWatcherExcludesMatcher() {
    fileWatcherManager.addExcludeMatcher(
        path -> excludes.values().stream().flatMap(Collection::stream).anyMatch(path::startsWith));
  }

  private void fillUpExcludesFromIgnoreFile(String ignoreFileLocation) {
    if (isNullOrEmpty(ignoreFileLocation)) {
      return;
    }

    Path ignoreFilePath = toNormalPath(root, ignoreFileLocation);
    if (!exists(ignoreFilePath)) {
      return;
    }

    Path projectPath = ignoreFilePath.getParent().getParent();
    excludes.remove(projectPath);

    try (Stream<String> lines = lines(ignoreFilePath)) {
      Set<Path> projectExcludes =
          lines
              .filter(line -> !isNullOrEmpty(line.trim()))
              .map(
                  line -> {
                    line = line.trim();
                    return "/".equals(line) ? projectPath : projectPath.resolve(line);
                  })
              .filter(excludePath -> exists(excludePath))
              .collect(toSet());

      if (!projectExcludes.isEmpty()) {
        excludes.put(projectPath, projectExcludes);
      }
    } catch (IOException e) {
      LOG.error(
          format(
              "Can not fill up file watcher excludes from file %s, the reason is: %s",
              ignoreFileLocation, e.getLocalizedMessage()));
    }
  }

  private boolean addExcludesToIgnoreFile(List<String> pathsToExclude) {
    boolean isRemoved =
        pathsToExclude.removeIf(
            location -> {
              Path pathToExclude = toNormalPath(root, location);
              return excludes
                  .values()
                  .stream()
                  .flatMap(Collection::stream)
                  .anyMatch(path -> path.equals(pathToExclude));
            });

    if (pathsToExclude.isEmpty()) {
      return false;
    }

    Map<Path, Set<String>> excludesToWrite = groupExcludes(pathsToExclude);
    excludesToWrite
        .keySet()
        .forEach(
            ignoreFilePath ->
                writeExcludesToIgnoreFile(ignoreFilePath, excludesToWrite.get(ignoreFilePath)));
    return !isRemoved;
  }

  private void writeExcludesToIgnoreFile(Path ignoreFilePath, Set<String> locationsToExclude) {
    try {
      write(ignoreFilePath, locationsToExclude, UTF_8, CREATE, APPEND);
    } catch (IOException e) {
      String errorMessage = "Can not add paths to File Watcher excludes ";

      LOG.error(errorMessage + e.getLocalizedMessage());

      throw new JsonRpcException(500, errorMessage);
    }
  }

  private boolean removeExcludesFromIgnoreFile(List<String> pathsToRemove) {
    Map<Path, Set<String>> excludesToRemove = groupExcludes(pathsToRemove);
    if (excludesToRemove.isEmpty()) {
      return false;
    }

    excludesToRemove
        .keySet()
        .forEach(
            ignoreFilePath ->
                removeExcludesFromIgnoreFile(ignoreFilePath, excludesToRemove.get(ignoreFilePath)));
    return true;
  }

  private void removeExcludesFromIgnoreFile(Path ignoreFilePath, Set<String> pathsToExclude) {
    if (!exists(ignoreFilePath)) {
      throw new JsonRpcException(
          400,
          "Can not remove paths from File Watcher excludes: ignore file is not found by path "
              + ignoreFilePath);
    }

    try (Stream<String> lines = lines(ignoreFilePath)) {
      Set<String> projectExcludes =
          lines
              .filter(
                  line -> {
                    String location = line.trim();
                    return !location.isEmpty() && !pathsToExclude.contains(location);
                  })
              .collect(toSet());

      write(ignoreFilePath, projectExcludes, UTF_8);
    } catch (IOException e) {
      String errorMessage = "Can not remove paths from File Watcher excludes ";

      LOG.error(errorMessage + e.getLocalizedMessage());

      throw new JsonRpcException(500, errorMessage);
    }
  }

  private Map<Path, Set<String>> groupExcludes(List<String> locationsToExclude) {
    Map<Path, Set<String>> groupedExcludes = new HashMap<>();
    try {
      for (String location : locationsToExclude) {
        if (isNullOrEmpty(location)) {
          throw new NotFoundException("The path to exclude should not be empty");
        }

        VirtualFileEntry itemToExclude =
            projectManagerProvider.get().getProjectsRoot().getChild(location);
        if (itemToExclude == null) {
          throw new NotFoundException("The file is not found by path " + location);
        }

        String projectLocation = itemToExclude.getProject();
        if (isNullOrEmpty(projectLocation)) {
          throw new ServerException("The project is not recognized for " + location);
        }

        Path pathToExclude = toNormalPath(root, location);
        Path projectPath = toNormalPath(root, projectLocation);
        Path ignoreFilePath = toNormalPath(root, projectLocation + FILE_WATCHER_IGNORE_FILE_PATH);

        Set<String> excludesToWrite =
            groupedExcludes.computeIfAbsent(ignoreFilePath, k -> new HashSet<>());
        String excludeToWrite =
            pathToExclude.equals(projectPath)
                ? "/"
                : projectPath.relativize(pathToExclude).toString();
        excludesToWrite.add(excludeToWrite);
      }
    } catch (NotFoundException e) {
      String errorMessage = "Can not add path to File Watcher excludes: " + e.getLocalizedMessage();

      LOG.error(errorMessage);

      throw new JsonRpcException(400, errorMessage);

    } catch (ServerException e) {
      String errorMessage = "Can not add path to File Watcher excludes ";

      LOG.error(errorMessage + e.getLocalizedMessage());

      throw new JsonRpcException(500, errorMessage);
    }
    return groupedExcludes;
  }

  private void notifyAboutIgnoreFileChanges() {
    List<String> ignoreFileExcludes =
        excludes
            .keySet()
            .stream()
            .flatMap(
                projectPath ->
                    excludes.get(projectPath).stream().map(path -> toInternalPath(root, path)))
            .collect(toList());
    if (ignoreFileExcludes.isEmpty()) {
      endpointIds.forEach(
          it ->
              transmitter
                  .newRequest()
                  .endpointId(it)
                  .methodName(EXCLUDES_CLEAN_UP)
                  .noParams()
                  .sendAndSkipResult());
      return;
    }

    endpointIds.forEach(
        it ->
            transmitter
                .newRequest()
                .endpointId(it)
                .methodName(EXCLUDES_CHANGED)
                .paramsAsListOfString(ignoreFileExcludes)
                .sendAndSkipResult());
  }
}
