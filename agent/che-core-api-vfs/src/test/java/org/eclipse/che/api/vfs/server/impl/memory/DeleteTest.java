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
import org.eclipse.che.api.vfs.server.VirtualFile;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.io.ByteArrayInputStream;

import javax.ws.rs.HttpMethod;

/** @author andrew00x */
public class DeleteTest extends MemoryFileSystemTest {
    private String      folderId;
    private String      folderChildId;
    private String      fileId;
    private String      folderPath;
    private String      folderChildPath;
    private String      filePath;
    private VirtualFile file;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        VirtualFile deleteTestFolder = mountPoint.getRoot().createFolder(name);

        VirtualFile folder = deleteTestFolder.createFolder("DeleteTest_FOLDER");
        // add child in folder
        VirtualFile childFile = folder.createFile("file", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        folderId = folder.getId();
        folderChildId = childFile.getId();
        folderPath = folder.getPath();
        folderChildPath = childFile.getPath();

        file = deleteTestFolder.createFile("DeleteTest_FILE", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        fileId = file.getId();
        filePath = file.getPath();
    }

    public void testDeleteFile() throws Exception {
        String path = SERVICE_URI + "delete/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(204, response.getStatus());
        try {
            mountPoint.getVirtualFileById(fileId);
            fail("File must be removed. ");
        } catch (NotFoundException e) {
        }
        try {
            mountPoint.getVirtualFile(filePath);
            fail("File must be removed. ");
        } catch (NotFoundException e) {
        }
        assertFalse(file.exists());
    }

    public void testDeleteFileLocked() throws Exception {
        String lockToken = file.lock(0);
        String path = SERVICE_URI + "delete/" + fileId + '?' + "lockToken=" + lockToken;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(204, response.getStatus());
        try {
            mountPoint.getVirtualFileById(fileId);
            fail("File must be removed. ");
        } catch (NotFoundException e) {
        }
        try {
            mountPoint.getVirtualFile(filePath);
            fail("File must be removed. ");
        } catch (NotFoundException e) {
        }
    }

    public void testDeleteFileLockedNoLockToken() throws Exception {
        file.lock(0);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "delete/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
        assertEquals(403, response.getStatus());
        log.info(new String(writer.getBody()));
        try {
            mountPoint.getVirtualFileById(fileId);
        } catch (NotFoundException e) {
            fail("File must not be removed since it is locked. ");
        }
    }

    public void testDeleteFileWrongId() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "delete/" + fileId + "_WRONG_ID";
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
        assertEquals(404, response.getStatus());
        log.info(new String(writer.getBody()));
    }

    public void testDeleteRootFolder() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "delete/" + mountPoint.getRoot().getId();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
        assertEquals(403, response.getStatus());
        log.info(new String(writer.getBody()));
    }

    public void testDeleteFolder() throws Exception {
        String path = SERVICE_URI + "delete/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(204, response.getStatus());
        try {
            mountPoint.getVirtualFileById(folderId);
            fail("Folder must be removed. ");
        } catch (NotFoundException e) {
        }
        try {
            mountPoint.getVirtualFileById(folderChildId);
            fail("Child file must be removed. ");
        } catch (NotFoundException e) {
        }
        try {
            mountPoint.getVirtualFile(folderPath);
            fail("Folder must be removed. ");
        } catch (NotFoundException e) {
        }
        try {
            mountPoint.getVirtualFile(folderChildPath);
            fail("Child file must be removed. ");
        } catch (NotFoundException e) {
        }
    }

    public void testDeleteFolderLockedChild() throws Exception {
        mountPoint.getVirtualFileById(folderChildId).lock(0);
        String path = SERVICE_URI + "delete/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(403, response.getStatus());
        try {
            mountPoint.getVirtualFileById(folderId);
        } catch (NotFoundException e) {
            fail("Folder must not be removed since child file is locked. ");
        }
    }
}
