/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.factory.server.builder;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.factory.FactoryParameter;
import org.eclipse.che.api.factory.server.impl.SourceStorageParametersValidator;
import org.eclipse.che.api.factory.shared.dto.Action;
import org.eclipse.che.api.factory.shared.dto.Author;
import org.eclipse.che.api.factory.shared.dto.Button;
import org.eclipse.che.api.factory.shared.dto.ButtonAttributes;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.factory.shared.dto.Ide;
import org.eclipse.che.api.factory.shared.dto.OnAppClosed;
import org.eclipse.che.api.factory.shared.dto.OnAppLoaded;
import org.eclipse.che.api.factory.shared.dto.OnProjectsLoaded;
import org.eclipse.che.api.factory.shared.dto.Policies;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link org.eclipse.che.api.factory.shared.dto.Factory}
 *
 * @author Alexander Garagatyi
 * @author Sergii Kabashniuk
 */
@SuppressWarnings("deprecation")
@Listeners(MockitoTestNGListener.class)
public class FactoryBuilderTest {

    private static DtoFactory dto = DtoFactory.getInstance();

    private FactoryBuilder factoryBuilder;

    private Factory actual;

    private Factory expected;

    @Mock
    private SourceStorageParametersValidator sourceProjectParametersValidator;

    @BeforeMethod
    public void setUp() throws Exception {
        factoryBuilder = new FactoryBuilder(sourceProjectParametersValidator);
        actual = prepareFactory();

        expected = dto.createDto(Factory.class);
    }

    @Test
    public void shouldBeAbleToValidateV4_0() throws Exception {
        factoryBuilder.checkValid(actual);

        verify(sourceProjectParametersValidator).validate(any(), eq(FactoryParameter.Version.V4_0));
    }

    @Test(expectedExceptions = ApiException.class)
    public void shouldNotValidateUnparseableFactory() throws ApiException, URISyntaxException {
        factoryBuilder.checkValid(null);
    }

    @Test(expectedExceptions = ApiException.class, dataProvider = "setByServerParamsProvider",
          expectedExceptionsMessageRegExp = "You have provided an invalid parameter .* for this version of Factory parameters.*")
    public void shouldNotAllowUsingParamsThatCanBeSetOnlyByServer(Factory factory)
            throws InvocationTargetException, IllegalAccessException, ApiException, NoSuchMethodException {
        factoryBuilder.checkValid(factory);
    }

    @DataProvider(name = "setByServerParamsProvider")
    public static Object[][] setByServerParamsProvider() throws URISyntaxException, IOException, NoSuchMethodException {
        Factory factory = prepareFactory();
        return new Object[][]{
                {dto.clone(factory)
                    .withId("id")},
                {dto.clone(factory)
                    .withCreator(dto.createDto(Author.class)
                                    .withUserId("id"))},
                {dto.clone(factory)
                    .withCreator(dto.createDto(Author.class)
                                    .withCreated(123L))}
        };
    }

    @Test(expectedExceptions = ApiException.class, dataProvider = "notValidParamsProvider")
    public void shouldNotAllowUsingNotValidParams(Factory factory)
            throws InvocationTargetException, IllegalAccessException, ApiException, NoSuchMethodException {
        factoryBuilder.checkValid(factory);
    }

    @DataProvider(name = "notValidParamsProvider")
    public static Object[][] notValidParamsProvider() throws URISyntaxException, IOException, NoSuchMethodException {
        return new Object[][]{};
    }


    @Test
    public void shouldBeAbleToValidateV4_0WithTrackedParamsWithoutAccountIdIfOnPremisesIsEnabled() throws Exception {
        factoryBuilder = new FactoryBuilder(sourceProjectParametersValidator);

        Factory factory = prepareFactory()
                .withPolicies(dto.createDto(Policies.class)
                                 .withReferer("referrer")
                                 .withSince(123L)
                                 .withUntil(123L));

        factoryBuilder.checkValid(factory);
    }

    private static Factory prepareFactory() {
        return dto.createDto(Factory.class)
                  .withV("4.0")
                  .withWorkspace(dto.createDto(WorkspaceConfigDto.class)
                                    .withProjects(Collections.singletonList(dto.createDto(
                                            ProjectConfigDto.class)
                                                                               .withSource(
                                                                                       dto.createDto(
                                                                                               SourceStorageDto.class)
                                                                                          .withType("git")
                                                                                          .withLocation("location"))
                                                                               .withType("type")
                                                                               .withAttributes(singletonMap("key", singletonList("value")))
                                                                               .withDescription("description")
                                                                               .withName("name")
                                                                               .withPath("/path")))
                                    .withAttributes(singletonMap("key", "value"))
                                    .withCommands(singletonList(dto.createDto(CommandDto.class)
                                                                   .withName("command1")
                                                                   .withType("maven")
                                                                   .withCommandLine("mvn test")))
                                    .withDefaultEnv("env1")
                                    .withEnvironments(singletonList(dto.createDto(EnvironmentDto.class)
                                                                              .withName("test")
                                                                              .withMachineConfigs(singletonList(dto.createDto(
                                                                                      MachineConfigDto.class)
                                                                                                                   .withName("name")
                                                                                                                   .withType("docker")
                                                                                                                   .withDev(true)
                                                                                                                   .withSource(
                                                                                                                           dto.createDto(
                                                                                                                                   MachineSourceDto.class)
                                                                                                                              .withType(
                                                                                                                                      "git")
                                                                                                                              .withLocation(
                                                                                                                                      "https://github.com/123/test.git"))))
                                                                              .withRecipe(dto.createDto(
                                                                                      RecipeDto.class)
                                                                                             .withType("sometype")
                                                                                             .withScript("some script")))))
                  .withCreator(dto.createDto(Author.class)
                                  .withAccountId("accountId")
                                  .withEmail("email")
                                  .withName("name"))
                  .withPolicies(dto.createDto(Policies.class)
                                   .withReferer("referrer")
                                   .withSince(123L)
                                   .withUntil(123L))
                  .withButton(dto.createDto(Button.class)
                                 .withType(Button.ButtonType.logo)
                                 .withAttributes(dto.createDto(
                                         ButtonAttributes.class)
                                                    .withColor("color")
                                                    .withCounter(true)
                                                    .withLogo("logo")
                                                    .withStyle("style")))
                  .withIde(dto.createDto(Ide.class)
                              .withOnAppClosed(
                                      dto.createDto(OnAppClosed.class)
                                         .withActions(singletonList(
                                                 dto.createDto(Action.class)
                                                    .withId("warnOnClose"))))
                              .withOnAppLoaded(
                                      dto.createDto(OnAppLoaded.class)
                                         .withActions(Arrays.asList(
                                                 dto.createDto(Action.class)
                                                    .withId("newProject"),
                                                 dto.createDto(Action.class)
                                                    .withId("openWelcomePage")
                                                    .withProperties(
                                                            ImmutableMap.of(
                                                                    "authenticatedTitle",
                                                                    "Greeting title for authenticated users",
                                                                    "authenticatedContentUrl",
                                                                    "http://example.com/content.url")))))
                              .withOnProjectsLoaded(
                                      dto.createDto(OnProjectsLoaded.class)
                                         .withActions(Arrays.asList(
                                                 dto.createDto(Action.class)
                                                    .withId("openFile")
                                                    .withProperties(
                                                            singletonMap(
                                                                    "file",
                                                                    "pom.xml")),
                                                 dto.createDto(Action.class)
                                                    .withId("run"),
                                                 dto.createDto(Action.class)
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
                                                                    "mode"))))));
    }
}
