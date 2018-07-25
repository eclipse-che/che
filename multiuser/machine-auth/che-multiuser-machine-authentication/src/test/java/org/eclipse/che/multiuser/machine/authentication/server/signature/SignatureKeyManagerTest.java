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
package org.eclipse.che.multiuser.machine.authentication.server.signature;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl.SignatureKeyImpl;
import org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl.SignatureKeyPairImpl;
import org.eclipse.che.multiuser.machine.authentication.server.signature.spi.SignatureKeyDao;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link SignatureKeyManager}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class SignatureKeyManagerTest {

  private static final int KEY_SIZE = 512;
  private static final String ALGORITHM = "RSA";

  @Mock SignatureKeyDao signatureKeyDao;

  private KeyPairGenerator kpg;
  private SignatureKeyManager signatureKeyManager;

  @BeforeMethod
  public void createEntities() throws Exception {
    kpg = KeyPairGenerator.getInstance(ALGORITHM);
    kpg.initialize(KEY_SIZE);
    signatureKeyManager = new SignatureKeyManager(KEY_SIZE, ALGORITHM, signatureKeyDao);
  }

  @Test
  public void testLoadSignatureKeys() throws Exception {
    final SignatureKeyPairImpl kp = newKeyPair("id_" + 1);
    when(signatureKeyDao.getAll(anyInt(), anyLong()))
        .thenReturn(new Page<>(singleton(kp), 0, 1, 1));

    signatureKeyManager.loadKeyPair();

    final KeyPair cachedPair = signatureKeyManager.getKeyPair();
    assertNotNull(cachedPair);
    assertKeys(cachedPair.getPublic(), kp.getPublicKey());
    assertKeys(cachedPair.getPrivate(), kp.getPrivateKey());
  }

  @Test
  public void testTriesToLoadKeysOnGettingKeyPairAndNoCachedKeyPair() throws Exception {
    when(signatureKeyDao.getAll(anyInt(), anyLong()))
        .thenThrow(new ServerException("unexpected end of stack"));

    signatureKeyManager.getKeyPair();

    verify(signatureKeyDao).getAll(anyInt(), anyLong());
  }

  @Test
  public void testGeneratesNewKeyPairWhenNoExistingKeyPairFound() throws Exception {
    doReturn(new Page<>(emptyList(), 0, 1, 0)).when(signatureKeyDao).getAll(anyInt(), anyLong());
    when(signatureKeyDao.create(any(SignatureKeyPairImpl.class)))
        .thenAnswer((Answer<SignatureKeyPairImpl>) invoke -> invoke.getArgument(0));

    final KeyPair cachedPair = signatureKeyManager.getKeyPair();

    verify(signatureKeyDao).getAll(anyInt(), anyLong());
    verify(signatureKeyDao).create(any(SignatureKeyPairImpl.class));
    assertNotNull(cachedPair);
  }

  @Test
  public void testReturnNullKeyPairWhenFailedToLoadAndGenerateKeys() throws Exception {
    doReturn(new Page<>(emptyList(), 0, 1, 0)).when(signatureKeyDao).getAll(anyInt(), anyLong());
    when(signatureKeyDao.create(any(SignatureKeyPairImpl.class)))
        .thenThrow(new ServerException("unexpected end of stack"));

    final KeyPair cachedPair = signatureKeyManager.getKeyPair();

    verify(signatureKeyDao).getAll(anyInt(), anyLong());
    verify(signatureKeyDao).create(any(SignatureKeyPairImpl.class));
    assertNull(cachedPair);
  }

  @Test
  public void testReturnNullKeyPairWhenAlgorithmIsNotSupported() throws Exception {
    final SignatureKeyImpl publicKey = new SignatureKeyImpl(new byte[] {}, "ECDH", "PKCS#15");
    final SignatureKeyImpl privateKey = new SignatureKeyImpl(new byte[] {}, "ECDH", "PKCS#3");
    final SignatureKeyPairImpl kp = new SignatureKeyPairImpl("id_" + 1, publicKey, privateKey);
    doReturn(new Page<>(singleton(kp), 0, 1, 1)).when(signatureKeyDao).getAll(anyInt(), anyLong());

    final KeyPair cachedPair = signatureKeyManager.getKeyPair();

    verify(signatureKeyDao).getAll(anyInt(), anyLong());
    assertNull(cachedPair);
  }

  private SignatureKeyPairImpl newKeyPair(String id) {
    final KeyPair pair = kpg.generateKeyPair();
    return new SignatureKeyPairImpl(id, pair.getPublic(), pair.getPrivate());
  }

  private static void assertKeys(Key key, SignatureKey signatureKey) {
    assertEquals(key.getAlgorithm(), signatureKey.getAlgorithm());
    assertEquals(key.getEncoded(), signatureKey.getEncoded());
    assertEquals(key.getFormat(), signatureKey.getFormat());
  }
}
