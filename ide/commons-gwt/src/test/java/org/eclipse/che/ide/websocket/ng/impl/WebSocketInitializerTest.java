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
 * Tests for {@link WebSocketInitializer}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketInitializerTest {
    @Mock
    private WebSocketConnectionManager connectionManager;
    @Mock
    private WebSocketPropertyManager   propertyManager;
    @Mock
    private UrlResolver                urlResolver;
    @InjectMocks
    private WebSocketInitializer       initializer;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldSetUrlMappingOnInitialize() {
        initializer.initialize("id", "url");

        verify(urlResolver).setMapping("id", "url");
    }

    @Test
    public void shouldRunConnectionManagerInitializeConnectionOnInitialize() {
        initializer.initialize("id", "url");

        verify(connectionManager).initializeConnection("url");
    }

    @Test
    public void shouldRunPropertyManagerInitializeConnectionOnInitialize() {
        initializer.initialize("id", "url");

        verify(propertyManager).initializeConnection("url");
    }

    @Test
    public void shouldRunEstablishConnectionOnInitialize() {
        initializer.initialize("id", "url");

        verify(connectionManager).establishConnection("url");
    }

    @Test
    public void shouldGetUrlOnTerminate(){
        when(urlResolver.removeMapping("id")).thenReturn("url");

        initializer.terminate("id");

        verify(urlResolver).removeMapping("id");
    }

    @Test
    public void shouldDisableSustainerOnTerminate(){
        when(urlResolver.removeMapping("id")).thenReturn("url");

        initializer.terminate("id");

        verify(propertyManager).disableSustainer("url");
    }

    @Test
    public void shouldCloseConnectionOnTerminate(){
        when(urlResolver.removeMapping("id")).thenReturn("url");

        initializer.terminate("id");

        verify(connectionManager).closeConnection("url");
    }
}
