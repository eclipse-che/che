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

import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.observation.CreateEvent;
import org.eclipse.che.api.vfs.server.observation.DeleteEvent;
import org.eclipse.che.api.vfs.server.observation.MoveEvent;
import org.eclipse.che.api.vfs.server.observation.RenameEvent;
import org.eclipse.che.api.vfs.server.observation.UpdateACLEvent;
import org.eclipse.che.api.vfs.server.observation.UpdateContentEvent;
import org.eclipse.che.api.vfs.server.observation.UpdatePropertiesEvent;
import org.eclipse.che.api.vfs.server.observation.VirtualFileEvent;
import org.everrest.core.impl.ContainerResponse;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/** @author andrew00x */
public class EventsTest extends MemoryFileSystemTest {
    private VirtualFile testEventsFolder;
    private String      testFolderId;
    private String      testFolderPath;
    private String      testFileId;
    private String      testFilePath;

    private String                 destinationFolderID;
    private String                 destinationFolderPath;
    private List<VirtualFileEvent> events;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        testEventsFolder = mountPoint.getRoot().createFolder(name);
        VirtualFile destinationFolder = mountPoint.getRoot().createFolder("EventsTest_DESTINATION_FOLDER");
        testFolderId = testEventsFolder.getId();
        testFolderPath = testEventsFolder.getPath();
        destinationFolderID = destinationFolder.getId();
        destinationFolderPath = destinationFolder.getPath();
        VirtualFile testFile = testEventsFolder.createFile("file", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        testFileId = testFile.getId();
        testFilePath = testFile.getPath();
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
        String path = SERVICE_URI + "file/" + testFolderId + '?' + "name=" + name;
        Map<String, List<String>> headers = new HashMap<>();
        List<String> contentType = new ArrayList<>();
        contentType.add("text/plain;charset=utf8");
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, headers, content.getBytes(), null);
        assertEquals(200, response.getStatus());
        assertEquals(1, events.size());
        assertEquals(VirtualFileEvent.ChangeType.CREATED, events.get(0).getType());
        assertFalse(events.get(0).isFolder());
        assertEquals(MY_WORKSPACE_ID, events.get(0).getWorkspaceId());
        assertEquals(testFolderPath + '/' + name, events.get(0).getPath());
    }

    public void testCreateFolder() throws Exception {
        String name = "testCreateFolder";
        String path = SERVICE_URI + "folder/" + testFolderId + '?' + "name=" + name;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        assertEquals(1, events.size());
        assertEquals(VirtualFileEvent.ChangeType.CREATED, events.get(0).getType());
        assertTrue(events.get(0).isFolder());
        assertEquals(MY_WORKSPACE_ID, events.get(0).getWorkspaceId());
        assertEquals(testFolderPath + '/' + name, events.get(0).getPath());
    }

    public void testCopy() throws Exception {
        String path = SERVICE_URI + "copy/" + testFileId + '?' + "parentId=" + destinationFolderID;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        assertEquals(1, events.size());
        assertEquals(VirtualFileEvent.ChangeType.CREATED, events.get(0).getType());
        assertFalse(events.get(0).isFolder());
        assertEquals(MY_WORKSPACE_ID, events.get(0).getWorkspaceId());
        assertEquals(destinationFolderPath + "/file", events.get(0).getPath());
    }

    public void testMove() throws Exception {
        String path = SERVICE_URI + "move/" + testFileId + '?' + "parentId=" + destinationFolderID;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        assertEquals(1, events.size());
        assertEquals(VirtualFileEvent.ChangeType.MOVED, events.get(0).getType());
        assertFalse(events.get(0).isFolder());
        assertEquals(destinationFolderPath + "/file", events.get(0).getPath());
        assertEquals(MY_WORKSPACE_ID, events.get(0).getWorkspaceId());
        assertEquals(testFilePath, ((MoveEvent)events.get(0)).getOldPath());
    }

    public void testUpdateContent() throws Exception {
        String path = SERVICE_URI + "content/" + testFileId;
        Map<String, List<String>> headers = new HashMap<>();
        List<String> contentType = new ArrayList<>();
        contentType.add(MediaType.APPLICATION_XML);
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);
        String content = "<?xml version='1.0'><root/>";
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, headers, content.getBytes(), null);
        assertEquals(204, response.getStatus());
        assertEquals(1, events.size());
        assertEquals(VirtualFileEvent.ChangeType.CONTENT_UPDATED, events.get(0).getType());
        assertFalse(events.get(0).isFolder());
        assertEquals(MY_WORKSPACE_ID, events.get(0).getWorkspaceId());
        assertEquals(testFilePath, events.get(0).getPath());
    }

    public void testUpdateProperties() throws Exception {
        String path = SERVICE_URI + "item/" + testFileId;
        Map<String, List<String>> headers = new HashMap<>();
        List<String> contentType = new ArrayList<>();
        contentType.add(MediaType.APPLICATION_JSON);
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);
        String properties = "[{\"name\":\"MyProperty\", \"value\":[\"MyValue\"]}]";
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, headers, properties.getBytes(), null);
        assertEquals(200, response.getStatus());
        assertEquals(1, events.size());
        assertEquals(VirtualFileEvent.ChangeType.PROPERTIES_UPDATED, events.get(0).getType());
        assertFalse(events.get(0).isFolder());
        assertEquals(MY_WORKSPACE_ID, events.get(0).getWorkspaceId());
        assertEquals(testFilePath, events.get(0).getPath());
    }

    public void testDelete() throws Exception {
        String path = SERVICE_URI + "delete/" + testFileId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(204, response.getStatus());
        assertEquals(1, events.size());
        assertEquals(VirtualFileEvent.ChangeType.DELETED, events.get(0).getType());
        assertFalse(events.get(0).isFolder());
        assertEquals(MY_WORKSPACE_ID, events.get(0).getWorkspaceId());
        assertEquals(testFilePath, events.get(0).getPath());
    }

    public void testRename() throws Exception {
        String path = SERVICE_URI + "rename/" + testFileId + '?' + "newname=" + "_FILE_NEW_NAME_";
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(200, response.getStatus());
        assertEquals(1, events.size());
        assertEquals(VirtualFileEvent.ChangeType.RENAMED, events.get(0).getType());
        assertFalse(events.get(0).isFolder());
        assertEquals(testFolderPath + '/' + "_FILE_NEW_NAME_", events.get(0).getPath());
        assertEquals(MY_WORKSPACE_ID, events.get(0).getWorkspaceId());
        assertEquals(testFilePath, ((RenameEvent)events.get(0)).getOldPath());
    }
}
