/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile;

import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileSchemaValidator;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class DevfileManagerTest {

  private static final String DEVFILE_YAML_CONTENT = "devfile yaml stub";

  @Mock private DevfileSchemaValidator schemaValidator;
  @Mock private DevfileIntegrityValidator integrityValidator;
  @Mock private ObjectMapper jsonMapper;
  @Mock private ObjectMapper yamlMapper;
  @Mock private FileContentProvider contentProvider;

  @Mock private JsonNode devfileJsonNode;
  private DevfileImpl devfile;

  private DevfileManager devfileManager;

  @BeforeMethod
  public void setUp() throws Exception {
    devfile = new DevfileImpl();
    devfileManager =
        new DevfileManager(schemaValidator, integrityValidator, yamlMapper, jsonMapper);

    lenient().when(jsonMapper.treeToValue(any(), eq(DevfileImpl.class))).thenReturn(devfile);
    lenient().when(yamlMapper.treeToValue(any(), eq(DevfileImpl.class))).thenReturn(devfile);
    lenient().when(yamlMapper.readTree(anyString())).thenReturn(devfileJsonNode);
  }

  @Test
  public void testValidateAndParse() throws Exception {
    // when
    DevfileImpl parsed = devfileManager.parseYaml(DEVFILE_YAML_CONTENT);

    // then
    assertEquals(parsed, devfile);
    verify(yamlMapper).treeToValue(devfileJsonNode, DevfileImpl.class);
    verify(schemaValidator).validate(eq(devfileJsonNode));
    verify(integrityValidator).validateDevfile(devfile);
  }

  @Test
  public void testInitializingDevfileMapsAfterParsing() throws Exception {
    // given
    CommandImpl command = new CommandImpl();
    command.getActions().add(new ActionImpl());
    devfile.getCommands().add(command);

    ComponentImpl component = new ComponentImpl();
    component.getEndpoints().add(new EndpointImpl());
    devfile.getComponents().add(component);

    // when
    DevfileImpl parsed = devfileManager.parseYaml(DEVFILE_YAML_CONTENT);

    // then
    assertNotNull(parsed.getCommands().get(0).getAttributes());
    assertNotNull(parsed.getComponents().get(0).getSelector());
    assertNotNull(parsed.getComponents().get(0).getEndpoints().get(0).getAttributes());
  }

  @Test
  public void shouldResolveReferencesIntoReferenceContentForFactories() throws Exception {

    String referenceContent = "my_content_yaml_v3";
    when(contentProvider.fetchContent(anyString())).thenReturn(referenceContent);

    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference("myfile.yaml");
    devfile.getComponents().add(component);

    // when
    devfileManager.resolveReference(devfile, contentProvider);

    // then
    verify(contentProvider).fetchContent(eq("myfile.yaml"));
    assertEquals(referenceContent, devfile.getComponents().get(0).getReferenceContent());
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp = "Unable to resolve reference of component: test")
  public void shouldThrowDevfileExceptionWhenReferenceIsNotResolvable() throws Exception {

    when(contentProvider.fetchContent(anyString())).thenThrow(IOException.class);

    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setAlias("test");
    component.setReference("myfile.yaml");
    devfile.getComponents().add(component);

    // when
    devfileManager.resolveReference(devfile, contentProvider);

    // then exception is thrown
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "non valid")
  public void shouldThrowExceptionWhenExceptionOccurredDuringSchemaValidation() throws Exception {
    // given
    doThrow(new DevfileFormatException("non valid")).when(schemaValidator).validate(any());

    // when
    devfileManager.parseYaml(DEVFILE_YAML_CONTENT);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "non valid")
  public void shouldThrowExceptionWhenErrorOccurredDuringDevfileParsing() throws Exception {
    // given
    JsonProcessingException jsonException = mock(JsonProcessingException.class);
    when(jsonException.getMessage()).thenReturn("non valid");
    doThrow(jsonException).when(jsonMapper).treeToValue(any(), any());

    // when
    devfileManager.parseJson(DEVFILE_YAML_CONTENT);
  }

  @Test
  public void shouldOverrideExistingPropertiesInDevfile() throws Exception {

    String json =
        "{"
            + "\"apiVersion\": \"1.0.0\","
            + "\"metadata\": {"
            + "   \"generateName\": \"python\""
            + "  }"
            + "}";
    // instance with real json mappers
    DevfileManager manager = new DevfileManager(schemaValidator, integrityValidator);
    Map<String, String> overrides = new HashMap<>();
    overrides.put("apiVersion", "2.0.0");
    overrides.put("metadata.generateName", "go");
    ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
    // when
    manager.parseJson(json, overrides);
    verify(schemaValidator).validate(captor.capture());

    JsonNode result = captor.getValue();
    assertEquals(result.get("apiVersion").textValue(), "2.0.0");
    assertEquals(result.get("metadata").get("generateName").textValue(), "go");
  }

  @Test
  public void shouldCreateUnExistingOverridePropertiesInDevfile() throws Exception {
    String json = "{" + "\"apiVersion\": \"1.0.0\"" + "}";
    // instance with real json mappers
    DevfileManager manager = new DevfileManager(schemaValidator, integrityValidator);
    Map<String, String> overrides = new HashMap<>();
    overrides.put("metadata.generateName", "go");
    ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
    // when
    manager.parseJson(json, overrides);
    verify(schemaValidator).validate(captor.capture());

    JsonNode result = captor.getValue();
    assertEquals(result.get("metadata").get("generateName").textValue(), "go");
  }

  @Test
  public void shouldRewriteValueInProjectsArrayElementByName() throws Exception {
    String json =
        "{"
            + "\"apiVersion\": \"1.0.0\","
            + "\"projects\": ["
            + "   {"
            + "      \"name\": \"test1\","
            + "      \"clonePath\": \"/foo/bar1\""
            + "   },"
            + "   {"
            + "      \"name\": \"test2\","
            + "      \"clonePath\": \"/foo/bar2\""
            + "   }"
            + "  ]"
            + "}";
    // instance with real json mappers
    DevfileManager manager = new DevfileManager(schemaValidator, integrityValidator);
    Map<String, String> overrides = new HashMap<>();
    overrides.put("projects.test1.clonePath", "baz1");
    overrides.put("projects.test2.clonePath", "baz2");
    ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
    // when
    manager.parseJson(json, overrides);
    verify(schemaValidator).validate(captor.capture());

    JsonNode result = captor.getValue();
    assertEquals(result.get("projects").get(0).get("clonePath").textValue(), "baz1");
    assertEquals(result.get("projects").get(1).get("clonePath").textValue(), "baz2");
  }

  @Test
  public void shouldRewriteValueInComponentsArrayElementByAlias() throws Exception {
    String json =
        "{"
            + "\"apiVersion\": \"1.0.0\","
            + "\"components\": ["
            + "   {"
            + "      \"alias\": \"java\","
            + "      \"memoryLimit\": \"300Mi\""
            + "   },"
            + "   {"
            + "      \"alias\": \"mysql\","
            + "      \"mountSources\": \"false\""
            + "   }"
            + "  ]"
            + "}";
    // instance with real json mappers
    DevfileManager manager = new DevfileManager(schemaValidator, integrityValidator);
    Map<String, String> overrides = new HashMap<>();
    overrides.put("components.java.memoryLimit", "500Mi");
    overrides.put("components.mysql.mountSources", "true");
    ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
    // when
    manager.parseJson(json, overrides);
    verify(schemaValidator).validate(captor.capture());

    JsonNode result = captor.getValue();
    assertEquals(result.get("components").get(0).get("memoryLimit").textValue(), "500Mi");
    assertEquals(result.get("components").get(1).get("mountSources").asBoolean(), true);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Object with name 'test3' not found in array of projects.")
  public void shouldThrowExceptionIfOverrideArrayObjectNotFoundByName() throws Exception {
    String json =
        "{"
            + "\"apiVersion\": \"1.0.0\","
            + "\"projects\": ["
            + "   {"
            + "      \"name\": \"test1\","
            + "      \"clonePath\": \"/foo/bar1\""
            + "   },"
            + "   {"
            + "      \"name\": \"test2\","
            + "      \"clonePath\": \"/foo/bar2\""
            + "   }"
            + "  ]"
            + "}";
    // instance with real json mappers
    DevfileManager manager = new DevfileManager(schemaValidator, integrityValidator);
    Map<String, String> overrides = new HashMap<>();
    overrides.put("projects.test3.clonePath", "baz1");
    // when
    manager.parseJson(json, overrides);
  }
}
