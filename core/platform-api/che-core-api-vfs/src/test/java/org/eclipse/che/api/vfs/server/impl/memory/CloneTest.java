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

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.Path;
import org.eclipse.che.api.vfs.server.VirtualFileSystem;
import org.eclipse.che.api.vfs.shared.dto.Item;

import java.io.ByteArrayInputStream;

/** @author Vitaliy Guliy */
public class CloneTest extends MemoryFileSystemTest {

    protected static final String SOURCE_WORKSPACE_ID = "source-ws";

    private MemoryFileSystemProvider srcFileSystemProvider;
    private MemoryMountPoint         srcMountPoint;


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        srcFileSystemProvider = new MemoryFileSystemProvider(SOURCE_WORKSPACE_ID, new EventService(), virtualFileSystemRegistry);
        virtualFileSystemRegistry.registerProvider(SOURCE_WORKSPACE_ID, srcFileSystemProvider);
        srcMountPoint = (MemoryMountPoint)srcFileSystemProvider.getMountPoint(true);
    }


    protected void tearDown() throws Exception {
        virtualFileSystemRegistry.unregisterProvider(SOURCE_WORKSPACE_ID);
        super.tearDown();
    }

    public void testCloneFile() throws Exception {
        // create file in 'my-ws'
        VirtualFile rootFolder = srcMountPoint.getRoot();
        VirtualFile sourceFile = rootFolder.createFile("file-to-clone", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));

        // clone it to 'next-ws'
        VirtualFileSystem sourceVFS = fileSystemProvider.newInstance(null);
        Item item = sourceVFS.clone(sourceFile.getPath(), SOURCE_WORKSPACE_ID, srcMountPoint.getRoot().getPath(), null);
        assertEquals("/file-to-clone", item.getPath());

        // check the result
        try {
           mountPoint.getVirtualFile(sourceFile.getPath());
        } catch (NotFoundException e) {
            fail("Destination file not found.");
        }
    }

    public void testCloneTree() throws Exception {
        // create below tree in 'my-ws'
        // folder1
        //     folder2
        //         file1
        //     folder3
        //         folder4
        //         file2
        //         file3
        //     folder5
        //     file4
        //     file5

        VirtualFile rootFolder = srcMountPoint.getRoot();

        VirtualFile folder1 = rootFolder.createFolder("folder1");
            VirtualFile folder2 = folder1.createFolder("folder2");
                VirtualFile file1 = folder2.createFile("file1", new ByteArrayInputStream("file1 text".getBytes()));
            VirtualFile folder3 = folder1.createFolder("folder3");
                VirtualFile folder4 = folder3.createFolder("folder4");
                VirtualFile file2 = folder3.createFile("file2", new ByteArrayInputStream("file2 text".getBytes()));
                VirtualFile file3 = folder3.createFile("file3", new ByteArrayInputStream("file3 text".getBytes()));
            VirtualFile folder5 = folder1.createFolder("folder5");
            VirtualFile file4 = folder1.createFile("file4", new ByteArrayInputStream("file4 text".getBytes()));
            VirtualFile file5 = folder1.createFile("file5", new ByteArrayInputStream("file5 text".getBytes()));

        // clone it to 'next-ws'
        VirtualFileSystem sourceVFS = fileSystemProvider.newInstance(null);
        Item item = sourceVFS.clone(folder1.getPath(), SOURCE_WORKSPACE_ID, srcMountPoint.getRoot().getPath(), null);
        assertEquals("/folder1", item.getPath());

        // check the result
        try {
            mountPoint.getVirtualFile(folder1.getPath());
            mountPoint.getVirtualFile(folder2.getPath());
            mountPoint.getVirtualFile(folder3.getPath());
            mountPoint.getVirtualFile(folder4.getPath());
            mountPoint.getVirtualFile(folder5.getPath());

            mountPoint.getVirtualFile(file1.getPath());
            mountPoint.getVirtualFile(file2.getPath());
            mountPoint.getVirtualFile(file3.getPath());
            mountPoint.getVirtualFile(file4.getPath());
            mountPoint.getVirtualFile(file5.getPath());
        } catch (NotFoundException e) {
            fail("Destination file not found. " + e.getMessage());
        }
    }

    public void testCloneTreeWithName() throws Exception {
        // create below tree in 'my-ws'
        // folder1
        //     folder2
        //         file1
        //     folder3
        //         folder4
        //         file2
        //         file3
        //     folder5
        //     file4
        //     file5

        VirtualFile rootFolder = srcMountPoint.getRoot();

        VirtualFile folder1 = rootFolder.createFolder("folder1");
        VirtualFile folder2 = folder1.createFolder("folder2");
        VirtualFile file1 = folder2.createFile("file1", new ByteArrayInputStream("file1 text".getBytes()));
        VirtualFile folder3 = folder1.createFolder("folder3");
        VirtualFile folder4 = folder3.createFolder("folder4");
        VirtualFile file2 = folder3.createFile("file2", new ByteArrayInputStream("file2 text".getBytes()));
        VirtualFile file3 = folder3.createFile("file3", new ByteArrayInputStream("file3 text".getBytes()));
        VirtualFile folder5 = folder1.createFolder("folder5");
        VirtualFile file4 = folder1.createFile("file4", new ByteArrayInputStream("file4 text".getBytes()));
        VirtualFile file5 = folder1.createFile("file5", new ByteArrayInputStream("file5 text".getBytes()));

        VirtualFile destination = mountPoint.getRoot().createFolder("a/b");
        // clone it to 'next-ws'
        VirtualFileSystem sourceVFS = fileSystemProvider.newInstance(null);
        Item item = sourceVFS.clone(folder1.getPath(), SOURCE_WORKSPACE_ID, destination.getPath(), "new_name");
        assertEquals("/a/b/new_name", item.getPath());

        Path basePath = destination.getVirtualFilePath().newPath("new_name");

        // check the result
        try {
            mountPoint.getVirtualFile(basePath.newPath(folder2.getVirtualFilePath().subPath(1)).toString());
            mountPoint.getVirtualFile(basePath.newPath(folder3.getVirtualFilePath().subPath(1)).toString());
            mountPoint.getVirtualFile(basePath.newPath(folder4.getVirtualFilePath().subPath(1)).toString());
            mountPoint.getVirtualFile(basePath.newPath(folder5.getVirtualFilePath().subPath(1)).toString());
            mountPoint.getVirtualFile(basePath.newPath(folder2.getVirtualFilePath().subPath(1)).toString());

            mountPoint.getVirtualFile(basePath.newPath(file1.getVirtualFilePath().subPath(1)).toString());
            mountPoint.getVirtualFile(basePath.newPath(file2.getVirtualFilePath().subPath(1)).toString());
            mountPoint.getVirtualFile(basePath.newPath(file3.getVirtualFilePath().subPath(1)).toString());
            mountPoint.getVirtualFile(basePath.newPath(file4.getVirtualFilePath().subPath(1)).toString());
            mountPoint.getVirtualFile(basePath.newPath(file5.getVirtualFilePath().subPath(1)).toString());
        } catch (NotFoundException e) {
            fail("Destination file not found. " + e.getMessage());
        }
    }
}
