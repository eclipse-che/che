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
package org.eclipse.che.api.vfs;

import com.google.common.io.ByteStreams;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.vfs.impl.file.LocalVirtualFileSystem;
import org.eclipse.che.api.vfs.search.Searcher;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TarArchiverTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String TEST_CONTENT       = "___TEST___";
    private static final byte[] TEST_CONTENT_BYTES = TEST_CONTENT.getBytes();

    private File        testDirectory;
    private VirtualFile vfsRoot;

    @Before
    public void setUp() throws Exception {
        File targetDir = new File(Thread.currentThread().getContextClassLoader().getResource(".").getPath()).getParentFile();
        testDirectory = new File(targetDir, NameGenerator.generate("fs-", 4));
        assertTrue(testDirectory.mkdir());

        SearcherProvider searcherProvider = mock(SearcherProvider.class);
        Searcher searcher = mock(Searcher.class);
        VirtualFileSystem virtualFileSystem = new LocalVirtualFileSystem(testDirectory,
                                                                         mock(ArchiverFactory.class),
                                                                         searcherProvider,
                                                                         mock(AbstractVirtualFileSystemProvider.CloseCallback.class));
        when(searcherProvider.getSearcher(eq(virtualFileSystem), eq(true))).thenReturn(searcher);
        when(searcherProvider.getSearcher(eq(virtualFileSystem))).thenReturn(searcher);
        vfsRoot = virtualFileSystem.getRoot();
    }

    @After
    public void tearDown() throws Exception {
        IoUtil.deleteRecursive(testDirectory);
        FileCleaner.stop();
    }

    @Test
    public void compressesFolderToArchive() throws Exception {
        VirtualFile folder = createFileTreeForArchiving();
        ByteArrayOutputStream compressedFolder = new ByteArrayOutputStream();
        Map<String, String> entries = getFileTreeAsList(folder).stream()
                                                               .collect(toMap(f -> getTarEntryName(folder, f),
                                                                              this::readContentUnchecked));

        new TarArchiver(folder).compress(compressedFolder);
        assertThatTarArchiveContainsAllEntries(new ByteArrayInputStream(compressedFolder.toByteArray()), entries);
    }

    @Test
    public void compressesFolderToArchiveWithFilter() throws Exception {
        VirtualFile folder = createFileTreeForArchiving();
        ByteArrayOutputStream compressedFolder = new ByteArrayOutputStream();
        Map<String, String> entries = getFileTreeAsList(folder).stream()
                                                               .filter(f -> f.isFolder() || f.getName().equals("_a.txt"))
                                                               .collect(toMap(f -> getTarEntryName(folder, f),
                                                                              this::readContentUnchecked));

        new TarArchiver(folder).compress(compressedFolder, f -> f.isFolder() || f.getName().equals("_a.txt"));
        assertThatTarArchiveContainsAllEntries(new ByteArrayInputStream(compressedFolder.toByteArray()), entries);
    }

    @Test
    public void extractsArchiveToFolder() throws Exception {
        byte[] archive = createTestTarArchive();
        VirtualFile folder = vfsRoot.createFolder("folder");
        new TarArchiver(folder).extract(new ByteArrayInputStream(archive), false, 0);

        Map<String, String> entries = getFileTreeAsList(folder).stream()
                                                               .collect(toMap(f -> getTarEntryName(folder, f),
                                                                              this::readContentUnchecked));

        assertEquals(readArchiveEntries(new ByteArrayInputStream(archive)), entries);
    }

    @Test
    public void extractsArchiveToFolderAndSkipsRootFolderFromArchive() throws Exception {
        byte[] archive = createTestTarArchive();
        VirtualFile folder = vfsRoot.createFolder("folder");
        new TarArchiver(folder).extract(new ByteArrayInputStream(archive), false, 1);

        Map<String, String> entries = getFileTreeAsList(folder).stream()
                                                               .collect(toMap(f -> getTarEntryName(folder, f),
                                                                              this::readContentUnchecked));

        Map<String, String> originalArchiveEntriesWithoutFirstPathSegment =
                readArchiveEntries(new ByteArrayInputStream(archive)).entrySet().stream()
                                                                     .filter(e -> !"arc/".equals(e.getKey()))
                                                                     .collect(toMap(e -> e.getKey().replace("arc/", ""),
                                                                                    Map.Entry::getValue));
        assertEquals(originalArchiveEntriesWithoutFirstPathSegment, entries);
    }

    @Test
    public void extractsArchiveToFolderAndOverwriteExistedFiles() throws Exception {
        byte[] archive = createTestTarArchive();
        VirtualFile folder = vfsRoot.createFolder("folder");
        VirtualFile arc = folder.createFolder("arc");
        VirtualFile fileOne = arc.createFolder("a").createFile("_a.txt", "xxx");
        VirtualFile fileTwo = arc.createFolder("b").createFile("_b.txt", "zzz");
        new TarArchiver(folder).extract(new ByteArrayInputStream(archive), true, 0);

        Map<String, String> entries = getFileTreeAsList(folder).stream()
                                                               .collect(toMap(f -> getTarEntryName(folder, f),
                                                                              this::readContentUnchecked));

        assertEquals(readArchiveEntries(new ByteArrayInputStream(archive)), entries);
        assertEquals(TEST_CONTENT, fileOne.getContentAsString());
        assertEquals(TEST_CONTENT, fileTwo.getContentAsString());
    }

    @Test
    public void failsExtractArchiveToFolderWhenItContainsItemWithSameNameAndOverwritingIsDisabled() throws Exception {
        byte[] archive = createTestTarArchive();
        VirtualFile folder = vfsRoot.createFolder("folder");
        VirtualFile arc = folder.createFolder("arc");
        VirtualFile lockedFile = arc.createFolder("a").createFile("_a.txt", "xxx");

        try {
            new TarArchiver(folder).extract(new ByteArrayInputStream(archive), false, 0);
            thrown.expect(ConflictException.class);
        } catch (ConflictException expected) {
            assertEquals("xxx", lockedFile.getContentAsString());
        }
    }

    @Test
    public void failsExtractArchiveToFolderWhenItContainsLockedFile() throws Exception {
        byte[] archive = createTestTarArchive();
        VirtualFile folder = vfsRoot.createFolder("folder");
        VirtualFile arc = folder.createFolder("arc");
        VirtualFile lockedFile = arc.createFolder("a").createFile("_a.txt", "xxx");
        lockedFile.lock(0);

        try {
            new TarArchiver(folder).extract(new ByteArrayInputStream(archive), true, 0);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException expected) {
            assertEquals("xxx", lockedFile.getContentAsString());
        }
    }

    private Map<String, String> readArchiveEntries(InputStream archive) throws Exception {
        Map<String, String> entries = newHashMap();
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(archive)) {
            TarArchiveEntry tarArchiveEntry;
            while ((tarArchiveEntry = tarIn.getNextTarEntry()) != null) {
                String name = tarArchiveEntry.getName();
                String content = tarArchiveEntry.isDirectory() ? "<none>" : new String(ByteStreams.toByteArray(tarIn));
                entries.put(name, content);
            }
        }
        return entries;
    }

    private String readContentUnchecked(VirtualFile virtualFile) {
        if (virtualFile.isFolder()) {
            return "<none>";
        }
        try {
            return virtualFile.getContentAsString();
        } catch (ForbiddenException | ServerException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTarEntryName(VirtualFile folderForArchiving, VirtualFile archiveItem) {
        String entryName = archiveItem.getPath().subPath(folderForArchiving.getPath()).toString();
        if (archiveItem.isFolder()) {
            entryName += "/";
        }
        return entryName;
    }

    private VirtualFile createFileTreeForArchiving() throws Exception {
        VirtualFile arc = vfsRoot.createFolder("arc");
        arc.createFolder("a").createFile("_a.txt", TEST_CONTENT);
        arc.createFolder("b").createFile("_b.txt", TEST_CONTENT);
        arc.createFolder("c").createFile("_c.txt", TEST_CONTENT);
        return arc;
    }

    private byte[] createTestTarArchive() throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(byteOut);
        addDirectoryEntry(tarOut, new TarArchiveEntry("arc/"));
        addDirectoryEntry(tarOut, new TarArchiveEntry("arc/a/"));
        addFileEntry(tarOut, "arc/a/_a.txt");
        addDirectoryEntry(tarOut, new TarArchiveEntry("arc/b/"));
        addFileEntry(tarOut, "arc/b/_b.txt");
        addDirectoryEntry(tarOut, new TarArchiveEntry("arc/c/"));
        addFileEntry(tarOut, "arc/c/_c.txt");
        tarOut.close();
        return byteOut.toByteArray();
    }

    private void addDirectoryEntry(TarArchiveOutputStream tarOut, TarArchiveEntry archiveEntry) throws IOException {
        tarOut.putArchiveEntry(archiveEntry);
        tarOut.closeArchiveEntry();
    }

    private void addFileEntry(TarArchiveOutputStream tarOut, String name) throws IOException {
        TarArchiveEntry entryA = new TarArchiveEntry(name);
        entryA.setSize(TEST_CONTENT_BYTES.length);
        tarOut.putArchiveEntry(entryA);
        tarOut.write(TEST_CONTENT_BYTES);
        tarOut.closeArchiveEntry();
    }

    private List<VirtualFile> getFileTreeAsList(VirtualFile rootOfTree) throws Exception {
        List<VirtualFile> list = newArrayList();

        VirtualFileVisitor treeWalker = new VirtualFileVisitor() {
            @Override
            public void visit(VirtualFile virtualFile) throws ServerException {
                list.add(virtualFile);
                if (virtualFile.isFolder()) {
                    for (VirtualFile child : virtualFile.getChildren()) {
                        child.accept(this);
                    }
                }
            }
        };

        for (VirtualFile child : rootOfTree.getChildren()) {
            child.accept(treeWalker);
        }

        return list;
    }

    private void assertThatTarArchiveContainsAllEntries(InputStream in, Map<String, String> entries) throws Exception {
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(in)) {
            TarArchiveEntry tarArchiveEntry;
            while ((tarArchiveEntry = tarIn.getNextTarEntry()) != null) {
                String name = tarArchiveEntry.getName();
                assertTrue(String.format("Unexpected entry %s in TAR", name), entries.containsKey(name));
                if (!tarArchiveEntry.isDirectory()) {
                    String content = new String(ByteStreams.toByteArray(tarIn));
                    assertEquals(String.format("Invalid content of file %s", name), entries.get(name), content);
                }
                entries.remove(name);
            }
        }
        assertTrue(String.format("Expected but were not found in TAR %s", entries), entries.isEmpty());
    }
}