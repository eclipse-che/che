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
package org.eclipse.che.workspace.infrastructure.docker.local.installer;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.shared.Utils;
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
public class WsAgentInstallerInfrastructureProvisionerTest {
  private static final RuntimeIdentity RUNTIME_IDENTITY =
      new RuntimeIdentityImpl("wsId", "env", "owner");
  private static final String MACHINE_1_NAME = "machine1";
  private static final String MACHINE_2_NAME = "machine2";

  @Mock private WsAgentBinariesInfrastructureProvisioner binariesProvisioner;
  @Mock private DockerExtConfBindingProvider extConfBindingProvider;

  private WsAgentInstallerInfrastructureProvisioner provisioner;
  private EnvironmentImpl envConfig;
  private DockerEnvironment dockerEnv;

  @BeforeMethod
  public void setUp() throws Exception {
    envConfig = createEnvironment(MACHINE_1_NAME, MACHINE_1_NAME);
    dockerEnv = createDockerEnvironment(MACHINE_1_NAME);
    provisioner =
        new WsAgentInstallerInfrastructureProvisioner(binariesProvisioner, extConfBindingProvider);
  }

  @Test
  public void shouldProvisionBinaries() throws Exception {
    // when
    provisioner.provision(envConfig, dockerEnv, RUNTIME_IDENTITY);

    // then
    verify(binariesProvisioner).provision(eq(envConfig), eq(dockerEnv), eq(RUNTIME_IDENTITY));
  }

  @Test
  public void shouldAddExtConfVolumeAndEnvVarWhenVolumeProviderReturnsNotNullValue()
      throws Exception {
    // given
    String volumeValue = "/host/path:/container/path";
    when(extConfBindingProvider.get()).thenReturn(volumeValue);
    provisioner =
        new WsAgentInstallerInfrastructureProvisioner(binariesProvisioner, extConfBindingProvider);

    EnvironmentImpl expectedEnvConfig = new EnvironmentImpl(envConfig);
    DockerEnvironment expectedDockerEnv = new DockerEnvironment(dockerEnv);
    DockerContainerConfig expectedContainerConfig =
        expectedDockerEnv.getContainers().get(MACHINE_1_NAME);
    expectedContainerConfig
        .getEnvironment()
        .put(CheBootstrap.CHE_LOCAL_CONF_DIR, DockerExtConfBindingProvider.EXT_CHE_LOCAL_CONF_DIR);
    expectedContainerConfig.getVolumes().add(volumeValue);

    // when
    provisioner.provision(envConfig, dockerEnv, RUNTIME_IDENTITY);

    // then
    assertEquals(dockerEnv, expectedDockerEnv);
    assertEquals(envConfig, expectedEnvConfig);
  }

  @Test
  public void shouldNotAddNeitherExtConfVolumeNorEnvVarIfVolumeProviderReturnsNull()
      throws Exception {
    // given
    EnvironmentImpl expectedEnvConfig = new EnvironmentImpl(envConfig);
    DockerEnvironment expectedDockerEnv = new DockerEnvironment(dockerEnv);

    // when
    provisioner.provision(envConfig, dockerEnv, RUNTIME_IDENTITY);

    // then
    assertEquals(dockerEnv, expectedDockerEnv);
    assertEquals(envConfig, expectedEnvConfig);
  }

  @Test
  public void shouldAddExtConfVolumeAndEnvVarIfMachineConfHasWsagentInstallerOnly()
      throws Exception {
    // given
    String volumeValue = "/host/path:/container/path";
    when(extConfBindingProvider.get()).thenReturn(volumeValue);
    provisioner =
        new WsAgentInstallerInfrastructureProvisioner(binariesProvisioner, extConfBindingProvider);

    envConfig = createEnvironment(MACHINE_1_NAME, MACHINE_2_NAME, MACHINE_1_NAME);
    dockerEnv = createDockerEnvironment(MACHINE_1_NAME, MACHINE_2_NAME);
    EnvironmentImpl expectedEnvConfig = new EnvironmentImpl(envConfig);
    DockerContainerConfig expectedContainerConfig =
        new DockerContainerConfig(dockerEnv.getContainers().get(MACHINE_2_NAME));

    // when
    provisioner.provision(envConfig, dockerEnv, RUNTIME_IDENTITY);

    // then
    assertEquals(dockerEnv.getContainers().get(MACHINE_2_NAME), expectedContainerConfig);
    assertEquals(envConfig, expectedEnvConfig);
  }

  private EnvironmentImpl createEnvironment(
      String nameOfMachineWithWsagentInstaller, String... machinesNames) {
    EnvironmentImpl environment = new EnvironmentImpl();
    for (String machineName : machinesNames) {
      environment.getMachines().put(machineName, new MachineConfigImpl());
    }
    environment
        .getMachines()
        .get(nameOfMachineWithWsagentInstaller)
        .getInstallers()
        .add(Utils.WSAGENT_INSTALLER);
    return environment;
  }

  private DockerEnvironment createDockerEnvironment(String... machinesNames) {
    DockerEnvironment environment = new DockerEnvironment();
    for (String machineName : machinesNames) {
      environment.getContainers().put(machineName, new DockerContainerConfig());
    }
    return environment;
  }
}
