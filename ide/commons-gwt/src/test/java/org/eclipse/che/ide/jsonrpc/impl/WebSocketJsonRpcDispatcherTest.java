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

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;
import org.eclipse.che.ide.dto.DtoFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WebSocketJsonRpcDispatcher}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketJsonRpcDispatcherTest {
    private static final String MESSAGE                  = "message";
    private static final String REGISTERED_TYPE          = "registered-type";
    private static final String RAW_JSON_RPC_OBJECT_SUBB = "raw-json-rpc-object-subb";

    @Mock
    private Map<String, JsonRpcDispatcher> dispatchers;
    @Mock
    private JsonRpcObjectValidator         validator;
    @Mock
    private DtoFactory                     dtoFactory;
    @InjectMocks
    private WebSocketJsonRpcDispatcher     dispatcher;

    @Mock
    private JsonRpcObject     object;
    @Mock
    private JsonRpcDispatcher jsonRpcDispatcher;

    @Before
    public void before() {
        when(dtoFactory.createDtoFromJson(anyString(), any())).thenReturn(object);
        when(object.getType()).thenReturn(REGISTERED_TYPE);
        when(object.getMessage()).thenReturn(MESSAGE);

        when(dispatchers.entrySet()).thenReturn(singletonMap(REGISTERED_TYPE, jsonRpcDispatcher).entrySet());
    }

    @Test
    public void should() {
        dispatcher.receive("");
    }

    @Test
    public void shouldCreateDtoOnReceive() {
        dispatcher.receive(RAW_JSON_RPC_OBJECT_SUBB);

        verify(dtoFactory).createDtoFromJson(eq(RAW_JSON_RPC_OBJECT_SUBB), eq(JsonRpcObject.class));
    }

    @Test
    public void shouldValidateOnReceive() {
        dispatcher.receive(RAW_JSON_RPC_OBJECT_SUBB);

        verify(validator).validate(eq(object));
    }

    @Test
    public void shouldDispatchIfMatchFound() {
        dispatcher.receive(RAW_JSON_RPC_OBJECT_SUBB);

        verify(jsonRpcDispatcher).dispatch(MESSAGE);
    }

    @Test
    public void shouldNotDispatchIfMatchNotFound() {
        when(object.getType()).thenReturn("not-"+REGISTERED_TYPE);

        dispatcher.receive(RAW_JSON_RPC_OBJECT_SUBB);

        verify(jsonRpcDispatcher, never()).dispatch(MESSAGE);
    }
}
