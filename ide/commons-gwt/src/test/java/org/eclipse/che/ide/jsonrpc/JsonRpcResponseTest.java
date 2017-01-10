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
import elemental.json.JsonObject;
import elemental.json.JsonValue;

import org.eclipse.che.ide.dto.DtoFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JsonRpcResponse}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonRpcResponseTest {
    public static final String ID = "0";
    JsonFactory jsonFactory = Json.instance();
    @Mock
    DtoFactory  dtoFactory;

    @Mock
    JsonRpcResult result;
    @Mock
    JsonRpcError error;

    JsonObject response;

    @Before
    public void setUp() throws Exception {
        response = jsonFactory.createObject();

        JsonObject error = jsonFactory.createObject();
        JsonObject result = jsonFactory.createObject();

        error.put("code", 0);
        error.put("message", "error message");

        response.put("jsonrpc", "2.0");
        response.put("id", "0");

        response.put("error", error);
        response.put("result", result);

        when(this.result.toJsonValue()).thenReturn(result);
        when(this.error.toJsonObject()).thenReturn(error);
    }

    @Test
    public void shouldHaveErrorWhenParsingStringWithError() throws Exception {
        response.remove("result");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(response.toJson(), jsonFactory, dtoFactory);

        assertTrue(jsonRpcResponse.hasError());
        assertFalse(jsonRpcResponse.hasResult());

    }

    @Test
    public void shouldHaveResultWhenParsingStringWithResult() throws Exception {
        response.remove("error");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(response.toJson(), jsonFactory, dtoFactory);

        assertTrue(jsonRpcResponse.hasResult());
        assertFalse(jsonRpcResponse.hasError());
    }

    @Test
    public void shouldToJsonObjectWhenParsingStringWithResult() throws Exception {
        JsonObject expected = response;
        expected.remove("error");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(expected.toJson(), jsonFactory, dtoFactory);
        JsonObject actual = jsonRpcResponse.toJsonObject();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToJsonObjectWhenParsingStringWithError() throws Exception {
        JsonObject expected = response;
        expected.remove("result");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(expected.toJson(), jsonFactory, dtoFactory);
        JsonObject actual = jsonRpcResponse.toJsonObject();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringWhenParsingStringWithResult() throws Exception {
        JsonObject expected = response;
        expected.remove("error");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(expected.toJson(), jsonFactory, dtoFactory);
        JsonValue actual = jsonFactory.parse(jsonRpcResponse.toString());

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringWhenParsingStringWithError() throws Exception {
        JsonObject expected = response;
        expected.remove("result");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(expected.toJson(), jsonFactory, dtoFactory);
        JsonValue actual = jsonFactory.parse(jsonRpcResponse.toString());

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldHaveErrorWhenPassingParametersWithError() throws Exception {
        response.remove("result");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(ID, null, error, jsonFactory);

        assertTrue(jsonRpcResponse.hasError());
        assertFalse(jsonRpcResponse.hasResult());
    }

    @Test
    public void shouldHaveResultWhenPassingParametersWithResult() throws Exception {
        response.remove("error");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(ID, result, null, jsonFactory);

        assertTrue(jsonRpcResponse.hasResult());
        assertFalse(jsonRpcResponse.hasError());
    }

    @Test
    public void shouldToJsonObjectWhenPassingParametersWithResult() throws Exception {
        JsonObject expected = response;
        expected.remove("error");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(ID, result, null, jsonFactory);
        JsonObject actual = jsonRpcResponse.toJsonObject();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToJsonObjectWhenPassingParametersWithError() throws Exception {
        JsonObject expected = response;
        expected.remove("result");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(ID, null, error, jsonFactory);
        JsonObject actual = jsonRpcResponse.toJsonObject();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResult() throws Exception {
        JsonObject expected = response;
        expected.remove("error");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(ID, result, null, jsonFactory);
        JsonValue actual = jsonFactory.parse(jsonRpcResponse.toString());

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringWhenPassingParametersWithError() throws Exception {
        JsonObject expected = response;
        expected.remove("result");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(ID, null, error, jsonFactory);
        JsonValue actual = jsonFactory.parse(jsonRpcResponse.toString());

        assertTrue(expected.jsEquals(actual));
    }
}
