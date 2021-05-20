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
package org.eclipse.che.api.workspace.server;

import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_REQUEST_ATTRIBUTE;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.api.workspace.shared.dto.CommandDto;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.MachineConfigDto;
import org.eclipse.che.api.workspace.shared.dto.RecipeDto;
import org.eclipse.che.api.workspace.shared.dto.ServerConfigDto;
import org.eclipse.che.api.workspace.shared.dto.VolumeDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link WorkspaceValidator}.
 *
 * @author Alexander Reshetnyak
 */
@Listeners(MockitoTestNGListener.class)
public class WorkspaceValidatorTest {

  @InjectMocks private WorkspaceValidator wsValidator;

  @Test
  public void shouldValidateCorrectWorkspace() throws Exception {
    final WorkspaceConfigDto config = createConfig();

    wsValidator.validateConfig(config);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Workspace name required")
  public void shouldFailValidationIfNameIsNull() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.withName(null);

    wsValidator.validateConfig(config);
  }

  @Test(
      dataProvider = "invalidNameProvider",
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Incorrect workspace name, it must be between 3 and 100 "
              + "characters and may contain digits, latin letters, underscores, dots, dashes and must "
              + "start and end only with digits, latin letters or underscores")
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
      {
        "long-name1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
      },
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

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Attribute name 'null' is not valid")
  public void shouldFailValidationIfAttributeNameIsNull() throws Exception {
    Map<String, String> attributes = new HashMap<>();
    attributes.put(null, "value1");

    wsValidator.validateAttributes(attributes);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Attribute name '' is not valid")
  public void shouldFailValidationIfAttributeNameIsEmpty() throws Exception {
    wsValidator.validateAttributes(ImmutableMap.of("", "value1"));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Attribute name 'codenvy_key' is not valid")
  public void shouldFailValidationIfAttributeNameStartsWithWordCodenvy() throws Exception {
    wsValidator.validateAttributes(ImmutableMap.of("codenvy_key", "value1"));
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Workspace default environment name required")
  public void shouldFailValidationOfChe6WSIfDefaultEnvNameIsNull() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.setDefaultEnv(null);

    wsValidator.validateConfig(config);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Workspace default environment name required")
  public void shouldFailValidationOfChe6WSIfDefaultEnvNameIsEmpty() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.setDefaultEnv("");

    wsValidator.validateConfig(config);
  }

  @Test
  public void shouldNotFailValidationOfChe7WSIfDefaultEnvNameIsNullAndNoEnvIsPresent()
      throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.setDefaultEnv(null);
    config.getEnvironments().clear();
    config.getAttributes().put(Constants.WORKSPACE_TOOLING_EDITOR_ATTRIBUTE, "something");

    wsValidator.validateConfig(config);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Workspace default environment configuration required")
  public void shouldFailValidationIfEnvWithDefaultEnvNameIsNull() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.setEnvironments(null);

    wsValidator.validateConfig(config);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Workspace ws-name contains command with null or empty name")
  public void shouldFailValidationIfCommandNameIsNull() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.getCommands().get(0).withName(null);

    wsValidator.validateConfig(config);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Value '.*' of attribute '" + MEMORY_LIMIT_ATTRIBUTE + "' in machine '.*' is illegal",
      dataProvider = "illegalMemoryAttributeValueProvider")
  public void shouldFailValidationIfMemoryLimitMachineAttributeHasIllegalValue(
      String attributeValue) throws Exception {
    final WorkspaceConfigDto config = createConfig();
    EnvironmentDto env = config.getEnvironments().values().iterator().next();
    MachineConfigDto machine = env.getMachines().values().iterator().next();
    machine.getAttributes().put(MEMORY_LIMIT_ATTRIBUTE, attributeValue);

    wsValidator.validateConfig(config);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Value '.*' of attribute '" + MEMORY_REQUEST_ATTRIBUTE + "' in machine '.*' is illegal",
      dataProvider = "illegalMemoryAttributeValueProvider")
  public void shouldFailValidationIfMemoryRequestMachineAttributeHasIllegalValue(
      String attributeValue) throws Exception {
    final WorkspaceConfigDto config = createConfig();
    EnvironmentDto env = config.getEnvironments().values().iterator().next();
    MachineConfigDto machine = env.getMachines().values().iterator().next();
    machine.getAttributes().put(MEMORY_REQUEST_ATTRIBUTE, attributeValue);

    wsValidator.validateConfig(config);
  }

  @DataProvider(name = "illegalMemoryAttributeValueProvider")
  public static Object[][] illegalMemoryAttributeValueProvider() {
    return new Object[][] {{"text"}, {""}, {"123MB"}, {"123GB"}, {"123KB"}};
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Workspace ws-name contains command with null or empty name")
  public void shouldFailValidationIfCommandNameIsEmpty() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.getCommands().get(0).withName(null);

    wsValidator.validateConfig(config);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Command line or content required for command '.*' in workspace '.*'\\.")
  public void shouldFailValidationIfCommandLineIsNull() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.getCommands().get(0).withCommandLine(null);

    wsValidator.validateConfig(config);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Command line or content required for command '.*' in workspace '.*'\\.")
  public void shouldFailValidationIfCommandLineIsEmpty() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    config.getCommands().get(0).withCommandLine("");

    wsValidator.validateConfig(config);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Volume name '.*' in machine '.*' is invalid",
      dataProvider = "illegalVolumeNameProvider")
  public void shouldFailValidationIfVolumeNameDoesNotMatchCriteria(String volumeName)
      throws Exception {
    final WorkspaceConfigDto config = createConfig();
    EnvironmentDto env = config.getEnvironments().values().iterator().next();
    MachineConfigDto machine = env.getMachines().values().iterator().next();
    machine.getVolumes().put(volumeName, newDto(VolumeDto.class).withPath("/path"));

    wsValidator.validateConfig(config);
  }

  @DataProvider(name = "illegalVolumeNameProvider")
  public static Object[][] illegalVolumeNameProvider() {
    return new Object[][] {
      {"0begin_with_number"},
      {"begin_with_dot."},
      {"begin_with_underscore_"},
      {"begin_with_hyphen-"},
      {"with_@_special_char"},
      {"with_@_special_char"},
      {"veryveryveryveryveryveryverylongname"},
      {"volume/name"},
    };
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Path of volume '.*' in machine '.*' is invalid. It should not be empty")
  public void shouldFailValidationIfVolumeValueIsEmpty() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    EnvironmentDto env = config.getEnvironments().values().iterator().next();
    MachineConfigDto machine = env.getMachines().values().iterator().next();
    machine.getVolumes().put("volume1", null);

    wsValidator.validateConfig(config);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Path of volume '.*' in machine '.*' is invalid. It should not be empty")
  public void shouldFailValidationIfVolumePathIsEmpty() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    EnvironmentDto env = config.getEnvironments().values().iterator().next();
    MachineConfigDto machine = env.getMachines().values().iterator().next();
    machine.getVolumes().put("volume1", newDto(VolumeDto.class).withPath(""));

    wsValidator.validateConfig(config);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Path of volume '.*' in machine '.*' is invalid. It should not be empty")
  public void shouldFailValidationIfVolumePathIsNull() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    EnvironmentDto env = config.getEnvironments().values().iterator().next();
    MachineConfigDto machine = env.getMachines().values().iterator().next();
    machine.getVolumes().put("volume1", newDto(VolumeDto.class).withPath(null));

    wsValidator.validateConfig(config);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Path '.*' of volume '.*' in machine '.*' is invalid. It should be absolute")
  public void shouldFailValidationIfVolumePathIsNotAbsolute() throws Exception {
    final WorkspaceConfigDto config = createConfig();
    EnvironmentDto env = config.getEnvironments().values().iterator().next();
    MachineConfigDto machine = env.getMachines().values().iterator().next();
    machine.getVolumes().put("volume1", newDto(VolumeDto.class).withPath("not/absolute/path"));

    wsValidator.validateConfig(config);
  }

  private static WorkspaceConfigDto createConfig() {
    final WorkspaceConfigDto workspaceConfigDto =
        newDto(WorkspaceConfigDto.class).withName("ws-name").withDefaultEnv("dev-env");

    MachineConfigDto machineConfig =
        newDto(MachineConfigDto.class)
            .withServers(
                singletonMap(
                    "ref1",
                    newDto(ServerConfigDto.class)
                        .withPort("8080/tcp")
                        .withProtocol("https")
                        .withAttributes(singletonMap("key", "value"))))
            .withAttributes(new HashMap<>(singletonMap(MEMORY_LIMIT_ATTRIBUTE, "1000000")));
    EnvironmentDto env =
        newDto(EnvironmentDto.class)
            .withMachines(singletonMap("devmachine1", machineConfig))
            .withRecipe(
                newDto(RecipeDto.class)
                    .withType("type")
                    .withContent("content")
                    .withContentType("content type"));
    workspaceConfigDto.setEnvironments(new HashMap<>(singletonMap("dev-env", env)));

    List<CommandDto> commandDtos = new ArrayList<>();
    commandDtos.add(
        newDto(CommandDto.class)
            .withName("command_name")
            .withType("maven")
            .withCommandLine("mvn clean install")
            .withAttributes(
                new HashMap<>(singletonMap("cmd-attribute-name", "cmd-attribute-value"))));
    workspaceConfigDto.setCommands(commandDtos);

    return workspaceConfigDto;
  }
}
