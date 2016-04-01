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
package org.eclipse.che.api.local;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.machine.server.impl.SnapshotImpl;
import org.eclipse.che.api.machine.server.recipe.adapters.InstanceKeyAdapter;
import org.eclipse.che.api.machine.server.spi.InstanceKey;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.util.Collections.singletonMap;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for {@link LocalSnapshotDaoImpl}
 *
 * @author Yevhenii Voevodin
 */
public class LocalSnapshotDaoTest {

    private static Gson GSON = new GsonBuilder().setPrettyPrinting()
                                                .registerTypeAdapter(InstanceKey.class, new InstanceKeyAdapter())
                                                .create();

    private LocalSnapshotDaoImpl snapshotDao;
    private Path                 snapshotsPath;

    @BeforeMethod
    public void setUp() throws Exception {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        final Path targetDir = Paths.get(url.toURI()).getParent();
        final Path storageRoot = targetDir.resolve("snapshots");
        snapshotsPath = storageRoot.resolve("snapshots.json");
        snapshotDao = new LocalSnapshotDaoImpl(new LocalStorageFactory(storageRoot.toString()));
    }

    @Test
    public void testSnapshotsSerialization() throws Exception {
        final SnapshotImpl snapshot = createSnapshot();

        snapshotDao.saveSnapshot(snapshot);
        snapshotDao.saveSnapshots();

        assertEquals(GSON.toJson(singletonMap(snapshot.getId(), snapshot)), new String(readAllBytes(snapshotsPath)));
    }

    @Test
    public void testSnapshotsDeserialization() throws Exception {
        final SnapshotImpl snapshot = createSnapshot();
        write(snapshotsPath, GSON.toJson(singletonMap(snapshot.getId(), snapshot)).getBytes());

        snapshotDao.loadSnapshots();

        final SnapshotImpl result = snapshotDao.getSnapshot(snapshot.getId());
        assertEquals(result, snapshot);
    }

    private SnapshotImpl createSnapshot() {
        return SnapshotImpl.builder()
                           .generateId()
                           .setType("docker")
                           .setInstanceKey(new DummyInstanceKey())
                           .setNamespace("user123")
                           .setWorkspaceId("workspace123")
                           .setMachineName("machine123")
                           .setEnvName("env123")
                           .setDescription("Test snapshot")
                           .setDev(true)
                           .useCurrentCreationDate()
                           .build();
    }

    private static class DummyInstanceKey implements InstanceKey {

        Map<String, String> fields = singletonMap("field1", "value1");

        @Override
        public Map<String, String> getFields() {
            return fields;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof InstanceKey && ((InstanceKey)obj).getFields().equals(getFields());
        }
    }
}
