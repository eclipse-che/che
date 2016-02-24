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
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.RecipeDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Tests for {@link WorkspaceConfigValidator} and {@link DefaultWorkspaceConfigValidator}
 *
 * @author Alexander Reshetnyak
 */
public class DefaultWorkspaceConfigValidatorTest {

    private WorkspaceConfigValidator wsValidator;

    @BeforeClass
    public void prepare() throws Exception {
        wsValidator = new DefaultWorkspaceConfigValidator();
    }

    @Test
    public void shouldValidateCorrectWorkspace() throws Exception {
        final WorkspaceConfigDto config = createConfig();


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Workspace name required")
    public void shouldFailValidationIfNameIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.withName(null);


        wsValidator.validate(config);
    }

    @Test(dataProvider = "invalidNameProvider",
          expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Incorrect workspace name, it must be between 3 and 20 characters and may contain digits, " +
                                            "latin letters, underscores, dots, dashes and should start and end only with digits, " +
                                            "latin letters or underscores")
    public void shouldFailValidationIfNameIsInvalid(String name) throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.withName(name);


        wsValidator.validate(config);
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


        wsValidator.validate(config);
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
        final WorkspaceConfigDto config = createConfig();
        config.getAttributes()
              .put(null, "value1");


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Attribute name '' is not valid")
    public void shouldFailValidationIfAttributeNameIsEmpty() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getAttributes()
              .put("", "value1");


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Attribute name '.*' is not valid")
    public void shouldFailValidationIfAttributeNameStartsWithWordCodenvy() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getAttributes()
              .put("codenvy_key", "value1");


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Workspace default environment name required")
    public void shouldFailValidationIfDefaultEnvNameIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.setDefaultEnv(null);


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Workspace default environment name required")
    public void shouldFailValidationIfDefaultEnvNameIsEmpty() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.setDefaultEnv("");


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Workspace default environment configuration required")
    public void shouldFailValidationIfEnvWithDefaultEnvNameIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.setEnvironments(null);


        wsValidator.validate(config);
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


        wsValidator.validate(config);
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


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Couldn't start workspace '.*' from environment '.*', environment recipe has unsupported type '.*'")
    public void shouldFailValidationIfEnvironmentRecipeTypeIsNotDocker() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .withRecipe(newDto(RecipeDto.class).withType("kubernetes"));


        wsValidator.validate(config);
    }

    @Test
    public void shouldNotFailValidationIfEnvironmentRecipeTypeIsDocker() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .withRecipe(newDto(RecipeDto.class).withType("docker"));


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Environment '.*' should contain at least 1 machine")
    public void shouldFailValidationIfMachinesListIsEmpty() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments()
              .get(0)
              .withMachineConfigs(null);


        wsValidator.validate(config);
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


        wsValidator.validate(config);
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


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Environment .* contains machine with null or empty name")
    public void shouldFailValidationIfMachineNameIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments().get(0).getMachineConfigs().get(0).withName(null);


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Environment .* contains machine with null or empty name")
    public void shouldFailValidationIfMachineNameIsEmpty() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments().get(0).getMachineConfigs().get(0).withName("");


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Environment .* contains machine without source")
    public void shouldFailValidationIfMachineSourceIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments().get(0).getMachineConfigs().get(0).withSource(null);


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Type of machine .* in environment .* is not supported. Supported value is 'docker'.")
    public void shouldFailValidationIfMachineTypeIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments().get(0).getMachineConfigs().get(0).withType(null);


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Type of machine .* in environment .* is not supported. Supported value is 'docker'.")
    public void shouldFailValidationIfMachineTypeIsNotDocker() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getEnvironments().get(0).getMachineConfigs().get(0).withType("compose");


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Workspace .* contains command with null or empty name")
    public void shouldFailValidationIfCommandNameIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getCommands().get(0).withName(null);


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Workspace .* contains command with null or empty name")
    public void shouldFailValidationIfCommandNameIsEmpty() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getCommands().get(0).withName(null);


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Command line required for command .* in workspace .*")
    public void shouldFailValidationIfCommandLineIsNull() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getCommands().get(0).withCommandLine(null);


        wsValidator.validate(config);
    }

    @Test(expectedExceptions = BadRequestException.class,
          expectedExceptionsMessageRegExp = "Command line required for command .* in workspace .*")
    public void shouldFailValidationIfCommandLineIsEmpty() throws Exception {
        final WorkspaceConfigDto config = createConfig();
        config.getCommands().get(0).withCommandLine("");


        wsValidator.validate(config);
    }

    private static WorkspaceConfigDto createConfig() {
        final WorkspaceConfigDto workspaceConfigDto = newDto(WorkspaceConfigDto.class).withName("ws-name")
                                                                                      .withDefaultEnv("dev-env");

        MachineConfigDto devMachine = newDto(MachineConfigDto.class).withDev(true)
                                                                    .withName("dev-machine")
                                                                    .withType("docker")
                                                                    .withSource(newDto(MachineSourceDto.class).withLocation("location")
                                                                                                              .withType("recipe"));
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

        workspaceConfigDto.withAttributes(new HashMap<>(singletonMap("ws-attribute-name", "ws-attribute-value")));

        return workspaceConfigDto;
    }
}
