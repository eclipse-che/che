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
import org.eclipse.che.commons.lang.ws.rs.ExtMediaType;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;

/** @author andrew00x */
public class ExportTest extends MemoryFileSystemTest {
    private String exportFolderId;

    private Set<String> expectedExportTestRootZipItems = new HashSet<>();
    private Set<String> expectedExportFolderZipItems   = new HashSet<>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        VirtualFile exportTestFolder = mountPoint.getRoot().createFolder(name);

//      Create in exportTestFolder folder next files and folders:
//      ----------------------------
//         folder1/
//         folder2/
//         folder3/
//         folder1/file1.txt
//         folder1/folder12/
//         folder2/file2.txt
//         folder2/folder22/
//         folder3/file3.txt
//         folder3/folder32/
//         folder1/folder12/file12.txt
//         folder2/folder22/file22.txt
//         folder3/folder32/file32.txt
//      ----------------------------

        VirtualFile folder1 = exportTestFolder.createFolder("folder1");
        VirtualFile folder2 = exportTestFolder.createFolder("folder2");
        VirtualFile folder3 = exportTestFolder.createFolder("folder3");

        VirtualFile file1 = folder1.createFile("file1.txt", new ByteArrayInputStream(DEFAULT_CONTENT_BYTES));
        VirtualFile file2 = folder2.createFile("file2.txt", new ByteArrayInputStream(DEFAULT_CONTENT_BYTES));
        VirtualFile file3 = folder3.createFile("file3.txt", new ByteArrayInputStream(DEFAULT_CONTENT_BYTES));

        VirtualFile folder12 = folder1.createFolder("folder12");
        VirtualFile folder22 = folder2.createFolder("folder22");
        VirtualFile folder32 = folder3.createFolder("folder32");

        VirtualFile file12 = folder12.createFile("file12.txt", new ByteArrayInputStream(DEFAULT_CONTENT_BYTES));
        VirtualFile file22 = folder22.createFile("file22.txt", new ByteArrayInputStream(DEFAULT_CONTENT_BYTES));
        VirtualFile file32 = folder32.createFile("file32.txt", new ByteArrayInputStream(DEFAULT_CONTENT_BYTES));

        expectedExportFolderZipItems.add("folder1/");
        expectedExportFolderZipItems.add("folder2/");
        expectedExportFolderZipItems.add("folder3/");
        expectedExportFolderZipItems.add("folder1/file1.txt");
        expectedExportFolderZipItems.add("folder1/folder12/");
        expectedExportFolderZipItems.add("folder2/file2.txt");
        expectedExportFolderZipItems.add("folder2/folder22/");
        expectedExportFolderZipItems.add("folder3/file3.txt");
        expectedExportFolderZipItems.add("folder3/folder32/");
        expectedExportFolderZipItems.add("folder1/folder12/file12.txt");
        expectedExportFolderZipItems.add("folder2/folder22/file22.txt");
        expectedExportFolderZipItems.add("folder3/folder32/file32.txt");

        exportFolderId = exportTestFolder.getId();

        expectedExportTestRootZipItems.add(exportTestFolder.getName() + '/');
        expectedExportTestRootZipItems.add(exportTestFolder.getName() + "/folder1/");
        expectedExportTestRootZipItems.add(exportTestFolder.getName() + "/folder2/");
        expectedExportTestRootZipItems.add(exportTestFolder.getName() + "/folder3/");
        expectedExportTestRootZipItems.add(exportTestFolder.getName() + "/folder1/file1.txt");
        expectedExportTestRootZipItems.add(exportTestFolder.getName() + "/folder1/folder12/");
        expectedExportTestRootZipItems.add(exportTestFolder.getName() + "/folder2/file2.txt");
        expectedExportTestRootZipItems.add(exportTestFolder.getName() + "/folder2/folder22/");
        expectedExportTestRootZipItems.add(exportTestFolder.getName() + "/folder3/file3.txt");
        expectedExportTestRootZipItems.add(exportTestFolder.getName() + "/folder3/folder32/");
        expectedExportTestRootZipItems.add(exportTestFolder.getName() + "/folder1/folder12/file12.txt");
        expectedExportTestRootZipItems.add(exportTestFolder.getName() + "/folder2/folder22/file22.txt");
        expectedExportTestRootZipItems.add(exportTestFolder.getName() + "/folder3/folder32/file32.txt");
    }

    public void testExportRootFolder() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "export/" + mountPoint.getRoot().getId();
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        assertEquals(200, response.getStatus());
        assertEquals(ExtMediaType.APPLICATION_ZIP, writer.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
        checkZipItems(expectedExportTestRootZipItems, new ZipInputStream(new ByteArrayInputStream(writer.getBody())));
    }

    public void testExportFile() throws Exception {
        VirtualFile file = mountPoint.getVirtualFileById(exportFolderId)
                                     .createFile("export_test_file.txt", new ByteArrayInputStream(DEFAULT_CONTENT_BYTES));
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "export/" + file.getId();
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        assertEquals(403, response.getStatus());
    }

    private void checkZipItems(Set<String> expected, ZipInputStream zip) throws Exception {
        ZipEntry zipEntry;
        while ((zipEntry = zip.getNextEntry()) != null) {
            String name = zipEntry.getName();
         /*if (!zipEntry.isDirectory())
         {
            byte[] buf = new byte[1024];
            int i = zip.read(buf);
            System.out.println(new String(buf, 0, i));
         }*/
            zip.closeEntry();
            assertTrue("Not found " + name + " entry in zip. ", expected.remove(name));
        }
        zip.close();
        assertTrue(expected.isEmpty());
    }
}
