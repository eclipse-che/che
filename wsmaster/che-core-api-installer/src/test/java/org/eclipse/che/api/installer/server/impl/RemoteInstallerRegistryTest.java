/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.installer.server.impl;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.api.installer.server.InstallerRegistryService;
import org.eclipse.che.api.installer.server.exception.IllegalInstallerKeyException;
import org.eclipse.che.api.installer.server.exception.InstallerAlreadyExistsException;
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

/** @author Anatolii Bazko */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class RemoteInstallerRegistryTest {
  @SuppressWarnings("unused")
  private static final InstallerExceptionMapper MAPPER = new InstallerExceptionMapper();

  private RemoteInstallerRegistry registry;

  @SuppressWarnings("unused")
  private InstallerRegistryService registryService;

  private InstallerImpl installer;
  private String installerKey;

  @BeforeMethod
  public void setUp(ITestContext context) throws Exception {
    installer = TestInstallerFactory.createInstaller("id_0", "1.0.0");
    installerKey = InstallerFqn.of(installer).toKey();

    LocalInstallerRegistry localInstallerRegistry =
        new LocalInstallerRegistry(
            Collections.singleton(installer), new MapBasedInstallerDao(), new InstallerValidator());

    registryService = new InstallerRegistryService(localInstallerRegistry);

    Integer port = (Integer) context.getAttribute(EverrestJetty.JETTY_PORT);
    registry =
        new RemoteInstallerRegistry(
            "http://localhost:" + port + "/rest", new DefaultHttpJsonRequestFactory());
  }

  @Test
  public void shouldAddNewInstaller() throws Exception {
    InstallerImpl newInstaller = TestInstallerFactory.createInstaller("id_1", "1.0.1");
    String newInstallerKey = InstallerFqn.of(newInstaller).toKey();

    registry.add(newInstaller);

    assertInstaller(registry.getInstaller(newInstallerKey), newInstaller);
  }

  @Test(expectedExceptions = InstallerAlreadyExistsException.class)
  public void shouldThrowInstallerConflictExceptionOnAddingIfInstallerExist() throws Exception {
    registry.add(installer);
  }

  @Test
  public void shouldUpdateInstaller() throws Exception {
    InstallerImpl newInstaller = TestInstallerFactory.createInstaller("id_0", "1.0.0");
    String newInstallerKey = InstallerFqn.of(newInstaller).toKey();

    registry.update(newInstaller);

    assertInstaller(registry.getInstaller(newInstallerKey), newInstaller);
  }

  @Test(expectedExceptions = InstallerNotFoundException.class)
  public void shouldThrowInstallerNotFoundExceptionOnUpdatingIfInstallerDoesNotExist()
      throws Exception {
    InstallerImpl newInstaller = TestInstallerFactory.createInstaller("id_1", "1.0.1");

    registry.update(newInstaller);
  }

  @Test(expectedExceptions = InstallerNotFoundException.class)
  public void shouldRemoveInstaller() throws Exception {
    registry.remove(installerKey);

    registry.getInstaller(installerKey);
  }

  @Test
  public void shouldNotThrowExceptionOnRemovalIfInstallerDoesNotExist() throws Exception {
    registry.remove("id_1:version_1");
  }

  @Test
  public void shouldReturnInstallerByFqn() throws Exception {
    assertInstaller(registry.getInstaller(installerKey), installer);
  }

  @Test(expectedExceptions = InstallerNotFoundException.class)
  public void shouldThrowInstallerNotFoundExceptionIfInstallerDoesNotExist() throws Exception {
    registry.getInstaller("non-existed:non-existed");
  }

  @Test(expectedExceptions = IllegalInstallerKeyException.class)
  public void shouldThrowInstallerIllegalInstallerKeyExceptionIfKeyInvalid() throws Exception {
    registry.getInstaller("1:2:3");
  }

  @Test
  public void shouldReturnAllVersions() throws Exception {
    List<String> versions = registry.getVersions(installer.getId());

    assertEquals(versions.size(), 1);
    assertEquals(versions.get(0), installer.getVersion());
  }

  @Test
  public void shouldReturnEmptyListOnGetVersionsIfNoInstallerExist() throws Exception {
    List<String> versions = registry.getVersions("non-existed");

    assertTrue(versions.isEmpty());
  }

  @Test
  public void shouldReturnAllInstallers() throws Exception {
    Page<? extends Installer> installers = registry.getInstallers(Integer.MAX_VALUE, 0);

    assertEquals(installers.getTotalItemsCount(), 1);
    assertEquals(installers.getItemsCount(), 1);
    assertEquals(installers.getSize(), Integer.MAX_VALUE);
    assertFalse(installers.hasNextPage());
    assertFalse(installers.hasPreviousPage());
  }

  @Test
  public void shouldReturnFirstPage() throws Exception {
    InstallerImpl installer1 = TestInstallerFactory.createInstaller("id_1", "1.0.1");
    InstallerImpl installer2 = TestInstallerFactory.createInstaller("id_2", "1.0.2");
    registry.add(installer1);
    registry.add(installer2);

    Page<? extends Installer> installers = registry.getInstallers(1, 0);
    assertEquals(installers.getTotalItemsCount(), 3);
    assertEquals(installers.getItemsCount(), 1);
    assertEquals(installers.getSize(), 1);
    assertTrue(installers.hasNextPage());
    assertFalse(installers.hasPreviousPage());

    Page.PageRef nextPageRef = installers.getNextPageRef();
    assertEquals(nextPageRef.getItemsBefore(), 1);
    assertEquals(nextPageRef.getPageSize(), 1);

    List<? extends Installer> items = installers.getItems();
    assertEquals(items.size(), 1);
    assertInstaller(items.get(0), installer);
  }

  @Test
  public void shouldReturnMiddlePage() throws Exception {
    InstallerImpl installer1 = TestInstallerFactory.createInstaller("id_1", "1.0.1");
    InstallerImpl installer2 = TestInstallerFactory.createInstaller("id_2", "1.0.2");
    registry.add(installer1);
    registry.add(installer2);

    Page<? extends Installer> installers = registry.getInstallers(1, 1);
    assertEquals(installers.getTotalItemsCount(), 3);
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
    assertInstaller(items.get(0), installer1);
  }

  @Test
  public void shouldReturnLastPage() throws Exception {
    InstallerImpl installer1 = TestInstallerFactory.createInstaller("id_1", "1.0.1");
    InstallerImpl installer2 = TestInstallerFactory.createInstaller("id_2", "1.0.2");
    registry.add(installer1);
    registry.add(installer2);

    Page<? extends Installer> installers = registry.getInstallers(3, 1);
    assertEquals(installers.getTotalItemsCount(), 3);
    assertEquals(installers.getItemsCount(), 2);
    assertEquals(installers.getSize(), 3);
    assertFalse(installers.hasNextPage());
    assertFalse(installers.hasPreviousPage());

    List<? extends Installer> items = installers.getItems();
    items.sort(Comparator.comparing(o -> InstallerFqn.of(o).toKey()));

    assertEquals(items.size(), 2);
    assertInstaller(items.get(0), installer1);
    assertInstaller(items.get(1), installer2);
  }

  @Test
  public void shouldReturnEmptyPageIfNoInstallersFound() throws Exception {
    registry.remove(installerKey);

    Page<? extends Installer> installers = registry.getInstallers(Integer.MAX_VALUE, 0);

    assertEquals(installers.getTotalItemsCount(), 0);
    assertEquals(installers.getItemsCount(), 0);
    assertEquals(installers.getSize(), Integer.MAX_VALUE);
    assertFalse(installers.hasNextPage());
    assertFalse(installers.hasPreviousPage());
  }

  @Test
  public void shouldReturnOrderedInstallers() throws Exception {
    InstallerImpl installer1 = TestInstallerFactory.createInstaller("id_1", "1.0.1");
    InstallerImpl installer2 = TestInstallerFactory.createInstaller("id_2", "1.0.2");

    installer.setDependencies(Collections.emptyList());
    installer1.setDependencies(Collections.singletonList("id_0:1.0.0"));
    installer2.setDependencies(Collections.singletonList("id_1:1.0.1"));

    registry.update(installer);
    registry.add(installer1);
    registry.add(installer2);

    List<Installer> orderedInstallers =
        registry.getOrderedInstallers(ImmutableList.of("id_2:1.0.2"));

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
