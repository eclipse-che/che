/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.installer.server.impl;

import com.google.common.collect.ImmutableSet;

import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.exception.InstallerNotFoundException;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.installer.server.impl.InstallerFqn.DEFAULT_VERSION;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
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

    private MapBasedInstallerDao installerDao;


    @BeforeMethod
    public void setUp() throws Exception {
        installerDao = new MapBasedInstallerDao();

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
                                                              installer3latest),
                                              installerDao);
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
    public void shouldReturnAllInstallers() throws Exception {
        Page<? extends Installer> installers = registry.getInstallers(Integer.MAX_VALUE, 0);

        assertEquals(installers.getTotalItemsCount(), 4);
        assertEquals(installers.getItemsCount(), 4);
        assertEquals(installers.getSize(), Integer.MAX_VALUE);
        assertFalse(installers.hasNextPage());
        assertFalse(installers.hasPreviousPage());
    }

    @Test
    public void shouldReturnFirstPage() throws Exception {
        Page<? extends Installer> installers = registry.getInstallers(1, 0);
        assertEquals(installers.getTotalItemsCount(), 4);
        assertEquals(installers.getItemsCount(), 1);
        assertEquals(installers.getSize(), 1);
        assertTrue(installers.hasNextPage());
        assertFalse(installers.hasPreviousPage());

        Page.PageRef nextPageRef = installers.getNextPageRef();
        assertEquals(nextPageRef.getItemsBefore(), 1);
        assertEquals(nextPageRef.getPageSize(), 1);

        List<? extends Installer> items = installers.getItems();
        assertEquals(items.size(), 1);
        assertEquals(items.get(0), new InstallerImpl(installer1v1));
    }

    @Test
    public void shouldReturnMiddlePage() throws Exception {
        Page<? extends Installer> installers = registry.getInstallers(1, 1);
        assertEquals(installers.getTotalItemsCount(), 4);
        assertEquals(installers.getItemsCount(), 1);
        assertEquals(installers.getSize(), 1);
        assertTrue(installers.hasNextPage());
        assertTrue(installers.hasPreviousPage());

        Page.PageRef nextPageRef = installers.getNextPageRef();
        assertEquals(nextPageRef.getItemsBefore(), 2);
        assertEquals(nextPageRef.getPageSize(), 1);

        Page.PageRef previousPageRef = installers.getPreviousPageRef();
        assertEquals(previousPageRef.getItemsBefore(), 0);
        assertEquals(previousPageRef.getPageSize(), 1);

        List<? extends Installer> items = installers.getItems();
        assertEquals(items.size(), 1);
        assertTrue(items.contains(new InstallerImpl(installer3latest)));
    }

    @Test
    public void shouldReturnLastPage() throws Exception {
        Page<? extends Installer> installers = registry.getInstallers(3, 2);
        assertEquals(installers.getTotalItemsCount(), 4);
        assertEquals(installers.getItemsCount(), 2);
        assertEquals(installers.getSize(), 3);
        assertFalse(installers.hasNextPage());
        assertFalse(installers.hasPreviousPage());

        List<? extends Installer> items = installers.getItems();
        assertEquals(items.size(), 2);
        assertTrue(items.contains(new InstallerImpl(installer1latest)));
        assertTrue(items.contains(new InstallerImpl(installer2latest)));
    }

    @Test
    public void shouldReturnEmptyPageIfNoInstallersFound() throws Exception {
        registry.remove("installer1:v1");
        registry.remove("installer1:latest");
        registry.remove("installer2:latest");
        registry.remove("installer3:latest");

        Page<? extends Installer> installers = registry.getInstallers(Integer.MAX_VALUE, 0);

        assertEquals(installers.getTotalItemsCount(), 0);
        assertEquals(installers.getItemsCount(), 0);
        assertEquals(installers.getSize(), Integer.MAX_VALUE);
        assertFalse(installers.hasNextPage());
        assertFalse(installers.hasPreviousPage());
    }

    @Test(dataProvider = "installerKeys")
    public void shouldReturnInstallerByIdAndVersion(String id, String version) throws Exception {
        Installer installer = registry.getInstaller(id + (version != null ? ":" + version : ""));

        assertNotNull(installer);
        assertNotNull(installer.getVersion());
        assertEquals(installer.getId(), id);
        assertEquals(installer.getVersion(), version == null ? DEFAULT_VERSION : version);
    }

    @DataProvider(name = "installerKeys")
    public static Object[][] installerKeys() {
        return new String[][] {{"installer1", "v1"},
                               {"installer1", "latest"},
                               {"installer1", null}};
    }

    @Test(expectedExceptions = InstallerNotFoundException.class)
    public void shouldThrowInstallerNotFoundExceptionIfInstallerDoesNotExist() throws Exception {
        registry.getInstaller("non-existed");
    }

    @Test
    public void sortInstallersRespectingDependencies() throws Exception {
        when(installer1v1.getDependencies()).thenReturn(asList("installer2", "installer3"));
        when(installer2latest.getDependencies()).thenReturn(singletonList("installer3"));

        installerDao.update(new InstallerImpl(installer1v1));
        installerDao.update(new InstallerImpl(installer2latest));

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

        installerDao.update(new InstallerImpl(installer1v1));
        installerDao.update(new InstallerImpl(installer2latest));

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

        installerDao.update(new InstallerImpl(installer1v1));
        installerDao.update(new InstallerImpl(installer2latest));
        installerDao.update(new InstallerImpl(installer3latest));

        registry.getOrderedInstallers(asList("installer1:v1", "installer2", "installer3:latest"));
    }
}
