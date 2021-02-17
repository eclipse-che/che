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
package org.eclipse.che.api.factory.server.urlfactory;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.shared.dto.ExtendedError;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.api.factory.server.scm.exception.UnknownScmProviderException;
import org.eclipse.che.api.factory.server.urlfactory.RemoteFactoryUrl.DevfileLocation;
import org.eclipse.che.api.factory.shared.dto.FactoryDevfileV2Dto;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.factory.shared.dto.FactoryMetaDto;
import org.eclipse.che.api.workspace.server.devfile.DevfileParser;
import org.eclipse.che.api.workspace.server.devfile.DevfileVersionDetector;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.URLFetcher;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.devfile.exception.OverrideParameterException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.MetadataImpl;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Testing {@link URLFactoryBuilder}
 *
 * @author Florent Benoit
 * @author Max Shaposhnyk
 */
@Listeners(MockitoTestNGListener.class)
public class URLFactoryBuilderTest {

  private final String defaultEditor = "eclipse/che-theia/1.0.0";
  private final String defaultPlugin = "eclipse/che-machine-exec-plugin/0.0.1";
  /** Grab content of URLs */
  @Mock private URLFetcher urlFetcher;

  @Mock private DevfileParser devfileParser;

  @Mock private DevfileVersionDetector devfileVersionDetector;

  /** Tested instance. */
  private URLFactoryBuilder urlFactoryBuilder;

  @BeforeClass
  public void setUp() {
    this.urlFactoryBuilder =
        new URLFactoryBuilder(defaultEditor, defaultPlugin, devfileParser, devfileVersionDetector);
  }

  @Test
  public void checkDefaultConfiguration() throws Exception {
    Map<String, String> attributes = new HashMap<>();
    attributes.put(WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, defaultEditor);
    attributes.put(WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE, defaultPlugin);
    // setup environment
    WorkspaceConfigDto expectedWsConfig =
        newDto(WorkspaceConfigDto.class).withAttributes(attributes).withName("foo");

    WorkspaceConfigDto actualWsConfigDto = urlFactoryBuilder.buildDefaultWorkspaceConfig("foo");

    assertEquals(actualWsConfigDto, expectedWsConfig);
  }

  @Test
  public void checkWithCustomDevfileAndRecipe() throws Exception {

    DevfileImpl devfile = new DevfileImpl();
    WorkspaceConfigImpl workspaceConfigImpl = new WorkspaceConfigImpl();
    String myLocation = "http://foo-location/";
    RecipeImpl expectedRecipe =
        new RecipeImpl(KUBERNETES_COMPONENT_TYPE, "application/x-yaml", "content", "");
    EnvironmentImpl expectedEnv = new EnvironmentImpl(expectedRecipe, emptyMap());
    workspaceConfigImpl.setEnvironments(singletonMap("name", expectedEnv));
    workspaceConfigImpl.setDefaultEnv("name");

    when(urlFetcher.fetchSafely(anyString())).thenReturn("random_content");
    when(devfileParser.parseYamlRaw(anyString()))
        .thenReturn(new ObjectNode(JsonNodeFactory.instance));
    when(devfileParser.parseJsonNode(any(JsonNode.class), anyMap())).thenReturn(devfile);
    when(devfileVersionDetector.devfileMajorVersion(any(JsonNode.class))).thenReturn(1);

    FactoryMetaDto factory =
        urlFactoryBuilder
            .createFactoryFromDevfile(
                new DefaultFactoryUrl().withDevfileFileLocation(myLocation),
                s -> myLocation + ".list",
                emptyMap())
            .get();

    assertNotNull(factory);
    assertNull(factory.getSource());
    assertTrue(factory instanceof FactoryDto);
  }

  @Test
  public void testDevfileV2() throws ApiException, DevfileException {
    String myLocation = "http://foo-location/";
    Map<String, Object> devfileAsMap = Map.of("hello", "there", "how", "are", "you", "?");

    JsonNode devfile = new ObjectNode(JsonNodeFactory.instance);
    when(devfileParser.parseYamlRaw(anyString())).thenReturn(devfile);
    when(devfileParser.convertYamlToMap(devfile)).thenReturn(devfileAsMap);
    when(devfileVersionDetector.devfileMajorVersion(devfile)).thenReturn(2);

    FactoryMetaDto factory =
        urlFactoryBuilder
            .createFactoryFromDevfile(
                new DefaultFactoryUrl().withDevfileFileLocation(myLocation),
                s -> myLocation + ".list",
                emptyMap())
            .get();

    assertNotNull(factory);
    assertNull(factory.getSource());
    assertTrue(factory instanceof FactoryDevfileV2Dto);
    assertEquals(((FactoryDevfileV2Dto) factory).getDevfile(), devfileAsMap);
  }

