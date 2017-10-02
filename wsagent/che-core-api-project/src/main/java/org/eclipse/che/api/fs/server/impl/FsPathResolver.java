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
package org.eclipse.che.api.fs.server.impl;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.File;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class FsPathResolver implements org.eclipse.che.api.fs.server.FsPathResolver {

  private final Path root;

  @Inject
  public FsPathResolver(@Named("che.user.workspaces.storage") File root) {
    this.root = root.toPath().normalize().toAbsolutePath();
  }

  @Override
  public Path toFsPath(String wsPath) {
    if (isNullOrEmpty(wsPath)) {
      throw new IllegalArgumentException("Parameter must not be null or empty");
    }

    if (wsPath.startsWith("/")) {
      wsPath = wsPath.substring(1, wsPath.length());
    }

    return root.resolve(wsPath).toAbsolutePath();
  }

  @Override
  public String toWsPath(Path fsPath) {
    if (fsPath == null || !fsPath.isAbsolute()) {
      throw new IllegalArgumentException("");
    }

    return "/" + root.relativize(fsPath);
  }

  @Override
  public boolean isRoot(String wsPath) {
    return "/".equals(wsPath);
  }

  @Override
  public boolean isRoot(Path fsPath) {
    return root.equals(fsPath);
  }

  @Override
  public String toAbsoluteWsPath(String wsPath) {
    return wsPath.startsWith("/") ? wsPath : "/" + wsPath;
  }

  @Override
  public Path toAbsoluteFsPath(String wsPath) {
    return toFsPath(wsPath);
  }

  @Override
  public String getName(String wsPath) {
    return wsPath.substring(wsPath.lastIndexOf("/") + 1);
  }

  @Override
  public String getParentWsPath(String wsPath) {
    String parentWsPath = wsPath.substring(0, wsPath.lastIndexOf("/"));
    return parentWsPath.isEmpty() ? "/" : parentWsPath;
  }

  @Override
  public String resolve(String parentWsPath, String name) {
    return parentWsPath + "/" + name;
  }
}
