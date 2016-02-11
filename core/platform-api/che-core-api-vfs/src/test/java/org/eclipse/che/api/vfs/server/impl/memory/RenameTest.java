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
public class RenameTest extends MemoryFileSystemTest {
    private VirtualFile renameTestFolder;
    private String      fileId;
    private String      folderId;
    private VirtualFile file;
    private VirtualFile folder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        renameTestFolder = mountPoint.getRoot().createFolder(name);

        folder = renameTestFolder.createFolder("RenameFileTest_FOLDER");
        folderId = folder.getId();

        file = renameTestFolder.createFile("file", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        fileId = file.getId();
    }

    public void testRenameFile() throws Exception {
        String path = SERVICE_URI + "rename/" + fileId + '?' + "newname=" + "_FILE_NEW_NAME_" + '&' + "mediaType=" +
                      "text/*;charset=ISO-8859-1";
        String originPath = file.getPath();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        String expectedPath = renameTestFolder.getPath() + '/' + "_FILE_NEW_NAME_";
        try {
            mountPoint.getVirtualFile(originPath);
            fail("File must be renamed. ");
        } catch (NotFoundException e) {
        }
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("Can't find file after rename. ");
        }

        checkFileContext(DEFAULT_CONTENT, "text/*;charset=ISO-8859-1", mountPoint.getVirtualFile(expectedPath));
    }

    public void testRenameFileAlreadyExists() throws Exception {
        renameTestFolder.createFile("_FILE_NEW_NAME_", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        String path = SERVICE_URI + "rename/" + fileId + '?' + "newname=" + "_FILE_NEW_NAME_" + '&' + "mediaType=" +
                      "text/*;charset=ISO-8859-1";
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(409, response.getStatus());
    }

    public void testRenameFileLocked() throws Exception {
        String lockToken = file.lock(0);
        String path = SERVICE_URI + "rename/" + fileId + '?' + "newname=" + "_FILE_NEW_NAME_" + '&' + "mediaType=" +
                      "text/*;charset=ISO-8859-1" + '&' + "lockToken=" + lockToken;
        String originPath = file.getPath();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        String expectedPath = renameTestFolder.getPath() + '/' + "_FILE_NEW_NAME_";
        try {
            mountPoint.getVirtualFile(originPath);
            fail("File must be renamed. ");
        } catch (NotFoundException e) {
        }
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("Can't find file after rename. ");
        }
    }

    public void testRenameFileLockedNoLockToken() throws Exception {
        file.lock(0);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "rename/" + fileId + '?' + "newname=" + "_FILE_NEW_NAME_" + '&' + "mediaType=" +
                      "text/*;charset=ISO-8859-1";
        String originPath = file.getPath();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
        assertEquals(403, response.getStatus());
        log.info(new String(writer.getBody()));
        String expectedPath = renameTestFolder.getPath() + '/' + "_FILE_NEW_NAME_";
        try {
            mountPoint.getVirtualFile(originPath);
        } catch (NotFoundException e) {
            fail("Source file not found. ");
        }
        try {
            mountPoint.getVirtualFile(expectedPath);
            fail("File must not be renamed since it is locked. ");
        } catch (NotFoundException e) {
        }
    }

    public void testRenameFolder() throws Exception {
        String path = SERVICE_URI + "rename/" + folderId + '?' + "newname=" + "_FOLDER_NEW_NAME_";
        String originPath = folder.getPath();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        String expectedPath = renameTestFolder.getPath() + '/' + "_FOLDER_NEW_NAME_";
        try {
            mountPoint.getVirtualFile(originPath);
            fail("Folder must be renamed. ");
        } catch (NotFoundException e) {
        }
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("Can't folder file after rename. ");
        }
    }

    public void testRenameFolderWithLockedFile() throws Exception {
        folder.createFile("file", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes())).lock(0);
        String path = SERVICE_URI + "rename/" + folderId + '?' + "newname=" + "_FOLDER_NEW_NAME_";
        String originPath = folder.getPath();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(403, response.getStatus());
        String expectedPath = renameTestFolder.getPath() + '/' + "_FOLDER_NEW_NAME_";
        try {
            mountPoint.getVirtualFile(originPath);
        } catch (NotFoundException e) {
            fail("Source file not found. ");
        }
        try {
            mountPoint.getVirtualFile(expectedPath);
            fail("Folder must not be renamed since it contains locked file. ");
        } catch (NotFoundException e) {
        }
    }
}
