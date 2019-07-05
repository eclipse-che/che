/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.server;

import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.fs.server.WsPathUtils.nameOf;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.ProjectManager;

/**
 * Zend debugger location handler. This class is responsible for bidirectional mapping/converting
 * locations that are specific for Che Virtual File System and Zend Debugger engine local file
 * system.
 *
 * @author Bartlomiej Laczkowski
 */
@Singleton
public class ZendDbgLocationHandler {

  private final FsManager fsManager;
  private final ProjectManager projectManager;

  @Inject
  public ZendDbgLocationHandler(FsManager fsManager, ProjectManager projectManager) {
    this.fsManager = fsManager;
    this.projectManager = projectManager;
  }

  public static final Location createVFS(
      String target, String resourceProjectPath, int lineNumber) {
    return new LocationImpl(target, lineNumber, false, null, resourceProjectPath, null, -1);
  }

  public static final Location createDBG(String resourcePath, int lineNumber) {
    return new LocationImpl(resourcePath, lineNumber, false, null, resourcePath, null, -1);
  }

  /**
   * Convert DBG specific location to VFS one.
   *
   * @return VFS specific location.
   */
  public Location convertToVFS(Location dbgLocation) {
    String remotePath = dbgLocation.getResourceProjectPath();
    String wsPath = absolutize(remotePath);
    if (wsPath.startsWith("/projects")) {
      wsPath = wsPath.substring("/projects".length());
    }

    if (!fsManager.exists(wsPath)) {
      return null;
    }

    String resourceProjectPath =
        projectManager
            .getClosest(wsPath)
            .orElseThrow(() -> new IllegalArgumentException("Can't find project"))
            .getPath();
    String target = nameOf(wsPath);
    int lineNumber = dbgLocation.getLineNumber();
    return new LocationImpl(
        target,
        lineNumber,
        false,
        null,
        resourceProjectPath,
        dbgLocation.getMethod(),
        dbgLocation.getThreadId());
  }

  /**
   * Convert VFS specific location to DBG one.
   *
   * @return DBG specific location.
   */
  public Location convertToDBG(Location vfsLocation) {
    return new LocationImpl(
        "/projects" + vfsLocation.getTarget(),
        vfsLocation.getLineNumber(),
        false,
        null,
        vfsLocation.getResourceProjectPath(),
        null,
        -1);
  }
}
