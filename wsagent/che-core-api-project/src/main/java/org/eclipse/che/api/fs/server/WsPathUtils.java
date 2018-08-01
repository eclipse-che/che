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
package org.eclipse.che.api.fs.server;

import static com.google.common.base.Strings.isNullOrEmpty;

/** Utility class for workspace path related manipulations */
public interface WsPathUtils {

  String ROOT = "/";

  String SEPARATOR = "/";

  /**
   * Show if a path is root path
   *
   * @param wsPath absolute workspace path
   * @return
   */
  static boolean isRoot(String wsPath) {
    return ROOT.equals(wsPath);
  }

  /**
   * Get name component of a path
   *
   * @param wsPath absolute workspace path
   * @return
   */
  static String nameOf(String wsPath) {
    if (isNullOrEmpty(wsPath)) {
      throw new IllegalArgumentException("Can't get name of empty path or null");
    }

    if (!isAbsolute(wsPath)) {
      throw new IllegalArgumentException("Workspace path should be absolute");
    }

    if (isRoot(wsPath)) {
      return ROOT;
    }

    if (wsPath.endsWith(SEPARATOR)) {
      wsPath = wsPath.substring(0, wsPath.length() - 1);
    }

    return wsPath.substring(wsPath.lastIndexOf(SEPARATOR) + 1);
  }

  /**
   * Get parent component of a path
   *
   * @param wsPath absolute workspace path
   * @return
   */
  static String parentOf(String wsPath) {
    if (isNullOrEmpty(wsPath)) {
      throw new IllegalArgumentException("Can't get parent of empty path or null");
    }

    if (!isAbsolute(wsPath)) {
      throw new IllegalArgumentException("Workspace path should be absolute");
    }

    if (isRoot(wsPath)) {
      throw new IllegalArgumentException("Can't get parent of root");
    }

    if (wsPath.endsWith(SEPARATOR)) {
      wsPath = wsPath.substring(0, wsPath.length() - 1);
    }

    int lastSeparatorPosition = wsPath.lastIndexOf(SEPARATOR);
    if (lastSeparatorPosition == 0) {
      return ROOT;
    } else {
      return wsPath.substring(0, lastSeparatorPosition);
    }
  }

  /**
   * Resolve the path with the name
   *
   * @param wsPath absolute workspace path
   * @param name name
   * @return
   */
  static String resolve(String wsPath, String name) {
    if (isNullOrEmpty(wsPath)) {
      throw new IllegalArgumentException("Can't resolve for parent that is empty or null");
    }

    if (!isAbsolute(wsPath)) {
      throw new IllegalArgumentException("Workspace path should be absolute");
    }

    if (isNullOrEmpty(name)) {
      throw new IllegalArgumentException("Can't resolve item that is empty or null");
    }

    return wsPath.endsWith(SEPARATOR) ? wsPath + name : wsPath + SEPARATOR + name;
  }

  /**
   * Make a path absolute
   *
   * @param wsPath relative workspace path
   * @return
   */
  static String absolutize(String wsPath) {
    return wsPath.startsWith(ROOT) ? wsPath : ROOT + wsPath;
  }

  /**
   * Show if workspace path is absolute
   *
   * @param wsPath workspace path
   * @return
   */
  static boolean isAbsolute(String wsPath) {
    return wsPath.startsWith(ROOT);
  }
}
