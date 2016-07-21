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
package org.eclipse.che.ide.jsonrpc.impl;

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcResponse;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcResponseReceiver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test for {@link WebSocketJsonRpcResponseDispatcher}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketJsonRpcResponseDispatcherTest {
    private static final String MESSAGE     = "message";
    private static final int    RESPONSE_ID = 0;

    @Mock
    private Map<String, JsonRpcResponseReceiver> receivers;
    @Mock
    private DtoFactory                           dtoFactory;
    @Mock
    private JsonRpcRequestRegistry               registry;
    @InjectMocks
    private WebSocketJsonRpcResponseDispatcher   dispatcher;

    @Mock
    private JsonRpcResponse         response;
    @Mock
    private JsonRpcResponseReceiver receiver;

    @Before
    public void before() {
        when(response.getId()).thenReturn(RESPONSE_ID);
        when(registry.extractFor(RESPONSE_ID)).thenReturn("");
        when(dtoFactory.createDtoFromJson(anyString(), any())).thenReturn(response);
        when(receivers.entrySet()).thenReturn(emptySet());
        dispatcher = new WebSocketJsonRpcResponseDispatcher(receivers, registry, dtoFactory);
    }

    @Test
    public void shouldRunDtoFactoryToCreateRequest() {
        dispatcher.dispatch(MESSAGE);
    }

    @Test
    public void shouldRunMatchingReceiver() {
        when(registry.extractFor(RESPONSE_ID)).thenReturn("test-method");
        when(receivers.entrySet()).thenReturn(singletonMap("test-method", receiver).entrySet());
        dispatcher = new WebSocketJsonRpcResponseDispatcher(receivers, registry, dtoFactory);


        dispatcher.dispatch(MESSAGE);

        verify(receiver).receive(response);
    }

    @Test
    public void shouldRunMatchingReceiverWithRegExpedName() {
        final Map<String, JsonRpcResponseReceiver> map = Collections.singletonMap("test-met*", receiver);
        when(receivers.entrySet()).thenReturn(map.entrySet());
        dispatcher = new WebSocketJsonRpcResponseDispatcher(receivers, registry, dtoFactory);


        when(registry.extractFor(RESPONSE_ID)).thenReturn("test-method");
        dispatcher.dispatch(MESSAGE);
        when(registry.extractFor(RESPONSE_ID)).thenReturn("test-metho");
        dispatcher.dispatch(MESSAGE);
        when(registry.extractFor(RESPONSE_ID)).thenReturn("test-meth");
        dispatcher.dispatch(MESSAGE);

        verify(receiver, times(3)).receive(response);
    }

    @Test
    public void shouldRunMatchingSeveralReceiverWithRegExpedName() {
        Map<String, JsonRpcResponseReceiver> map = new HashMap<>();
        map.put("test-met*", receiver);
        map.put("test-me*", receiver);
        map.put("test-m*", receiver);

        when(receivers.entrySet()).thenReturn(map.entrySet());
        dispatcher = new WebSocketJsonRpcResponseDispatcher(receivers, registry, dtoFactory);

        when(registry.extractFor(RESPONSE_ID)).thenReturn("test-method");
        dispatcher.dispatch(MESSAGE);

        verify(receiver, times(3)).receive(response);
    }
}
