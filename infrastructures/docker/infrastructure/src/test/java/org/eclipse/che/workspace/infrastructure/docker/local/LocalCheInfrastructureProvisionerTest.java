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
package org.eclipse.che.workspace.infrastructure.docker.local;

import static org.mockito.ArgumentMatchers.eq;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.local.dod.DockerApiHostEnvVariableProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.installer.LocalInstallersBinariesVolumeProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.installer.WsAgentServerConfigProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.local.projects.ProjectsVolumeProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.model.DockerEnvironment;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.ContainerSystemSettingsProvisionersApplier;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.env.EnvVarsConverter;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.labels.RuntimeLabelsProvisioner;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.memory.MemoryAttributeConverter;
import org.eclipse.che.workspace.infrastructure.docker.provisioner.server.ServersConverter;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(MockitoTestNGListener.class)
public class LocalCheInfrastructureProvisionerTest {

  @Mock private ContainerSystemSettingsProvisionersApplier settingsProvisioners;
  @Mock private ProjectsVolumeProvisioner projectsVolumeProvisioner;
  @Mock private LocalInstallersBinariesVolumeProvisioner installerConfigProvisioner;
  @Mock private RuntimeLabelsProvisioner labelsProvisioner;
  @Mock private DockerApiHostEnvVariableProvisioner dockerApiEnvProvisioner;
  @Mock private InternalEnvironment environment;
  @Mock private DockerEnvironment dockerEnvironment;
  @Mock private RuntimeIdentity runtimeIdentity;
  @Mock private WsAgentServerConfigProvisioner wsAgentServerConfigProvisioner;
  @Mock private ServersConverter serversConverter;
  @Mock private EnvVarsConverter envVarsConverter;
  @Mock private MemoryAttributeConverter memoryAttributeConverter;

  private LocalCheInfrastructureProvisioner provisioner;

  private Object[] allInnerProvisioners;

  @BeforeMethod
  public void setUp() throws Exception {
    provisioner =
        new LocalCheInfrastructureProvisioner(
            settingsProvisioners,
            projectsVolumeProvisioner,
            installerConfigProvisioner,
            labelsProvisioner,
            dockerApiEnvProvisioner,
            wsAgentServerConfigProvisioner,
            serversConverter,
            envVarsConverter,
            memoryAttributeConverter);

    allInnerProvisioners =
        new Object[] {
          settingsProvisioners,
          projectsVolumeProvisioner,
          installerConfigProvisioner,
          labelsProvisioner,
          dockerApiEnvProvisioner,
          wsAgentServerConfigProvisioner,
          serversConverter,
          envVarsConverter,
          memoryAttributeConverter
        };
  }

  @Test
  public void shouldCallProvisionersInSpecificOrder() throws Exception {
    // when
    provisioner.provision(environment, dockerEnvironment, runtimeIdentity);

    // then
    InOrder inOrder = Mockito.inOrder((Object[]) allInnerProvisioners);
    inOrder
        .verify(serversConverter)
        .provision(eq(environment), eq(dockerEnvironment), eq(runtimeIdentity));
    inOrder
        .verify(envVarsConverter)
        .provision(eq(environment), eq(dockerEnvironment), eq(runtimeIdentity));
    inOrder
        .verify(memoryAttributeConverter)
        .provision(eq(environment), eq(dockerEnvironment), eq(runtimeIdentity));
    inOrder
        .verify(labelsProvisioner)
        .provision(eq(environment), eq(dockerEnvironment), eq(runtimeIdentity));
    inOrder
        .verify(installerConfigProvisioner)
        .provision(eq(environment), eq(dockerEnvironment), eq(runtimeIdentity));
    inOrder
        .verify(projectsVolumeProvisioner)
        .provision(eq(environment), eq(dockerEnvironment), eq(runtimeIdentity));
    inOrder
        .verify(wsAgentServerConfigProvisioner)
        .provision(eq(environment), eq(dockerEnvironment), eq(runtimeIdentity));
    inOrder
        .verify(settingsProvisioners)
        .provision(eq(environment), eq(dockerEnvironment), eq(runtimeIdentity));
    inOrder
        .verify(dockerApiEnvProvisioner)
        .provision(eq(environment), eq(dockerEnvironment), eq(runtimeIdentity));
    inOrder.verifyNoMoreInteractions();
  }
}
