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

import org.eclipse.che.api.core.ServerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author andrew00x
 */
public class MappedDirectoryLocalFSMountStrategyTest {
    private final String workspaceId = "ws1";
    private java.io.File mountPath;

    private java.io.File mappingFile;

    @Before
    public void setUp() throws Exception {
        java.io.File testDir = new java.io.File(Thread.currentThread().getContextClassLoader().getResource(".").toURI()).getParentFile();
        mappingFile = new java.io.File(testDir, "directory_mapping.properties");
        mountPath = new java.io.File(testDir, "ws1");
    }

    @After
    public void tearDown() throws Exception {
        mappingFile.delete();
    }

    @Test
    public void loadsMappingFromPropertiesFile() throws Exception {
        final Properties mappingProperties = new Properties();
        mappingProperties.setProperty(workspaceId, mountPath.getAbsolutePath());
        try (Writer out = new FileWriter(mappingFile)) {
            mappingProperties.store(out, null);
        }

        final MappedDirectoryLocalFSMountStrategy mountStrategy = new MappedDirectoryLocalFSMountStrategy();
        mountStrategy.loadFromPropertiesFile(mappingFile);

        final Map<String, java.io.File> mapping = mountStrategy.getDirectoryMapping();
        assertEquals(mountPath, mapping.get(workspaceId));
    }

    @Test
    public void savesMappingInPropertiesFile() throws Exception {
        final Map<String, java.io.File> mapping = new HashMap<>();
        mapping.put(workspaceId, mountPath);

        final MappedDirectoryLocalFSMountStrategy mountStrategy = new MappedDirectoryLocalFSMountStrategy(mapping);
        mountStrategy.saveInPropertiesFile(mappingFile);

        final Properties mappingProperties = new Properties();
        try (Reader in = new FileReader(mappingFile)) {
            mappingProperties.load(in);
        }

        assertEquals(mountPath, new java.io.File(mappingProperties.getProperty(workspaceId)));
    }

    @Test
    public void setsMountPath() {
        final MappedDirectoryLocalFSMountStrategy mountStrategy = new MappedDirectoryLocalFSMountStrategy();
        mountStrategy.setMountPath(workspaceId, mountPath);

        assertEquals(mountPath, mountStrategy.getDirectoryMapping().get(workspaceId));
    }

    @Test
    public void removesMountPath() {
        final Map<String, java.io.File> mapping = new HashMap<>();
        mapping.put(workspaceId, mountPath);

        final MappedDirectoryLocalFSMountStrategy mountStrategy = new MappedDirectoryLocalFSMountStrategy(mapping);
        mountStrategy.removeMountPath(workspaceId);

        assertNull(mountStrategy.getDirectoryMapping().get(workspaceId));
    }

    @Test
    public void getsMountPathForWorkspace() throws Exception {
        final String workspaceId = "ws_1";
        final Map<String, java.io.File> mapping = new HashMap<>();
        mapping.put(workspaceId, mountPath);

        final LocalFSMountStrategy mountStrategy = new MappedDirectoryLocalFSMountStrategy(mapping);
        assertEquals(mountPath, mountStrategy.getMountPath(workspaceId));
    }

    @Test(expected = ServerException.class)
    public void throwsExceptionIfMountPathForWorkspaceIsNotSet() throws Exception {
        final LocalFSMountStrategy mountStrategy = new MappedDirectoryLocalFSMountStrategy();
        mountStrategy.getMountPath(workspaceId);
    }

    @Test(expected = ServerException.class)
    public void throwsExceptionIfWorkspaceIdIsNull() throws Exception {
        final Map<String, java.io.File> mapping = new HashMap<>();
        mapping.put(workspaceId, mountPath);

        final LocalFSMountStrategy mountStrategy = new MappedDirectoryLocalFSMountStrategy(mapping);
        mountStrategy.getMountPath(null);
    }
}
