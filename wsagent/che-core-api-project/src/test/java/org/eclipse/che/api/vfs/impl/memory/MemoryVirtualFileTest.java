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
package org.eclipse.che.api.vfs.impl.memory;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.vfs.Archiver;
import org.eclipse.che.api.vfs.ArchiverFactory;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileVisitor;
import org.eclipse.che.api.vfs.search.Searcher;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MemoryVirtualFileTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final String DEFAULT_CONTENT       = "__TEST__";
    private final byte[] DEFAULT_CONTENT_BYTES = DEFAULT_CONTENT.getBytes();

    private Searcher                searcher;
    private ArchiverFactory         archiverFactory;
    private MemoryVirtualFileSystem fileSystem;

    @Before
    public void setUp() throws Exception {
        archiverFactory = mock(ArchiverFactory.class);
        SearcherProvider searcherProvider = mock(SearcherProvider.class);
        fileSystem = new MemoryVirtualFileSystem(archiverFactory, searcherProvider);
        searcher = mock(Searcher.class);
        when(searcherProvider.getSearcher(eq(fileSystem), eq(true))).thenReturn(searcher);
        when(searcherProvider.getSearcher(eq(fileSystem))).thenReturn(searcher);
    }

    @Test
    public void getsName() throws Exception {
        String name = generateFileName();
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(name, DEFAULT_CONTENT);

        assertEquals(name, file.getName());
    }

    @Test
    public void getsPath() throws Exception {
        String name = generateFileName();
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(name, DEFAULT_CONTENT);

        assertEquals("/" + file.getName(), file.getPath().toString());
    }

    @Test
    public void getsRootPath() throws Exception {
        VirtualFile root = getRoot();
        assertEquals("/", root.getPath().toString());
    }

    @Test
    public void checksIsFile() throws Exception {
        VirtualFile root = getRoot();

        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        assertTrue(file.isFile());

        VirtualFile folder = root.createFolder(generateFolderName());
        assertFalse(folder.isFile());
    }

    @Test
    public void checksIsFolder() throws Exception {
        VirtualFile root = getRoot();

        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        assertFalse(file.isFolder());

        VirtualFile folder = root.createFolder(generateFolderName());
        assertTrue(folder.isFolder());
    }

    @Test
    public void checksFileExistence() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);

        assertTrue(file.exists());
    }

    @Test
    public void checksDeletedFileExistence() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);

        file.delete();

        assertFalse(file.exists());
    }

    @Test
    public void checksIsRoot() throws Exception {
        VirtualFile root = getRoot();

        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        VirtualFile folder = root.createFolder(generateFolderName());

        assertFalse(file.isRoot());
        assertFalse(folder.isRoot());
        assertTrue(root.isRoot());
    }

    @Test
    public void getsLastModificationDate() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        long beforeUpdate = file.getLastModificationDate();
        Thread.sleep(1000);

        file.updateContent("updated content");

        long afterUpdate = file.getLastModificationDate();
        assertTrue(afterUpdate > beforeUpdate);
    }

    @Test
    public void getsParent() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);

        assertEquals(root, file.getParent());
    }

    @Test
    public void getsRootParent() throws Exception {
        VirtualFile root = getRoot();
        assertNull(root.getParent());
    }

    @Test
    public void getsEmptyPropertiesMapIfFileDoesNotHaveProperties() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);

        assertTrue(file.getProperties().isEmpty());
    }

    @Test
    public void getsPropertiesMap() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), "");
        Map<String, String> properties = ImmutableMap.of("property1", "value1", "property2", "value2");
        file.updateProperties(properties);
        assertEquals(properties, file.getProperties());
    }

    @Test
    public void getsProperty() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), "");
        file.updateProperties(ImmutableMap.of("property1", "value1"));

        assertEquals("value1", file.getProperty("property1"));
    }

    @Test
    public void updatesProperties() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), "");
        file.updateProperties(ImmutableMap.of("property1", "valueX",
                                              "new property1", "value3"));

        Map<String, String> expected = ImmutableMap.of("property1", "valueX",
                                                       "new property1", "value3");
        assertEquals(expected, file.getProperties());
    }

    @Test
    public void setsProperty() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), "");

        file.setProperty("property1", "value1");

        Map<String, String> expected = ImmutableMap.of("property1", "value1");
        assertEquals(expected, file.getProperties());
    }

    @Test
    public void removesPropertyBySetValueToNull() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), "");
        file.setProperty("property1", "value1");
        Map<String, String> expected = ImmutableMap.of("property1", "value1");
        assertEquals(expected, file.getProperties());

        file.setProperty("property1", null);

        assertTrue(file.getProperties().isEmpty());
    }

    @Test
    public void acceptsVisitor() throws Exception {
        VirtualFile root = getRoot();
        boolean[] visitedFlag = new boolean[]{false};
        VirtualFileVisitor visitor = virtualFile -> {
            assertSame(root, virtualFile);
            visitedFlag[0] = true;
        };
        root.accept(visitor);
        assertTrue("visit(VirtualFile) method was not invoked", visitedFlag[0]);
    }

    @Test
    public void countsMd5Sums() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile folder = root.createFolder(generateFolderName());
        VirtualFile file1 = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        VirtualFile file2 = folder.createFile(generateFileName(), "xxx");
        root.createFolder(generateFolderName());
        Set<Pair<String, String>> expected = newHashSet(Pair.of(countMd5Sum(file1), file1.getPath().subPath(folder.getPath()).toString()),
                                                        Pair.of(countMd5Sum(file2), file2.getPath().subPath(folder.getPath()).toString()));

        assertEquals(expected, newHashSet(folder.countMd5Sums()));
    }

    @Test
    public void returnsEmptyListWhenCountMd5SumsOnFile() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);

        assertTrue(file.countMd5Sums().isEmpty());
    }

    @Test
    public void getsChildren() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile folder = root.createFolder(generateFolderName());
        VirtualFile file1 = root.createFile(generateFileName(), DEFAULT_CONTENT);
        VirtualFile file2 = root.createFile(generateFileName(), DEFAULT_CONTENT);

        List<VirtualFile> expectedResult = newArrayList(file1, file2, folder);
        Collections.sort(expectedResult);

        assertEquals(expectedResult, root.getChildren());
    }

    @Test
    public void getsChildrenWithFilter() throws Exception {
        VirtualFile root = getRoot();
        root.createFolder(generateFolderName());
        VirtualFile file1 = root.createFile(generateFileName(), DEFAULT_CONTENT);
        VirtualFile file2 = root.createFile(generateFileName(), DEFAULT_CONTENT);

        List<VirtualFile> expectedResult = newArrayList(file1, file2);
        Collections.sort(expectedResult);

        List<VirtualFile> children = root.getChildren(file -> file.equals(file1) || file.equals(file2));

        assertEquals(expectedResult, children);
    }

    @Test
    public void getsChild() throws Exception {
        VirtualFile root = getRoot();
        String name = generateFileName();
        VirtualFile file = root.createFile(name, DEFAULT_CONTENT);

        assertEquals(file, root.getChild(Path.of(name)));
    }

    @Test
    public void getsChildByHierarchicalPath() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile folder = root.createFolder("a/b/c/d");
        String name = generateFileName();
        VirtualFile file = folder.createFile(name, DEFAULT_CONTENT);

        assertEquals(file, root.getChild(folder.getPath().newPath(name)));
    }

    @Test
    public void getsContentAsStream() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);

        byte[] bytes;
        try (InputStream content = file.getContent()) {
            bytes = ByteStreams.toByteArray(content);
        }

        assertEquals(DEFAULT_CONTENT, new String(bytes));
    }

    @Test
    public void getsContentAsBytes() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);

        byte[] content = file.getContentAsBytes();

        assertEquals(DEFAULT_CONTENT, new String(content));
    }

    @Test
    public void getsContentAsString() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);

        String content = file.getContentAsString();

        assertEquals(DEFAULT_CONTENT, content);
    }

    @Test
    public void failsGetContentOfFolderAsStream() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile folder = root.createFolder(generateFolderName());

        thrown.expect(ForbiddenException.class);

        folder.getContent();
    }

    @Test
    public void failsGetContentOfFolderAsBytes() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile folder = root.createFolder(generateFolderName());

        thrown.expect(ForbiddenException.class);

        folder.getContentAsBytes();
    }

    @Test
    public void failsGetContentOfFolderAsString() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile folder = root.createFolder(generateFolderName());

        thrown.expect(ForbiddenException.class);

        folder.getContentAsString();
    }

    @Test
    public void updatesContentByStream() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);

        file.updateContent(new ByteArrayInputStream("updated content".getBytes()));

        assertEquals("updated content", file.getContentAsString());
    }

    @Test
    public void updatesContentByBytes() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);

        file.updateContent("updated content".getBytes());

        assertEquals("updated content", file.getContentAsString());
    }

    @Test
    public void updatesContentByString() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);

        file.updateContent("updated content");

        assertEquals("updated content", file.getContentAsString());
    }

    @Test
    public void updatesContentOfLockedFileByStreamWithLockToken() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        String lockToken = file.lock(0);

        file.updateContent(new ByteArrayInputStream("updated content".getBytes()), lockToken);

        assertEquals("updated content", file.getContentAsString());
    }

    @Test
    public void updatesContentOfLockedFileByBytesWithLockToken() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        String lockToken = file.lock(0);

        file.updateContent("updated content".getBytes(), lockToken);

        assertEquals("updated content", file.getContentAsString());
    }

    @Test
    public void updatesContentOfLockedFileByStringWithLockToken() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        String lockToken = file.lock(0);

        file.updateContent("updated content", lockToken);

        assertEquals("updated content", file.getContentAsString());
    }

    @Test
    public void failsUpdateContentOfLockedFileByStreamWithoutLockToken() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        file.lock(0);

        try {
            file.updateContent(new ByteArrayInputStream("updated content".getBytes()));
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException expected) {
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void failsUpdateContentOfLockedFileByBytesWithoutLockToken() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        file.lock(0);

        try {
            file.updateContent("updated content".getBytes());
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException expected) {
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void failsUpdateContentOfLockedFileByStringWithoutLockToken() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        file.lock(0);

        try {
            file.updateContent("updated content");
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException expected) {
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void failsUpdateContentOfLockedFileByStreamWhenLockTokenIsInvalid() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        String invalidLockToken = invalidateLockToken(file.lock(0));

        try {
            file.updateContent(new ByteArrayInputStream("updated content".getBytes()), invalidLockToken);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException expected) {
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void failsUpdateContentOfLockedFileByBytesWhenLockTokenIsInvalid() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        String invalidLockToken = invalidateLockToken(file.lock(0));

        try {
            file.updateContent("updated content".getBytes(), invalidLockToken);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException expected) {
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void failsUpdateContentOfLockedFileByStringWhenLockTokenIsInvalid() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        String invalidLockToken = invalidateLockToken(file.lock(0));

        try {
            file.updateContent("updated content", invalidLockToken);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException expected) {
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void getsFileContentLength() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        assertEquals(DEFAULT_CONTENT_BYTES.length, file.getLength());
    }

    @Test
    public void folderContentLengthIsZero() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile folder = root.createFolder(generateFolderName());
        assertEquals(0, folder.getLength());
    }

    @Test
    public void copiesFile() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        VirtualFile targetFolder = root.createFolder(generateFolderName());

        VirtualFile copy = file.copyTo(targetFolder);

        assertEquals(file.getContentAsString(), copy.getContentAsString());
        assertEquals(file.getProperties(), copy.getProperties());
    }

    @Test
    public void copiesLockedFile() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        file.lock(0);
        VirtualFile targetFolder = root.createFolder(generateFolderName());

        VirtualFile copy = file.copyTo(targetFolder);

        assertFalse(copy.isLocked());
        assertEquals(file.getContentAsString(), copy.getContentAsString());
        assertEquals(file.getProperties(), copy.getProperties());
    }

    @Test
    public void copiesFileUnderNewName() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        VirtualFile targetFolder = root.createFolder(generateFolderName());

        VirtualFile copy = file.copyTo(targetFolder, "new name", false);

        assertEquals(file.getContentAsString(), copy.getContentAsString());
        assertEquals(file.getProperties(), copy.getProperties());
    }

    @Test
    public void copiesFileUnderNewNameAndOverwritesExistedFile() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        VirtualFile targetFolder = root.createFolder(generateFolderName());
        targetFolder.createFile("existed_name", "existed content");

        VirtualFile copy = file.copyTo(targetFolder, "existed_name", true);

        assertEquals(file.getContentAsString(), copy.getContentAsString());
        assertEquals(file.getProperties(), copy.getProperties());
    }

    @Test
    public void failsCopyFileWhenItemWithTheSameNameExistsInTargetFolderAndOverwritingIsDisabled() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        VirtualFile targetFolder = root.createFolder(generateFolderName());
        VirtualFile conflictFile = targetFolder.createFile("existed_name", "xxx");

        try {
            file.copyTo(targetFolder, "existed_name", false);
            thrown.expect(ConflictException.class);
        } catch (ConflictException e) {
            assertEquals("xxx", conflictFile.getContentAsString());
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void copiesFolder() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        createFileTree(folder, 3);
        List<VirtualFile> originalTree = getFileTreeAsList(folder);
        for (int i = 0; i < originalTree.size(); i++) {
            originalTree.get(i).setProperty("property" + i, "value" + 1);
        }

        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());

        VirtualFile copiedFolder = folder.copyTo(targetFolder);

        List<VirtualFile> copiedTree = getFileTreeAsList(copiedFolder);

        Iterator<VirtualFile> originalIterator = originalTree.iterator();
        Iterator<VirtualFile> copiedIterator = copiedTree.iterator();
        while (originalIterator.hasNext() && copiedIterator.hasNext()) {
            VirtualFile original = originalIterator.next();
            VirtualFile copy = copiedIterator.next();
            assertEquals(String.format("Properties of virtual file %s not copied properly", copy.getPath()),
                         original.getProperties(), copy.getProperties());
            if (original.isFile()) {
                assertEquals(String.format("Content of file %s not copied properly", copy.getPath()),
                             original.getContentAsString(), copy.getContentAsString());
            }
        }
        assertFalse(originalIterator.hasNext() || copiedIterator.hasNext());
    }

    @Test
    public void copiesFolderThatContainsLockedFile() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        file.lock(0);

        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());

        VirtualFile copiedFolder = folder.copyTo(targetFolder);
        assertNotNull(getRoot().getChild(copiedFolder.getPath()));

        VirtualFile copiedFile = copiedFolder.getChild(Path.of(file.getName()));
        assertFalse(copiedFile.isLocked());
        assertEquals(file.getContentAsString(), copiedFile.getContentAsString());
    }

    @Test
    public void copiesFolderUnderNewName() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);

        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());

        VirtualFile copiedFolder = folder.copyTo(targetFolder, "new_name", false);
        assertNotNull(getRoot().getChild(copiedFolder.getPath()));

        VirtualFile copiedFile = copiedFolder.getChild(Path.of(file.getName()));
        assertEquals(file.getContentAsString(), copiedFile.getContentAsString());
    }

    @Test
    public void copiesFolderAndReplaceExistedItem() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);

        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        targetFolder.createFolder(folder.getName());

        VirtualFile copiedFolder = folder.copyTo(targetFolder, null, true);
        assertNotNull(getRoot().getChild(copiedFolder.getPath()));

        VirtualFile copiedFile = copiedFolder.getChild(Path.of(file.getName()));
        assertEquals(file.getContentAsString(), copiedFile.getContentAsString());
    }

    @Test
    public void copiesFolderUnderNewNameAndReplaceExistedItem() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);

        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        targetFolder.createFolder("new_name");

        VirtualFile copiedFolder = folder.copyTo(targetFolder, "new_name", true);
        assertNotNull(getRoot().getChild(copiedFolder.getPath()));

        VirtualFile copiedFile = copiedFolder.getChild(Path.of(file.getName()));
        assertEquals(file.getContentAsString(), copiedFile.getContentAsString());
    }

    @Test
    public void failsCopyFolderWhenTargetFolderContainsItemWithSameNameAndOverwritingIsDisabled() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);

        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        VirtualFile conflictFolder = targetFolder.createFolder(folder.getName());

        try {
            folder.copyTo(targetFolder);
            thrown.expect(ConflictException.class);
        } catch (ConflictException expected) {
            assertNull(getRoot().getChild(conflictFolder.getPath().newPath(file.getName())));
        }
    }

    @Test
    public void failsCopyFolderUnderNewNameWhenTargetFolderContainsItemWithSameNameAndOverwritingIsDisabled() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);

        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        VirtualFile conflictFolder = targetFolder.createFolder("new_name");

        try {
            folder.copyTo(targetFolder, "new_name", false);
            thrown.expect(ConflictException.class);
        } catch (ConflictException expected) {
            assertNull(getRoot().getChild(conflictFolder.getPath().newPath(file.getName())));
        }
    }

    @Test
    public void failsCopyFolderWhenTargetFolderNeedBeOverwrittenButContainsLockedFile() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);

        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        VirtualFile conflictFolder = targetFolder.createFolder(folder.getName());
        VirtualFile lockedFileInConflictFolder = conflictFolder.createFile(generateFileName(), "xxx");
        lockedFileInConflictFolder.lock(0);

        try {
            folder.copyTo(targetFolder, null, true);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException expected) {
            assertNull(getRoot().getChild(conflictFolder.getPath().newPath(file.getName())));
            assertEquals("xxx", lockedFileInConflictFolder.getContentAsString());
        }
    }

    @Test
    public void movesFile() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        Path filePath = file.getPath();
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());

        VirtualFile movedFile = file.moveTo(targetFolder);

        assertEquals(movedFile, getRoot().getChild(movedFile.getPath()));
        assertEquals(ImmutableMap.of("property1", "value1"), movedFile.getProperties());
        assertEquals(DEFAULT_CONTENT, movedFile.getContentAsString());

        assertNull(getRoot().getChild(filePath));
    }

    @Test
    public void movesFileUnderNewName() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        Path filePath = file.getPath();
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());

        VirtualFile movedFile = file.moveTo(targetFolder, "new_name", false, null);

        assertEquals(movedFile, getRoot().getChild(movedFile.getPath()));
        assertEquals(ImmutableMap.of("property1", "value1"), movedFile.getProperties());
        assertEquals(DEFAULT_CONTENT, movedFile.getContentAsString());

        assertNull(getRoot().getChild(filePath));
    }

    @Test
    public void movesFileUnderNewNameAndOverwriteExistedFile() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        Path filePath = file.getPath();
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        targetFolder.createFolder("new_name");

        VirtualFile movedFile = file.moveTo(targetFolder, "new_name", true, null);

        assertEquals(movedFile, getRoot().getChild(movedFile.getPath()));
        assertEquals(ImmutableMap.of("property1", "value1"), movedFile.getProperties());
        assertEquals(DEFAULT_CONTENT, movedFile.getContentAsString());

        assertNull(getRoot().getChild(filePath));
    }

    @Test
    public void movesLockedFileWithLockToken() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        String lockToken = file.lock(0);
        Path filePath = file.getPath();
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());

        VirtualFile movedFile = file.moveTo(targetFolder, null, false, lockToken);

        assertEquals(movedFile, getRoot().getChild(movedFile.getPath()));
        assertFalse(movedFile.isLocked());
        assertEquals(ImmutableMap.of("property1", "value1"), movedFile.getProperties());
        assertEquals(DEFAULT_CONTENT, movedFile.getContentAsString());

        assertNull(getRoot().getChild(filePath));
    }

    @Test
    public void failsMoveLockedFileWithoutLockToken() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        Path filePath = file.getPath();
        file.lock(0);
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        Path movedFilePath = targetFolder.getPath().newPath(file.getName());

        try {
            file.moveTo(targetFolder);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertNull(getRoot().getChild(movedFilePath));

            assertEquals(file, getRoot().getChild(filePath));
            assertEquals(ImmutableMap.of("property1", "value1"), file.getProperties());
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void failsMoveLockedFileWhenLockTokenIsInvalid() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        Path filePath = file.getPath();
        String invalidLockToken = invalidateLockToken(file.lock(0));
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        Path movedFilePath = targetFolder.getPath().newPath(file.getName());

        try {
            file.moveTo(targetFolder, null, false, invalidLockToken);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertNull(getRoot().getChild(movedFilePath));

            assertTrue(file.isLocked());
            assertEquals(file, getRoot().getChild(filePath));
            assertEquals(ImmutableMap.of("property1", "value1"), file.getProperties());
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void failsMoveFileWhenTargetFolderContainsItemWithTheSameNameAndOverwritingIsDisabled() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        Path filePath = file.getPath();
        VirtualFile existedFile = targetFolder.createFile("existed_name", "existed content");

        try {
            file.moveTo(targetFolder, "existed_name", false, null);
            thrown.expect(ConflictException.class);
        } catch (ConflictException e) {
            assertEquals(file, getRoot().getChild(filePath));
            assertEquals("existed content", existedFile.getContentAsString());
        }
    }

    @Test
    public void movesFolder() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        createFileTree(folder, 3);
        List<VirtualFile> originalTree = getFileTreeAsList(folder);
        for (int i = 0; i < originalTree.size(); i++) {
            originalTree.get(i).setProperty("property" + i, "value" + i);
        }
        List<Path> originalTreePaths = originalTree.stream().map(VirtualFile::getPath).collect(toList());
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());

        VirtualFile movedFolder = folder.moveTo(targetFolder);

        List<VirtualFile> movedTree = getFileTreeAsList(movedFolder);
        Iterator<Path> originalPathIterator = originalTreePaths.iterator();
        Iterator<VirtualFile> movedIterator = movedTree.iterator();
        int i = 0;
        while (originalPathIterator.hasNext() && movedIterator.hasNext()) {
            Path originalPath = originalPathIterator.next();
            VirtualFile moved = movedIterator.next();
            assertEquals(originalPath, moved.getPath().subPath(targetFolder.getPath()));

            assertEquals(String.format("Properties of virtual file %s not copied properly", moved.getPath()),
                         ImmutableMap.of("property" + i, "value" + i), moved.getProperties());
            if (moved.isFile()) {
                assertEquals(String.format("Content of file %s not copied properly", moved.getPath()),
                             DEFAULT_CONTENT, moved.getContentAsString());
            }
            assertNull(String.format("Item %s must ne removed", originalPath), getRoot().getChild(originalPath));
            i++;
        }
        assertFalse(originalPathIterator.hasNext() || movedIterator.hasNext());
    }

    @Test
    public void movesFolderUnderNewName() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        String fileName = file.getName();
        Path folderPath = folder.getPath();
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());

        VirtualFile movedFolder = folder.moveTo(targetFolder, "new_name", false, null);
        assertNotNull(getRoot().getChild(movedFolder.getPath()));

        VirtualFile movedFile = movedFolder.getChild(Path.of(file.getName()));
        assertEquals(DEFAULT_CONTENT, movedFile.getContentAsString());

        assertNull(getRoot().getChild(folderPath.newPath(fileName)));
        assertNull(getRoot().getChild(folderPath));
    }

    @Test
    public void movesFolderAndReplaceExistedItem() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        String fileName = file.getName();
        Path folderPath = folder.getPath();
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        targetFolder.createFolder(folder.getName());

        VirtualFile movedFolder = folder.moveTo(targetFolder, null, true, null);
        assertNotNull(getRoot().getChild(movedFolder.getPath()));

        VirtualFile movedFile = movedFolder.getChild(Path.of(file.getName()));
        assertEquals(DEFAULT_CONTENT, movedFile.getContentAsString());

        assertNull(getRoot().getChild(folderPath.newPath(fileName)));
        assertNull(getRoot().getChild(folderPath));
    }

    @Test
    public void movesFolderUnderNewNameAndReplaceExistedItem() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        String fileName = file.getName();
        Path folderPath = folder.getPath();
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        targetFolder.createFolder("new_name");

        VirtualFile movedFolder = folder.moveTo(targetFolder, "new_name", true, null);
        assertNotNull(getRoot().getChild(movedFolder.getPath()));

        VirtualFile movedFile = movedFolder.getChild(Path.of(fileName));
        assertEquals(DEFAULT_CONTENT, movedFile.getContentAsString());

        assertNull(getRoot().getChild(folderPath));
    }

    @Test
    public void failsMoveFolderWhenItContainsLockedFile() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile lockedFile = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        lockedFile.lock(0);
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        Path movedFolderPath = targetFolder.getPath().newPath(folder.getName());

        try {
            folder.moveTo(targetFolder);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertNull(getRoot().getChild(movedFolderPath.newPath(lockedFile.getName())));
            assertNull(getRoot().getChild(movedFolderPath));

            assertNotNull(getRoot().getChild(folder.getPath()));
            assertEquals(DEFAULT_CONTENT, lockedFile.getContentAsString());
        }
    }

    @Test
    public void failsMoveFolderWhenTargetFolderContainsItemWithTheSameNameAndOverwritingIsDisabled() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        VirtualFile conflictFolder = targetFolder.createFolder(folder.getName());

        try {
            folder.moveTo(targetFolder);
            thrown.expect(ConflictException.class);
        } catch (ConflictException expected) {
            assertNull(getRoot().getChild(conflictFolder.getPath().newPath(file.getName())));

            assertNotNull(getRoot().getChild(folder.getPath()));
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void failsMoveFolderUnderNewNameWhenTargetFolderContainsItemWithTheSameNameAndOverwritingIsDisabled() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        VirtualFile conflictFolder = targetFolder.createFolder("new_name");

        try {
            folder.moveTo(targetFolder, "new_name", false, null);
            thrown.expect(ConflictException.class);
        } catch (ConflictException expected) {
            assertNull(getRoot().getChild(conflictFolder.getPath().newPath(file.getName())));

            assertNotNull(getRoot().getChild(folder.getPath()));
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void failsMoveFolderWhenTargetFolderNeedBeOverwrittenButContainsLockedFile() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        VirtualFile targetFolder = getRoot().createFolder(generateFolderName());
        VirtualFile conflictFolder = targetFolder.createFolder(folder.getName());
        VirtualFile lockedFileInConflictFolder = conflictFolder.createFile(generateFileName(), "xxx");
        lockedFileInConflictFolder.lock(0);

        try {
            folder.moveTo(targetFolder, null, true, null);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException expected) {
            assertEquals("xxx", lockedFileInConflictFolder.getContentAsString());
            assertNull(getRoot().getChild(conflictFolder.getPath().newPath(file.getName())));

            assertNotNull(getRoot().getChild(folder.getPath()));
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void renamesFile() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        Path filePath = file.getPath();

        VirtualFile renamedFile = file.rename("new name");

        assertEquals(renamedFile, getRoot().getChild(renamedFile.getPath()));
        assertEquals(ImmutableMap.of("property1", "value1"), renamedFile.getProperties());
        assertEquals(DEFAULT_CONTENT, renamedFile.getContentAsString());

        assertNull(getRoot().getChild(filePath));
    }

    @Test
    public void renamesLockedFileWithLockToken() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        Path filePath = file.getPath();
        String lockToken = file.lock(0);

        VirtualFile renamedFile = file.rename("new name", lockToken);

        assertFalse(renamedFile.isLocked());
        assertEquals(renamedFile, getRoot().getChild(renamedFile.getPath()));
        assertEquals(DEFAULT_CONTENT, renamedFile.getContentAsString());

        assertNull(getRoot().getChild(filePath));
    }

    @Test
    public void failsRenameLockedFileWithoutLockToken() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        Path filePath = file.getPath();
        Path newPath = folder.getPath().newPath("new name");
        file.lock(0);

        try {
            file.rename("new name");
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertNull(getRoot().getChild(newPath));

            assertTrue(file.isLocked());
            assertEquals(file, getRoot().getChild(filePath));
            assertEquals(ImmutableMap.of("property1", "value1"), file.getProperties());
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void failsRenameLockedFileWhenLockTokenIsInvalid() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        Path filePath = file.getPath();
        Path newPath = folder.getPath().newPath("new name");
        String invalidLockToken = invalidateLockToken(file.lock(0));

        try {
            file.rename("new name", invalidLockToken);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertNull(getRoot().getChild(newPath));

            assertTrue(file.isLocked());
            assertEquals(file, getRoot().getChild(filePath));
            assertEquals(ImmutableMap.of("property1", "value1"), file.getProperties());
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void failsRenameFileWhenFolderContainsItemWithSameName() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        VirtualFile conflictFile = folder.createFile("existed_name", "xxx");
        conflictFile.setProperty("property2", "value2");

        try {
            file.rename("existed_name");
            thrown.expect(ConflictException.class);
        } catch (ConflictException e) {
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
            assertEquals(ImmutableMap.of("property1", "value1"), file.getProperties());

            assertEquals("xxx", conflictFile.getContentAsString());
            assertEquals(ImmutableMap.of("property2", "value2"), conflictFile.getProperties());
        }
    }

    @Test
    public void renamesFolder() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        Path folderPath = folder.getPath();
        folder.setProperty("property1", "value1");
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        String fileName = file.getName();
        file.setProperty("property2", "value2");

        VirtualFile renamed = folder.rename("new_name");


        assertNotNull(getRoot().getChild(renamed.getPath()));
        assertEquals(ImmutableMap.of("property1", "value1"), renamed.getProperties());

        Path newFilePath = renamed.getPath().newPath(fileName);
        assertEquals(DEFAULT_CONTENT, getRoot().getChild(newFilePath).getContentAsString());
        assertEquals(ImmutableMap.of("property2", "value2"), getRoot().getChild(newFilePath).getProperties());

        assertNull(getRoot().getChild(folderPath.newPath(fileName)));
        assertNull(getRoot().getChild(folderPath));
    }

    @Test
    public void failsRenamesFolderWheItContainsLockedFile() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile lockedFile = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        lockedFile.lock(0);
        Path renamedFolderPath = Path.of("/new_name");

        try {
            folder.rename("new_name");
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertNull(getRoot().getChild(renamedFolderPath.newPath(lockedFile.getName())));
            assertNull(getRoot().getChild(renamedFolderPath));

            assertNotNull(getRoot().getChild(folder.getPath()));
            assertEquals(DEFAULT_CONTENT, lockedFile.getContentAsString());
            assertTrue(lockedFile.isLocked());
        }
    }

    @Test
    public void failsRenameFolderWhenParentContainsItemWithSameName() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        VirtualFile conflictFolder = getRoot().createFolder("new_name");

        try {
            folder.rename("new_name");
            thrown.expect(ConflictException.class);
        } catch (ConflictException expected) {
            assertNull(getRoot().getChild(conflictFolder.getPath().newPath(file.getName())));

            assertNotNull(getRoot().getChild(folder.getPath()));
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
        }
    }

    @Test
    public void deletesFile() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        Path filePath = file.getPath();

        file.delete();

        assertNull(getRoot().getChild(filePath));
    }

    @Test
    public void deletesLockedFileWithLockToken() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        Path filePath = file.getPath();
        String lockToken = file.lock(0);

        file.delete(lockToken);

        assertNull(getRoot().getChild(filePath));
    }

    @Test
    public void failsDeleteLockedFileWithoutLockToken() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile folder = root.createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        Path filePath = file.getPath();
        file.lock(0);

        try {
            file.delete();
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertEquals(file, getRoot().getChild(filePath));
            assertTrue(file.isLocked());
        }
    }

    @Test
    public void failsDeleteLockedFileWhenLockTokenIsInvalid() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile folder = root.createFolder(generateFolderName());
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        Path filePath = file.getPath();
        String invalidLockToken = invalidateLockToken(file.lock(0));

        try {
            file.delete(invalidLockToken);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertEquals(file, getRoot().getChild(filePath));
            assertTrue(file.isLocked());
        }
    }

    @Test
    public void deletesFolder() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        folder.setProperty("property1", "value1");
        Path folderPath = folder.getPath();
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        file.setProperty("property1", "value1");
        Path filePath = file.getPath();

        folder.delete();

        assertNull(getRoot().getChild(folderPath));
        assertNull(getRoot().getChild(filePath));
    }

    @Test
    public void failsDeleteFolderWhenItContainsLockedFile() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        Path folderPath = folder.getPath();
        VirtualFile file = folder.createFile(generateFileName(), DEFAULT_CONTENT);
        Path filePath = file.getPath();
        file.lock(0);

        try {
            folder.delete();
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertEquals(folder, getRoot().getChild(folderPath));
            assertEquals(file, getRoot().getChild(filePath));
        }
    }

    @Test
    public void compressesFolderToZipArchive() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        Archiver archiver = mock(Archiver.class);
        when(archiverFactory.createArchiver(eq(folder), eq("zip"))).thenReturn(archiver);
        folder.zip();
        verify(archiver).compress(any(OutputStream.class));
    }

    @Test
    public void failsZipFile() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);

        thrown.expect(ForbiddenException.class);

        file.zip();
    }

    @Test
    public void unzipsInFolder() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        Archiver archiver = mock(Archiver.class);
        when(archiverFactory.createArchiver(eq(folder), eq("zip"))).thenReturn(archiver);
        folder.unzip(new ByteArrayInputStream(new byte[0]), false, 0);
        verify(archiver).extract(any(InputStream.class), eq(false), eq(0));
    }

    @Test
    public void failsUnzipInFile() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        thrown.expect(ForbiddenException.class);
        file.unzip(new ByteArrayInputStream(new byte[0]), false, 0);
    }

    @Test
    public void compressFolderToTarArchive() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        Archiver archiver = mock(Archiver.class);
        when(archiverFactory.createArchiver(eq(folder), eq("tar"))).thenReturn(archiver);
        folder.tar();
        verify(archiver).compress(any(OutputStream.class));
    }

    @Test
    public void failsTarFile() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        thrown.expect(ForbiddenException.class);
        file.tar();
    }

    @Test
    public void untarsInFolder() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        Archiver archiver = mock(Archiver.class);
        when(archiverFactory.createArchiver(eq(folder), eq("tar"))).thenReturn(archiver);
        folder.untar(new ByteArrayInputStream(new byte[0]), false, 0);
        verify(archiver).extract(any(InputStream.class), eq(false), eq(0));
    }

    @Test
    public void locksFile() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        file.lock(0);
        assertTrue(file.isLocked());
    }

    @Test
    public void lockExpiredAfterTimeout() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        file.lock(500);
        assertTrue(file.isLocked());
        Thread.sleep(1000);
        assertFalse(file.isLocked());
    }

    @Test
    public void failsLockFolder() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        try {
            folder.lock(0);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertFalse(folder.isLocked());
        }
    }

    @Test
    public void unlocksFile() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        String lockToken = file.lock(0);
        file.unlock(lockToken);
        assertFalse(file.isLocked());
    }

    @Test
    public void failsUnlockFileWhenLockTokenIsNull() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        file.lock(0);
        try {
            file.unlock(null);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertTrue(file.isLocked());
        }
    }

    @Test
    public void failsUnlockFileWhenLockTokenIsInvalid() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        String invalidLockToken = invalidateLockToken(file.lock(0));
        try {
            file.unlock(invalidLockToken);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertTrue(file.isLocked());
        }
    }

    @Test
    public void createsFileWithStringContent() throws Exception {
        VirtualFile folder = getRoot().createFolder("a/b/c");
        VirtualFile file = folder.createFile("new_file", DEFAULT_CONTENT);

        assertEquals(file, folder.getChild(Path.of("new_file")));
        assertEquals("/a/b/c/new_file", file.getPath().toString());
        assertEquals(DEFAULT_CONTENT, file.getContentAsString());
    }

    @Test
    public void createsFileWithBytesContent() throws Exception {
        VirtualFile folder = getRoot().createFolder("a/b/c");
        VirtualFile file = folder.createFile("new_file", DEFAULT_CONTENT_BYTES);

        assertEquals(file, folder.getChild(Path.of("new_file")));
        assertEquals("/a/b/c/new_file", file.getPath().toString());
        assertEquals(DEFAULT_CONTENT, file.getContentAsString());
    }

    @Test
    public void createsFileWithStreamContent() throws Exception {
        VirtualFile folder = getRoot().createFolder("a/b/c");
        VirtualFile file = folder.createFile("new_file", new ByteArrayInputStream(DEFAULT_CONTENT_BYTES));

        assertEquals(file, folder.getChild(Path.of("new_file")));
        assertEquals("/a/b/c/new_file", file.getPath().toString());
        assertEquals(DEFAULT_CONTENT, file.getContentAsString());
    }

    @Test
    public void failsCreateFileWhenNameContainsSlash() throws Exception {
        VirtualFile folder = getRoot().createFolder("a/b/c");

        String name = "x/new_file";

        thrown.expect(ServerException.class);
        thrown.expectMessage(String.format("Invalid name '%s'", name));

        folder.createFile(name, new ByteArrayInputStream(DEFAULT_CONTENT_BYTES));
    }

    @Test
    public void failsCreateFileWhenNameOfNewFileConflictsWithExistedFile() throws Exception {
        VirtualFile file = getRoot().createFile("file", DEFAULT_CONTENT);
        file.setProperty("property1", "value1");

        try {
            getRoot().createFile("file", "xxx");
            thrown.expect(ConflictException.class);
        } catch (ConflictException expected) {
            assertEquals(DEFAULT_CONTENT, file.getContentAsString());
            assertEquals(ImmutableMap.of("property1", "value1"), file.getProperties());
        }
    }

    @Test
    public void failsCreateFileWhenParenIsNotFolder() throws Exception {
        VirtualFile parent = getRoot().createFile("parent", "");

        try {
            parent.createFile("file", DEFAULT_CONTENT);
            thrown.expect(ForbiddenException.class);
        } catch (ForbiddenException expected) {
            assertNull(getRoot().getChild(parent.getPath().newPath("file")));
        }
    }

    @Test
    public void createsFolder() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile folder = root.createFolder("new_folder");

        assertEquals(folder, root.getChild(Path.of("new_folder")));
    }

    @Test
    public void createsFolderHierarchy() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile folder = root.createFolder("a/b");

        assertEquals(folder, root.getChild(Path.of("a/b")));
    }

    @Test
    public void convertsToIoFile() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), DEFAULT_CONTENT);
        assertNull(file.toIoFile());
    }

    @Test
    public void comparesFileAndFolder() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile file = root.createFile(generateFileName(), "");
        VirtualFile folder = root.createFolder(generateFolderName());
        assertTrue(folder.compareTo(file) < 0);
    }

    @Test
    public void comparesTwoFiles() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile fileA = root.createFile("a", "");
        VirtualFile fileB = root.createFile("b", "");
        assertTrue(fileA.compareTo(fileB) < 0);
    }

    @Test
    public void comparesTwoFolders() throws Exception {
        VirtualFile root = getRoot();
        VirtualFile folderA = root.createFolder("a");
        VirtualFile folderB = root.createFolder("b");
        assertTrue(folderA.compareTo(folderB) < 0);
    }

    @Test
    public void addsNewlyCreatedFileInSearcher() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        verify(searcher).add(file);
    }

    @Test
    public void addsFileThatCopiedFromOtherFileInSearcher() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        Mockito.reset(searcher);
        VirtualFile copy = file.copyTo(folder);
        verify(searcher).add(copy);
    }

    @Test
    public void addsFolderThatCopiedFromOtherFolderInSearcher() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        VirtualFile newParent = getRoot().createFolder(generateFolderName());
        VirtualFile copy = folder.copyTo(newParent);
        verify(searcher).add(copy);
    }

    @Test
    public void doesNotAddNewlyCreatedFolderInSearcher() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        verify(searcher, never()).add(folder);
    }

    @Test
    public void removesDeletedFileFromSearcher() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        String path = file.getPath().toString();
        file.delete();
        verify(searcher).delete(path, true);
    }

    @Test
    public void removesDeletedFolderFromSearcher() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        String path = folder.getPath().toString();
        folder.delete();
        verify(searcher).delete(path, false);
    }

    @Test
    public void updatesFileInSearcherWhenContentUpdatedByStream() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), "");
        file.updateContent(new ByteArrayInputStream(DEFAULT_CONTENT_BYTES));
        verify(searcher).update(file);
    }

    @Test
    public void updatesFileInSearcherWhenContentUpdatedByBytes() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), "");
        file.updateContent(DEFAULT_CONTENT_BYTES);
        verify(searcher).update(file);
    }

    @Test
    public void updatesFileInSearcherWhenContentUpdatedByString() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), "");
        file.updateContent(DEFAULT_CONTENT);
        verify(searcher).update(file);
    }

    @Test
    public void updatesFileInSearcherWhenItIsRenamed() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        String oldPath = file.getPath().toString();
        Mockito.reset(searcher);
        VirtualFile renamed = file.rename("new_name");
        verify(searcher).add(renamed);
        verify(searcher).delete(oldPath, true);
    }

    @Test
    public void updatesFolderInSearcherWhenItIsRenamed() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        String oldPath = folder.getPath().toString();
        Mockito.reset(searcher);
        VirtualFile renamed = folder.rename("new_name");
        verify(searcher).add(renamed);
        verify(searcher).delete(oldPath, false);
    }

    @Test
    public void updatesFileInSearcherWhenItIsMoved() throws Exception {
        VirtualFile file = getRoot().createFile(generateFileName(), DEFAULT_CONTENT);
        VirtualFile newParent = getRoot().createFolder(generateFolderName());
        String oldPath = file.getPath().toString();
        Mockito.reset(searcher);
        VirtualFile moved = file.moveTo(newParent);
        verify(searcher).add(moved);
        verify(searcher).delete(oldPath, true);
    }

    @Test
    public void updatesFolderInSearcherWhenItIsMoved() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFileName());
        VirtualFile newParent = getRoot().createFolder(generateFolderName());
        String oldPath = folder.getPath().toString();
        Mockito.reset(searcher);
        VirtualFile moved = folder.moveTo(newParent);
        verify(searcher).add(moved);
        verify(searcher).delete(oldPath, false);
    }

    @Test
    public void addFolderInSearcherAfterExtractZipArchive() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        Mockito.reset(searcher);
        Archiver archiver = mock(Archiver.class);
        when(archiverFactory.createArchiver(eq(folder), eq("zip"))).thenReturn(archiver);
        folder.unzip(new ByteArrayInputStream(new byte[0]), false, 0);
        verify(searcher).add(folder);
    }

    @Test
    public void addFolderInSearcherAfterExtractTarArchive() throws Exception {
        VirtualFile folder = getRoot().createFolder(generateFolderName());
        Mockito.reset(searcher);
        Archiver archiver = mock(Archiver.class);
        when(archiverFactory.createArchiver(eq(folder), eq("tar"))).thenReturn(archiver);
        folder.untar(new ByteArrayInputStream(new byte[0]), false, 0);
        verify(searcher).add(folder);
    }

    private VirtualFile getRoot() {
        return fileSystem.getRoot();
    }

    private String generateFileName() {
        return NameGenerator.generate("file-", 8);
    }

    private String generateFolderName() {
        return NameGenerator.generate("folder-", 8);
    }

    private String countMd5Sum(VirtualFile file) throws Exception {
        return ByteSource.wrap(file.getContentAsBytes()).hash(Hashing.md5()).toString();
    }

    private String invalidateLockToken(String lockToken) {
        return new StringBuilder(lockToken).reverse().toString();
    }

    private List<VirtualFile> getFileTreeAsList(VirtualFile rootOfTree) throws Exception {
        List<VirtualFile> list = newArrayList();
        rootOfTree.accept(new VirtualFileVisitor() {
            @Override
            public void visit(VirtualFile virtualFile) throws ServerException {
                list.add(virtualFile);
                if (virtualFile.isFolder()) {
                    for (VirtualFile child : virtualFile.getChildren()) {
                        child.accept(this);
                    }
                }
            }
        });
        return list;
    }

    private void createFileTree(VirtualFile rootOfTree, int depth) throws Exception {
        if (depth > 0) {
            VirtualFile folder = rootOfTree.createFolder(generateFolderName());
            for (int i = 0; i < 3; i++) {
                folder.createFile(generateFileName(), DEFAULT_CONTENT);
            }
            createFileTree(folder, depth - 1);
        }
    }
}