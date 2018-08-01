/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.installer.server.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
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

/**
 * Test for {@link LocalInstallerRegistry}.
 *
 * @author Anatolii Bazko
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class LocalInstallerRegistryTest {

  private LocalInstallerRegistry registry;

  @Mock private Installer installer1v1;
  @Mock private Installer installer1v2;
  @Mock private Installer installer2v1;
  @Mock private Installer installer3v1;
  @Mock private InstallerValidator installerValidator;

  private MapBasedInstallerDao installerDao;

  @BeforeMethod
  public void setUp() throws Exception {
    installerDao = new MapBasedInstallerDao();

    when(installer1v1.getId()).thenReturn("installer1");
    when(installer1v1.getVersion()).thenReturn("1.0.0");

    when(installer1v2.getId()).thenReturn("installer1");
    when(installer1v2.getVersion()).thenReturn("2.0.0");

    when(installer2v1.getId()).thenReturn("installer2");
    when(installer2v1.getVersion()).thenReturn("1.0.0"); // Default version

    when(installer3v1.getId()).thenReturn("installer3");
    when(installer3v1.getVersion()).thenReturn("1.0.0");

    registry =
        new LocalInstallerRegistry(
            ImmutableSet.of(installer1v1, installer1v2, installer2v1, installer3v1),
            installerDao,
            installerValidator);
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
    return new Object[][] {
      {"installer1", ImmutableSet.of("1.0.0", "2.0.0")},
      {"installer2", ImmutableSet.of("1.0.0")},
      {"installer3", ImmutableSet.of("1.0.0")}
    };
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
    assertTrue(items.contains(new InstallerImpl(installer1v2)));
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
    assertTrue(items.contains(new InstallerImpl(installer2v1)));
    assertTrue(items.contains(new InstallerImpl(installer3v1)));
  }

  @Test
  public void shouldReturnEmptyPageIfNoInstallersFound() throws Exception {
    registry.remove("installer1:1.0.0");
    registry.remove("installer1:2.0.0");
    registry.remove("installer2:1.0.0");
    registry.remove("installer3:1.0.0");

    Page<? extends Installer> installers = registry.getInstallers(Integer.MAX_VALUE, 0);

    assertEquals(installers.getTotalItemsCount(), 0);
    assertEquals(installers.getItemsCount(), 0);
    assertEquals(installers.getSize(), Integer.MAX_VALUE);
    assertFalse(installers.hasNextPage());
    assertFalse(installers.hasPreviousPage());
  }

  @Test
  public void shouldReturnInstallerByIdAndVersion() throws Exception {
    Installer installer = registry.getInstaller("installer1:1.0.0");

    assertNotNull(installer);
    assertNotNull(installer.getVersion());
    assertEquals(installer.getId(), "installer1");
    assertEquals(installer.getVersion(), "1.0.0");
  }

  @Test
  public void shouldReturnLatestInstaller() throws Exception {
    Installer installer = registry.getInstaller("installer1");

    assertNotNull(installer);
    assertNotNull(installer.getVersion());
    assertEquals(installer.getId(), "installer1");
    assertEquals(installer.getVersion(), "2.0.0");
  }

  @Test(expectedExceptions = InstallerNotFoundException.class)
  public void shouldThrowInstallerNotFoundExceptionIfInstallerDoesNotExist() throws Exception {
    registry.getInstaller("non-existed");
  }

  @Test
  public void sortInstallersRespectingDependencies() throws Exception {
    when(installer1v1.getDependencies()).thenReturn(asList("installer2", "installer3"));
    when(installer2v1.getDependencies()).thenReturn(singletonList("installer3"));

    installerDao.update(new InstallerImpl(installer1v1));
    installerDao.update(new InstallerImpl(installer2v1));

    List<Installer> sorted =
        registry.getOrderedInstallers(asList("installer1:1.0.0", "installer2", "installer3"));

    assertEquals(sorted.size(), 3);
    assertEquals(InstallerFqn.of(sorted.get(0)).toString(), "installer3:1.0.0");
    assertEquals(InstallerFqn.of(sorted.get(1)).toString(), "installer2:1.0.0");
    assertEquals(InstallerFqn.of(sorted.get(2)).toString(), "installer1:1.0.0");
  }

  @Test
  public void shouldReturnInstallerAlongWithItsTransitiveDependenciesOnSorting() throws Exception {
    when(installer1v1.getDependencies()).thenReturn(singletonList("installer2:1.0.0"));
    when(installer2v1.getDependencies()).thenReturn(singletonList("installer3"));

    installerDao.update(new InstallerImpl(installer1v1));
    installerDao.update(new InstallerImpl(installer2v1));

    List<Installer> sorted = registry.getOrderedInstallers(singletonList("installer1:1.0.0"));

    assertEquals(sorted.size(), 3);
    assertEquals(InstallerFqn.of(sorted.get(0)).toString(), "installer3:1.0.0");
    assertEquals(InstallerFqn.of(sorted.get(1)).toString(), "installer2:1.0.0");
    assertEquals(InstallerFqn.of(sorted.get(2)).toString(), "installer1:1.0.0");
  }

  @Test(
    expectedExceptions = InstallerException.class,
    expectedExceptionsMessageRegExp =
        "Installers circular dependency found between 'installer1:1.0.0'"
            + " and 'installer3:1.0.0'"
  )
  public void sortingShouldFailIfCircularDependenciesFound() throws Exception {
    when(installer1v1.getDependencies()).thenReturn(singletonList("installer2:1.0.0"));
    when(installer2v1.getDependencies()).thenReturn(singletonList("installer3:1.0.0"));
    when(installer3v1.getDependencies()).thenReturn(singletonList("installer1:1.0.0"));

    installerDao.update(new InstallerImpl(installer1v1));
    installerDao.update(new InstallerImpl(installer2v1));
    installerDao.update(new InstallerImpl(installer3v1));

    registry.getOrderedInstallers(
        asList("installer1:1.0.0", "installer2:1.0.0", "installer3:1.0.0"));
  }

  @Test(
    expectedExceptions = InstallerException.class,
    expectedExceptionsMessageRegExp =
        "Installers dependencies conflict. Several version '2.0.0' and '1.0.0' of the some id 'installer1"
  )
  public void shouldNotReturnOrderedSeveralInstallersDifferentVersions() throws Exception {
    when(installer2v1.getDependencies()).thenReturn(singletonList("installer1:1.0.0"));
    when(installer3v1.getDependencies()).thenReturn(singletonList("installer1:2.0.0"));

    installerDao.update(new InstallerImpl(installer2v1));
    installerDao.update(new InstallerImpl(installer3v1));

    registry.getOrderedInstallers(asList("installer2:1.0.0", "installer3:1.0.0"));
  }
}
