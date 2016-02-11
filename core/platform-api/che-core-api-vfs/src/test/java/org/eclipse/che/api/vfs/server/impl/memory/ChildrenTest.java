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
import org.eclipse.che.api.vfs.shared.dto.ItemList;
import org.eclipse.che.api.vfs.shared.dto.Property;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.HttpMethod;

/** @author andrew00x */
public class ChildrenTest extends MemoryFileSystemTest {
    private String folderId;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        VirtualFile parentFolder = mountPoint.getRoot().createFolder(name);

        VirtualFile folder = parentFolder.createFolder("ChildrenTest_FOLDER");

        VirtualFile file = folder.createFile("ChildrenTest_FILE01", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        file.updateProperties(Arrays.asList(createProperty("PropertyA", "A"), createProperty("PropertyB", "B")), null);

        VirtualFile folder1 = folder.createFolder("ChildrenTest_FOLDER01");
        folder1.updateProperties(Arrays.asList(createProperty("PropertyA", "A"), createProperty("PropertyB", "B")), null);

        VirtualFile folder2 = folder.createFolder("ChildrenTest_FOLDER02");
        folder2.updateProperties(Arrays.asList(createProperty("PropertyA", "A"), createProperty("PropertyB", "B")), null);

        folderId = folder.getId();
    }

    public void testGetChildren() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "children/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        assertEquals(200, response.getStatus());
        //log.info(new String(writer.getBody()));
        @SuppressWarnings("unchecked")
        ItemList children = (ItemList)response.getEntity();
        List<String> list = new ArrayList<>(3);
        for (Item i : children.getItems()) {
            validateLinks(i);
            list.add(i.getName());
        }
        assertEquals(3, list.size());
        assertTrue(list.contains("ChildrenTest_FOLDER01"));
        assertTrue(list.contains("ChildrenTest_FOLDER02"));
        assertTrue(list.contains("ChildrenTest_FILE01"));
    }

    @SuppressWarnings("unchecked")
    public void testGetChildrenPagingSkipCount() throws Exception {
        // Get all children.
        String path = SERVICE_URI + "children/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        ItemList children = (ItemList)response.getEntity();
        List<Object> all = new ArrayList<>(3);
        for (Item i : children.getItems()) {
            all.add(i.getName());
        }

        Iterator<Object> iteratorAll = all.iterator();
        iteratorAll.next();
        iteratorAll.remove();

        // Skip first item in result.
        path = SERVICE_URI + "children/" + folderId + "?" + "skipCount=" + 1;
        checkPage(path, HttpMethod.GET, Item.class.getMethod("getName"), all);
    }

    @SuppressWarnings("unchecked")
    public void testGetChildrenPagingMaxItems() throws Exception {
        // Get all children.
        String path = SERVICE_URI + "children/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        ItemList children = (ItemList)response.getEntity();
        List<Object> all = new ArrayList<>(3);
        for (Item i : children.getItems()) {
            all.add(i.getName());
        }

        // Exclude last item from result.
        path = SERVICE_URI + "children/" + folderId + "?" + "maxItems=" + 2;
        all.remove(2);
        checkPage(path, HttpMethod.GET, Item.class.getMethod("getName"), all);
    }

    @SuppressWarnings("unchecked")
    public void testGetChildrenNoPropertyFilter() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        // Get children without filter.
        String path = SERVICE_URI + "children/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        //log.info(new String(writer.getBody()));
        assertEquals(200, response.getStatus());
        ItemList children = (ItemList)response.getEntity();
        assertEquals(3, children.getItems().size());
        for (Item i : children.getItems()) {
            // No properties without filter. 'none' filter is used if nothing set by client.
            assertFalse(hasProperty(i, "PropertyA"));
            assertFalse(hasProperty(i, "PropertyB"));
        }
    }

    @SuppressWarnings("unchecked")
    public void testGetChildrenPropertyFilter() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        // Get children and apply filter for properties.
        String propertyFilter = "PropertyA";
        String path = SERVICE_URI + "children/" + folderId + "?" + "propertyFilter=" + propertyFilter;
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        //log.info(new String(writer.getBody()));
        assertEquals(200, response.getStatus());
        ItemList children = (ItemList)response.getEntity();
        assertEquals(3, children.getItems().size());
        for (Item i : children.getItems()) {
            assertTrue(hasProperty(i, "PropertyA"));
            assertFalse(hasProperty(i, "PropertyB")); // must be excluded
        }
    }

    @SuppressWarnings("unchecked")
    public void testGetChildrenTypeFilter() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        // Get children and apply filter for properties.
        String path = SERVICE_URI + "children/" + folderId + "?" + "itemType=folder";
        ContainerResponse response = launcher.service(HttpMethod.GET, path, BASE_URI, null, null, writer, null);
        //log.info(new String(writer.getBody()));
        assertEquals(200, response.getStatus());
        ItemList children = (ItemList)response.getEntity();
        assertEquals(2, children.getItems().size());
        for (Item i : children.getItems()) {
            assertTrue(i.getItemType() == ItemType.FOLDER);
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean hasProperty(Item i, String propertyName) {
        List<Property> properties = i.getProperties();
        if (properties.size() == 0) {
            return false;
        }
        for (Property p : properties) {
            if (p.getName().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }
}
