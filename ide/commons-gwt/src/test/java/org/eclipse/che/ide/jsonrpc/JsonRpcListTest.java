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
import elemental.json.JsonArray;
import elemental.json.JsonFactory;
import elemental.json.JsonValue;

import org.eclipse.che.ide.dto.DtoFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JsonRpcList}
 *
 * @author Dmitry Kuleshov
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonRpcListTest {
    static final String ARRAY         = "[]";
    static final String NOT_ARRAY     = "{}";
    static final String DTO           = "{\"parameter\":\"value\"}";

    @Mock
    DtoFactory dtoFactory;

    JsonFactory jsonFactory = Json.instance();

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
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        String actual = jsonRpcList.get(0, String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToListParsedStringArray() throws Exception {
        String expected = "a";
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        List<String> actual = jsonRpcList.toList(String.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToStringifiedListParsedStringArray() throws Exception {
        String expected = "a";
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        List<String> actual = jsonRpcList.toStringifiedList();

        assertEquals("\"a\"", actual.get(0));
    }

    @Test
    public void shouldToJsonArrayParsedStringArray() throws Exception {
        String expected = "a";
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        JsonArray actual = jsonRpcList.toJsonArray();

        assertTrue(array.jsEquals(actual));
    }

    @Test
    public void shouldToStringArrayParsedStringArray() throws Exception {
        String expected = "a";
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        String actual = jsonRpcList.toString();

        assertEquals(message, actual);
    }

    @Test
    public void shouldParseNumberArray() throws Exception {
        Double expected = 0D;
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        Double actual = jsonRpcList.get(0, Double.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToListParsedNumberArray() throws Exception {
        Double expected = 0D;
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        List<Double> actual = jsonRpcList.toList(Double.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToStringifiedListParsedNumberArray() throws Exception {
        Double expected = 0D;
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        List<String> actual = jsonRpcList.toStringifiedList();

        assertEquals(expected, Double.valueOf(actual.iterator().next()));
    }

    @Test
    public void shouldToJsonArrayParsedNumberArray() throws Exception {
        Double expected = 0D;
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        JsonArray actual = jsonRpcList.toJsonArray();

        assertTrue(array.jsEquals(actual));
    }

    @Test
    public void shouldToStringArrayParsedNumberArray() throws Exception {
        Double expected = 0D;
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        String actual = jsonRpcList.toString();

        assertEquals(message, actual);
    }


    @Test
    public void shouldParseBooleanArray() throws Exception {
        Boolean expected = false;
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        Boolean actual = jsonRpcList.get(0, Boolean.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToListParsedBooleanArray() throws Exception {
        Boolean expected = false;
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        List<Boolean> actual = jsonRpcList.toList(Boolean.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToStringifiedListParsedBooleanArray() throws Exception {
        Boolean expected = false;
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        List<String> actual = jsonRpcList.toStringifiedList();

        assertEquals(expected, Boolean.valueOf(actual.iterator().next()));
    }

    @Test
    public void shouldToJsonArrayParsedBooleanArray() throws Exception {
        Boolean expected = false;
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        JsonArray actual = jsonRpcList.toJsonArray();

        assertTrue(array.jsEquals(actual));
    }

    @Test
    public void shouldToStringArrayParsedBooleanArray() throws Exception {
        Boolean expected = false;
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        String actual = jsonRpcList.toString();

        assertEquals(message, actual);
    }

    @Test
    public void shouldParseDtoArray() throws Exception {
        Dto expected = mock(Dto.class);
        when(expected.toString()).thenReturn(DTO);
        when(expected.getParameter()).thenReturn("value");

        when(dtoFactory.createDtoFromJson(DTO, Dto.class)).thenReturn(expected);

        JsonValue parse = jsonFactory.parse(DTO);
        JsonArray array = jsonFactory.createArray();
        array.set(0, parse);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        Dto actual = jsonRpcList.get(0, Dto.class);

        assertEquals(expected.getParameter(), actual.getParameter());
    }

    @Test
    public void shouldToListParsedDtoArray() throws Exception {
        Dto expected = mock(Dto.class);
        when(expected.toString()).thenReturn(DTO);
        when(expected.getParameter()).thenReturn("value");

        when(dtoFactory.createDtoFromJson(DTO, Dto.class)).thenReturn(expected);

        JsonValue parse = jsonFactory.parse(DTO);
        JsonArray array = jsonFactory.createArray();
        array.set(0, parse);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        List<Dto> actual = jsonRpcList.toList(Dto.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToStringifiedListParsedDtoArray() throws Exception {
        Dto expected = mock(Dto.class);
        when(expected.toString()).thenReturn(DTO);
        when(expected.getParameter()).thenReturn("value");

        when(dtoFactory.createDtoFromJson(DTO, Dto.class)).thenReturn(expected);

        JsonValue parse = jsonFactory.parse(DTO);
        JsonArray array = jsonFactory.createArray();
        array.set(0, parse);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        List<String> actual = jsonRpcList.toStringifiedList();

        assertEquals(expected.toString(), actual.iterator().next());
    }

    @Test
    public void shouldToJsonArrayParsedDtoArray() throws Exception {
        Dto expected = mock(Dto.class);
        when(expected.toString()).thenReturn(DTO);
        when(expected.getParameter()).thenReturn("value");

        when(dtoFactory.createDtoFromJson(DTO, Dto.class)).thenReturn(expected);

        JsonValue parse = jsonFactory.parse(DTO);
        JsonArray array = jsonFactory.createArray();
        array.set(0, parse);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        JsonArray actual = jsonRpcList.toJsonArray();

        assertTrue(array.jsEquals(actual));
    }

    @Test
    public void shouldToStringArrayParsedDtoArray() throws Exception {
        Dto expected = mock(Dto.class);
        when(expected.toString()).thenReturn(DTO);
        when(expected.getParameter()).thenReturn("value");

        when(dtoFactory.createDtoFromJson(DTO, Dto.class)).thenReturn(expected);

        JsonValue parse = jsonFactory.parse(DTO);
        JsonArray array = jsonFactory.createArray();
        array.set(0, parse);
        String message = array.toJson();

        JsonRpcList jsonRpcList = new JsonRpcList(message, jsonFactory, dtoFactory);
        String actual = jsonRpcList.toString();

        assertEquals(message, actual);
    }

    @Test
    public void shouldCreateStringArray() throws Exception {
        String expected = "a";

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonFactory, dtoFactory);
        String actual = jsonRpcList.get(0, String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToListCreatedStringArray() throws Exception {
        String expected = "a";

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonFactory, dtoFactory);
        List<String> actual = jsonRpcList.toList(String.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToStringifiedListCreatedStringArray() throws Exception {
        String expected = "a";

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonFactory, dtoFactory);
        List<String> actual = jsonRpcList.toStringifiedList();

        assertEquals("\"a\"", actual.iterator().next());
    }

    @Test
    public void shouldToJsonArrayCreatedStringArray() throws Exception {
        JsonArray expected = jsonFactory.createArray();
        expected.set(0, "a");

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList("a"), jsonFactory, dtoFactory);
        JsonArray actual = jsonRpcList.toJsonArray();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringCreatedStringArray() throws Exception {
        JsonArray array = jsonFactory.createArray();
        array.set(0, "a");

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList("a"), jsonFactory, dtoFactory);

        assertEquals(array.toJson(), jsonRpcList.toString());
    }

    @Test
    public void shouldCreateDoubleArray() throws Exception {
        Double expected = 0D;

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonFactory, dtoFactory);
        Double actual = jsonRpcList.get(0, Double.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToListCreatedDoubleArray() throws Exception {
        Double expected = 0D;

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonFactory, dtoFactory);
        List<Double> actual = jsonRpcList.toList(Double.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToStringifiedListCreatedDoubleArray() throws Exception {
        Double expected = 0D;

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonFactory, dtoFactory);
        List<String> actual = jsonRpcList.toStringifiedList();

        assertEquals(expected, Double.valueOf(actual.iterator().next()));
    }

    @Test
    public void shouldToJsonArrayCreatedDoubleArray() throws Exception {
        JsonArray expected = jsonFactory.createArray();
        expected.set(0, 0D);

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(0D), jsonFactory, dtoFactory);
        JsonArray actual = jsonRpcList.toJsonArray();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringCreatedDoubleArray() throws Exception {
        JsonArray array = jsonFactory.createArray();
        array.set(0, 0D);

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(0D), jsonFactory, dtoFactory);

        assertEquals(array.toJson(), jsonRpcList.toString());
    }

    @Test
    public void shouldCreateBooleanArray() throws Exception {
        Boolean expected = false;

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonFactory, dtoFactory);
        Boolean actual = jsonRpcList.get(0, Boolean.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToListCreatedBooleanArray() throws Exception {
        Boolean expected = false;

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonFactory, dtoFactory);
        List<Boolean> actual = jsonRpcList.toList(Boolean.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToStringifiedListCreatedBooleanArray() throws Exception {
        Boolean expected = false;

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonFactory, dtoFactory);
        List<String> actual = jsonRpcList.toStringifiedList();

        assertEquals(expected, Boolean.valueOf(actual.iterator().next()));
    }

    @Test
    public void shouldToJsonArrayCreatedBooleanArray() throws Exception {
        JsonArray expected = jsonFactory.createArray();
        expected.set(0, false);

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(false), jsonFactory, dtoFactory);
        JsonArray actual = jsonRpcList.toJsonArray();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringCreatedBooleanArray() throws Exception {
        JsonArray array = jsonFactory.createArray();
        array.set(0, false);

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(false), jsonFactory, dtoFactory);

        assertEquals(array.toJson(), jsonRpcList.toString());
    }


    @Test
    public void shouldCreateDtoArray() throws Exception {
        Dto expected = mock(Dto.class);
        when(expected.toString()).thenReturn(DTO);
        when(expected.getParameter()).thenReturn("value");

        when(dtoFactory.createDtoFromJson(DTO, Dto.class)).thenReturn(expected);

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonFactory, dtoFactory);
        Dto actual = jsonRpcList.get(0, Dto.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToListCreatedDtoArray() throws Exception {
        Dto expected = mock(Dto.class);
        when(expected.toString()).thenReturn(DTO);
        when(expected.getParameter()).thenReturn("value");

        when(dtoFactory.createDtoFromJson(DTO, Dto.class)).thenReturn(expected);

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonFactory, dtoFactory);
        List<Dto> actual = jsonRpcList.toList(Dto.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToStringifiedListCreatedDtoArray() throws Exception {
        Dto expected = mock(Dto.class);
        when(expected.toString()).thenReturn(DTO);
        when(expected.getParameter()).thenReturn("value");

        when(dtoFactory.createDtoFromJson(DTO, Dto.class)).thenReturn(expected);

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(expected), jsonFactory, dtoFactory);
        List<String> actual = jsonRpcList.toStringifiedList();

        assertEquals(expected.toString(), actual.iterator().next());
    }

    @Test
    public void shouldToJsonArrayCreatedDtoArray() throws Exception {
        Dto dto = mock(Dto.class);
        when(dto.toString()).thenReturn(DTO);
        when(dto.getParameter()).thenReturn("value");

        when(dtoFactory.createDtoFromJson(DTO, Dto.class)).thenReturn(dto);

        JsonArray expected = jsonFactory.createArray();
        JsonValue parse = jsonFactory.parse(dto.toString());
        expected.set(0, parse);

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(dto), jsonFactory, dtoFactory);
        JsonArray actual = jsonRpcList.toJsonArray();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringCreatedDtoArray() throws Exception {
        Dto dto = mock(Dto.class);
        when(dto.toString()).thenReturn(DTO);
        when(dto.getParameter()).thenReturn("value");

        when(dtoFactory.createDtoFromJson(DTO, Dto.class)).thenReturn(dto);

        JsonArray array = jsonFactory.createArray();
        JsonValue parse = jsonFactory.parse(dto.toString());
        array.set(0, parse);

        JsonRpcList jsonRpcList = new JsonRpcList(singletonList(dto), jsonFactory, dtoFactory);

        assertEquals(array.toJson(), jsonRpcList.toString());
    }

    interface Dto {
        String getParameter();
    }
}
