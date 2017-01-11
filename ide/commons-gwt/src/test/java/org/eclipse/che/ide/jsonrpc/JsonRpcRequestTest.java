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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JsonRpcRequest}
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonRpcRequestTest {
    public static final String MESSAGE = "{\"method\" : \"method\", \"params\":\"{\\\"parameter\\\":\\\"value\\\"}\"}";

    JsonFactory jsonFactory = Json.instance();

    @Mock
    JsonRpcFactory jsonRpcFactory;

    @Mock
    JsonRpcParams params;

    JsonObject request;

    @Before
    public void setUp() throws Exception {
        when(jsonRpcFactory.createParams(anyString())).thenReturn(params);

        JsonObject params = jsonFactory.createObject();
        params.put("parameter", "value");

        when(this.params.toJsonValue()).thenReturn(params);
        when(this.params.toString()).thenReturn(params.toJson());

        request = jsonFactory.createObject();
        request.put("jsonrpc", "2.0");
        request.put("method", "method");
        request.put("params", params);
        request.put("id", "0");
    }

    @Test
    public void shouldHaveParamsWhenParseStringWithParams() throws Exception {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toJson(), jsonFactory, jsonRpcFactory);

        assertTrue(jsonRpcRequest.hasParams());
    }

    @Test
    public void shouldNotHaveParamsWhenParseStringWithoutParams() throws Exception {
        request.remove("params");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toJson(), jsonFactory, jsonRpcFactory);

        assertFalse(jsonRpcRequest.hasParams());
    }

    @Test
    public void shouldHaveIdWhenParseStringWithId() throws Exception {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toJson(), jsonFactory, jsonRpcFactory);

        assertTrue(jsonRpcRequest.hasId());
    }

    @Test
    public void shouldNotIdParamsWhenParseStringWithoutId() throws Exception {
        request.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toJson(), jsonFactory, jsonRpcFactory);

        assertFalse(jsonRpcRequest.hasId());
    }

    @Test
    public void shouldToJsonObjectWhenParseStringParamsWithIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toJson(), jsonFactory, jsonRpcFactory);
        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToJsonObjectWhenParseStringParamsWithoutIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toJson(), jsonFactory, jsonRpcFactory);
        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToJsonObjectWhenParseStringParamsWithIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("params");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toJson(), jsonFactory, jsonRpcFactory);
        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToJsonObjectWhenParseStringParamsWithoutIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("params");
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toJson(), jsonFactory, jsonRpcFactory);
        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringWhenParseStringParamsWithIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toJson(), jsonFactory, jsonRpcFactory);
        JsonValue actual = jsonFactory.parse(jsonRpcRequest.toString());

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringWhenParseStringParamsWithoutIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toJson(), jsonFactory, jsonRpcFactory);
        JsonValue actual = jsonFactory.parse(jsonRpcRequest.toString());

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringWhenParseStringParamsWithIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("params");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toJson(), jsonFactory, jsonRpcFactory);
        JsonValue actual = jsonFactory.parse(jsonRpcRequest.toString());

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringWhenParseStringParamsWithoutIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("params");
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(request.toJson(), jsonFactory, jsonRpcFactory);
        JsonValue actual = jsonFactory.parse(jsonRpcRequest.toString());

        assertTrue(expected.jsEquals(actual));
    }


    @Test
    public void shouldHaveParamsWhenPassStringWithParamsWithId() throws Exception {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", params, jsonFactory);

        assertTrue(jsonRpcRequest.hasParams());
    }

    @Test
    public void shouldHaveParamsWhenPassStringWithParamsWithoutId() throws Exception {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("method", params, jsonFactory);

        assertTrue(jsonRpcRequest.hasParams());
    }

    @Test
    public void shouldNotHaveParamsWhenPassingStringWithoutParamsWithId() throws Exception {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", null, jsonFactory);

        assertFalse(jsonRpcRequest.hasParams());
    }

    @Test
    public void shouldNotHaveParamsWhenPassingStringWithEmptyParamsWithId() throws Exception {
        when(params.toJsonValue()).thenReturn(jsonFactory.createObject());
        when(params.emptyOrAbsent()).thenReturn(true);

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", params, jsonFactory);

        assertFalse(jsonRpcRequest.hasParams());
    }

    @Test
    public void shouldNotHaveParamsWhenPassStringWithoutParamsWithoutId() throws Exception {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("method", null, jsonFactory);

        assertFalse(jsonRpcRequest.hasParams());
    }

    @Test
    public void shouldHaveIdWhenPassStringWithId() throws Exception {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", params, jsonFactory);

        assertTrue(jsonRpcRequest.hasId());
    }

    @Test
    public void shouldNotIdParamsWhenPassStringWithoutId() throws Exception {
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("method", params, jsonFactory);

        assertFalse(jsonRpcRequest.hasId());
    }

    @Test
    public void shouldToJsonObjectWhenPassStringParamsWithIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", params, jsonFactory);
        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToJsonObjectWhenPassStringParamsWithoutIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("method", params, jsonFactory);

        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToJsonObjectWhenPassStringParamsWithIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(true);

        JsonObject expected = request;
        expected.remove("params");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", params, jsonFactory);

        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToJsonObjectWhenPassStringParamsWithoutIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(true);

        JsonObject expected = request;
        expected.remove("params");
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("method", params, jsonFactory);
        JsonObject actual = jsonRpcRequest.toJsonObject();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringWhenPassStringParamsWithIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", params, jsonFactory);
        JsonValue actual = jsonFactory.parse(jsonRpcRequest.toString());

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringWhenPassStringParamsWithoutIdAndWithParams() {
        when(params.emptyOrAbsent()).thenReturn(false);

        JsonObject expected = request;
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("method", params, jsonFactory);

        JsonValue actual = jsonFactory.parse(jsonRpcRequest.toString());

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringWhenPassStringParamsWithIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(true);

        JsonObject expected = request;
        expected.remove("params");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("0", "method", params, jsonFactory);
        JsonValue actual = jsonFactory.parse(jsonRpcRequest.toString());

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringWhenPassStringParamsWithoutIdAndWithoutParams() {
        when(params.emptyOrAbsent()).thenReturn(true);

        JsonObject expected = request;
        expected.remove("params");
        expected.remove("id");

        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("method", params, jsonFactory);

        JsonValue actual = jsonFactory.parse(jsonRpcRequest.toString());

        assertTrue(expected.jsEquals(actual));
    }
}
