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
import org.eclipse.che.api.vfs.server.ContentStream;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.shared.dto.Item;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

/** @author andrew00x */
public class CreateTest extends MemoryFileSystemTest {
    private String      createTestFolderId;
    private String      createTestFolderPath;
    private VirtualFile createTestFolder;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        createTestFolder = mountPoint.getRoot().createFolder(name);
        createTestFolderId = createTestFolder.getId();
        createTestFolderPath = createTestFolder.getPath();
    }

    public void testCreateFile() throws Exception {
        String name = "testCreateFile.txt";
        String content = "test create file";
        String path = SERVICE_URI + "file/" + createTestFolderId + '?' + "name=" + name; //

        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, content.getBytes(), null);
        assertEquals(200, response.getStatus());
        String expectedPath = createTestFolderPath + "/" + name;
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("File was not created in expected location. ");
        }
        try {
            mountPoint.getVirtualFileById(((Item)response.getEntity()).getId());
        } catch (NotFoundException e) {
            fail("Created file not accessible by id. ");
        }
        VirtualFile file = mountPoint.getVirtualFile(expectedPath);
        checkFileContext(content, MediaType.TEXT_PLAIN, file);
    }

    public void testCreateFileInRoot() throws Exception {
        String name = "testCreateFileInRoot.txt";
        String content = "test create file";
        String path = SERVICE_URI + "file/" + mountPoint.getRoot().getId() + '?' + "name=" + name;

        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, content.getBytes(), null);
        assertEquals(200, response.getStatus());
        String expectedPath = "/" + name;
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("File was not created in expected location. ");
        }
        try {
            mountPoint.getVirtualFileById(((Item)response.getEntity()).getId());
        } catch (NotFoundException e) {
            fail("Created file not accessible by id. ");
        }
        VirtualFile file = mountPoint.getVirtualFile(expectedPath);
        checkFileContext(content, MediaType.TEXT_PLAIN, file);
    }

    public void testCreateFileNoContent() throws Exception {
        String name = "testCreateFileNoContent";
        String path = SERVICE_URI + "file/" + createTestFolderId + '?' + "name=" + name;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);

        assertEquals(200, response.getStatus());
        String expectedPath = createTestFolderPath + "/" + name;
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("File was not created in expected location. ");
        }
        try {
            mountPoint.getVirtualFileById(((Item)response.getEntity()).getId());
        } catch (NotFoundException e) {
            fail("Created file not accessible by id. ");
        }
        VirtualFile file = mountPoint.getVirtualFile(expectedPath);
        ContentStream contentStream = file.getContent();
        assertEquals(0, contentStream.getLength());
    }

    public void testCreateFileNoMediaType() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String name = "testCreateFileNoMediaType";
        String content = "test create file without media type";
        String path = SERVICE_URI + "file/" + createTestFolderId + '?' + "name=" + name;

        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, content.getBytes(), writer, null);
        assertEquals(200, response.getStatus());
        String expectedPath = createTestFolderPath + "/" + name;
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("File was not created in expected location. ");
        }
        try {
            mountPoint.getVirtualFileById(((Item)response.getEntity()).getId());
        } catch (NotFoundException e) {
            fail("Created file not accessible by id. ");
        }
        VirtualFile file = mountPoint.getVirtualFile(expectedPath);
        checkFileContext(content, MediaType.APPLICATION_OCTET_STREAM, file);
    }

    public void testCreateFileNoName() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "file/" + createTestFolderId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, DEFAULT_CONTENT.getBytes(), writer, null);
        assertEquals(500, response.getStatus());
        log.info(new String(writer.getBody()));
    }

