/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.installer.server.impl;

import com.google.common.collect.ImmutableList;

import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.installer.server.InstallerRegistryService;
import org.eclipse.che.api.installer.server.exception.IllegalInstallerKeyException;
import org.eclipse.che.api.installer.server.exception.InstallerConflictException;
import org.eclipse.che.api.installer.server.exception.InstallerExceptionMapper;
import org.eclipse.che.api.installer.server.exception.InstallerNotFoundException;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.everrest.assured.EverrestJetty;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Anatolii Bazko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class RemoteInstallerRegistryTest {
    @SuppressWarnings("unused")
    private static final InstallerExceptionMapper MAPPER = new InstallerExceptionMapper();

    private RemoteInstallerRegistry  remoteInstallerRegistry;
    @SuppressWarnings("unused")
    private InstallerRegistryService registryService;

    private InstallerImpl installer;
    private InstallerFqn  installerFqn;
    private String        installerKey;


    @BeforeMethod
    public void setUp(ITestContext context) throws Exception {
        installer = TestInstallerFactory.createInstaller("id_0", "version_0");
        installerFqn = InstallerFqn.of(installer);
        installerKey = InstallerFqn.of(installer).toKey();

        LocalInstallerRegistry localInstallerRegistry = new LocalInstallerRegistry(Collections.singleton(installer), new MapBasedInstallerDao());
        registryService = new InstallerRegistryService(localInstallerRegistry);

        Integer port = (Integer)context.getAttribute(EverrestJetty.JETTY_PORT);
        remoteInstallerRegistry = new RemoteInstallerRegistry("http://localhost:" + port + "/rest",
                                                              new DefaultHttpJsonRequestFactory());
    }

    @Test
    public void shouldAddNewInstaller() throws Exception {
        InstallerImpl newInstaller = TestInstallerFactory.createInstaller("id_1", "version_1");
        String newInstallerKey = InstallerFqn.of(newInstaller).toKey();

        remoteInstallerRegistry.add(newInstaller);

        assertInstaller(remoteInstallerRegistry.getInstaller(newInstallerKey), newInstaller);
    }

    @Test(expectedExceptions = InstallerConflictException.class)
    public void shouldThrowInstallerConflictExceptionOnAddingIfInstallerExist() throws Exception {
        remoteInstallerRegistry.add(installer);
    }

    @Test
    public void shouldUpdateInstaller() throws Exception {
        InstallerImpl newInstaller = TestInstallerFactory.createInstaller("id_0", "version_0");
        String newInstallerKey = InstallerFqn.of(newInstaller).toKey();

        remoteInstallerRegistry.update(newInstaller);

        assertInstaller(remoteInstallerRegistry.getInstaller(newInstallerKey), newInstaller);
    }

    @Test(expectedExceptions = InstallerNotFoundException.class)
    public void shouldThrowInstallerNotFoundExceptionOnUpdatingIfInstallerDoesNotExist() throws Exception {
        InstallerImpl newInstaller = TestInstallerFactory.createInstaller("id_1", "version_1");

        remoteInstallerRegistry.update(newInstaller);
    }

    @Test(expectedExceptions = InstallerNotFoundException.class)
    public void shouldRemoveInstaller() throws Exception {
        remoteInstallerRegistry.remove(installerFqn);

        remoteInstallerRegistry.getInstaller(installerKey);
    }

    @Test
    public void shouldNotThrowExceptionOnRemovalIfInstallerDoesNotExist() throws Exception {
        InstallerImpl newInstaller = TestInstallerFactory.createInstaller("id_1", "version_1");

        remoteInstallerRegistry.remove(InstallerFqn.of(newInstaller));
    }

    @Test
    public void shouldReturnInstallerByFqn() throws Exception {
        assertInstaller(remoteInstallerRegistry.getInstaller(installerKey), installer);
    }

    @Test(expectedExceptions = InstallerNotFoundException.class)
    public void shouldThrowInstallerNotFoundExceptionIfInstallerDoesNotExist() throws Exception {
        remoteInstallerRegistry.getInstaller("non-existed:non-existed");
    }

    @Test(expectedExceptions = IllegalInstallerKeyException.class)
    public void shouldThrowInstallerIllegalInstallerKeyExceptionIfKeyInvalid() throws Exception {
        remoteInstallerRegistry.getInstaller("1:2:3");
    }

    @Test
    public void shouldReturnAllVersions() throws Exception {
        List<String> versions = remoteInstallerRegistry.getVersions(installer.getId());

        assertEquals(versions.size(), 1);
        assertEquals(versions.get(0), installer.getVersion());
    }

    @Test
    public void shouldReturnEmptyListOnGetVersionsIfNoInstallerExist() throws Exception {
        List<String> versions = remoteInstallerRegistry.getVersions("non-existed");

        assertTrue(versions.isEmpty());
    }

    @Test
    public void shouldReturnAllInstallers() throws Exception {
        List<Installer> installers = remoteInstallerRegistry.getInstallers();

        assertEquals(installers.size(), 1);
        assertInstaller(installers.get(0), installer);
    }

    @Test
    public void shouldReturnEmptyListOnGetInstallersIfNoInstallersExist() throws Exception {
        remoteInstallerRegistry.remove(installerFqn);

        List<Installer> installers = remoteInstallerRegistry.getInstallers();

        assertTrue(installers.isEmpty());
    }

    @Test
    public void shouldReturnOrderedInstallers() throws Exception {
        InstallerImpl installer1 = TestInstallerFactory.createInstaller("id_1", "version_1");
        InstallerImpl installer2 = TestInstallerFactory.createInstaller("id_2", "version_2");

        installer.setDependencies(Collections.emptyList());
        installer1.setDependencies(Collections.singletonList("id_0:version_0"));
        installer2.setDependencies(Collections.singletonList("id_1:version_1"));

        remoteInstallerRegistry.update(installer);
        remoteInstallerRegistry.add(installer1);
        remoteInstallerRegistry.add(installer2);

        List<Installer> orderedInstallers = remoteInstallerRegistry.getOrderedInstallers(ImmutableList.of("id_2:version_2"));

        assertEquals(orderedInstallers.size(), 3);
        assertInstaller(orderedInstallers.get(0), installer);
        assertInstaller(orderedInstallers.get(1), installer1);
        assertInstaller(orderedInstallers.get(2), installer2);
    }

    private void assertInstaller(Installer actual, Installer expected) {
        assertEquals(actual.getId(), expected.getId());
        assertEquals(actual.getVersion(), expected.getVersion());
        assertEquals(actual.getDescription(), expected.getDescription());
        assertEquals(actual.getScript(), expected.getScript());
        assertEquals(actual.getName(), expected.getName());
        assertEquals(actual.getDependencies(), expected.getDependencies());
        assertEquals(actual.getProperties(), expected.getProperties());

        assertEquals(actual.getServers().size(), expected.getServers().size());

        for (Entry<String, ? extends ServerConfig> e : actual.getServers().entrySet()) {
            expected.getServers().containsKey(e.getKey());

            ServerConfig actualServerConfig = e.getValue();
            ServerConfig expectedServerConfig = expected.getServers().get(e.getKey());

            assertEquals(actualServerConfig.getPath(), expectedServerConfig.getPath());
            assertEquals(actualServerConfig.getPort(), expectedServerConfig.getPort());
            assertEquals(actualServerConfig.getProtocol(), expectedServerConfig.getProtocol());
        }
    }
}
