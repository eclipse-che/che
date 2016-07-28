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
package org.eclipse.che.api.ssh.server.spi.tck;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.server.spi.SshDao;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.test.tck.TckModuleFactory;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Mihail Kuznyetsov.
 */
@Guice(moduleFactory = TckModuleFactory.class)
@Test(suiteName = SshDaoTest.SUITE_NAME)
public class SshDaoTest {
    public static final String SUITE_NAME   = "SshDaoTck";
    private static final int COUNT_OF_PAIRS = 5;
    private static final int COUNT_OF_USERS = 3;

    SshPairImpl[] pairs;

    @Inject
    private SshDao sshDao;

    @Inject
    private TckRepository<SshPairImpl> sshRepository;

    @Inject
    private TckRepository<UserImpl> userRepository;

    @BeforeMethod
    public void setUp() throws TckRepositoryException {
        UserImpl[] users = new UserImpl[COUNT_OF_USERS];
        for (int i = 0; i < COUNT_OF_USERS; i++) {
            users[i] = new UserImpl("owner" + i,
                                    "owner" + i + "@eclipse.org",
                                    "owner" + i,
                                    "password",
                                    emptyList());
        }

        pairs = new SshPairImpl[COUNT_OF_PAIRS];

        for (int i = 0; i < COUNT_OF_PAIRS; i++) {
            // 2 pairs share same owner and service
            pairs[i] = new SshPairImpl("owner" + i/2,
                                       "service" + i/2,
                                       "name" + i,
                                       NameGenerator.generate("publicKey-", 20),
                                       NameGenerator.generate("privateKey-", 20));
        }

        userRepository.createAll(Arrays.asList(users));
        sshRepository.createAll(Arrays.asList(pairs));
    }

    @AfterMethod
    public void cleanUp() throws TckRepositoryException {
        sshRepository.removeAll();
        userRepository.removeAll();
    }

    @Test
    public void shouldCreateSshKeyPair() throws Exception {
        SshPairImpl pair = new SshPairImpl("owner1", "service", "name", "publicKey", "privateKey");
        sshDao.create(pair);

        assertEquals(sshDao.get("owner1", "service", "name"), pair);
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenSshPairWithSuchOwnerAndServiceAndNameAlreadyExists() throws Exception {
        sshDao.create(pairs[0]);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnCreateIfSshPairIsNull() throws Exception {
        sshDao.create(null);
    }
    @Test
    public void shouldGetSshPairByNameOwnerAndService() throws Exception{
        SshPairImpl sshPair = pairs[0];

        sshDao.get(sshPair.getOwner(), sshPair.getService(), sshPair.getName());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionIfPairWithSuchNameOwnerAndServiceDoesNotExist() throws Exception {
        SshPairImpl sshPair = pairs[0];

        sshDao.get(sshPair.getService(), sshPair.getService(), sshPair.getName());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnGetSshPairWhenOwnerIsNull() throws Exception{
        sshDao.get(null, "service", "name");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnGetSshPairWhenServiceIsNull() throws Exception{
        sshDao.get("owner", null, "name");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnGetSshPairWhenNameIsNull() throws Exception{
        sshDao.get("owner", "service", null);
    }

    @Test
    public void shouldGetSshPairListByNameAndService() throws Exception{
        SshPairImpl sshPair1 = pairs[0];
        SshPairImpl sshPair2 = pairs[1];
        assertEquals(sshPair1.getOwner(), sshPair2.getOwner(), "Owner must be the same");
        assertEquals(sshPair1.getService(), sshPair2.getService(), "Service must be the same");

        final List<SshPairImpl> found = sshDao.get(sshPair1.getOwner(), sshPair1.getService());
        assertEquals(new HashSet<>(found), new HashSet<>(asList(sshPair1, sshPair2)));
    }

    @Test
    public void shouldReturnEmptyListWhenThereAreNoPairsWithGivenOwnerAndService() throws Exception {
        assertTrue(sshDao.get("non-existing-owner", "non-existing-service").isEmpty());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnGetSshPairsListWhenOwnerIsNull() throws Exception{
        sshDao.get(null, "service");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnGetSshPairsListWhenServiceIsNull() throws Exception{
        sshDao.get("owner", null);
    }

    @Test
    public void shouldRemoveSshKeyPair() throws Exception {
        sshDao.remove(pairs[4].getOwner(), pairs[4].getService(), pairs[4].getName());

        try {
            sshDao.get(pairs[4].getOwner(), pairs[4].getService(), pairs[4].getName());
            fail("Object is still present in database");
        } catch (NotFoundException e) {
        }

    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenRemovingNonExistingPair() throws Exception {
        sshDao.remove(pairs[4].getService(), pairs[4].getService(), pairs[4].getService());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnRemoveWhenOwnerIsNull() throws Exception {
        sshDao.remove(null, "service", "name");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnRemoveWhenServiceIsNull() throws Exception {
        sshDao.remove("owner", null, "name");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeOnRemoveWhenNameIsNull() throws Exception {
        sshDao.remove("owner", "service", null);
    }
}
