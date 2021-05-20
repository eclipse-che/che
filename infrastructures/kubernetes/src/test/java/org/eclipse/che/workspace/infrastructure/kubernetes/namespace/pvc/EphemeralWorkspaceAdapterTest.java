/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.EmptyDirVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import java.util.Collections;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link EphemeralWorkspaceAdapter}.
 *
 * @author Ilya Buziuk
 * @author Angel Misevski
 */
@Listeners(MockitoTestNGListener.class)
public class EphemeralWorkspaceAdapterTest {
  private static final String EPHEMERAL_WORKSPACE_ID = "workspace123";
  private static final String NON_EPHEMERAL_WORKSPACE_ID = "workspace234";
  private static final String POD_NAME = "pod1";

  @Mock private Workspace nonEphemeralWorkspace;
  @Mock private Workspace ephemeralWorkspace;

  @Mock private PVCProvisioner pvcProvisioner;
  @Mock private SubPathPrefixes subPathPrefixes;

  private KubernetesEnvironment k8sEnv;
  @Mock private RuntimeIdentity identity;

  private InOrder provisionOrder;
  @Captor private ArgumentCaptor<KubernetesEnvironment> k8sEnvCaptor;

  private EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter;

  @BeforeMethod
  public void setup() throws Exception {
    ephemeralWorkspaceAdapter = new EphemeralWorkspaceAdapter(pvcProvisioner, subPathPrefixes);

    // ephemeral workspace configuration
    lenient().when(ephemeralWorkspace.getId()).thenReturn(EPHEMERAL_WORKSPACE_ID);
    WorkspaceConfig ephemeralWorkspaceConfig = mock(WorkspaceConfig.class);
    lenient().when(ephemeralWorkspace.getConfig()).thenReturn(ephemeralWorkspaceConfig);
    Map<String, String> ephemeralConfigAttributes =
        Collections.singletonMap(PERSIST_VOLUMES_ATTRIBUTE, "false");
    lenient().when(ephemeralWorkspaceConfig.getAttributes()).thenReturn(ephemeralConfigAttributes);

    // regular / non-ephemeral workspace configuration
    lenient().when(nonEphemeralWorkspace.getId()).thenReturn(NON_EPHEMERAL_WORKSPACE_ID);
    WorkspaceConfig nonEphemeralWorkspaceConfig = mock(WorkspaceConfig.class);
    lenient().when(nonEphemeralWorkspace.getConfig()).thenReturn(nonEphemeralWorkspaceConfig);
    Map<String, String> nonEphemeralConfigAttributes = Collections.emptyMap();
    lenient().when(nonEphemeralWorkspace.getAttributes()).thenReturn(nonEphemeralConfigAttributes);

    k8sEnv = KubernetesEnvironment.builder().build();
    provisionOrder = inOrder(pvcProvisioner, subPathPrefixes);
  }

  @Test
  public void testIsEphemeralWorkspace() throws Exception {
    assertTrue(EphemeralWorkspaceUtility.isEphemeral(ephemeralWorkspace));
    assertFalse(EphemeralWorkspaceUtility.isEphemeral(nonEphemeralWorkspace));
  }

  @Test
  public void testProvisioningAllPVCsInWorkspace() throws Exception {
    // given
    PersistentVolumeClaim pvc1 = UniqueWorkspacePVCStrategyTest.newPVC("pvc1");
    PersistentVolumeClaim pvc2 = UniqueWorkspacePVCStrategyTest.newPVC("pvc2");
    k8sEnv.getPersistentVolumeClaims().put("pvc1", pvc1);
    k8sEnv.getPersistentVolumeClaims().put("pvc2", pvc2);
    when(identity.getWorkspaceId()).thenReturn(EPHEMERAL_WORKSPACE_ID);

    // when
    ephemeralWorkspaceAdapter.provision(k8sEnv, identity);

    // then
    provisionOrder
        .verify(pvcProvisioner)
        .provision(k8sEnv, ImmutableMap.of("pvc1", pvc1, "pvc2", pvc2));
    provisionOrder.verify(pvcProvisioner).convertCheVolumes(k8sEnv, EPHEMERAL_WORKSPACE_ID);
    provisionOrder
        .verify(subPathPrefixes)
        .prefixVolumeMountsSubpaths(k8sEnv, EPHEMERAL_WORKSPACE_ID);
  }

  @Test
  public void testConvertsAllPVCsToEmptyDir() throws Exception {
    // given
    k8sEnv.getPersistentVolumeClaims().put("pvc1", mock(PersistentVolumeClaim.class));
    k8sEnv.getPersistentVolumeClaims().put("pvc2", mock(PersistentVolumeClaim.class));

    io.fabric8.kubernetes.api.model.Volume configMapVolume =
        new VolumeBuilder().withNewConfigMap().withName("configMap").endConfigMap().build();
    io.fabric8.kubernetes.api.model.Volume emptyDirVolume =
        new VolumeBuilder().withNewEmptyDir().endEmptyDir().build();
    io.fabric8.kubernetes.api.model.Volume pvcVolume =
        new VolumeBuilder()
            .withNewPersistentVolumeClaim()
            .withClaimName("pvc1")
            .endPersistentVolumeClaim()
            .build();
    Pod pod =
        new PodBuilder()
            .withNewMetadata()
            .withName(POD_NAME)
            .endMetadata()
            .withNewSpec()
            .withVolumes(
                new VolumeBuilder(pvcVolume).build(),
                new VolumeBuilder(configMapVolume).build(),
                new VolumeBuilder(emptyDirVolume).build())
            .endSpec()
            .build();

    k8sEnv.addPod(pod);

    ephemeralWorkspaceAdapter.provision(k8sEnv, identity);

    assertTrue(k8sEnv.getPersistentVolumeClaims().isEmpty());
    assertNull(pod.getSpec().getVolumes().get(0).getPersistentVolumeClaim());
    assertEquals(pod.getSpec().getVolumes().get(0).getEmptyDir(), new EmptyDirVolumeSource());
    assertEquals(pod.getSpec().getVolumes().get(1), configMapVolume);
    assertEquals(pod.getSpec().getVolumes().get(2), emptyDirVolume);
  }
}
