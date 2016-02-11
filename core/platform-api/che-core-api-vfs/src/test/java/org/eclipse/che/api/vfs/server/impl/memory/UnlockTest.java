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
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.io.ByteArrayInputStream;

import javax.ws.rs.HttpMethod;

/** @author andrew00x */
public class UnlockTest extends MemoryFileSystemTest {
    private String lockedFileId;
    private String notLockedFileId;
    private String fileLockToken;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String name = getClass().getName();
        VirtualFile unlockTestFolder = mountPoint.getRoot().createFolder(name);

        VirtualFile lockedFile = unlockTestFolder.createFile("UnlockTest_LOCKED",
                                                             new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        fileLockToken = lockedFile.lock(0);
        lockedFileId = lockedFile.getId();

        VirtualFile notLockedFile = unlockTestFolder.createFile("UnlockTest_NOTLOCKED",
                                                                new ByteArrayInputStream(DEFAULT_CONTENT.getBytes()));
        notLockedFileId = notLockedFile.getId();
    }

    public void testUnlockFile() throws Exception {
        String path = SERVICE_URI + "unlock/" + lockedFileId + '?' + "lockToken=" + fileLockToken;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, null);
        assertEquals(204, response.getStatus());
        VirtualFile file = mountPoint.getVirtualFileById(lockedFileId);
        assertFalse("Lock must be removed. ", file.isLocked());
    }

    public void testUnlockFileNoLockToken() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "unlock/" + lockedFileId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
        assertEquals(403, response.getStatus());
        log.info(new String(writer.getBody()));
    }

    public void testUnlockFileWrongLockToken() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "unlock/" + lockedFileId + '?' + "lockToken=" + fileLockToken + "_WRONG";
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
        assertEquals(403, response.getStatus());
        log.info(new String(writer.getBody()));
    }


    public void testUnlockFileNotLocked() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String path = SERVICE_URI + "unlock/" + notLockedFileId;
        ContainerResponse response = launcher.service(HttpMethod.POST, path, BASE_URI, null, null, writer, null);
        assertEquals(409, response.getStatus());
        log.info(new String(writer.getBody()));
    }
}
