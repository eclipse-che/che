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
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link WebSocketConnectionSustainer}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketConnectionSustainerTest {
    @Mock
    private WebSocketConnectionManager   connectionManager;
    @Mock
    private WebSocketPropertyManager     propertyManager;
    @InjectMocks
    private WebSocketConnectionSustainer sustainer;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void shouldGetReConnectionAttemptsOnReset() {
        sustainer.reset("url");

        verify(propertyManager).getReConnectionAttempts("url");
    }

    @Test
    public void shouldSetReConnectionAttemptsOnReset() {
        sustainer.reset("url");

        verify(propertyManager).setReConnectionAttempts("url", 0);
    }

    @Test
    public void shouldGetReConnectionAttemptsOnSustain() {
        sustainer.sustain("url");

        verify(propertyManager).getReConnectionAttempts("url");

    }

    @Test
    public void shouldDisableSustainerOnExceedingTheLimitOfAttepts(){
        when(propertyManager.getReConnectionAttempts("url")).thenReturn(10);

        sustainer.sustain("url");

        verify(propertyManager).disableSustainer("url");
    }

    @Test
    public void shouldNotDisableSustainerIfNotExceededTheLimitOfAttepts(){
        when(propertyManager.getReConnectionAttempts("url")).thenReturn(0);

        sustainer.sustain("url");

        verify(propertyManager, never()).disableSustainer("url");
    }

    @Test
    public void shouldCheckIfSustainerIsEnabled(){
        sustainer.sustain("url");

        verify(propertyManager).sustainerEnabled("url");
    }

    @Test
    public void shouldProperlySetReconnectionAttemptsWhenSustainerIsEnabled(){
        when(propertyManager.getReConnectionAttempts("url")).thenReturn(0);
        when(propertyManager.sustainerEnabled("url")).thenReturn(true);

        sustainer.sustain("url");

        verify(propertyManager).setReConnectionAttempts("url", 1);
    }

    @Test
    public void shouldNotSetReconnectionAttemptsWhenSustainerIsDisabled(){
        when(propertyManager.sustainerEnabled("url")).thenReturn(false);

        sustainer.sustain("url");

        verify(propertyManager, never()).setReConnectionAttempts("url", 1);
    }


    @Test
    public void shouldProperlySetConnectionDelayWhenSustainerIsEnabled(){
        when(propertyManager.getReConnectionAttempts("url")).thenReturn(0);

        when(propertyManager.sustainerEnabled("url")).thenReturn(true);

        sustainer.sustain("url");

        verify(propertyManager).setConnectionDelay("url", 1_000);
    }

    @Test
    public void shouldNotSetConnectionDelayWhenSustainerIsDisabled(){
        when(propertyManager.sustainerEnabled("url")).thenReturn(false);

        sustainer.sustain("url");

        verify(propertyManager, never()).setConnectionDelay("url", 1_000);
    }

    @Test
    public void shouldRunEstablishConnectionWhenSustainerIsEnabled(){
        when(propertyManager.sustainerEnabled("url")).thenReturn(true);

        sustainer.sustain("url");

        verify(connectionManager).establishConnection("url");
    }

    @Test
    public void shouldNotRunConnectionWhenSustainerIsDisabled(){
        when(propertyManager.sustainerEnabled("url")).thenReturn(false);

        sustainer.sustain("url");

        verify(connectionManager, never()).establishConnection("url");
    }
}
