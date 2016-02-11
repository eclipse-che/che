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

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import java.io.ByteArrayInputStream;

/** @author andrew00x */
public class GetContentTest extends MemoryFileSystemTest {
    private String fileId;
    private String fileName;
    private String folderId;
    private String content = "__GetContentTest__";
    private String filePath;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        VirtualFile getContentTestFolder = mountPoint.getRoot().createFolder(name);

        VirtualFile file =
                getContentTestFolder.createFile("GetContentTest_FILE.txt", new ByteArrayInputStream(content.getBytes()));
        fileId = file.getId();
        fileName = file.getName();
        filePath = file.getPath();

        VirtualFile folder = getContentTestFolder.createFolder("GetContentTest_FOLDER");
        folderId = folder.getId();
    }

    public void testGetContent() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "content/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        assertEquals(200, response.getStatus());
        //log.info(new String(writer.getBody()));
        assertEquals(content, new String(writer.getBody()));
        assertEquals(MediaType.TEXT_PLAIN, writer.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    public void testDownloadFile() throws Exception {
        // Expect the same as 'get content' plus header HttpHeaders.CONTENT_DISPOSITION.
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "downloadfile/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        assertEquals(200, response.getStatus());
        //log.info(new String(writer.getBody()));
        assertEquals(content, new String(writer.getBody()));
        assertEquals(MediaType.TEXT_PLAIN, writer.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        assertEquals("attachment; filename=\"" + fileName + "\"", writer.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
    }

    public void testGetContentFolder() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "content/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        assertEquals(403, response.getStatus());
        log.info(new String(writer.getBody()));
    }

    public void testGetContentByPath() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "contentbypath" + filePath;
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        assertEquals(200, response.getStatus());
        //log.info(new String(writer.getBody()));
        assertEquals(content, new String(writer.getBody()));
        assertEquals(MediaType.TEXT_PLAIN, writer.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }

    public void testGetContentByPathWithVersionID() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "contentbypath" + filePath + '?' + "versionId=" + "0";
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        assertEquals(200, response.getStatus());
        //log.info(new String(writer.getBody()));
        assertEquals(content, new String(writer.getBody()));
        assertEquals(MediaType.TEXT_PLAIN, writer.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
    }
}
