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
package org.eclipse.che.ide.jsonrpc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcError;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcErrorTransmitter;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcMarshaller;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcResponse;
import org.eclipse.che.api.core.websocket.commons.WebSocketMessageTransmitter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link JsonRpcErrorTransmitter} */
@RunWith(MockitoJUnitRunner.class)
public class JsonRpcErrorTransmitterTest {
  static final String ENDPOINT_ID = "endointId";
  static final String ERROR_MESSAGE = "message";
  static final String REQUEST_ID = "0";
  static final int ERROR_CODE = 0;
  static final String MARSHALED_RESPONSE = "marshaled response";

  @Mock WebSocketMessageTransmitter transmitter;
  @Mock JsonRpcMarshaller marshaller;
  @InjectMocks JsonRpcErrorTransmitter errorTransmitter;

  @Mock JsonRpcException jsonRpcException;
  @Mock JsonRpcError jsonRpcError;

  @Before
  public void setUp() {
    when(jsonRpcError.getCode()).thenReturn(ERROR_CODE);
    when(jsonRpcError.getMessage()).thenReturn(ERROR_MESSAGE);

    when(marshaller.marshall(any(JsonRpcResponse.class))).thenReturn(MARSHALED_RESPONSE);

    when(jsonRpcException.getCode()).thenReturn(ERROR_CODE);
    when(jsonRpcException.getId()).thenReturn(REQUEST_ID);
    when(jsonRpcException.getMessage()).thenReturn(ERROR_MESSAGE);
  }

  @Test
  public void shouldMarshalResponse() throws Exception {
    errorTransmitter.transmit(ENDPOINT_ID, jsonRpcException);

    verify(marshaller).marshall(any(JsonRpcResponse.class));
  }

  @Test
  public void shouldTransmitResponse() throws Exception {
    errorTransmitter.transmit(ENDPOINT_ID, jsonRpcException);

    verify(transmitter).transmit(eq(ENDPOINT_ID), eq(MARSHALED_RESPONSE));
  }
}
