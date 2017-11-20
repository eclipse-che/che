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

import static org.eclipse.che.api.watcher.server.impl.FileWatcherUtils.toNormalPath;

import com.google.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.function.Consumer;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.watcher.server.FileWatcherManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SimpleFileWatcherManager implements FileWatcherManager {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleFileWatcherManager.class);

  private final FileWatcherByPathValue fileWatcherByPathValue;
  private final FileWatcherByPathMatcher fileWatcherByPathMatcher;
  private final Path root;
  private final FileWatcherExcludePatternsRegistry excludePatternsRegistry;

  @Inject
  public SimpleFileWatcherManager(
      @Named("che.user.workspaces.storage") File root,
      FileWatcherByPathValue watcherByPathValue,
      FileWatcherByPathMatcher watcherByPathMatcher,
      FileWatcherExcludePatternsRegistry excludePatternsRegistry) {
    this.fileWatcherByPathMatcher = watcherByPathMatcher;
    this.fileWatcherByPathValue = watcherByPathValue;
    this.root = root.toPath().normalize().toAbsolutePath();
    this.excludePatternsRegistry = excludePatternsRegistry;
  }

  @Override
  public int registerByPath(
      String path, Consumer<String> create, Consumer<String> modify, Consumer<String> delete) {
    LOG.debug("Registering operations to an item with path '{}'", path);

    return fileWatcherByPathValue.watch(toNormalPath(root, path), create, modify, delete);
  }

  @Override
  public void unRegisterByPath(int id) {
    LOG.debug(
        "Canceling registering of an operation with id '{}' registered to an item with path", id);

    fileWatcherByPathValue.unwatch(id);
  }

  @Override
  public int registerByMatcher(
      PathMatcher matcher,
      Consumer<String> create,
      Consumer<String> modify,
      Consumer<String> delete) {
    LOG.debug("Registering operations to an item with matcher '{}'", matcher);

    return fileWatcherByPathMatcher.watch(matcher, create, modify, delete);
  }

  @Override
  public void unRegisterByMatcher(int id) {
    LOG.debug("Canceling registering of an operation with id '{}' registered to path matcher", id);

    fileWatcherByPathMatcher.unwatch(id);
  }

  @Override
  public void addExcludeMatcher(PathMatcher exclude) {
    excludePatternsRegistry.addExcludeMatcher(exclude);
  }

  @Override
  public void removeExcludeMatcher(PathMatcher exclude) {
    excludePatternsRegistry.removeExcludeMatcher(exclude);
  }

  @Override
  public void addIncludeMatcher(PathMatcher matcher) {
    excludePatternsRegistry.addIncludeMatcher(matcher);
  }

  @Override
  public void removeIncludeMatcher(PathMatcher matcher) {
    excludePatternsRegistry.removeIncludeMatcher(matcher);
  }

  @Override
  public boolean isExcluded(Path path) {
    return excludePatternsRegistry.isExcluded(path);
  }
}
