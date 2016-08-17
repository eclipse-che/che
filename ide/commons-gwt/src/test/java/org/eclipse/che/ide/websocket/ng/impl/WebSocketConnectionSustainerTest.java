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
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.stream.IntStream.range;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;


/**
 * Tests for {@link WebSocketConnectionSustainer}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class WebSocketConnectionSustainerTest {

    @Mock
    private WebSocketConnection          connection;
    @Spy
    @InjectMocks
    private WebSocketConnectionSustainer sustainer;

    @Before
    public void before() {
        sustainer.enable();
    }

    @Test
    public void shouldDisableSustainerOnExceedingLimits() {
        range(0, 5).forEach(value -> sustainer.sustain());

        sustainer.sustain();

        verify(sustainer).disable();
    }

    @Test
    public void shouldResetAttemptsOnSustainerReset() {
        range(0, 5).forEach(value -> sustainer.sustain());

        sustainer.reset();

        range(0, 5).forEach(value -> sustainer.sustain());

        verify(sustainer, never()).disable();
    }

    @Test
    public void shouldOpenConnectionOnSustain() {
        sustainer.sustain();

        verify(connection).open(500);
    }
}
