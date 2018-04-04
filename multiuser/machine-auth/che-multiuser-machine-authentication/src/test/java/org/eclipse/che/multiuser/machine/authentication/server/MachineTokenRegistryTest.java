/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
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

import java.util.HashMap;
import java.util.Map;
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
    userToToken.put("user1", registry.getOrCreateToken("user1", "workspace123"));
    userToToken.put("user2", registry.getOrCreateToken("user2", "workspace123"));
    userToToken.put("user3", registry.getOrCreateToken("user3", "workspace123"));
    final String tokenNotToRemove = registry.getOrCreateToken("user1", "workspace234");

    final Map<String, String> removedTokens = registry.removeTokens("workspace123");

    assertEquals(removedTokens, userToToken);

    assertEquals(registry.getOrCreateToken("user1", "workspace234"), tokenNotToRemove);
    assertFalse(
        registry.getOrCreateToken("user1", "workspace123").equals(userToToken.get("user1")));
    assertFalse(
        registry.getOrCreateToken("user2", "workspace123").equals(userToToken.get("user2")));
    assertFalse(
        registry.getOrCreateToken("user2", "workspace123").equals(userToToken.get("user3")));
  }
}
