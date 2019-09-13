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
package org.eclipse.che.workspace.infrastructure.docker.environment.compose;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.workspace.infrastructure.docker.environment.WsAgentMachineFinderUtil;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.BuildContext;
import org.eclipse.che.workspace.infrastructure.docker.environment.compose.model.ComposeService;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class ComposeEnvironmentValidatorTest {
  private static final String MACHINE_NAME = "machine1";

  @Mock private ComposeEnvironment composeEnv;
  @Mock private InternalMachineConfig machineConfig;
  @Mock private ComposeService service;
  @Mock private BuildContext buildContext;
  @Mock private InstallerImpl installer;
  @Mock private ServerConfigImpl server;

  private ComposeEnvironmentValidator composeEnvironmentValidator =
      new ComposeEnvironmentValidator();

  @BeforeMethod
  public void setUp() throws Exception {
    when(composeEnv.getMachines()).thenReturn(singletonMap(MACHINE_NAME, machineConfig));
    when(composeEnv.getServices())
        .thenReturn(newLinkedHashMap(singletonMap(MACHINE_NAME, service)));
    when(service.getImage()).thenReturn("test/image:latest");
    when(machineConfig.getInstallers()).thenReturn(singletonList(installer));
    when(installer.getId()).thenReturn(WsAgentMachineFinderUtil.WS_AGENT_INSTALLER);
    when(server.getPort()).thenReturn("8080/tcp");
    when(server.getPath()).thenReturn("/some/path");
    when(server.getProtocol()).thenReturn("https");
    when(server.getAttributes()).thenReturn(singletonMap("key", "value"));
  }

  @Test
  public void shouldSucceedOnValidationOfValidEnvironment() throws Exception {
    // given
    String machine2Name = "anotherMachine";
    ComposeService container2 = mock(ComposeService.class);
    when(container2.getImage()).thenReturn("test/image:latest");
    when(composeEnv.getMachines()).thenReturn(singletonMap(MACHINE_NAME, machineConfig));
    when(composeEnv.getServices())
        .thenReturn(
            newLinkedHashMap(ImmutableMap.of(MACHINE_NAME, service, machine2Name, container2)));
    ServerConfigImpl server =
        new ServerConfigImpl()
            .withPort("8080")
            .withPath("/some/path")
            .withProtocol("https")
            .withAttributes(singletonMap("key", "value"));
    when(machineConfig.getServers())
        .thenReturn(singletonMap(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, server));
    Map<String, String> attributes =
        ImmutableMap.of("testKey", "value", MEMORY_LIMIT_ATTRIBUTE, "1000000000");
    when(machineConfig.getAttributes()).thenReturn(attributes);
    when(service.getExpose()).thenReturn(ImmutableSet.of("8090", "9090/tcp", "7070/udp"));
    when(service.getLinks()).thenReturn(singletonList(machine2Name + ":alias1"));
    when(service.getDependsOn()).thenReturn(singletonList(machine2Name));
    when(service.getVolumesFrom()).thenReturn(singletonList(machine2Name + ":ro"));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Environment should contain at least 1 service")
  public void shouldFailIfContainersListIsEmpty() throws Exception {
    // given
    when(composeEnv.getServices()).thenReturn(newLinkedHashMap());

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Environment should contain at least 1 service")
  public void shouldFailIfContainersListIsNull() throws Exception {
    // given
    when(composeEnv.getServices()).thenReturn(null);

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Environment contains machines that are missing in environment recipe: .*")
  public void shouldFailIfMachineIsNotInServicesList() throws Exception {
    // given
    when(composeEnv.getMachines()).thenReturn(singletonMap(MACHINE_NAME, machineConfig));
    when(composeEnv.getServices()).thenReturn(newLinkedHashMap(singletonMap("machine2", service)));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      dataProvider = "badServiceNameProvider",
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Name of service '.*' in environment is invalid")
  public void shouldFailIfContainerNameIsInvalid(String name) throws Exception {
    // given
    when(composeEnv.getServices()).thenReturn(newLinkedHashMap(singletonMap(name, service)));
    when(composeEnv.getMachines()).thenReturn(singletonMap(name, machineConfig));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @DataProvider(name = "badServiceNameProvider")
  public static Object[][] badServiceNameProvider() {
    return new Object[][] {
      {"name with-space"}, {""}, {"name%symbol"}, {"name|symbol"},
    };
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Field 'image' or 'build.context' is required in service '.*' in environment")
  public void shouldFailIfNeitherImageNorBuildAreProvided() throws Exception {
    // given
    when(service.getBuild()).thenReturn(null);
    when(service.getImage()).thenReturn(null);

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Service '.*' in environment contains mutually exclusive dockerfile content and build context.")
  public void shouldFailIfBothDockerfileAndBuildContextAreProvided() throws Exception {
    // given
    when(service.getImage()).thenReturn(null);
    when(service.getBuild()).thenReturn(buildContext);
    when(buildContext.getContext()).thenReturn("some value");
    when(buildContext.getDockerfile()).thenReturn("FROM ubuntu");

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test
  public void shouldPassIfServiceFromRecipeIsNotInTheListOfMachines() throws Exception {
    // given
    ComposeService service2 = mock(ComposeService.class);
    when(service2.getImage()).thenReturn("test/image:latest");
    when(composeEnv.getMachines()).thenReturn(singletonMap(MACHINE_NAME, machineConfig));
    when(composeEnv.getServices())
        .thenReturn(
            newLinkedHashMap(ImmutableMap.of(MACHINE_NAME, service, "anotherContainer", service2)));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Exposed port '.*' in service '.*' in environment is invalid",
      dataProvider = "invalidExposeProvider")
  public void shouldFailIfContainerExposeIsInvalid(String expose) throws Exception {
    // given
    when(service.getExpose()).thenReturn(ImmutableSet.of(expose));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @DataProvider(name = "invalidExposeProvider")
  public static Object[][] invalidExposeProvider() {
    return new Object[][] {{"0"}, {"8080/ttp"}, {"8080/"}, {"0111"}, {"tcp"}, {"/tcp"}};
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Link '.*' in service '.*' in environment is invalid")
  public void shouldFailIfContainerLinkIsInvalid() throws Exception {
    // given
    when(service.getLinks()).thenReturn(singletonList(MACHINE_NAME + "->alias1"));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Container '.*' has illegal link to itself")
  public void shouldFailIfContainerHasLinkToItself() throws Exception {
    // given
    when(service.getLinks()).thenReturn(singletonList(MACHINE_NAME + ":alias1"));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Service '.*' in environment contains link to non existing service '.*'")
  public void shouldFailIfContainerLinkContainsMissingContainer() throws Exception {
    // given
    when(service.getLinks()).thenReturn(singletonList("nonExistingContainer:alias1"));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Dependency '.*' in service '.*' in environment is invalid")
  public void shouldFailIfContainerDependencyIsInvalid() throws Exception {
    // given
    when(service.getDependsOn()).thenReturn(singletonList("!--service"));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Container '.*' has illegal dependency to itself")
  public void shouldFailIfContainerHasDependencyToItself() throws Exception {
    // given
    when(service.getDependsOn()).thenReturn(singletonList(MACHINE_NAME));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Service '.*' in environment contains dependency to non existing service '.*'")
  public void shouldFailIfContainerDependencyContainsMissingContainer() throws Exception {
    // given
    when(service.getDependsOn()).thenReturn(singletonList("nonExistingContainer"));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Service name '.*' in field 'volumes_from' of service '.*' in environment is invalid")
  public void shouldFailIfContainerVolumeFromIsInvalid() throws Exception {
    // given
    when(service.getVolumesFrom()).thenReturn(singletonList(MACHINE_NAME + ":777"));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Service '.*' in environment contains non existing service '.*' in 'volumes_from' field")
  public void shouldFailIfContainerVolumeFromContainsMissingContainer() throws Exception {
    // given
    when(service.getVolumesFrom()).thenReturn(singletonList("nonExistingContainer"));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp = "Container '.*' can not mount volume from itself")
  public void shouldFailIfContainerHasVolumeFromItself() throws Exception {
    // given
    when(service.getVolumesFrom()).thenReturn(singletonList(MACHINE_NAME));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Ports binding is forbidden but found in service '.*' of environment")
  public void shouldFailIfThereIsPortBindingInContainer() throws Exception {
    // given
    when(service.getPorts()).thenReturn(singletonList("8080:8080"));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Volumes binding is forbidden but found in service '.*' of environment")
  public void shouldFailIfThereIsVolumeBindingInContainer() throws Exception {
    // given
    when(service.getVolumes()).thenReturn(singletonList("/etc:/etc"));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Networks configuration is forbidden but found in service '.*' of environment")
  public void shouldFailIfThereIsNetworkInContainer() throws Exception {
    // given
    when(service.getNetworks()).thenReturn(singletonList("newNetwork"));

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "Memory limit less than memory request found in service '.*' of environment")
  public void shouldFailIfMemoryLimitIsLessThenMemoryRequirement() throws Exception {
    // given
    when(service.getMemLimit()).thenReturn(1048576L);
    when(service.getMemRequest()).thenReturn(2097152L);

    // when
    composeEnvironmentValidator.validate(composeEnv);
  }
}
