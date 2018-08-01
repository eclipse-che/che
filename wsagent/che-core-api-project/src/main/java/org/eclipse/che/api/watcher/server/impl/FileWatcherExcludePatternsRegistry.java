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
package org.eclipse.che.api.watcher.server.impl;

import static com.google.common.collect.Sets.newConcurrentHashSet;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Registry for managing of tracking creation, modification and deletion events for corresponding
 * entries. Allows to add entries to excludes or includes by {@link PathMatcher}.
 */
@Singleton
public class FileWatcherExcludePatternsRegistry {
  private Set<PathMatcher> excludes;
  private Set<PathMatcher> includes = newConcurrentHashSet();

  @Inject
  public FileWatcherExcludePatternsRegistry(
      @Named("che.user.workspaces.storage.excludes") Set<PathMatcher> excludes) {
    this.excludes = newConcurrentHashSet(excludes);
  }

  /**
   * Registers a matcher to skip tracking of creation, modification and deletion events for
   * corresponding entries.
   *
   * @param matcher matcher's pattern
   */
  public void addExcludeMatcher(PathMatcher matcher) {
    excludes.add(matcher);
  }

  /**
   * Removes a matcher from excludes to resume tracking of corresponding entries creation,
   * modification and deletion events.
   *
   * @param matcher matcher's pattern
   */
  public void removeExcludeMatcher(PathMatcher matcher) {
    excludes.remove(matcher);
  }

  /**
   * Adds entries to includes by path matcher for tracking creation, modification and deletion
   * events for corresponding entries. Note: the goal of this method is to add some entries to
   * includes in case when parent directory is added to excludes.
   *
   * @param matcher matcher's pattern
   */
  public void addIncludeMatcher(PathMatcher matcher) {
    includes.add(matcher);
  }

  /**
   * Removes entries from includes by path matcher. Note: use this method to remove some entries
   * from includes in case when these one are added to includes by {@link #addIncludeMatcher}.
   *
   * @param matcher matcher's pattern
   */
  public void removeIncludeMatcher(PathMatcher matcher) {
    includes.remove(matcher);
  }

  /**
   * Checks if specified path is within excludes
   *
   * @param path path being examined
   * @return true if path is within excludes, false otherwise
   */
  public boolean isExcluded(Path path) {
    return !isIncluded(path)
        && excludes.stream().anyMatch(pathMatcher -> pathMatcher.matches(path));
  }

  /**
   * Checks if specified path is within includes
   *
   * @param path path being examined
   * @return true if path is within includes, false otherwise
   */
  public boolean isIncluded(Path path) {
    return includes.stream().anyMatch(pathMatcher -> pathMatcher.matches(path));
  }
}
