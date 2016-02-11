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

import org.eclipse.che.api.vfs.shared.dto.Principal;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.user.UserImpl;
import org.eclipse.che.dto.server.DtoFactory;

import com.google.common.collect.Sets;

import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.tools.ByteArrayContainerResponseWriter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.ws.rs.HttpMethod;

import static org.eclipse.che.api.vfs.shared.dto.VirtualFileSystemInfo.BasicPermissions;

public class DeleteTest extends LocalFileSystemTest {
    private final String lockToken = "01234567890abcdef";

    private String filePath;
    private String fileId;

    private String lockedFileId;
    private String lockedFilePath;

    private String protectedFilePath;
    private String protectedFileId;

    private String protectedFolderPath;
    private String protectedFolderId;

    private String protectedChildFolderPath;
    private String protectedChildFolderId;

    private String lockedChildFolderPath;
    private String lockedChildFolderId;

    private String folderPath;
    private String folderId;

    private String notEmptyFolderPath;
    private String notEmptyFolderId;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Map<Principal, Set<String>> permissions = new HashMap<>(2);
        Principal user = DtoFactory.getInstance().createDto(Principal.class).withName("andrew").withType(Principal.Type.USER);
        Principal admin = DtoFactory.getInstance().createDto(Principal.class).withName("admin").withType(Principal.Type.USER);
        permissions.put(user, Sets.newHashSet(BasicPermissions.ALL.value()));
        permissions.put(admin, Sets.newHashSet(BasicPermissions.READ.value()));

        filePath = createFile(testRootPath, "DeleteTest_File", DEFAULT_CONTENT_BYTES);
        lockedFilePath = createFile(testRootPath, "DeleteTest_LockedFile", DEFAULT_CONTENT_BYTES);
        protectedFilePath = createFile(testRootPath, "DeleteTest_ProtectedFile", DEFAULT_CONTENT_BYTES);
        folderPath = createDirectory(testRootPath, "DeleteTest_Folder");
        protectedFolderPath = createDirectory(testRootPath, "DeleteTest_ProtectedFolder");
        createTree(protectedFolderPath, 6, 4, null);
        notEmptyFolderPath = createDirectory(testRootPath, "DeleteTest_NotEmptyFolder");
        createTree(notEmptyFolderPath, 6, 4, null);
        protectedChildFolderPath = createDirectory(testRootPath, "DeleteTest_ProtectedChildFolder");
        createTree(protectedChildFolderPath, 6, 4, null);
        lockedChildFolderPath = createDirectory(testRootPath, "DeleteTest_LockedChildFolder");
        createTree(lockedChildFolderPath, 6, 4, null);

        List<String> l = flattenDirectory(protectedChildFolderPath);
        // Find one child in the list and remove write permission for 'admin'.
        writePermissions(protectedChildFolderPath + '/' + l.get(new Random().nextInt(l.size())), permissions);

        l = flattenDirectory(lockedChildFolderPath);
        // Find one child in the list and lock it.
        for (String s : l) {
            if (createLock(lockedChildFolderPath + '/' + s, lockToken, Long.MAX_VALUE)) {
                break;
            }
        }

        writePermissions(protectedFilePath, permissions);
        writePermissions(protectedFolderPath, permissions);

        createLock(lockedFilePath, lockToken, Long.MAX_VALUE);

        Map<String, String[]> properties = new HashMap<>(2);
        properties.put("MyProperty01", new String[]{"foo"});
        properties.put("MyProperty02", new String[]{"bar"});
        writeProperties(filePath, properties);


        fileId = pathToId(filePath);
        lockedFileId = pathToId(lockedFilePath);
        protectedFileId = pathToId(protectedFilePath);
        folderId = pathToId(folderPath);
        protectedFolderId = pathToId(protectedFolderPath);
        protectedChildFolderId = pathToId(protectedChildFolderPath);
        lockedChildFolderId = pathToId(lockedChildFolderPath);
        notEmptyFolderId = pathToId(notEmptyFolderPath);
    }

    public void testDeleteFile() throws Exception {
        String requestPath = SERVICE_URI + "delete/" + fileId;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, null);
        assertEquals(204, response.getStatus());
        assertFalse("File must be removed. ", exists(filePath));
        assertNull("Properties must be removed. ", readProperties(filePath));
    }

    public void testDeleteFileLocked() throws Exception {
        String requestPath = SERVICE_URI + "delete/" + lockedFileId + '?' + "lockToken=" + lockToken;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, null);
        assertEquals(204, response.getStatus());
        assertFalse("File must be removed. ", exists(lockedFilePath));
        assertNull("Lock file must be removed. ", readLock(lockedFilePath));
    }

    public void testDeleteFileLockedNoLockToken() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "delete/" + lockedFileId;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, writer, null);
        assertEquals(403, response.getStatus());
        log.info(new String(writer.getBody()));
        assertTrue("File must not be removed. ", exists(lockedFilePath));
        assertEquals(lockToken, readLock(lockedFilePath).getLockToken()); // lock file must not be removed
    }

    public void testDeleteFileHavePermissions() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "delete/" + protectedFileId;
        // File is protected and default principal 'andrew' has not write permission.
        // Replace default principal by principal who has write permission.
        EnvironmentContext.getCurrent().setUser(new UserImpl("andrew", "andrew", null, Arrays.asList("workspace/developer"), false));
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, writer, null);
        assertEquals(204, response.getStatus());
        assertFalse("File must not be removed. ", exists(protectedFilePath));
        assertNull("ACL file must be removed. ", readPermissions(protectedFilePath)); // file which stored ACL must be removed
    }

    public void testDeleteFileWrongId() throws Exception {
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "delete/" + fileId + "_WRONG_ID";
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, writer, null);
        log.info(new String(writer.getBody()));
        assertEquals(404, response.getStatus());
        assertTrue(exists(filePath));
    }

    public void testDeleteFolder() throws Exception {
        String requestPath = SERVICE_URI + "delete/" + folderId;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, null);
        assertEquals(204, response.getStatus());
        assertFalse("Folder must be removed. ", exists(folderPath));
    }

    public void testDeleteRootFolder() throws Exception {
        String requestPath = SERVICE_URI + "delete/" + ROOT_ID;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, null);
        assertEquals(403, response.getStatus()); // must not be able delete root folder
        assertTrue("Folder must not be removed. ", exists("/"));
    }

    public void testDeleteFolderChildLocked() throws Exception {
        List<String> before = flattenDirectory(lockedChildFolderPath);
        ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
        String requestPath = SERVICE_URI + "delete/" + lockedChildFolderId;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, writer, null);
        assertEquals(403, response.getStatus());
        log.info(new String(writer.getBody()));
        assertTrue("Folder must not be removed. ", exists(lockedChildFolderPath));
        List<String> after = flattenDirectory(lockedChildFolderPath);
        before.removeAll(after);
        assertTrue(String.format("Missed items: %s", before), before.isEmpty());
    }

    public void testDeleteTree() throws Exception {
        String requestPath = SERVICE_URI + "delete/" + notEmptyFolderId;
        ContainerResponse response = launcher.service(HttpMethod.POST, requestPath, BASE_URI, null, null, null);
        assertEquals(204, response.getStatus());
        assertFalse("Folder must be removed. ", exists(notEmptyFolderPath));
    }
}
