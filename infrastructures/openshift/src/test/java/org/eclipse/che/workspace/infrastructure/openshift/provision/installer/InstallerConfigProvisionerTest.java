/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.openshift.provision.installer;


import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.workspace.shared.Utils.WSAGENT_INSTALLER;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link InstallerConfigProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class InstallerConfigProvisionerTest {

    private static final String CHE_SERVER_ENDPOINT = "localhost:8080";

    @Mock
    private InstallerRegistry    registry;
    @Mock
    private EnvironmentImpl      environment;
    @Mock
    private OpenShiftEnvironment osEnv;
    @Mock
    private RuntimeIdentity      runtimeIdentity;

    private InstallerConfigProvisioner installerConfigProvisioner;

    @BeforeMethod
    public void setUp() throws Exception {
        installerConfigProvisioner = new InstallerConfigProvisioner(registry, CHE_SERVER_ENDPOINT);
    }

    @Test
    public void provisionInstallerConfig() throws Exception {
        final String podName = "test";
        final Container container = mockContainer("machine");
        final Pod pod = mockPod(podName, singletonList(container));
        when(osEnv.getPods()).thenReturn(ImmutableMap.of(podName, pod));
        final MachineConfigImpl devMachine = mock(MachineConfigImpl.class);
        final Map<String, MachineConfigImpl> machines = ImmutableMap.of("test/machine", devMachine);
        when(environment.getMachines()).thenReturn(machines);
        final List<String> installerIds = singletonList(WSAGENT_INSTALLER);
        when(devMachine.getInstallers()).thenReturn(installerIds);
        final Installer installer = mock(Installer.class);
        when(registry.getOrderedInstallers(installerIds)).thenReturn(singletonList(installer));
        final Map<String, String> envVars = ImmutableMap.of("environment", "CHE_HOST=localhost");
        when(installer.getProperties()).thenReturn(envVars);
        final List<EnvVar> envVariables = new ArrayList<>();
        when(container.getEnv()).thenReturn(envVariables);
        when(installer.getServers()).thenReturn(emptyMap());

        installerConfigProvisioner.provision(environment, osEnv, runtimeIdentity);

        verify(osEnv, times(1)).getPods();
        verify(runtimeIdentity, atLeast(1)).getWorkspaceId();
        verify(environment, times(1)).getMachines();
        assertTrue(envVariables.size() == 3);
    }

    @Test(expectedExceptions = InfrastructureException.class)
    public void throwsInfrastructureExceptionWhenInstallerExceptionOccurs() throws Exception {
        final String podName = "test";
        final Pod pod = mockPod(podName, "machine");
        when(osEnv.getPods()).thenReturn(ImmutableMap.of(podName, pod));
        when(environment.getMachines()).thenReturn(ImmutableMap.of("test/machine", mock(MachineConfigImpl.class)));
        when(registry.getOrderedInstallers(any())).thenThrow(new InstallerException("not found"));

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
