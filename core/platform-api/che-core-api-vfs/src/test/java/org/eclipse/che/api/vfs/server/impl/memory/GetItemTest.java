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
import org.eclipse.che.api.vfs.shared.ItemType;
import org.eclipse.che.api.vfs.shared.dto.Item;
import org.eclipse.che.api.vfs.shared.dto.Property;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;

/** @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a> */
public class GetItemTest extends MemoryFileSystemTest {
    private String folderId;
    private String folderPath;
    private String fileId;
    private String filePath;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        VirtualFile parentFolder = mountPoint.getRoot().createFolder(name);
        VirtualFile folder = parentFolder.createFolder("GetObjectTest_PARENT_FOLDER");
        folderId = folder.getId();
        folderPath = folder.getPath();

        VirtualFile file =
                parentFolder.createFile("GetObjectTest_FILE.txt", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        file.updateProperties(Arrays.asList(
                createProperty("MyProperty01", "hello world"),
                createProperty("MyProperty02", "to be or not to be"),
                createProperty("MyProperty03", "123"),
                createProperty("MyProperty04", "true"),
                createProperty("MyProperty05", "123.456")), null);
        fileId = file.getId();
        filePath = file.getPath();
    }

    public void testGetFile() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "item/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        assertEquals(200, response.getStatus());
        //log.info(new String(writer.getBody()));
        Item item = (Item)response.getEntity();
        assertEquals(ItemType.FILE, item.getItemType());
        assertEquals(fileId, item.getId());
        assertEquals(filePath, item.getPath());
        validateLinks(item);
    }

    public void testGetFileByPath() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "itembypath" + filePath;
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals(200, response.getStatus());
        Item item = (Item)response.getEntity();
        assertEquals(ItemType.FILE, item.getItemType());
        assertEquals(fileId, item.getId());
        assertEquals(filePath, item.getPath());
        validateLinks(item);
    }

    @SuppressWarnings("rawtypes")
    public void testGetFilePropertyFilter() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        // No filter - all properties
        String path = SERVICE_URI + "item/" + fileId;

        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        //log.info(new String(writer.getBody()));
        assertEquals(200, response.getStatus());
        List<Property> properties = ((Item)response.getEntity()).getProperties();
        Map<String, List> m = new HashMap<>(properties.size());
        for (Property p : properties) {
            m.put(p.getName(), p.getValue());
        }
        assertTrue(m.size() >= 5);
        assertTrue(m.containsKey("MyProperty01"));
        assertTrue(m.containsKey("MyProperty02"));
        assertTrue(m.containsKey("MyProperty03"));
        assertTrue(m.containsKey("MyProperty04"));
        assertTrue(m.containsKey("MyProperty05"));

        // With filter
        path = SERVICE_URI + "item/" + fileId + '?' + "propertyFilter=" + "MyProperty02";

        response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        m.clear();
        properties = ((Item)response.getEntity()).getProperties();
        for (Property p : properties) {
            m.put(p.getName(), p.getValue());
        }
        assertEquals(1, m.size());
        assertEquals("to be or not to be", m.get("MyProperty02").get(0));
    }

    public void testGetFileNotFound() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "item/" + fileId + "_WRONG_ID_";
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        assertEquals(404, response.getStatus());
        log.info(new String(writer.getBody()));
    }

    public void testGetFolder() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "item/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        //log.info(new String(writer.getBody()));
        assertEquals(200, response.getStatus());
        Item item = (Item)response.getEntity();
        assertEquals(ItemType.FOLDER, item.getItemType());
        assertEquals(folderId, item.getId());
        assertEquals(folderPath, item.getPath());
        validateLinks(item);
    }

    public void testGetFolderByPath() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "itembypath" + folderPath;
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        //log.info(new String(writer.getBody()));
        assertEquals(200, response.getStatus());
        Item item = (Item)response.getEntity();
        assertEquals(ItemType.FOLDER, item.getItemType());
        assertEquals(folderId, item.getId());
        assertEquals(folderPath, item.getPath());
        validateLinks(item);
    }

    public void testGetFolderByPathWithVersionID() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "itembypath" + folderPath + '?' + "versionId=" + "0";
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals(403, response.getStatus());
    }
}
