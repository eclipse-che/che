/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.openshift.provision.volume;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.workspace.shared.Utils.WSAGENT_INSTALLER;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;

/**
 * Tests {@link PersistentVolumeClaimProvisioner}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class PersistentVolumeClaimProvisionerTest {

    @Mock
    private EnvironmentImpl      environment;
    @Mock
    private OpenShiftEnvironment osEnv;
    @Mock
    private RuntimeIdentity      runtimeIdentity;

    private PersistentVolumeClaimProvisioner pvcProvisioner;

    @Test
    public void doNothingWhenPVCDisabled() throws Exception {
        pvcProvisioner = new PersistentVolumeClaimProvisioner(false,
                                                              "claim-che-workspace",
                                                              "10Gi",
                                                              "ReadWriteOnce",
                                                              "/projects");

        pvcProvisioner.provision(environment, osEnv, runtimeIdentity);

        verify(osEnv, never()).getPersistentVolumeClaims();
        verify(environment, never()).getMachines();
    }

    @Test
    public void provisionPVC() throws Exception {
        pvcProvisioner = new PersistentVolumeClaimProvisioner(true,
                                                              "claim-che-workspace",
                                                              "10Gi",
                                                              "ReadWriteOnce",
                                                              "/projects");
        final Map<String, PersistentVolumeClaim> pvcs = new HashMap<>();
        when(osEnv.getPersistentVolumeClaims()).thenReturn(pvcs);
        final MachineConfigImpl devMachine = mock(MachineConfigImpl.class);
        when(environment.getMachines()).thenReturn(ImmutableMap.of("test/machine", devMachine));
        when(devMachine.getInstallers()).thenReturn(singletonList(WSAGENT_INSTALLER));
        final String podName = "test";
        final Pod pod = mock(Pod.class);
        final PodSpec podSpec = mock(PodSpec.class);
        final ObjectMeta podMeta = mock(ObjectMeta.class);
        final Container container = mock(Container.class);
        when(pod.getSpec()).thenReturn(podSpec);
        when(pod.getMetadata()).thenReturn(podMeta);
        when(podMeta.getName()).thenReturn(podName);
        when(podSpec.getContainers()).thenReturn(singletonList(container));
        final List<Volume> volumes = new ArrayList<>();
        when(podSpec.getVolumes()).thenReturn(volumes);
        when(container.getName()).thenReturn("machine");
        final List<VolumeMount> volumeMounts = new ArrayList<>();
        when(container.getVolumeMounts()).thenReturn(volumeMounts);
        when(osEnv.getPods()).thenReturn(ImmutableMap.of(podName, pod));

        pvcProvisioner.provision(environment, osEnv, runtimeIdentity);

        verify(osEnv, times(1)).getPersistentVolumeClaims();
        verify(environment, times(1)).getMachines();
        assertFalse(pvcs.isEmpty());
        assertFalse(volumes.isEmpty());
        assertFalse(volumeMounts.isEmpty());
    }

}
