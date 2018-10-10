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
package org.eclipse.che.workspace.infrastructure.docker.local.installer;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.inject.CheBootstrap;
import org.eclipse.che.workspace.infrastructure.docker.local.server.DockerExtConfBindingProvider;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class WsAgentServerConfigProvisionerTest {
  private static final RuntimeIdentity RUNTIME_IDENTITY =
      new RuntimeIdentityImpl("wsId", "env", "id");
  private static final String MACHINE_1_NAME = "machine1";
  private static final String MACHINE_2_NAME = "machine2";

  @Mock private DockerExtConfBindingProvider extConfBindingProvider;

  private WsAgentServerConfigProvisioner provisioner;
  private DockerEnvironment dockerEnv;

  @BeforeMethod
  public void setUp() throws Exception {
    dockerEnv = createDockerEnvironment(MACHINE_1_NAME, MACHINE_1_NAME);
    provisioner = new WsAgentServerConfigProvisioner(extConfBindingProvider);
  }

  @Test
  public void shouldAddExtConfVolumeAndEnvVarWhenVolumeProviderReturnsNotNullValue()
      throws Exception {
    // given
    String volumeValue = "/host/path:/container/path";
    when(extConfBindingProvider.get()).thenReturn(volumeValue);
    provisioner = new WsAgentServerConfigProvisioner(extConfBindingProvider);

    DockerEnvironment expectedDockerEnv = new DockerEnvironment(dockerEnv);
    DockerContainerConfig expectedContainerConfig =
        expectedDockerEnv.getContainers().get(MACHINE_1_NAME);
    expectedContainerConfig
        .getEnvironment()
        .put(CheBootstrap.CHE_LOCAL_CONF_DIR, DockerExtConfBindingProvider.EXT_CHE_LOCAL_CONF_DIR);
    expectedContainerConfig.getVolumes().add(volumeValue);

    // when
    provisioner.provision(dockerEnv, RUNTIME_IDENTITY);

    // then
    assertEquals(dockerEnv, expectedDockerEnv);
  }

  @Test
  public void shouldNotAddNeitherExtConfVolumeNorEnvVarIfVolumeProviderReturnsNull()
      throws Exception {
    // given
    DockerEnvironment expectedDockerEnv = new DockerEnvironment(dockerEnv);

    // when
    provisioner.provision(dockerEnv, RUNTIME_IDENTITY);

    // then
    assertEquals(dockerEnv, expectedDockerEnv);
  }

  @Test
  public void shouldNotAddNeitherExtConfVolumeNorEnvVarIfMachineDoesNotHaveServerButHasInstaller()
      throws Exception {
    // given
    dockerEnv = createDockerEnvironment(MACHINE_1_NAME, MACHINE_2_NAME, MACHINE_1_NAME);
    DockerEnvironment expectedDockerEnv = new DockerEnvironment(dockerEnv);
    InternalMachineConfig machine = dockerEnv.getMachines().get(MACHINE_1_NAME);
    when(machine.getServers())
        .thenReturn(singletonMap(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, new ServerConfigImpl()));

    // when
    provisioner.provision(dockerEnv, RUNTIME_IDENTITY);

    // then
    assertEquals(dockerEnv, expectedDockerEnv);
  }

  @Test
  public void shouldAddExtConfVolumeAndEnvVarIfMachineConfHasWsagentServerOnly() throws Exception {
    // given
    String volumeValue = "/host/path:/container/path";
    when(extConfBindingProvider.get()).thenReturn(volumeValue);
    provisioner = new WsAgentServerConfigProvisioner(extConfBindingProvider);

    dockerEnv = createDockerEnvironment(MACHINE_1_NAME, MACHINE_2_NAME, MACHINE_1_NAME);
    DockerContainerConfig expectedContainerConfig =
        new DockerContainerConfig(dockerEnv.getContainers().get(MACHINE_2_NAME));

    // when
    provisioner.provision(dockerEnv, RUNTIME_IDENTITY);

    // then
    assertEquals(dockerEnv.getContainers().get(MACHINE_2_NAME), expectedContainerConfig);
  }

  private DockerEnvironment createDockerEnvironment(
      String nameOfMachineWithWsagentServer, String... machinesNames) {
    Map<String, InternalMachineConfig> machines = new HashMap<>();
    for (String machineName : machinesNames) {
      InternalMachineConfig machine = mock(InternalMachineConfig.class);
      machines.put(machineName, machine);
      if (machineName.equals(nameOfMachineWithWsagentServer)) {
        lenient()
            .when(machine.getServers())
            .thenReturn(
                singletonMap(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, new ServerConfigImpl()));
      }
    }

    DockerEnvironment environment = new DockerEnvironment(null, machines, null);
    for (String machineName : machinesNames) {
      environment.getContainers().put(machineName, new DockerContainerConfig());
    }

    return environment;
  }
}
