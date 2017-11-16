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
package org.eclipse.che.workspace.infrastructure.docker.local.projects;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class ProjectsVolumeProvisionerTest {
  private static final String WORKSPACE_ID = "wsId";
  private static final RuntimeIdentity RUNTIME_IDENTITY =
      new RuntimeIdentityImpl(WORKSPACE_ID, "env", "owner");
  private static final String MACHINE_1_NAME = "machine1";
  private static final String MACHINE_2_NAME = "machine2";
  private static final String PATH_IN_CONTAINER = "/projects1";
  private static final String PATH_ON_HOST = "/test/path";

  @Mock private LocalProjectsFolderPathProvider workspaceFolderPathProvider;
  @Mock private WindowsPathEscaper pathEscaper;

  private ProjectsVolumeProvisioner provisioner;
  private InternalEnvironment environment;
  private DockerEnvironment dockerEnvironment;

  @BeforeMethod
  public void setUp() throws Exception {
    provisioner =
        new ProjectsVolumeProvisioner(
            workspaceFolderPathProvider, pathEscaper, PATH_IN_CONTAINER, "");
    environment = createEnvironment(MACHINE_1_NAME, MACHINE_1_NAME);
    dockerEnvironment = createDockerEnvironment(MACHINE_1_NAME);
  }

  @Test
  public void shouldNotAddVolumeIfWsAgentServerIsNotFound() throws Exception {
    // given
    InternalMachineConfig machineConfig = environment.getMachines().get(MACHINE_1_NAME);
    when(machineConfig.getServers())
        .thenReturn(singletonMap("org.eclipse.che.ssh", new ServerConfigImpl()));
    DockerEnvironment expectedDockerEnv = new DockerEnvironment(dockerEnvironment);

    // when
    provisioner.provision(environment, dockerEnvironment, RUNTIME_IDENTITY);

    // then
    assertEquals(dockerEnvironment, expectedDockerEnv);
  }

  @Test
  public void shouldAddProjectsVolumeToDevMachine() throws Exception {
    // given
    when(workspaceFolderPathProvider.getPath(anyString())).thenReturn(PATH_ON_HOST);
    DockerEnvironment expectedDockerEnv = new DockerEnvironment(dockerEnvironment);
    expectedDockerEnv
        .getContainers()
        .get(MACHINE_1_NAME)
        .getVolumes()
        .add(PATH_ON_HOST + ":" + PATH_IN_CONTAINER);

    // when
    provisioner.provision(environment, dockerEnvironment, RUNTIME_IDENTITY);

    // then
    assertEquals(dockerEnvironment, expectedDockerEnv);
  }

  @Test
  public void shouldNotAddProjectsVolumeToNonDevMachine() throws Exception {
    // given
    when(workspaceFolderPathProvider.getPath(anyString())).thenReturn(PATH_ON_HOST);
    environment = createEnvironment(MACHINE_1_NAME, MACHINE_1_NAME, MACHINE_2_NAME);
    dockerEnvironment = createDockerEnvironment(MACHINE_1_NAME, MACHINE_2_NAME);
    DockerEnvironment expectedDockerEnv = new DockerEnvironment(dockerEnvironment);
    expectedDockerEnv
        .getContainers()
        .get(MACHINE_1_NAME)
        .getVolumes()
        .add(PATH_ON_HOST + ":" + PATH_IN_CONTAINER);

    // when
    provisioner.provision(environment, dockerEnvironment, RUNTIME_IDENTITY);

    // then
    assertEquals(dockerEnvironment, expectedDockerEnv);
  }

  @Test(
    expectedExceptions = InternalInfrastructureException.class,
    expectedExceptionsMessageRegExp =
        "Error occurred on resolving path to files of workspace " + WORKSPACE_ID
  )
  public void shouldThrowExceptionWhenWsFolderPathProviderThrowsException() throws Exception {
    // given
    when(workspaceFolderPathProvider.getPath(anyString())).thenThrow(new IOException("test"));

    // when
    provisioner.provision(environment, dockerEnvironment, RUNTIME_IDENTITY);
  }

  @Test
  public void shouldAddVolumeOptionsToProjectsVolume() throws Exception {
    // given
    String options = "rwZ";
    provisioner =
        new ProjectsVolumeProvisioner(
            workspaceFolderPathProvider, pathEscaper, PATH_IN_CONTAINER, options);
    when(workspaceFolderPathProvider.getPath(anyString())).thenReturn(PATH_ON_HOST);
    DockerEnvironment expectedDockerEnv = new DockerEnvironment(dockerEnvironment);
    expectedDockerEnv
        .getContainers()
        .get(MACHINE_1_NAME)
        .getVolumes()
        .add(PATH_ON_HOST + ":" + PATH_IN_CONTAINER + ":" + options);

    // when
    provisioner.provision(environment, dockerEnvironment, RUNTIME_IDENTITY);

    // then
    assertEquals(dockerEnvironment, expectedDockerEnv);
  }

  private InternalEnvironment createEnvironment(
      String nameOfMachineWithWsagentServer, String... machinesNames) {

    Map<String, InternalMachineConfig> machines = new HashMap<>();
    for (String machineName : machinesNames) {
      InternalMachineConfig machine = mock(InternalMachineConfig.class);
      machines.put(machineName, machine);
      if (machineName.equals(nameOfMachineWithWsagentServer)) {
        when(machine.getServers())
            .thenReturn(
                singletonMap(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, new ServerConfigImpl()));
      }
    }
    InternalEnvironment environment = mock(InternalEnvironment.class);
    when(environment.getMachines()).thenReturn(machines);
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
