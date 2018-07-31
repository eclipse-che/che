/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.account.spi.tck;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

import javax.inject.Inject;
import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link AccountDao} contract.
 *
 * @author Sergii Leschenko
 */
@Listeners(TckListener.class)
@Test(suiteName = AccountDaoTest.SUITE_NAME)
public class AccountDaoTest {
  public static final String SUITE_NAME = "AccountDaoTck";

  private AccountImpl[] accounts;

  @Inject private AccountDao accountDao;

  @Inject private TckRepository<AccountImpl> accountRepo;

  @BeforeMethod
  private void setUp() throws TckRepositoryException {
    accounts = new AccountImpl[2];

    accounts[0] = new AccountImpl(NameGenerator.generate("account", 10), "test1", "test");
    accounts[1] = new AccountImpl(NameGenerator.generate("account", 10), "test2", "test");

    accountRepo.createAll(asList(accounts));
  }

  @AfterMethod
  private void cleanup() throws TckRepositoryException {
    accountRepo.removeAll();
  }

  @Test(dependsOnMethods = "shouldGetAccountById")
  public void shouldCreateAccount() throws Exception {
    AccountImpl toCreate = new AccountImpl("account123", "test123", "test");

    accountDao.create(toCreate);

    assertEquals(toCreate, accountDao.getById(toCreate.getId()));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnCreatingNullAccount() throws Exception {
    accountDao.create(null);
  }

  @Test(dependsOnMethods = "shouldGetAccountById")
  public void shouldUpdateAccount() throws Exception {
    AccountImpl account = accounts[0];
    account.setName("newName");
    account.setType("newType");

    accountDao.update(account);

    assertEquals(account, accountDao.getById(account.getId()));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnUpdatingNullAccount() throws Exception {
    accountDao.update(null);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionWhenUpdatingAccountWithExistingName() throws Exception {
    AccountImpl account = accounts[0];
    account.setName(accounts[1].getName());

    accountDao.update(account);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void shouldThrowConflictExceptionWhenCreatingAccountWithExistingName() throws Exception {
    AccountImpl account =
        new AccountImpl(NameGenerator.generate("account", 5), accounts[0].getName(), "test");

    accountDao.create(account);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenUpdatingNonExistingAccount() throws Exception {
    AccountImpl account = accounts[0];
    account.setId("nonExisting");

    accountDao.update(account);
  }

  @Test
  public void shouldGetAccountById() throws Exception {
    final AccountImpl account = accounts[0];

    final AccountImpl found = accountDao.getById(account.getId());

    assertEquals(account, found);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnGettingNonExistingAccountById() throws Exception {
    accountDao.getById("non-existing-account");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnGettingAccountByNullId() throws Exception {
    accountDao.getById(null);
  }

  @Test
  public void shouldGetAccountByName() throws Exception {
    final AccountImpl account = accounts[0];

    final AccountImpl found = accountDao.getByName(account.getName());

    assertEquals(account, found);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void shouldThrowNotFoundExceptionOnGettingNonExistingAccountByName() throws Exception {
    accountDao.getByName("non-existing-account");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnGettingAccountByNullName() throws Exception {
    accountDao.getByName(null);
  }

  @Test(
    dependsOnMethods = "shouldThrowNotFoundExceptionOnGettingNonExistingAccountById",
    expectedExceptions = NotFoundException.class
  )
  public void shouldRemoveAccount() throws Exception {
    String toRemove = accounts[0].getId();

    accountDao.remove(toRemove);

    accountDao.getById(toRemove);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void shouldThrowNpeOnRemovingAccountByNullId() throws Exception {
    accountDao.remove(null);
  }
}
