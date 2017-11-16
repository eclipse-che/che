/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.docker.environment;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.MEMORY_LIMIT_ATTRIBUTE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.workspace.server.WsAgentMachineFinderUtil;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerBuildContext;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class EnvironmentValidatorTest {
  static final String MACHINE_NAME = "machine1";

  @Mock InternalEnvironment environment;
  @Mock DockerEnvironment dockerEnvironment;
  @Mock InternalMachineConfig machineConfig;
  @Mock DockerContainerConfig container;
  @Mock DockerBuildContext buildContext;
  @Mock InstallerImpl installer;
  @Mock ServerConfigImpl server;

  private EnvironmentValidator environmentValidator = new EnvironmentValidator();

  @BeforeMethod
  public void setUp() throws Exception {
    when(environment.getMachines()).thenReturn(singletonMap(MACHINE_NAME, machineConfig));
    when(dockerEnvironment.getContainers()).thenReturn(singletonMap(MACHINE_NAME, container));
    when(container.getImage()).thenReturn("test/image:latest");
    when(machineConfig.getInstallers()).thenReturn(singletonList(installer));
    when(installer.getId()).thenReturn(WsAgentMachineFinderUtil.WS_AGENT_INSTALLER);
    when(server.getPort()).thenReturn("8080/tcp");
    when(server.getPath()).thenReturn("/some/path");
    when(server.getProtocol()).thenReturn("https");
  }

  @Test
  public void shouldSucceedOnValidationOfValidEnvironment() throws Exception {
    // given
    String machine2Name = "anotherMachine";
    DockerContainerConfig container2 = mock(DockerContainerConfig.class);
    when(container2.getImage()).thenReturn("test/image:latest");
    when(environment.getMachines()).thenReturn(singletonMap(MACHINE_NAME, machineConfig));
    when(dockerEnvironment.getContainers())
        .thenReturn(ImmutableMap.of(MACHINE_NAME, container, machine2Name, container2));
    ServerConfigImpl server =
        new ServerConfigImpl().withPort("8080").withPath("/some/path").withProtocol("https");
    when(machineConfig.getServers())
        .thenReturn(singletonMap(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, server));
    Map<String, String> attributes =
        ImmutableMap.of("testKey", "value", MEMORY_LIMIT_ATTRIBUTE, "1000000000");
    when(machineConfig.getAttributes()).thenReturn(attributes);
    when(container.getExpose()).thenReturn(asList("8090", "9090/tcp", "7070/udp"));
    when(container.getLinks()).thenReturn(singletonList(machine2Name + ":alias1"));
    when(container.getDependsOn()).thenReturn(singletonList(machine2Name));
    when(container.getVolumesFrom()).thenReturn(singletonList(machine2Name + ":ro"));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Environment should contain at least 1 container"
  )
  public void shouldFailIfContainersListIsEmpty() throws Exception {
    // given
    when(dockerEnvironment.getContainers()).thenReturn(emptyMap());

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Environment should contain at least 1 container"
  )
  public void shouldFailIfContainersListIsNull() throws Exception {
    // given
    when(dockerEnvironment.getContainers()).thenReturn(null);

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Environment should contain at least 1 machine"
  )
  public void shouldFailIfMachinesListIsEmpty() throws Exception {
    // given
    when(environment.getMachines()).thenReturn(emptyMap());

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Environment should contain at least 1 machine"
  )
  public void shouldFailIfMachinesListIsNull() throws Exception {
    // given
    when(environment.getMachines()).thenReturn(null);

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Environment contains machines that are missing in environment recipe: .*"
  )
  public void shouldFailIfMachineIsNotInContainersList() throws Exception {
    // given
    when(environment.getMachines()).thenReturn(singletonMap(MACHINE_NAME, machineConfig));
    when(dockerEnvironment.getContainers()).thenReturn(singletonMap("machine2", container));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Environment should contain exactly 1 machine with wsagent, but contains '.*'. All machines with this agent: .*",
    dataProvider = "severalWsAgentsProvider"
  )
  public void shouldFailIfThereIsMoreThan1MachineWithWsAgent(
      Map<String, InternalMachineConfig> machines, Map<String, DockerContainerConfig> containers)
      throws Exception {
    // given
    when(environment.getMachines()).thenReturn(machines);
    when(dockerEnvironment.getContainers()).thenReturn(containers);

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @DataProvider(name = "severalWsAgentsProvider")
  public static Object[][] severalWsAgentsProvider() {
    return new Object[][] {
      {
        ImmutableMap.of(
            "machine1", machineMockWithServers(Constants.SERVER_WS_AGENT_HTTP_REFERENCE),
            "machine2", machineMockWithServers(Constants.SERVER_WS_AGENT_HTTP_REFERENCE)),
        ImmutableMap.of(
            "machine1", containerMock(),
            "machine2", containerMock())
      },
      {
        ImmutableMap.of(
            "machine1", machineMockWithInstallers(WsAgentMachineFinderUtil.WS_AGENT_INSTALLER),
            "machine2", machineMockWithServers(Constants.SERVER_WS_AGENT_HTTP_REFERENCE)),
        ImmutableMap.of(
            "machine1", containerMock(),
            "machine2", containerMock())
      },
      {
        ImmutableMap.of(
            "machine1", machineMockWithInstallers(WsAgentMachineFinderUtil.WS_AGENT_INSTALLER),
            "machine2", machineMockWithInstallers(WsAgentMachineFinderUtil.WS_AGENT_INSTALLER)),
        ImmutableMap.of(
            "machine1", containerMock(),
            "machine2", containerMock())
      }
    };
  }

  @Test
  public void shouldPassIfWsAgentServerAndInstallerAreInTheSameMachine() throws Exception {
    // given
    when(machineConfig.getServers())
        .thenReturn(singletonMap(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, server));
    when(machineConfig.getInstallers()).thenReturn(singletonList(installer));
    when(installer.getId()).thenReturn(WsAgentMachineFinderUtil.WS_AGENT_INSTALLER);

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    dataProvider = "badMachineNameProvider",
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Name of machine '.*' in environment is invalid"
  )
  public void shouldFailIfContainerNameIsInvalid(String name) throws Exception {
    // given
    when(dockerEnvironment.getContainers()).thenReturn(singletonMap(name, container));
    when(environment.getMachines()).thenReturn(singletonMap(name, machineConfig));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @DataProvider(name = "badMachineNameProvider")
  public static Object[][] badMachineNameProvider() {
    return new Object[][] {
      {"name with-space"}, {""}, {"name%symbol"}, {"name|symbol"},
    };
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Field 'image' or 'build.context' is required in machine '.*' in environment"
  )
  public void shouldFailIfNeitherImageNorBuildAreProvided() throws Exception {
    // given
    when(container.getBuild()).thenReturn(null);
    when(container.getImage()).thenReturn(null);

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Machine '.*' in environment contains mutually exclusive dockerfile content and build context."
  )
  public void shouldFailIfBothDockerfileAndBuildContextAreProvided() throws Exception {
    // given
    when(container.getImage()).thenReturn(null);
    when(container.getBuild()).thenReturn(buildContext);
    when(buildContext.getContext()).thenReturn("some value");
    when(buildContext.getDockerfileContent()).thenReturn("FROM ubuntu");

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test
  public void shouldPassIfMachineFromRecipeIsNotInTheListOfMachines() throws Exception {
    // given
    DockerContainerConfig container2 = mock(DockerContainerConfig.class);
    when(container2.getImage()).thenReturn("test/image:latest");
    when(environment.getMachines()).thenReturn(singletonMap(MACHINE_NAME, machineConfig));
    when(dockerEnvironment.getContainers())
        .thenReturn(ImmutableMap.of(MACHINE_NAME, container, "anotherContainer", container2));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Machine '.*' in environment contains server conf '.*' with invalid port '.*'"
  )
  public void shouldFailIfServerPortInMachineIsInvalid() throws Exception {
    // given
    when(server.getPort()).thenReturn("aaaaa");
    when(machineConfig.getServers())
        .thenReturn(singletonMap(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, server));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Machine '.*' in environment contains server conf '.*' with invalid protocol '.*'"
  )
  public void shouldFailIfServerProtocolInMachineIsInvalid() throws Exception {
    // given
    when(machineConfig.getServers()).thenReturn(singletonMap("server1", server));
    when(server.getProtocol()).thenReturn("0");

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Exposed port '.*' in machine '.*' in environment is invalid",
    dataProvider = "invalidExposeProvider"
  )
  public void shouldFailIfContainerExposeIsInvalid(String expose) throws Exception {
    // given
    when(container.getExpose()).thenReturn(singletonList(expose));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @DataProvider(name = "invalidExposeProvider")
  public static Object[][] invalidExposeProvider() {
    return new Object[][] {{"0"}, {"8080/ttp"}, {"8080/"}, {"0111"}, {"tcp"}, {"/tcp"}};
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Link '.*' in machine '.*' in environment is invalid"
  )
  public void shouldFailIfContainerLinkIsInvalid() throws Exception {
    // given
    when(container.getLinks()).thenReturn(singletonList(MACHINE_NAME + "->alias1"));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Container '.*' has illegal link to itself"
  )
  public void shouldFailIfContainerHasLinkToItself() throws Exception {
    // given
    when(container.getLinks()).thenReturn(singletonList(MACHINE_NAME + ":alias1"));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Machine '.*' in environment contains link to non existing machine '.*'"
  )
  public void shouldFailIfContainerLinkContainsMissingContainer() throws Exception {
    // given
    when(container.getLinks()).thenReturn(singletonList("nonExistingContainer:alias1"));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Dependency '.*' in machine '.*' in environment is invalid"
  )
  public void shouldFailIfContainerDependencyIsInvalid() throws Exception {
    // given
    when(container.getDependsOn()).thenReturn(singletonList("--container"));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Container '.*' has illegal dependency to itself"
  )
  public void shouldFailIfContainerHasDependencyToItself() throws Exception {
    // given
    when(container.getDependsOn()).thenReturn(singletonList(MACHINE_NAME));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Machine '.*' in environment contains dependency to non existing machine '.*'"
  )
  public void shouldFailIfContainerDependencyContainsMissingContainer() throws Exception {
    // given
    when(container.getDependsOn()).thenReturn(singletonList("nonExistingContainer"));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Machine name '.*' in field 'volumes_from' of machine '.*' in environment is invalid"
  )
  public void shouldFailIfContainerVolumeFromIsInvalid() throws Exception {
    // given
    when(container.getVolumesFrom()).thenReturn(singletonList(MACHINE_NAME + ":777"));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Machine '.*' in environment contains non existing machine '.*' in 'volumes_from' field"
  )
  public void shouldFailIfContainerVolumeFromContainsMissingContainer() throws Exception {
    // given
    when(container.getVolumesFrom()).thenReturn(singletonList("nonExistingContainer"));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Container '.*' can not mount volume from itself"
  )
  public void shouldFailIfContainerHasVolumeFromItself() throws Exception {
    // given
    when(container.getVolumesFrom()).thenReturn(singletonList(MACHINE_NAME));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Ports binding is forbidden but found in machine '.*' of environment"
  )
  public void shouldFailIfThereIsPortBindingInContainer() throws Exception {
    // given
    when(container.getPorts()).thenReturn(singletonList("8080:8080"));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Volumes binding is forbidden but found in machine '.*' of environment"
  )
  public void shouldFailIfThereIsVolumeBindingInContainer() throws Exception {
    // given
    when(container.getVolumes()).thenReturn(singletonList("/etc:/etc"));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test(
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp =
        "Networks configuration is forbidden but found in machine '.*' of environment"
  )
  public void shouldFailIfThereIsNetworkInContainer() throws Exception {
    // given
    when(container.getNetworks()).thenReturn(singletonList("newNetwork"));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  private static InternalMachineConfig machineMock() {
    InternalMachineConfig mock = mock(InternalMachineConfig.class);
    when(mock.getServers()).thenReturn(emptyMap());
    when(mock.getInstallers()).thenReturn(emptyList());
    return mock;
  }

  private static InternalMachineConfig machineMockWithServers(String... servers) {
    InternalMachineConfig mock = machineMock();
    when(mock.getServers())
        .thenReturn(
            Arrays.stream(servers)
                .collect(Collectors.toMap(Function.identity(), s -> new ServerConfigImpl())));
    return mock;
  }

  private static InternalMachineConfig machineMockWithInstallers(String... servers) {
    InternalMachineConfig mock = machineMock();
    when(mock.getInstallers())
        .thenReturn(
            Arrays.stream(servers)
                .map(s -> new InstallerImpl().withId(s))
                .collect(Collectors.toList()));
    return mock;
  }

  private static DockerContainerConfig containerMock() {
    return mock(DockerContainerConfig.class);
  }
}
