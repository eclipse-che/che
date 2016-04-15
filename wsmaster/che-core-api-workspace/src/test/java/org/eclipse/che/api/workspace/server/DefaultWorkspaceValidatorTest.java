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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.machine.shared.dto.CommandDto;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.machine.shared.dto.ServerConfDto;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.RecipeDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Tests for {@link WorkspaceValidator} and {@link DefaultWorkspaceValidator}
 *
 * @author Alexander Reshetnyak
 */
public class DefaultWorkspaceValidatorTest {

    private WorkspaceValidator wsValidator;

    @BeforeClass
    public void prepare() throws Exception {
        wsValidator = new DefaultWorkspaceValidator();
    }

    @Test
    public void shouldValidateCorrectWorkspace() throws Exception {
        final WorkspaceConfigDto config = createConfig();


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Workspace name required")
    public void shouldFailValidationIfNameIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.withName(null);


        wsValidator.validateConfig(config);
    }

    @Test(dataProvider = "invalidNameProvider",
          expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Incorrect workspace name, it must be between 3 and 20 characters and may contain digits, " +
                                            "latin letters, underscores, dots, dashes and should start and end only with digits, " +
                                            "latin letters or underscores")
    public void shouldFailValidationIfNameIsInvalid(String name) throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.withName(name);


