/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.jsonrpc;

import elemental.json.JsonFactory;
import elemental.json.JsonObject;

import org.eclipse.che.ide.jsonrpc.JsonRpcEntityQualifier.JsonRpcEntityType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JsonRpcEntityQualifier}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonRpcEntityQualifierTest {
    @Mock
    JsonFactory            jsonFactory;
    @InjectMocks
    JsonRpcEntityQualifier qualifier;

    @Mock
    JsonObject jsonObject;

    @Before
    public void setUp() {
        when(jsonFactory.parse(anyString())).thenReturn(jsonObject);
    }

    @Test
    public void shouldQualifyRequestWhenMessageContainsMethod() {
        when(jsonObject.keys()).thenReturn(new String[]{"method"});

        JsonRpcEntityType type = qualifier.qualify("message");

        verify(jsonFactory).parse("message");
        assertEquals(JsonRpcEntityType.REQUEST, type);
    }

    @Test
    public void shouldQualifyResponseWhenMessageContainsError() {
        when(jsonObject.keys()).thenReturn(new String[]{"error"});

        JsonRpcEntityType type = qualifier.qualify("message");

        verify(jsonFactory).parse("message");
        assertEquals(JsonRpcEntityType.RESPONSE, type);
    }

    @Test
    public void shouldQualifyResponseWhenMessageContainsResult() {
        when(jsonObject.keys()).thenReturn(new String[]{"result"});

        JsonRpcEntityType type = qualifier.qualify("message");

        verify(jsonFactory).parse("message");
        assertEquals(JsonRpcEntityType.RESPONSE, type);
    }

    @Test
    public void shouldQualifyUndefinedWhenMessageContainsResultAndError() {
        when(jsonObject.keys()).thenReturn(new String[]{"result", "error"});

        JsonRpcEntityType type = qualifier.qualify("message");

        verify(jsonFactory).parse("message");
        assertEquals(JsonRpcEntityType.UNDEFINED, type);
    }

    @Test
    public void shouldQualifyUndefinedWhenMessageContainsNoValuableKeys() {
        when(jsonObject.keys()).thenReturn(new String[]{});

        JsonRpcEntityType type = qualifier.qualify("message");

        verify(jsonFactory).parse("message");
        assertEquals(JsonRpcEntityType.UNDEFINED, type);
    }
}
