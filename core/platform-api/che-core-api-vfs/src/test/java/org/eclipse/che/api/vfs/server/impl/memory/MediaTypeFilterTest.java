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
package org.eclipse.che.api.vfs.server.impl.memory;

import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.util.MediaTypeFilter;

import java.io.ByteArrayInputStream;

/**
 * @author Valeriy Svydenko
 */
public class MediaTypeFilterTest extends MemoryFileSystemTest {
    private MediaTypeFilter mediaTypeFilter;
    private VirtualFile     file1;
    private VirtualFile     file2;
    private VirtualFile     file3;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mediaTypeFilter = new MediaTypeFilter();
        VirtualFile searchTestFolder1 = mountPoint.getRoot().createFolder("SearcherTest");
        VirtualFile searchTestFolder = searchTestFolder1.createFolder("SearcherTest_Folder");
        file1 = searchTestFolder.createFile("File", new ByteArrayInputStream("to be or not to be".getBytes()));
        file2 = searchTestFolder.createFile("HTMLFile.html", new ByteArrayInputStream("<html><head></head></html>".getBytes()));
        file3 = searchTestFolder.createFile("JavaFile.java", new ByteArrayInputStream("public class JavaFile02 {}".getBytes()));
    }

    public void testFilesShouldAccepted () throws Exception {
        assertTrue(mediaTypeFilter.accept(file1));
        assertTrue(mediaTypeFilter.accept(file2));
        assertTrue(mediaTypeFilter.accept(file3));
    }
}