        wsValidator.validateConfig(config);
    }

    @DataProvider(name = "invalidNameProvider")
    public static Object[][] invalidNameProvider() {
        return new Object[][] {
                {".name"},
                {"name."},
                {"-name"},
                {"name-"},
                {"long-name12345678901234567890"},
                {"_name"},
                {"name_"}
        };
    }

    @Test(dataProvider = "validNameProvider")
    public void shouldValidateCorrectWorkspaceName(String name) throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.withName(name);


        wsValidator.validateConfig(config);
    }

    @DataProvider(name = "validNameProvider")
    public static Object[][] validNameProvider() {
        return new Object[][] {
                {"name"},
                {"quiteLongName1234567"},
                {"name-with-dashes"},
                {"name.with.dots"},
                {"name0with1digits"},
                {"mixed-symbols.name12"},
                {"123456"},
                {"name_name"},
                {"123-456.78"}
        };
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Attribute name 'null' is not valid")
    public void shouldFailValidationIfAttributeNameIsNull() throws Exception {
        final WorkspaceImpl workspace = new WorkspaceImpl("id", "namespace", createConfig());
        workspace.getAttributes().put(null, "value1");


        wsValidator.validateWorkspace(workspace);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Attribute name '' is not valid")
    public void shouldFailValidationIfAttributeNameIsEmpty() throws Exception {
        final WorkspaceImpl workspace = new WorkspaceImpl("id", "namespace", createConfig());
        workspace.getAttributes().put("", "value1");

        wsValidator.validateWorkspace(workspace);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Attribute name '.*' is not valid")
    public void shouldFailValidationIfAttributeNameStartsWithWordCodenvy() throws Exception {
        final WorkspaceImpl workspace = new WorkspaceImpl("id", "namespace", createConfig());
        workspace.getAttributes().put("codenvy_key", "value1");

        wsValidator.validateWorkspace(workspace);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Workspace default environment name required")
    public void shouldFailValidationIfDefaultEnvNameIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.setDefaultEnv(null);


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Workspace default environment name required")
    public void shouldFailValidationIfDefaultEnvNameIsEmpty() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.setDefaultEnv("");


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Workspace default environment configuration required")
    public void shouldFailValidationIfEnvWithDefaultEnvNameIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.setEnvironments(null);


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Environment name should be neither null nor empty")
    public void shouldFailValidationIfEnvNameIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .add(newDto(EnvironmentDto.class).withName(null)
                                               .withMachineConfigs(config.getEnvironments()
                                                                         .get(0)
                                                                         .getMachineConfigs()));


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Environment name should be neither null nor empty")
    public void shouldFailValidationIfEnvNameIsEmpty() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .add(newDto(EnvironmentDto.class).withName("")
                                               .withMachineConfigs(config.getEnvironments()
                                                                         .get(0)
                                                                         .getMachineConfigs()));


        wsValidator.validateConfig(config);
    }

    @Test
    public void shouldNotFailValidationIfEnvironmentRecipeTypeIsDocker() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .withRecipe(newDto(RecipeDto.class).withType("docker"));


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Environment '.*' should contain at least 1 machine")
    public void shouldFailValidationIfMachinesListIsEmpty() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .withMachineConfigs(null);


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Environment should contain exactly 1 dev machine, but '.*' contains '0'")
    public void shouldFailValidationIfNoDevMachineFound() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .getMachineConfigs()
              .stream()
              .filter(MachineConfigDto::isDev)
              .forEach(machine -> machine.withDev(false));


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Environment should contain exactly 1 dev machine, but '.*' contains '2'")
    public void shouldFailValidationIf2DevMachinesFound() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        final Optional<MachineConfigDto> devMachine = config.getEnvironments()
                                                            .get(0)
                                                            .getMachineConfigs()
                                                            .stream()
                                                            .filter(MachineConfigDto::isDev)
                                                            .findAny();
        config.getEnvironments()
              .get(0)
              .getMachineConfigs()
              .add(devMachine.get().withName("other-name"));


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Environment .* contains machine with null or empty name")
    public void shouldFailValidationIfMachineNameIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .getMachineConfigs()
              .get(0)
              .withName(null);


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Environment .* contains machine with null or empty name")
    public void shouldFailValidationIfMachineNameIsEmpty() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .getMachineConfigs()
              .get(0)
              .withName("");


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Environment .* contains machine without source")
    public void shouldFailValidationIfMachineSourceIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .getMachineConfigs()
              .get(0)
              .withSource(null);


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Type .* of machine .* in environment .* is not supported. Supported value are 'docker' and 'ssh'.")
    public void shouldFailValidationIfMachineTypeIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .getMachineConfigs()
              .get(0)
              .withType(null);


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Type .* of machine .* in environment .* is not supported. Supported value are 'docker' and 'ssh'.")
    public void shouldFailValidationIfMachineTypeIsNotDocker() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .getMachineConfigs()
              .get(0)
              .withType("compose");


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Workspace ws-name contains command with null or empty name")
    public void shouldFailValidationIfCommandNameIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getCommands()
              .get(0)
              .withName(null);


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Workspace ws-name contains command with null or empty name")
    public void shouldFailValidationIfCommandNameIsEmpty() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getCommands()
              .get(0)
              .withName(null);


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Command line required for command '.*'")
    public void shouldFailValidationIfCommandLineIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getCommands()
              .get(0)
              .withCommandLine(null);


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Command line required for command '.*'")
    public void shouldFailValidationIfCommandLineIsEmpty() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getCommands()
              .get(0)
              .withCommandLine("");


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Machine .* contains server conf with invalid port .*",
          dataProvider = "invalidPortProvider")
    public void shouldFailValidationIfServerConfPortIsInvalid(String invalidPort) throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .getMachineConfigs()
              .get(0)
              .getServers()
              .add(newDto(ServerConfDto.class).withPort(invalidPort));


        wsValidator.validateConfig(config);
    }

    @DataProvider(name = "invalidPortProvider")
    public static Object[][] invalidPortProvider() {
        return new Object[][] {
                {"0"},
                {"0123"},
                {"012/tcp"},
                {"8080"},
                {"8080/pct"},
                {"8080/pdu"},
                {"/tcp"},
                {"tcp"},
                {""},
                {"8080/tcp1"},
                {"8080/tcpp"},
                {"8080tcp"},
                {"8080/tc"},
                {"8080/ud"},
                {"8080/udpp"},
                {"8080/udp/"},
                {"8080/tcp/"},
                {"8080/tcp/udp"},
                {"8080/tcp/tcp"},
                {"8080/tcp/8080"},
                {null}
        };
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Machine .* contains server conf with invalid protocol .*",
          dataProvider = "invalidProtocolProvider")
    public void shouldFailValidationIfServerConfProtocolIsInvalid(String invalidProtocol) throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .getMachineConfigs()
              .get(0)
              .getServers()
              .add(newDto(ServerConfDto.class).withPort("8080/tcp")
                                              .withProtocol(invalidProtocol));


        wsValidator.validateConfig(config);
    }

    @DataProvider(name = "invalidProtocolProvider")
    public static Object[][] invalidProtocolProvider() {
        return new Object[][] {
                {""},
                {"http!"},
                {"2http"},
                {"http:"},
                };
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Machine %s contains environment variable with null or empty name")
    public void shouldFailValidationIfEnvVarNameIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .getMachineConfigs()
              .get(0)
              .getEnvVariables()
              .put(null, "value");


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Machine %s contains environment variable with null or empty name")
    public void shouldFailValidationIfEnvVarNameIsEmpty() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .getMachineConfigs()
              .get(0)
              .getEnvVariables()
              .put("", "value");


        wsValidator.validateConfig(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Machine %s contains environment variable with null value")
    public void shouldFailValidationIfEnvVarValueIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .getMachineConfigs()
              .get(0)
              .getEnvVariables()
              .put("key", null);


        wsValidator.validateConfig(config);
    }

    private static WorkspaceConfigDto createConfig() {
        final WorkspaceConfigDto workspaceConfigDto = newDto(WorkspaceConfigDto.class).withName("ws-name")
                                                                                      .withDefaultEnv("dev-env");

        final List<ServerConfDto> serversConf = new ArrayList<>(Arrays.asList(newDto(ServerConfDto.class).withRef("ref1")
                                                                                                         .withPort("8080/tcp")
                                                                                                         .withProtocol("https")
                                                                                                         .withPath("some/path"),
                                                                              newDto(ServerConfDto.class).withRef("ref2")
                                                                                                         .withPort("9090/udp")
                                                                                                         .withProtocol("protocol")
                                                                                                         .withPath("/some/path")));
        MachineConfigDto devMachine = newDto(MachineConfigDto.class).withDev(true)
                                                                    .withName("dev-machine")
                                                                    .withType("docker")
                                                                    .withSource(newDto(MachineSourceDto.class).withLocation("location")
                                                                                                              .withType("dockerfile"))
                                                                    .withServers(serversConf)
                                                                    .withEnvVariables(new HashMap<>(singletonMap("key1", "value1")));
        EnvironmentDto devEnv = newDto(EnvironmentDto.class).withName("dev-env")
                                                            .withMachineConfigs(new ArrayList<>(singletonList(devMachine)))
                                                            .withRecipe(null);
        workspaceConfigDto.setEnvironments(new ArrayList<>(singletonList(devEnv)));

        List<CommandDto> commandDtos = new ArrayList<>();
        commandDtos.add(newDto(CommandDto.class).withName("command_name")
                                                .withType("maven")
                                                .withCommandLine("mvn clean install")
                                                .withAttributes(new HashMap<>(singletonMap("cmd-attribute-name", "cmd-attribute-value"))));
        workspaceConfigDto.setCommands(commandDtos);

        return workspaceConfigDto;
    }
}
