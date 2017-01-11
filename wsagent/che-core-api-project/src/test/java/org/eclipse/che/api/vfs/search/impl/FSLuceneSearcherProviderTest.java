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
package org.eclipse.che.api.vfs.search.impl;

import org.eclipse.che.api.vfs.VirtualFile;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.search.Searcher;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.PathMatcher;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FSLuceneSearcherProviderTest {
    private File                     indexRootDirectory;
    private FSLuceneSearcherProvider fsLuceneSearcherProvider;

    @Before
    public void setUp() throws Exception {
        File targetDir = new File(Thread.currentThread().getContextClassLoader().getResource(".").getPath()).getParentFile();
        indexRootDirectory = new File(targetDir, NameGenerator.generate("index-root", 4));
        assertTrue(indexRootDirectory.mkdir());

        fsLuceneSearcherProvider = new FSLuceneSearcherProvider(indexRootDirectory, newHashSet(mock(PathMatcher.class)));
    }

    @After
    public void tearDown() throws Exception {
        IoUtil.deleteRecursive(indexRootDirectory);
    }

    @Test
    public void doesNotCreateSearcherWhenItIsNotCreatedYetAndCreationIsNotRequested() throws Exception {
        VirtualFileSystem virtualFileSystem = mockVirtualFileSystem();
        assertNull(fsLuceneSearcherProvider.getSearcher(virtualFileSystem, false));
    }

    @Test
    public void createsAndInitializeSearcherWhenCreationRequested() throws Exception {
        VirtualFileSystem virtualFileSystem = mockVirtualFileSystem();
        assertNotNull(fsLuceneSearcherProvider.getSearcher(virtualFileSystem, true));
    }

    @Test
    public void returnsSameInstanceOfSearcherOnceItWasCreated() throws Exception {
        VirtualFileSystem virtualFileSystem = mockVirtualFileSystem();

        Searcher searcher = fsLuceneSearcherProvider.getSearcher(virtualFileSystem, true);
        assertNotNull(searcher);
        assertSame(searcher, fsLuceneSearcherProvider.getSearcher(virtualFileSystem, true));
    }

    @Test
    public void closesSearcherWhenProviderIsClosed() throws Exception {
        Searcher searcher = mock(Searcher.class);
        fsLuceneSearcherProvider.searcherReference.set(searcher);

        fsLuceneSearcherProvider.close();

        verify(searcher).close();
    }

    @Test
    public void resetsSearcherInProviderAfterClosingSearcher() throws Exception {
        VirtualFileSystem virtualFileSystem = mockVirtualFileSystem();
        Searcher searcher = fsLuceneSearcherProvider.getSearcher(virtualFileSystem, true);
        assertNotNull(searcher);
        searcher.close();
        assertNull(fsLuceneSearcherProvider.getSearcher(virtualFileSystem, false));
    }

    private VirtualFileSystem mockVirtualFileSystem() {
        VirtualFileSystem virtualFileSystem = mock(VirtualFileSystem.class);
        VirtualFile root = mock(VirtualFile.class);
        when(virtualFileSystem.getRoot()).thenReturn(root);
        return virtualFileSystem;
    }
}