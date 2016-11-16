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
package org.eclipse.che.api.vfs.search.impl;

import com.google.common.base.Optional;

import org.eclipse.che.api.vfs.ArchiverFactory;
import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileFilter;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.impl.memory.MemoryVirtualFileSystem;
import org.eclipse.che.api.vfs.search.QueryExpression;
import org.eclipse.che.api.vfs.search.SearchResult;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("Duplicates")
public class FSLuceneSearcherTest {
    private static final String[] TEST_CONTENT = {
            "Apollo set several major human spaceflight milestones",
            "Maybe you should think twice",
            "To be or not to be",
            "In early 1961, direct ascent was generally the mission mode in favor at NASA"
    };

    private File                                         indexDirectory;
    private VirtualFileFilter                            filter;
    private FSLuceneSearcher                             searcher;
    private AbstractLuceneSearcherProvider.CloseCallback closeCallback;

    @Before
    public void setUp() throws Exception {
        File targetDir = new File(Thread.currentThread().getContextClassLoader().getResource(".").getPath()).getParentFile();
        indexDirectory = new File(targetDir, NameGenerator.generate("index-", 4));
        assertTrue(indexDirectory.mkdir());

        filter = mock(VirtualFileFilter.class);
        when(filter.accept(any(VirtualFile.class))).thenReturn(false);

        closeCallback = mock(AbstractLuceneSearcherProvider.CloseCallback.class);
        searcher = new FSLuceneSearcher(indexDirectory, filter, closeCallback);
    }

    @After
    public void tearDown() throws Exception {
        searcher.close();
        IoUtil.deleteRecursive(indexDirectory);
    }

    @Test
    public void initializesIndexForExistedFiles() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        VirtualFile folder = virtualFileSystem.getRoot().createFolder("folder");
        folder.createFile("xxx.txt", TEST_CONTENT[2]);
        folder.createFile("zzz.txt", TEST_CONTENT[1]);
        searcher.init(virtualFileSystem);

