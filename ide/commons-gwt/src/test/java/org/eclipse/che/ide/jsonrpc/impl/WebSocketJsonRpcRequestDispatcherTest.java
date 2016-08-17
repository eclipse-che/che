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

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestReceiver;
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
 * Test for {@link WebSocketJsonRpcRequestDispatcher}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketJsonRpcRequestDispatcherTest {
    private static final String MESSAGE = "message";

    @Mock
    private Map<String, JsonRpcRequestReceiver> receivers;
    @Mock
    private DtoFactory                          dtoFactory;
    @InjectMocks
    private WebSocketJsonRpcRequestDispatcher dispatcher;

    @Mock
    private JsonRpcRequest         request;
    @Mock
    private JsonRpcRequestReceiver receiver;

    @Before
    public void before() {
        when(request.getMethod()).thenReturn("");
        when(dtoFactory.createDtoFromJson(anyString(), any())).thenReturn(request);
        when(receivers.entrySet()).thenReturn(emptySet());
    }

    @Test
    public void shouldRunDtoFactoryToCreateRequest() {
        dispatcher.dispatch(MESSAGE);
    }

    @Test
    public void shouldRunMatchingReceiver() {
        when(request.getMethod()).thenReturn("test-method");
        when(receivers.entrySet()).thenReturn(singletonMap("test-method", receiver).entrySet());
        dispatcher = new WebSocketJsonRpcRequestDispatcher(receivers, dtoFactory);

        dispatcher.dispatch(MESSAGE);

        verify(receiver).receive(request);
    }
}
