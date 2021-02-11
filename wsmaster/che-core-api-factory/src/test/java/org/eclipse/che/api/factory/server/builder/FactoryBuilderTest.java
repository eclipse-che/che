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
package org.eclipse.che.api.factory.server.builder;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.factory.server.impl.SourceStorageParametersValidator;
import org.eclipse.che.api.factory.shared.dto.AuthorDto;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.factory.shared.dto.IdeActionDto;
import org.eclipse.che.api.factory.shared.dto.IdeDto;
import org.eclipse.che.api.factory.shared.dto.OnAppClosedDto;
import org.eclipse.che.api.factory.shared.dto.OnAppLoadedDto;
import org.eclipse.che.api.factory.shared.dto.OnProjectsLoadedDto;
import org.eclipse.che.api.factory.shared.dto.PoliciesDto;
import org.eclipse.che.api.workspace.shared.dto.CommandDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.MachineConfigDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RecipeDto;
import org.eclipse.che.api.workspace.shared.dto.SourceStorageDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link FactoryDto}
 *
 * @author Alexander Garagatyi
 * @author Sergii Kabashniuk
 */
@SuppressWarnings("deprecation")
@Listeners(MockitoTestNGListener.class)
public class FactoryBuilderTest {

  private static DtoFactory dto = DtoFactory.getInstance();

  private FactoryBuilder factoryBuilder;

  private FactoryDto actual;

  private FactoryDto expected;

  @Mock private SourceStorageParametersValidator sourceProjectParametersValidator;

  @BeforeMethod
  public void setUp() throws Exception {
    factoryBuilder = new FactoryBuilder(sourceProjectParametersValidator);
    actual = prepareFactory();

    expected = dto.createDto(FactoryDto.class);
  }

  @Test
  public void shouldBeAbleToValidateV4_0() throws Exception {
    factoryBuilder.checkValid(actual);

    verify(sourceProjectParametersValidator).validate(any(), eq(FactoryParameter.Version.V4_0));
  }

  @Test(expectedExceptions = ApiException.class)
  public void shouldNotValidateUnparseableFactory() throws Exception {
    factoryBuilder.checkValid(null);
  }

  @Test(
      expectedExceptions = ApiException.class,
      dataProvider = "setByServerParamsProvider",
      expectedExceptionsMessageRegExp =
          "You have provided an invalid parameter .* for this version of Factory parameters.*")
  public void shouldNotAllowUsingParamsThatCanBeSetOnlyByServer(FactoryDto factory)
      throws Exception {
    factoryBuilder.checkValid(factory);
  }

  @Test(dataProvider = "setByServerParamsProvider")
  public void shouldAllowUsingParamsThatCanBeSetOnlyByServerDuringUpdate(FactoryDto factory)
      throws Exception {
    factoryBuilder.checkValid(factory, true);
  }

  @DataProvider(name = "setByServerParamsProvider")
  public static Object[][] setByServerParamsProvider() throws Exception {
    FactoryDto factory = prepareFactory();
    return new Object[][] {
      {requireNonNull(dto.clone(factory)).withId("id")},
      {
        requireNonNull(dto.clone(factory))
            .withCreator(dto.createDto(AuthorDto.class).withUserId("id"))
      },
      {
        requireNonNull(dto.clone(factory))
            .withCreator(dto.createDto(AuthorDto.class).withCreated(123L))
      }
    };
  }

  @Test(expectedExceptions = ApiException.class, dataProvider = "notValidParamsProvider")
  public void shouldNotAllowUsingNotValidParams(FactoryDto factory)
      throws InvocationTargetException, IllegalAccessException, ApiException,
          NoSuchMethodException {
    factoryBuilder.checkValid(factory);
  }

  @DataProvider(name = "notValidParamsProvider")
  public static Object[][] notValidParamsProvider()
      throws URISyntaxException, IOException, NoSuchMethodException {
    FactoryDto factory = prepareFactory();
    EnvironmentDto environmentDto =
        factory.getWorkspace().getEnvironments().values().iterator().next();
    environmentDto.getRecipe().withType(null);
    return new Object[][] {
      {
        requireNonNull(dto.clone(factory))
            .withWorkspace(factory.getWorkspace().withDefaultEnv(null))
      },
      {
        requireNonNull(dto.clone(factory))
            .withWorkspace(
                factory.getWorkspace().withEnvironments(singletonMap("test", environmentDto)))
      }
    };
  }

