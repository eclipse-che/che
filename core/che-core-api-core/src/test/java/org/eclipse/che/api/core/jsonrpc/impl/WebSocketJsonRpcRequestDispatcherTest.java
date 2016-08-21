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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

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

    @Mock
    private Map<String, JsonRpcRequestReceiver> receivers;
    @InjectMocks
    private WebSocketJsonRpcRequestDispatcher dispatcher;

    @Mock
    private JsonRpcRequestReceiver receiver;

    @BeforeMethod
    public void before() {
        when(receivers.entrySet()).thenReturn(singletonMap(METHOD_NAME, receiver).entrySet());
    }

    @Test
    public void shouldRunMatchingReceiver() {
        dispatcher.dispatch(getMessage(METHOD_NAME), ENDPOINT_ID);

        ArgumentCaptor<JsonRpcRequest> requestCaptor = ArgumentCaptor.forClass(JsonRpcRequest.class);

        verify(receiver).receive(requestCaptor.capture(), eq(ENDPOINT_ID));

        final JsonRpcRequest request = requestCaptor.getValue();

        assertEquals(request.getMethod(), "test-method");
        assertEquals(request.getParams(), PARAMS);
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
