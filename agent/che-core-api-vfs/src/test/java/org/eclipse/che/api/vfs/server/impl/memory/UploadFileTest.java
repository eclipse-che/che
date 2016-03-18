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
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.test.mock.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author andrew00x */
public class UploadFileTest extends MemoryFileSystemTest {
    private String uploadTestFolderId;
    private String uploadTestFolderPath;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        VirtualFile uploadTestFolder = mountPoint.getRoot().createFolder(name);
        uploadTestFolderId = uploadTestFolder.getId();
        uploadTestFolderPath = uploadTestFolder.getPath();
    }

    public void testUploadNewFile() throws Exception {
        // Passed by browser.
        String fileName = "testUploadNewFile.txt";
        // File content.
        String fileContent = "test upload file";
        ContainerResponse response = doUploadFile(fileName, fileContent, "", "", false);
        assertEquals(200, response.getStatus());
        String expectedPath = uploadTestFolderPath + '/' + fileName;
        VirtualFile file = mountPoint.getVirtualFile(expectedPath);
        assertNotNull("File was not created in expected location. ", file);
        checkFileContext(fileContent, MediaType.TEXT_PLAIN, file);
    }

    public void testUploadNewFileInRootFolder() throws Exception {
        // Passed by browser.
        String fileName = "testUploadNewFile.txt";
        // File content.
        String fileContent = "test upload file";
        uploadTestFolderId = mountPoint.getRoot().getId();
        ContainerResponse response = doUploadFile(fileName, fileContent, "", "", false);
        assertEquals(200, response.getStatus());
        String expectedPath = "/" + fileName;
        VirtualFile file = mountPoint.getVirtualFile(expectedPath);
        assertNotNull("File was not created in expected location. ", file);
        checkFileContext(fileContent, MediaType.TEXT_PLAIN, file);
    }

    public void testUploadNewFileCustomizeName() throws Exception {
        // Passed by browser.
        String fileName = "testUploadNewFileCustomizeName";
        // File content.
        String fileContent = "test upload file with custom name";
        // Name of file passed in HTML form. If present it should be used instead of original file name.
        String formFileName = fileName + ".txt";
        ContainerResponse response = doUploadFile(fileName, fileContent, "", formFileName, false);
        assertEquals(200, response.getStatus());
        String expectedPath = uploadTestFolderPath + '/' + formFileName;
        VirtualFile file = mountPoint.getVirtualFile(expectedPath);
        assertNotNull("File was not created in expected location. ", file);
        checkFileContext(fileContent, MediaType.TEXT_PLAIN, file);
    }

    public void testUploadNewFileCustomizeMediaType() throws Exception {
        // Passed by browser.
        String fileName = "testUploadNewFileCustomizeMediaType";
        // File content.
        String fileContent = "test upload file with custom media type";
        // Name of file passed in HTML form. If present it should be used instead of original file name.
        String formFileName = fileName + ".txt";
        // Media type of file passed in HTML form. If present it should be used instead of original file media type.
        String formMediaType = MediaType.TEXT_PLAIN;
        ContainerResponse response =
                doUploadFile(fileName, fileContent, formMediaType, formFileName, false);
        assertEquals(200, response.getStatus());
        String expectedPath = uploadTestFolderPath + '/' + formFileName;
        VirtualFile file = mountPoint.getVirtualFile(expectedPath);
        assertNotNull("File was not created in expected location. ", file);
        checkFileContext(fileContent, MediaType.TEXT_PLAIN, file);
    }

    public void testUploadFileAlreadyExists() throws Exception {
        String fileName = "existedFile.txt";
        VirtualFile folder = mountPoint.getVirtualFileById(uploadTestFolderId);
        folder.createFile(fileName, new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        ContainerResponse response = doUploadFile(fileName, DEFAULT_CONTENT, "", "", false);
        assertEquals(200, response.getStatus());
        String entity = (String)response.getEntity();
        assertTrue(entity.contains("Item with the same name exists"));
        log.info(entity);
    }

    public void testUploadFileAlreadyExistsOverwrite() throws Exception {
        String fileName = "existedFileOverwrite.txt";
        VirtualFile folder = mountPoint.getVirtualFileById(uploadTestFolderId);
        folder.createFile(fileName, new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));

        String fileContent = "test upload and overwrite existed file";
        ContainerResponse response = doUploadFile(fileName, fileContent, "", "", true);
        assertEquals(200, response.getStatus());
        String expectedPath = uploadTestFolderPath + '/' + fileName;
        VirtualFile file = mountPoint.getVirtualFile(expectedPath);
        assertNotNull("File was not created in expected location. ", file);
        checkFileContext(fileContent, MediaType.TEXT_PLAIN, file);
    }

    private ContainerResponse doUploadFile(String fileName, String fileContent, String formMediaType, String formFileName,
                                           boolean formOverwrite) throws Exception {
        String path = SERVICE_URI + "uploadfile/" + uploadTestFolderId; //

        Map<String, List<String>> headers = new HashMap<>();
        List<String> contentType = new ArrayList<>();
        contentType.add("multipart/form-data; boundary=abcdef");
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);

        String uploadBodyPattern = "--abcdef\r\n"
                                   + "Content-Disposition: form-data; name=\"file\"; filename=\"%1$s\"\r\n\r\n"
                                   + "%2$s\r\n--abcdef\r\nContent-Disposition: form-data; name=\"mimeType\"\r\n\r\n%3$s"
                                   + "\r\n--abcdef\r\nContent-Disposition: form-data; name=\"name\"\r\n\r\n%4$s\r\n"
                                   + "--abcdef\r\nContent-Disposition: form-data; name=\"overwrite\"\r\n\r\n%5$b\r\n--abcdef--\r\n";
        byte[] formData =
                String.format(uploadBodyPattern, fileName, fileContent, formMediaType, formFileName,
                              formOverwrite).getBytes();
        EnvironmentContext env = new EnvironmentContext();
        env.put(HttpServletRequest.class, new MockHttpServletRequest("", new ByteArrayInputStream(formData),
                                                                     formData.length, HttpMethod.POST, headers));

        return launcher.service(HttpMethod.POST, path, BASE_URI, headers, formData, env);
    }
}
