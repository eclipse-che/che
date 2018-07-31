/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.jsonrpc.commons;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests for {@link RequestDispatcher} */
@Listeners(MockitoTestNGListener.class)
public class RequestDispatcherTest {
  static final String ENDPOINT_ID = "endpoint-id";
  static final String REQUEST_ID = "request-id";
  static final String REQUEST_METHOD = "request-method";

  @Mock RequestHandlerManager requestHandlerManager;
  @InjectMocks RequestDispatcher requestDispatcher;

  @Mock JsonRpcRequest request;
  @Mock JsonRpcParams params;

  @BeforeMethod
  public void setUp() throws Exception {
    when(request.getId()).thenReturn(REQUEST_ID);
    when(request.getMethod()).thenReturn(REQUEST_METHOD);
    when(request.getParams()).thenReturn(params);
    when(requestHandlerManager.isRegistered(REQUEST_METHOD)).thenReturn(true);
  }

  @Test
  public void shouldHandleRequest() throws Exception {
    when(request.hasId()).thenReturn(true);

    requestDispatcher.dispatch(ENDPOINT_ID, request);

    verify(requestHandlerManager).handle(ENDPOINT_ID, REQUEST_ID, REQUEST_METHOD, params);
  }

  @Test
  public void shouldHandleNotification() throws Exception {
    when(request.hasId()).thenReturn(false);

    requestDispatcher.dispatch(ENDPOINT_ID, request);

    verify(requestHandlerManager).handle(ENDPOINT_ID, REQUEST_METHOD, params);
  }

  @Test(expectedExceptions = JsonRpcException.class)
  public void shouldThrowExceptionOnNotRegisteredRequestHandler() throws Exception {
    when(requestHandlerManager.isRegistered(REQUEST_METHOD)).thenReturn(false);

    requestDispatcher.dispatch(ENDPOINT_ID, request);
  }

  @Test(expectedExceptions = JsonRpcException.class)
  public void shouldThrowExceptionOnNotRegisteredNotificationHandler() throws Exception {
    when(requestHandlerManager.isRegistered(REQUEST_METHOD)).thenReturn(false);

    requestDispatcher.dispatch(ENDPOINT_ID, request);
  }
}
