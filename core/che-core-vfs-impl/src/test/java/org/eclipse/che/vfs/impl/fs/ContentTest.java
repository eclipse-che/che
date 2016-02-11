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

import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.BasicPermissions;

public class ContentTest extends LocalFileSystemTest {
    private final String lockToken     = "1234567890abcdef";
    private final byte[] content       = "__ContentTest__".getBytes();
    private final byte[] updateContent = "__UpdateContentTest__".getBytes();

    private String filePath;
    private String fileId;

    private String protectedFilePath;
    private String protectedFileId;

    private String lockedFilePath;
    private String lockedFileId;

    private String folderId;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        filePath = createFile(testRootPath, "ContentTest_File.txt", content);
        lockedFilePath = createFile(testRootPath, "ContentTest_LockedFile.txt", content);
        protectedFilePath = createFile(testRootPath, "ContentTest_ProtectedFile.txt", content);
        String folderPath = createDirectory(testRootPath, "ContentTest_Folder");

        createLock(lockedFilePath, lockToken, Long.MAX_VALUE);

        Map<Principal, Set<String>> permissions = new HashMap<>(1);
        Principal principal = DtoFactory.getInstance().createDto(Principal.class).withName("andrew").withType(Principal.Type.USER);
        permissions.put(principal, Sets.newHashSet(BasicPermissions.ALL.value()));
        writePermissions(protectedFilePath, permissions);

        fileId = pathToId(filePath);
        lockedFileId = pathToId(lockedFilePath);
        protectedFileId = pathToId(protectedFilePath);
        folderId = pathToId(folderPath);
    }

    public void testGetContent() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "content/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        assertTrue(Arrays.equals(content, writer.getBody()));
        assertEquals(MediaType.TEXT_PLAIN, writer.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    public void testDownloadFile() throws Exception {
        // Expect the same as 'get content' plus header HttpHeaders.CONTENT_DISPOSITION.
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "downloadfile/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        assertTrue(Arrays.equals(content, writer.getBody()));
        assertEquals(MediaType.TEXT_PLAIN, writer.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals(String.format("attachment; filename=\"%s\"", "ContentTest_File.txt"),
                     writer.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    }

    public void testGetContentFolder() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "content/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals(403, response.getStatus());
    }

    public void testGetContentByPath() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "contentbypath" + filePath;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        log.info(new String(writer.getBody()));
        assertTrue(Arrays.equals(content, writer.getBody()));
        assertEquals(MediaType.TEXT_PLAIN, writer.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    public void testUpdateContent() throws Exception {
        String requestPath = SERVICE_URI + "content/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, updateContent, null);
        assertEquals(204, response.getStatus());
        assertTrue(Arrays.equals(updateContent, readFile(filePath)));
    }

    public void testUpdateContentFolder() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "content/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, updateContent, writer, null);
        assertEquals(403, response.getStatus());
        log.info(new String(writer.getBody()));
    }

    public void testUpdateContentLocked() throws Exception {
        String requestPath = SERVICE_URI + "content/" + lockedFileId + '?' + "lockToken=" + lockToken;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, updateContent, null);
        // File is locked.
        assertEquals(204, response.getStatus());
        assertTrue(Arrays.equals(updateContent, readFile(lockedFilePath))); // content updated
    }

    public void testUpdateContentLockedNoLockToken() throws Exception {
        String requestPath = SERVICE_URI + "content/" + lockedFileId;
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, updateContent, writer, null);
        // File is locked.
        assertEquals(403, response.getStatus());
        log.info(new String(writer.getBody()));
        assertTrue("Content must not be updated", Arrays.equals(content, readFile(lockedFilePath)));
        assertNull("Properties must not be updated", readProperties(lockedFilePath));
    }
}
