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
public class MoveTest extends MemoryFileSystemTest {
    private VirtualFile moveTestDestinationFolder;
    private VirtualFile folderForMove;
    private VirtualFile fileForMove;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        VirtualFile moveTestFolder = mountPoint.getRoot().createFolder(name);

        moveTestDestinationFolder = mountPoint.getRoot().createFolder(name + "_MoveTest_DESTINATION");

        folderForMove = moveTestFolder.createFolder("MoveTest_FOLDER");
        folderForMove.createFile("file", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));

        fileForMove = moveTestFolder.createFile("MoveTest_FILE", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
    }

    public void testMoveFile() throws Exception {
        String path = SERVICE_URI + "move/" + fileForMove.getId() + '?' + "parentId=" + moveTestDestinationFolder.getId();
        String originPath = fileForMove.getPath();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        String expectedPath = moveTestDestinationFolder.getPath() + '/' + fileForMove.getName();
        try {
            mountPoint.getVirtualFile(originPath);
            fail("File must be moved. ");
        } catch (NotFoundException e) {
        }
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("Not found file in destination location. ");
        }
    }

    public void testMoveFileAlreadyExist() throws Exception {
        moveTestDestinationFolder.createFile(fileForMove.getName(), new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        String originPath = fileForMove.getPath();
        String path = SERVICE_URI + "move/" + fileForMove.getId() + '?' + "parentId=" + moveTestDestinationFolder.getId();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(409, response.getStatus());
        try {
            mountPoint.getVirtualFile(originPath);
        } catch (NotFoundException e) {
            fail("Source file not found. ");
        }
    }

    public void testCopyFileWrongParent() throws Exception {
        final String originPath = fileForMove.getPath();
        VirtualFile destination =
                mountPoint.getRoot().createFile("destination", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        String path = SERVICE_URI + "move/" + fileForMove.getId() + '?' + "parentId=" + destination.getId();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(403, response.getStatus());
        try {
            mountPoint.getVirtualFile(originPath);
        } catch (NotFoundException e) {
            fail("Source file not found. ");
        }
    }

    public void testMoveLockedFile() throws Exception {
        String lockToken = fileForMove.lock(0);
        String path = SERVICE_URI + "move/" + fileForMove.getId() + '?' + "parentId=" + moveTestDestinationFolder.getId() +
                      '&' + "lockToken=" + lockToken;
        String originPath = fileForMove.getPath();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        String expectedPath = moveTestDestinationFolder.getPath() + '/' + fileForMove.getName();
        try {
            mountPoint.getVirtualFile(originPath);
            fail("File must be moved. ");
        } catch (NotFoundException e) {
        }
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("Not found file in destination location. ");
        }
    }

    public void testMoveLockedFileNoLockToken() throws Exception {
        fileForMove.lock(0);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "move/" + fileForMove.getId() + '?' + "parentId=" + moveTestDestinationFolder.getId();
        String originPath = fileForMove.getPath();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals(403, response.getStatus());
        String expectedPath = moveTestDestinationFolder.getPath() + '/' + fileForMove.getName();
        try {
            mountPoint.getVirtualFile(originPath);
        } catch (NotFoundException e) {
            fail("Source file not found. ");
        }
        try {
            mountPoint.getVirtualFile(expectedPath);
            fail("File must not be moved since it is locked. ");
        } catch (NotFoundException e) {
        }
    }

    public void testMoveFolder() throws Exception {
        String path = SERVICE_URI + "move/" + folderForMove.getId() + '?' + "parentId=" + moveTestDestinationFolder.getId();
        String originPath = folderForMove.getPath();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        String expectedPath = moveTestDestinationFolder.getPath() + '/' + folderForMove.getName();
        try {
            mountPoint.getVirtualFile(originPath);
            fail("Folder must be moved. ");
        } catch (NotFoundException e) {
        }
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("Not found folder in destination location. ");
        }
        try {
            mountPoint.getVirtualFile(expectedPath + "/file");
        } catch (NotFoundException e) {
            fail("Child of folder missing after moving. ");
        }
    }

    public void testMoveFolderWithLockedFile() throws Exception {
        folderForMove.getChild("file").lock(0);
        String path = SERVICE_URI + "move/" + folderForMove.getId() + '?' + "parentId=" + moveTestDestinationFolder.getId();
        String originPath = folderForMove.getPath();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(403, response.getStatus());
        String expectedPath = moveTestDestinationFolder.getPath() + '/' + folderForMove.getName();
        try {
            mountPoint.getVirtualFile(originPath);
        } catch (NotFoundException e) {
            fail("Source file not found. ");
        }
        try {
            mountPoint.getVirtualFile(expectedPath);
            fail("Folder must not be moved since it contains locked file. ");
        } catch (NotFoundException e) {
        }
    }

    public void testMoveFolderAlreadyExist() throws Exception {
        moveTestDestinationFolder.createFolder(folderForMove.getName());
        String path = SERVICE_URI + "move/" + folderForMove.getId() + '?' + "parentId=" + moveTestDestinationFolder.getId();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(409, response.getStatus());
    }
}
