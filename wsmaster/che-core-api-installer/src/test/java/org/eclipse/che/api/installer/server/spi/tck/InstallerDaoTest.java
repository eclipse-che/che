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
package org.eclipse.che.api.installer.server.spi.tck;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.installer.server.exception.InstallerAlreadyExistsException;
import org.eclipse.che.api.installer.server.exception.InstallerNotFoundException;
import org.eclipse.che.api.installer.server.impl.InstallerFqn;
import org.eclipse.che.api.installer.server.impl.TestInstallerFactory;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.installer.server.spi.InstallerDao;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link InstallerDao} contract.
 *
 * @author Anatolii Bazko
 */
@Listeners(TckListener.class)
@Test(suiteName = InstallerDaoTest.SUITE_NAME)
public class InstallerDaoTest {
  static final String SUITE_NAME = "InstallerDaoTck";

  private static final int INSTALLER_COUNT = 5;

  private InstallerImpl[] installers;

  @Inject private InstallerDao installerDao;
  @Inject private TckRepository<InstallerImpl> tckRepository;

  @BeforeMethod
  public void setUp() throws TckRepositoryException {
    installers = new InstallerImpl[INSTALLER_COUNT];

    for (int i = 0; i < installers.length; i++) {
      installers[i] = TestInstallerFactory.createInstaller("id_" + i, "1.0." + i);
    }

    tckRepository.createAll(asList(installers));
  }

  @AfterMethod
  public void cleanUp() throws TckRepositoryException {
    tckRepository.removeAll();
  }

  @Test
  public void shouldGetInstallerByFqn() throws Exception {
    final InstallerFqn fqn = new InstallerFqn("id_0", "1.0.0");

    assertEquals(new InstallerImpl(installers[0]), installerDao.getByFqn(fqn));
  }

  @Test(expectedExceptions = InstallerNotFoundException.class)
  public void shouldThrowNotFoundExceptionIfInstallerWithSuchFqnDoesNotExist() throws Exception {
    final InstallerFqn fqn = new InstallerFqn("non-existed", "non-existed");

    installerDao.getByFqn(fqn);
  }

  @Test(expectedExceptions = InstallerNotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenGettingInstallerByWrongVersion() throws Exception {
    final InstallerFqn fqn = new InstallerFqn("id_0", "non-existed");

    installerDao.getByFqn(fqn);
  }

  @Test(expectedExceptions = InstallerNotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenGettingInstallerByWrongId() throws Exception {
    final InstallerFqn fqn = new InstallerFqn("non-existed", "1.0.0");

    installerDao.getByFqn(fqn);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenGettingInstallerByNullFqn() throws Exception {
    installerDao.getByFqn(null);
  }

  @Test
  public void shouldGetTotalInstallerCount() throws Exception {
    assertEquals(installerDao.getTotalCount(), INSTALLER_COUNT);
  }

  @Test
  public void shouldReturnAllInstallersBySpecificId() throws Exception {
    List<String> result = installerDao.getVersions("id_0");
    assertEquals(result.size(), 1);
    assertEquals(result.get(0), "1.0.0");
  }

  @Test
  public void getAllShouldReturnAllInstallersWithinSingleResponse() throws Exception {
    List<InstallerImpl> result = installerDao.getAll(6, 0).getItems();
    assertEquals(result.size(), INSTALLER_COUNT);

    result.sort(Comparator.comparing(InstallerImpl::getId));
    for (int i = 0; i < result.size(); i++) {
      assertEquals(installers[i], result.get(i));
    }
  }

  @Test
  public void shouldReturnGetAllWithSkipCountAndMaxItems() throws Exception {
    List<InstallerImpl> installers = installerDao.getAll(3, 0).getItems();
    assertEquals(installers.size(), 3);

    installers = installerDao.getAll(3, 3).getItems();
    assertEquals(installers.size(), 2);
  }

  @Test
  public void shouldReturnEmptyListIfNoMoreInstallers() throws Exception {
    List<InstallerImpl> installers = installerDao.getAll(1, 6).getItems();
    assertTrue(installers.isEmpty());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getAllShouldThrowIllegalArgumentExceptionIfMaxItemsWrong() throws Exception {
    installerDao.getAll(-1, 5);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void getAllShouldThrowIllegalArgumentExceptionIfSkipCountWrong() throws Exception {
    installerDao.getAll(2, -1);
  }

  @Test
  public void shouldReturnCorrectTotalCountAlongWithRequestedInstallers() throws Exception {
    final Page<InstallerImpl> page = installerDao.getAll(2, 0);

    assertEquals(page.getItems().size(), 2);
    assertEquals(page.getTotalItemsCount(), 5);
  }

  @Test
  public void shouldCreateInstaller() throws Exception {
    InstallerImpl newInstaller = TestInstallerFactory.createInstaller("id_new", "1.0.0");

    installerDao.create(newInstaller);

    assertEquals(
        installerDao.getByFqn(new InstallerFqn("id_new", "1.0.0")),
        new InstallerImpl(newInstaller));
  }

  @Test(expectedExceptions = InstallerAlreadyExistsException.class)
  public void shouldThrowConflictExceptionWhenCreatingInstallerWithExistingFqn() throws Exception {
    InstallerImpl newInstaller =
        TestInstallerFactory.createInstaller(installers[0].getId(), installers[0].getVersion());

    installerDao.create(newInstaller);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrownNpeWhenTryingToCreateNullInstaller() throws Exception {
    installerDao.create(null);
  }

  @Test
  public void shouldUpdateInstaller() throws Exception {
    InstallerImpl updatedInstaller =
        TestInstallerFactory.createInstaller(installers[0].getId(), installers[0].getVersion());

    installerDao.update(updatedInstaller);

    assertEquals(installerDao.getByFqn(new InstallerFqn("id_0", "1.0.0")), updatedInstaller);
  }

  @Test(expectedExceptions = InstallerNotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenUpdatingNonExistingInstaller() throws Exception {
    InstallerImpl updatedInstaller = TestInstallerFactory.createInstaller("non-existed", "1.0.0");

    installerDao.update(updatedInstaller);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenUpdatingNullInstaller() throws Exception {
    installerDao.update(null);
  }

  @Test(expectedExceptions = InstallerNotFoundException.class)
  public void shouldRemoveInstaller() throws Exception {
    InstallerFqn fqn = new InstallerFqn("id_0", "1.0.0");

    installerDao.remove(fqn);
    installerDao.getByFqn(fqn);
  }

  @Test
  public void shouldNotThrowAnyExceptionWhenRemovingNonExistingInstaller() throws Exception {
    InstallerFqn fqn = new InstallerFqn("non-existed", "non-existed");

    installerDao.remove(fqn);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeWhenRemovingNull() throws Exception {
    installerDao.remove(null);
  }
}
