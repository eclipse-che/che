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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link JsonRpcParams}
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonRpcParamsTest {
    final String DTO_JSON  = "{\"parameter\":\"value\"}";
    final String DTO_VALUE = "value";

    JsonFactory jsonFactory = Json.instance();

    @Mock
    DtoFactory dtoFactory;
    @Mock
    Dto        dto;

    @Before
    public void setUp() throws Exception {
        when(dto.getParameter()).thenReturn(DTO_VALUE);
        when(dto.toString()).thenReturn(DTO_JSON);

        when(dtoFactory.createDtoFromJson(anyString(), eq(Dto.class))).thenReturn(dto);
    }

    @Test
    public void shouldGetAsForParsedSingleStringParams() throws Exception {
        String expected = "value";

        JsonRpcParams jsonRpcParams = new JsonRpcParams('"' + expected + '"', jsonFactory, dtoFactory);
        String actual = jsonRpcParams.getAs(String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueForParsedSingleStringParams() throws Exception {
        String expected = "\"value\"";

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcParams.toJsonValue();

        assertEquals(expected, actual.toJson());
    }

    @Test
    public void shouldToStringForParsedSingleStringParams() throws Exception {
        String expected = "\"value\"";

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, jsonFactory, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListForParsedListStringParams() throws Exception {
        String expected = "value";

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[\"" + expected + "\"]", jsonFactory, dtoFactory);
        List<String> actual = jsonRpcParams.getAsListOf(String.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToJsonValueForParsedListStringParams() throws Exception {
        String value = "value";
        JsonArray expected = jsonFactory.createArray();
        expected.set(0, value);

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[\"" + value + "\"]", jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcParams.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringForParsedListStringParams() throws Exception {
        String value = "value";
        JsonArray expected = jsonFactory.createArray();
        expected.set(0, value);

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[\"" + value + "\"]", jsonFactory, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected.toJson(), actual);
    }

    @Test
    public void shouldGetAsForCreatedSingleStringParams() throws Exception {
        String expected = "value";

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, dtoFactory, jsonFactory);
        String actual = jsonRpcParams.getAs(String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonForCreatedSingleStringParams() throws Exception {
        String value = "value";
        String expected = "\"" + value + "\"";

        JsonRpcParams jsonRpcParams = new JsonRpcParams(value, dtoFactory, jsonFactory);
        JsonValue actual = jsonRpcParams.toJsonValue();

        assertEquals(expected, actual.toJson());
    }

    @Test
    public void shouldToStringCreatedSingleStringParams() throws Exception {
        String value = "value";
        String expected = "\"" + value + "\"";

        JsonRpcParams jsonRpcParams = new JsonRpcParams(value, dtoFactory, jsonFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListForCreatedListStringParams() throws Exception {
        List<String> expected = singletonList("value");

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, jsonFactory, dtoFactory);
        List<String> actual = jsonRpcParams.getAsListOf(String.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonForCreatedListStringParams() throws Exception {
        String value = "value";
        JsonArray expected = jsonFactory.createArray();
        expected.set(0, value);

        JsonRpcParams jsonRpcParams = new JsonRpcParams(singletonList(value), jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcParams.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }


    @Test
    public void shouldToStringCreatedListStringParams() throws Exception {
        String value = "value";
        JsonArray expected = jsonFactory.createArray();
        expected.set(0, value);

        JsonRpcParams jsonRpcParams = new JsonRpcParams(singletonList(value), jsonFactory, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected.toJson(), actual);
    }

    @Test
    public void shouldGetAsForParsedSingleDoubleParams() throws Exception {
        Double expected = 0D;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected.toString(), jsonFactory, dtoFactory);
        Double actual = jsonRpcParams.getAs(Double.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueForParsedSingleDoubleParams() throws Exception {
        Double expected = 0D;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected.toString(), jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcParams.toJsonValue();

        assertEquals(expected, Double.valueOf(actual.toJson()));
    }

    @Test
    public void shouldToStringForParsedSingleDoubleParams() throws Exception {
        Double expected = 0D;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected.toString(), jsonFactory, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, Double.valueOf(actual));
    }

    @Test
    public void shouldGetAsListForParsedListDoubleParams() throws Exception {
        Double expected = 0D;

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[\"" + expected + "\"]", jsonFactory, dtoFactory);
        List<Double> actual = jsonRpcParams.getAsListOf(Double.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToJsonValueForParsedListDoubleParams() throws Exception {
        Double expected = 0D;
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + expected + "]", jsonFactory, dtoFactory);
        JsonValue jsonValue = jsonRpcParams.toJsonValue();

        assertTrue(array.jsEquals(jsonValue));
    }

    @Test
    public void shouldToStringForParsedListDoubleParams() throws Exception {
        Double value = 0D;
        JsonArray array = jsonFactory.createArray();
        array.set(0, value);
        String expected = array.toJson();

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + value + "]", jsonFactory, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsForCreatedSingleDoubleParams() throws Exception {
        Double expected = 0D;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, dtoFactory, jsonFactory);
        Double actual = jsonRpcParams.getAs(Double.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonForCreatedSingleDoubleParams() throws Exception {
        Double expected = 0D;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, dtoFactory, jsonFactory);
        JsonValue actual = jsonRpcParams.toJsonValue();

        assertEquals(expected, Double.valueOf(actual.toJson()));
    }

    @Test
    public void shouldToStringCreatedSingleDoubleParams() throws Exception {
        Double expected = 0D;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, dtoFactory, jsonFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, Double.valueOf(actual));
    }

    @Test
    public void shouldGetAsListForCreatedListDoubleParams() throws Exception {
        List<Double> expected = singletonList(0D);

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, jsonFactory, dtoFactory);
        List<Double> actual = jsonRpcParams.getAsListOf(Double.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonForCreatedListDoubleParams() throws Exception {
        double value = 0D;
        List<Double> list = singletonList(value);
        JsonArray expected = jsonFactory.createArray();
        expected.set(0, value);

        JsonRpcParams jsonRpcParams = new JsonRpcParams(list, jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcParams.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }


    @Test
    public void shouldToStringCreatedListDoubleParams() throws Exception {
        double value = 0D;
        List<Double> list = singletonList(value);
        JsonArray jsonArray = jsonFactory.createArray();
        jsonArray.set(0, value);
        String expected = jsonArray.toJson();

        JsonRpcParams jsonRpcParams = new JsonRpcParams(list, jsonFactory, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsForParsedSingleBooleanParams() throws Exception {
        Boolean expected = false;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected.toString(), jsonFactory, dtoFactory);
        Boolean actual = jsonRpcParams.getAs(Boolean.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueForParsedSingleBooleanParams() throws Exception {
        Boolean expected = false;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected.toString(), jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcParams.toJsonValue();

        assertEquals(expected, Boolean.valueOf(actual.toJson()));
    }

    @Test
    public void shouldToStringForParsedSingleBooleanParams() throws Exception {
        Boolean expected = false;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected.toString(), jsonFactory, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, Boolean.valueOf(actual));
    }

    @Test
    public void shouldGetAsListForParsedListBooleanParams() throws Exception {
        Boolean expected = false;

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + expected + "]", jsonFactory, dtoFactory);
        List<Boolean> actual = jsonRpcParams.getAsListOf(Boolean.class);

        assertEquals(singletonList(expected), actual);
    }

    @Test
    public void shouldToJsonValueForParsedListBooleanParams() throws Exception {
        Boolean expected = false;
        JsonArray array = jsonFactory.createArray();
        array.set(0, expected);

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + expected + "]", jsonFactory, dtoFactory);
        JsonValue jsonValue = jsonRpcParams.toJsonValue();

        assertTrue(array.jsEquals(jsonValue));
    }

    @Test
    public void shouldToStringForParsedListBooleanParams() throws Exception {
        Boolean value = false;
        JsonArray array = jsonFactory.createArray();
        array.set(0, value);
        String expected = array.toJson();

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + value + "]", jsonFactory, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsForCreatedSingleBooleanParams() throws Exception {
        Boolean expected = false;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, dtoFactory, jsonFactory);
        Boolean actual = jsonRpcParams.getAs(Boolean.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonForCreatedSingleBooleanParams() throws Exception {
        Boolean expected = false;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, dtoFactory, jsonFactory);
        JsonValue actual = jsonRpcParams.toJsonValue();

        assertEquals(expected, Boolean.valueOf(actual.toJson()));
    }

    @Test
    public void shouldToStringCreatedSingleBooleanParams() throws Exception {
        Boolean expected = false;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, dtoFactory, jsonFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, Boolean.valueOf(actual));
    }

    @Test
    public void shouldGetAsListForCreatedListBooleanParams() throws Exception {
        List<Boolean> expected = singletonList(false);

        JsonRpcParams jsonRpcParams = new JsonRpcParams(expected, jsonFactory, dtoFactory);
        List<Boolean> actual = jsonRpcParams.getAsListOf(Boolean.class);

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonForCreatedListBooleanParams() throws Exception {
        Boolean value = false;
        List<Boolean> list = singletonList(value);
        JsonArray expected = jsonFactory.createArray();
        expected.set(0, value);

        JsonRpcParams jsonRpcParams = new JsonRpcParams(list, jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcParams.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringCreatedListBooleanParams() throws Exception {
        Boolean value = false;
        List<Boolean> list = singletonList(value);
        JsonArray jsonArray = jsonFactory.createArray();
        jsonArray.set(0, value);
        String expected = jsonArray.toJson();

        JsonRpcParams jsonRpcParams = new JsonRpcParams(list, jsonFactory, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsForParsedSingleDtoParams() throws Exception {
        String expected = DTO_JSON;

        JsonRpcParams jsonRpcParams = new JsonRpcParams(DTO_JSON, dtoFactory, jsonFactory);
        String actual = jsonRpcParams.getAs(Dto.class).toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldToJsonValueForParsedSingleDtoParams() throws Exception {
        JsonString expected = jsonFactory.create(DTO_JSON);

        JsonRpcParams jsonRpcParams = new JsonRpcParams(DTO_JSON, dtoFactory, jsonFactory);
        JsonValue actual = jsonRpcParams.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringForParsedSingleDtoParams() throws Exception {
        String expected = jsonFactory.create(DTO_JSON).toJson();

        JsonRpcParams jsonRpcParams = new JsonRpcParams(DTO_JSON, dtoFactory, jsonFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListForParsedListDtoParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + DTO_JSON + "]", jsonFactory, dtoFactory);
        List<Dto> actual = jsonRpcParams.getAsListOf(Dto.class);

        assertEquals(singletonList(dto), actual);
    }

    @Test
    public void shouldToJsonValueForParsedListDtoParams() throws Exception {
        JsonArray array = jsonFactory.createArray();
        JsonObject jsonObject = jsonFactory.parse(DTO_JSON);
        array.set(0, jsonObject);

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + DTO_JSON + "]", jsonFactory, dtoFactory);
        JsonValue jsonValue = jsonRpcParams.toJsonValue();

        assertEquals(array.toJson(), jsonValue.toJson());
        assertTrue(array.jsEquals(jsonValue));
    }

    @Test
    public void shouldToStringForParsedListDtoParams() throws Exception {
        JsonArray array = jsonFactory.createArray();
        JsonObject jsonObject = jsonFactory.parse(DTO_JSON);
        array.set(0, jsonObject);
        String expected = array.toJson();

        JsonRpcParams jsonRpcParams = new JsonRpcParams("[" + DTO_JSON + "]", jsonFactory, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsForCreatedSingleDtoParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams(dto, dtoFactory, jsonFactory);
        Dto actual = jsonRpcParams.getAs(Dto.class);

        assertEquals(dto, actual);
    }

    @Test
    public void shouldToJsonForCreatedSingleDtoParams() throws Exception {
        JsonValue expected = jsonFactory.parse(dto.toString());
        JsonRpcParams jsonRpcParams = new JsonRpcParams(dto, dtoFactory, jsonFactory);
        JsonValue actual = jsonRpcParams.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringCreatedSingleDtoParams() throws Exception {
        String expected = jsonFactory.parse(dto.toString()).toJson();

        JsonRpcParams jsonRpcParams = new JsonRpcParams(dto, dtoFactory, jsonFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldGetAsListForCreatedListDtoParams() throws Exception {
        List<Dto> list = singletonList(dto);

        JsonRpcParams jsonRpcParams = new JsonRpcParams(list, jsonFactory, dtoFactory);
        List<Dto> actual = jsonRpcParams.getAsListOf(Dto.class);

        assertEquals(list, actual);
    }

    @Test
    public void shouldToJsonForCreatedListDtoParams() throws Exception {
        List<Dto> list = singletonList(dto);

        JsonArray expected = jsonFactory.createArray();
        JsonValue jsonValue = jsonFactory.parse(dto.toString());
        expected.set(0, jsonValue);

        JsonRpcParams jsonRpcParams = new JsonRpcParams(list, jsonFactory, dtoFactory);
        JsonValue actual = jsonRpcParams.toJsonValue();

        assertTrue(expected.jsEquals(actual));
    }

    @Test
    public void shouldToStringCreatedListDtoParams() throws Exception {
        List<Dto> list = singletonList(dto);

        JsonArray jsonArray = jsonFactory.createArray();
        JsonValue jsonValue = jsonFactory.parse(dto.toString());
        jsonArray.set(0, jsonValue);
        String expected = jsonArray.toJson();

        JsonRpcParams jsonRpcParams = new JsonRpcParams(list, jsonFactory, dtoFactory);
        String actual = jsonRpcParams.toString();

        assertEquals(expected, actual);
    }

    @Test
    public void shouldNotBeEmptyOrAbsentForStringParsedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams("\"a\"", jsonFactory, dtoFactory);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentForBooleanParsedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams("false", jsonFactory, dtoFactory);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }


    @Test
    public void shouldNotBeEmptyOrAbsentForNumberParsedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams("0", jsonFactory, dtoFactory);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentForDtoParsedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams(DTO_JSON, jsonFactory, dtoFactory);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldBeEmptyOrAbsentForEmptyParsedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams("{}", jsonFactory, dtoFactory);

        assertTrue(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentForStringCreatedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams("a", dtoFactory, jsonFactory);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentForBooleanCreatedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams(false, dtoFactory, jsonFactory);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentForNumberCreatedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams(0D, dtoFactory, jsonFactory);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldNotBeEmptyOrAbsentForDtoCreatedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams(dto, dtoFactory, jsonFactory);

        assertFalse(jsonRpcParams.emptyOrAbsent());
    }

    @Test
    public void shouldBeEmptyOrAbsentForEmptyCreatedParams() throws Exception {
        JsonRpcParams jsonRpcParams = new JsonRpcParams(null, dtoFactory, jsonFactory);

        assertTrue(jsonRpcParams.emptyOrAbsent());
    }

    interface Dto {
        String getParameter();
    }
}
