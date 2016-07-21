/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.jsonrpc.impl;

import org.eclipse.che.api.core.jsonrpc.JsonRpcRequestReceiver;
import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link WebSocketJsonRpcRequestDispatcher}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class WebSocketJsonRpcRequestDispatcherTest {
    private static final String METHOD_NAME = "test-method";
    private static final int    ENDPOINT_ID = 0;
    private static final String PARAMS      = "params";

    @Spy
    private Map<String, JsonRpcRequestReceiver> receivers = new HashMap<>();
    @Mock
    private JsonRpcRequestReceiver receiver;

    private WebSocketJsonRpcRequestDispatcher dispatcher;

    @BeforeMethod
    public void before() {
        receivers.clear();
    }

    @Test
    public void shouldRunMatchingReceiver() {
        receivers.put(METHOD_NAME, receiver);
        dispatcher = new WebSocketJsonRpcRequestDispatcher(receivers);

        dispatcher.dispatch(getMessage(METHOD_NAME), ENDPOINT_ID);

        ArgumentCaptor<JsonRpcRequest> requestCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);

        verify(receiver).receive(requestCaptor.capture(), eq(ENDPOINT_ID));

        final JsonRpcRequest request = requestCaptor.getValue();

        assertEquals(request.getMethod(), "test-method");
        assertEquals(request.getParams(), PARAMS);
    }

    @Test
    public void shouldRunMatchingReceiverWithRegExpedName() {
        receivers.put("test-met.*", receiver);
        dispatcher = new WebSocketJsonRpcRequestDispatcher(receivers);

        final List<String> methods = asList("test-meth", "test-metho", METHOD_NAME);

        methods.forEach(m -> dispatcher.dispatch(getMessage(m), ENDPOINT_ID));

        ArgumentCaptor<JsonRpcRequest> requestCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);

        verify(receiver, times(methods.size())).receive(requestCaptor.capture(), eq(ENDPOINT_ID));

        requestCaptor.getAllValues().forEach(request -> {
            assertTrue(methods.contains(request.getMethod()));
            assertEquals(request.getParams(), PARAMS);
        });
    }

    @Test
    public void shouldRunMatchingSeveralReceiverWithRegExpedName() {
        receivers.put("test-met.*", receiver);
        receivers.put("test-me.*", receiver);
        receivers.put("test-m.*", receiver);
        dispatcher = new WebSocketJsonRpcRequestDispatcher(receivers);

        dispatcher.dispatch(getMessage("test-method"), ENDPOINT_ID);

        verify(receiver, times(receivers.size())).receive(any(JsonRpcRequest.class), eq(ENDPOINT_ID));
    }

    private String getMessage(String method) {
        return "{" +
               "\"id\":\"" + "0" + "\"," +
               "\"jsonrpc\":\"" + "2.0" + "\"," +
               "\"method\":\"" + method + "\"," +
               "\"params\":\"" + PARAMS + "\"" +
               "}";
    }
}
