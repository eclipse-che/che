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
package org.eclipse.che.multiuser.machine.authentication.server.signature.spi.tck;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.test.tck.TckListener;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl.SignatureKeyPairImpl;
import org.eclipse.che.multiuser.machine.authentication.server.signature.spi.SignatureKeyDao;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link SignatureKeyDao}.
 *
 * @author Anton Korneta
 */
@Listeners(TckListener.class)
@Test(suiteName = SignatureKeyDaoTest.SUITE_NAME)
public class SignatureKeyDaoTest {

  public static final String SUITE_NAME = "SignatureKeyDaoTck";
  public static final String ALGORITHM = "RSA";
  public static final int KEY_SIZE = 512;

  private static final int COUNT_KEY_PAIRS = 3;

  @Inject private TckRepository<SignatureKeyPairImpl> signatureKeyRepo;
  @Inject private TckRepository<AccountImpl> accountRepository;
  @Inject private TckRepository<WorkspaceImpl> workspaceRepository;
  @Inject private SignatureKeyDao dao;

  private SignatureKeyPairImpl[] storedKeyPairs;
  private KeyPairGenerator kpg;

  @AfterMethod
  public void removeEntities() throws TckRepositoryException {
    signatureKeyRepo.removeAll();
    workspaceRepository.removeAll();
    accountRepository.removeAll();
  }

  @BeforeMethod
  public void createEntities() throws Exception {

    AccountImpl account = new AccountImpl("account1", "accountName", "test");
    accountRepository.createAll(Collections.singletonList(account));
    workspaceRepository.createAll(
        Arrays.asList(
            new WorkspaceImpl(
                "ws0",
                account,
                new WorkspaceConfigImpl("ws-name0", "", "cfg0", null, null, null, null)),
            new WorkspaceImpl(
                "ws1",
                account,
                new WorkspaceConfigImpl("ws-name1", "", "cfg1", null, null, null, null)),
            new WorkspaceImpl(
                "ws2",
                account,
                new WorkspaceConfigImpl("ws-name2", "", "cfg2", null, null, null, null)),
            new WorkspaceImpl(
                "id_10",
                account,
                new WorkspaceConfigImpl("ws-name10", "", "cfg1", null, null, null, null))));

    storedKeyPairs = new SignatureKeyPairImpl[COUNT_KEY_PAIRS];
    kpg = KeyPairGenerator.getInstance(ALGORITHM);
    kpg.initialize(KEY_SIZE);
    for (int i = 0; i < COUNT_KEY_PAIRS; i++) {
      storedKeyPairs[i] = newKeyPair("ws" + i);
    }
    signatureKeyRepo.createAll(
        Stream.of(storedKeyPairs).map(SignatureKeyPairImpl::new).collect(toList()));
  }

  @Test
  public void testGetsAllKeys() throws Exception {
    List<SignatureKeyPairImpl> foundKeys = new ArrayList<>();
    for (SignatureKeyPairImpl expected : storedKeyPairs) {
      foundKeys.add(dao.get(expected.getWorkspaceId()));
    }
    assertEquals(new HashSet<>(foundKeys), new HashSet<>(asList(storedKeyPairs)));
    assertEquals(foundKeys.size(), COUNT_KEY_PAIRS);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void throwsConflictExceptionWhenCreatingSignatureKeyPair() throws Exception {
    final SignatureKeyPairImpl signKeyPair = newKeyPair(storedKeyPairs[0].getWorkspaceId());

    dao.create(signKeyPair);
  }

  @Test(
      expectedExceptions = ConflictException.class,
      expectedExceptionsMessageRegExp =
          "Unable to create signature key pair because referenced workspace with id '.*' doesn't exist")
  public void throwsConflictExceptionWhenCreatingKeyPairNotExistedWs() throws Exception {

    dao.create(newKeyPair("wrong_ws"));
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void throwsNoResultExceptionWhenSearchingWrongWorkspace() throws Exception {
    dao.get("unknown");
  }

  @Test
  public void testCreatesSignatureKeyPair() throws Exception {
    final SignatureKeyPairImpl signKeyPair = newKeyPair("id_" + 10);

    dao.create(signKeyPair);

    final SignatureKeyPairImpl kp = dao.get(signKeyPair.getWorkspaceId());
    assertNotNull(kp);
    assertEquals(kp, signKeyPair);
  }

  @Test(expectedExceptions = NotFoundException.class)
  public void testRemovesSignatureKeyPair() throws Exception {
    final SignatureKeyPairImpl toRemove = storedKeyPairs[0];

    dao.remove(toRemove.getWorkspaceId());

    dao.get(toRemove.getWorkspaceId());
  }

  private SignatureKeyPairImpl newKeyPair(String workspaceId) {
    final KeyPair pair = kpg.generateKeyPair();
    return new SignatureKeyPairImpl(workspaceId, pair.getPublic(), pair.getPrivate());
  }
}