        List<String> paths = searcher.search(new QueryExpression().setText("think")).getFilePaths();
        assertEquals(newArrayList("/folder/zzz.txt"), paths);
    }

    @Test
    public void addsSingleFileInIndex() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        searcher.init(virtualFileSystem);
        VirtualFile file = virtualFileSystem.getRoot().createFolder("aaa").createFile("aaa.txt", TEST_CONTENT[1]);

        searcher.add(file);

        List<String> paths = searcher.search(new QueryExpression().setText("should")).getFilePaths();
        assertEquals(newArrayList(file.getPath().toString()), paths);
    }

    @Test
    public void addsFileTreeInIndex() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        searcher.init(virtualFileSystem);
        VirtualFile folder = virtualFileSystem.getRoot().createFolder("folder");
        folder.createFile("xxx.txt", TEST_CONTENT[2]);
        folder.createFile("zzz.txt", TEST_CONTENT[1]);

        searcher.add(virtualFileSystem.getRoot());

        List<String> paths = searcher.search(new QueryExpression().setText("be")).getFilePaths();
        assertEquals(newArrayList("/folder/xxx.txt"), paths);
        paths = searcher.search(new QueryExpression().setText("should")).getFilePaths();
        assertEquals(newArrayList("/folder/zzz.txt"), paths);
    }

    @Test
    public void updatesSingleFileInIndex() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        VirtualFile file = virtualFileSystem.getRoot().createFolder("aaa").createFile("aaa.txt", TEST_CONTENT[2]);
        searcher.init(virtualFileSystem);

        List<String> paths = searcher.search(new QueryExpression().setText("should")).getFilePaths();
        assertTrue(paths.isEmpty());

        file.updateContent(TEST_CONTENT[1]);
        searcher.update(file);

        paths = searcher.search(new QueryExpression().setText("should")).getFilePaths();

        assertEquals(newArrayList(file.getPath().toString()), paths);
    }

    @Test
    public void deletesSingleFileFromIndex() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        VirtualFile file = virtualFileSystem.getRoot().createFolder("aaa").createFile("aaa.txt", TEST_CONTENT[2]);
        searcher.init(virtualFileSystem);

        List<String> paths = searcher.search(new QueryExpression().setText("be")).getFilePaths();
        assertEquals(newArrayList(file.getPath().toString()), paths);

        searcher.delete(file.getPath().toString(), file.isFile());

        paths = searcher.search(new QueryExpression().setText("be")).getFilePaths();
        assertTrue(paths.isEmpty());
    }

    @Test
    public void deletesFileTreeFromIndex() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        VirtualFile folder = virtualFileSystem.getRoot().createFolder("folder");
        folder.createFile("xxx.txt", TEST_CONTENT[2]);
        folder.createFile("zzz.txt", TEST_CONTENT[1]);
        searcher.init(virtualFileSystem);

        List<String> paths = searcher.search(new QueryExpression().setText("be")).getFilePaths();
        assertEquals(newArrayList("/folder/xxx.txt"), paths);
        paths = searcher.search(new QueryExpression().setText("should")).getFilePaths();
        assertEquals(newArrayList("/folder/zzz.txt"), paths);

        searcher.delete("/folder", false);

        paths = searcher.search(new QueryExpression().setText("be")).getFilePaths();
        assertTrue(paths.isEmpty());
        paths = searcher.search(new QueryExpression().setText("should")).getFilePaths();
        assertTrue(paths.isEmpty());
    }


    @Test
    public void searchesByWordFragment() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        VirtualFile folder = virtualFileSystem.getRoot().createFolder("folder");
        folder.createFile("xxx.txt", TEST_CONTENT[0]);
        searcher.init(virtualFileSystem);

        List<String> paths = searcher.search(new QueryExpression().setText("*stone*")).getFilePaths();
        assertEquals(newArrayList("/folder/xxx.txt"), paths);
    }

    @Test
    public void searchesByTextAndFileName() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        VirtualFile folder = virtualFileSystem.getRoot().createFolder("folder");
        folder.createFile("xxx.txt", TEST_CONTENT[2]);
        folder.createFile("zzz.txt", TEST_CONTENT[2]);
        searcher.init(virtualFileSystem);

        List<String> paths = searcher.search(new QueryExpression().setText("be").setName("xxx.txt")).getFilePaths();
        assertEquals(newArrayList("/folder/xxx.txt"), paths);
    }

    @Test
    public void searchesByTextAndPath() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        VirtualFile folder1 = virtualFileSystem.getRoot().createFolder("folder1/a/b");
        VirtualFile folder2 = virtualFileSystem.getRoot().createFolder("folder2");
        folder1.createFile("xxx.txt", TEST_CONTENT[2]);
        folder2.createFile("zzz.txt", TEST_CONTENT[2]);
        searcher.init(virtualFileSystem);

        List<String> paths = searcher.search(new QueryExpression().setText("be").setPath("/folder1")).getFilePaths();
        assertEquals(newArrayList("/folder1/a/b/xxx.txt"), paths);
    }

    @Test
    public void searchesByTextAndPathAndFileName() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        VirtualFile folder1 = virtualFileSystem.getRoot().createFolder("folder1/a/b");
        VirtualFile folder2 = virtualFileSystem.getRoot().createFolder("folder2/a/b");
        folder1.createFile("xxx.txt", TEST_CONTENT[2]);
        folder1.createFile("yyy.txt", TEST_CONTENT[2]);
        folder2.createFile("zzz.txt", TEST_CONTENT[2]);
        searcher.init(virtualFileSystem);

        List<String> paths = searcher.search(new QueryExpression().setText("be").setPath("/folder1").setName("xxx.txt")).getFilePaths();
        assertEquals(newArrayList("/folder1/a/b/xxx.txt"), paths);
    }

    @Test
    public void closesLuceneIndexWriterWhenSearcherClosed() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        searcher.init(virtualFileSystem);

        searcher.close();

        assertTrue(searcher.isClosed());
        assertFalse(searcher.getIndexWriter().isOpen());
    }

    @Test
    public void notifiesCallbackWhenSearcherClosed() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        searcher.init(virtualFileSystem);

        searcher.close();
        verify(closeCallback).onClose();
    }

    @Test
    public void excludesFilesFromIndexWithFilter() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        VirtualFile folder = virtualFileSystem.getRoot().createFolder("folder");
        folder.createFile("xxx.txt", TEST_CONTENT[2]);
        folder.createFile("yyy.txt", TEST_CONTENT[2]);
        folder.createFile("zzz.txt", TEST_CONTENT[2]);

        when(filter.accept(withName("yyy.txt"))).thenReturn(true);
        searcher.init(virtualFileSystem);

        List<String> paths = searcher.search(new QueryExpression().setText("be")).getFilePaths();
        assertEquals(newArrayList("/folder/xxx.txt", "/folder/zzz.txt"), paths);
    }

    @Test
    public void limitsNumberOfSearchResultsWhenMaxItemIsSet() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        for (int i = 0; i < 100; i++) {
            virtualFileSystem.getRoot().createFile(String.format("file%02d", i), TEST_CONTENT[i % TEST_CONTENT.length]);
        }
        searcher.init(virtualFileSystem);

        SearchResult result = searcher.search(new QueryExpression().setText("mission").setMaxItems(5));

        assertEquals(25, result.getTotalHits());
        assertEquals(5, result.getFilePaths().size());
    }

    @Test
    public void generatesQueryExpressionForRetrievingNextPageOfResults() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        for (int i = 0; i < 100; i++) {
            virtualFileSystem.getRoot().createFile(String.format("file%02d", i), TEST_CONTENT[i % TEST_CONTENT.length]);
        }
        searcher.init(virtualFileSystem);

        SearchResult result = searcher.search(new QueryExpression().setText("spaceflight").setMaxItems(7));

        assertEquals(25, result.getTotalHits());

        Optional<QueryExpression> optionalNextPageQueryExpression = result.getNextPageQueryExpression();
        assertTrue(optionalNextPageQueryExpression.isPresent());

        QueryExpression nextPageQueryExpression = optionalNextPageQueryExpression.get();
        assertEquals("spaceflight", nextPageQueryExpression.getText());
        assertEquals(7, nextPageQueryExpression.getSkipCount());
        assertEquals(7, nextPageQueryExpression.getMaxItems());
    }

    @Test
    public void retrievesSearchResultWithPages() throws Exception {
        VirtualFileSystem virtualFileSystem = virtualFileSystem();
        for (int i = 0; i < 100; i++) {
            virtualFileSystem.getRoot().createFile(String.format("file%02d", i), TEST_CONTENT[i % TEST_CONTENT.length]);
        }
        searcher.init(virtualFileSystem);

        SearchResult firstPage = searcher.search(new QueryExpression().setText("spaceflight").setMaxItems(8));
        assertEquals(8, firstPage.getFilePaths().size());

        QueryExpression nextPageQueryExpression = firstPage.getNextPageQueryExpression().get();
        nextPageQueryExpression.setMaxItems(100);

        SearchResult lastPage = searcher.search(nextPageQueryExpression);
        assertEquals(17, lastPage.getFilePaths().size());

        assertTrue(Collections.disjoint(firstPage.getFilePaths(), lastPage.getFilePaths()));
    }

    private VirtualFileSystem virtualFileSystem() throws Exception {
        return new MemoryVirtualFileSystem(mock(ArchiverFactory.class), null);
    }

    private static VirtualFile withName(String name) {
        return argThat(new ArgumentMatcher<VirtualFile>() {
            @Override
            public boolean matches(Object argument) {
                return name.equals(((VirtualFile)argument).getName());
            }
        });
    }
}
