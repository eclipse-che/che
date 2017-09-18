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
package org.eclipse.che.ide.websocket.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.security.oauth.SecurityTokenProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for {@link WebSocketInitializer}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketInitializerTest {
  @Mock private WebSocketConnectionManager connectionManager;
  @Mock private WebSocketPropertyManager propertyManager;
  @Mock private UrlResolver urlResolver;
  @Mock private WebSocketActionManager webSocketActionManager;
  @Mock private SecurityTokenProvider securityTokenProvider;
  @Mock private Promise promise;
  @Captor private ArgumentCaptor<Operation<String>> operation;
  @InjectMocks private WebSocketInitializer initializer;

  @Before
  public void setUp() throws Exception {
    when(securityTokenProvider.getSecurityToken()).thenReturn(promise);
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void shouldSetUrlMappingOnInitialize() throws OperationException {

    initializer.initialize("id", "http://test.com");
    verify(promise).then(operation.capture());
    operation.getValue().apply("token");
    verify(securityTokenProvider).getSecurityToken();
    verify(urlResolver).setMapping("id", "http://test.com?token=token");
  }

  @Test
  public void shouldRunConnectionManagerInitializeConnectionOnInitialize()
      throws OperationException {
    initializer.initialize("id", "http://test.com");
    verify(promise).then(operation.capture());
    operation.getValue().apply("token");
    verify(securityTokenProvider).getSecurityToken();
    verify(connectionManager).initializeConnection("http://test.com?token=token");
  }

  @Test
  public void shouldRunPropertyManagerInitializeConnectionOnInitialize() throws OperationException {
    initializer.initialize("id", "url");
    verify(promise).then(operation.capture());
    operation.getValue().apply("token");
    verify(propertyManager).initializeConnection("url?token=token");
  }

  @Test
  public void shouldRunEstablishConnectionOnInitialize() throws OperationException {
    initializer.initialize("id", "url");
    verify(promise).then(operation.capture());
    operation.getValue().apply("token");
    verify(connectionManager).establishConnection("url?token=token");
  }

  @Test
  public void shouldGetUrlOnTerminate() {
    when(urlResolver.removeMapping("id")).thenReturn("url");

    initializer.terminate("id");

    verify(urlResolver).removeMapping("id");
  }

  @Test
  public void shouldDisableSustainerOnTerminate() {
    when(urlResolver.removeMapping("id")).thenReturn("url");

    initializer.terminate("id");

    verify(propertyManager).disableSustainer("url");
  }

  @Test
  public void shouldCloseConnectionOnTerminate() {
    when(urlResolver.removeMapping("id")).thenReturn("url");

    initializer.terminate("id");

    verify(connectionManager).closeConnection("url");
  }
}
