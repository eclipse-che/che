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

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.Files.readAllBytes;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for {@link LocalSshDaoImpl}
 *
 * @author Sergii Leschenko
 */
public class LocalSshDaoImplTest {

    static Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    LocalSshDaoImpl sshDao;
    Path            sshPath;

    @BeforeMethod
    public void setUp() throws Exception {
        final URL url = Thread.currentThread().getContextClassLoader().getResource(".");
        assertNotNull(url);
        final Path targetDir = Paths.get(url.toURI()).getParent();
        final Path storageRoot = targetDir.resolve("ssh");
        sshPath = storageRoot.resolve("ssh.json");
        sshDao = new LocalSshDaoImpl(new LocalStorageFactory(storageRoot.toString()));
    }

    @Test
    public void testSshPairsSerialization() throws Exception {
        SshPairImpl pair = createPair();

        sshDao.create(pair);
        sshDao.saveSshPairs();

        assertEquals(GSON.toJson(singletonList(pair)), new String(readAllBytes(sshPath)));
    }

    @Test
    public void testSshPairsDeserialization() throws Exception {
        SshPairImpl pair = createPair();
        Files.write(sshPath, GSON.toJson(singletonList(pair)).getBytes());

        sshDao.loadSshPairs();

        List<SshPairImpl> result = sshDao.get("owner", "service");
        assertEquals(result.size(), 1);
        assertEquals(result.get(0), pair);
    }

    private static SshPairImpl createPair() {
        return new SshPairImpl("owner", "service", "name", "publicKey", "privateKey");
    }
}
