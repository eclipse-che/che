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

import java.io.File;
import java.nio.file.Path;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.fs.server.FsPaths;

@Singleton
public class StandardFsPaths implements FsPaths {

  private final Path root;

  @Inject
  public StandardFsPaths(@Named("che.user.workspaces.storage") File root) {
    this.root = root.toPath().normalize().toAbsolutePath();
  }

  @Override
  public Path toFsPath(String wsPath) {
    wsPath = wsPath.startsWith(ROOT) ? wsPath.substring(1) : wsPath;

    return root.resolve(wsPath).normalize().toAbsolutePath();
  }

  @Override
  public String toWsPath(Path fsPath) {
    Path absoluteFsPath = fsPath.toAbsolutePath();

    return ROOT + root.relativize(absoluteFsPath);
  }

  @Override
  public boolean isRoot(String wsPath) {
    return ROOT.equals(wsPath);
  }

  @Override
  public String getName(String wsPath) {
    return wsPath.substring(wsPath.lastIndexOf(SEPARATOR) + 1);
  }

  @Override
  public String getParentWsPath(String wsPath) {
    String parentWsPath = wsPath.substring(0, wsPath.lastIndexOf(SEPARATOR));
    return parentWsPath.isEmpty() ? ROOT : parentWsPath;
  }

  @Override
  public String resolve(String wsPath, String name) {
    return wsPath + SEPARATOR + name;
  }
}
