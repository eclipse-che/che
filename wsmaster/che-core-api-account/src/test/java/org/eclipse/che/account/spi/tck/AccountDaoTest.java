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
package org.eclipse.che.account.spi.tck;

import org.eclipse.che.account.spi.AccountDao;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

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

    @Inject
    private AccountDao accountDao;

    @Inject
    private TckRepository<AccountImpl> accountRepo;

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
    public void shouldThrowNotFoundExceptionOnGettingNonExistingaccountByName() throws Exception {
        accountDao.getByName("non-existing-account");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnGettingAccountByNullName() throws Exception {
        accountDao.getByName(null);
    }
}
