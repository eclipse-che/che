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
package org.eclipse.che.api.core.jsonrpc;

import org.eclipse.che.api.core.websocket.WebSocketMessageTransmitter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RequestDispatcher}
 */
@Listeners(MockitoTestNGListener.class)
public class RequestDispatcherTest {
    static final String REQUEST_ID           = "0";
    static final String METHOD_NAME          = "method";
    static final String ENDPOINT_ID          = "endpointId";
    static final String STRINGIFIED_RESPONSE = "";

    @Mock
    RequestHandlerRegistry      requestHandlerRegistry;
    @Mock
    JsonRpcFactory              jsonRpcFactory;
    @Mock
    WebSocketMessageTransmitter transmitter;
    @InjectMocks
    RequestDispatcher           requestDispatcher;

    @Mock
    JsonRpcRequest      request;
    @Mock
    JsonRpcResponse     response;
    @Mock
    JsonRpcParams       params;
    @Mock
    RequestHandler      requestHandler;
    @Mock
    NotificationHandler notificationHandler;
    @Mock
    JsonRpcResult       result;

    @BeforeMethod
    public void setUp() throws Exception {
        when(request.hasId()).thenReturn(true);
        when(request.getId()).thenReturn(REQUEST_ID);
        when(request.getMethod()).thenReturn(METHOD_NAME);
        when(request.getParams()).thenReturn(params);

        when(requestHandler.handle(ENDPOINT_ID, params)).thenReturn(result);

        when(jsonRpcFactory.createResponse(REQUEST_ID, result, null)).thenReturn(response);

        when(response.toString()).thenReturn(STRINGIFIED_RESPONSE);

        when(requestHandlerRegistry.getRequestHandler("method")).thenReturn(requestHandler);
        when(requestHandlerRegistry.getNotificationHandler("method")).thenReturn(notificationHandler);
    }

    @Test
    public void shouldGetRequestHandlerIfRequestHasId() throws Exception {
        when(request.hasId()).thenReturn(true);

        requestDispatcher.dispatch(ENDPOINT_ID, request);

        verify(requestHandlerRegistry).getRequestHandler(METHOD_NAME);
        verify(requestHandlerRegistry, never()).getNotificationHandler(METHOD_NAME);
    }

    @Test(expectedExceptions = JsonRpcException.class)
    public void shouldThrowExceptionIfRequestHandlerIsNotFound() throws Exception {
        when(request.hasId()).thenReturn(true);
        when(requestHandlerRegistry.getRequestHandler("method")).thenReturn(null);

        requestDispatcher.dispatch(ENDPOINT_ID, request);
    }

    @Test
    public void shouldHandleRequest() throws Exception {
        requestDispatcher.dispatch(ENDPOINT_ID, request);

        verify(requestHandler).handle(ENDPOINT_ID, params);
    }

    @Test
    public void shouldCreateResponseWhenRequestIsHandled() throws Exception {
        requestDispatcher.dispatch(ENDPOINT_ID, request);

        verify(jsonRpcFactory).createResponse(REQUEST_ID, result, null);
    }

    @Test
    public void shouldTransmitResponseWhenItIsCreated() throws Exception {
        requestDispatcher.dispatch(ENDPOINT_ID, request);

        verify(transmitter).transmit(ENDPOINT_ID, STRINGIFIED_RESPONSE);
    }

    @Test
    public void shouldGetNotificationHandlerIfRequestHasId() throws Exception {
        when(request.hasId()).thenReturn(false);

        requestDispatcher.dispatch(ENDPOINT_ID, request);

        verify(requestHandlerRegistry).getNotificationHandler(METHOD_NAME);
        verify(requestHandlerRegistry, never()).getRequestHandler(METHOD_NAME);
    }

    @Test
    public void shouldHandleNotification() throws Exception {
        when(request.hasId()).thenReturn(false);

        requestDispatcher.dispatch(ENDPOINT_ID, request);

        verify(notificationHandler).handle(ENDPOINT_ID, params);
    }

    @Test(expectedExceptions = JsonRpcException.class)
    public void shouldThrowExceptionIfNotificationHandlerIsNotFound() throws Exception {
        when(request.hasId()).thenReturn(false);
        when(requestHandlerRegistry.getNotificationHandler("method")).thenReturn(null);

        requestDispatcher.dispatch(ENDPOINT_ID, request);
    }
}
