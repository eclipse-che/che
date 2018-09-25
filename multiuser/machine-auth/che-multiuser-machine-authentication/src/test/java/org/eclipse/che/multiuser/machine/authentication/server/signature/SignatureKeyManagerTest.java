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
package org.eclipse.che.multiuser.machine.authentication.server.signature;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl.SignatureKeyImpl;
import org.eclipse.che.multiuser.machine.authentication.server.signature.model.impl.SignatureKeyPairImpl;
import org.eclipse.che.multiuser.machine.authentication.server.signature.spi.SignatureKeyDao;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
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
  @Mock EventService eventService;

  @Captor private ArgumentCaptor<EventSubscriber<WorkspaceStatusEvent>> captor;

  private KeyPairGenerator kpg;
  private SignatureKeyManager signatureKeyManager;

  @BeforeMethod
  public void createEntities() throws Exception {
    kpg = KeyPairGenerator.getInstance(ALGORITHM);
    kpg.initialize(KEY_SIZE);
    signatureKeyManager =
        new SignatureKeyManager(KEY_SIZE, ALGORITHM, eventService, signatureKeyDao);
  }

  @Test
  public void shouldRemoveKeyPairOnWorkspaceStop() throws Exception {
    final String wsId = "ws123";
    signatureKeyManager.subscribe();
    verify(eventService).subscribe(captor.capture());
    final EventSubscriber<WorkspaceStatusEvent> subscriber = captor.getValue();

    subscriber.onEvent(
        DtoFactory.newDto(WorkspaceStatusEvent.class)
            .withStatus(WorkspaceStatus.STOPPED)
            .withWorkspaceId(wsId));

    verify(signatureKeyDao, times(1)).remove(eq(wsId));
  }

  @Test(expectedExceptions = SignatureKeyManagerException.class)
  public void shouldThrowsExceptionWhenAlgorithmIsNotSupported() throws Exception {
    final SignatureKeyImpl publicKey = new SignatureKeyImpl(new byte[] {}, "ECDH", "PKCS#15");
    final SignatureKeyImpl privateKey = new SignatureKeyImpl(new byte[] {}, "ECDH", "PKCS#3");
    final SignatureKeyPairImpl kp = new SignatureKeyPairImpl("id_" + 1, publicKey, privateKey);
    doReturn(kp).when(signatureKeyDao).get(anyString());

    signatureKeyManager.getOrCreateKeyPair("ws1");

    verify(signatureKeyDao).get(anyString());
  }

  @Test
  public void shouldReturnSignatureKeys() throws Exception {
    String wsId = "WS_id_1";
    final SignatureKeyPairImpl kp = newKeyPair(wsId);
    when(signatureKeyDao.get(anyString())).thenReturn(kp);

    final KeyPair cachedPair = signatureKeyManager.getOrCreateKeyPair(wsId);

    assertNotNull(cachedPair);
    assertKeys(cachedPair.getPublic(), kp.getPublicKey());
    assertKeys(cachedPair.getPrivate(), kp.getPrivateKey());
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
