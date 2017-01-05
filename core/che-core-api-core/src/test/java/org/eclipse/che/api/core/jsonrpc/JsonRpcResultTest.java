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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Tests for {@link JsonRpcResult}
 */
@Listeners(MockitoTestNGListener.class)
public class JsonRpcResultTest {

    JsonParser jsonParser = new JsonParser();

    @Mock
    DtoFactory dtoFactory;
    @Mock
    Dto        dto;

    JsonObject result;

    JsonArray resultList;

    @BeforeMethod
    public void setUp() throws Exception {
        result = new JsonObject();
        resultList = new JsonArray();

        when(dto.getParameters()).thenReturn("value");
        when(dto.toString()).thenReturn("{\"parameter\":\"value\"}");

        when(dtoFactory.createDtoFromJson(dto.toString(), Dto.class)).thenReturn(dto);
    }

    @Test
    public void shouldBeEmptyOrAbsentWhenParsingStringWithEmptyResult() throws Exception {
        JsonRpcResult jsonRpcResult = new JsonRpcResult(result.toString(), jsonParser, dtoFactory);

        assertTrue(jsonRpcResult.isEmptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentWhenParsingStringWithNotEmptyResult() throws Exception {
        result.addProperty("key", "value");

        JsonRpcResult jsonRpcResult = new JsonRpcResult(result.toString(), jsonParser, dtoFactory);

        assertFalse(jsonRpcResult.isEmptyOrAbsent());
    }

    @Test
    public void shouldBeEmptyOrAbsentWhenParsingStringWithEmptyResultList() throws Exception {
        JsonRpcResult jsonRpcResult = new JsonRpcResult(resultList.toString(), jsonParser, dtoFactory);

        assertTrue(jsonRpcResult.isEmptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentWhenParsingStringWithNotEmptyResultList() throws Exception {
        resultList.add(new JsonObject());

        JsonRpcResult jsonRpcResult = new JsonRpcResult(resultList.toString(), jsonParser, dtoFactory);

        assertFalse(jsonRpcResult.isEmptyOrAbsent());
    }

    @Test
    public void shouldBeAnArrayWhenParsingStringWithResultList() throws Exception {
        JsonRpcResult jsonRpcResult = new JsonRpcResult(resultList.toString(), jsonParser, dtoFactory);

        assertTrue(jsonRpcResult.isArray());
    }

    @Test
    public void shouldNotBeAnArrayWhenParsingStringWithSingleResult() throws Exception {
        JsonRpcResult jsonRpcResult = new JsonRpcResult(result.toString(), jsonParser, dtoFactory);

        assertFalse(jsonRpcResult.isArray());
    }

    @Test
    public void shouldGetAsStringWhenParsingStringWithResultAsString() throws Exception {
        JsonPrimitive primitive = new JsonPrimitive("a");
        String expected = primitive.getAsString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(primitive.toString(), jsonParser, dtoFactory);
        String actual = jsonRpcResult.getAs(String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListOfStringWhenParsingStringWithResultAsString() throws Exception {
        String expected = "a";
        JsonPrimitive primitive = new JsonPrimitive(expected);
        JsonArray array = new JsonArray();
        array.add(primitive);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(array.toString(), jsonParser, dtoFactory);
        List<String> actual = jsonRpcResult.getAsListOf(String.class);

        assertEquals(expected, actual.iterator().next());
    }

    @Test
    public void shouldToStringWhenParsingStringWithResultAsASingleString() throws Exception {
        JsonPrimitive primitive = new JsonPrimitive("a");
        String expected = primitive.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(primitive.toString(), jsonParser, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenParsingStringWithResultAsAListOfString() throws Exception {
        JsonPrimitive primitive = new JsonPrimitive("a");
        JsonArray array = new JsonArray();
        array.add(primitive);
        String expected = array.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(array.toString(), jsonParser, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenParsingStringWithResultAsASingleString() throws Exception {
        JsonPrimitive expected = new JsonPrimitive("a");

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected.toString(), jsonParser, dtoFactory);
        JsonElement actual = jsonRpcResult.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenParsingStringWithResultAsAListOfString() throws Exception {
        JsonPrimitive primitive = new JsonPrimitive("a");
        JsonArray expected = new JsonArray();
        expected.add(primitive);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected.toString(), jsonParser, dtoFactory);
        JsonElement actual = jsonRpcResult.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsNumberWhenParsingStringWithResultAsNumber() throws Exception {
        JsonPrimitive value = new JsonPrimitive(0D);
        Double expected = value.getAsDouble();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(value.toString(), jsonParser, dtoFactory);
        Double actual = jsonRpcResult.getAs(Double.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListOfNumberWhenParsingStringWithResultAsListOfNumber() throws Exception {
        Double expected = 0D;
        JsonPrimitive primitive = new JsonPrimitive(expected);
        JsonArray array = new JsonArray();
        array.add(primitive);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(array.toString(), jsonParser, dtoFactory);
        List<Double> actual = jsonRpcResult.getAsListOf(Double.class);

        assertEquals(expected, actual.iterator().next());
    }

    @Test
    public void shouldToStringWhenParsingStringWithResultAsASingleNumber() throws Exception {
        Double expected = 0D;
        JsonPrimitive primitive = new JsonPrimitive(expected);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(primitive.toString(), jsonParser, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, Double.valueOf(actual));
    }

    @Test
    public void shouldToStringWhenParsingStringWithResultAsAListOfNumber() throws Exception {
        JsonPrimitive primitive = new JsonPrimitive(0D);
        JsonArray array = new JsonArray();
        array.add(primitive);
        String expected = array.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(array.toString(), jsonParser, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenParsingStringWithResultAsASingleNumber() throws Exception {
        JsonPrimitive expected = new JsonPrimitive(0D);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected.toString(), jsonParser, dtoFactory);
        JsonElement actual = jsonRpcResult.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenParsingStringWithResultAsAListOfNumber() throws Exception {
        JsonPrimitive primitive = new JsonPrimitive(0D);

        JsonArray expected = new JsonArray();
        expected.add(primitive);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected.toString(), jsonParser, dtoFactory);
        JsonElement actual = jsonRpcResult.toJsonElement();

        assertEquals(expected, actual);
    }

    //Void

    @Test
    public void shouldGetAsVoidWhenParsingStringWithResultAsVoid() throws Exception {
        JsonObject value = new JsonObject();
        Void expected = null;

        JsonRpcResult jsonRpcResult = new JsonRpcResult(value.toString(), jsonParser, dtoFactory);
        Void actual = jsonRpcResult.getAs(Void.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenParsingStringWithResultAsASingleVoid() throws Exception {
        JsonObject value = new JsonObject();
        String expected = value.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(value.toString(), jsonParser, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenParsingStringWithResultAsASingleVoid() throws Exception {
        JsonObject expected = new JsonObject();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected.toString(), jsonParser, dtoFactory);
        JsonElement actual = jsonRpcResult.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsDtoWhenParsingStringWithResultAsDto() throws Exception {
        JsonRpcResult jsonRpcResult = new JsonRpcResult(dto.toString(), jsonParser, dtoFactory);
        Dto actual = jsonRpcResult.getAs(Dto.class);

        assertEquals(dto, actual);
    }

    @Test
    public void shouldGetAsListOfDtoWhenParsingStringWithResultAsListOfDto() throws Exception {
        JsonArray array = new JsonArray();
        JsonObject value = jsonParser.parse(dto.toString()).getAsJsonObject();
        array.add(value);
        List<Dto> expected = singletonList(dto);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(array.toString(), jsonParser, dtoFactory);
        List<Dto> actual = jsonRpcResult.getAsListOf(Dto.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenParsingStringWithResultAsASingleDto() throws Exception {
        String expected = dto.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(dto.toString(), jsonParser, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenParsingStringWithResultAsAListOfDto() throws Exception {
        JsonArray array = new JsonArray();
        JsonObject value = jsonParser.parse(dto.toString()).getAsJsonObject();
        array.add(value);
        String expected = array.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(array.toString(), jsonParser, dtoFactory);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenParsingStringWithResultAsASingleDto() throws Exception {
        JsonObject value = jsonParser.parse(dto.toString()).getAsJsonObject();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(dto.toString(), jsonParser, dtoFactory);
        JsonElement actual = jsonRpcResult.toJsonElement();

        assertEquals(value, actual);
    }

    @Test
    public void shouldToJsonValueWhenParsingStringWithResultAsAListOfDto() throws Exception {
        JsonArray expected = new JsonArray();
        JsonObject value = jsonParser.parse(dto.toString()).getAsJsonObject();
        expected.add(value);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected.toString(), jsonParser, dtoFactory);
        JsonElement actual = jsonRpcResult.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsStringWhenPassingParametersWithResultAsString() throws Exception {
        String expected = "a";

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected, jsonParser, dtoFactory);
        String actual = jsonRpcResult.getAs(String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListOfStringWhenPassingParametersWithResultAsString() throws Exception {
        List<String> expected = singletonList("a");

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected, jsonParser, dtoFactory);
        List<String> actual = jsonRpcResult.getAsListOf(String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResultAsASingleString() throws Exception {
        String value = "a";
        String expected = jsonParser.parse(value).toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(value, dtoFactory, jsonParser);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResultAsAListOfString() throws Exception {
        String value = "a";
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(value));
        String expected = array.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(singletonList(value), dtoFactory, jsonParser);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenPassingParametersWithResultAsASingleString() throws Exception {
        String value = "a";
        JsonPrimitive expected = new JsonPrimitive(value);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(value, dtoFactory, jsonParser);
        JsonElement actual = jsonRpcResult.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenPassingParametersWithResultAsAListOfString() throws Exception {
        String value = "a";
        JsonArray expected = new JsonArray();
        expected.add(new JsonPrimitive(value));

        JsonRpcResult jsonRpcResult = new JsonRpcResult(singletonList(value), dtoFactory, jsonParser);
        JsonElement actual = jsonRpcResult.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsNumberWhenPassingParametersWithResultAsNumber() throws Exception {
        Double expected = 0D;

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected, dtoFactory, jsonParser);
        Double actual = jsonRpcResult.getAs(Double.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListOfNumberWhenPassingParametersWithResultAsListOfNumber() throws Exception {
        Double expected = 0D;
        JsonPrimitive primitive = new JsonPrimitive(expected);
        JsonArray array = new JsonArray();
        array.add(primitive);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(singletonList(expected), jsonParser, dtoFactory);
        List<Double> actual = jsonRpcResult.getAsListOf(Double.class);

        assertEquals(expected, actual.iterator().next());
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResultAsASingleNumber() throws Exception {
        Double expected = 0D;

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected, dtoFactory, jsonParser);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, Double.valueOf(actual));
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResultAsAListOfNumber() throws Exception {
        Double value = 0D;
        JsonPrimitive primitive = new JsonPrimitive(value);
        JsonArray array = new JsonArray();
        array.add(primitive);
        String expected = array.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(singletonList(value), dtoFactory, jsonParser);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenPassingParametersWithResultAsASingleNumber() throws Exception {
        Double value = 0D;
        JsonPrimitive expected = new JsonPrimitive(value);


        JsonRpcResult jsonRpcResult = new JsonRpcResult(value, dtoFactory, jsonParser);
        JsonElement actual = jsonRpcResult.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenPassingParametersWithResultAsAListOfNumber() throws Exception {
        Double value = 0D;
        JsonPrimitive primitive = new JsonPrimitive(value);
        JsonArray expected = new JsonArray();
        expected.add(primitive);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(singletonList(value), dtoFactory, jsonParser);
        JsonElement actual = jsonRpcResult.toJsonElement();

        assertEquals(expected, (actual));
    }

    @Test
    public void shouldGetAsVoidWhenPassingParametersWithResultAsVoid() throws Exception {
        Void expected = null;

        JsonRpcResult jsonRpcResult = new JsonRpcResult(null, dtoFactory, jsonParser);
        Void actual = jsonRpcResult.getAs(Void.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResultAsASingleVoid() throws Exception {
        JsonObject value = new JsonObject();
        String expected = value.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(null, dtoFactory, jsonParser);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }


    @Test
    public void shouldToJsonValueWhenPassingParametersWithResultAsASingleVoid() throws Exception {
        JsonObject expected = new JsonObject();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(null, dtoFactory, jsonParser);
        JsonElement actual = jsonRpcResult.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsDtoWhenPassingParametersWithResultAsDto() throws Exception {
        JsonRpcResult jsonRpcResult = new JsonRpcResult(dto, dtoFactory, jsonParser);
        Dto actual = jsonRpcResult.getAs(Dto.class);

        assertEquals(dto, actual);
    }

    @Test
    public void shouldGetAsListOfDtoWhenPassingParametersWithResultAsListOfDto() throws Exception {
        List<Dto> expected = singletonList(dto);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(expected, jsonParser, dtoFactory);
        List<Dto> actual = jsonRpcResult.getAsListOf(Dto.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResultAsASingleDto() throws Exception {
        String expected = dto.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(dto, dtoFactory, jsonParser);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringWhenPassingParametersWithResultAsAListOfDto() throws Exception {
        JsonArray array = new JsonArray();
        JsonObject value = jsonParser.parse(dto.toString()).getAsJsonObject();
        array.add(value);
        String expected = array.toString();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(singletonList(dto), dtoFactory, jsonParser);
        String actual = jsonRpcResult.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenPassingParametersWithResultAsASingleDto() throws Exception {
        JsonObject expected = jsonParser.parse(dto.toString()).getAsJsonObject();

        JsonRpcResult jsonRpcResult = new JsonRpcResult(dto, dtoFactory, jsonParser);
        JsonElement actual = jsonRpcResult.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueWhenPassingParametersWithResultAsAListOfDto() throws Exception {
        JsonArray expected = new JsonArray();
        JsonObject value = jsonParser.parse(dto.toString()).getAsJsonObject();
        expected.add(value);

        JsonRpcResult jsonRpcResult = new JsonRpcResult(singletonList(dto), dtoFactory, jsonParser);
        JsonElement actual = jsonRpcResult.toJsonElement();

        assertEquals(expected, actual);
    }

    interface Dto {
        String getParameters();
    }
}
