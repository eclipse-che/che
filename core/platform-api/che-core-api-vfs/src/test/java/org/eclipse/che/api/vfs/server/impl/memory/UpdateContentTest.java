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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/** @author andrew00x */
public class UpdateContentTest extends MemoryFileSystemTest {
    private String fileId;
    private String folderId;
    private String content = "__UpdateContentTest__";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        VirtualFile updateContentTestFolder = mountPoint.getRoot().createFolder(name);
        VirtualFile file = updateContentTestFolder.createFile("UpdateContentTest_FILE.txt",
                                                              new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        fileId = file.getId();
        VirtualFile folder = updateContentTestFolder.createFolder("UpdateContentTest_FOLDER");
        folderId = folder.getId();
    }

    public void testUpdateContent() throws Exception {
        String path = SERVICE_URI + "content/" + fileId;

        Map<String, List<String>> headers = new HashMap<>();
        List<String> contentType = new ArrayList<>();
        contentType.add(MediaType.TEXT_PLAIN);
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);

        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, headers, content.getBytes(), null);
        assertEquals(204, response.getStatus());

        VirtualFile file = mountPoint.getVirtualFileById(fileId);
        checkFileContext(content, MediaType.TEXT_PLAIN, file);
    }

    public void testUpdateContentFolder() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "content/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, content.getBytes(), writer, null);
        assertEquals(403, response.getStatus());
        log.info(new String(writer.getBody()));
    }

    public void testUpdateContentLocked() throws Exception {
        VirtualFile file = mountPoint.getVirtualFileById(fileId);
        String lockToken = file.lock(0);

        String path = SERVICE_URI + "content/" + fileId + '?' + "lockToken=" + lockToken;

        Map<String, List<String>> headers = new HashMap<>();
        List<String> contentType = new ArrayList<>();
        contentType.add(MediaType.TEXT_PLAIN);
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);

        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, headers, content.getBytes(), null);
        assertEquals(204, response.getStatus());

        file = mountPoint.getVirtualFileById(fileId);
        checkFileContext(content, MediaType.TEXT_PLAIN, file);
    }

    public void testUpdateContentLockedNoLockToken() throws Exception {
        VirtualFile file = mountPoint.getVirtualFileById(fileId);
        file.lock(0);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "content/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
        assertEquals(403, response.getStatus());
        log.info(new String(writer.getBody()));
    }
}
