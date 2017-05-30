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

import org.eclipse.che.api.vfs.AbstractVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LocalVirtualFileSystemProviderTest {
    private File                           fsRootDirectory;
    private LocalVirtualFileSystemProvider fileSystemProvider;

    @Before
    public void setUp() throws Exception {
        File targetDir = new File(Thread.currentThread().getContextClassLoader().getResource(".").getPath()).getParentFile();
        fsRootDirectory = new File(targetDir, NameGenerator.generate("index-root", 4));
        assertTrue(fsRootDirectory.mkdir());
        fileSystemProvider = new LocalVirtualFileSystemProvider(fsRootDirectory, mock(SearcherProvider.class));
    }

    @After
    public void tearDown() throws Exception {
        IoUtil.deleteRecursive(fsRootDirectory);
    }

    @Test
    public void doesNotCreateVirtualFileSystemWhenItIsNotCreatedYetAndCreationIsNotRequested() throws Exception {
        assertNull(fileSystemProvider.getVirtualFileSystem(false));
    }

    @Test
    public void createsVirtualFileSystemWhenCreationRequested() throws Exception {
        assertNotNull(fileSystemProvider.getVirtualFileSystem(true));
    }

    @Test
    public void returnsSameInstanceOfVirtualFileSystemOnceItWasCreated() throws Exception {
        VirtualFileSystem fileSystem = fileSystemProvider.getVirtualFileSystem(true);
        assertNotNull(fileSystem);
        assertSame(fileSystem, fileSystemProvider.getVirtualFileSystem(false));
    }

    @Test
    public void closesVirtualFileSystemWhenProviderIsClosed() throws Exception {
        AtomicReference<VirtualFileSystem> fileSystemReference = getFileSystemReference();
        VirtualFileSystem fileSystem = mock(VirtualFileSystem.class);
        fileSystemReference.set(fileSystem);

        fileSystemProvider.close();

        verify(fileSystem).close();
    }

    private AtomicReference<VirtualFileSystem> getFileSystemReference() throws Exception {
        Field fileSystemReferenceField = AbstractVirtualFileSystemProvider.class.getDeclaredField("fileSystemReference");
        fileSystemReferenceField.setAccessible(true);
        return (AtomicReference<VirtualFileSystem>)fileSystemReferenceField.get(fileSystemProvider);
    }

    @Test
    public void resetsVirtualFileSystemInProviderAfterClosingVirtualFileSystem() throws Exception {
        VirtualFileSystem fileSystem = fileSystemProvider.getVirtualFileSystem(true);
        assertNotNull(fileSystem);
        fileSystem.close();
        assertNull(fileSystemProvider.getVirtualFileSystem(false));
    }
}