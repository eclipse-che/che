/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.dto;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.dto.definitions.ComplicatedDto;
import org.eclipse.che.dto.definitions.DTOHierarchy;
import org.eclipse.che.dto.definitions.DTOHierarchy.GrandchildDto;
import org.eclipse.che.dto.definitions.DtoWithAny;
import org.eclipse.che.dto.definitions.DtoWithDelegate;
import org.eclipse.che.dto.definitions.DtoWithFieldNames;
import org.eclipse.che.dto.definitions.DtoWithSerializable;
import org.eclipse.che.dto.definitions.SimpleDto;
import org.eclipse.che.dto.definitions.model.Model;
import org.eclipse.che.dto.definitions.model.ModelComponentDto;
import org.eclipse.che.dto.definitions.model.ModelDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests that the interfaces specified in org.eclipse.che.dto.definitions have corresponding
 * generated server implementations.
 *
 * @author Artem Zatsarynnyi
 */
public class ServerDtoTest {

  protected static final DtoFactory dtoFactory = DtoFactory.getInstance();

  @Test
  public void testCreateSimpleDto() throws Exception {
    final String fooString = "Something";
    final int fooId = 1;
    final String _default = "test_default_keyword";

    SimpleDto dto =
        dtoFactory
            .createDto(SimpleDto.class)
            .withName(fooString)
            .withId(fooId)
            .withDefault(_default);

    // Check to make sure things are in a sane state.
    checkSimpleDto(dto, fooString, fooId, _default);
  }

  @Test
  public void testSimpleDtoSerializer() throws Exception {
    final String fooString = "Something";
    final int fooId = 1;
    final String _default = "test_default_keyword";

    SimpleDto dto =
        dtoFactory
            .createDto(SimpleDto.class)
            .withName(fooString)
            .withId(fooId)
            .withDefault(_default);
    final String json = dtoFactory.toJson(dto);

    JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
    assertEquals(jsonObject.get("name").getAsString(), fooString);
    assertEquals(jsonObject.get("id").getAsInt(), fooId);
    assertEquals(jsonObject.get("default").getAsString(), _default);
  }

  @Test
  public void testSimpleDtoDeserializer() throws Exception {
    final String fooString = "Something";
    final int fooId = 1;
    final String _default = "test_default_keyword";

    JsonObject json = new JsonObject();
    json.add("name", new JsonPrimitive(fooString));
    json.add("id", new JsonPrimitive(fooId));
    json.add("default", new JsonPrimitive(_default));

    SimpleDto dto = dtoFactory.createDtoFromJson(json.toString(), SimpleDto.class);

    // Check to make sure things are in a sane state.
    checkSimpleDto(dto, fooString, fooId, _default);
  }

  @Test
  public void testSerializerWithFieldNames() throws Exception {
    final String fooString = "Something";
    final String _default = "test_default_keyword";

    DtoWithFieldNames dto =
        dtoFactory
            .createDto(DtoWithFieldNames.class)
            .withTheName(fooString)
            .withTheDefault(_default);
    final String json = dtoFactory.toJson(dto);

    JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
    assertEquals(jsonObject.get(DtoWithFieldNames.THENAME_FIELD).getAsString(), fooString);
    assertEquals(jsonObject.get(DtoWithFieldNames.THEDEFAULT_FIELD).getAsString(), _default);
  }

  @Test
  public void testDeserializerWithFieldNames() throws Exception {
    final String fooString = "Something";
    final String _default = "test_default_keyword";

    JsonObject json = new JsonObject();
    json.add(DtoWithFieldNames.THENAME_FIELD, new JsonPrimitive(fooString));
    json.add(DtoWithFieldNames.THEDEFAULT_FIELD, new JsonPrimitive(_default));

    DtoWithFieldNames dto = dtoFactory.createDtoFromJson(json.toString(), DtoWithFieldNames.class);

    assertEquals(dto.getTheName(), fooString);
    assertEquals(dto.getTheDefault(), _default);
  }

  @Test
  public void testSerializerWithAny() throws Exception {
    final List<Object> objects = createListTestValueForAny();
    DtoWithAny dto =
        dtoFactory
            .createDto(DtoWithAny.class)
            .withStuff(createTestValueForAny())
            .withObjects(objects);
    final String json = dtoFactory.toJson(dto);

    JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
    Assert.assertEquals(jsonObject.get("stuff"), createTestValueForAny());

    Assert.assertTrue(jsonObject.has("objects"));
    JsonArray jsonArray = jsonObject.get("objects").getAsJsonArray();
    assertEquals(jsonArray.size(), objects.size());
    for (int i = 0; i < jsonArray.size(); i++) {
      assertEquals(jsonArray.get(i), objects.get(i));
    }
  }

