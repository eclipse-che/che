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

import org.eclipse.che.api.vfs.AbstractVirtualFileSystemProvider;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.search.SearcherProvider;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MemoryVirtualFileSystemProviderTest {
    private MemoryVirtualFileSystemProvider fileSystemProvider;

    @Before
    public void setUp() throws Exception {
        fileSystemProvider = new MemoryVirtualFileSystemProvider(mock(SearcherProvider.class));
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
        AtomicReference fileSystemReference = getFileSystemReference();
        VirtualFileSystem fileSystem = mock(VirtualFileSystem.class);
        fileSystemReference.set(fileSystem);

        fileSystemProvider.close();

        verify(fileSystem).close();
    }

    private AtomicReference getFileSystemReference() throws Exception {
        Field fileSystemReferenceField = AbstractVirtualFileSystemProvider.class.getDeclaredField("fileSystemReference");
        fileSystemReferenceField.setAccessible(true);
        return (AtomicReference)fileSystemReferenceField.get(fileSystemProvider);
    }

    @Test
    public void resetsVirtualFileSystemInProviderAfterClosingVirtualFileSystem() throws Exception {
        VirtualFileSystem fileSystem = fileSystemProvider.getVirtualFileSystem(true);
        assertNotNull(fileSystem);
        fileSystem.close();
        assertNull(fileSystemProvider.getVirtualFileSystem(false));
    }
}