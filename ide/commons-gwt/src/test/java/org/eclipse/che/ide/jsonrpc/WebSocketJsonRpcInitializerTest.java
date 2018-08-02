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
package org.eclipse.che.ide.jsonrpc;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.verify;

import org.eclipse.che.ide.websocket.impl.WebSocketInitializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link WebSocketJsonRpcInitializer}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketJsonRpcInitializerTest {
  @Mock private WebSocketInitializer webSocketInitializer;
  @InjectMocks private WebSocketJsonRpcInitializer jsonRpcInitializer;

  @Test
  public void shouldRunInitializeOnInitialize() {
    jsonRpcInitializer.initialize("id", singletonMap("url", "url"));

    verify(webSocketInitializer).initialize("id", "url", emptySet());
  }

  @Test
  public void shouldRunTerminateOnTerminate() {
    jsonRpcInitializer.terminate("id");

    verify(webSocketInitializer).terminate("id", emptySet());
  }
}