//    public void testCreateFileNoPermissions() throws Exception {
//        Principal adminPrincipal = createPrincipal("admin", Principal.Type.USER);
//        Principal userPrincipal = createPrincipal("john", Principal.Type.USER);
//        Map<Principal, Set<String>> permissions = new HashMap<>(2);
//        permissions.put(adminPrincipal, Sets.newHashSet(BasicPermissions.ALL.value()));
//        permissions.put(userPrincipal, Sets.newHashSet(BasicPermissions.READ.value()));
//        createTestFolder.updateACL(createAcl(permissions), true, null);
//
//        String name = "testCreateFileNoPermissions";
//        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
//        String path = SERVICE_URI + "file/" + createTestFolderId + '?' + "name=" + name;
//        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, DEFAULT_CONTENT.getBytes(), writer, null);
//        assertEquals(403, response.getStatus());
//        log.info(new String(writer.getBody()));
//    }

    public void testCreateFileWrongParent() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String name = "testCreateFileWrongParent";
        String path = SERVICE_URI + "file/" + createTestFolderId + "_WRONG_ID" + '?' + "name=" + name;
        ContainerResponse response =
                launcher.service(HttpMethod.POST, path, BASE_URI, null, DEFAULT_CONTENT.getBytes(), writer, null);
        assertEquals(404, response.getStatus());
        log.info(new String(writer.getBody()));
    }

    public void testCreateFolder() throws Exception {
        String name = "testCreateFolder";
        String path = SERVICE_URI + "folder/" + createTestFolderId + '?' + "name=" + name;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        String expectedPath = createTestFolderPath + "/" + name;
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("Folder was not created in expected location. ");
        }
        try {
            mountPoint.getVirtualFileById(((Item)response.getEntity()).getId());
        } catch (NotFoundException e) {
            fail("Created folder not accessible by id. ");
        }
    }

    public void testCreateFolderInRoot() throws Exception {
        String name = "testCreateFolderInRoot";
        String path = SERVICE_URI + "folder/" + mountPoint.getRoot().getId() + '?' + "name=" + name;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        String expectedPath = "/" + name;
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("Folder was not created in expected location. ");
        }
        try {
            mountPoint.getVirtualFileById(((Item)response.getEntity()).getId());
        } catch (NotFoundException e) {
            fail("Created folder not accessible by id. ");
        }
    }

    public void testCreateFolderNoName() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "folder/" + createTestFolderId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
        assertEquals(500, response.getStatus());
        log.info(new String(writer.getBody()));
    }

//    public void testCreateFolderNoPermissions() throws Exception {
//        Principal adminPrincipal = createPrincipal("admin", Principal.Type.USER);
//        Map<Principal, Set<String>> permissions = new HashMap<>(1);
//        permissions.put(adminPrincipal, Sets.newHashSet(BasicPermissions.ALL.value()));
//        createTestFolder.updateACL(createAcl(permissions), true, null);
//
//        String name = "testCreateFolderNoPermissions";
//        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
//        String path = SERVICE_URI + "folder/" + createTestFolderId + '?' + "name=" + name;
//        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
//        assertEquals(403, response.getStatus());
//        log.info(new String(writer.getBody()));
//    }

    public void testCreateFolderWrongParent() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String name = "testCreateFolderWrongParent";
        String path = SERVICE_URI + "folder/" + createTestFolderId + "_WRONG_ID" + '?' + "name=" + name;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
        assertEquals(404, response.getStatus());
        log.info(new String(writer.getBody()));
    }

    public void testCreateFolderHierarchy() throws Exception {
        String name = "testCreateFolderHierarchy/1/2/3/4/5";
        String path = SERVICE_URI + "folder/" + createTestFolderId + '?' + "name=" + name;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null, null);
        assertEquals(200, response.getStatus());
        String expectedPath = createTestFolderPath + "/" + name;
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("Folder was not created in expected location. ");
        }
        try {
            mountPoint.getVirtualFileById(((Item)response.getEntity()).getId());
        } catch (NotFoundException e) {
            fail("Created folder not accessible by id. ");
        }
    }

    public void testCreateFolderHierarchy2() throws Exception {
        // create some items in path
        String name = "testCreateFolderHierarchy/1/2/3";
        String path = SERVICE_URI + "folder/" + createTestFolderId + '?' + "name=" + name;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null, null);
        assertEquals(200, response.getStatus());
        String expectedPath = createTestFolderPath + "/" + name;
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("Folder was not created in expected location. ");
        }
        try {
            mountPoint.getVirtualFileById(((Item)response.getEntity()).getId());
        } catch (NotFoundException e) {
            fail("Created folder not accessible by id. ");
        }
        // create the rest of path
        name += "/4/5";
        path = SERVICE_URI + "folder/" + createTestFolderId + '?' + "name=" + name;
        response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null, null);
        assertEquals(200, response.getStatus());
        expectedPath = createTestFolderPath + "/" + name;
        try {
            mountPoint.getVirtualFile(expectedPath);
        } catch (NotFoundException e) {
            fail("Folder was not created in expected location. ");
        }
        try {
            mountPoint.getVirtualFileById(((Item)response.getEntity()).getId());
        } catch (NotFoundException e) {
            fail("Created folder not accessible by id. ");
        }
    }
}
