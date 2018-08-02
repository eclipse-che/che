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
package org.eclipse.che.api.core.websocket.impl;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

import java.util.Optional;
import javax.websocket.Session;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link WebSocketSessionRegistry}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class WebSocketSessionRegistryTest {

  private WebSocketSessionRegistry registry;

  @Mock private Session session;

  @BeforeMethod
  public void setUp() throws Exception {
    registry = new WebSocketSessionRegistry();
  }

  @Test
  public void shouldAddSession() {
    assertTrue(registry.getSessions().isEmpty());

    registry.add("0", session);

    assertFalse(registry.getSessions().isEmpty());
  }

  @Test
  public void shouldAddCorrectSession() {
    registry.add("0", this.session);

    final Optional<Session> sessionOptional = registry.get("0");
    assertTrue(sessionOptional.isPresent());

    final Session session = sessionOptional.get();
    assertEquals(this.session, session);
  }

  @Test
  public void shouldRemoveSession() {
    registry.add("0", session);

    assertFalse(registry.getSessions().isEmpty());
    assertEquals(1, registry.getSessions().size());

    registry.remove("0");

    assertTrue(registry.getSessions().isEmpty());
  }

  @Test
  public void shouldGetAllSessions() {
    registry.add("0", session);

    assertFalse(registry.getSessions().isEmpty());
    assertEquals(1, registry.getSessions().size());

    registry.add("1", mock(Session.class));

    assertFalse(registry.getSessions().isEmpty());
    assertEquals(2, registry.getSessions().size());
  }
}
