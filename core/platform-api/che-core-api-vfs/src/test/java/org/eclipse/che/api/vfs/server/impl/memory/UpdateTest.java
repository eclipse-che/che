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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/** @author andrew00x */
public class UpdateTest extends MemoryFileSystemTest {
    private String fileId;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        VirtualFile updateTestFolder = mountPoint.getRoot().createFolder(name);
        VirtualFile file =
                updateTestFolder.createFile("UpdateTest_FILE", new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        fileId = file.getId();
    }

    public void testUpdatePropertiesFile() throws Exception {
        String properties = "[{\"name\":\"MyProperty\", \"value\":[\"MyValue\"]}]";
        doUpdate(fileId, properties);
        VirtualFile file = mountPoint.getVirtualFileById(fileId);
        assertEquals("MyValue", file.getPropertyValue("MyProperty"));
    }

    public void testUpdatePropertiesLockedFile() throws Exception {
        VirtualFile file = mountPoint.getVirtualFileById(fileId);
        String lockToken = file.lock(0);
        String properties = "[{\"name\":\"MyProperty\", \"value\":[\"MyValue\"]}]";
        String path = SERVICE_URI + "item/" + fileId + "?lockToken=" + lockToken;
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, h, properties.getBytes(), null);
        assertEquals(200, response.getStatus());
        file = mountPoint.getVirtualFileById(fileId);
        assertEquals("MyValue", file.getPropertyValue("MyProperty"));
    }

    public void testUpdatePropertiesLockedFileNoLockToken() throws Exception {
        VirtualFile file = mountPoint.getVirtualFileById(fileId);
        file.lock(0);
        String properties = "[{\"name\":\"MyProperty\", \"value\":[\"MyValue\"]}]";
        String path = SERVICE_URI + "item/" + fileId;
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, h, properties.getBytes(), null);
        assertEquals(403, response.getStatus());
        file = mountPoint.getVirtualFileById(fileId);
        assertEquals(null, file.getPropertyValue("MyProperty"));
    }

    public void doUpdate(String id, String rawData) throws Exception {
        String path = SERVICE_URI + "item/" + id;
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, h, rawData.getBytes(), null);
        assertEquals(200, response.getStatus());
    }
}
