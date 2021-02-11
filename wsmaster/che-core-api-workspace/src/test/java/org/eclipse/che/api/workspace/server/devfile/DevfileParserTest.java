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
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileSchemaValidator;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class DevfileParserTest {

  private static final String DEVFILE_YAML_CONTENT = "devfile yaml stub";

  @Mock private DevfileSchemaValidator schemaValidator;
  @Mock private DevfileIntegrityValidator integrityValidator;
  @Mock private ObjectMapper jsonMapper;
  @Mock private ObjectMapper yamlMapper;
  @Mock private FileContentProvider contentProvider;

  @Mock private JsonNode devfileJsonNode;
  private DevfileImpl devfile;

  private DevfileParser devfileParser;

  @BeforeMethod
  public void setUp() throws Exception {
    devfile = new DevfileImpl();
    devfileParser = new DevfileParser(schemaValidator, integrityValidator, yamlMapper, jsonMapper);

    lenient().when(jsonMapper.treeToValue(any(), eq(DevfileImpl.class))).thenReturn(devfile);
    lenient().when(yamlMapper.treeToValue(any(), eq(DevfileImpl.class))).thenReturn(devfile);
    lenient().when(yamlMapper.readTree(anyString())).thenReturn(devfileJsonNode);
  }

  @Test
  public void testValidateAndParse() throws Exception {
    // when
    DevfileImpl parsed = devfileParser.parseYaml(DEVFILE_YAML_CONTENT);

    // then
    assertEquals(parsed, devfile);
    verify(yamlMapper).treeToValue(devfileJsonNode, DevfileImpl.class);
    verify(schemaValidator).validate(eq(devfileJsonNode));
    verify(integrityValidator).validateDevfile(devfile);
  }

  @Test
  public void testParseRaw() throws DevfileFormatException {
    JsonNode parsed = devfileParser.parseYamlRaw(DEVFILE_YAML_CONTENT);

    assertEquals(parsed, devfileJsonNode);
    verify(schemaValidator).validate(eq(devfileJsonNode));
  }

  @Test(expectedExceptions = DevfileFormatException.class)
  public void testParseRawThrowsExceptionWhenNothinParsed()
      throws DevfileFormatException, JsonProcessingException {
    when(yamlMapper.readTree(DEVFILE_YAML_CONTENT)).thenReturn(null);
    devfileParser.parseYamlRaw(DEVFILE_YAML_CONTENT);
  }

  @Test(expectedExceptions = DevfileFormatException.class)
  public void testParseRawThrowsDevfilExceptionWhenJsonParsingFails()
      throws JsonProcessingException, DevfileFormatException {
    when(yamlMapper.readTree(DEVFILE_YAML_CONTENT)).thenThrow(JsonProcessingException.class);
    devfileParser.parseYamlRaw(DEVFILE_YAML_CONTENT);
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
    DevfileImpl parsed = devfileParser.parseYaml(DEVFILE_YAML_CONTENT);

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
    devfileParser.resolveReference(devfile, contentProvider);

    // then
    verify(contentProvider).fetchContent(eq("myfile.yaml"));
    assertEquals(devfile.getComponents().get(0).getReferenceContent(), referenceContent);
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp = "Unable to parse Devfile - provided source is empty")
  public void shouldThrowDevfileExceptionWhenEmptyObjectProvided() throws Exception {
    // when
    devfileParser.parseJson("{}");
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp = "Unable to parse Devfile - provided source is empty")
  public void shouldThrowDevfileExceptionWhenEmptySourceProvided() throws Exception {
    // when
    devfileParser.parseJson("");
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
    devfileParser.resolveReference(devfile, contentProvider);

    // then exception is thrown
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "non valid")
  public void shouldThrowExceptionWhenExceptionOccurredDuringSchemaValidation() throws Exception {
    // given
    doThrow(new DevfileFormatException("non valid")).when(schemaValidator).validate(any());

    // when
    devfileParser.parseYaml(DEVFILE_YAML_CONTENT);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "non valid")
  public void shouldThrowExceptionWhenErrorOccurredDuringDevfileParsing() throws Exception {
    // given
    JsonProcessingException jsonException = mock(JsonProcessingException.class);
    when(jsonException.getMessage()).thenReturn("non valid");
    when(jsonMapper.readTree(anyString())).thenReturn(devfileJsonNode);
    doThrow(jsonException).when(jsonMapper).treeToValue(any(), any());

    // when
    devfileParser.parseJson(DEVFILE_YAML_CONTENT);
  }
}
