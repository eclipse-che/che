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
package org.eclipse.che.multiuser.machine.authentication.server;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.NotFoundException;
import org.testng.annotations.Test;

/**
 * Tests for {@link MachineTokenRegistry}.
 *
 * @author Yevhenii Voevodin
 */
public class MachineTokenRegistryTest {

  @Test
  public void removeTokensShouldReturnUserToTokenMap() throws Exception {
    final MachineTokenRegistry registry = new MachineTokenRegistry();

    final Map<String, String> userToToken = new HashMap<>();
    userToToken.put("user1", registry.generateToken("user1", "workspace123"));
    userToToken.put("user2", registry.generateToken("user2", "workspace123"));
    userToToken.put("user3", registry.generateToken("user3", "workspace123"));
    registry.generateToken("user1", "workspace234");

    final Map<String, String> removedTokens = registry.removeTokens("workspace123");

    assertEquals(removedTokens, userToToken);
    assertTrue(exists(registry, "user1", "workspace234"));
    assertFalse(exists(registry, "user1", "workspace123"));
    assertFalse(exists(registry, "user2", "workspace123"));
    assertFalse(exists(registry, "user3", "workspace123"));
  }

  private static boolean exists(MachineTokenRegistry registry, String user, String workspace) {
    try {
      registry.getOrCreateToken(user, workspace);
      return true;
    } catch (NotFoundException e) {
      return false;
    }
  }

  @Test
  public void shouldreturnWorkspaceId() throws Exception {
    final MachineTokenRegistry registry = new MachineTokenRegistry();

    String token11 = registry.generateToken("user1", "workspace1");
    String token12 = registry.generateToken("user1", "workspace2");
    String token21 = registry.generateToken("user2", "workspace1");
    String token22 = registry.generateToken("user2", "workspace2");

    assertEquals(registry.getWorkspaceId(token11), "workspace1");
    assertEquals(registry.getWorkspaceId(token12), "workspace2");
    assertEquals(registry.getWorkspaceId(token21), "workspace1");
    assertEquals(registry.getWorkspaceId(token22), "workspace2");
  }
}
