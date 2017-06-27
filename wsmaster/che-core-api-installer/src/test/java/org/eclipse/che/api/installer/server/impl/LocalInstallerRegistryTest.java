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

import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.impl.LocalInstallerRegistry.InstallerFqn;
import org.eclipse.che.api.installer.shared.dto.InstallerDto;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.installer.server.impl.LocalInstallerRegistry.InstallerFqn.DEFAULT_VERSION;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Test for {@link LocalInstallerRegistry}.
 *
 * @author Anatolii Bazko
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class LocalInstallerRegistryTest {

    private LocalInstallerRegistry registry;

    @Mock
    private Installer installer1v1;
    @Mock
    private Installer installer1latest;

    @Mock
    private Installer installer2latest;

    @Mock
    private Installer installer3latest;


    @BeforeMethod
    public void setUp() throws Exception {
        when(installer1v1.getId()).thenReturn("installer1");
        when(installer1v1.getVersion()).thenReturn("v1");

        when(installer1latest.getId()).thenReturn("installer1");
        when(installer1latest.getVersion()).thenReturn("latest");

        when(installer2latest.getId()).thenReturn("installer2");
        when(installer2latest.getVersion()).thenReturn(null); // Default version

        when(installer3latest.getId()).thenReturn("installer3");
        when(installer3latest.getVersion()).thenReturn("latest");

        registry = new LocalInstallerRegistry(ImmutableSet.of(installer1v1,
                                                              installer1latest,
                                                              installer2latest,
                                                              installer3latest));
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Installer with fqn 'installer1:latest' has been registered already.")
    public void shouldNotRegisterAgentWithSameIdAndVersion() throws Exception {
        when(installer1v1.getVersion()).thenReturn("latest");
        new LocalInstallerRegistry(ImmutableSet.of(installer1v1, installer1latest));
    }

    @Test(dataProvider = "versions")
    public void shouldReturnVersionsById(String id, Set<String> expectedVersions) throws Exception {
        List<String> versions = registry.getVersions(id);

        assertEquals(versions.size(), expectedVersions.size());
        for (String v : expectedVersions) {
            assertTrue(versions.contains(v));
        }
    }

    @DataProvider(name = "versions")
    public static Object[][] versions() {
        return new Object[][] {{"installer1", ImmutableSet.of("v1", "latest")},
                               {"installer2", ImmutableSet.of("latest")},
                               {"installer3", ImmutableSet.of("latest")}};
    }

    @Test
    public void shouldReturnAllAgents() throws Exception {
        Collection<Installer> installers = registry.getInstallers();

        assertEquals(installers.size(), 4);
        assertTrue(installers.contains(installer1latest));
        assertTrue(installers.contains(installer1v1));
        assertTrue(installers.contains(installer2latest));
        assertTrue(installers.contains(installer3latest));
    }

    @Test(dataProvider = "agentKeys")
    public void shouldReturnAgentByIdAndVersion(String id, String version) throws Exception {
        Installer installer = registry.getInstaller(id + (version != null ? ":" + version : ""));

        assertNotNull(installer);
        assertEquals(installer.getId(), id);
        assertEquals(installer.getVersion(), version == null ? DEFAULT_VERSION : version);
    }

    @DataProvider(name = "agentKeys")
    public static Object[][] agentKeys() {
        return new String[][] {{"installer1", "v1"},
                               {"installer1", "latest"},
                               {"installer1", null}};
    }

    @Test
    public void sortAgentsRespectingDependencies() throws Exception {
        when(installer1v1.getDependencies()).thenReturn(asList("installer2", "installer3"));
        when(installer2latest.getDependencies()).thenReturn(singletonList("installer3"));
        List<Installer> sorted = registry.getOrderedInstallers(asList("installer1:v1", "installer2", "installer3"));

        assertEquals(sorted.size(), 3);
        assertEquals(InstallerFqn.of(sorted.get(0)).toString(), "installer3:latest");
        assertEquals(InstallerFqn.of(sorted.get(1)).toString(), "installer2:latest");
        assertEquals(InstallerFqn.of(sorted.get(2)).toString(), "installer1:v1");
    }

    @Test
    public void shouldReturnInstallerAlongWithItsTransitiveDependenciesOnSorting() throws Exception {
        when(installer1v1.getDependencies()).thenReturn(singletonList("installer2:latest"));
        when(installer2latest.getDependencies()).thenReturn(singletonList("installer3"));

        List<Installer> sorted = registry.getOrderedInstallers(singletonList("installer1:v1"));

        assertEquals(sorted.size(), 3);
        assertEquals(InstallerFqn.of(sorted.get(0)).toString(), "installer3:latest");
        assertEquals(InstallerFqn.of(sorted.get(1)).toString(), "installer2:latest");
        assertEquals(InstallerFqn.of(sorted.get(2)).toString(), "installer1:v1");
    }

    @Test(expectedExceptions = InstallerException.class,
          expectedExceptionsMessageRegExp = "Installers circular dependency found between 'installer1:v1'" +
                                            " and 'installer3:latest'")
    public void sortingShouldFailIfCircularDependenciesFound() throws Exception {
        when(installer1v1.getDependencies()).thenReturn(singletonList("installer2:latest"));
        when(installer2latest.getDependencies()).thenReturn(singletonList("installer3:latest"));
        when(installer3latest.getDependencies()).thenReturn(singletonList("installer1:v1"));

        registry.getOrderedInstallers(asList("installer1:v1", "installer2", "installer3:latest"));
    }
}
