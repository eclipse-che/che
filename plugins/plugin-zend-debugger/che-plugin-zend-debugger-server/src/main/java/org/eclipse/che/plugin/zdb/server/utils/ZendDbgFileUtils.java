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
package org.eclipse.che.plugin.zdb.server.utils;

import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.plugin.zdb.server.ZendDebugger;

import javax.inject.Inject;
import java.io.File;

/**
 * Zend debug utils.
 *
 * @author Bartlomiej Laczkowski
 */
@Singleton
public class ZendDbgFileUtils {

    private static Provider<ProjectManager> projectManagerProvider;

    @Inject
    public ZendDbgFileUtils(Provider<ProjectManager> projectManagerProvider) {
        ZendDbgFileUtils.projectManagerProvider = projectManagerProvider;
    }


    /**
     * Finds local file entry that corresponds to remote file path.
     *
     * @param remotePath
     * @return corresponding local file entry
     */
    public static VirtualFileEntry findVirtualFileEntry(String remotePath) {
        Path remoteFilePath = Path.of(remotePath);
        try {
            for (int i = 0; i < remoteFilePath.length(); i++) {
                Path path = remoteFilePath.subPath(i);
                VirtualFileEntry child = getVirtualFileEntry(path.toString());
                if (child != null) {
                    return child;
                }
            }
        } catch (Exception e) {
            ZendDebugger.LOG.error(e.getMessage(), e);
            return null;
        }
        return null;
    }

    /**
     * Returns local file absolute path.
     *
     * @param vfsPath
     * @return local file absolute path
     */
    public static String findAbsolutePath(String vfsPath) {
        VirtualFileEntry virtualFileEntry = getVirtualFileEntry(vfsPath);
        if (virtualFileEntry != null) {
            File ioFile = virtualFileEntry.getVirtualFile().toIoFile();
            if (ioFile != null) {
                return ioFile.getAbsolutePath();
            }
            return virtualFileEntry.getVirtualFile().getPath().toString();
        }
        return vfsPath;
    }

    private static VirtualFileEntry getVirtualFileEntry(String path) {
        VirtualFileEntry virtualFileEntry = null;
        try {
            virtualFileEntry = projectManagerProvider.get().getProjectsRoot().getChild(path);
        } catch (ServerException e) {
            ZendDebugger.LOG.error(e.getMessage(), e);
        }
        return virtualFileEntry;
    }

}
