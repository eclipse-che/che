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

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy.SUBPATHS_PROPERTY_FMT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesPersistentVolumeClaims;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link PerWorkspacePVCStrategy}
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class PerWorkspacePVCStrategyTest {

  private static final String WORKSPACE_ID = "workspace123";
  private static final String INFRA_NAMESPACE = "infraNamespace";
  private static final String PVC_NAME_PREFIX = "che-claim";

  private static final String PVC_QUANTITY = "10Gi";
  private static final String PVC_ACCESS_MODE = "RWO";
  private static final String PVC_STORAGE_CLASS_NAME = "special";

  private static final RuntimeIdentity IDENTITY =
      new RuntimeIdentityImpl(WORKSPACE_ID, "userid", null, INFRA_NAMESPACE);

  @Mock private PVCSubPathHelper pvcSubPathHelper;
  @Mock private KubernetesNamespaceFactory factory;
  @Mock private KubernetesNamespace k8sNamespace;
  @Mock private KubernetesPersistentVolumeClaims pvcs;
  @Mock private EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter;

  @Mock private PVCProvisioner volumeConverter;
  @Mock private PodsVolumes podsVolumes;
  @Mock private SubPathPrefixes subpathPrefixes;

  private PerWorkspacePVCStrategy strategy;

  @BeforeMethod
  public void setup() throws Exception {
    strategy =
        new PerWorkspacePVCStrategy(
            PVC_NAME_PREFIX,
            PVC_QUANTITY,
            PVC_ACCESS_MODE,
            true,
            PVC_STORAGE_CLASS_NAME,
            true,
            pvcSubPathHelper,
            factory,
            ephemeralWorkspaceAdapter,
            volumeConverter,
            podsVolumes,
            subpathPrefixes);

    lenient().when(factory.getOrCreate(IDENTITY)).thenReturn(k8sNamespace);
    lenient().when(factory.get(any(Workspace.class))).thenReturn(k8sNamespace);
    lenient().when(k8sNamespace.persistentVolumeClaims()).thenReturn(pvcs);
  }

  @Test
  public void shouldPreparePerWorkspacePVCWithSubPaths() throws Exception {
    // given
    final PersistentVolumeClaim pvc = newPVC(PVC_NAME_PREFIX + "-" + WORKSPACE_ID);
    String perWorkspacePVCName = pvc.getMetadata().getName();

    KubernetesEnvironment k8sEnv = KubernetesEnvironment.builder().build();
    k8sEnv.getPersistentVolumeClaims().put(perWorkspacePVCName, pvc);

    String[] subPaths = {"/projects", "/plugins"};
    pvc.getAdditionalProperties().put(format(SUBPATHS_PROPERTY_FMT, WORKSPACE_ID), subPaths);

    // when
    strategy.prepare(k8sEnv, IDENTITY, 100, emptyMap());

    // then
    verify(pvcs).get();
    verify(pvcs).create(pvc);
    verify(pvcs).waitBound(perWorkspacePVCName, 100);
    verify(pvcSubPathHelper)
        .createDirs(IDENTITY, WORKSPACE_ID, perWorkspacePVCName, emptyMap(), subPaths);
  }

  @Test
  public void shouldPreparePerWorkspacePVCWithSubPathsWhenWaitBoundIsDisabled() throws Exception {
    // given
    strategy =
        new PerWorkspacePVCStrategy(
            PVC_NAME_PREFIX,
            PVC_QUANTITY,
            PVC_ACCESS_MODE,
            true,
            PVC_STORAGE_CLASS_NAME,
            false,
            pvcSubPathHelper,
            factory,
            ephemeralWorkspaceAdapter,
            volumeConverter,
            podsVolumes,
            subpathPrefixes);
    final PersistentVolumeClaim pvc = newPVC(PVC_NAME_PREFIX + "-" + WORKSPACE_ID);
    String perWorkspacePVCName = pvc.getMetadata().getName();

    KubernetesEnvironment k8sEnv = KubernetesEnvironment.builder().build();
    k8sEnv.getPersistentVolumeClaims().put(perWorkspacePVCName, pvc);

    String[] subPaths = {"/projects", "/plugins"};
    pvc.getAdditionalProperties().put(format(SUBPATHS_PROPERTY_FMT, WORKSPACE_ID), subPaths);

    // when
    strategy.prepare(k8sEnv, IDENTITY, 100, emptyMap());

    // then
    verify(pvcs).get();
    verify(pvcs).create(pvc);
    verify(pvcs, never()).waitBound(anyString(), anyLong());
    verify(pvcSubPathHelper)
        .createDirs(IDENTITY, WORKSPACE_ID, perWorkspacePVCName, emptyMap(), subPaths);
  }

  @Test
  public void shouldReturnPVCPerWorkspace() throws Exception {
    // when
    PersistentVolumeClaim commonPVC = strategy.createCommonPVC(WORKSPACE_ID);

    // then
    assertEquals(commonPVC.getMetadata().getName(), PVC_NAME_PREFIX + "-" + WORKSPACE_ID);
    assertEquals(commonPVC.getMetadata().getLabels().get(CHE_WORKSPACE_ID_LABEL), WORKSPACE_ID);
    assertEquals(commonPVC.getSpec().getStorageClassName(), PVC_STORAGE_CLASS_NAME);
  }

  @Test
  public void shouldReturnNullStorageClassNameWhenStorageClassNameIsEmptyOrNull() throws Exception {
    String[] storageClassNames = {"", null};

    for (String storageClassName : storageClassNames) {
      final PerWorkspacePVCStrategy strategy =
          new PerWorkspacePVCStrategy(
              PVC_NAME_PREFIX,
              PVC_QUANTITY,
              PVC_ACCESS_MODE,
              true,
              storageClassName,
              true,
              pvcSubPathHelper,
              factory,
              ephemeralWorkspaceAdapter,
              volumeConverter,
              podsVolumes,
              subpathPrefixes);

      final PersistentVolumeClaim commonPVC = strategy.createCommonPVC(WORKSPACE_ID);

      assertNull(commonPVC.getSpec().getStorageClassName());
    }
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
    verify(pvcs).delete(ImmutableMap.of(CHE_WORKSPACE_ID_LABEL, WORKSPACE_ID));
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
    verify(pvcs, never()).delete(ImmutableMap.of(CHE_WORKSPACE_ID_LABEL, WORKSPACE_ID));
  }

  private static PersistentVolumeClaim newPVC(String name) {
    return new PersistentVolumeClaimBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .endSpec()
        .build();
  }
}
