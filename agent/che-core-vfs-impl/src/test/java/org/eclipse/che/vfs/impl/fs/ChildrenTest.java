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
import org.eclipse.che.api.vfs.shared.dto.ItemList;
import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.UserImpl;
import org.eclipse.che.dto.server.DtoFactory;

import com.google.common.collect.Sets;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.HttpMethod;

import static org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.BasicPermissions;

public class ChildrenTest extends LocalFileSystemTest {
    private Map<String, String[]> properties;
    private String                fileId;
    private String                folderPath;
    private String                folderId;
    private String                protectedFolderId;
    private Set<String>           childrenNames;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        folderPath = createDirectory(testRootPath, "ChildrenTest_Folder");
        String file01 = createFile(folderPath, "FILE01", DEFAULT_CONTENT_BYTES);
        String file02 = createFile(folderPath, "FILE02", DEFAULT_CONTENT_BYTES);
        String folder01 = createDirectory(folderPath, "FOLDER01");
        String folder02 = createDirectory(folderPath, "FOLDER02");

        childrenNames = new HashSet<>(4);
        childrenNames.add("FILE01");
        childrenNames.add("FILE02");
        childrenNames.add("FOLDER01");
        childrenNames.add("FOLDER02");

        properties = new HashMap<>(2);
        properties.put("MyProperty01", new String[]{"hello world"});
        properties.put("MyProperty02", new String[]{"to be or not to be"});
        writeProperties(file01, properties);
        writeProperties(file02, properties);
        writeProperties(folder01, properties);
        writeProperties(folder02, properties);

        String filePath = createFile(testRootPath, "ChildrenTest_File", DEFAULT_CONTENT_BYTES);

        String protectedFolderPath = createDirectory(testRootPath, "ChildrenTest_ProtectedFolder");
        Map<Principal, Set<String>> permissions = new HashMap<>(1);
        Principal principal = DtoFactory.getInstance().createDto(Principal.class).withName("andrew").withType(Principal.Type.USER);
        permissions.put(principal, Sets.newHashSet(BasicPermissions.ALL.value()));
        writePermissions(protectedFolderPath, permissions);

        fileId = pathToId(filePath);
        folderId = pathToId(folderPath);
        protectedFolderId = pathToId(protectedFolderPath);
    }

    public void testGetChildren() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "children/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        log.info(new String(writer.getBody()));
        @SuppressWarnings("unchecked")
        ItemList children = (ItemList)response.getEntity();
        List<String> list = new ArrayList<>(4);
        for (Item i : children.getItems()) {
            validateLinks(i);
            list.add(i.getName());
        }

        assertEquals(4, list.size());
        childrenNames.removeAll(list);
        if (!childrenNames.isEmpty()) {
            fail("Expected items " + childrenNames + " missed in response. ");
        }
    }

    public void testGetChildren_File() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "children/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        assertEquals(403, response.getStatus());
        log.info(new String(writer.getBody()));
    }

    public void testGetChildrenHavePermissions() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "children/" + protectedFolderId;
        // Replace default principal by principal who has read permission.
        EnvironmentContext.getCurrent().setUser(new UserImpl("andrew", "andrew", null, Arrays.asList("workspace/developer"), false));
        // ---
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        log.info(new String(writer.getBody()));
        @SuppressWarnings("unchecked")
        ItemList children = (ItemList)response.getEntity();
        assertTrue(children.getItems().isEmpty()); // folder is empty
        assertEquals(0, children.getNumItems());
    }

    public void testGetChildrenPagingSkipCount() throws Exception {
        // Get all children.
        String requestPath = SERVICE_URI + "children/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        @SuppressWarnings("unchecked")
        ItemList children = (ItemList)response.getEntity();
        List<Object> all = new ArrayList<>(4);
        for (Item i : children.getItems()) {
            all.add(i.getName());
        }

        // Remove first name from the list.
        Iterator<Object> iteratorAll = all.iterator();
        iteratorAll.next();
        iteratorAll.remove();

        // Skip first item in result.
        requestPath = SERVICE_URI + "children/" + folderId + '?' + "skipCount=" + 1;
        checkPage(requestPath, HttpMethod.GET, Item.class.getMethod("getName"), all);
    }

    public void testGetChildrenPagingMaxItems() throws Exception {
        // Get all children.
        String requestPath = SERVICE_URI + "children/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        @SuppressWarnings("unchecked")
        ItemList children = (ItemList)response.getEntity();
        List<Object> all = new ArrayList<>(4);
        for (Item i : children.getItems()) {
            all.add(i.getName());
        }

        all.remove(3);

        // Exclude last item from result.
        requestPath = SERVICE_URI + "children/" + folderId + '?' + "maxItems=" + 3;
        checkPage(requestPath, HttpMethod.GET, Item.class.getMethod("getName"), all);
    }

    public void testGetChildrenNoPropertyFilter() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        // Get children without filter.
        String requestPath = SERVICE_URI + "children/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        @SuppressWarnings("unchecked")
        ItemList children = (ItemList)response.getEntity();
        assertEquals(4, children.getItems().size());
        for (Item i : children.getItems()) {
            // No properties without filter. 'none' filter is used if nothing set by client.
            assertNull(getPropertyValue(i, "MyProperty01"));
            assertNull(getPropertyValue(i, "MyProperty02"));
        }
    }

    public void testGetChildrenPropertyFilter() throws Exception {
        Iterator<Map.Entry<String, String[]>> iterator = properties.entrySet().iterator();
        Map.Entry<String, String[]> e1 = iterator.next();
        Map.Entry<String, String[]> e2 = iterator.next();
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        // Get children and apply filter for properties.
        String requestPath = SERVICE_URI + "children/" + folderId + '?' + "propertyFilter=" + e1.getKey();
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        @SuppressWarnings("unchecked")
        ItemList children = (ItemList)response.getEntity();
        assertEquals(4, children.getItems().size());
        for (Item i : children.getItems()) {
            assertNull(getPropertyValue(i, e2.getKey()));
            assertEquals(e1.getValue()[0], getPropertyValue(i, e1.getKey()));
        }
    }

    public void testGetChildrenTypeFilter() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "children/" + folderId + '?' + "itemType=" + "folder";
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        @SuppressWarnings("unchecked")
        ItemList children = (ItemList)response.getEntity();
        assertEquals(2, children.getItems().size());
        for (Item i : children.getItems()) {
            assertTrue(i.getItemType() == ItemType.FOLDER);
        }
    }
}
