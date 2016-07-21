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

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcObject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WebSocketJsonRpcDispatcher}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class WebSocketJsonRpcDispatcherTest {
    private static final String MESSAGE             = "message";
    private static final String REGISTERED_TYPE     = "registered-type";
    private static final String NOT_REGISTERED_TYPE = "not-registered-type";
    private static final int    ENDPOINT_ID         = 0;

    @Mock
    private Map<String, JsonRpcDispatcher> dispatchers;
    @Mock
    private JsonRpcObjectValidator         validator;
    @InjectMocks
    private WebSocketJsonRpcDispatcher     dispatcher;

    @Mock
    private JsonRpcDispatcher jsonRpcDispatcher;

    private JsonRpcObject object;

    @BeforeMethod
    public void before() {
        when(dispatchers.entrySet()).thenReturn(singletonMap(REGISTERED_TYPE, jsonRpcDispatcher).entrySet());

        object = newDto(JsonRpcObject.class).withType(REGISTERED_TYPE).withMessage(MESSAGE);
    }

    @Test
    public void should() {
        dispatcher.receive(object.toString(), ENDPOINT_ID);
    }

    @Test
    public void shouldValidateOnReceive() {
        dispatcher.receive(object.toString(), ENDPOINT_ID);

        verify(validator).validate(eq(object));
    }

    @Test
    public void shouldDispatchIfMatchFound() {
        dispatcher.receive(object.toString(), ENDPOINT_ID);

        verify(jsonRpcDispatcher).dispatch(MESSAGE, ENDPOINT_ID);
    }

    @Test
    public void shouldNotDispatchIfMatchNotFound() {
        object.withType(NOT_REGISTERED_TYPE);

        dispatcher.receive(object.toString(), ENDPOINT_ID);

        verify(jsonRpcDispatcher, never()).dispatch(MESSAGE, ENDPOINT_ID);
    }
}
