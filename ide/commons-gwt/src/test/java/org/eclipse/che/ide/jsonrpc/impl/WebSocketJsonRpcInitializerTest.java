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

import org.eclipse.che.ide.websocket.ng.impl.SessionWebSocketInitializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;


/**
 * Tests for {@link WebSocketJsonRpcInitializer}
 *
 * @author Dmitry Kuleshov
 */

@RunWith(MockitoJUnitRunner.class)
public class WebSocketJsonRpcInitializerTest {
    @Mock
    private SessionWebSocketInitializer webSocketInitializer;
    @InjectMocks
    private WebSocketJsonRpcInitializer jsonRpcInitializer;

    @Test
    public void shouldRunWebSocketInitializeWithCorrectProperties(){
        final Map<String, String> map = singletonMap("test-key", "test-value");

        jsonRpcInitializer.initialize(map);

        verify(webSocketInitializer).initialize(eq(map));
    }

    @Test
    public void shouldRunWebSocketTerminate(){
        jsonRpcInitializer.terminate();

        verify(webSocketInitializer).terminate();
    }
}
