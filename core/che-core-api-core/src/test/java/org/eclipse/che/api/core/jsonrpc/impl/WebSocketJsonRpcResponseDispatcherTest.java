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
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    private static final String  METHOD_NAME = "test-method";

    @Mock
    private JsonRpcRequestRegistry               requestRegistry;
    @Mock
    private Map<String, JsonRpcResponseReceiver> receivers;
    @InjectMocks
    private WebSocketJsonRpcResponseDispatcher   dispatcher;

    @Mock
    private JsonRpcResponseReceiver receiver;

    @BeforeMethod
    public void before() {
        when(receivers.entrySet()).thenReturn(singletonMap(METHOD_NAME, receiver).entrySet());
    }

    @Test
    public void shouldRunMatchingReceiver() {
        when(requestRegistry.extractFor(eq(0))).thenReturn(METHOD_NAME);

        dispatcher.dispatch(getMessage(0), ENDPOINT_ID);

        ArgumentCaptor<JsonRpcResponse> responseCaptor = ArgumentCaptor.forClass(JsonRpcResponse.class);

        verify(receiver).receive(responseCaptor.capture(), eq(ENDPOINT_ID));

        final JsonRpcResponse response = responseCaptor.getValue();

        assertEquals(response.getResult(), RESULT);
        assertEquals(response.getId(), REQUEST_ID);
    }

    private String getMessage(Integer id) {
        return "{" +
               "\"id\":\"" + id + "\"," +
               "\"jsonrpc\":\"" + "2.0" + "\"," +
               "\"result\":\"" + RESULT + "\"" +
               "}";
    }
}