  @Test
  public void testDeserializerWithAny() throws Exception {
    JsonObject json = new JsonObject();
    json.add("stuff", createTestValueForAny());
    JsonArray jsonArray = createElementListTestValueForAny();
    json.add("objects", jsonArray);

    DtoWithAny dto = dtoFactory.createDtoFromJson(json.toString(), DtoWithAny.class);

    Gson gson = new Gson();
    Object stuffValue = gson.fromJson(createTestValueForAny(), Object.class);
    Assert.assertEquals(dto.getStuff(), stuffValue);
    Object objectsValue = gson.fromJson(createElementListTestValueForAny(), Object.class);
    Assert.assertEquals(dto.getObjects(), objectsValue);
  }

  @Test
  public void testShadowedFields() throws Exception {
    GrandchildDto dto1 = dtoFactory.createDto(GrandchildDto.class);
    dtoFactory.toJson(dto1);
  }

  /** Intentionally call several times to ensure non-reference equality */
  private static JsonElement createTestValueForAny() {
    return new JsonParser().parse("{a:100,b:{c:'blah'}}");
  }

  /** Intentionally call several times to ensure non-reference equality */
  private static List<Object> createListTestValueForAny() {
    final ArrayList<Object> objects = new ArrayList<>();
    objects.add(new JsonParser().parse("{x:1}"));
    objects.add(new JsonParser().parse("{b:120}"));
    return objects;
  }

  private JsonArray createElementListTestValueForAny() {
    JsonArray jsonArray = new JsonArray();
    for (Object object : createListTestValueForAny()) {
      jsonArray.add((JsonElement) object);
    }
    return jsonArray;
  }

  @Test
  public void testListSimpleDtoDeserializer() throws Exception {
    final String fooString_1 = "Something 1";
    final int fooId_1 = 1;
    final String _default_1 = "test_default_keyword_1";
    final String fooString_2 = "Something 2";
    final int fooId_2 = 2;
    final String _default_2 = "test_default_keyword_2";

    JsonObject json1 = new JsonObject();
    json1.add("name", new JsonPrimitive(fooString_1));
    json1.add("id", new JsonPrimitive(fooId_1));
    json1.add("default", new JsonPrimitive(_default_1));

    JsonObject json2 = new JsonObject();
    json2.add("name", new JsonPrimitive(fooString_2));
    json2.add("id", new JsonPrimitive(fooId_2));
    json2.add("default", new JsonPrimitive(_default_2));

    JsonArray jsonArray = new JsonArray();
    jsonArray.add(json1);
    jsonArray.add(json2);

    org.eclipse.che.dto.shared.JsonArray<SimpleDto> listDtoFromJson =
        dtoFactory.createListDtoFromJson(jsonArray.toString(), SimpleDto.class);

    assertEquals(listDtoFromJson.get(0).getName(), fooString_1);
    assertEquals(listDtoFromJson.get(0).getId(), fooId_1);
    assertEquals(listDtoFromJson.get(0).getDefault(), _default_1);

    assertEquals(listDtoFromJson.get(1).getName(), fooString_2);
    assertEquals(listDtoFromJson.get(1).getId(), fooId_2);
    assertEquals(listDtoFromJson.get(1).getDefault(), _default_2);
  }

