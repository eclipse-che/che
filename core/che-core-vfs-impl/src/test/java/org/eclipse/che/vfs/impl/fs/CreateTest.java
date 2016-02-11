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
package org.eclipse.che.vfs.impl.fs;

import com.google.common.collect.Sets;

import org.eclipse.che.api.vfs.shared.dto.Folder;
import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.UserImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import javax.ws.rs.HttpMethod;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.BasicPermissions;

public class CreateTest extends LocalFileSystemTest {
    private String folderId;
    private String folderPath;

    private String protectedFolderPath;
    private String protectedFolderId;

    private String fileId;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderPath = createDirectory(testRootPath, "CreateTest_Folder");
        protectedFolderPath = createDirectory(testRootPath, "CreateTest_ProtectedFolder");
        String filePath = createFile(testRootPath, "CreateTest_File", DEFAULT_CONTENT_BYTES);

        Map<Principal, Set<String>> permissions = new HashMap<>(2);
        Principal user = DtoFactory.getInstance().createDto(Principal.class).withName("andrew").withType(Principal.Type.USER);
        Principal admin = DtoFactory.getInstance().createDto(Principal.class).withName("admin").withType(Principal.Type.USER);
        permissions.put(user, Sets.newHashSet(BasicPermissions.ALL.value()));
        permissions.put(admin, Sets.newHashSet(BasicPermissions.READ.value()));
        writePermissions(protectedFolderPath, permissions);

        folderId = pathToId(folderPath);
        protectedFolderId = pathToId(protectedFolderPath);
        fileId = pathToId(filePath);
    }

    public void testCreateFile() throws Exception {
        String name = "testCreateFile";
        String content = "test create file";
        String requestPath = SERVICE_URI + "file/" + folderId + '?' + "name=" + name;

        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, content.getBytes(), null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        String expectedPath = folderPath + '/' + name;
        assertTrue("File was not created in expected location. ", exists(expectedPath));
        assertEquals(content, new String(readFile(expectedPath)));
    }

    public void testCreateFileAlreadyExists() throws Exception {
        String name = "testCreateFileAlreadyExists";
        createFile(folderPath, name, null);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "file/" + folderId + '?' + "name=" + name;
        ContainerResponse response =
                launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, DEFAULT_CONTENT_BYTES, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals(409, response.getStatus());
    }

    public void testCreateFileInRoot() throws Exception {
        String name = "FileInRoot";
        String content = "test create file";
        String requestPath = SERVICE_URI + "file/" + ROOT_ID + '?' + "name=" + name;

        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response =
                launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, content.getBytes(), writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        String expectedPath = '/' + name;
        assertTrue("File was not created in expected location. ", exists(expectedPath));
        assertEquals(content, new String(readFile(expectedPath)));
    }

    public void testCreateFileNoContent() throws Exception {
        String name = "testCreateFileNoContent";
        String requestPath = SERVICE_URI + "file/" + folderId + '?' + "name=" + name;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        String expectedPath = folderPath + '/' + name;
        assertTrue("File was not created in expected location. ", exists(expectedPath));
        assertTrue(readFile(expectedPath).length == 0);
    }

    public void testCreateFileNoMediaType() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String name = "testCreateFileNoMediaType";
        String content = "test create file without media type";
        String requestPath = SERVICE_URI + "file/" + folderId + '?' + "name=" + name;
        ContainerResponse response =
                launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, content.getBytes(), writer, null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        String expectedPath = folderPath + '/' + name;
        assertTrue("File was not created in expected location. ", exists(expectedPath));
        assertEquals(content, new String(readFile(expectedPath)));
    }

    public void testCreateFileNoName() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "file/" + folderId;
        ContainerResponse response =
                launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, DEFAULT_CONTENT_BYTES, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals(500, response.getStatus());
    }

    public void testCreateFileHavePermissions() throws Exception {
        String name = "testCreateFileHavePermissions";
        String content = "test create file";
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "file/" + protectedFolderId + '?' + "name=" + name;
        // Replace default principal by principal who has write permission.
        EnvironmentContext.getCurrent().setUser(new UserImpl("andrew", "andrew", null, Arrays.asList("workspace/developer"), false));
        // --
        ContainerResponse response =
                launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, content.getBytes(), writer, null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        String expectedPath = protectedFolderPath + '/' + name;
        assertTrue("File was not created in expected location. ", exists(expectedPath));
        assertEquals(content, new String(readFile(expectedPath)));
    }

    public void testCreateFileWrongParent() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String name = "testCreateFileWrongParent";
        // Try to create new file in other file.
        String requestPath = SERVICE_URI + "file/" + fileId + '?' + "name=" + name;
        ContainerResponse response =
                launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, DEFAULT_CONTENT_BYTES, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals(403, response.getStatus());
    }

    public void testCreateFileWrongParentId() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String name = "testCreateFileWrongParentId";
        String requestPath = SERVICE_URI + "file/" + folderId + "_WRONG_ID" + '?' + "name=" + name;
        ContainerResponse response =
                launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, DEFAULT_CONTENT_BYTES, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals(404, response.getStatus());
    }

    public void testCreateFolder() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String name = "testCreateFolder";
        String requestPath = SERVICE_URI + "folder/" + folderId + '?' + "name=" + name;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        String expectedPath = folderPath + '/' + name;
        assertTrue("Folder was not created in expected location. ", exists(expectedPath));
    }

    public void testCreateFolderInRoot() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String name = "FolderInRoot";
        String requestPath = SERVICE_URI + "folder/" + ROOT_ID + '?' + "name=" + name;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        String expectedPath = '/' + name;
        assertTrue("Folder was not created in expected location. ", exists(expectedPath));
    }

    public void testCreateFolderNoName() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "folder/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals(500, response.getStatus());
    }

    public void testCreateFolderWrongParentId() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String name = "testCreateFolderWrongParentId";
        String requestPath = SERVICE_URI + "folder/" + folderId + "_WRONG_ID" + '?' + "name=" + name;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals(404, response.getStatus());
    }

    public void testCreateFolderHierarchy() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String name = "testCreateFolderHierarchy/1/2/3/4/5";
        String requestPath = SERVICE_URI + "folder/" + folderId + '?' + "name=" + name;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        String expectedPath = folderPath + '/' + name;
        assertTrue("Folder was not created in expected location. ", exists(expectedPath));
    }

    public void testCreateFolderHierarchyExists() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String name = "testCreateFolderHierarchyExists/1/2/3/4/5";
        createDirectory(folderPath, name);
        String requestPath = SERVICE_URI + "folder/" + folderId + '?' + "name=" + name;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, DEFAULT_CONTENT_BYTES, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals(409, response.getStatus());
    }

    public void testCreateFolderHierarchy2() throws Exception {
        // create some items in path
        String name = "testCreateFolderHierarchy2/1/2/3";
        createDirectory(folderPath, name);
        // create the rest of path
        name += "/4/5";
        String requestPath = SERVICE_URI + "folder/" + folderId + '?' + "name=" + name;
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        assertEquals(folderPath + "/testCreateFolderHierarchy2/1/2/3/4/5", ((Folder)response.getEntity()).getPath());
        String expectedPath = folderPath + '/' + name;
        assertTrue("Folder was not created in expected location. ", exists(expectedPath));
    }
}
