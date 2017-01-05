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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Tests for {@link JsonRpcResponse}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class JsonRpcResponseTest {
    public static final String ID = "0";
    JsonParser jsonParser = new JsonParser();
    @Mock
    DtoFactory dtoFactory;

    @Mock
    JsonRpcResult result;
    @Mock
    JsonRpcError  error;

    JsonObject response;

    @BeforeMethod
    public void setUp() throws Exception {
        response = new JsonObject();

        JsonObject error = new JsonObject();
        JsonObject result = new JsonObject();

        error.addProperty("code", 0);
        error.addProperty("message", "error message");

        response.addProperty("jsonrpc", "2.0");
        response.addProperty("id", "0");

        response.add("error", error);
        response.add("result", result);

        when(this.result.toJsonElement()).thenReturn(result);
        when(this.error.toJsonObject()).thenReturn(error);
    }

    @Test
    public void shouldHaveErrorWhenParsingStringWithError() throws Exception {
        response.remove("result");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(response.toString(), jsonParser, dtoFactory);

        assertTrue(jsonRpcResponse.hasError());
        assertFalse(jsonRpcResponse.hasResult());

    }

    @Test
    public void shouldHaveResultWhenParsingStringWithResult() throws Exception {
        response.remove("error");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(response.toString(), jsonParser, dtoFactory);

        assertTrue(jsonRpcResponse.hasResult());
        assertFalse(jsonRpcResponse.hasError());
    }

    @Test
    public void shouldToJsonObjectWhenParsingStringWithResult() throws Exception {
        JsonObject expected = response;
        expected.remove("error");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(expected.toString(), jsonParser, dtoFactory);
        JsonObject actual = jsonRpcResponse.toJsonObject();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonObjectWhenParsingStringWithError() throws Exception {
        JsonObject expected = response;
        expected.remove("result");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(expected.toString(), jsonParser, dtoFactory);
        JsonObject actual = jsonRpcResponse.toJsonObject();

        assertEquals(expected, actual);

    }

    @Test
    public void shouldToStringWhenParsingStringWithResult() throws Exception {
        JsonObject expected = response;
        expected.remove("error");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(expected.toString(), jsonParser, dtoFactory);
        JsonElement actual = jsonParser.parse(jsonRpcResponse.toString());

        assertEquals(expected, actual);

    }

    @Test
    public void shouldToStringWhenParsingStringWithError() throws Exception {
        JsonObject expected = response;
        expected.remove("result");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(expected.toString(), jsonParser, dtoFactory);
        JsonElement actual = jsonParser.parse(jsonRpcResponse.toString());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldHaveErrorWhenPassingParametersWithError() throws Exception {
        response.remove("result");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(ID, null, error);

        assertTrue(jsonRpcResponse.hasError());
        assertFalse(jsonRpcResponse.hasResult());
    }

    @Test
    public void shouldHaveResultWhenPassingParametersWithResult() throws Exception {
        response.remove("error");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(ID, result, null);

        assertTrue(jsonRpcResponse.hasResult());
        assertFalse(jsonRpcResponse.hasError());
    }

    @Test
    public void shouldToJsonObjectWhenPassingParametersWithResult() throws Exception {
        JsonObject expected = response;
        expected.remove("error");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(ID, result, null);
        JsonObject actual = jsonRpcResponse.toJsonObject();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonObjectWhenPassingParametersWithError() throws Exception {
        JsonObject expected = response;
        expected.remove("result");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(ID, null, error);
        JsonObject actual = jsonRpcResponse.toJsonObject();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResult() throws Exception {
        JsonObject expected = response;
        expected.remove("error");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(ID, result, null);
        JsonElement actual = jsonParser.parse(jsonRpcResponse.toString());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassingParametersWithError() throws Exception {
        JsonObject expected = response;
        expected.remove("result");

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse(ID, null, error);
        JsonElement actual = jsonParser.parse(jsonRpcResponse.toString());

        assertEquals(expected, actual);
    }
}
