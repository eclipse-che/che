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

import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.vfs.server.observation.CreateEvent;
import org.eclipse.che.api.vfs.server.observation.DeleteEvent;
import org.eclipse.che.api.vfs.server.observation.MoveEvent;
import org.eclipse.che.api.vfs.server.observation.RenameEvent;
import org.eclipse.che.api.vfs.server.observation.UpdateACLEvent;
import org.eclipse.che.api.vfs.server.observation.UpdateContentEvent;
import org.eclipse.che.api.vfs.server.observation.UpdatePropertiesEvent;
import org.eclipse.che.api.vfs.server.observation.VirtualFileEvent;
import org.everrest.core.impl.ContainerResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 */
public class EventsTest extends LocalFileSystemTest {
    final String fileName   = "EventsTest_File";
    final String folderName = "EventsTest_Folder";

    private String folderId;
    private String folderPath;

    private String fileId;
    private String filePath;

    private String destinationFolderId;
    private String destinationFolderPath;

    private List<VirtualFileEvent> events;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        folderPath = createDirectory(testRootPath, folderName);
        filePath = createFile(testRootPath, fileName, DEFAULT_CONTENT_BYTES);
        Map<String, String[]> fileProperties = new HashMap<>(1);
        fileProperties.put("vfs:mimeType", new String[]{MediaType.TEXT_PLAIN});
        writeProperties(filePath, fileProperties);
        destinationFolderPath = createDirectory(testRootPath, "EventsTest_DestinationFolder");
        folderId = pathToId(folderPath);
        fileId = pathToId(filePath);
        destinationFolderId = pathToId(destinationFolderPath);
        events = new ArrayList<>();
        mountPoint.getEventService().subscribe(new EventSubscriber<CreateEvent>() {
            @Override
            public void onEvent(CreateEvent event) {
                events.add(event);
            }
        });
        mountPoint.getEventService().subscribe(new EventSubscriber<MoveEvent>() {
            @Override
            public void onEvent(MoveEvent event) {
                events.add(event);
            }
        });
        mountPoint.getEventService().subscribe(new EventSubscriber<RenameEvent>() {
            @Override
            public void onEvent(RenameEvent event) {
                events.add(event);
            }
        });
        mountPoint.getEventService().subscribe(new EventSubscriber<DeleteEvent>() {
            @Override
            public void onEvent(DeleteEvent event) {
                events.add(event);
            }
        });
        mountPoint.getEventService().subscribe(new EventSubscriber<UpdateContentEvent>() {
            @Override
            public void onEvent(UpdateContentEvent event) {
                events.add(event);
            }
        });
        mountPoint.getEventService().subscribe(new EventSubscriber<UpdatePropertiesEvent>() {
            @Override
            public void onEvent(UpdatePropertiesEvent event) {
                events.add(event);
            }
        });
        mountPoint.getEventService().subscribe(new EventSubscriber<UpdateACLEvent>() {
            @Override
            public void onEvent(UpdateACLEvent event) {
                events.add(event);
            }
        });
    }

    public void testCreateFile() throws Exception {
        String name = "testCreateFile";
        String content = "test create file";
        String requestPath = SERVICE_URI + "file/" + folderId + '?' + "name=" + name;
        Map<String, List<String>> headers = new HashMap<>(1);
        headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList("text/plain;charset=utf8"));
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, headers, content.getBytes(), null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());

        String expectedPath = folderPath + '/' + name;
        assertEquals(content, new String(readFile(expectedPath)));
        assertEquals(1, events.size());
        VirtualFileEvent event = events.get(0);
        assertEquals(VirtualFileEvent.ChangeType.CREATED, event.getType());
        assertFalse(event.isFolder());
        assertEquals(expectedPath, event.getPath());
    }

    public void testCreateFolder() throws Exception {
        String name = "testCreateFolder";
        String requestPath = SERVICE_URI + "folder/" + folderId + '?' + "name=" + name;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());

        String expectedPath = folderPath + '/' + name;
        assertTrue(exists(expectedPath));
        assertEquals(1, events.size());
        VirtualFileEvent event = events.get(0);
        assertEquals(VirtualFileEvent.ChangeType.CREATED, event.getType());
        assertTrue(event.isFolder());
        assertEquals(expectedPath, event.getPath());
    }

    public void testCopy() throws Exception {
        String requestPath = SERVICE_URI + "copy/" + fileId + '?' + "parentId=" + destinationFolderId;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, null);

        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());

        String expectedPath = destinationFolderPath + '/' + fileName;
        assertTrue(exists(expectedPath));
        assertEquals(1, events.size());
        VirtualFileEvent event = events.get(0);
        assertEquals(VirtualFileEvent.ChangeType.CREATED, event.getType());
        assertFalse(event.isFolder());
        assertEquals(expectedPath, event.getPath());
    }

    public void testMove() throws Exception {
        String requestPath = SERVICE_URI + "move/" + fileId + '?' + "parentId=" + destinationFolderId;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, null);

        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());

        String expectedPath = destinationFolderPath + '/' + fileName;
        assertTrue(exists(expectedPath));
        assertFalse(exists(filePath));
        assertEquals(1, events.size());
        VirtualFileEvent event = events.get(0);
        assertEquals(VirtualFileEvent.ChangeType.MOVED, event.getType());
        assertFalse(event.isFolder());
        assertEquals(expectedPath, event.getPath());
        assertEquals(filePath, ((MoveEvent)event).getOldPath());
    }

    public void testUpdateContent() throws Exception {
        String contentType = MediaType.APPLICATION_XML;
        String requestPath = SERVICE_URI + "content/" + fileId;
        Map<String, List<String>> headers = new HashMap<>(1);
        headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(contentType));
        String content = "<?xml version='1.0'><root/>";
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, headers, content.getBytes(), null);
        assertEquals(204, response.getStatus());

        assertEquals(1, events.size());
        VirtualFileEvent event = events.get(0);
        assertEquals(VirtualFileEvent.ChangeType.CONTENT_UPDATED, event.getType());
        assertFalse(event.isFolder());
        assertEquals(filePath, event.getPath());
    }

    public void testUpdateProperties() throws Exception {
        String requestPath = SERVICE_URI + "item/" + fileId;
        Map<String, List<String>> headers = new HashMap<>(1);
        headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));
        String properties = "[{\"name\":\"MyProperty\", \"value\":[\"MyValue\"]}]";
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, headers, properties.getBytes(), null);
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());

        assertEquals(1, events.size());
        VirtualFileEvent event = events.get(0);
        assertEquals(VirtualFileEvent.ChangeType.PROPERTIES_UPDATED, event.getType());
        assertFalse(event.isFolder());
        assertEquals(filePath, event.getPath());
    }

    public void testUpdateAcl() throws Exception {
        String requestPath = SERVICE_URI + "acl/" + fileId;
        Map<String, List<String>> headers = new HashMap<>(1);
        headers.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));
        String acl = "[{\"principal\":{\"name\":\"admin\",\"type\":\"USER\"},\"permissions\":[\"all\"]}," +
                     "{\"principal\":{\"name\":\"john\",\"type\":\"USER\"},\"permissions\":[\"read\", \"write\"]}]";
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, headers, acl.getBytes(), null);
        assertEquals("Error: " + response.getEntity(), 204, response.getStatus());

        assertEquals(1, events.size());
        VirtualFileEvent event = events.get(0);
        assertEquals(VirtualFileEvent.ChangeType.ACL_UPDATED, event.getType());
        assertFalse(event.isFolder());
        assertEquals(filePath, event.getPath());
    }

    public void testDelete() throws Exception {
        String requestPath = SERVICE_URI + "delete/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, null);
        assertEquals("Error: " + response.getEntity(), 204, response.getStatus());

        assertFalse(exists(filePath));
        assertEquals(1, events.size());
        VirtualFileEvent event = events.get(0);
        assertEquals(VirtualFileEvent.ChangeType.DELETED, event.getType());
        assertFalse(event.isFolder());
        assertEquals(filePath, event.getPath());
    }

    public void testRename() throws Exception {
        String requestPath = SERVICE_URI + "rename/" + fileId + '?' + "newname=" + "_FILE_NEW_NAME_";
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, null);

        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());

        String expectedPath = testRootPath + '/' + "_FILE_NEW_NAME_";
        assertTrue(exists(expectedPath));
        assertFalse(exists(filePath));
        assertEquals(1, events.size());
        VirtualFileEvent event = events.get(0);
        assertEquals(VirtualFileEvent.ChangeType.RENAMED, event.getType());
        assertFalse(event.isFolder());
        assertEquals(expectedPath, event.getPath());
        assertEquals(filePath, ((RenameEvent)event).getOldPath());
    }
}
