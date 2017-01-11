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

import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.vfs.AbstractVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.ArchiverFactory;
import org.eclipse.che.api.vfs.search.Searcher;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LocalVirtualFileSystemTest {
    private LocalVirtualFileSystem                          fileSystem;
    private Searcher                                        searcher;
    private AbstractVirtualFileSystemProvider.CloseCallback closeCallback;
    private File                                            testDirectory;

    @Before
    public void setUp() throws Exception {
        SearcherProvider searcherProvider = mock(SearcherProvider.class);
        searcher = mock(Searcher.class);
        closeCallback = mock(AbstractVirtualFileSystemProvider.CloseCallback.class);
        File targetDir = new File(Thread.currentThread().getContextClassLoader().getResource(".").getPath()).getParentFile();
        testDirectory = new File(targetDir, NameGenerator.generate("fs-", 4));
        fileSystem = new LocalVirtualFileSystem(testDirectory, mock(ArchiverFactory.class), searcherProvider, closeCallback);
        when(searcherProvider.getSearcher(eq(fileSystem), anyBoolean())).thenReturn(searcher);
        when(searcherProvider.getSearcher(eq(fileSystem))).thenReturn(searcher);
    }

    @After
    public void tearDown() throws Exception {
        IoUtil.deleteRecursive(testDirectory);
        FileCleaner.stop();
    }

    @Test
    public void notifiedCallbackWhenFileSystemClosed() throws Exception {
        fileSystem.close();
        verify(closeCallback).onClose();
    }

    @Test
    public void closesSearcherWhenFileSystemClosed() throws Exception {
        fileSystem.close();
        verify(searcher).close();
    }
}