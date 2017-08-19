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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
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
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
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

  private EnvironmentValidator environmentValidator = new EnvironmentValidator();

  @Test
  public void shouldSucceedOnValidationOfValidEnvironment() throws Exception {
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
    // wsagent installers
    // wsagent server + installer in different machines
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

  @Test(
    dataProvider = "badMachineNameProvider",
    expectedExceptions = ValidationException.class,
    expectedExceptionsMessageRegExp = "Name of machine '.*' in environment is invalid"
  )
  public void shouldFailIfContainerNameIsInvalid(String name) throws Exception {
    // given
    when(environment.getMachines()).thenReturn(singletonMap(name, machineConfig));
    when(dockerEnvironment.getContainers()).thenReturn(singletonMap(name, container));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @DataProvider(name = "badMachineNameProvider")
  public static Object[][] badMachineNameProvider() {
    return new Object[][] {
      {"name with-space"}, {""}, {"name%symbol"}, {"name|symbol"},
    };
  }

  @Test
  public void shouldFailIfNeitherImageNorBuildAreProvided() throws Exception {
    // given
    when(environment.getMachines()).thenReturn(singletonMap(MACHINE_NAME, machineConfig));
    when(dockerEnvironment.getContainers()).thenReturn(singletonMap(MACHINE_NAME, container));

    // when
    environmentValidator.validate(environment, dockerEnvironment);
  }

  @Test
  public void shouldFailIfBothDockerfileAndBuildContextAreProvided() throws Exception {}

  @Test
  public void shouldPassIfWsAgentServerAndInstallerAreInTheSameMachine() throws Exception {}

  @Test
  public void shouldPassIfMachineFromRecipeIsNotInTheListOfMachines() throws Exception {}

  @Test
  public void shouldFailIfMemoryAttributeIsIllegal() throws Exception {
    // -1
    //0
    // non num
  }

  @Test
  public void shouldFailIfServerPortInMachineIsInvalid() throws Exception {}

  @Test
  public void shouldFailIfServerProtocolInMachineIsInvalid() throws Exception {}

  @Test
  public void shouldFailIfContainerExposeIsInvalid() throws Exception {}

  @Test
  public void shouldFailIfContainerLinkIsInvalid() throws Exception {}

  @Test
  public void shouldFailIfContainerLinkContainsMissingContainer() throws Exception {}

  @Test
  public void shouldFailIfContainerDependencyIsInvalid() throws Exception {}

  @Test
  public void shouldFailIfContainerDependencyContainsMissingContainer() throws Exception {}

  @Test
  public void shouldFailIfContainerVolumeFromIsInvalid() throws Exception {}

  @Test
  public void shouldFailIfContainerVolumeFromContainsMissingContainer() throws Exception {}

  @Test
  public void shouldFailIfThereIsPortBindingInContainer() throws Exception {}

  @Test
  public void shouldFailIfThereIsVolumeBindingInContainer() throws Exception {}

  @Test
  public void shouldFailIfThereIsNetworkInContainer() throws Exception {}

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
