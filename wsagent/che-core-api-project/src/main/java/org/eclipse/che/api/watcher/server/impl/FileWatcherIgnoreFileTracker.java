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
package org.eclipse.che.api.watcher.server.impl;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.lines;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;
import static org.eclipse.che.api.project.shared.Constants.CHE_DIR;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
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
      CHE_DIR + "/" + FILE_WATCHER_IGNORE_FILE_NAME;
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
  private final ProjectManager projectManager;
  private final PathTransformer pathTransformer;
  private final RequestHandlerConfigurator configurator;

  @Inject
  public FileWatcherIgnoreFileTracker(
      FileWatcherManager fileWatcherManager,
      RequestTransmitter transmitter,
      ProjectManager projectManager,
      PathTransformer pathTransformer,
      RequestHandlerConfigurator configurator) {
    this.projectManager = projectManager;
    this.pathTransformer = pathTransformer;
    this.transmitter = transmitter;
    this.fileWatcherManager = fileWatcherManager;
    this.configurator = configurator;
  }

  @PostConstruct
  public void initialize() {
    configureHandlers();
    readExcludesFromIgnoreFiles();
    addFileWatcherExcludesMatcher();
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
        .withFunction((endpointId, pathsToExclude) -> addExcludes(pathsToExclude));

    configurator
        .newConfiguration()
        .methodName(REMOVE_FROM_EXCLUDES)
        .paramsAsListOfString()
        .resultAsBoolean()
        .withFunction((endpointId, excludesToRemove) -> removeExcludes(excludesToRemove));
  }

  private void readExcludesFromIgnoreFiles() {
    projectManager
        .getAll()
        .forEach(
            project -> {
              String wsPath = absolutize(project.getPath());
              if (wsPath != null) {
                String ignoreFileLocation = resolve(wsPath, FILE_WATCHER_IGNORE_FILE_PATH);
                fillUpExcludesFromIgnoreFile(ignoreFileLocation);
              }
            });
  }

  private void addFileWatcherExcludesMatcher() {
    fileWatcherManager.addExcludeMatcher(
        path -> excludes.values().stream().flatMap(Collection::stream).anyMatch(path::startsWith));
  }

  private void fillUpExcludesFromIgnoreFile(String ignoreFileLocation) {
    if (isNullOrEmpty(ignoreFileLocation)) {
      return;
    }

    Path ignoreFilePath = pathTransformer.transform(ignoreFileLocation);
    if (!exists(ignoreFilePath)) {
      return;
    }

    Path projectPath = ignoreFilePath.getParent().getParent();
    excludes.remove(projectPath);

    try (Stream<String> lines = lines(ignoreFilePath)) {
      Set<Path> projectExcludes =
          lines
              .map(String::trim)
              .filter(line -> !line.isEmpty())
              .map(line -> "/".equals(line) ? projectPath : projectPath.resolve(line))
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

  private boolean addExcludes(List<String> pathsToExclude) {
    Map<Path, Set<Path>> groupedExcludes = groupExcludes(pathsToExclude);
    if (pathsToExclude.isEmpty()) {
      return false;
    }

    groupedExcludes
        .keySet()
        .forEach(
            projectPath -> {
              excludes.putIfAbsent(projectPath, new HashSet<>());

              Set<Path> projectExcludes = excludes.get(projectPath);
              Set<Path> excludesForAdding = groupedExcludes.get(projectPath);

              excludesForAdding.removeIf(projectExcludes::contains);
              projectExcludes.addAll(excludesForAdding);

              writeExcludesToIgnoreFile(projectPath, excludesForAdding);
            });

    notifyAboutIgnoreFileChanges();
    return true;
  }

  private boolean removeExcludes(List<String> pathsToRemove) {
    Map<Path, Set<Path>> groupedExcludes = groupExcludes(pathsToRemove);
    if (groupedExcludes.isEmpty()) {
      return false;
    }

    groupedExcludes
        .keySet()
        .forEach(
            projectPath -> {
              excludes.putIfAbsent(projectPath, new HashSet<>());

              Set<Path> excludesToRemove = groupedExcludes.get(projectPath);
              Set<Path> projectExcludes = excludes.get(projectPath);

              projectExcludes.removeIf(excludesToRemove::contains);

              removeExcludes(projectPath, excludesToRemove);
            });

    notifyAboutIgnoreFileChanges();
    return true;
  }

  private Set<String> prepareToWrite(Path projectPath, Set<Path> pathsToExclude) {
    return pathsToExclude
        .stream()
        .map(
            pathToExclude ->
                pathToExclude.equals(projectPath)
                    ? "/"
                    : projectPath.relativize(pathToExclude).toString())
        .collect(toSet());
  }

  private void writeExcludesToIgnoreFile(Path projectPath, Set<Path> pathsToExclude) {
    Path ignoreFilePath = projectPath.resolve(FILE_WATCHER_IGNORE_FILE_PATH);
    try {
      Path cheDir = ignoreFilePath.getParent();
      if (!exists(cheDir)) {
        createDirectories(cheDir);
      }

      Set<String> excludesToWrite = prepareToWrite(projectPath, pathsToExclude);
      write(ignoreFilePath, excludesToWrite, UTF_8, CREATE, APPEND);
    } catch (IOException e) {
      String errorMessage = "Can not add paths to File Watcher excludes ";

      LOG.error(errorMessage + e.getLocalizedMessage());

      throw new JsonRpcException(500, errorMessage);
    }
  }

  private void removeExcludes(Path projectPath, Set<Path> pathsToExclude) {
    Path ignoreFilePath = projectPath.resolve(FILE_WATCHER_IGNORE_FILE_PATH);
    if (!exists(ignoreFilePath)) {
      return;
    }

    Set<String> excludesToRemove = prepareToWrite(projectPath, pathsToExclude);
    try (Stream<String> lines = lines(ignoreFilePath)) {
      Set<String> excludesToWrite =
          lines
              .map(String::trim)
              .filter(line -> !line.isEmpty())
              .filter(line -> !excludesToRemove.contains(line))
              .collect(toSet());

      write(ignoreFilePath, excludesToWrite, UTF_8);
    } catch (IOException e) {
      String errorMessage = "Can not remove paths from File Watcher excludes ";

      LOG.error(errorMessage + e.getLocalizedMessage());

      throw new JsonRpcException(500, errorMessage);
    }
  }

  private Map<Path, Set<Path>> groupExcludes(List<String> locationsToExclude) {
    Map<Path, Set<Path>> groupedExcludes = new HashMap<>();
    try {
      for (String location : locationsToExclude) {
        if (isNullOrEmpty(location)) {
          throw new NotFoundException("The path to exclude should not be empty");
        }

        Path fsPath = pathTransformer.transform(location);
        if (!fsPath.toFile().exists()) {
          throw new NotFoundException("The file is not found by path " + location);
        }

        String projectWsPath =
            projectManager
                .getClosest(location)
                .orElseThrow(
                    () -> new ServerException("The project is not recognized for " + location))
                .getPath();

        Path pathToExclude = pathTransformer.transform(location);
        Path projectPath = pathTransformer.transform(projectWsPath);

        groupedExcludes.putIfAbsent(projectPath, new HashSet<>());
        groupedExcludes.get(projectPath).add(pathToExclude);
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
                projectPath -> excludes.get(projectPath).stream().map(pathTransformer::transform))
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
