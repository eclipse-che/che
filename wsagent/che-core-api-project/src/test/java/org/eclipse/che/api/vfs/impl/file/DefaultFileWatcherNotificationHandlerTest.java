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

import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.eclipse.che.commons.lang.NameGenerator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.CREATED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.DELETED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author andrew00x
 */
public class DefaultFileWatcherNotificationHandlerTest {
    private File                   testDirectory;

    private FileWatcherNotificationListener notificationListener;
    private LocalVirtualFileSystem virtualFileSystem;

    private Path virtualFilePath;
    private VirtualFile virtualFile;
    private DefaultFileWatcherNotificationHandler notificationHandler;

    @Before
    public void setUp() throws Exception {
        File targetDir = new File(Thread.currentThread().getContextClassLoader().getResource(".").getPath()).getParentFile();
        testDirectory = new File(targetDir, NameGenerator.generate("watcher-notifications-", 4));

        virtualFileSystem = mock(LocalVirtualFileSystem.class, RETURNS_DEEP_STUBS);
        VirtualFileSystemProvider virtualFileSystemProvider = mock(VirtualFileSystemProvider.class);
        when(virtualFileSystemProvider.getVirtualFileSystem(true)).thenReturn(virtualFileSystem);

        notificationListener = mock(FileWatcherNotificationListener.class);

        notificationHandler = new DefaultFileWatcherNotificationHandler(virtualFileSystemProvider);
        notificationHandler.addNotificationListener(notificationListener);

        virtualFilePath = Path.of("/a/b/c");
        virtualFile =  new LocalVirtualFile(testDirectory, virtualFilePath, virtualFileSystem);
        when(virtualFileSystem.getRoot().getChild(virtualFilePath)).thenReturn(virtualFile);
        when(notificationListener.shouldBeNotifiedFor(virtualFile)).thenReturn(true);
    }

    @Test
    public void notifiesFileWatcherNotificationListenersWhenPathIsCreated() throws Exception {
        notificationHandler.handleFileWatcherEvent(CREATED, testDirectory, "/a/b/c", true);

        verify(notificationListener).shouldBeNotifiedFor(virtualFile);
        verify(notificationListener).onFileWatcherEvent(virtualFile, CREATED);
    }

    @Test
    public void notifiesFileWatcherNotificationListenersWhenPathIsDeleted() throws Exception {
        VirtualFile _viVirtualFile = virtualFile;
        deleteFile();

        notificationHandler.handleFileWatcherEvent(DELETED, testDirectory, "/a/b/c", true);

        verify(notificationListener).shouldBeNotifiedFor(eq(_viVirtualFile));
        verify(notificationListener).onFileWatcherEvent(eq(_viVirtualFile), eq(DELETED));
    }

    private void deleteFile() {
        virtualFile = null;
    }

    @Test
    public void notifiesFileWatcherNotificationListenersWhenPathIsModified() throws Exception {
        notificationHandler.handleFileWatcherEvent(MODIFIED, testDirectory, "/a/b/c", true);

        verify(notificationListener).shouldBeNotifiedFor(virtualFile);
        verify(notificationListener).onFileWatcherEvent(virtualFile, MODIFIED);
    }
}
