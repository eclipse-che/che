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

import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.ACLCapability;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.BasicPermissions;
import org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.QueryCapability;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.HttpMethod;

public class GetAvailableFileSystemsTest extends LocalFileSystemTest {
    @SuppressWarnings("unchecked")
    public void testAvailableFS() throws Exception {
        String requestPath = BASE_URI + "/vfs/my-ws";
        ByteArrayContainerResponseWriter wr = new ByteArrayContainerResponseWriter();
        ContainerResponse response = launcher.service(HttpMethod.GET, requestPath, BASE_URI, null, null, wr, null);
        //log.info(new String(wr.getBody()));
        assertEquals("Error: " + response.getEntity(), 200, response.getStatus());
        Collection<VirtualFileSystemInfo> entity = (Collection<VirtualFileSystemInfo>)response.getEntity();
        assertNotNull(entity);
        //assertEquals(1, entity.size());
        VirtualFileSystemInfo vfsInfo = null;
        for (VirtualFileSystemInfo e : entity) {
            if (e.getId().equals(MY_WORKSPACE_ID)) {
                if (vfsInfo != null) {
                    fail("More then one VFS with the same ID found. ");
                }
                vfsInfo = e;
            }
        }
        assertNotNull(vfsInfo);
        assertEquals(false, vfsInfo.isVersioningSupported());
        assertEquals(true, vfsInfo.isLockSupported());
        assertEquals(ACLCapability.MANAGE, vfsInfo.getAclCapability());
        assertEquals(QueryCapability.NONE, vfsInfo.getQueryCapability()); // TODO : update when implement search
        assertEquals(VirtualFileSystemInfo.ANONYMOUS_PRINCIPAL, vfsInfo.getAnonymousPrincipal());
        assertEquals(VirtualFileSystemInfo.ANY_PRINCIPAL, vfsInfo.getAnyPrincipal());
        assertEquals(MY_WORKSPACE_ID, vfsInfo.getId());
        BasicPermissions[] basicPermissions = BasicPermissions.values();
        List<String> expectedPermissions = new ArrayList<>(basicPermissions.length);
        for (BasicPermissions bp : basicPermissions) {
            expectedPermissions.add(bp.value());
        }
        Collection<String> permissions = vfsInfo.getPermissions();
        assertTrue(permissions.containsAll(expectedPermissions));
        assertNotNull(vfsInfo.getRoot());
        assertEquals("/", vfsInfo.getRoot().getPath());
        validateLinks(vfsInfo.getRoot());
        validateUrlTemplates(vfsInfo);
    }
}
