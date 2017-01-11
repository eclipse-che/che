/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.jsonrpc;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.eclipse.che.ide.jsonrpc.JsonRpcEntityQualifier.JsonRpcEntityType.REQUEST;
import static org.eclipse.che.ide.jsonrpc.JsonRpcEntityQualifier.JsonRpcEntityType.RESPONSE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JsonRpcMessageReceiver}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonRpcMessageReceiverTest {
    static final String ENDPOINT_ID    = "endpointId";
    static final String LIST_MESSAGE   = "[]";
    static final String OBJECT_MESSAGE = "{}";
    static final int    ERROR_CODE     = 0;
    static final String ERROR_MESSAGE  = "error message";

    @Mock
    RequestDispatcher       requestDispatcher;
    @Mock
    ResponseDispatcher      responseDispatcher;
    @Mock
    JsonRpcEntityQualifier  qualifier;
    @Mock
    JsonRpcEntityValidator  validator;
    @Mock
    JsonRpcErrorTransmitter errorTransmitter;
    @Mock
    JsonRpcFactory          jsonRpcFactory;
    @InjectMocks
    JsonRpcMessageReceiver  receiver;

    @Mock
    JsonRpcList list;
    @Mock
    JsonRpcRequest request;
    @Mock
    JsonRpcResponse response;

    @Before
    public void setUp() throws Exception {
        when(qualifier.qualify(anyString())).thenReturn(JsonRpcEntityQualifier.JsonRpcEntityType.UNDEFINED);
        when(jsonRpcFactory.createList(anyString())).thenReturn(list);
        when(jsonRpcFactory.createRequest(anyString())).thenReturn(request);
        when(jsonRpcFactory.createResponse(anyString())).thenReturn(response);
        when(list.toStringifiedList()).thenReturn(Collections.singletonList(OBJECT_MESSAGE));
    }

    @Test
    public void shouldRunValidate() throws Exception {
        receiver.receive(ENDPOINT_ID, OBJECT_MESSAGE);

        verify(validator).validate(OBJECT_MESSAGE);
    }

    @Test
    public void shouldRunErrorTransmitterOnValidationFailure() throws Exception {
        JsonRpcException exception = new JsonRpcException(ERROR_CODE, ERROR_MESSAGE);
        doThrow(exception).when(validator).validate(anyString());

        receiver.receive(ENDPOINT_ID, OBJECT_MESSAGE);

        verify(errorTransmitter).transmit(ENDPOINT_ID, exception);
    }

    @Test
    public void shouldNotRunErrorTransmitterOnValidationSuccess() throws Exception {
        receiver.receive(ENDPOINT_ID, OBJECT_MESSAGE);

        verify(errorTransmitter).transmit(anyString(), any(JsonRpcException.class));
    }

    @Test
    public void shouldCreateListIfMessageIsList() throws Exception {
        receiver.receive(ENDPOINT_ID, LIST_MESSAGE);

        verify(jsonRpcFactory).createList(LIST_MESSAGE);
    }

    @Test
    public void shouldRunQualifyForObjectMessage() throws Exception {
        receiver.receive(ENDPOINT_ID, OBJECT_MESSAGE);

        verify(qualifier).qualify(OBJECT_MESSAGE);
    }

    @Test
    public void shouldRunQualifyForListMessage() throws Exception {
        receiver.receive(ENDPOINT_ID, LIST_MESSAGE);

        verify(qualifier).qualify(OBJECT_MESSAGE);
    }

    @Test
    public void shouldProcessRequest() throws Exception {
        when(qualifier.qualify(anyString())).thenReturn(REQUEST);

        receiver.receive(ENDPOINT_ID, OBJECT_MESSAGE);

        verify(jsonRpcFactory).createRequest(anyString());
        verify(requestDispatcher).dispatch(ENDPOINT_ID, request);
    }

    @Test
    public void shouldProcessResponse() throws Exception {
        when(qualifier.qualify(anyString())).thenReturn(RESPONSE);

        receiver.receive(ENDPOINT_ID, OBJECT_MESSAGE);

        verify(jsonRpcFactory).createResponse(anyString());
        verify(responseDispatcher).dispatch(ENDPOINT_ID, response);
    }

    @Test
    public void shouldProcessUndefined() throws Exception {
        receiver.receive(ENDPOINT_ID, LIST_MESSAGE);

        verify(errorTransmitter).transmit(eq(ENDPOINT_ID), any(JsonRpcException.class));
    }
}
