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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.shared.Utils;
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

  @Mock private LocalWorkspaceFolderPathProvider workspaceFolderPathProvider;
  @Mock private WindowsPathEscaper pathEscaper;

  private ProjectsVolumeProvisioner provisioner;
  private EnvironmentImpl environment;
  private DockerEnvironment dockerEnvironment;

  @BeforeMethod
  public void setUp() throws Exception {
    provisioner =
        new ProjectsVolumeProvisioner(
            workspaceFolderPathProvider, pathEscaper, PATH_IN_CONTAINER, "");
    environment = createEnvironment(MACHINE_1_NAME, MACHINE_1_NAME);
    dockerEnvironment = createDockerEnvironment(MACHINE_1_NAME);
  }

  @Test(
    expectedExceptions = InternalInfrastructureException.class,
    expectedExceptionsMessageRegExp = "ws-machine is not found on installers applying"
  )
  public void shouldThrowExceptionIfDevMachineIsNotFound() throws Exception {
    // given
    environment
        .getMachines()
        .get(MACHINE_1_NAME)
        .setInstallers(Collections.singletonList("org.eclipse.che.ssh"));

    // when
    provisioner.provision(environment, dockerEnvironment, RUNTIME_IDENTITY);
  }

  @Test
  public void shouldAddProjectsVolumeToDevMachine() throws Exception {
    // given
    when(workspaceFolderPathProvider.getPath(anyString())).thenReturn(PATH_ON_HOST);
    EnvironmentImpl expectedEnvironment = new EnvironmentImpl(environment);
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
    assertEquals(environment, expectedEnvironment);
  }

  @Test
  public void shouldNotAddProjectsVolumeToNonDevMachine() throws Exception {
    // given
    when(workspaceFolderPathProvider.getPath(anyString())).thenReturn(PATH_ON_HOST);
    environment = createEnvironment(MACHINE_1_NAME, MACHINE_1_NAME, MACHINE_2_NAME);
    dockerEnvironment = createDockerEnvironment(MACHINE_1_NAME, MACHINE_2_NAME);
    EnvironmentImpl expectedEnvironment = new EnvironmentImpl(environment);
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
    assertEquals(environment, expectedEnvironment);
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
    EnvironmentImpl expectedEnvironment = new EnvironmentImpl(environment);
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
    assertEquals(environment, expectedEnvironment);
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
