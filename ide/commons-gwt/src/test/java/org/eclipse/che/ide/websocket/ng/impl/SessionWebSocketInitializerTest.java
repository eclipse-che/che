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
package org.eclipse.che.ide.websocket.ng.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link SessionWebSocketInitializer}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionWebSocketInitializerTest {
    @Mock
    private WebSocketConnection          connection;
    @Mock
    private WebSocketConnectionSustainer sustainer;
    @InjectMocks
    private SessionWebSocketInitializer  initializer;

    @Before
    public void before(){
        when(connection.initialize(any())).thenReturn(connection);
    }

    @Test
    public void shouldInitializeControllerOnInitialize() {
        initializer.initialize(singletonMap("url", "url-value"));

        verify(connection).initialize((eq("url-value")));
    }

    @Test
    public void shouldOpenConnectionOnInitialize() {
        initializer.initialize(emptyMap());

        verify(connection).open(WebSocketConnection.IMMEDIATELY);
    }

    @Test
    public void shouldEnableSustainerOnInitialize() {
        initializer.initialize(emptyMap());

        verify(sustainer).enable();
    }

    @Test
    public void shouldCloseSessionOnTerminate() {
        initializer.terminate();

        verify(connection).close();
    }

    @Test
    public void shouldDisableSustainerOnInitialize() {
        initializer.terminate();

        verify(sustainer).disable();
    }

}
