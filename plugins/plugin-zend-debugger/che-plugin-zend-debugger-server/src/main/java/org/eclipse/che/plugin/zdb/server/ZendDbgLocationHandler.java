/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.zdb.server;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.impl.LocationImpl;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.plugin.zdb.server.utils.ZendDbgFileUtils;

/**
 * Zend debugger location handler. This class is responsible for bidirectional
 * mapping/converting locations that are specific for Che Virtual File System
 * and Zend Debugger engine local file system.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgLocationHandler {

    public static final Location createVFS(String target, String resourcePath, String resourceProjectPath,
            int lineNumber) {
        return new LocationImpl(target, lineNumber, resourcePath, false, 0, resourceProjectPath);
    }

    public static final Location createDBG(String resourcePath, int lineNumber) {
        return new LocationImpl(Path.of(resourcePath).getName(), lineNumber, resourcePath, false, 0, null);
    }

    /**
     * Convert DBG specific location to VFS one.
     * 
     * @param dbgLocation
     * @return VFS specific location.
     */
    public Location convertToVFS(Location dbgLocation) {
        VirtualFileEntry localFileEntry = ZendDbgFileUtils.findVirtualFileEntry(dbgLocation.getResourcePath());
        if (localFileEntry == null) {
            return null;
        }
        String resourceProjectPath = localFileEntry.getProject();
        String target = localFileEntry.getName();
        String resourcePath = localFileEntry.getPath().toString();
        int lineNumber = dbgLocation.getLineNumber();
        return new LocationImpl(target, lineNumber, resourcePath, false, 0, resourceProjectPath);
    }

    /**
     * Convert VFS specific location to DBG one.
     * 
     * @param dbgLocation
     * @return DBG specific location.
     */
    public Location convertToDBG(Location vfsLocation) {
        String resourcePath = ZendDbgFileUtils.findAbsolutePath(vfsLocation.getResourcePath());
        int lineNumber = vfsLocation.getLineNumber();
        return new LocationImpl(null, lineNumber, resourcePath, false, 0, null);
    }

}
