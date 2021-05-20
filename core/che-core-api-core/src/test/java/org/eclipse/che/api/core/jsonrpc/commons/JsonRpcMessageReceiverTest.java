/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.core.jsonrpc.commons;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.websocket.impl.WebsocketIdService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** Tests for {@link JsonRpcMessageReceiver} */
@Listeners(MockitoTestNGListener.class)
public class JsonRpcMessageReceiverTest {

  static final String MESSAGE = "message";
  static final String ENDPOINT_ID = "client-id" + WebsocketIdService.SEPARATOR + "endpoint-id";

  @Mock RequestDispatcher requestDispatcher;
  @Mock ResponseDispatcher responseDispatcher;
  @Mock JsonRpcErrorTransmitter errorTransmitter;
  @Mock JsonRpcQualifier jsonRpcQualifier;
  @Mock JsonRpcUnmarshaller jsonRpcUnmarshaller;
  @Mock RequestProcessor requestProcessor;
  @InjectMocks JsonRpcMessageReceiver jsonRpcMessageReceiver;

  @Test
  public void shouldValidateMessage() throws Exception {
    jsonRpcMessageReceiver.receive(ENDPOINT_ID, MESSAGE);

    verify(jsonRpcQualifier).isValidJson(MESSAGE);
  }

  @Test
  public void shouldTransmitErrorWhenValidationFailed() throws Exception {
    when(jsonRpcQualifier.isValidJson(MESSAGE)).thenReturn(false);

    jsonRpcMessageReceiver.receive(ENDPOINT_ID, MESSAGE);

    verify(errorTransmitter).transmit(eq(ENDPOINT_ID), any(JsonRpcException.class));
  }

  @Test
  public void shouldNotTransmitErrorWhenValidationSucceeded() throws Exception {
    when(jsonRpcQualifier.isValidJson(MESSAGE)).thenReturn(true);

    jsonRpcMessageReceiver.receive(ENDPOINT_ID, MESSAGE);

    verify(errorTransmitter, never()).transmit(eq(ENDPOINT_ID), any(JsonRpcException.class));
  }

  @Test
  public void shouldUnmarshalArray() throws Exception {
    jsonRpcMessageReceiver.receive(ENDPOINT_ID, MESSAGE);

    verify(jsonRpcUnmarshaller).unmarshalArray(MESSAGE);
  }

  @Test
  public void shouldDispatchResponseIfResponseReceived() throws Exception {
    when(jsonRpcQualifier.isJsonRpcResponse(MESSAGE)).thenReturn(true);
    when(jsonRpcQualifier.isJsonRpcRequest(MESSAGE)).thenReturn(false);
    when(jsonRpcUnmarshaller.unmarshalArray(any())).thenReturn(singletonList(MESSAGE));
    JsonRpcResponse jsonRpcResponse = Mockito.mock(JsonRpcResponse.class);
    when(jsonRpcUnmarshaller.unmarshalResponse(any())).thenReturn(jsonRpcResponse);

    jsonRpcMessageReceiver.receive(ENDPOINT_ID, MESSAGE);

    verify(responseDispatcher).dispatch(eq(ENDPOINT_ID), any(JsonRpcResponse.class));
  }

  @Test
  public void shouldDispatchRequestIfRequestReceived() throws Exception {
    when(jsonRpcQualifier.isJsonRpcRequest(MESSAGE)).thenReturn(true);
    when(jsonRpcUnmarshaller.unmarshalArray(any())).thenReturn(singletonList(MESSAGE));

    jsonRpcMessageReceiver.receive(ENDPOINT_ID, MESSAGE);

    verify(requestProcessor).process(any(), any());
  }
}
