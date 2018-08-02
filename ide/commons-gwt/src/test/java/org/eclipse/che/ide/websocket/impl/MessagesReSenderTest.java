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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link MessagesReSender}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class MessagesReSenderTest {
  @Mock private WebSocketConnectionManager connectionManager;
  @Mock private UrlResolver urlResolver;
  @InjectMocks private MessagesReSender reSender;

  @Before
  public void setUp() throws Exception {
    when(urlResolver.getUrl("endpointId")).thenReturn("url");
    when(urlResolver.resolve("url")).thenReturn("endpointId");
  }

  @Test
  public void shouldResendAllMessages() {
    reSender.add("endpointId", "1");
    reSender.add("endpointId", "2");
    reSender.add("endpointId", "3");

    when(connectionManager.isConnectionOpen("url")).thenReturn(true);

    reSender.reSend("url");
    verify(connectionManager, times(3)).sendMessage(eq("url"), anyString());
  }

  @Test
  public void shouldStopSendingIfSessionIsClosed() {
    reSender.add("endpointId", "1");
    reSender.add("endpointId", "2");
    reSender.add("endpointId", "3");

    final int[] i = {0};
    when(connectionManager.isConnectionOpen("url")).thenAnswer(invocation -> (i[0]++ <= 1));
    reSender.reSend("url");
    verify(connectionManager, times(2)).sendMessage(eq("url"), anyString());

    when(connectionManager.isConnectionOpen("url")).thenReturn(true);
    reSender.reSend("url");

    verify(connectionManager, times(3)).sendMessage(eq("url"), anyString());
  }
}