  @Test
  public void testComplicatedDtoSerializer() throws Exception {
    final String fooString = "Something";
    final int fooId = 1;
    final String _default = "test_default_keyword";

    List<String> listStrings = new ArrayList<>(2);
    listStrings.add("Something 1");
    listStrings.add("Something 2");

    ComplicatedDto.SimpleEnum simpleEnum = ComplicatedDto.SimpleEnum.ONE;

    // Assume that SimpleDto works. Use it to test nested objects
    SimpleDto simpleDto =
        dtoFactory
            .createDto(SimpleDto.class)
            .withName(fooString)
            .withId(fooId)
            .withDefault(_default);

    Map<String, SimpleDto> mapDtos = new HashMap<>(1);
    mapDtos.put(fooString, simpleDto);

    List<SimpleDto> listDtos = new ArrayList<>(1);
    listDtos.add(simpleDto);

    List<List<ComplicatedDto.SimpleEnum>> listOfListOfEnum = new ArrayList<>(1);
    List<ComplicatedDto.SimpleEnum> listOfEnum = new ArrayList<>(3);
    listOfEnum.add(ComplicatedDto.SimpleEnum.ONE);
    listOfEnum.add(ComplicatedDto.SimpleEnum.TWO);
    listOfEnum.add(ComplicatedDto.SimpleEnum.THREE);
    listOfListOfEnum.add(listOfEnum);

    ComplicatedDto dto =
        dtoFactory
            .createDto(ComplicatedDto.class)
            .withStrings(listStrings)
            .withSimpleEnum(simpleEnum)
            .withMap(mapDtos)
            .withSimpleDtos(listDtos)
            .withArrayOfArrayOfEnum(listOfListOfEnum);

    final String json = dtoFactory.toJson(dto);
    JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();

    Assert.assertTrue(jsonObject.has("strings"));
    JsonArray jsonArray = jsonObject.get("strings").getAsJsonArray();
    assertEquals(jsonArray.get(0).getAsString(), listStrings.get(0));
    assertEquals(jsonArray.get(1).getAsString(), listStrings.get(1));

    Assert.assertTrue(jsonObject.has("simpleEnum"));
    assertEquals(jsonObject.get("simpleEnum").getAsString(), simpleEnum.name());

    Assert.assertTrue(jsonObject.has("map"));
    JsonObject jsonMap = jsonObject.get("map").getAsJsonObject();
    JsonObject value = jsonMap.get(fooString).getAsJsonObject();
    assertEquals(value.get("name").getAsString(), fooString);
    assertEquals(value.get("id").getAsInt(), fooId);
    assertEquals(value.get("default").getAsString(), _default);

    Assert.assertTrue(jsonObject.has("simpleDtos"));
    JsonArray simpleDtos = jsonObject.get("simpleDtos").getAsJsonArray();
    JsonObject simpleDtoJsonObject = simpleDtos.get(0).getAsJsonObject();
    assertEquals(simpleDtoJsonObject.get("name").getAsString(), fooString);
    assertEquals(simpleDtoJsonObject.get("id").getAsInt(), fooId);
    assertEquals(simpleDtoJsonObject.get("default").getAsString(), _default);

    Assert.assertTrue(jsonObject.has("arrayOfArrayOfEnum"));
    JsonArray arrayOfArrayOfEnum =
        jsonObject.get("arrayOfArrayOfEnum").getAsJsonArray().get(0).getAsJsonArray();
    assertEquals(arrayOfArrayOfEnum.get(0).getAsString(), ComplicatedDto.SimpleEnum.ONE.name());
    assertEquals(arrayOfArrayOfEnum.get(1).getAsString(), ComplicatedDto.SimpleEnum.TWO.name());
    assertEquals(arrayOfArrayOfEnum.get(2).getAsString(), ComplicatedDto.SimpleEnum.THREE.name());
  }

  @Test
  public void testComplicatedDtoDeserializer() throws Exception {
    final String fooString = "Something";
    final int fooId = 1;
    final String _default = "test_default_keyword";

    JsonArray jsonArray = new JsonArray();
    jsonArray.add(new JsonPrimitive(fooString));

    JsonObject simpleDtoJsonObject = new JsonObject();
    simpleDtoJsonObject.add("name", new JsonPrimitive(fooString));
    simpleDtoJsonObject.add("id", new JsonPrimitive(fooId));
    simpleDtoJsonObject.add("default", new JsonPrimitive(_default));

    JsonObject jsonMap = new JsonObject();
    jsonMap.add(fooString, simpleDtoJsonObject);

    JsonArray simpleDtosArray = new JsonArray();
    simpleDtosArray.add(simpleDtoJsonObject);

    JsonArray arrayOfEnum = new JsonArray();
    arrayOfEnum.add(new JsonPrimitive(ComplicatedDto.SimpleEnum.ONE.name()));
    arrayOfEnum.add(new JsonPrimitive(ComplicatedDto.SimpleEnum.TWO.name()));
    arrayOfEnum.add(new JsonPrimitive(ComplicatedDto.SimpleEnum.THREE.name()));
    JsonArray arrayOfArrayEnum = new JsonArray();
    arrayOfArrayEnum.add(arrayOfEnum);

    JsonObject complicatedDtoJsonObject = new JsonObject();
    complicatedDtoJsonObject.add("strings", jsonArray);
    complicatedDtoJsonObject.add(
        "simpleEnum", new JsonPrimitive(ComplicatedDto.SimpleEnum.ONE.name()));
    complicatedDtoJsonObject.add("map", jsonMap);
    complicatedDtoJsonObject.add("simpleDtos", simpleDtosArray);
    complicatedDtoJsonObject.add("arrayOfArrayOfEnum", arrayOfArrayEnum);

    ComplicatedDto complicatedDto =
        dtoFactory.createDtoFromJson(complicatedDtoJsonObject.toString(), ComplicatedDto.class);

    assertEquals(complicatedDto.getStrings().get(0), fooString);
    assertEquals(complicatedDto.getSimpleEnum(), ComplicatedDto.SimpleEnum.ONE);
    checkSimpleDto(complicatedDto.getMap().get(fooString), fooString, fooId, _default);
    checkSimpleDto(complicatedDto.getSimpleDtos().get(0), fooString, fooId, _default);
    assertEquals(
        complicatedDto.getArrayOfArrayOfEnum().get(0).get(0), ComplicatedDto.SimpleEnum.ONE);
    assertEquals(
        complicatedDto.getArrayOfArrayOfEnum().get(0).get(1), ComplicatedDto.SimpleEnum.TWO);
    assertEquals(
        complicatedDto.getArrayOfArrayOfEnum().get(0).get(2), ComplicatedDto.SimpleEnum.THREE);
  }

