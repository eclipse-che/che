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
package org.eclipse.che.api.core.jsonrpc;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Tests for {@link JsonRpcError}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class JsonRpcErrorTest {
    static final String ERROR_JSON = "{\"code\":0, \"message\":\"message\"}";

    JsonParser jsonParser = new JsonParser();

    JsonRpcError error;

    @Test
    public void shouldInitFieldsFromString() {
        error = new JsonRpcError(ERROR_JSON, jsonParser);

        assertEquals(0, error.getCode());
        assertEquals("message", error.getMessage());
    }

    @Test
    public void shouldInitFieldsFromValues() {
        error = new JsonRpcError(0, "message");

        assertEquals(0, error.getCode());
        assertEquals("message", error.getMessage());
    }

    @Test
    public void shouldToJsonObjectProperly() {
        JsonRpcError real = new JsonRpcError(ERROR_JSON, jsonParser);

        JsonElement expected = jsonParser.parse(ERROR_JSON);

        assertTrue(real.toJsonObject().equals(expected));
    }


    @Test
    public void shouldToStringProperly() {
        JsonRpcError real = new JsonRpcError(ERROR_JSON, jsonParser);

        JsonElement expected = jsonParser.parse(ERROR_JSON);

        assertEquals(real.toJsonObject(), expected);
    }
}
