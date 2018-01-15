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
package org.eclipse.che.ide.websocket.impl;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link BasicWebSocketEndpoint}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class BasicWebSocketEndpointTest {
  @Mock private WebSocketConnectionSustainer sustainer;
  @Mock private MessagesReSender reSender;
  @Mock private WebSocketDispatcher dispatcher;
  @Mock private WebSocketActionManager actionManager;

  @InjectMocks private BasicWebSocketEndpoint endpoint;

  @Test
  public void shouldResetSustainerOnOpen() {
    endpoint.onOpen("url");

    verify(sustainer).reset("url");
  }

  @Test
  public void shouldReSendMessagesOnOpen() {
    endpoint.onOpen("url");

    verify(reSender).reSend("url");
  }

  @Test
  public void shouldOnOpenActionsOnOpen() {
    endpoint.onOpen("url");

    verify(actionManager).getOnOpenActions("url");
  }

  @Test
  public void shouldOnCloseActionsOnClose() {
    endpoint.onClose("url");

    verify(actionManager).getOnCloseActions("url");
  }

  @Test
  public void shouldSustainOnClose() {
    endpoint.onClose("url");

    verify(sustainer).sustain("url");
  }

  @Test
  public void shouldDispatchOnMessage() {
    endpoint.onMessage("url", "message");

    verify(dispatcher).dispatch("url", "message");
  }
}
