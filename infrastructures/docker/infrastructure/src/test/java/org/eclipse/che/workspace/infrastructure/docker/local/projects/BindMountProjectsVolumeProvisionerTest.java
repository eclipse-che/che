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
package org.eclipse.che.workspace.infrastructure.docker.local.projects;

import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.commons.lang.os.WindowsPathEscaper;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerContainerConfig;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.volume.VolumeNames;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class BindMountProjectsVolumeProvisionerTest {
  private static final String WORKSPACE_ID = "wsId";
  private static final RuntimeIdentity RUNTIME_IDENTITY =
      new RuntimeIdentityImpl(WORKSPACE_ID, "env", "id");
  private static final String MACHINE_1_NAME = "machine1";
  private static final String MACHINE_2_NAME = "machine2";
  private static final String MACHINE_3_NAME = "machine3";
  private static final String PATH_IN_CONTAINER = "/projects1";
  private static final String PATH_ON_HOST = "/test/path";

  @Mock private InternalMachineConfig machine1;
  @Mock private InternalMachineConfig machine2;
  @Mock private InternalMachineConfig machine3;
  @Mock private LocalProjectsFolderPathProvider workspaceFolderPathProvider;
  @Mock private WindowsPathEscaper pathEscaper;

  private BindMountProjectsVolumeProvisioner provisioner;
  private DockerEnvironment dockerEnvironment;

  @BeforeMethod
  public void setUp() throws Exception {
    provisioner =
        new BindMountProjectsVolumeProvisioner(workspaceFolderPathProvider, pathEscaper, "");
    Map<String, InternalMachineConfig> machines =
        ImmutableMap.of(
            MACHINE_1_NAME, machine1, MACHINE_2_NAME, machine2, MACHINE_3_NAME, machine3);
    dockerEnvironment = new DockerEnvironment(null, machines, null);
    dockerEnvironment.getContainers().put(MACHINE_1_NAME, new DockerContainerConfig());
    dockerEnvironment.getContainers().put(MACHINE_2_NAME, new DockerContainerConfig());
    dockerEnvironment.getContainers().put(MACHINE_3_NAME, new DockerContainerConfig());
    // doesn't influence volumes
    when(machine1.getServers())
        .thenReturn(
            Collections.singletonMap(
                Constants.SERVER_WS_AGENT_HTTP_REFERENCE,
                new ServerConfigImpl("8080", "http", "/api", singletonMap("key", "value"))));
  }

  @Test
  public void shouldChangeMatchingVolumesOnly() throws Exception {
    // given
    dockerEnvironment
        .getContainers()
        .get(MACHINE_1_NAME)
        .getVolumes()
        .add(volume(WORKSPACE_ID, "projects2"));
    dockerEnvironment
        .getContainers()
        .get(MACHINE_1_NAME)
        .getVolumes()
        .add(volume(WORKSPACE_ID + "2", "projects"));
    dockerEnvironment.getContainers().get(MACHINE_2_NAME).getVolumes();
    dockerEnvironment
        .getContainers()
        .get(MACHINE_3_NAME)
        .getVolumes()
        .add(volume(WORKSPACE_ID, "/projects", "/projects"));
    dockerEnvironment
        .getContainers()
        .get(MACHINE_3_NAME)
        .getVolumes()
        .add(volume(WORKSPACE_ID, "projects", "/non/common/projects/path"));
    when(workspaceFolderPathProvider.getPath(anyString())).thenReturn(PATH_ON_HOST);
    DockerEnvironment expectedDockerEnv = new DockerEnvironment(dockerEnvironment);
    List<String> expectedMachine3Volumes =
        expectedDockerEnv.getContainers().get(MACHINE_3_NAME).getVolumes();
    expectedMachine3Volumes.clear();
    expectedMachine3Volumes.add(volume(WORKSPACE_ID, "/projects", "/projects"));
    expectedMachine3Volumes.add(PATH_ON_HOST + ":" + "/non/common/projects/path");

    // when
    provisioner.provision(dockerEnvironment, RUNTIME_IDENTITY);

    // then
    assertEquals(dockerEnvironment, expectedDockerEnv);
  }

  @Test(
      expectedExceptions = InternalInfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Error occurred on resolving path to files of workspace " + WORKSPACE_ID)
  public void shouldThrowExceptionWhenWsFolderPathProviderThrowsException() throws Exception {
    // given
    when(workspaceFolderPathProvider.getPath(anyString())).thenThrow(new IOException("test"));
    dockerEnvironment
        .getContainers()
        .get(MACHINE_1_NAME)
        .getVolumes()
        .add(volume(WORKSPACE_ID, "projects", PATH_IN_CONTAINER));

    // when
    provisioner.provision(dockerEnvironment, RUNTIME_IDENTITY);
  }

  @Test
  public void shouldAddVolumeOptionsToProjectsVolume() throws Exception {
    // given
    String options = "rwZ";
    provisioner =
        new BindMountProjectsVolumeProvisioner(workspaceFolderPathProvider, pathEscaper, options);
    when(workspaceFolderPathProvider.getPath(anyString())).thenReturn(PATH_ON_HOST);
    dockerEnvironment
        .getContainers()
        .get(MACHINE_1_NAME)
        .getVolumes()
        .add(volume(WORKSPACE_ID, "projects", PATH_IN_CONTAINER));
    DockerEnvironment expectedDockerEnv = new DockerEnvironment(dockerEnvironment);
    List<String> expectedVolumes =
        expectedDockerEnv.getContainers().get(MACHINE_1_NAME).getVolumes();
    expectedVolumes.clear();
    expectedVolumes.add(PATH_ON_HOST + ":" + PATH_IN_CONTAINER + ":" + options);

    // when
    provisioner.provision(dockerEnvironment, RUNTIME_IDENTITY);

    // then
    assertEquals(dockerEnvironment, expectedDockerEnv);
  }

  private String volume(String workspaceId, String name) {
    return volume(workspaceId, name, "/path");
  }

  private String volume(String workspaceId, String name, String path) {
    return VolumeNames.generate(workspaceId, name) + ":" + path;
  }
}
