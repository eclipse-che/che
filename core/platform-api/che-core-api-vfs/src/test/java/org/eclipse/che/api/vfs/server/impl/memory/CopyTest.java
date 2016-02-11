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
import org.eclipse.che.api.vfs.shared.dto.Item;

import org.everrest.core.impl.ContainerResponse;

import java.io.ByteArrayInputStream;

import javax.ws.rs.HttpMethod;

/** @author andrew00x */
public class CopyTest extends MemoryFileSystemTest {
    private VirtualFile copyTestDestinationFolder;
    private VirtualFile fileForCopy;
    private VirtualFile folderForCopy;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        VirtualFile parentFolder = mountPoint.getRoot().createFolder(name);

        folderForCopy = parentFolder.createFolder("CopyTest_FOLDER");
        // add child in folder
        fileForCopy = folderForCopy.createFile("CopyTest_FILE", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));

        copyTestDestinationFolder = mountPoint.getRoot().createFolder("CopyTest_DESTINATION");
    }

    public void testCopyFile() throws Exception {
        final String originPath = fileForCopy.getPath();
        String path = SERVICE_URI + "copy/" + fileForCopy.getId() + '?' + "parentId=" + copyTestDestinationFolder.getId();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        String expectedPath = copyTestDestinationFolder.getPath() + '/' + fileForCopy.getName();
        try {
            mountPoint.getVirtualFile(originPath);
        } catch (NotFoundException e) {
            fail("Source file not found. ");
        }
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("Not found file in destination location. ");
        }
        try {
            mountPoint.getVirtualFileById(((Item)response.getEntity()).getId());
        } catch (NotFoundException e) {
            fail("Copied file not accessible by id. ");
        }
    }

    public void testCopyFileAlreadyExist() throws Exception {
        final String originPath = fileForCopy.getPath();
        copyTestDestinationFolder.createFile("CopyTest_FILE", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        String path = SERVICE_URI + "copy/" + fileForCopy.getId() + '?' + "parentId=" + copyTestDestinationFolder.getId();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(409, response.getStatus());
        try {
            mountPoint.getVirtualFile(originPath);
        } catch (NotFoundException e) {
            fail("Source file not found. ");
        }
    }

    public void testCopyFileWrongParent() throws Exception {
        final String originPath = fileForCopy.getPath();
        VirtualFile destination =
                mountPoint.getRoot().createFile("destination", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        String path = SERVICE_URI + "copy/" + fileForCopy.getId() + '?' + "parentId=" + destination.getId();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(403, response.getStatus());
        try {
            mountPoint.getVirtualFile(originPath);
        } catch (NotFoundException e) {
            fail("Source file not found. ");
        }
    }

    public void testCopyFolder() throws Exception {
        String path = SERVICE_URI + "copy/" + folderForCopy.getId() + '?' + "parentId=" + copyTestDestinationFolder.getId();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        String expectedPath = copyTestDestinationFolder.getPath() + "/" + folderForCopy.getName();
        final String originPath = folderForCopy.getPath();
        try {
            mountPoint.getVirtualFile(originPath);
        } catch (NotFoundException e) {
            fail("Source folder not found. ");
        }
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("Not found folder in destination location. ");
        }
        try {
            mountPoint.getVirtualFileById(((Item)response.getEntity()).getId());
        } catch (NotFoundException e) {
            fail("Copied folder not accessible by id. ");
        }
        try {
            mountPoint.getVirtualFile(expectedPath + "/CopyTest_FILE");
        } catch (NotFoundException e) {
            fail("Child of folder missing after coping. ");
        }
        String childCopyId = mountPoint.getVirtualFile(expectedPath + "/CopyTest_FILE").getId();
        try {
            mountPoint.getVirtualFileById(childCopyId);
        } catch (NotFoundException e) {
            fail("Child of copied folder not accessible by id. ");
        }
    }

    public void testCopyFolderAlreadyExist() throws Exception {
        final String originPath = folderForCopy.getPath();
        copyTestDestinationFolder.createFolder("CopyTest_FOLDER");
        String path = SERVICE_URI + "copy/" + folderForCopy.getId() + "?" + "parentId=" + copyTestDestinationFolder.getId();
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(409, response.getStatus());
        try {
            mountPoint.getVirtualFile(originPath);
        } catch (NotFoundException e) {
            fail("Source folder not found. ");
        }
    }
}
