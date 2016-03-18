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

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.io.ByteArrayInputStream;

import javax.ws.rs.HttpMethod;

/** @author andrew00x */
public class LockTest extends MemoryFileSystemTest {
    private String folderId;
    private String fileId;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        VirtualFile lockTestFolder = mountPoint.getRoot().createFolder(name);

        VirtualFile folder = lockTestFolder.createFolder("LockTest_FOLDER");
        folderId = folder.getId();

        VirtualFile file = lockTestFolder.createFile("LockTest_FILE", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        fileId = file.getId();
    }

    public void testLockFile() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "lock/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
        assertEquals(200, response.getStatus());
        log.info(new String(writer.getBody()));
        VirtualFile file = mountPoint.getVirtualFileById(fileId);
        assertTrue("File must be locked. ", file.isLocked());
        validateLinks(getItem(fileId));
    }

    public void testLockFileAlreadyLocked() throws Exception {
        VirtualFile file = mountPoint.getVirtualFileById(fileId);
        file.lock(0);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "lock/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
        assertEquals(409, response.getStatus());
        log.info(new String(writer.getBody()));
    }

    public void testLockFolder() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "lock/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
        assertEquals(403, response.getStatus());
    }

    public void testLockTimeout() throws Exception {
        VirtualFile file = mountPoint.getVirtualFileById(fileId);
        file.lock(100);
        assertTrue(file.isLocked());
        Thread.sleep(200);
        assertFalse(file.isLocked());
    }
}
