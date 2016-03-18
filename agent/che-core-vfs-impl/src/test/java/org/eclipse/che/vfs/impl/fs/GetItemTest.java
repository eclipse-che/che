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

import org.eclipse.che.api.vfs.shared.ItemType;
import org.eclipse.che.api.vfs.shared.dto.Item;
import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.UserImpl;
import org.eclipse.che.dto.server.DtoFactory;

import com.google.common.collect.Sets;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.HttpMethod;

import static org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.BasicPermissions;

public class GetItemTest extends LocalFileSystemTest {
    private Map<String, String[]> properties;

    private String fileId;
    private String filePath;

    private String protectedFileId;
    private String protectedFilePath;

    private String protectedParentId;
    private String protectedParentPath;

    private String folderId;
    private String folderPath;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        filePath = createFile(testRootPath, "GetObjectTest_File", DEFAULT_CONTENT_BYTES);
        folderPath = createDirectory(testRootPath, "GetObjectTest_Folder");
        protectedFilePath = createFile(testRootPath, "GetObjectTest_ProtectedFile", DEFAULT_CONTENT_BYTES);
        String protectedParent = createDirectory(testRootPath, "GetObjectTest_ProtectedParent");
        protectedParentPath = createFile(protectedParent, "GetObjectTest_ProtectedChildFile", DEFAULT_CONTENT_BYTES);

        properties = new HashMap<>(2);
        properties.put("MyProperty01", new String[]{"hello world"});
        properties.put("MyProperty02", new String[]{"to be or not to be"});
        writeProperties(filePath, properties);

        Map<Principal, Set<String>> permissions = new HashMap<>(1);
        Principal principal = DtoFactory.getInstance().createDto(Principal.class).withName("andrew").withType(Principal.Type.USER);

        permissions.put(principal, Sets.newHashSet(BasicPermissions.ALL.value()));
        writePermissions(protectedFilePath, permissions);
        writePermissions(protectedParent, permissions);

        fileId = pathToId(filePath);
        protectedFileId = pathToId(protectedFilePath);
        folderId = pathToId(folderPath);
        protectedParentId = pathToId(protectedParentPath);
    }

    public void testGetFile() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "item/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        Item item = (Item)response.getEntity();
        assertEquals(ItemType.FILE, item.getItemType());
        assertEquals(fileId, item.getId());
        assertEquals(filePath, item.getPath());
        validateLinks(item);
    }

    public void testGetFileByPath() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "itembypath" + filePath;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        Item item = (Item)response.getEntity();
        assertEquals(ItemType.FILE, item.getItemType());
        assertEquals(fileId, item.getId());
        assertEquals(filePath, item.getPath());
        validateLinks(item);
    }

   /*
    * --- Versions is not supported. Parameter 'versionId' must be absent or equals to '0'. ---
    */

    public void testGetFileByPathWithVersionId() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "itembypath" + filePath + '?' + "versionId=" + 0;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        Item item = (Item)response.getEntity();
        assertEquals(ItemType.FILE, item.getItemType());
        assertEquals(fileId, item.getId());
        assertEquals(filePath, item.getPath());
        validateLinks(item);
    }

    public void testGetFileByPathWithVersionId2() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "itembypath" + filePath + '?' + "versionId=" + 1; // must fail
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals(404, response.getStatus());
    }

   /*
    * ---
    */

    @SuppressWarnings("rawtypes")
    public void testGetFilePropertyFilter() throws Exception {
        Iterator<Map.Entry<String, String[]>> iterator = properties.entrySet().iterator();
        Map.Entry<String, String[]> e1 = iterator.next();
        Map.Entry<String, String[]> e2 = iterator.next();

        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        // No filter - all properties
        String requestPath = SERVICE_URI + "item/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        Item i = (Item)response.getEntity();

        assertEquals(e1.getValue()[0], getPropertyValue(i, e1.getKey()));
        assertEquals(e2.getValue()[0], getPropertyValue(i, e2.getKey()));

        // With filter
        requestPath = SERVICE_URI + "item/" + fileId + '?' + "propertyFilter=" + e1.getKey();

        response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        i = (Item)response.getEntity();

        assertEquals(e1.getValue()[0], getPropertyValue(i, e1.getKey()));
        assertNull(getPropertyValue(i, e2.getKey()));
    }

    public void testGetFileNotFound() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "item/" + fileId + "_WRONG_ID_";
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        assertEquals(404, response.getStatus());
        log.info(new String(writer.getBody()));
    }

    public void testGetFileHavePermissions() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "item/" + protectedFileId;
        // Replace default principal by principal who has read permission.
        EnvironmentContext.getCurrent().setUser(new UserImpl("andrew", "andrew", null, Arrays.asList("workspace/developer"), false));
        // ---
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        Item item = (Item)response.getEntity();
        assertEquals(ItemType.FILE, item.getItemType());
        assertEquals(protectedFileId, item.getId());
        assertEquals(protectedFilePath, item.getPath());
        validateLinks(item);
    }

    public void testGetFolder() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "item/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        Item item = (Item)response.getEntity();
        assertEquals(ItemType.FOLDER, item.getItemType());
        assertEquals(folderId, item.getId());
        assertEquals(folderPath, item.getPath());
        validateLinks(item);
    }

    public void testGetFolderByPath() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "itembypath" + folderPath;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        Item item = (Item)response.getEntity();
        assertEquals(ItemType.FOLDER, item.getItemType());
        assertEquals(folderId, item.getId());
        assertEquals(folderPath, item.getPath());
        validateLinks(item);
    }

    public void testGetFolderByPathWithVersionId() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        // Parameter 'versionId' is not acceptable for folders, must be absent.
        String requestPath = SERVICE_URI + "itembypath" + folderPath + '?' + "versionId=" + 1;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals(403, response.getStatus());
    }
}
