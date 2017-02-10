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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.dto.shared.DTO;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Tests for {@link JsonRpcList}
 *
 * @author Dmitry Kuleshov
 */
@Listeners(MockitoTestNGListener.class)
public class JsonRpcListTest {
    static final String ARRAY     = "[]";
    static final String NOT_ARRAY = "{}";
    static final String DTO       = "{\"parameter\":\"value\"}";

    DtoFactory dtoFactory = DtoFactory.getInstance();

    JsonParser jsonParser = new JsonParser();

    @Test
    public void shouldProperlyDetectArray() throws Exception {
        assertTrue(JsonRpcList.isArray(ARRAY));
    }

    @Test
    public void shouldProperlyNotDetectArray() throws Exception {
        assertFalse(JsonRpcList.isArray(NOT_ARRAY));
    }

    @Test
    public void shouldParseStringArray() throws Exception {
        String expected = "a";

        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(expected));
        String message = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        String actual = jsonRpcList.get(0, String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToListParsedStringArray() throws Exception {
        String expected = "a";
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(expected));
        String message = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        List<String> actual = jsonRpcList.toList(String.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToStringifiedListParsedStringArray() throws Exception {
        String expected = "a";
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(expected));
        String message = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        List<String> actual = jsonRpcList.toStringifiedList();

        assertEquals("\"a\"", actual.get(0));
    }

    @Test
    public void shouldToJsonArrayParsedStringArray() throws Exception {
        JsonArray expected = new JsonArray();
        expected.add(new JsonPrimitive("a"));
        String message = expected.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        JsonArray actual = jsonRpcList.toJsonArray();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringArrayParsedStringArray() throws Exception {
        String expected = "a";
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(expected));
        String message = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        String actual = jsonRpcList.toString();

        assertEquals(message, actual);
    }

    @Test
    public void shouldParseNumberArray() throws Exception {
        Double expected = 0D;
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(expected));
        String message = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        Double actual = jsonRpcList.get(0, Double.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToListParsedNumberArray() throws Exception {
        Double expected = 0D;
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(expected));
        String message = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        List<Double> actual = jsonRpcList.toList(Double.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToStringifiedListParsedNumberArray() throws Exception {
        Double expected = 0D;
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(expected));
        String message = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        List<String> actual = jsonRpcList.toStringifiedList();

        assertEquals(expected, Double.valueOf(actual.iterator().next()));
    }

    @Test
    public void shouldToJsonArrayParsedNumberArray() throws Exception {
        JsonArray expected = new JsonArray();
        expected.add(new JsonPrimitive(0D));
        String message = expected.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        JsonArray actual = jsonRpcList.toJsonArray();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringArrayParsedNumberArray() throws Exception {
        Double expected = 0D;
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(expected));
        String message = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        String actual = jsonRpcList.toString();

        assertEquals(message, actual);
    }

    @Test
    public void shouldParseBooleanArray() throws Exception {
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(false));
        String message = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        Boolean actual = jsonRpcList.get(0, Boolean.class);

        assertFalse(actual);
    }

    @Test
    public void shouldToListParsedBooleanArray() throws Exception {
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(false));
        String message = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        List<Boolean> actual = jsonRpcList.toList(Boolean.class);

        assertEquals(singletonList(false), actual);
    }

    @Test
    public void shouldToStringifiedListParsedBooleanArray() throws Exception {
        Boolean expected = false;
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(expected));
        String message = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        List<String> actual = jsonRpcList.toStringifiedList();

        assertEquals(expected, Boolean.valueOf(actual.iterator().next()));
    }

    @Test
    public void shouldToJsonArrayParsedBooleanArray() throws Exception {
        Boolean expected = false;
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(expected));
        String message = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        JsonArray actual = jsonRpcList.toJsonArray();

        assertEquals(array, actual);
    }

    @Test
    public void shouldToStringArrayParsedBooleanArray() throws Exception {
        Boolean expected = false;
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(expected));
        String message = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonParser);
        String actual = jsonRpcList.toString();

        assertEquals(message, actual);
    }

    @Test
    public void shouldCreateStringArray() throws Exception {
        String expected = "a";

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonParser);
        String actual = jsonRpcList.get(0, String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToListCreatedStringArray() throws Exception {
        String expected = "a";

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonParser);
        List<String> actual = jsonRpcList.toList(String.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToJsonArrayCreatedStringArray() throws Exception {
        JsonArray expected = new JsonArray();
        expected.add(new JsonPrimitive("a"));

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList("a"), jsonParser);
        JsonArray actual = jsonRpcList.toJsonArray();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringCreatedStringArray() throws Exception {
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive("a"));
        String expected = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList("a"), jsonParser);
        String actual = jsonRpcList.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldCreateDoubleArray() throws Exception {
        Double expected = 0D;

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonParser);
        Double actual = jsonRpcList.get(0, Double.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToListCreatedDoubleArray() throws Exception {
        Double expected = 0D;

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonParser);
        List<Double> actual = jsonRpcList.toList(Double.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToStringifiedListCreatedDoubleArray() throws Exception {
        Double expected = 0D;

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonParser);
        List<String> actual = jsonRpcList.toStringifiedList();

        assertEquals(expected, Double.valueOf(actual.iterator().next()));
    }

    @Test
    public void shouldToJsonArrayCreatedDoubleArray() throws Exception {
        JsonArray expected = new JsonArray();
        expected.add(new JsonPrimitive(0D));

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(0D), jsonParser);
        JsonArray actual = jsonRpcList.toJsonArray();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringCreatedDoubleArray() throws Exception {
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(0D));
        String expected = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(0D), jsonParser);
        String actual = jsonRpcList.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldCreateBooleanArray() throws Exception {
        Boolean expected = false;

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonParser);
        Boolean actual = jsonRpcList.get(0, Boolean.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToListCreatedBooleanArray() throws Exception {
        Boolean expected = false;

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonParser);
        List<Boolean> actual = jsonRpcList.toList(Boolean.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToStringifiedListCreatedBooleanArray() throws Exception {
        Boolean expected = false;

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonParser);
        List<String> actual = jsonRpcList.toStringifiedList();

        assertEquals(expected, Boolean.valueOf(actual.iterator().next()));
    }

    @Test
    public void shouldToJsonArrayCreatedBooleanArray() throws Exception {
        JsonArray expected = new JsonArray();
        expected.add(new JsonPrimitive(false));

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(false), jsonParser);
        JsonArray actual = jsonRpcList.toJsonArray();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringCreatedBooleanArray() throws Exception {
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(false));
        String expected = array.toString();

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(false), jsonParser);
        String actual = jsonRpcList.toString();

        assertEquals(expected, actual);
    }
}