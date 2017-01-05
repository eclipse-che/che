/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.impl.file;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.eclipse.che.api.vfs.Path.ROOT;

@Singleton
public class DefaultFileWatcherNotificationHandler implements FileWatcherNotificationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultFileWatcherNotificationHandler.class);

    private final VirtualFileSystemProvider             virtualFileSystemProvider;
    private final List<FileWatcherNotificationListener> fileWatcherNotificationListeners;

    @Inject
    public DefaultFileWatcherNotificationHandler(VirtualFileSystemProvider virtualFileSystemProvider) {
        this.virtualFileSystemProvider = virtualFileSystemProvider;
        fileWatcherNotificationListeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public void handleFileWatcherEvent(FileWatcherEventType eventType, File watchRoot, String subPath, boolean isDir) {
        VirtualFile virtualFile = convertToVirtualFile(watchRoot, subPath, isDir);
        if (virtualFile == null) {
            return;
        }
        for (FileWatcherNotificationListener virtualFileListener : fileWatcherNotificationListeners) {
            if (virtualFileListener.shouldBeNotifiedFor(virtualFile)) {
                virtualFileListener.onFileWatcherEvent(virtualFile, eventType);
            }
        }
    }

    public void started(File watchRoot) {
        LOG.debug("Start watching file events on {}", watchRoot);
    }

    public void errorOccurred(File watchRoot, Throwable cause) {
        LOG.warn("Error occurs while watching file events on {}: {}", watchRoot, cause.getMessage());
    }

    @Override
    public boolean addNotificationListener(FileWatcherNotificationListener fileWatcherNotificationListener) {
        return fileWatcherNotificationListeners.add(fileWatcherNotificationListener);
    }

    @Override
    public boolean removeNotificationListener(FileWatcherNotificationListener fileWatcherNotificationListener) {
        return fileWatcherNotificationListeners.remove(fileWatcherNotificationListener);
    }

    private VirtualFile convertToVirtualFile(File root, String subPath, boolean isDir) {
        try {
            LocalVirtualFileSystem virtualFileSystem = (LocalVirtualFileSystem)virtualFileSystemProvider.getVirtualFileSystem(true);
            Path vfsPath = Path.of(subPath);
            VirtualFile virtualFile = virtualFileSystem.getRoot().getChild(vfsPath);
            if (virtualFile == null) {
                virtualFile = new DeletedLocalVirtualFile(new File(root, subPath), ROOT.newPath(vfsPath), virtualFileSystem, isDir);
            }
            return virtualFile;
        } catch (ServerException e) {
            LOG.warn(e.getMessage());
        }
        return null;
    }

    private static class DeletedLocalVirtualFile extends LocalVirtualFile {
        private final boolean isDir;

        DeletedLocalVirtualFile(File ioFile, Path path, LocalVirtualFileSystem fileSystem, boolean isDir) {
            super(ioFile, path, fileSystem);
            this.isDir = isDir;
        }

        @Override
        public boolean isFile() {
            return !isDir;
        }

        @Override
        public boolean isFolder() {
            return isDir;
        }
    }
}