  private void checkSimpleDto(
      SimpleDto dto, String expectedName, int expectedId, String expectedDefault) {
    assertEquals(dto.getName(), expectedName);
    assertEquals(dto.getId(), expectedId);
    assertEquals(dto.getDefault(), expectedDefault);
  }

  @Test
  public void testDelegate() {
    assertEquals(
        DtoFactory.getInstance()
            .createDto(DtoWithDelegate.class)
            .withFirstName("TEST")
            .nameWithPrefix("### "),
        "### TEST");
  }

  @Test
  public void shouldBeAbleToExtendModelSkeletonWithDTOs() {
    final DtoFactory factory = DtoFactory.getInstance();
    final Model model =
        factory
            .createDto(ModelDto.class)
            .withPrimary(factory.createDto(ModelComponentDto.class).withName("primary name"))
            .withComponents(
                asList(
                    factory.createDto(ModelComponentDto.class).withName("name"),
                    factory.createDto(ModelComponentDto.class).withName("name2"),
                    factory.createDto(ModelComponentDto.class).withName("name3")));

    assertEquals(
        model.getPrimary(), factory.createDto(ModelComponentDto.class).withName("primary name"));
    assertEquals(model.getComponents().size(), 3);
    assertTrue(
        model
            .getComponents()
            .contains(factory.createDto(ModelComponentDto.class).withName("name")));
    assertTrue(
        model
            .getComponents()
            .contains(factory.createDto(ModelComponentDto.class).withName("name2")));
    assertTrue(
        model
            .getComponents()
            .contains(factory.createDto(ModelComponentDto.class).withName("name3")));
  }

  @Test
  public void shouldBeAbleToExtendsNotDTOInterfacesHierarchyWithDTOInterface() {
    final DTOHierarchy.ChildDto childDto =
        DtoFactory.getInstance()
            .createDto(DTOHierarchy.ChildDto.class)
            .withDtoField("dto-field")
            .withChildField("child-field")
            .withParentField("parent-field");

    assertEquals(childDto.getDtoField(), "dto-field");
    assertEquals(childDto.getChildField(), "child-field");
    assertEquals(childDto.getParentField(), "parent-field");
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp =
          "Only interfaces can be DTO, but class java.lang.String is not an interface.")
  public void shouldThrowExceptionWhenThereIsClassType() {
    DtoFactory.newDto(String.class);
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp =
          "interface org.eclipse.che.dto.definitions.DTOHierarchy\\$GrandchildWithoutDto is not a DTO type")
  public void shouldThrowExceptionWhenInterfaceIsNotAnnotatedAsDto() {
    DtoFactory.newDto(DTOHierarchy.GrandchildWithoutDto.class);
  }

  @Test
  public void checkDtoDeserializationWithSerializableFields() {
    final int fooId = 1;
    final String fooString = "some string";
    final long fooLong = 1234514362645634611L;
    final double fooDouble = 1.2345;
    final double fooRoundingDouble = 6.00;

    JsonObject jsonMap = new JsonObject();
    jsonMap.add("fooLong", new JsonPrimitive(fooLong));
    jsonMap.add("fooBoolean", new JsonPrimitive(true));
    jsonMap.add("fooDouble", new JsonPrimitive(fooDouble));
    jsonMap.add("fooRoundingDouble", new JsonPrimitive(fooRoundingDouble));
    jsonMap.add("fooString", new JsonPrimitive(fooString));

    JsonObject json = new JsonObject();
    json.add("id", new JsonPrimitive(fooId));
    json.add("object", new JsonPrimitive(fooString));
    json.add("objectMap", jsonMap);

    DtoWithSerializable dto =
        dtoFactory.createDtoFromJson(json.toString(), DtoWithSerializable.class);

    assertEquals(dto.getId(), fooId);
    assertEquals(dto.getObject(), fooString);
    assertEquals(dto.getObjectMap().get("fooLong"), fooLong);
    assertEquals(dto.getObjectMap().get("fooBoolean"), true);
    assertEquals(dto.getObjectMap().get("fooDouble"), fooDouble);
    assertEquals(dto.getObjectMap().get("fooString"), fooString);
    assertEquals(dto.getObjectMap().get("fooRoundingDouble"), Math.round(fooRoundingDouble));
  }
}
