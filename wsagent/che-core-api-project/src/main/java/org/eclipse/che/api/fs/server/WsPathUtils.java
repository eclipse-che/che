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
package org.eclipse.che.api.fs.server;

import static com.google.common.base.Strings.isNullOrEmpty;

public interface WsPathUtils {

  String ROOT = "/";

  String SEPARATOR = "/";

  static boolean isRoot(String wsPath) {
    return ROOT.equals(wsPath);
  }

  static String nameOf(String wsPath) {
    if (isNullOrEmpty(wsPath)) {
      throw new IllegalArgumentException("Can't get name of empty path or null");
    }

    if (!isAbsolute(wsPath)) {
      throw new IllegalArgumentException("Workspace path should be absolute");
    }

    if (isRoot(wsPath)) {
      throw new IllegalArgumentException("Can't get name of root");
    }

    return wsPath.substring(wsPath.lastIndexOf(SEPARATOR) + 1);
  }

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

    return wsPath.substring(0, wsPath.lastIndexOf(SEPARATOR));
  }

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

  static String absolutize(String wsPath) {
    return wsPath.startsWith(ROOT) ? wsPath : ROOT + wsPath;
  }

  static boolean isAbsolute(String wsPath) {
    return wsPath.startsWith(ROOT);
  }
}