  @Test
  public void shouldBeAbleToValidateV4_0WithTrackedParamsWithoutAccountIdIfOnPremisesIsEnabled()
      throws Exception {
    factoryBuilder = new FactoryBuilder(sourceProjectParametersValidator);

    FactoryDto factory =
        prepareFactory()
            .withPolicies(
                dto.createDto(PoliciesDto.class)
                    .withReferer("referrer")
                    .withSince(123L)
                    .withUntil(123L));

    factoryBuilder.checkValid(factory);
  }

  @Test(
      expectedExceptions = ConflictException.class,
      expectedExceptionsMessageRegExp =
          "You are missing a mandatory parameter \"workspace.projects\\[1\\].path\". .*")
  public void shouldThrowExceptionWithMessagePointingToMissingMandatoryParameter()
      throws Exception {
    factoryBuilder = new FactoryBuilder(sourceProjectParametersValidator);

    ProjectConfigDto project =
        dto.createDto(ProjectConfigDto.class)
            .withSource(
                dto.createDto(SourceStorageDto.class).withType("git").withLocation("location"))
            .withType("type")
            .withAttributes(singletonMap("key", singletonList("value")))
            .withDescription("description")
            .withName("name")
            .withPath("/path");

    ProjectConfigDto project2 =
        dto.createDto(ProjectConfigDto.class)
            .withSource(
                dto.createDto(SourceStorageDto.class).withType("git").withLocation("location"))
            .withType("")
            .withAttributes(singletonMap("key", singletonList("value")))
            .withDescription("description")
            .withName("test")
            .withPath("");
    FactoryDto factory = prepareFactory();
    factory.getWorkspace().setProjects(asList(project, project2));

    factoryBuilder.checkValid(factory);
  }

  private static FactoryDto prepareFactory() {
    ProjectConfigDto project =
        dto.createDto(ProjectConfigDto.class)
            .withSource(
                dto.createDto(SourceStorageDto.class).withType("git").withLocation("location"))
            .withType("type")
            .withAttributes(singletonMap("key", singletonList("value")))
            .withDescription("description")
            .withName("name")
            .withPath("/path");
    EnvironmentDto environment =
        dto.createDto(EnvironmentDto.class)
            .withRecipe(
                newDto(RecipeDto.class)
                    .withType("compose")
                    .withContentType("application/x-yaml")
                    .withContent("some content"))
            .withMachines(
                singletonMap(
                    "devmachine",
                    newDto(MachineConfigDto.class)
                        .withAttributes(
                            singletonMap(MEMORY_LIMIT_ATTRIBUTE, "" + 512L * 1024L * 1024L))));

    WorkspaceConfigDto workspaceConfig =
        dto.createDto(WorkspaceConfigDto.class)
            .withProjects(singletonList(project))
            .withCommands(
                singletonList(
                    dto.createDto(CommandDto.class)
                        .withName("command1")
                        .withType("maven")
                        .withCommandLine("mvn test")))
            .withDefaultEnv("env1")
            .withEnvironments(singletonMap("test", environment));
    IdeDto ide =
        dto.createDto(IdeDto.class)
            .withOnAppClosed(
                dto.createDto(OnAppClosedDto.class)
                    .withActions(
                        singletonList(dto.createDto(IdeActionDto.class).withId("warnOnClose"))))
            .withOnAppLoaded(
                dto.createDto(OnAppLoadedDto.class)
                    .withActions(
                        asList(
                            dto.createDto(IdeActionDto.class).withId("newProject"),
                            dto.createDto(IdeActionDto.class)
                                .withId("openWelcomePage")
                                .withProperties(
                                    ImmutableMap.of(
                                        "authenticatedTitle",
                                        "Greeting title for authenticated users",
                                        "authenticatedContentUrl",
                                        "http://example.com/content.url")))))
            .withOnProjectsLoaded(
                dto.createDto(OnProjectsLoadedDto.class)
                    .withActions(
                        asList(
                            dto.createDto(IdeActionDto.class)
                                .withId("openFile")
                                .withProperties(singletonMap("file", "pom.xml")),
                            dto.createDto(IdeActionDto.class).withId("run"),
                            dto.createDto(IdeActionDto.class)
                                .withId("findReplace")
                                .withProperties(
                                    ImmutableMap.of(
                                        "in",
                                        "src/main/resources/consts2.properties",
                                        "find",
                                        "OLD_VALUE_2",
                                        "replace",
                                        "NEW_VALUE_2",
                                        "replaceMode",
                                        "mode")))));
    return dto.createDto(FactoryDto.class)
        .withV("4.0")
        .withWorkspace(workspaceConfig)
        .withCreator(dto.createDto(AuthorDto.class).withEmail("email").withName("name"))
        .withPolicies(
            dto.createDto(PoliciesDto.class)
                .withReferer("referrer")
                .withSince(123L)
                .withUntil(123L))
        .withIde(ide);
  }
}
