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

import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.dto.server.DtoFactory;

import com.google.common.collect.Sets;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;
import org.everrest.test.mock.MockHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.BasicPermissions;

public class UploadFileTest extends LocalFileSystemTest {
    private String folderId;
    private String folderPath;

    private String protectedFolderId;
    private String protectedFolderPath;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        folderPath = createDirectory(testRootPath, "UploadTest");
        protectedFolderPath = createDirectory(testRootPath, "UploadTest_Protected");

        Map<Principal, Set<String>> permissions = new HashMap<>(2);
        Principal user = DtoFactory.getInstance().createDto(Principal.class).withName("andrew").withType(Principal.Type.USER);
        Principal admin = DtoFactory.getInstance().createDto(Principal.class).withName("admin").withType(Principal.Type.USER);

        permissions.put(user, Sets.newHashSet(BasicPermissions.READ.value(), BasicPermissions.WRITE.value()));
        permissions.put(admin, Sets.newHashSet(BasicPermissions.READ.value()));
        writePermissions(protectedFolderPath, permissions);

        folderId = pathToId(folderPath);
        protectedFolderId = pathToId(protectedFolderPath);
    }

    public void testUploadNewFile() throws Exception {
        final String fileName = "testUploadNewFile";
        final String fileContent = "test upload file";
        ContainerResponse response = doUploadFile(folderId, fileName, fileContent, "", "", false);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        assertTrue(((String)response.getEntity()).isEmpty()); // empty if successful
        String expectedPath = folderPath + '/' + fileName;
        assertTrue("File was not created in expected location. ", exists(expectedPath));
        assertEquals(fileContent, new String(readFile(expectedPath)));
    }

    public void testUploadNewFileInRootFolder() throws Exception {
        final String fileName = "testUploadNewFile";
        final String fileContent = "test upload file";
        folderId = ROOT_ID;
        ContainerResponse response = doUploadFile(folderId, fileName, fileContent, "", "", false);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        assertTrue(((String)response.getEntity()).isEmpty()); // empty if successful
        String expectedPath = '/' + fileName;
        assertTrue("File was not created in expected location. ", exists(expectedPath));
        assertEquals(fileContent, new String(readFile(expectedPath)));
    }

    public void testUploadNewFileCustomizeName() throws Exception {
        final String fileName = "testUploadNewFileCustomizeName";
        final String fileContent = "test upload file with custom name";
        // Name of file passed in HTML form. If present it should be used instead of original file name.
        final String formFileName = fileName + ".txt";
        ContainerResponse response = doUploadFile(folderId, fileName, fileContent, "", formFileName, false);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        assertTrue(((String)response.getEntity()).isEmpty()); // empty if successful
        String expectedPath = folderPath + '/' + formFileName;
        assertTrue("File was not created in expected location. ", exists(expectedPath));
        assertEquals(fileContent, new String(readFile(expectedPath)));
    }

    public void testUploadNewFileCustomizeMediaType() throws Exception {
        final String fileName = "testUploadNewFileCustomizeMediaType";
        final String fileContent = "test upload file with custom media type";
        final String formFileName = fileName + ".txt";
        final String formMediaType = MediaType.TEXT_PLAIN; // should be used instead of fileMediaType
        ContainerResponse response =
                doUploadFile(folderId, fileName, fileContent, formMediaType, formFileName, false);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        assertTrue(((String)response.getEntity()).isEmpty()); // empty if successful
        String expectedPath = folderPath + '/' + formFileName;
        assertTrue("File was not created in expected location. ", exists(expectedPath));
        assertEquals(fileContent, new String(readFile(expectedPath)));
    }

    public void testUploadFileAlreadyExists() throws Exception {
        final String fileName = "existedFile";
        final String fileContent = "existed file";
        createFile(folderPath, fileName, fileContent.getBytes());
        ContainerResponse response = doUploadFile(folderId, fileName, DEFAULT_CONTENT, "", "", false);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus()); // always 200 even for errors
        assertTrue(((String)response.getEntity()).startsWith("<pre>message: "));
        assertEquals(fileContent, new String(readFile(folderPath + '/' + fileName)));
    }

    public void testUploadFileAlreadyExistsAndLocked() throws Exception {
        final String fileName = "existedLockedFile";
        final String fileContent = "existed locked file";
        String path = createFile(folderPath, fileName, fileContent.getBytes());
        createLock(path, "1234567890", Long.MAX_VALUE);
        // File is locked and may not be overwritten even if 'overwrite' parameter is 'true'
        ContainerResponse response = doUploadFile(folderId, fileName, DEFAULT_CONTENT, "", "", true);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus()); // always 200 even for errors
        assertTrue(((String)response.getEntity()).startsWith("<pre>message: "));
        assertEquals(fileContent, new String(readFile(folderPath + '/' + fileName)));
    }

    public void testUploadFileAlreadyExistsOverwrite() throws Exception {
        String fileName = "existedFileOverwrite";
        final String fileContent = "existed file overwrite";
        createFile(folderPath, fileName, fileContent.getBytes());
        final String newFileContent = "test upload and overwrite existed file";
        ContainerResponse response = doUploadFile(folderId, fileName, newFileContent, "", "", true);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        assertTrue(((String)response.getEntity()).isEmpty()); // empty if successful
        String expectedPath = folderPath + '/' + fileName;
        assertTrue("File was not created in expected location. ", exists(expectedPath));
        assertEquals(newFileContent, new String(readFile(expectedPath)));
    }

    private ContainerResponse doUploadFile(String parentId,
                                           String fileName,
                                           String fileContent,
                                           String formMediaType,
                                           String formFileName,
                                           boolean formOverwrite) throws Exception {
        String requestPath = SERVICE_URI + "uploadfile/" + parentId;
        Map<String, List<String>> headers = new HashMap<>(1);
        headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList("multipart/form-data; boundary=abcdef"));
        byte[] formData = String.format(uploadBodyPattern,
                                        fileName, fileContent, formMediaType, formFileName, formOverwrite)
                                .getBytes();
        EnvironmentContext env = new EnvironmentContext();
        env.put(HttpServletRequest.class, new MockHttpServletRequest("", new ByteArrayInputStream(formData),
                                                                     formData.length, HttpMethod.POST, headers));
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, headers, formData, writer, env);
        if (writer.getBody() != null) {
            log.info(new String(writer.getBody()));
        }
        return response;
    }

    private static final String uploadBodyPattern =
            "--abcdef\r\nContent-Disposition: form-data; name=\"file\"; filename=\"%1$s\"\r\n\r\n%2$s\r\n" +
            "--abcdef\r\nContent-Disposition: form-data; name=\"mimeType\"\r\n\r\n%3$s\r\n" +
            "--abcdef\r\nContent-Disposition: form-data; name=\"name\"\r\n\r\n%4$s\r\n" +
            "--abcdef\r\nContent-Disposition: form-data; name=\"overwrite\"\r\n\r\n%5$b\r\n" +
            "--abcdef--\r\n";
}
