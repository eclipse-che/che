/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.server.LazyIterator;
import org.eclipse.che.api.vfs.server.VirtualFile;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.eclipse.che.api.vfs.server.VirtualFileFilter.ALL;

/**
 * The class which store information about changed files (move,rename,create,delete,open) through IDE. This class need to track
 * changing of files through IDE, to distinguish them from changes through file system. The instance of {@link FilesBuffer} is
 * singleton.
 *
 * @author Dmitry Shnurenko
 */
public class FilesBuffer {

    private static FilesBuffer buffer;

    private List<String> filesBuffer;

    private FilesBuffer() {
        this.filesBuffer = new CopyOnWriteArrayList<>();
    }

    public static FilesBuffer get() {
        if (buffer == null) {
            buffer = new FilesBuffer();
        }

        return buffer;
    }

    /**
     * Add paths of changed files to special temporary buffer. Before each adding paths the buffer is cleared.
     *
     * @param paths
     *         paths to changed files
     */
    public synchronized void addToBuffer(@NotNull String... paths) {
        filesBuffer.clear();

        for (String path : paths) {
            filesBuffer.add(getValidPath(path));
        }
    }

    @NotNull
    private String getValidPath(@NotNull String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }

    /**
     * Add paths of changed files to special temporary buffer recursively. Method can throw {@link ServerException}
     * when something happens with getting children.
     *
     * @param root
     *         root file which contains inner files which will be added to temporary buffer
     * @throws ServerException
     */
    public synchronized void addToBufferRecursive(@NotNull VirtualFile root) throws ServerException {
        filesBuffer.add(getValidPath(root.getPath()));

        LazyIterator<VirtualFile> children = root.getChildren(ALL);

        while (children.hasNext()) {
            addToBufferRecursive(children.next());
        }
    }

    /**
     * Check if is there current path to file in buffer or not.
     *
     * @param filePath
     *         path which need to check
     * @return <code>true</code> if file is in buffer, <code>false</code> if file is not in buffer
     */
    public boolean isContainsPath(@NotNull String filePath) {
        return filesBuffer.contains(filePath);
    }
}
