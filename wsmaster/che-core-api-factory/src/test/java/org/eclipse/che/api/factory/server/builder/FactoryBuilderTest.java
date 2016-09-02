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
import org.eclipse.che.api.machine.shared.dto.ServerConfDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentRecipeDto;
import org.eclipse.che.api.workspace.shared.dto.ExtendedMachineDto;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
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

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
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

    @Test(dataProvider = "setByServerParamsProvider")
    public void shouldAllowUsingParamsThatCanBeSetOnlyByServerDuringUpdate(Factory factory)
            throws InvocationTargetException, IllegalAccessException, ApiException, NoSuchMethodException {
        factoryBuilder.checkValid(factory, true);
    }

    @DataProvider(name = "setByServerParamsProvider")
    public static Object[][] setByServerParamsProvider() throws URISyntaxException, IOException, NoSuchMethodException {
        Factory factory = prepareFactory();
        return new Object[][] {
                {dto.clone(factory).withId("id")},
                {dto.clone(factory).withCreator(dto.createDto(Author.class)
                                                   .withUserId("id"))},
                {dto.clone(factory).withCreator(dto.createDto(Author.class)
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
        Factory factory = prepareFactory();
        EnvironmentDto environmentDto = factory.getWorkspace().getEnvironments().values().iterator().next();
        environmentDto.getRecipe().withType(null);

        return new Object[][] {
                {dto.clone(factory).withWorkspace(factory.getWorkspace().withDefaultEnv(null)) },
                {dto.clone(factory).withWorkspace(factory.getWorkspace().withEnvironments(singletonMap("test", environmentDto))) }
        };
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
        ProjectConfigDto project = dto.createDto(ProjectConfigDto.class)
                                      .withSource(dto.createDto(SourceStorageDto.class)
                                                     .withType("git")
                                                     .withLocation("location"))
                                      .withType("type")
                                      .withAttributes(singletonMap("key", singletonList("value")))
                                      .withDescription("description")
                                      .withName("name")
                                      .withPath("/path");
        MachineConfigDto machineConfig = dto.createDto(MachineConfigDto.class)
                                            .withName("name")
                                            .withType("docker")
                                            .withDev(true)
                                            .withSource(dto.createDto(MachineSourceDto.class)
                                                           .withType("git")
                                                           .withLocation("https://github.com/123/test.git"))
                                            .withServers(asList(newDto(ServerConfDto.class).withRef("ref1")
                                                                                           .withPort("8080")
                                                                                           .withProtocol("https"),
                                                                newDto(ServerConfDto.class).withRef("ref2")
                                                                                           .withPort("9090/udp")
                                                                                           .withProtocol("someprotocol")))
                                            .withEnvVariables(singletonMap("key1", "value1"));
        EnvironmentDto environment = dto.createDto(EnvironmentDto.class)
                                        .withRecipe(newDto(EnvironmentRecipeDto.class).withType("compose")
                                                                                      .withContentType("application/x-yaml")
                                                                                      .withContent("some content"))
                                        .withMachines(singletonMap("devmachine",
                                                                   newDto(ExtendedMachineDto.class).withAgents(singletonList("ws-agent"))));

        WorkspaceConfigDto workspaceConfig = dto.createDto(WorkspaceConfigDto.class)
                                                .withProjects(singletonList(project))
                                                .withCommands(singletonList(dto.createDto(CommandDto.class)
                                                                               .withName("command1")
                                                                               .withType("maven")
                                                                               .withCommandLine("mvn test")))
                                                .withDefaultEnv("env1")
                                                .withEnvironments(singletonMap("test", environment));
        Ide ide = dto.createDto(Ide.class)
                     .withOnAppClosed(dto.createDto(OnAppClosed.class)
                                         .withActions(singletonList(dto.createDto(Action.class).withId("warnOnClose"))))
                     .withOnAppLoaded(dto.createDto(OnAppLoaded.class)
                                         .withActions(asList(dto.createDto(Action.class)
                                                                .withId("newProject"),
                                                             dto.createDto(Action.class)
                                                                .withId("openWelcomePage")
                                                                .withProperties(ImmutableMap.of(
                                                                        "authenticatedTitle",
                                                                        "Greeting title for authenticated users",
                                                                        "authenticatedContentUrl",
                                                                        "http://example.com/content.url")))))
                     .withOnProjectsLoaded(dto.createDto(OnProjectsLoaded.class)
                                              .withActions(asList(dto.createDto(Action.class)
                                                                     .withId("openFile")
                                                                     .withProperties(singletonMap("file", "pom.xml")),
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
                                                                                     "mode")))));
        return dto.createDto(Factory.class)
                  .withV("4.0")
                  .withWorkspace(workspaceConfig)
                  .withCreator(dto.createDto(Author.class)
                                  .withEmail("email")
                                  .withName("name"))
                  .withPolicies(dto.createDto(Policies.class)
                                   .withReferer("referrer")
                                   .withSince(123L)
                                   .withUntil(123L))
                  .withButton(dto.createDto(Button.class)
                                 .withType(Button.ButtonType.logo)
                                 .withAttributes(dto.createDto(ButtonAttributes.class)
                                                    .withColor("color")
                                                    .withCounter(true)
                                                    .withLogo("logo")
                                                    .withStyle("style")))
                  .withIde(ide);
    }
}
