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

import elemental.json.Json;
import elemental.json.JsonFactory;
import elemental.json.JsonValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link JsonRpcError}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonRpcErrorTest {
    static final String ERROR_JSON = "{\"code\":0, \"message\":\"message\"}";

    JsonFactory jsonFactory = Json.instance();

    JsonRpcError error;

    @Test
    public void shouldInitFieldsFromString() {
        error = new JsonRpcError(ERROR_JSON, jsonFactory);

        assertEquals(0, error.getCode());
        assertEquals("message", error.getMessage());
    }

    @Test
    public void shouldInitFieldsFromValues() {
        error = new JsonRpcError(0, "message", jsonFactory);

        assertEquals(0, error.getCode());
        assertEquals("message", error.getMessage());
    }

    @Test
    public void shouldToJsonObjectProperly() {
        JsonRpcError real = new JsonRpcError(ERROR_JSON, jsonFactory);

        JsonValue expected = jsonFactory.parse(ERROR_JSON);

        assertTrue(real.toJsonObject().jsEquals(expected));
    }


    @Test
    public void shouldToStringProperly() {
        JsonRpcError real = new JsonRpcError(ERROR_JSON, jsonFactory);

        JsonValue expected = jsonFactory.parse(ERROR_JSON);

        assertEquals(real.toString(), expected.toJson());
    }
}
