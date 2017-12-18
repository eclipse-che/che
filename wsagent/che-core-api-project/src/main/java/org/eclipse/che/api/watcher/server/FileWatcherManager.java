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
package org.eclipse.che.api.watcher.server;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.function.Consumer;

/** Facade for all dynamic file watcher system related operations. */
public interface FileWatcherManager {

  /**
   * Start watching a file system item by specifying its path. If path points to a file than only
   * file related events are taken into account, if path points to a folder than all folder entries
   * related events are taken into account. Path is expected to be in absolute form in internal
   * virtual file system format.
   *
   * <p>To react on events related to an aforementioned item you can specify {@link Consumer} for
   * create, modify and delete event correspondingly. It is possible to omit one ore more event
   * consumers if it is needed by using empty consumer stub.
   *
   * <p>On successful start you receive a registration identifier to distinguish your specific
   * consumer set as there can be registered arbitrary number of consumers to a single path.
   *
   * @param wsPath absolute workspace path
   * @param create consumer for create event
   * @param modify consumer for modify event
   * @param delete consumer for delete event
   * @return operation set identifier
   */
  int registerByPath(
      String wsPath, Consumer<String> create, Consumer<String> modify, Consumer<String> delete);

  /**
   * Stops watching a file system item. More accurately it cancels registration of an operation set
   * identified by a parameter to a specific path, so any event related to that path no longer calls
   * corresponding consumers. However if there are other consumers registered to that specific path
   * they are still active and can be called on event.
   *
   * @param id operation set identifier
   */
  void unRegisterByPath(int id);

  /**
   * Start watching a file system item by specifying its path matcher. Any item on file system that
   * matches is registered and being watched. If matched path points to a file than only file
   * related events are taken into account, if matched path points to a folder than all folder
   * entries related events are taken into account.
   *
   * <p>To react on events related to an aforementioned item you can specify {@link Consumer} for
   * create, modify and delete event correspondingly. It is possible to omit one ore more event
   * consumers if it is needed by using empty consumer stub.
   *
   * <p>On successful start you receive a registration identifier to distinguish specific consumer
   * sets as there can be registered arbitrary number of consumers to a single path matcher.
   *
   * @param matcher absolute workspace path
   * @param create consumer for create event
   * @param modify consumer for modify event
   * @param delete consumer for delete event
   * @return operation set identifier
   */
  int registerByMatcher(
      PathMatcher matcher,
      Consumer<String> create,
      Consumer<String> modify,
      Consumer<String> delete);

  /**
   * Stops watching all file system items registered to corresponding path matcher. More accurately
   * it cancels registration of an operation set identified by a parameter to all items defined by
   * path matcher, so any event related to any path that matches the matcher no longer calls
   * corresponding consumers. However if there are other consumers registered to that specific path
   * matcher they are still active and can be called on event.
   *
   * @param id operation set identifier
   */
  void unRegisterByMatcher(int id);

  /**
   * Registers a matcher to skip tracking of creation, modification and deletion events for
   * corresponding entries.
   *
   * @param exclude matcher's pattern
   */
  void addExcludeMatcher(PathMatcher exclude);

  /**
   * Removes a matcher from excludes to resume tracking of corresponding entries creation,
   * modification and deletion events.
   *
   * @param exclude matcher's pattern
   */
  void removeExcludeMatcher(PathMatcher exclude);

  /**
   * Adds entries to includes by path matcher for tracking creation, modification and deletion
   * events for corresponding entries. Note: the goal of this method is to add some entries to
   * includes in case when parent directory is added to excludes.
   *
   * @param matcher matcher's pattern
   */
  void addIncludeMatcher(PathMatcher matcher);

  /**
   * Removes entries from includes by path matcher. Note: use this method to remove some entries
   * from includes in case when these one are added to includes by {@link #addIncludeMatcher}.
   *
   * @param matcher matcher's pattern
   */
  void removeIncludeMatcher(PathMatcher matcher);

  /**
   * Checks if specified path is within excludes
   *
   * @param path path being examined
   * @return true if path is within excludes, false otherwise
   */
  boolean isExcluded(Path path);
}
