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

import java.nio.file.Path;

public interface FsPaths {

  String ROOT = "/";

  String SEPARATOR = "/";

  Path toFsPath(String wsPath);

  String toWsPath(Path fsPath);

  boolean isRoot(String wsPath);

  String getName(String wsPath);

  String getParentWsPath(String wsPath);

  String resolve(String wsPath, String name);

  default boolean isRoot(Path fsPath) {
    return isRoot(toWsPath(fsPath));
  }

  default String absolutize(String wsPath) {
    return toWsPath(toFsPath(wsPath));
  }
}
