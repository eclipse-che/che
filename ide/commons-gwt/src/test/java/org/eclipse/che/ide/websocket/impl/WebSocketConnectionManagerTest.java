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
package org.eclipse.che.ide.websocket.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link WebSocketConnectionManager}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketConnectionManagerTest {
  @Mock private WebSocketFactory webSocketFactory;
  @Mock private WebSocketActionManager webSocketActionManager;
  @InjectMocks private WebSocketConnectionManager connectionManager;

  @Mock private WebSocketConnection connection;

  @Before
  public void setUp() throws Exception {
    when(webSocketFactory.create("url")).thenReturn(connection);

    connectionManager.initializeConnection("url");
  }

  @Test
  public void shouldCreateConnectionOnInitialize() {
    verify(webSocketFactory).create("url");
  }

  @Test
  public void shouldOpenOnEstablishConnection() {
    connectionManager.establishConnection("url");

    verify(connection).open();
  }

  @Test
  public void shouldCloseOnCloseConnection() {
    connectionManager.closeConnection("url");

    verify(connection).close();
  }

  @Test
  public void shouldSendOnSendMessage() {
    connectionManager.sendMessage("url", "message");

    verify(connection).send("message");
  }

  @Test
  public void shouldReturnTrueWhenConnectionIsOpened() {
    when(connection.isOpen()).thenReturn(true);

    final boolean opened = connectionManager.isConnectionOpen("url");

    assertTrue(opened);
  }

  @Test
  public void shouldReturnFalseWhenConnectionIsClosed() {
    when(connection.isOpen()).thenReturn(false);

    final boolean opened = connectionManager.isConnectionOpen("url");

    assertFalse(opened);
  }
}
