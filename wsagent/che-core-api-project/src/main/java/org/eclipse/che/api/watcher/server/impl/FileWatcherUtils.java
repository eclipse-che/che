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

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Set;

public class FileWatcherUtils {

  /**
   * Transform internal path representation into normal path representation
   *
   * @param root root of virtual file system
   * @param path internal path representation
   * @return normal path
   */
  public static Path toNormalPath(Path root, String path) {
    return root.resolve(path.startsWith("/") ? path.substring(1) : path).toAbsolutePath();
  }

  /**
   * Transforms normal path representation into internal virtual file system
   *
   * @param root root of virtual file system
   * @param path normal path representation
   * @return internal path
   */
  public static String toInternalPath(Path root, Path path) {
    return "/" + root.toAbsolutePath().relativize(path);
  }

  /**
   * Checks if specified path is within excludes
   *
   * @param excludes set of exclude matchers
   * @param path path being examined
   * @return true if path is within excludes, false otherwise
   */
  public static boolean isExcluded(Set<PathMatcher> excludes, Path path) {
    for (PathMatcher matcher : excludes) {
      if (matcher.matches(path)) {
        return true;
      }
    }
    return false;
  }
}
