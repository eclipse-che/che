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

import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.CREATED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.DELETED;
import static org.eclipse.che.api.project.shared.dto.event.FileWatcherEventType.MODIFIED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FileTreeWatcherTest {
    private File                testDirectory;
    private FileTreeWatcher     fileWatcher;
    private FileWatcherTestTree fileWatcherTestTree;

    @Before
    public void setUp() throws Exception {
        File targetDir = new File(Thread.currentThread().getContextClassLoader().getResource(".").getPath()).getParentFile();
        testDirectory = new File(targetDir, NameGenerator.generate("watcher-", 4));
        assertTrue(testDirectory.mkdir());
        fileWatcherTestTree = new FileWatcherTestTree(testDirectory);
    }

    @After
    public void tearDown() throws Exception {
        if (fileWatcher != null) {
            fileWatcher.shutdown();
        }
        IoUtil.deleteRecursive(testDirectory);
    }

    @Test
    public void watchesCreate() throws Exception {
        fileWatcherTestTree.createDirectory("", "watched");

        FileWatcherNotificationHandler notificationHandler = aNotificationHandler();
        fileWatcher = new FileTreeWatcher(testDirectory, newHashSet(), notificationHandler);
        fileWatcher.startup();

        Thread.sleep(500);

        Set<String> created = newHashSet(fileWatcherTestTree.createDirectory(""),
                                         fileWatcherTestTree.createFile(""),
                                         fileWatcherTestTree.createDirectory("watched"),
                                         fileWatcherTestTree.createFile("watched"));

        Thread.sleep(5000);

        verify(notificationHandler, never()).errorOccurred(eq(testDirectory), any(Throwable.class));
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(DELETED), eq(testDirectory), anyString(), anyBoolean());
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(MODIFIED), eq(testDirectory), anyString(), anyBoolean());

        ArgumentCaptor<String> createdEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationHandler, times(4)).handleFileWatcherEvent(eq(CREATED), eq(testDirectory), createdEvents.capture(), anyBoolean());
        assertEquals(newHashSet(created), newHashSet(createdEvents.getAllValues()));
    }

    @Test
    public void watchesCreateDirectoryStructure() throws Exception {
        FileWatcherNotificationHandler notificationHandler = aNotificationHandler();
        fileWatcher = new FileTreeWatcher(testDirectory, newHashSet(), notificationHandler);
        fileWatcher.startup();

        Thread.sleep(500);

        List<String> created = fileWatcherTestTree.createTree("", 2, 2);

        Thread.sleep(5000);

        verify(notificationHandler, never()).errorOccurred(eq(testDirectory), any(Throwable.class));
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(DELETED), eq(testDirectory), anyString(), anyBoolean());
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(MODIFIED), eq(testDirectory), anyString(), anyBoolean());

        ArgumentCaptor<String> createdEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationHandler, times(4)).handleFileWatcherEvent(eq(CREATED), eq(testDirectory), createdEvents.capture(), anyBoolean());
        assertEquals(newHashSet(created), newHashSet(createdEvents.getAllValues()));
    }

    @Test
    public void watchesCreatedSubDirectoriesRecursively() throws Exception {
        FileWatcherNotificationHandler notificationHandler = aNotificationHandler();
        fileWatcher = new FileTreeWatcher(testDirectory, newHashSet(), notificationHandler);
        fileWatcher.startup();

        Thread.sleep(500);

        final String first = fileWatcherTestTree.createDirectory("", "first");
        final String second = fileWatcherTestTree.createDirectory(first, "second");
        final String file = fileWatcherTestTree.createFile(second);

        Thread.sleep(5000);

        fileWatcherTestTree.updateFile(file);

        Thread.sleep(5000);

        verify(notificationHandler, never()).errorOccurred(eq(testDirectory), any(Throwable.class));
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(DELETED), eq(testDirectory), anyString(), anyBoolean());

        verify(notificationHandler, times(3)).handleFileWatcherEvent(eq(CREATED), eq(testDirectory), anyString(), anyBoolean());

        ArgumentCaptor<String> modifiedEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationHandler).handleFileWatcherEvent(eq(MODIFIED), eq(testDirectory), modifiedEvents.capture(), anyBoolean());
        assertEquals(newHashSet(file), newHashSet(modifiedEvents.getAllValues()));
    }

    @Test
    public void watchesCreateDirectoryAndStartsWatchingNewlyCreatedDirectory() throws Exception {
        FileWatcherNotificationHandler notificationHandler = aNotificationHandler();
        fileWatcher = new FileTreeWatcher(testDirectory, newHashSet(), notificationHandler);
        fileWatcher.startup();

        Thread.sleep(500);

        String directory = fileWatcherTestTree.createDirectory("");

        Thread.sleep(5000);

        String file = fileWatcherTestTree.createFile(directory);

        Thread.sleep(5000);

        verify(notificationHandler, never()).errorOccurred(eq(testDirectory), any(Throwable.class));
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(DELETED), eq(testDirectory), anyString(), anyBoolean());
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(MODIFIED), eq(testDirectory), anyString(), anyBoolean());

        ArgumentCaptor<String> createdEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationHandler, times(2)).handleFileWatcherEvent(eq(CREATED), eq(testDirectory), createdEvents.capture(), anyBoolean());
        assertEquals(newHashSet(directory, file), newHashSet(createdEvents.getAllValues()));
    }

    @Test
    public void watchesUpdate() throws Exception {
        fileWatcherTestTree.createDirectory("", "watched");
        String notifiedFile1 = fileWatcherTestTree.createFile("");
        String notifiedFile2 = fileWatcherTestTree.createFile("watched");
        Set<String> updated = newHashSet(notifiedFile1, notifiedFile2);

        FileWatcherNotificationHandler notificationHandler = aNotificationHandler();
        fileWatcher = new FileTreeWatcher(testDirectory, newHashSet(), notificationHandler);
        fileWatcher.startup();

        Thread.sleep(1000);

        fileWatcherTestTree.updateFile(notifiedFile1);
        fileWatcherTestTree.updateFile(notifiedFile2);

        Thread.sleep(5000);

        verify(notificationHandler, never()).errorOccurred(eq(testDirectory), any(Throwable.class));
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(CREATED), eq(testDirectory), anyString(), anyBoolean());
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(DELETED), eq(testDirectory), anyString(), anyBoolean());

        ArgumentCaptor<String> updatedEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationHandler, times(2)).handleFileWatcherEvent(eq(MODIFIED), eq(testDirectory), updatedEvents.capture(), anyBoolean());
        assertEquals(updated, newHashSet(updatedEvents.getAllValues()));
    }

    @Test
    public void watchesFolderModifiedOnDelete() throws Exception {
        final String watchedDir = fileWatcherTestTree.createDirectory("", "watched");
        final String notifiedFile = fileWatcherTestTree.createFile("watched");
        final Set<String> deleted = newHashSet(notifiedFile);
        final Set<String> modified = newHashSet(watchedDir);

        final FileWatcherNotificationHandler notificationHandler = aNotificationHandler();
        fileWatcher = new FileTreeWatcher(testDirectory, newHashSet(), notificationHandler);
        fileWatcher.startup();

        Thread.sleep(1000);

        fileWatcherTestTree.delete(notifiedFile);

        Thread.sleep(5000);

        verify(notificationHandler, never()).errorOccurred(eq(testDirectory), any(Throwable.class));

        final ArgumentCaptor<String> deletedEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationHandler, times(1)).handleFileWatcherEvent(eq(DELETED), eq(testDirectory), deletedEvents.capture(), anyBoolean());
        assertEquals(deleted, newHashSet(deletedEvents.getAllValues()));

        final ArgumentCaptor<String> modifiedEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationHandler, times(1)).handleFileWatcherEvent(eq(MODIFIED), eq(testDirectory), modifiedEvents.capture(), anyBoolean());
        assertEquals(modified, newHashSet(modifiedEvents.getAllValues()));
    }

    @Test
    public void watchesFolderModifiedOnCreate() throws Exception {
        final String watchedDir = fileWatcherTestTree.createDirectory("", "watched");
        final Set<String> modified = newHashSet(watchedDir);

        final FileWatcherNotificationHandler notificationHandler = aNotificationHandler();
        fileWatcher = new FileTreeWatcher(testDirectory, newHashSet(), notificationHandler);
        fileWatcher.startup();

        Thread.sleep(1000);

        final String notifiedFile = fileWatcherTestTree.createFile("watched");
        final Set<String> created = newHashSet(notifiedFile);

        Thread.sleep(5000);

        verify(notificationHandler, never()).errorOccurred(eq(testDirectory), any(Throwable.class));

        final ArgumentCaptor<String> deletedEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationHandler, times(1)).handleFileWatcherEvent(eq(CREATED), eq(testDirectory), deletedEvents.capture(), anyBoolean());
        assertEquals(created, newHashSet(deletedEvents.getAllValues()));

        final ArgumentCaptor<String> modifiedEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationHandler, times(1)).handleFileWatcherEvent(eq(MODIFIED), eq(testDirectory), modifiedEvents.capture(), anyBoolean());
        assertEquals(modified, newHashSet(modifiedEvents.getAllValues()));
    }

    @Test
    public void watchesDelete() throws Exception {
        fileWatcherTestTree.createDirectory("", "watched");
        String deletedDir1 = fileWatcherTestTree.createDirectory("watched");
        String deletedFile1 = fileWatcherTestTree.createFile("watched");
        Set<String> deleted = newHashSet("watched", deletedDir1, deletedFile1);

        FileWatcherNotificationHandler notificationHandler = aNotificationHandler();
        fileWatcher = new FileTreeWatcher(testDirectory, newHashSet(), notificationHandler);
        fileWatcher.startup();

        Thread.sleep(500);

        fileWatcherTestTree.delete("watched");

        Thread.sleep(5000);

        verify(notificationHandler, never()).errorOccurred(eq(testDirectory), any(Throwable.class));
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(CREATED), eq(testDirectory), anyString(), anyBoolean());
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(MODIFIED), eq(testDirectory), anyString(), anyBoolean());

        ArgumentCaptor<String> deletedEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationHandler, times(3)).handleFileWatcherEvent(eq(DELETED), eq(testDirectory), deletedEvents.capture(), anyBoolean());
        assertEquals(deleted, newHashSet(deletedEvents.getAllValues()));
    }

    @Test
    public void doesNotWatchExcludedDirectories() throws Exception {
        fileWatcherTestTree.createDirectory("", "excluded");

        FileWatcherNotificationHandler notificationHandler = aNotificationHandler();
        PathMatcher excludeMatcher =  FileSystems.getDefault().getPathMatcher("glob:excluded");
        fileWatcher = new FileTreeWatcher(testDirectory, newHashSet(excludeMatcher), notificationHandler);
        fileWatcher.startup();

        Thread.sleep(500);

        String directory = fileWatcherTestTree.createDirectory("");
        String file = fileWatcherTestTree.createFile("");
        fileWatcherTestTree.createDirectory("excluded");
        fileWatcherTestTree.createFile("excluded");

        Set<String> created = newHashSet(directory, file);

        Thread.sleep(5000);

        verify(notificationHandler, never()).errorOccurred(eq(testDirectory), any(Throwable.class));
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(DELETED), eq(testDirectory), anyString(), anyBoolean());
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(MODIFIED), eq(testDirectory), anyString(), anyBoolean());

        ArgumentCaptor<String> createdEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationHandler, times(2)).handleFileWatcherEvent(eq(CREATED), eq(testDirectory), createdEvents.capture(), anyBoolean());
        assertEquals(newHashSet(created), newHashSet(createdEvents.getAllValues()));
    }

    @Test
    public void doesNotNotifyAboutIgnoredFiles() throws Exception {
        FileWatcherNotificationHandler notificationHandler = aNotificationHandler();
        PathMatcher excludeMatcher =  FileSystems.getDefault().getPathMatcher("glob:*.{foo,bar}");
        fileWatcher = new FileTreeWatcher(testDirectory, newHashSet(excludeMatcher), notificationHandler);
        fileWatcher.startup();

        Thread.sleep(500);

        String file = fileWatcherTestTree.createFile("");
        fileWatcherTestTree.createFile("", "xxx.bar");
        fileWatcherTestTree.createFile("", "xxx.foo");

        Set<String> created = newHashSet(file);

        Thread.sleep(5000);

        verify(notificationHandler, never()).errorOccurred(eq(testDirectory), any(Throwable.class));
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(DELETED), eq(testDirectory), anyString(), anyBoolean());
        verify(notificationHandler, never()).handleFileWatcherEvent(eq(MODIFIED), eq(testDirectory), anyString(), anyBoolean());

        ArgumentCaptor<String> createdEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationHandler, times(1)).handleFileWatcherEvent(eq(CREATED), eq(testDirectory), createdEvents.capture(), anyBoolean());
        assertEquals(newHashSet(created), newHashSet(createdEvents.getAllValues()));
    }

    @Test
    public void notifiesNotificationListenerWhenStarted() throws Exception {
        FileWatcherNotificationHandler notificationHandler = aNotificationHandler();
        fileWatcher = new FileTreeWatcher(testDirectory, newHashSet(), notificationHandler);
        fileWatcher.startup();

        Thread.sleep(500);

        verify(notificationHandler, timeout(10000)).started(eq(testDirectory));
    }

    @Test
    public void notifiesNotificationListenerWhenErrorOccurs() throws Exception {
        RuntimeException error = new RuntimeException();
        FileWatcherNotificationHandler notificationHandler = aNotificationHandler();
        doThrow(error).when(notificationHandler).handleFileWatcherEvent(eq(CREATED), eq(testDirectory), anyString(), anyBoolean());

        fileWatcher = new FileTreeWatcher(testDirectory, newHashSet(), notificationHandler);
        fileWatcher.startup();

        Thread.sleep(500);
        fileWatcherTestTree.createFile("");
        Thread.sleep(5000);

        verify(notificationHandler, timeout(10000)).errorOccurred(eq(testDirectory), eq(error));
    }

    private FileWatcherNotificationHandler aNotificationHandler() {
        return mock(FileWatcherNotificationHandler.class);
    }
}
