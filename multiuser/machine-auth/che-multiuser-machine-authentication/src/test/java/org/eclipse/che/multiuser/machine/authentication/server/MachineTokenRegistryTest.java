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
package org.eclipse.che.multiuser.machine.authentication.server;

import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.USER_ID_CLAIM;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.USER_NAME_CLAIM;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.WORKSPACE_ID_CLAIM;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManager;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link MachineTokenRegistry}.
 *
 * @author Yevhenii Voevodin
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class MachineTokenRegistryTest {

  private MachineTokenRegistry tokenRegistry;

  private static final int KEY_SIZE = 1024;
  private static final String SIGNATURE_ALGORITHM = "RSA";
  private static final String USER_ID = "user13";
  private static final String USER_NAME = "testUser";
  private static final String WORKSPACE_ID = "workspace31";

  @Mock private SignatureKeyManager signatureKeyManager;
  @Mock private UserManager userManager;

  private KeyPair keyPair;

  @BeforeMethod
  private void setUp() throws Exception {
    tokenRegistry = new MachineTokenRegistry(signatureKeyManager, userManager);
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance(SIGNATURE_ALGORITHM);
    kpg.initialize(KEY_SIZE);
    keyPair = kpg.generateKeyPair();

    mockUser(USER_ID, USER_NAME);
    when(signatureKeyManager.getKeyPair(anyString())).thenReturn(keyPair);
  }

  @Test
  public void testCreatesNewTokenWhenNoPreviouslyCreatedFound() throws Exception {
    final String generatedToken = tokenRegistry.getOrCreateToken(USER_ID, WORKSPACE_ID);

    final Claims claims =
        Jwts.parser().setSigningKey(keyPair.getPublic()).parseClaimsJws(generatedToken).getBody();

    final SubjectImpl subject =
        new SubjectImpl(
            claims.get(USER_NAME_CLAIM, String.class),
            claims.get(USER_ID_CLAIM, String.class),
            null,
            false);
    assertEquals(subject.getUserId(), USER_ID);
    assertEquals(subject.getUserName(), USER_NAME);
    assertEquals(claims.get(WORKSPACE_ID_CLAIM, String.class), WORKSPACE_ID);
    verify(userManager).getById(USER_ID);
    verify(signatureKeyManager).getKeyPair(anyString());
    assertNotNull(generatedToken);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testThrowsIllegalStateExceptionWhenTryToGetTokenForNonExistingUser()
      throws Exception {
    when(userManager.getById(anyString())).thenThrow(new NotFoundException("User not found"));

    tokenRegistry.getOrCreateToken(USER_ID, WORKSPACE_ID);
  }

  @Test
  public void removeTokensShouldReturnUserToTokenMap() throws Exception {
    final Map<String, String> userToToken = new HashMap<>();
    final String user1 = "user1";
    final String user2 = "user2";
    final String workspace1 = "workspace123";
    final String workspace2 = "workspace234";
    mockUser(user1, user1);
    mockUser(user2, user2);
    userToToken.put(user1, tokenRegistry.getOrCreateToken(user1, workspace1));
    userToToken.put(user2, tokenRegistry.getOrCreateToken(user2, workspace1));
    final String tokenNotToRemove = tokenRegistry.getOrCreateToken(user1, workspace2);

    final Map<String, String> removedTokens = tokenRegistry.removeTokens(workspace1);

    assertEquals(removedTokens, userToToken);
    assertEquals(tokenRegistry.getOrCreateToken(user1, workspace2), tokenNotToRemove);
    assertNotEquals(tokenRegistry.getOrCreateToken(user1, workspace1), removedTokens.get(user1));
    assertNotEquals(tokenRegistry.getOrCreateToken(user2, workspace1), removedTokens.get(user2));
  }

  private void mockUser(String userId, String userName) throws Exception {
    final User userMock = mock(User.class);
    when(userMock.getId()).thenReturn(userId);
    when(userMock.getName()).thenReturn(userName);
    when(userManager.getById(userId)).thenReturn(userMock);
  }
}
