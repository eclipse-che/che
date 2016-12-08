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

import org.eclipse.che.ide.websocket.ng.impl.WebSocketInitializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;

/**
 * Tests for {@link WebSocketJsonRpcInitializer}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketJsonRpcInitializerTest {
    @Mock
    private WebSocketInitializer        webSocketInitializer;
    @InjectMocks
    private WebSocketJsonRpcInitializer jsonRpcInitializer;

    @Test
    public void shouldRunInitializeOnInitialize() {
        jsonRpcInitializer.initialize("id", Collections.singletonMap("url", "url"));

        verify(webSocketInitializer).initialize("id", "url");
    }

    @Test
    public void shouldRunTerminateOnTerminate() {
        jsonRpcInitializer.terminate("id");

        verify(webSocketInitializer).terminate("id");
    }
}
