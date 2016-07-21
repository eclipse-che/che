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

import org.eclipse.che.api.core.jsonrpc.JsonRpcResponseReceiver;
import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Tests for {@link WebSocketJsonRpcResponseDispatcher}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class WebSocketJsonRpcResponseDispatcherTest {
    private static final Integer REQUEST_ID  = 0;
    private static final int     ENDPOINT_ID = 1;
    private static final String  RESULT      = "result";

    @Mock
    private JsonRpcRequestRegistry requestRegistry;
    @Spy
    private Map<String, JsonRpcResponseReceiver> receivers = new HashMap<>();
    @InjectMocks
    private WebSocketJsonRpcResponseDispatcher dispatcher;

    @Mock
    private JsonRpcResponseReceiver receiver;

    @BeforeMethod
    public void before() {
        receivers.clear();
    }

    @Test
    public void shouldRunMatchingReceiver() {
        receivers.put("test-method", receiver);
        dispatcher = new WebSocketJsonRpcResponseDispatcher(requestRegistry, receivers);
        when(requestRegistry.extractFor(eq(0))).thenReturn("test-method");

        dispatcher.dispatch(getMessage(0), ENDPOINT_ID);

        ArgumentCaptor<JsonRpcResponse> responseCaptor = ArgumentCaptor.forClass(JsonRpcResponse.class);

        verify(receiver).receive(responseCaptor.capture(), eq(ENDPOINT_ID));

        final JsonRpcResponse response = responseCaptor.getValue();

        assertEquals(response.getResult(), RESULT);
        assertEquals(response.getId(), REQUEST_ID);
    }

    @Test
    public void shouldRunMatchingReceiverWithRegExpedName() {
        receivers.put("test-met.*", receiver);
        dispatcher = new WebSocketJsonRpcResponseDispatcher(requestRegistry, receivers);

        when(requestRegistry.extractFor(eq(0))).thenReturn("test-method");
        when(requestRegistry.extractFor(eq(1))).thenReturn("test-metho");
        when(requestRegistry.extractFor(eq(2))).thenReturn("test-meth");

        final List<Integer> ids = Arrays.asList(0, 1, 2);

        for (int i : ids) {
            dispatcher.dispatch(getMessage(i), ENDPOINT_ID);
        }

        ArgumentCaptor<JsonRpcResponse> responseCaptor = ArgumentCaptor.forClass(JsonRpcResponse.class);

        verify(receiver, times(3)).receive(responseCaptor.capture(), eq(ENDPOINT_ID));

        responseCaptor.getAllValues().forEach(response -> {
            assertTrue(ids.contains(response.getId()));
            assertEquals(response.getResult(), RESULT);
        });
    }

    @Test
    public void shouldRunMatchingSeveralReceiverWithRegExpedName() {
        receivers.put("test-met.*", receiver);
        receivers.put("test-me.*", receiver);
        receivers.put("test-m.*", receiver);
        dispatcher = new WebSocketJsonRpcResponseDispatcher(requestRegistry, receivers);
        when(requestRegistry.extractFor(eq(0))).thenReturn("test-method");

        dispatcher.dispatch(getMessage(0), ENDPOINT_ID);

        verify(receiver, times(receivers.size())).receive(any(JsonRpcResponse.class), eq(ENDPOINT_ID));
    }

    private String getMessage(Integer id) {
        return "{" +
               "\"id\":\"" + id + "\"," +
               "\"jsonrpc\":\"" + "2.0" + "\"," +
               "\"result\":\"" + RESULT + "\"" +
               "}";
    }
}
