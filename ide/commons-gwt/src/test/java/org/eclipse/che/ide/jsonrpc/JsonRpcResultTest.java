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
import elemental.json.JsonNumber;
import elemental.json.JsonObject;
import elemental.json.JsonString;
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
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JsonRpcResult}
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonRpcResultTest {

    JsonFactory jsonFactory = Json.instance();

    @Mock
    DtoFactory dtoFactory;
    @Mock
    Dto        dto;

    JsonObject result;
    JsonArray  resultList;

    @Before
    public void setUp() throws Exception {
        result = jsonFactory.createObject();
        resultList = jsonFactory.createArray();

        when(dto.getParameters()).thenReturn("value");
        when(dto.toString()).thenReturn("{\"parameter\":\"value\"}");

        when(dtoFactory.createDtoFromJson(dto.toString(), Dto.class)).thenReturn(dto);
    }


    @Test
    public void shouldBeEmptyOrAbsentWhenParsingStringWithEmptyResult() throws Exception {
        JsonRpcResult jsonRpcResult = new JsonRpcResult(result.toJson(), jsonFactory, dtoFactory);

        assertTrue(jsonRpcResult.isEmptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentWhenParsingStringWithNotEmptyResult() throws Exception {
        result.put("key", "value");

        JsonRpcResult jsonRpcResult = new JsonRpcResult(result.toJson(), jsonFactory, dtoFactory);

        assertFalse(jsonRpcResult.isEmptyOrAbsent());
    }

    @Test
    public void shouldBeEmptyOrAbsentWhenParsingStringWithEmptyResultList() throws Exception {
        JsonRpcResult jsonRpcResult = new JsonRpcResult(resultList.toJson(), jsonFactory, dtoFactory);

        assertTrue(jsonRpcResult.isEmptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentWhenParsingStringWithNotEmptyResultList() throws Exception {
        resultList.set(0, jsonFactory.createObject());

        JsonRpcResult jsonRpcResult = new JsonRpcResult(resultList.toJson(), jsonFactory, dtoFactory);

        assertFalse(jsonRpcResult.isEmptyOrAbsent());
    }

    @Test
    public void shouldBeAnArrayWhenParsingStringWithResultList() throws Exception {
        JsonRpcResult jsonRpcResult = new JsonRpcResult(resultList.toJson(), jsonFactory, dtoFactory);

        assertTrue(jsonRpcResult.isArray());
    }

    @Test
    public void shouldNotBeAnArrayWhenParsingStringWithSingleResult() throws Exception {
        JsonRpcResult jsonRpcResult = new JsonRpcResult(result.toJson(), jsonFactory, dtoFactory);

        assertFalse(jsonRpcResult.isArray());
    }

    // String

    @Test
    public void shouldGetAsStringWhenParsingStringWithResultAsString() throws Exception {
        JsonString value = jsonFactory.create("a");
        String expected = value.asString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(value.toJson(), jsonFactory, dtoFactory);
        String actual = jsonRpcResult.getAs(String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListOfStringWhenParsingStringWithResultAsString() throws Exception {
        String expected = "a";
        JsonString string = jsonFactory.create(expected);
        JsonArray array = jsonFactory.createArray();
        array.set(0, string);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(array.toJson(), jsonFactory, dtoFactory);
        List<String> actual = jsonRpcResult.getAsListOf(String.class);

        assertEquals(expected, actual.iterator().next());
    }

    @Test
    public void shouldToStringWhenParsingStringWithResultAsASingleString() throws Exception {
        JsonString string = jsonFactory.create("a");
        String expected = string.toJson();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(string.toJson(), jsonFactory, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenParsingStringWithResultAsAListOfString() throws Exception {
        JsonString string = jsonFactory.create("a");
        JsonArray array = jsonFactory.createArray();
        array.set(0, string);
        String expected = array.toJson();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(array.toJson(), jsonFactory, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenParsingStringWithResultAsASingleString() throws Exception {
        JsonString expected = jsonFactory.create("a");

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected.toJson(), jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcResult.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToJsonValueWhenParsingStringWithResultAsAListOfString() throws Exception {
        JsonString string = jsonFactory.create("a");
        JsonArray expected = jsonFactory.createArray();
        expected.set(0, string);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected.toJson(), jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcResult.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    // Number

    @Test
    public void shouldGetAsNumberWhenParsingStringWithResultAsNumber() throws Exception {
        JsonNumber value = jsonFactory.create(0D);
        Double expected = value.asNumber();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(value.toJson(), jsonFactory, dtoFactory);
        Double actual = jsonRpcResult.getAs(Double.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListOfNumberWhenParsingStringWithResultAsListOfNumber() throws Exception {
        Double expected = 0D;
        JsonNumber string = jsonFactory.create(expected);
        JsonArray array = jsonFactory.createArray();
        array.set(0, string);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(array.toJson(), jsonFactory, dtoFactory);
        List<Double> actual = jsonRpcResult.getAsListOf(Double.class);

        assertEquals(expected, actual.iterator().next());
    }

    @Test
    public void shouldToStringWhenParsingStringWithResultAsASingleNumber() throws Exception {
        Double expected = 0D;
        JsonNumber string = jsonFactory.create(expected);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(string.toJson(), jsonFactory, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, Double.valueOf(actual));
    }

    @Test
    public void shouldToStringWhenParsingStringWithResultAsAListOfNumber() throws Exception {
        JsonNumber number = jsonFactory.create(0D);
        JsonArray array = jsonFactory.createArray();
        array.set(0, number);
        String expected = array.toJson();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(array.toJson(), jsonFactory, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenParsingStringWithResultAsASingleNumber() throws Exception {
        Double number = 0D;
        JsonNumber expected = jsonFactory.create(number);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected.toJson(), jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcResult.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToJsonValueWhenParsingStringWithResultAsAListOfNumber() throws Exception {
        JsonNumber number = jsonFactory.create(0D);
        JsonArray expected = jsonFactory.createArray();
        expected.set(0, number);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected.toJson(), jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcResult.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    //Void

    @Test
    public void shouldGetAsVoidWhenParsingStringWithResultAsVoid() throws Exception {
        JsonObject value = jsonFactory.createObject();
        Void expected = null;

        JsonRpcResult jsonRpcResult = new JsonRpcResult(value.toJson(), jsonFactory, dtoFactory);
        Void actual = jsonRpcResult.getAs(Void.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenParsingStringWithResultAsASingleVoid() throws Exception {
        JsonObject value = jsonFactory.createObject();
        String expected = value.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(value.toJson(), jsonFactory, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }


    @Test
    public void shouldToJsonValueWhenParsingStringWithResultAsASingleVoid() throws Exception {
        JsonObject expected = jsonFactory.createObject();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected.toJson(), jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcResult.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    // Dto

    @Test
    public void shouldGetAsDtoWhenParsingStringWithResultAsDto() throws Exception {
        JsonRpcResult jsonRpcResult = new JsonRpcResult(dto.toString(), jsonFactory, dtoFactory);
        Dto actual = jsonRpcResult.getAs(Dto.class);

        assertEquals(dto, actual);
    }

    @Test
    public void shouldGetAsListOfDtoWhenParsingStringWithResultAsListOfDto() throws Exception {
        JsonArray array = jsonFactory.createArray();
        JsonObject value = jsonFactory.parse(dto.toString());
        array.set(0, value);
        List<Dto> expected = singletonList(dto);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(array.toJson(), jsonFactory, dtoFactory);
        List<Dto> actual = jsonRpcResult.getAsListOf(Dto.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenParsingStringWithResultAsASingleDto() throws Exception {
        String expected = dto.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(dto.toString(), jsonFactory, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenParsingStringWithResultAsAListOfDto() throws Exception {
        JsonArray array = jsonFactory.createArray();
        JsonObject value = jsonFactory.parse(dto.toString());
        array.set(0, value);
        String expected = array.toJson();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(array.toJson(), jsonFactory, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenParsingStringWithResultAsASingleDto() throws Exception {
        JsonObject value = jsonFactory.parse(dto.toString());

        JsonRpcResult jsonRpcResult = new JsonRpcResult(dto.toString(), jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcResult.toJsonValue();

        assertTrue(value.jsEquals(actual));
    }

    @Test
    public void shouldToJsonValueWhenParsingStringWithResultAsAListOfDto() throws Exception {
        JsonArray expected = jsonFactory.createArray();
        JsonObject value = jsonFactory.parse(dto.toString());
        expected.set(0, value);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected.toJson(), jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcResult.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    // String

    @Test
    public void shouldGetAsStringWhenPassingParametersWithResultAsString() throws Exception {
        String expected = "a";

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected, dtoFactory, jsonFactory);
        String actual = jsonRpcResult.getAs(String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListOfStringWhenPassingParametersWithResultAsString() throws Exception {
        List<String> expected = singletonList("a");

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected, jsonFactory, dtoFactory);
        List<String> actual = jsonRpcResult.getAsListOf(String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResultAsASingleString() throws Exception {
        String value = "a";
        String expected = jsonFactory.create(value).toJson();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(value, dtoFactory, jsonFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResultAsAListOfString() throws Exception {
        String value = "a";
        JsonArray array = jsonFactory.createArray();
        array.set(0, value);
        String expected = array.toJson();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(singletonList(value), jsonFactory, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenPassingParametersWithResultAsASingleString() throws Exception {
        String value = "a";
        JsonString expected = jsonFactory.create("a");

        JsonRpcResult jsonRpcResult = new JsonRpcResult(value, dtoFactory, jsonFactory);
        JsonValue actual = jsonRpcResult.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToJsonValueWhenPassingParametersWithResultAsAListOfString() throws Exception {
        String value = "a";
        JsonArray expected = jsonFactory.createArray();
        expected.set(0, value);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(singletonList(value), jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcResult.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    // Number

    @Test
    public void shouldGetAsNumberWhenPassingParametersWithResultAsNumber() throws Exception {
        Double expected = 0D;

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected, dtoFactory, jsonFactory);
        Double actual = jsonRpcResult.getAs(Double.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListOfNumberWhenPassingParametersWithResultAsListOfNumber() throws Exception {
        Double expected = 0D;
        JsonNumber string = jsonFactory.create(expected);
        JsonArray array = jsonFactory.createArray();
        array.set(0, string);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(singletonList(expected), jsonFactory, dtoFactory);
        List<Double> actual = jsonRpcResult.getAsListOf(Double.class);

        assertEquals(expected, actual.iterator().next());
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResultAsASingleNumber() throws Exception {
        Double expected = 0D;

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected, dtoFactory, jsonFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, Double.valueOf(actual));
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResultAsAListOfNumber() throws Exception {
        Double value = 0D;
        JsonNumber string = jsonFactory.create(value);
        JsonArray array = jsonFactory.createArray();
        array.set(0, string);
        String expected = array.toJson();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(singletonList(value), jsonFactory, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenPassingParametersWithResultAsASingleNumber() throws Exception {
        Double number = 0D;
        JsonNumber expected = jsonFactory.create(number);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(number, dtoFactory, jsonFactory);
        JsonValue actual = jsonRpcResult.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToJsonValueWhenPassingParametersWithResultAsAListOfNumber() throws Exception {
        Double value = 0D;
        JsonArray expected = jsonFactory.createArray();
        expected.set(0, value);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(singletonList(value), jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcResult.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    //Void

    @Test
    public void shouldGetAsVoidWhenPassingParametersWithResultAsVoid() throws Exception {
        Void expected = null;

        JsonRpcResult jsonRpcResult = new JsonRpcResult(null, dtoFactory, jsonFactory);
        Void actual = jsonRpcResult.getAs(Void.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResultAsASingleVoid() throws Exception {
        JsonObject value = jsonFactory.createObject();
        String expected = value.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(null, dtoFactory, jsonFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }


    @Test
    public void shouldToJsonValueWhenPassingParametersWithResultAsASingleVoid() throws Exception {
        JsonObject expected = jsonFactory.createObject();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(null, dtoFactory, jsonFactory);
        JsonValue actual = jsonRpcResult.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    // Dto

    @Test
    public void shouldGetAsDtoWhenPassingParametersWithResultAsDto() throws Exception {
        JsonRpcResult jsonRpcResult = new JsonRpcResult(dto, dtoFactory, jsonFactory);
        Dto actual = jsonRpcResult.getAs(Dto.class);

        assertEquals(dto, actual);
    }

    @Test
    public void shouldGetAsListOfDtoWhenPassingParametersWithResultAsListOfDto() throws Exception {
        List<Dto> expected = singletonList(dto);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected, jsonFactory, dtoFactory);
        List<Dto> actual = jsonRpcResult.getAsListOf(Dto.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResultAsASingleDto() throws Exception {
        String expected = dto.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(dto, dtoFactory, jsonFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResultAsAListOfDto() throws Exception {
        JsonArray array = jsonFactory.createArray();
        JsonObject value = jsonFactory.parse(dto.toString());
        array.set(0, value);
        String expected = array.toJson();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(singletonList(dto), jsonFactory, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenPassingParametersWithResultAsASingleDto() throws Exception {
        JsonObject expected = jsonFactory.parse(dto.toString());

        JsonRpcResult jsonRpcResult = new JsonRpcResult(dto, dtoFactory, jsonFactory);
        JsonValue actual = jsonRpcResult.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToJsonValueWhenPassingParametersWithResultAsAListOfDto() throws Exception {
        JsonArray expected = jsonFactory.createArray();
        JsonObject value = jsonFactory.parse(dto.toString());
        expected.set(0, value);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(singletonList(dto), jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcResult.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    interface Dto {
        String getParameters();
    }
}
