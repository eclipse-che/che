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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ZipArchiverTest {
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
                                                               .collect(toMap(f -> getZipEntryName(folder, f),
                                                                              this::readContentUnchecked));

        new ZipArchiver(folder).compress(compressedFolder);
        assertThatZipArchiveContainsAllEntries(new ByteArrayInputStream(compressedFolder.toByteArray()), entries);
    }

    @Test
    public void compressesFolderToArchiveWithFilter() throws Exception {
        VirtualFile folder = createFileTreeForArchiving();
        ByteArrayOutputStream compressedFolder = new ByteArrayOutputStream();
        Map<String, String> entries = getFileTreeAsList(folder).stream()
                                                               .filter(f -> f.isFolder() || f.getName().equals("_a.txt"))
                                                               .collect(toMap(f -> getZipEntryName(folder, f),
                                                                              this::readContentUnchecked));

        new ZipArchiver(folder).compress(compressedFolder, f -> f.isFolder() || f.getName().equals("_a.txt"));
        assertThatZipArchiveContainsAllEntries(new ByteArrayInputStream(compressedFolder.toByteArray()), entries);
    }

    @Test
    public void extractsArchiveToFolder() throws Exception {
        byte[] archive = createTestZipArchive();
        VirtualFile folder = vfsRoot.createFolder("folder");
        new ZipArchiver(folder).extract(new ByteArrayInputStream(archive), false, 0);

        Map<String, String> entries = getFileTreeAsList(folder).stream()
                                                               .collect(toMap(f -> getZipEntryName(folder, f),
                                                                              this::readContentUnchecked));

        assertEquals(readArchiveEntries(new ByteArrayInputStream(archive)), entries);
    }

    @Test
    public void extractsArchiveToFolderAndSkipsRootFolderFromArchive() throws Exception {
        byte[] archive = createTestZipArchive();
        VirtualFile folder = vfsRoot.createFolder("folder");
        new ZipArchiver(folder).extract(new ByteArrayInputStream(archive), false, 1);

        Map<String, String> entries = getFileTreeAsList(folder).stream()
                                                               .collect(toMap(f -> getZipEntryName(folder, f),
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
        byte[] archive = createTestZipArchive();
        VirtualFile folder = vfsRoot.createFolder("folder");
        VirtualFile arc = folder.createFolder("arc");
        VirtualFile fileOne = arc.createFolder("a").createFile("_a.txt", "xxx");
        VirtualFile fileTwo = arc.createFolder("b").createFile("_b.txt", "zzz");
        new ZipArchiver(folder).extract(new ByteArrayInputStream(archive), true, 0);

        Map<String, String> entries = getFileTreeAsList(folder).stream()
                                                               .collect(toMap(f -> getZipEntryName(folder, f),
                                                                              this::readContentUnchecked));

        assertEquals(readArchiveEntries(new ByteArrayInputStream(archive)), entries);
        assertEquals(TEST_CONTENT, fileOne.getContentAsString());
        assertEquals(TEST_CONTENT, fileTwo.getContentAsString());
    }

    @Test
    public void failsExtractArchiveToFolderWhenItContainsItemWithSameNameAndOverwritingIsDisabled() throws Exception {
        byte[] archive = createTestZipArchive();
        VirtualFile folder = vfsRoot.createFolder("folder");
        VirtualFile arc = folder.createFolder("arc");
        VirtualFile lockedFile = arc.createFolder("a").createFile("_a.txt", "xxx");

        try {
            new ZipArchiver(folder).extract(new ByteArrayInputStream(archive), false, 0);
            thrown.expect(ConflictException.class);
        } catch (ConflictException expected) {
            assertEquals("xxx", lockedFile.getContentAsString());
        }
    }

    @Test
    public void failsExtractArchiveToFolderWhenItContainsLockedFile() throws Exception {
        byte[] archive = createTestZipArchive();
        VirtualFile folder = vfsRoot.createFolder("folder");
        VirtualFile arc = folder.createFolder("arc");
        VirtualFile lockedFile = arc.createFolder("a").createFile("_a.txt", "xxx");
        lockedFile.lock(0);

        try {
            new ZipArchiver(folder).extract(new ByteArrayInputStream(archive), true, 0);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException expected) {
            assertEquals("xxx", lockedFile.getContentAsString());
        }
    }

    private Map<String, String> readArchiveEntries(InputStream archive) throws Exception {
        Map<String, String> entries = newHashMap();
        try (ZipInputStream zip = new ZipInputStream(archive)) {
            ZipEntry zipEntry;
            while ((zipEntry = zip.getNextEntry()) != null) {
                String name = zipEntry.getName();
                String content = zipEntry.isDirectory() ? "<none>" : new String(ByteStreams.toByteArray(zip));
                zip.closeEntry();
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

    private String getZipEntryName(VirtualFile folderForArchiving, VirtualFile archiveItem) {
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

    private byte[] createTestZipArchive() throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(byteOut);
        zipOut.putNextEntry(new ZipEntry("arc/"));

        zipOut.putNextEntry(new ZipEntry("arc/a/"));
        zipOut.putNextEntry(new ZipEntry("arc/a/_a.txt"));
        zipOut.write(TEST_CONTENT_BYTES);

        zipOut.putNextEntry(new ZipEntry("arc/b/"));
        zipOut.putNextEntry(new ZipEntry("arc/b/_b.txt"));
        zipOut.write(TEST_CONTENT_BYTES);

        zipOut.putNextEntry(new ZipEntry("arc/c/"));
        zipOut.putNextEntry(new ZipEntry("arc/c/_c.txt"));
        zipOut.write(TEST_CONTENT_BYTES);

        zipOut.close();
        return byteOut.toByteArray();
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

    private void assertThatZipArchiveContainsAllEntries(InputStream in, Map<String, String> entries) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(in)) {
            ZipEntry zipEntry;
            while ((zipEntry = zip.getNextEntry()) != null) {
                String name = zipEntry.getName();
                assertTrue(String.format("Unexpected entry %s in zip", name), entries.containsKey(name));
                if (!zipEntry.isDirectory()) {
                    String content = new String(ByteStreams.toByteArray(zip));
                    assertEquals(String.format("Invalid content of file %s", name), entries.get(name), content);
                }
                entries.remove(name);
                zip.closeEntry();
            }
        }
        assertTrue(String.format("Expected but were not found in zip %s", entries), entries.isEmpty());
    }
}