/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
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

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesPersistentVolumeClaims;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link UniqueWorkspacePVCStrategy}.
 *
 * @author Anton Korneta
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class UniqueWorkspacePVCStrategyTest {

  private static final String WORKSPACE_ID = "workspace123";
  private static final String NAMESPACE = "infraNamespace";
  private static final String PVC_NAME_PREFIX = "che-claim";

  private static final RuntimeIdentity IDENTITY =
      new RuntimeIdentityImpl(WORKSPACE_ID, "env1", "id1", NAMESPACE);

  private KubernetesEnvironment k8sEnv;

  @Mock private KubernetesNamespaceFactory factory;
  @Mock private KubernetesNamespace k8sNamespace;
  @Mock private KubernetesPersistentVolumeClaims pvcs;
  @Mock private EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter;
  @Mock private PVCProvisioner pvcProvisioner;
  @Mock private PodsVolumes podsVolumes;
  @Mock private SubPathPrefixes subpathPrefixes;
  @Captor private ArgumentCaptor<KubernetesEnvironment> k8sEnvCaptor;

  private InOrder provisionOrder;

  private UniqueWorkspacePVCStrategy strategy;

  @BeforeMethod
  public void setup() throws Exception {
    strategy =
        new UniqueWorkspacePVCStrategy(
            true, factory, ephemeralWorkspaceAdapter, pvcProvisioner, subpathPrefixes);

    k8sEnv = KubernetesEnvironment.builder().build();

    provisionOrder = inOrder(pvcProvisioner, subpathPrefixes, podsVolumes);

    lenient().when(factory.getOrCreate(eq(IDENTITY))).thenReturn(k8sNamespace);
    lenient().when(factory.get(any(Workspace.class))).thenReturn(k8sNamespace);
    when(k8sNamespace.persistentVolumeClaims()).thenReturn(pvcs);
  }

  @Test
  public void testProvisionVolumesIntoKubernetesEnvironment() throws Exception {
    // given
    PersistentVolumeClaim pvc1 = newPVC("pvc1");
    PersistentVolumeClaim pvc2 = newPVC("pvc2");
    k8sEnv.getPersistentVolumeClaims().put("pvc1", pvc1);
    k8sEnv.getPersistentVolumeClaims().put("pvc2", pvc2);

    PersistentVolumeClaim existingPVC = newPVC("existingPVC");
    when(pvcs.getByLabel(CHE_WORKSPACE_ID_LABEL, WORKSPACE_ID))
        .thenReturn(singletonList(existingPVC));

    // when
    strategy.provision(k8sEnv, IDENTITY);

    // then
    provisionOrder
        .verify(pvcProvisioner)
        .provision(k8sEnvCaptor.capture(), eq(ImmutableMap.of("pvc1", pvc1, "pvc2", pvc2)));
    provisionOrder.verify(pvcProvisioner).convertCheVolumes(k8sEnv, WORKSPACE_ID);
    provisionOrder.verify(subpathPrefixes).prefixVolumeMountsSubpaths(k8sEnv, WORKSPACE_ID);

    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 1);
    assertNotNull(k8sEnv.getPersistentVolumeClaims().get("existingPVC"));
    ;
  }

  @Test
  public void shouldProvisionWorkspaceIdLabelToPVCs() throws Exception {
    // given
    PersistentVolumeClaim existingPVC = newPVC("existingPVC");
    when(pvcs.getByLabel(CHE_WORKSPACE_ID_LABEL, WORKSPACE_ID))
        .thenReturn(singletonList(existingPVC));

    // when
    strategy.provision(k8sEnv, IDENTITY);

    // then
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 1);
    PersistentVolumeClaim pvc = k8sEnv.getPersistentVolumeClaims().get("existingPVC");
    assertNotNull(pvc);
    assertEquals(pvc.getMetadata().getLabels().get(CHE_WORKSPACE_ID_LABEL), WORKSPACE_ID);
  }

  @Test
  public void testCreatesProvisionedPVCsOnPrepare() throws Exception {
    final String uniqueName = PVC_NAME_PREFIX + "-3121";
    final PersistentVolumeClaim pvc = newPVC(uniqueName);
    k8sEnv.getPersistentVolumeClaims().clear();
    k8sEnv.getPersistentVolumeClaims().putAll(singletonMap(uniqueName, pvc));
    doReturn(pvc).when(pvcs).create(any());

    strategy.prepare(k8sEnv, IDENTITY, 100, emptyMap());

    verify(pvcs).createIfNotExist(any());
    verify(pvcs).waitBound(uniqueName, 100);
  }

  @Test
  public void testCreatesProvisionedPVCsOnPrepareIfWaitIsDisabled() throws Exception {
    strategy =
        new UniqueWorkspacePVCStrategy(
            false, // wait bound PVCs
            factory,
            ephemeralWorkspaceAdapter,
            pvcProvisioner,
            subpathPrefixes);

    final String uniqueName = PVC_NAME_PREFIX + "-3121";
    final PersistentVolumeClaim pvc = newPVC(uniqueName);
    k8sEnv.getPersistentVolumeClaims().clear();
    k8sEnv.getPersistentVolumeClaims().putAll(singletonMap(uniqueName, pvc));
    doReturn(pvc).when(pvcs).create(any());

    strategy.prepare(k8sEnv, IDENTITY, 100, emptyMap());

    verify(pvcs).createIfNotExist(any());
    verify(pvcs, never()).waitBound(anyString(), anyLong());
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsInfrastructureExceptionWhenFailedToCreatePVCs() throws Exception {
    final PersistentVolumeClaim pvc = mock(PersistentVolumeClaim.class);
    when(pvc.getMetadata()).thenReturn(new ObjectMetaBuilder().withName(PVC_NAME_PREFIX).build());
    k8sEnv.getPersistentVolumeClaims().clear();
    k8sEnv.getPersistentVolumeClaims().put(PVC_NAME_PREFIX, pvc);
    doThrow(InfrastructureException.class).when(pvcs).createIfNotExist(any());

    strategy.prepare(k8sEnv, IDENTITY, 100, emptyMap());
  }

  @Test
  public void shouldDeletePVCsIfThereIsNoPersistAttributeInWorkspaceConfigWhenCleanupCalled()
      throws Exception {
    // given
    Workspace workspace = mock(Workspace.class);
    lenient().when(workspace.getId()).thenReturn(WORKSPACE_ID);

    WorkspaceConfig workspaceConfig = mock(WorkspaceConfig.class);
    lenient().when(workspace.getConfig()).thenReturn(workspaceConfig);

    Map<String, String> workspaceConfigAttributes = new HashMap<>();
    lenient().when(workspaceConfig.getAttributes()).thenReturn(workspaceConfigAttributes);

    // when
    strategy.cleanup(workspace);

    // then
    verify(pvcs).delete(ImmutableMap.of(CHE_WORKSPACE_ID_LABEL, WORKSPACE_ID));
  }

  @Test
  public void shouldDeletePVCsIfPersistAttributeIsSetToTrueInWorkspaceConfigWhenCleanupCalled()
      throws Exception {
    // given
    Workspace workspace = mock(Workspace.class);
    lenient().when(workspace.getId()).thenReturn(WORKSPACE_ID);

    WorkspaceConfig workspaceConfig = mock(WorkspaceConfig.class);
    lenient().when(workspace.getConfig()).thenReturn(workspaceConfig);

    Map<String, String> workspaceConfigAttributes = new HashMap<>();
    lenient().when(workspaceConfig.getAttributes()).thenReturn(workspaceConfigAttributes);
    workspaceConfigAttributes.put(PERSIST_VOLUMES_ATTRIBUTE, "true");

    // when
    strategy.cleanup(workspace);

    // then
    verify(pvcs).delete(any());
  }

  @Test
  public void shouldDoNothingIfPersistAttributeIsSetToFalseInWorkspaceConfigWhenCleanupCalled()
      throws Exception {
    // given
    Workspace workspace = mock(Workspace.class);
    lenient().when(workspace.getId()).thenReturn(WORKSPACE_ID);

    WorkspaceConfig workspaceConfig = mock(WorkspaceConfig.class);
    lenient().when(workspace.getConfig()).thenReturn(workspaceConfig);

    Map<String, String> workspaceConfigAttributes = new HashMap<>();
    lenient().when(workspaceConfig.getAttributes()).thenReturn(workspaceConfigAttributes);
    workspaceConfigAttributes.put(PERSIST_VOLUMES_ATTRIBUTE, "false");

    // when
    strategy.cleanup(workspace);

    // then
    verify(pvcs, never()).delete(any());
  }

  static PersistentVolumeClaim newPVC(String name) {
    return newPVC(name, new HashMap<>());
  }

  static PersistentVolumeClaim newPVC(String name, Map<String, String> labels) {
    return new PersistentVolumeClaimBuilder()
        .withNewMetadata()
        .withName(name)
        .withLabels(labels)
        .endMetadata()
        .withNewSpec()
        .endSpec()
        .build();
  }
}
