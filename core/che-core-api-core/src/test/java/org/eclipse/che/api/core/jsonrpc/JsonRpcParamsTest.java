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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Tests for {@link JsonRpcParams}
 */
@Listeners(MockitoTestNGListener.class)
public class JsonRpcParamsTest {
    final String DTO_JSON  = "{\"parameter\":\"value\"}";
    final String DTO_VALUE = "value";

    JsonParser jsonParser = new JsonParser();

    @Mock
    DtoFactory dtoFactory;
    @Mock
    Dto        dto;

    @BeforeMethod
    public void setUp() throws Exception {
        when(dto.getParameter()).thenReturn(DTO_VALUE);
        when(dto.toString()).thenReturn(DTO_JSON);

        when(dtoFactory.createDtoFromJson(anyString(), eq(Dto.class))).thenReturn(dto);
    }

    @Test
    public void shouldGetAsForParsedSingleStringParams() throws Exception {
        String expected = "value";

        JsonRpcParams jsonRpcParams = new JsonRpcParams('"' + expected + '"', jsonParser, dtoFactory);
        String actual = jsonRpcParams.getAs(String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueForParsedSingleStringParams() throws Exception {
        String expected = "\"value\"";

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, jsonParser, dtoFactory);
        JsonElement element = jsonRpcParams.toJsonElement();
        String actual = element.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringForParsedSingleStringParams() throws Exception {
        String expected = "\"value\"";

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, jsonParser, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListForParsedListStringParams() throws Exception {
        String expected = "value";

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[\"" + expected + "\"]", jsonParser, dtoFactory);
        List<String> actual = jsonRpcParams.getAsListOf(String.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToJsonValueForParsedListStringParams() throws Exception {
        String value = "value";
        JsonArray expected = new JsonArray();
        expected.add(new JsonPrimitive(value));

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[\"" + value + "\"]", jsonParser, dtoFactory);
        JsonElement actual = jsonRpcParams.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringForParsedListStringParams() throws Exception {
        String value = "value";
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(value));
        String expected = array.toString();

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[\"" + value + "\"]", jsonParser, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsForCreatedSingleStringParams() throws Exception {
        String expected = "value";

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, jsonParser, dtoFactory);
        String actual = jsonRpcParams.getAs(String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonForCreatedSingleStringParams() throws Exception {
        String value = "value";
        String expected = "\"" + value + "\"";

        JsonRpcParams jsonRpcParams = new JsonRpcParams(value, jsonParser, dtoFactory);
        JsonElement element = jsonRpcParams.toJsonElement();
        String actual = element.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringCreatedSingleStringParams() throws Exception {
        String value = "value";
        String expected = "\"" + value + "\"";

        JsonRpcParams jsonRpcParams = new JsonRpcParams(value, jsonParser, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListForCreatedListStringParams() throws Exception {
        List<String> expected = singletonList("value");

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, jsonParser, dtoFactory);
        List<String> actual = jsonRpcParams.getAsListOf(String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonForCreatedListStringParams() throws Exception {
        String value = "value";
        JsonArray expected = new JsonArray();
        expected.add(new JsonPrimitive(value));

        JsonRpcParams jsonRpcParams = new JsonRpcParams(singletonList(value), jsonParser, dtoFactory);
        JsonElement actual = jsonRpcParams.toJsonElement();

        assertEquals(expected, actual);
    }


    @Test
    public void shouldToStringCreatedListStringParams() throws Exception {
        String value = "value";
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(value));
        String expected = array.toString();

        JsonRpcParams jsonRpcParams = new JsonRpcParams(singletonList(value), jsonParser, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsForParsedSingleDoubleParams() throws Exception {
        Double expected = 0D;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected.toString(), jsonParser, dtoFactory);
        Double actual = jsonRpcParams.getAs(Double.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueForParsedSingleDoubleParams() throws Exception {
        Double expected = 0D;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected.toString(), jsonParser, dtoFactory);
        JsonElement actual = jsonRpcParams.toJsonElement();

        assertEquals(expected, Double.valueOf(actual.toString()));
    }

    @Test
    public void shouldToStringForParsedSingleDoubleParams() throws Exception {
        Double expected = 0D;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected.toString(), jsonParser, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, Double.valueOf(actual));
    }

    @Test
    public void shouldGetAsListForParsedListDoubleParams() throws Exception {
        Double expected = 0D;

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[\"" + expected + "\"]", jsonParser, dtoFactory);
        List<Double> actual = jsonRpcParams.getAsListOf(Double.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToJsonValueForParsedListDoubleParams() throws Exception {
        Double expected = 0D;
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(expected));

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + expected + "]", jsonParser, dtoFactory);
        JsonElement jsonValue = jsonRpcParams.toJsonElement();

        assertEquals(array, jsonValue);
    }

    @Test
    public void shouldToStringForParsedListDoubleParams() throws Exception {
        Double value = 0D;
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(value));
        String expected = array.toString();

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + value + "]", jsonParser, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsForCreatedSingleDoubleParams() throws Exception {
        Double expected = 0D;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, dtoFactory, jsonParser);
        Double actual = jsonRpcParams.getAs(Double.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonForCreatedSingleDoubleParams() throws Exception {
        Double expected = 0D;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, dtoFactory, jsonParser);
        JsonElement actual = jsonRpcParams.toJsonElement();

        assertEquals(expected, Double.valueOf(actual.toString()));
    }

    @Test
    public void shouldToStringCreatedSingleDoubleParams() throws Exception {
        Double expected = 0D;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, dtoFactory, jsonParser);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, Double.valueOf(actual));
    }

    @Test
    public void shouldGetAsListForCreatedListDoubleParams() throws Exception {
        List<Double> expected = singletonList(0D);

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, jsonParser, dtoFactory);
        List<Double> actual = jsonRpcParams.getAsListOf(Double.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonForCreatedListDoubleParams() throws Exception {
        double value = 0D;
        List<Double> list = singletonList(value);
        JsonArray expected = new JsonArray();
        expected.add(new JsonPrimitive(value));

        JsonRpcParams jsonRpcParams = new JsonRpcParams(list, jsonParser, dtoFactory);
        JsonElement actual = jsonRpcParams.toJsonElement();

        assertEquals(expected, actual);
    }


    @Test
    public void shouldToStringCreatedListDoubleParams() throws Exception {
        double value = 0D;
        List<Double> list = singletonList(value);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(new JsonPrimitive(value));
        String expected = jsonArray.toString();

        JsonRpcParams jsonRpcParams = new JsonRpcParams(list, jsonParser, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsForParsedSingleBooleanParams() throws Exception {
        Boolean expected = false;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected.toString(), jsonParser, dtoFactory);
        Boolean actual = jsonRpcParams.getAs(Boolean.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueForParsedSingleBooleanParams() throws Exception {
        Boolean expected = false;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected.toString(), jsonParser, dtoFactory);
        JsonElement actual = jsonRpcParams.toJsonElement();

        assertEquals(expected, Boolean.valueOf(actual.toString()));
    }

    @Test
    public void shouldToStringForParsedSingleBooleanParams() throws Exception {
        Boolean expected = false;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected.toString(), jsonParser, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, Boolean.valueOf(actual));
    }

    @Test
    public void shouldGetAsListForParsedListBooleanParams() throws Exception {
        Boolean expected = false;

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + expected + "]", jsonParser, dtoFactory);
        List<Boolean> actual = jsonRpcParams.getAsListOf(Boolean.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToJsonValueForParsedListBooleanParams() throws Exception {
        Boolean expected = false;
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(expected));

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + expected + "]", jsonParser, dtoFactory);
        JsonElement jsonValue = jsonRpcParams.toJsonElement();

        assertEquals(array, jsonValue);
    }

    @Test
    public void shouldToStringForParsedListBooleanParams() throws Exception {
        Boolean value = false;
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(value));
        String expected = array.toString();

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + value + "]", jsonParser, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsForCreatedSingleBooleanParams() throws Exception {
        Boolean expected = false;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected.toString(), jsonParser, dtoFactory);
        Boolean actual = jsonRpcParams.getAs(Boolean.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonForCreatedSingleBooleanParams() throws Exception {
        Boolean expected = false;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, dtoFactory, jsonParser);
        JsonElement actual = jsonRpcParams.toJsonElement();

        assertEquals(expected, Boolean.valueOf(actual.toString()));
    }

    @Test
    public void shouldToStringCreatedSingleBooleanParams() throws Exception {
        Boolean expected = false;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, dtoFactory, jsonParser);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, Boolean.valueOf(actual));
    }

    @Test
    public void shouldGetAsListForCreatedListBooleanParams() throws Exception {
        List<Boolean> expected = singletonList(false);

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, jsonParser, dtoFactory);
        List<Boolean> actual = jsonRpcParams.getAsListOf(Boolean.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonForCreatedListBooleanParams() throws Exception {
        Boolean value = false;
        List<Boolean> list = singletonList(value);
        JsonArray expected = new JsonArray();
        expected.add(new JsonPrimitive(value));

        JsonRpcParams jsonRpcParams = new JsonRpcParams(list, jsonParser, dtoFactory);
        JsonElement actual = jsonRpcParams.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringCreatedListBooleanParams() throws Exception {
        Boolean value = false;
        List<Boolean> list = singletonList(value);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(new JsonPrimitive(value));
        String expected = jsonArray.toString();

        JsonRpcParams jsonRpcParams = new JsonRpcParams(list, jsonParser, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsForParsedSingleDtoParams() throws Exception {
        String expected = DTO_JSON;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(DTO_JSON, dtoFactory, jsonParser);
        String actual = jsonRpcParams.getAs(Dto.class).toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueForParsedSingleDtoParams() throws Exception {
        JsonObject expected = jsonParser.parse(DTO_JSON).getAsJsonObject();

        JsonRpcParams jsonRpcParams = new JsonRpcParams(DTO_JSON, dtoFactory, jsonParser);
        JsonElement actual = jsonRpcParams.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringForParsedSingleDtoParams() throws Exception {
        String expected = jsonParser.parse(DTO_JSON).toString();

        JsonRpcParams jsonRpcParams = new JsonRpcParams(DTO_JSON, dtoFactory, jsonParser);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListForParsedListDtoParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + DTO_JSON + "]", jsonParser, dtoFactory);
        List<Dto> actual = jsonRpcParams.getAsListOf(Dto.class);

        assertEquals(singletonList(dto), actual);
    }

    @Test
    public void shouldToJsonValueForParsedListDtoParams() throws Exception {
        JsonArray array = new JsonArray();
        JsonObject jsonObject = jsonParser.parse(DTO_JSON).getAsJsonObject();
        array.add(jsonObject);

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + DTO_JSON + "]", jsonParser, dtoFactory);
        JsonElement jsonValue = jsonRpcParams.toJsonElement();

        assertEquals(array, jsonValue);
    }

    @Test
    public void shouldToStringForParsedListDtoParams() throws Exception {
        JsonArray array = new JsonArray();
        JsonObject jsonObject = jsonParser.parse(DTO_JSON).getAsJsonObject();
        array.add(jsonObject);
        String expected = array.toString();

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + DTO_JSON + "]", jsonParser, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsForCreatedSingleDtoParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams(dto, dtoFactory, jsonParser);
        Dto actual = jsonRpcParams.getAs(Dto.class);

        assertEquals(dto, actual);
    }

    @Test
    public void shouldToJsonForCreatedSingleDtoParams() throws Exception {
        JsonObject expected = jsonParser.parse(dto.toString()).getAsJsonObject();
        JsonRpcParams jsonRpcParams = new JsonRpcParams(dto, dtoFactory, jsonParser);
        JsonElement actual = jsonRpcParams.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringCreatedSingleDtoParams() throws Exception {
        String expected = jsonParser.parse(dto.toString()).toString();

        JsonRpcParams jsonRpcParams = new JsonRpcParams(dto, dtoFactory, jsonParser);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListForCreatedListDtoParams() throws Exception {
        List<Dto> list = singletonList(dto);

        JsonRpcParams jsonRpcParams = new JsonRpcParams(list, jsonParser, dtoFactory);
        List<Dto> actual = jsonRpcParams.getAsListOf(Dto.class);

        assertEquals(list, actual);
    }

    @Test
    public void shouldToJsonForCreatedListDtoParams() throws Exception {
        List<Dto> list = singletonList(dto);

        JsonArray expected = new JsonArray();
        JsonElement element = jsonParser.parse(dto.toString());
        expected.add(element);

        JsonRpcParams jsonRpcParams = new JsonRpcParams(list, jsonParser, dtoFactory);
        JsonElement actual = jsonRpcParams.toJsonElement();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToStringCreatedListDtoParams() throws Exception {
        List<Dto> list = singletonList(dto);

        JsonArray jsonArray = new JsonArray();
        JsonElement jsonValue = jsonParser.parse(dto.toString());
        jsonArray.add(jsonValue);
        String expected = jsonArray.toString();

        JsonRpcParams jsonRpcParams = new JsonRpcParams(list, jsonParser, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldNotBeEmptyOrAbsentForStringParsedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams("\"a\"", jsonParser, dtoFactory);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentForBooleanParsedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams("false", jsonParser, dtoFactory);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }


    @Test
    public void shouldNotBeEmptyOrAbsentForNumberParsedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams("0", jsonParser, dtoFactory);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentForDtoParsedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams(DTO_JSON, jsonParser, dtoFactory);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldBeEmptyOrAbsentForEmptyParsedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams("{}", jsonParser, dtoFactory);

        assertTrue(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentForStringCreatedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams("a", dtoFactory, jsonParser);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentForBooleanCreatedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams(false, dtoFactory, jsonParser);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentForNumberCreatedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams(0D, dtoFactory, jsonParser);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentForDtoCreatedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams(dto, dtoFactory, jsonParser);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldBeEmptyOrAbsentForEmptyCreatedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams(null, dtoFactory, jsonParser);

        assertTrue(jsonRpcParams.emptyOrAbsent());
    }

    interface Dto {
        String getParameter();
    }
}
