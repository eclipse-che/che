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
import org.eclipse.che.api.vfs.server.VirtualFile;
import org.eclipse.che.api.vfs.server.observation.CreateEvent;
import org.eclipse.che.api.vfs.server.observation.VirtualFileEvent;
import org.everrest.core.impl.ContainerResponse;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.HttpMethod;

/**
 * @author andrew00x
 */
public class ImportTest extends LocalFileSystemTest {
    private String importTestRootId;
    private byte[] zipFolder;

    private List<VirtualFileEvent> events;

    private EventSubscriber<CreateEvent> eventSubscriber = new EventSubscriber<CreateEvent>() {
        @Override
        public void onEvent(CreateEvent event) {
            events.add(event);
        }
    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        VirtualFile importTestRoot = mountPoint.getRoot().createFolder(name);
        importTestRootId = importTestRoot.getId();

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(bout);
        zipOut.putNextEntry(new ZipEntry("folder1/"));
        zipOut.putNextEntry(new ZipEntry("folder2/"));
        zipOut.putNextEntry(new ZipEntry("folder3/"));
        zipOut.putNextEntry(new ZipEntry("folder1/file1.txt"));
        zipOut.write(DEFAULT_CONTENT_BYTES);
        zipOut.putNextEntry(new ZipEntry("folder2/file2.txt"));
        zipOut.write(DEFAULT_CONTENT_BYTES);
        zipOut.putNextEntry(new ZipEntry("folder3/file3.txt"));
        zipOut.write(DEFAULT_CONTENT_BYTES);
        zipOut.close();
        zipFolder = bout.toByteArray();

        events = new ArrayList<>();

        mountPoint.getEventService().subscribe(eventSubscriber);
    }

    @Override
    public void tearDown() throws Exception {
        events.clear();
        mountPoint.getEventService().unsubscribe(eventSubscriber);
        super.tearDown();
    }

    public void testImportFolder() throws Exception {
        String path = SERVICE_URI + "import/" + importTestRootId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, zipFolder, null);
        assertEquals(204, response.getStatus());
        VirtualFile parent = mountPoint.getVirtualFileById(importTestRootId);
        VirtualFile folder1 = parent.getChild("folder1");
        assertNotNull(folder1);
        VirtualFile folder2 = parent.getChild("folder2");
        assertNotNull(folder2);
        VirtualFile folder3 = parent.getChild("folder3");
        assertNotNull(folder3);
        VirtualFile file1 = folder1.getChild("file1.txt");
        assertNotNull(file1);
        assertTrue(Arrays.equals(DEFAULT_CONTENT_BYTES, readFile(file1.getPath())));
        VirtualFile file2 = folder2.getChild("file2.txt");
        assertNotNull(file2);
        assertTrue(Arrays.equals(DEFAULT_CONTENT_BYTES, readFile(file2.getPath())));
        VirtualFile file3 = folder3.getChild("file3.txt");
        assertNotNull(file3);
        assertTrue(Arrays.equals(DEFAULT_CONTENT_BYTES, readFile(file3.getPath())));

        assertEquals(6, events.size());

        List<VirtualFileEvent> _events = new ArrayList<>(events);

        for (Iterator<VirtualFileEvent> iterator = _events.iterator(); iterator.hasNext(); ) {
            VirtualFileEvent event = iterator.next();
            if (event.getPath().equals(folder1.getPath())
                || event.getPath().equals(folder2.getPath())
                || event.getPath().equals(folder3.getPath())
                || event.getPath().equals(file1.getPath())
                || event.getPath().equals(file2.getPath())
                || event.getPath().equals(file3.getPath())) {
                iterator.remove();
            } else {
                fail("Unexpected event " + event.getType() + " : " + event.getPath());
            }
        }

        assertEquals(0, _events.size());
    }
}
