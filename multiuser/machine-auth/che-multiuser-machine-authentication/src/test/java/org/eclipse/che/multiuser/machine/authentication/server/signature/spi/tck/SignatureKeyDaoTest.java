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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashSet;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.Page;
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

  private static final int COUNT_KEY_PAIRS = 5;

  @Inject private TckRepository<SignatureKeyPairImpl> signatureKeyRepo;
  @Inject private SignatureKeyDao dao;

  private SignatureKeyPairImpl[] storedKeyPairs;
  private KeyPairGenerator kpg;

  @AfterMethod
  public void removeEntities() throws TckRepositoryException {
    signatureKeyRepo.removeAll();
  }

  @BeforeMethod
  public void createEntities() throws Exception {
    storedKeyPairs = new SignatureKeyPairImpl[COUNT_KEY_PAIRS];
    kpg = KeyPairGenerator.getInstance(ALGORITHM);
    kpg.initialize(KEY_SIZE);
    for (int i = 0; i < COUNT_KEY_PAIRS; i++) {
      storedKeyPairs[i] = newKeyPair("id_" + i);
    }
    signatureKeyRepo.createAll(
        Stream.of(storedKeyPairs).map(SignatureKeyPairImpl::new).collect(toList()));
  }

  @Test
  public void testGetsAllKeys() throws Exception {
    final Page<SignatureKeyPairImpl> foundKeys = dao.getAll(COUNT_KEY_PAIRS, 0);

    assertEquals(new HashSet<>(foundKeys.getItems()), new HashSet<>(asList(storedKeyPairs)));
    assertEquals(foundKeys.getTotalItemsCount(), COUNT_KEY_PAIRS);
    assertEquals(foundKeys.getItems().size(), COUNT_KEY_PAIRS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testThrowsIllegalArgumentExceptionWhenMaxItemsIsNegative() throws Exception {
    dao.getAll(-1, 0);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testThrowsIllegalArgumentExceptionWhenSkipCountIsNegative() throws Exception {
    dao.getAll(1, -1);
  }

  @Test(expectedExceptions = ConflictException.class)
  public void throwsConflictExceptionWhenCreatingSignatureKeyPair() throws Exception {
    final SignatureKeyPairImpl signKeyPair = newKeyPair(storedKeyPairs[0].getId());

    dao.create(signKeyPair);
  }

  @Test
  public void testCreatesSignatureKeyPair() throws Exception {
    final SignatureKeyPairImpl signKeyPair = newKeyPair("id_" + 10);

    dao.create(signKeyPair);

    final Page<SignatureKeyPairImpl> keys = dao.getAll(COUNT_KEY_PAIRS + 1, 0);
    assertTrue(keys.getItems().contains(signKeyPair));
    assertEquals(keys.getTotalItemsCount(), COUNT_KEY_PAIRS + 1);
  }

  @Test
  public void testRemovesSignatureKeyPair() throws Exception {
    final SignatureKeyPairImpl toRemove = storedKeyPairs[0];

    dao.remove(toRemove.getId());

    final Page<SignatureKeyPairImpl> keys = dao.getAll(COUNT_KEY_PAIRS, 0);
    assertFalse(keys.getItems().contains(toRemove));
    assertEquals(keys.getTotalItemsCount(), COUNT_KEY_PAIRS - 1);
  }

  private SignatureKeyPairImpl newKeyPair(String id) {
    final KeyPair pair = kpg.generateKeyPair();
    return new SignatureKeyPairImpl(id, pair.getPublic(), pair.getPrivate());
  }
}
