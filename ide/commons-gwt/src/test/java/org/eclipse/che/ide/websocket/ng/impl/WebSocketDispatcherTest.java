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

import org.eclipse.che.ide.websocket.ng.WebSocketMessageReceiver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WebSocketDispatcher}
 *
 * @author Dmitry Kuleshov
 */

@RunWith(MockitoJUnitRunner.class)
public class WebSocketDispatcherTest {
    @Mock
    private WebSocketMessageReceiver receiver;
    @Mock
    private UrlResolver              urlResolver;
    @InjectMocks
    private WebSocketDispatcher      dispatcher;

    @Before
    public void setUp() throws Exception {
        when(urlResolver.resolve("url")).thenReturn("id");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldResolveUrlOnDispatch() {
        dispatcher.dispatch("url", "message");

        verify(urlResolver).resolve("url");
    }

    @Test
    public void shouldRunReceiveOnDispatch(){
        dispatcher.dispatch("url", "message");

        verify(receiver).receive("id", "message");
    }

}
