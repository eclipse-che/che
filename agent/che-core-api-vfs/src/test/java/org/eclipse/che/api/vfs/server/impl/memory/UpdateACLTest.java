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
import org.eclipse.che.api.vfs.shared.dto.AccessControlEntry;
import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.BasicPermissions;

import com.google.common.collect.Sets;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/** @author andrew00x */
public class UpdateACLTest extends MemoryFileSystemTest {
    private String objectId;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        VirtualFile updateAclTestFolder = mountPoint.getRoot().createFolder(name);

        VirtualFile file = updateAclTestFolder.createFile("UpdateACLTest_FILE",
                                                          new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        objectId = file.getId();
    }

    public void testUpdateAcl() throws Exception {
        String path = SERVICE_URI + "acl/" + objectId;
        String body = "[{\"principal\":{\"name\":\"admin\",\"type\":\"USER\"},\"permissions\":[\"all\"]}," + //
                      "{\"principal\":{\"name\":\"john\",\"type\":\"USER\"},\"permissions\":[\"read\"]}]";
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, h, body.getBytes(), null);
        assertEquals(204, response.getStatus());
        List<AccessControlEntry> acl = mountPoint.getVirtualFileById(objectId).getACL();
        Map<String, List<String>> m = toMap(acl);
        assertEquals(m.get("admin"), Arrays.asList("all"));
        assertEquals(m.get("john"), Arrays.asList("read"));
    }

    public void testUpdateAclOverride() throws Exception {
        Principal anyPrincipal = createPrincipal("any", Principal.Type.USER);
        Map<Principal, Set<String>> permissions = new HashMap<>(1);
        permissions.put(anyPrincipal, Sets.newHashSet(BasicPermissions.ALL.value()));
        mountPoint.getVirtualFileById(objectId).updateACL(createAcl(permissions), false, null);

        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "acl/" + objectId + '?' + "override=" + true;
        String body = "[{\"principal\":{\"name\":\"admin\",\"type\":\"USER\"},\"permissions\":[\"all\"]}," + //
                      "{\"principal\":{\"name\":\"john\",\"type\":\"USER\"},\"permissions\":[\"read\"]}]";
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, h, body.getBytes(), writer, null);
        assertEquals(204, response.getStatus());

        List<AccessControlEntry> acl = mountPoint.getVirtualFileById(objectId).getACL();
        Map<String, List<String>> m = toMap(acl);
        assertEquals(m.get("admin"), Arrays.asList("all"));
        assertEquals(m.get("john"), Arrays.asList("read"));
        assertNull("Anonymous permissions must be removed.", m.get("anonymous"));
    }

    public void testUpdateAclMerge() throws Exception {
        Principal anyPrincipal = createPrincipal("any", Principal.Type.USER);
        Map<Principal, Set<String>> permissions = new HashMap<>(1);
        permissions.put(anyPrincipal, Sets.newHashSet(BasicPermissions.ALL.value()));
        mountPoint.getVirtualFileById(objectId).updateACL(createAcl(permissions), false, null);

        String path = SERVICE_URI + "acl/" + objectId;
        String body = "[{\"principal\":{\"name\":\"admin\",\"type\":\"USER\"},\"permissions\":[\"all\"]}," + //
                      "{\"principal\":{\"name\":\"john\",\"type\":\"USER\"},\"permissions\":[\"read\"]}]";
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, h, body.getBytes(), null);
        assertEquals(204, response.getStatus());

        List<AccessControlEntry> acl = mountPoint.getVirtualFileById(objectId).getACL();
        Map<String, List<String>> m = toMap(acl);
        assertEquals(m.get("admin"), Arrays.asList("all"));
        assertEquals(m.get("john"), Arrays.asList("read"));
        assertEquals(m.get("any"), Arrays.asList("all"));
    }

    public void testUpdateAclLocked() throws Exception {
        String lockToken = mountPoint.getVirtualFileById(objectId).lock(0);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "acl/" + objectId + '?' + "lockToken=" + lockToken;
        String body = "[{\"principal\":{\"name\":\"admin\",\"type\":\"USER\"},\"permissions\":[\"all\"]}," + //
                      "{\"principal\":{\"name\":\"john\",\"type\":\"USER\"},\"permissions\":[\"read\"]}]";
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, h, body.getBytes(), writer, null);
        assertEquals(204, response.getStatus());

        List<AccessControlEntry> acl = mountPoint.getVirtualFileById(objectId).getACL();
        Map<String, List<String>> m = toMap(acl);
        assertEquals(m.get("admin"), Arrays.asList("all"));
        assertEquals(m.get("john"), Arrays.asList("read"));
    }

    public void testUpdateAclLockedNoLockToken() throws Exception {
        mountPoint.getVirtualFileById(objectId).lock(0);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "acl/" + objectId;
        String body = "[{\"principal\":{\"name\":\"admin\",\"type\":\"USER\"},\"permissions\":[\"all\"]}," + //
                      "{\"principal\":{\"name\":\"john\",\"type\":\"USER\"},\"permissions\":[\"read\"]}]";
        Map<String, List<String>> h = new HashMap<>(1);
        h.put(HttpHeaders.CONTENT_TYPE, Arrays.asList(MediaType.APPLICATION_JSON));
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, h, body.getBytes(), writer, null);
        assertEquals(403, response.getStatus());
        log.info(new String(writer.getBody()));
    }

    private Map<String, List<String>> toMap(List<AccessControlEntry> acl) {
        Map<String, List<String>> m = new HashMap<>();
        for (AccessControlEntry e : acl) {
            m.put(e.getPrincipal().getName(), e.getPermissions());
        }
        return m;
    }
}
