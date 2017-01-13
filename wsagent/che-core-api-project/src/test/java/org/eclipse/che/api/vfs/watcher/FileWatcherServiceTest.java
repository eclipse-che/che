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
package org.eclipse.che.api.vfs.watcher;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.util.Set;

import static java.nio.file.Files.createDirectory;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.util.Collections.emptySet;
import static org.apache.commons.io.FileUtils.write;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link FileWatcherService}
 */
@RunWith(MockitoJUnitRunner.class)
public class FileWatcherServiceTest {
    private static final int    TIMEOUT_VALUE = 3_000;
    private static final String FOLDER_NAME   = "folder";
    private static final String FILE_NAME     = "file";

    @Rule
    public TemporaryFolder rootFolder = new TemporaryFolder();

    @Mock
    FileWatcherEventHandler handler;
    Set<PathMatcher> excludes     = emptySet();
    WatchService     watchService = FileSystems.getDefault().newWatchService();

    FileWatcherService service;

    public FileWatcherServiceTest() throws IOException {
    }

    @BeforeClass
    public void setUp() throws Exception {
        service = new FileWatcherService(excludes, handler, watchService);

        service.start();
    }

    private static boolean osIsMacOsX() {
        return System.getProperty("os.name").equalsIgnoreCase("mac os x");
    }

    @Test
    public void shouldWatchRegisteredFolderForFileCreation() throws Exception {
        service.register(rootFolder.getRoot().toPath());

        Path path = rootFolder.newFile(FILE_NAME).toPath();

        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_CREATE);
    }

    @AfterClass
    public void tearDown() throws Exception {
        service.stop();

        reset(handler);

        for (int i = 0; i < 10; i++) {
            if (service.isStopped()) {
                return;
            }
            Thread.sleep(1_000);
        }
        fail();
    }

    @Test
    public void shouldWatchRegisteredFolderForFileRemoval() throws Exception {
        service.register(rootFolder.getRoot().toPath());

        File file = rootFolder.newFile(FILE_NAME);
        Path path = file.toPath();
        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_CREATE);

        boolean deleted = file.delete();
        assertTrue(deleted);
        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_DELETE);
    }

    @Test
    public void shouldWatchRegisteredFolderForDirectoryCreation() throws Exception {
        service.register(rootFolder.getRoot().toPath());

        Path path = rootFolder.newFolder(FOLDER_NAME).toPath();

        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_CREATE);
    }

    @Test
    public void shouldWatchForRegisteredFolderForFileModification() throws Exception {
        service.register(rootFolder.getRoot().toPath());

        File file = rootFolder.newFile(FILE_NAME);
        Path path = file.toPath();
        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_CREATE);

        write(file, "");
        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_MODIFY);
    }

    @Test
    public void shouldWatchRegisteredFolderForFolderRemoval() throws Exception {
        service.register(rootFolder.getRoot().toPath());

        File file = rootFolder.newFolder(FOLDER_NAME);
        Path path = file.toPath();
        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_CREATE);

        boolean deleted = file.delete();
        assertTrue(deleted);
        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_DELETE);
    }

    @Test
    public void shouldNotWatchUnRegisteredFolderForFileCreation() throws Exception {
        Path path = rootFolder.newFile(FILE_NAME).toPath();

        verify(handler, timeout(TIMEOUT_VALUE).never()).handle(path, ENTRY_CREATE);
    }

    @Test
    public void shouldWatchForRegisteredFolderForFolderModification() throws Exception {
        if (!osIsMacOsX()){
            return;
        }

        service.register(rootFolder.getRoot().toPath());

        File file = rootFolder.newFolder(FOLDER_NAME);
        Path path = file.toPath();
        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_CREATE);

        createDirectory(path.resolve(FOLDER_NAME));
        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_MODIFY);
    }

    @Test
    public void shouldNotWatchUnRegisteredFolderForFileRemoval() throws Exception {
        service.register(rootFolder.getRoot().toPath());

        File file = rootFolder.newFile(FILE_NAME);
        Path path = file.toPath();
        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_CREATE);

        service.unRegister(rootFolder.getRoot().toPath());

        boolean deleted = file.delete();
        assertTrue(deleted);
        verify(handler, timeout(TIMEOUT_VALUE).never()).handle(path, ENTRY_DELETE);
    }

    @Test
    public void shouldNotWatchUnRegisteredFolderForDirectoryCreation() throws Exception {
        Path path = rootFolder.newFolder(FOLDER_NAME).toPath();

        verify(handler, timeout(TIMEOUT_VALUE).never()).handle(path, ENTRY_CREATE);
    }

    @Test
    public void shouldNotWatchUnRegisteredFolderForFileModification() throws Exception {
        service.register(rootFolder.getRoot().toPath());

        File file = rootFolder.newFile(FILE_NAME);
        Path path = file.toPath();
        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_CREATE);

        service.unRegister(rootFolder.getRoot().toPath());

        write(file, "");
        verify(handler, timeout(TIMEOUT_VALUE).never()).handle(path, ENTRY_MODIFY);
    }

    @Test
    public void shouldNotWatchUnRegisteredFolderForFolderRemoval() throws Exception {
        service.register(rootFolder.getRoot().toPath());

        File file = rootFolder.newFolder(FOLDER_NAME);
        Path path = file.toPath();
        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_CREATE);

        service.unRegister(rootFolder.getRoot().toPath());

        boolean deleted = file.delete();
        assertTrue(deleted);
        verify(handler, timeout(TIMEOUT_VALUE).never()).handle(path, ENTRY_DELETE);
    }

    @Test
    public void shouldWatchTwiceRegisteredFolderForFileCreationAfterSingleUnregister() throws Exception {
        Path root = rootFolder.getRoot().toPath();

        service.register(root);
        service.register(root);
        service.unRegister(root);


        Path path = rootFolder.newFile(FILE_NAME).toPath();

        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_CREATE);
    }

    @Test
    public void shouldNotWatchTwiceRegisteredFolderForFileCreationAfterDoubleUnRegister() throws Exception {
        Path root = rootFolder.getRoot().toPath();

        service.register(root);
        service.register(root);
        service.unRegister(root);
        service.unRegister(root);

        Path path = rootFolder.newFile(FILE_NAME).toPath();

        verify(handler, timeout(TIMEOUT_VALUE).never()).handle(path, ENTRY_CREATE);
    }

    @Test
    public void shouldNotWatchUnRegisteredFolderForFolderModification() throws Exception {
        service.register(rootFolder.getRoot().toPath());

        File file = rootFolder.newFolder(FOLDER_NAME);
        Path path = file.toPath();
        verify(handler, timeout(TIMEOUT_VALUE)).handle(path, ENTRY_CREATE);

        service.unRegister(rootFolder.getRoot().toPath());

        createDirectory(path.resolve(FILE_NAME));
        verify(handler, timeout(TIMEOUT_VALUE).never()).handle(path, ENTRY_MODIFY);
    }
}