  @Test
  public void testDevfileV2WithFilename() throws ApiException, DevfileException {
    String myLocation = "http://foo-location/";
    Map<String, Object> devfileAsMap = Map.of("hello", "there", "how", "are", "you", "?");

    JsonNode devfile = new ObjectNode(JsonNodeFactory.instance);
    when(devfileParser.parseYamlRaw(anyString())).thenReturn(devfile);
    when(devfileParser.convertYamlToMap(devfile)).thenReturn(devfileAsMap);
    when(devfileVersionDetector.devfileMajorVersion(devfile)).thenReturn(2);

    RemoteFactoryUrl githubLikeRemoteUrl =
        () ->
            Collections.singletonList(
                new DevfileLocation() {
                  @Override
                  public Optional<String> filename() {
                    return Optional.of("devfile.yaml");
                  }

                  @Override
                  public String location() {
                    return myLocation;
                  }
                });

    FactoryMetaDto factory =
        urlFactoryBuilder
            .createFactoryFromDevfile(githubLikeRemoteUrl, s -> myLocation + ".list", emptyMap())
            .get();

    assertNotNull(factory);
    assertEquals(factory.getSource(), "devfile.yaml");
    assertTrue(factory instanceof FactoryDevfileV2Dto);
    assertEquals(((FactoryDevfileV2Dto) factory).getDevfile(), devfileAsMap);
  }

  @DataProvider
  public Object[][] devfiles() {
    final String NAME = "name";
    final String GEN_NAME = "genName";

    DevfileImpl devfileTemplate = new DevfileImpl();
    devfileTemplate.setApiVersion("1.0.0");
    MetadataImpl metadataTemplate = new MetadataImpl();

    metadataTemplate.setName(NAME);
    devfileTemplate.setMetadata(metadataTemplate);
    DevfileImpl justName = new DevfileImpl(devfileTemplate);

    metadataTemplate.setName(null);
    metadataTemplate.setGenerateName(GEN_NAME);
    devfileTemplate.setMetadata(metadataTemplate);
    DevfileImpl justGenerateName = new DevfileImpl(devfileTemplate);

    metadataTemplate.setName(NAME);
    metadataTemplate.setGenerateName(GEN_NAME);
    devfileTemplate.setMetadata(metadataTemplate);
    DevfileImpl bothNames = new DevfileImpl(devfileTemplate);

    return new Object[][] {{justName, NAME}, {justGenerateName, GEN_NAME}, {bothNames, GEN_NAME}};
  }

  @Test(dataProvider = "devfiles")
  public void checkThatDtoHasCorrectNames(DevfileImpl devfile, String expectedGenerateName)
      throws ApiException, IOException, OverrideParameterException, DevfileException {
    DefaultFactoryUrl defaultFactoryUrl = mock(DefaultFactoryUrl.class);
    FileContentProvider fileContentProvider = mock(FileContentProvider.class);
    when(defaultFactoryUrl.devfileFileLocations())
        .thenReturn(
            singletonList(
                new DevfileLocation() {
                  @Override
                  public Optional<String> filename() {
                    return Optional.empty();
                  }

                  @Override
                  public String location() {
                    return "http://foo.bar/anything";
                  }
                }));
    when(fileContentProvider.fetchContent(anyString())).thenReturn("anything");
    when(devfileParser.parseYamlRaw("anything"))
        .thenReturn(new ObjectNode(JsonNodeFactory.instance));
    when(devfileParser.parseJsonNode(any(JsonNode.class), anyMap())).thenReturn(devfile);
    when(devfileVersionDetector.devfileMajorVersion(any(JsonNode.class))).thenReturn(1);
    FactoryDto factory =
        (FactoryDto)
            urlFactoryBuilder
                .createFactoryFromDevfile(defaultFactoryUrl, fileContentProvider, emptyMap())
                .get();

    assertNull(factory.getDevfile().getMetadata().getName());
    assertEquals(factory.getDevfile().getMetadata().getGenerateName(), expectedGenerateName);
  }

  @Test(dataProvider = "devfileExceptions")
  public void checkCorrectExceptionThrownDependingOnCause(
      Throwable cause,
      Class expectedClass,
      String expectedMessage,
      Map<String, String> expectedAttributes)
      throws IOException, DevfileException {
    DefaultFactoryUrl defaultFactoryUrl = mock(DefaultFactoryUrl.class);
    FileContentProvider fileContentProvider = mock(FileContentProvider.class);
    when(defaultFactoryUrl.devfileFileLocations())
        .thenReturn(
            singletonList(
                new DevfileLocation() {
                  @Override
                  public Optional<String> filename() {
                    return Optional.empty();
                  }

                  @Override
                  public String location() {
                    return "http://foo.bar/anything";
                  }
                }));

    when(fileContentProvider.fetchContent(anyString())).thenThrow(new DevfileException("", cause));

    // when
    try {
      urlFactoryBuilder.createFactoryFromDevfile(
          defaultFactoryUrl, fileContentProvider, emptyMap());
    } catch (ApiException e) {
      assertTrue(e.getClass().isAssignableFrom(expectedClass));
      assertEquals(e.getMessage(), expectedMessage);
      if (e.getServiceError() instanceof ExtendedError)
        assertEquals(((ExtendedError) e.getServiceError()).getAttributes(), expectedAttributes);
    }
  }

  @DataProvider
  public static Object[][] devfileExceptions() {
    return new Object[][] {
      {
        new ScmCommunicationException("foo"),
        ServerException.class,
        "There is an error happened when communicate with SCM server. Error message:foo",
        null
      },
      {
        new UnknownScmProviderException("foo", "bar"),
        ServerException.class,
        "Provided location is unknown or misconfigured on the server side. Error message:foo",
        null
      },
      {
        new ScmUnauthorizedException("foo", "bitbucket", "1.0", "http://foo.bar"),
        UnauthorizedException.class,
        "SCM Authentication required",
        Map.of(
            "oauth_version",
            "1.0",
            "oauth_authentication_url",
            "http://foo.bar",
            "oauth_provider",
            "bitbucket")
      }
    };
  }
}
