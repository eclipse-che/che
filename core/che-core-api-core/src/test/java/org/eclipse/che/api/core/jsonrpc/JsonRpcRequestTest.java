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

import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Tests for {@link JsonRpcRequest}
 */
@Listeners(MockitoTestNGListener.class)
public class JsonRpcRequestTest {
    public static final String MESSAGE = "{\"method\" : \"method\", \"params\":\"{\\\"parameter\\\":\\\"value\\\"}\"}";

    JsonParser jsonParser = new JsonParser();

    @Mock
    JsonRpcFactory jsonRpcFactory;

    @Mock
    JsonRpcParams params;

    JsonObject request;

    @BeforeMethod
    public void setUp() throws Exception {
        when(jsonRpcFactory.createParams(anyString())).thenReturn(params);

        JsonObject params = new JsonObject();
        params.addProperty("parameter", "value");

        when(this.params.toJsonElement()).thenReturn(params);
        when(this.params.toString()).thenReturn(params.toString());

        request = new JsonObject();
        request.addProperty("jsonrpc", "2.0");
        request.addProperty("id", "0");
        request.addProperty("method", "method");
        request.add("params", params);
    }

    @Test
    public void shouldHaveParamsWhenParseStringWithParams() throws Exception {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toString(), jsonParser, jsonRpcFactory);

        assertTrue(jsonRpcRequest.hasParams());
    }

    @Test
    public void shouldNotHaveParamsWhenParseStringWithoutParams() throws Exception {
        request.remove("params");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toString(), jsonParser, jsonRpcFactory);

        assertFalse(jsonRpcRequest.hasParams());
    }

    @Test
    public void shouldHaveIdWhenParseStringWithId() throws Exception {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toString(), jsonParser, jsonRpcFactory);

        assertTrue(jsonRpcRequest.hasId());
    }

    @Test
    public void shouldNotIdParamsWhenParseStringWithoutId() throws Exception {
        request.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toString(), jsonParser, jsonRpcFactory);

        assertFalse(jsonRpcRequest.hasId());
    }

    @Test
    public void shouldToJsonObjectWhenParseStringParamsWithIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toString(), jsonParser, jsonRpcFactory);
        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonObjectWhenParseStringParamsWithoutIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toString(), jsonParser, jsonRpcFactory);
        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonObjectWhenParseStringParamsWithIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("params");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toString(), jsonParser, jsonRpcFactory);
        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonObjectWhenParseStringParamsWithoutIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("params");
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toString(), jsonParser, jsonRpcFactory);
        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenParseStringParamsWithIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toString(), jsonParser, jsonRpcFactory);
        JsonElement actual = jsonParser.parse(jsonRpcRequest.toString());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenParseStringParamsWithoutIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toString(), jsonParser, jsonRpcFactory);
        JsonElement actual = jsonParser.parse(jsonRpcRequest.toString());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenParseStringParamsWithIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("params");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toString(), jsonParser, jsonRpcFactory);
        JsonElement actual = jsonParser.parse(jsonRpcRequest.toString());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenParseStringParamsWithoutIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("params");
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toString(), jsonParser, jsonRpcFactory);
        JsonElement actual = jsonParser.parse(jsonRpcRequest.toString());

        assertEquals(expected, actual);
    }


    @Test
    public void shouldHaveParamsWhenPassStringWithParamsWithId() throws Exception {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", params, jsonParser);

        assertTrue(jsonRpcRequest.hasParams());
    }

    @Test
    public void shouldHaveParamsWhenPassStringWithParamsWithoutId() throws Exception {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("method", params, jsonParser);

        assertTrue(jsonRpcRequest.hasParams());
    }

    @Test
    public void shouldNotHaveParamsWhenPassingStringWithoutParamsWithId() throws Exception {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", null, jsonParser);

        assertFalse(jsonRpcRequest.hasParams());
    }

    @Test
    public void shouldNotHaveParamsWhenPassingStringWithEmptyParamsWithId() throws Exception {
        when(params.toJsonElement()).thenReturn(new JsonObject());
        when(params.emptyOrAbsent()).thenReturn(true);

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", params, jsonParser);

        assertFalse(jsonRpcRequest.hasParams());
    }

    @Test
    public void shouldNotHaveParamsWhenPassStringWithoutParamsWithoutId() throws Exception {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("method", null, jsonParser);

        assertFalse(jsonRpcRequest.hasParams());
    }

    @Test
    public void shouldHaveIdWhenPassStringWithId() throws Exception {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", params, jsonParser);

        assertTrue(jsonRpcRequest.hasId());
    }

    @Test
    public void shouldNotIdParamsWhenPassStringWithoutId() throws Exception {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("method", params, jsonParser);

        assertFalse(jsonRpcRequest.hasId());
    }

    @Test
    public void shouldToJsonObjectWhenPassStringParamsWithIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", params, jsonParser);
        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonObjectWhenPassStringParamsWithoutIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("method", params, jsonParser);

        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonObjectWhenPassStringParamsWithIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(true);

        JsonObject expected = request;
        expected.remove("params");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", params, jsonParser);

        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonObjectWhenPassStringParamsWithoutIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(true);

        JsonObject expected = request;
        expected.remove("params");
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("method", params, jsonParser);
        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassStringParamsWithIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", params, jsonParser);
        JsonElement actual = jsonParser.parse(jsonRpcRequest.toString());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassStringParamsWithoutIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("method", params, jsonParser);

        JsonElement actual = jsonParser.parse(jsonRpcRequest.toString());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassStringParamsWithIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(true);

        JsonObject expected = request;
        expected.remove("params");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", params, jsonParser);
        JsonElement actual = jsonParser.parse(jsonRpcRequest.toString());

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassStringParamsWithoutIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(true);

        JsonObject expected = request;
        expected.remove("params");
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("method", params, jsonParser);

        JsonElement actual = jsonParser.parse(jsonRpcRequest.toString());

        assertEquals(expected, actual);
    }
}
