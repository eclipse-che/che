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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.VirtualFileEntry;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.search.QueryExpression;
import org.eclipse.che.api.vfs.search.SearchResult;
import org.eclipse.che.api.vfs.search.Searcher;
import org.eclipse.che.plugin.zdb.server.ZendDebugger;

import com.google.inject.Singleton;

/**
 * Zend debug utils.
 *
 * @author Bartlomiej Laczkowski
 */
@Singleton
public class ZendDbgFileUtils {

    private static ProjectManager projectManager;

    @Inject
    public ZendDbgFileUtils(ProjectManager projectManager) {
        ZendDbgFileUtils.projectManager = projectManager;
    }

    /**
     * Finds local file path that corresponds to remote file path.
     *
     * @param remotePath
     * @return corresponding local file path
     */
    public static String getLocalPath(String remotePath) {
        Path remoteFilePath = Path.of(remotePath);
        String remoteFileName = remoteFilePath.getName();
        SearchResult searchResult = null;
        try {
            Searcher searcher = projectManager.getSearcher();
            QueryExpression searchQuery = new QueryExpression().setName(remoteFileName);
            searchResult = searcher.search(searchQuery);
        } catch (Exception e) {
            ZendDebugger.LOG.error(e.getMessage(), e);
            return null;
        }
        if (searchResult == null) {
            return null;
        }
        List<Path> resultPaths = new ArrayList<>();
        for (String resultPath : searchResult.getFilePaths()) {
            resultPaths.add(Path.of(resultPath));
        }
        // Dummy best match for now...
        Path bestMatchPath = null;
        int i = 1;
        while (remoteFilePath.length() - i >= 0) {
            String remoteSegment = remoteFilePath.element(remoteFilePath.length() - i);
            for (Path localFilePath : resultPaths) {
                if (localFilePath.length() - i < 0) {
                    continue;
                }
                String localSegment = localFilePath.element(localFilePath.length() - i);
                if (localSegment.equals(remoteSegment)) {
                    bestMatchPath = localFilePath;
                }
            }
            i++;
        }
        return bestMatchPath != null ? bestMatchPath.toString() : null;
    }

    /**
     * Returns local file absolute path.
     *
     * @param vfsPath
     * @return local file absolute path
     */
    public static String getAbsolutePath(String vfsPath) {
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

    /**
     * Finds and returns VFS file entry for given local path.
     *
     * @param path
     * @return VFS file entry for given local path
     */
    public static VirtualFileEntry getVirtualFileEntry(String path) {
        VirtualFileEntry virtualFileEntry = null;
        try {
            virtualFileEntry = projectManager.getProjectsRoot().getChild(path);
        } catch (ServerException e) {
            ZendDebugger.LOG.error(e.getMessage(), e);
        }
        return virtualFileEntry;
    }

}
