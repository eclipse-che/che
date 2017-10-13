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

public interface WsPathUtils {

  String ROOT = "/";

  String SEPARATOR = "/";

  static boolean isRoot(String wsPath) {
    return ROOT.equals(wsPath);
  }

  static String nameOf(String wsPath) {
    return wsPath.substring(wsPath.lastIndexOf(SEPARATOR) + 1);
  }

  static String parentOf(String wsPath) {
    String parentWsPath = wsPath.substring(0, wsPath.lastIndexOf(SEPARATOR));
    return parentWsPath.isEmpty() ? ROOT : parentWsPath;
  }

  static String resolve(String wsPath, String name) {
    return wsPath + SEPARATOR + name;
  }

  static String absolutize(String wsPath) {
    return wsPath.startsWith(ROOT) ? wsPath : ROOT + wsPath;
  }
}
