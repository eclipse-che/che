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
package org.eclipse.che.api.core.websocket.impl;

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.websocket.Session;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.*;

/**
 * Tests for {@link WebSocketSessionRegistry}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class WebSocketSessionRegistryTest {

    private WebSocketSessionRegistry registry;

    @Mock
    private Session session;

    @BeforeMethod
    public void setUp() throws Exception {
        registry = new WebSocketSessionRegistry();
    }

    @Test
    public void shouldAddSession() {
        assertTrue(registry.getSessions().isEmpty());

        registry.add("0", session);

        assertFalse(registry.getSessions().isEmpty());
    }

    @Test
    public void shouldAddCorrectSession() {
        registry.add("0", this.session);

        final Optional<Session> sessionOptional = registry.get("0");
        assertTrue(sessionOptional.isPresent());

        final Session session = sessionOptional.get();
        assertEquals(this.session, session);
    }


    @Test
    public void shouldRemoveSession() {
        registry.add("0", session);

        assertFalse(registry.getSessions().isEmpty());
        assertEquals(1, registry.getSessions().size());

        registry.remove("0");

        assertTrue(registry.getSessions().isEmpty());
    }

    @Test
    public void shouldGetAllSessions() {
        registry.add("0", session);

        assertFalse(registry.getSessions().isEmpty());
        assertEquals(1, registry.getSessions().size());

        registry.add("1", mock(Session.class));

        assertFalse(registry.getSessions().isEmpty());
        assertEquals(2, registry.getSessions().size());
    }

}
