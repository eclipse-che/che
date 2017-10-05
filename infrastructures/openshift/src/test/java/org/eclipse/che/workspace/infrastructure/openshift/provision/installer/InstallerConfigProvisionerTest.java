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
package org.eclipse.che.workspace.infrastructure.openshift.provision.installer;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.InternalMachineConfig;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link InstallerConfigProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class InstallerConfigProvisionerTest {

  private static final String CHE_SERVER_ENDPOINT = "localhost:8080";

  @Mock private InternalEnvironment environment;
  @Mock private OpenShiftEnvironment osEnv;
  @Mock private RuntimeIdentity runtimeIdentity;

  private InstallerConfigProvisioner installerConfigProvisioner;

  @BeforeMethod
  public void setUp() throws Exception {
    installerConfigProvisioner = new InstallerConfigProvisioner(CHE_SERVER_ENDPOINT);
  }

  @Test
  public void provisionInstallerConfig() throws Exception {
    final String podName = "test";
    final Container container = mockContainer("machine");
    final Pod pod = mockPod(podName, singletonList(container));
    when(osEnv.getPods()).thenReturn(ImmutableMap.of(podName, pod));
    final InternalMachineConfig devMachine = mock(InternalMachineConfig.class);
    final Map<String, InternalMachineConfig> machines = ImmutableMap.of("test/machine", devMachine);
    when(environment.getMachines()).thenReturn(machines);
    when(devMachine.getServers())
        .thenReturn(singletonMap(Constants.SERVER_WS_AGENT_HTTP_REFERENCE, new ServerConfigImpl()));
    final InstallerImpl installer = mock(InstallerImpl.class);
    final List<InstallerImpl> installers = singletonList(installer);
    when(devMachine.getInstallers()).thenReturn(installers);
    final Map<String, String> envVars = ImmutableMap.of("environment", "CHE_HOST=localhost");
    when(installer.getProperties()).thenReturn(envVars);
    final List<EnvVar> envVariables = new ArrayList<>();
    when(container.getEnv()).thenReturn(envVariables);
    when(installer.getServers()).thenReturn(emptyMap());

    installerConfigProvisioner.provision(environment, osEnv, runtimeIdentity);

    verify(osEnv, times(1)).getPods();
    verify(runtimeIdentity, atLeast(1)).getWorkspaceId();
    verify(environment, times(2)).getMachines();
    assertTrue(envVariables.size() == 3);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsInfrastructureExceptionWhenInstallerExceptionOccurs() throws Exception {
    final String podName = "test";
    final Pod pod = mockPod(podName, "machine");
    when(osEnv.getPods()).thenReturn(ImmutableMap.of(podName, pod));
    when(environment.getMachines())
        .thenReturn(ImmutableMap.of("test/machine", mock(InternalMachineConfig.class)));

    installerConfigProvisioner.provision(environment, osEnv, runtimeIdentity);
  }

  private static Pod mockPod(String podName, List<Container> containers) {
    final Pod pod = mock(Pod.class);
    final ObjectMeta podMeta = mock(ObjectMeta.class);
    when(pod.getMetadata()).thenReturn(podMeta);
    when(podMeta.getName()).thenReturn(podName);
    final PodSpec podSpec = mock(PodSpec.class);
    when(pod.getSpec()).thenReturn(podSpec);
    when(podSpec.getContainers()).thenReturn(containers);
    return pod;
  }

  private static Pod mockPod(String podName, String... containerNames) {
    final List<Container> containers = new ArrayList<>();
    for (String containerName : containerNames) {
      containers.add(mockContainer(containerName));
    }
    return mockPod(podName, containers);
  }

  private static Container mockContainer(String name) {
    final Container container = mock(Container.class);
    when(container.getName()).thenReturn(name);
    return container;
  }
}
