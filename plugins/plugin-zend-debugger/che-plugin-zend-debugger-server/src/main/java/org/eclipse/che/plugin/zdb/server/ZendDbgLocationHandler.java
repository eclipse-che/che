/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.zdb.server;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.plugin.zdb.server.utils.ZendDbgFileUtils;

/**
 * Zend debugger location handler. This class is responsible for bidirectional mapping/converting
 * locations that are specific for Che Virtual File System and Zend Debugger engine local file
 * system.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgLocationHandler {

  public static final Location createVFS(
      String target, String resourceProjectPath, int lineNumber) {
    return new LocationImpl(target, lineNumber, false, 0, resourceProjectPath, null, -1);
  }

  public static final Location createDBG(String resourcePath, int lineNumber) {
    return new LocationImpl(
        Path.of(resourcePath).getName(), lineNumber, false, 0, resourcePath, null, -1);
  }

  /**
   * Convert DBG specific location to VFS one.
   *
   * @param dbgLocation
   * @return VFS specific location.
   */
  public Location convertToVFS(Location dbgLocation) {
    VirtualFileEntry localFileEntry =
        ZendDbgFileUtils.findVirtualFileEntry(dbgLocation.getResourceProjectPath());
    if (localFileEntry == null) {
      return null;
    }
    String resourceProjectPath = localFileEntry.getProject();
    String target = localFileEntry.getName();
    int lineNumber = dbgLocation.getLineNumber();
    return new LocationImpl(
        target,
        lineNumber,
        false,
        0,
        resourceProjectPath,
        dbgLocation.getMethod(),
        dbgLocation.getThreadId());
  }

  /**
   * Convert VFS specific location to DBG one.
   *
   * @param vfsLocation
   * @return DBG specific location.
   */
  public Location convertToDBG(Location vfsLocation) {
    return new LocationImpl(
        "/projects" + vfsLocation.getTarget(),
        vfsLocation.getLineNumber(),
        false,
        0,
        vfsLocation.getResourceProjectPath(),
        null,
        -1);
  }
}
