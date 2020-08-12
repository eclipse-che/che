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
import static org.eclipse.che.api.factory.shared.Constants.CURRENT_VERSION;
import static org.eclipse.che.api.workspace.server.devfile.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_TOOLING_PLUGINS_ATTRIBUTE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.factory.server.urlfactory.RemoteFactoryUrl.DevfileLocation;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.workspace.server.devfile.DevfileManager;
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
import org.eclipse.che.dto.server.DtoFactory;
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

  @Mock private DevfileManager devfileManager;

  /** Tested instance. */
  private URLFactoryBuilder urlFactoryBuilder;

  @BeforeClass
  public void setUp() {
    this.urlFactoryBuilder =
        new URLFactoryBuilder(defaultEditor, defaultPlugin, urlFetcher, devfileManager);
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

  /** Check that with a custom factory.json we've this factory being built */
  @Test
  public void checkWithCustomFactoryJsonFile() throws Exception {

    WorkspaceConfigDto workspaceConfigDto = newDto(WorkspaceConfigDto.class);
    FactoryDto templateFactory =
        newDto(FactoryDto.class)
            .withV(CURRENT_VERSION)
            .withName("florent")
            .withWorkspace(workspaceConfigDto);
    String jsonFactory = DtoFactory.getInstance().toJson(templateFactory);

    String myLocation = "http://foo-location";
    when(urlFetcher.fetchSafely(myLocation)).thenReturn(jsonFactory);

    FactoryDto factory =
        urlFactoryBuilder
            .createFactoryFromJson(
                new DefaultFactoryUrl()
                    .withFactoryFileLocation(myLocation)
                    .withFactoryFilename(".factory.json"))
            .get();

    assertEquals(templateFactory.withSource(".factory.json"), factory);
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
    when(devfileManager.parseYaml(anyString(), anyMap())).thenReturn(devfile);

    FactoryDto factory =
        urlFactoryBuilder
            .createFactoryFromDevfile(
                new DefaultFactoryUrl().withDevfileFileLocation(myLocation),
                s -> myLocation + ".list",
                emptyMap())
            .get();

    assertNotNull(factory);
    assertNull(factory.getSource());
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
      throws BadRequestException, ServerException, DevfileException, IOException,
          OverrideParameterException {
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
    when(devfileManager.parseYaml(anyString(), anyMap())).thenReturn(devfile);
    when(urlFetcher.fetchSafely(anyString())).thenReturn("anything");
    FactoryDto factory =
        urlFactoryBuilder
            .createFactoryFromDevfile(defaultFactoryUrl, fileContentProvider, emptyMap())
            .get();

    assertNull(factory.getDevfile().getMetadata().getName());
    assertEquals(factory.getDevfile().getMetadata().getGenerateName(), expectedGenerateName);
  }
}
