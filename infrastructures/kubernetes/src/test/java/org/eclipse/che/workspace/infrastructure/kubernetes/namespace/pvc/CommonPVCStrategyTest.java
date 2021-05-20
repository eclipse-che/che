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

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc.CommonPVCStrategy.SUBPATHS_PROPERTY_FMT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.Quantity;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesPersistentVolumeClaims;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link CommonPVCStrategy}.
 *
 * @author Anton Korneta
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class CommonPVCStrategyTest {

  private static final String WORKSPACE_ID = "workspace123";
  private static final String NAMESPACE = "infraNamespace";
  private static final String PVC_NAME = "che-claim";

  private static final String PVC_QUANTITY = "10Gi";
  private static final String PVC_ACCESS_MODE = "RWO";
  private static final String PVC_STORAGE_CLASS_NAME = "special";

  private static final String[] WORKSPACE_SUBPATHS = {"/projects", "/logs"};

  private static final RuntimeIdentity IDENTITY =
      new RuntimeIdentityImpl(WORKSPACE_ID, "env1", "id1", NAMESPACE);

  private KubernetesEnvironment k8sEnv;

  @Mock private PVCSubPathHelper pvcSubPathHelper;

  @Mock private KubernetesNamespaceFactory factory;
  @Mock private KubernetesNamespace k8sNamespace;
  @Mock private KubernetesPersistentVolumeClaims pvcs;

  @Mock private EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter;
  @Mock private PVCProvisioner volumeConverter;
  @Mock private PodsVolumes podsVolumes;
  @Mock private SubPathPrefixes subpathPrefixes;

  private InOrder provisionOrder;

  private CommonPVCStrategy commonPVCStrategy;

  @BeforeMethod
  public void setup() throws Exception {
    commonPVCStrategy =
        new CommonPVCStrategy(
            PVC_NAME,
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

    k8sEnv = KubernetesEnvironment.builder().build();

    provisionOrder = inOrder(volumeConverter, subpathPrefixes, podsVolumes);

    lenient().doNothing().when(pvcSubPathHelper).execute(any(), any(), any(), any());
    lenient()
        .doReturn(CompletableFuture.completedFuture(null))
        .when(pvcSubPathHelper)
        .removeDirsAsync(anyString(), anyString(), any(String.class));

    lenient().when(factory.getOrCreate(IDENTITY)).thenReturn(k8sNamespace);
    lenient().when(k8sNamespace.persistentVolumeClaims()).thenReturn(pvcs);

    lenient().when(subpathPrefixes.getWorkspaceSubPath(WORKSPACE_ID)).thenReturn(WORKSPACE_ID);
  }

  @Test
  public void testProvisionVolumesIntoKubernetesEnvironment() throws Exception {
    // given
    k8sEnv.getPersistentVolumeClaims().put("pvc1", newPVC("pvc1"));
    k8sEnv.getPersistentVolumeClaims().put("pvc2", newPVC("pvc2"));

    // when
    commonPVCStrategy.provision(k8sEnv, IDENTITY);

    // then
    provisionOrder.verify(volumeConverter).convertCheVolumes(k8sEnv, WORKSPACE_ID);
    provisionOrder.verify(subpathPrefixes).prefixVolumeMountsSubpaths(k8sEnv, WORKSPACE_ID);
    provisionOrder.verify(podsVolumes).replacePVCVolumesWithCommon(k8sEnv.getPodsData(), PVC_NAME);
    assertEquals(k8sEnv.getPersistentVolumeClaims().size(), 1);
    PersistentVolumeClaim commonPVC = k8sEnv.getPersistentVolumeClaims().get(PVC_NAME);
    assertNotNull(commonPVC);
    assertEquals(commonPVC.getMetadata().getName(), PVC_NAME);
    assertEquals(commonPVC.getSpec().getAccessModes(), Collections.singletonList(PVC_ACCESS_MODE));
    assertEquals(
        commonPVC.getSpec().getResources().getRequests().get("storage"),
        new Quantity(PVC_QUANTITY));
  }

  @Test
  public void testReplacePVCWhenItsAlreadyInKubernetesEnvironment() throws Exception {
    PersistentVolumeClaim provisioned = newPVC(PVC_NAME);
    k8sEnv.getPersistentVolumeClaims().put(PVC_NAME, provisioned);

    commonPVCStrategy.provision(k8sEnv, IDENTITY);

    assertNotEquals(k8sEnv.getPersistentVolumeClaims().get(PVC_NAME), provisioned);
  }

  @Test
  public void testDoNotAddsSubpathsPropertyWhenPreCreationIsNotNeeded() throws Exception {
    commonPVCStrategy =
        new CommonPVCStrategy(
            PVC_NAME,
            PVC_QUANTITY,
            PVC_ACCESS_MODE,
            false,
            PVC_STORAGE_CLASS_NAME,
            true,
            pvcSubPathHelper,
            factory,
            ephemeralWorkspaceAdapter,
            volumeConverter,
            podsVolumes,
            subpathPrefixes);

    commonPVCStrategy.provision(k8sEnv, IDENTITY);

    final Map<String, PersistentVolumeClaim> actual = k8sEnv.getPersistentVolumeClaims();
    assertFalse(actual.isEmpty());
    assertTrue(actual.containsKey(PVC_NAME));
    assertFalse(
        actual
            .get(PVC_NAME)
            .getAdditionalProperties()
            .containsKey(format(SUBPATHS_PROPERTY_FMT, WORKSPACE_ID)));
  }

  @Test
  public void testCreatesPVCsWithSubpathsOnPrepare() throws Exception {
    final PersistentVolumeClaim pvc = newPVC(PVC_NAME);
    pvc.getAdditionalProperties()
        .put(format(SUBPATHS_PROPERTY_FMT, WORKSPACE_ID), WORKSPACE_SUBPATHS);
    k8sEnv.getPersistentVolumeClaims().put(PVC_NAME, pvc);
    doNothing()
        .when(pvcSubPathHelper)
        .createDirs(IDENTITY, WORKSPACE_ID, PVC_NAME, emptyMap(), WORKSPACE_SUBPATHS);

    commonPVCStrategy.prepare(k8sEnv, IDENTITY, 100, emptyMap());

    verify(pvcs).get();
    verify(pvcs).create(pvc);
    verify(pvcs).waitBound(PVC_NAME, 100);
    verify(pvcSubPathHelper)
        .createDirs(IDENTITY, WORKSPACE_ID, PVC_NAME, emptyMap(), WORKSPACE_SUBPATHS);
  }

  @Test
  public void testCreatesPVCsWithSubpathsOnPrepareIfWaitIsDisabled() throws Exception {
    commonPVCStrategy =
        new CommonPVCStrategy(
            PVC_NAME,
            PVC_QUANTITY,
            PVC_ACCESS_MODE,
            true,
            PVC_STORAGE_CLASS_NAME,
            false, // wait bound PVCs
            pvcSubPathHelper,
            factory,
            ephemeralWorkspaceAdapter,
            volumeConverter,
            podsVolumes,
            subpathPrefixes);
    final PersistentVolumeClaim pvc = newPVC(PVC_NAME);
    pvc.getAdditionalProperties()
        .put(format(SUBPATHS_PROPERTY_FMT, WORKSPACE_ID), WORKSPACE_SUBPATHS);
    k8sEnv.getPersistentVolumeClaims().put(PVC_NAME, pvc);
    doNothing()
        .when(pvcSubPathHelper)
        .createDirs(IDENTITY, WORKSPACE_ID, PVC_NAME, emptyMap(), WORKSPACE_SUBPATHS);

    commonPVCStrategy.prepare(k8sEnv, IDENTITY, 100, emptyMap());

    verify(pvcs).get();
    verify(pvcs).create(pvc);
    verify(pvcs, never()).waitBound(anyString(), anyLong());
    verify(pvcSubPathHelper)
        .createDirs(IDENTITY, WORKSPACE_ID, PVC_NAME, emptyMap(), WORKSPACE_SUBPATHS);
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "The only one PVC MUST be present in common strategy while it contains: pvc1, pvc2\\.")
  public void shouldThrowExceptionIfK8sEnvHasMoreThanOnePVCOnPreparing() throws Exception {
    final PersistentVolumeClaim pvc1 = newPVC("pvc1");
    final PersistentVolumeClaim pvc2 = newPVC("pvc2");
    k8sEnv.getPersistentVolumeClaims().put("pvc1", pvc1);
    k8sEnv.getPersistentVolumeClaims().put("pvc2", pvc2);

    commonPVCStrategy.prepare(k8sEnv, IDENTITY, 100, emptyMap());
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsInfrastructureExceptionWhenFailedToGetExistingPVCs() throws Exception {
    k8sEnv.getPersistentVolumeClaims().put(PVC_NAME, mock(PersistentVolumeClaim.class));
    doThrow(InfrastructureException.class).when(pvcs).get();

    commonPVCStrategy.prepare(k8sEnv, IDENTITY, 100, emptyMap());
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsInfrastructureExceptionWhenPVCCreationFailed() throws Exception {
    final PersistentVolumeClaim claim = newPVC(PVC_NAME);
    k8sEnv.getPersistentVolumeClaims().put(PVC_NAME, claim);
    when(pvcs.get()).thenReturn(emptyList());
    doThrow(InfrastructureException.class).when(pvcs).create(any());

    commonPVCStrategy.prepare(k8sEnv, IDENTITY, 100, emptyMap());
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

    KubernetesNamespace ns = mock(KubernetesNamespace.class);
    when(factory.get(eq(workspace))).thenReturn(ns);
    when(ns.getName()).thenReturn("ns");

    // when
    commonPVCStrategy.cleanup(workspace);

    // then
    verify(pvcSubPathHelper).removeDirsAsync(WORKSPACE_ID, "ns", PVC_NAME, WORKSPACE_ID);
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

    KubernetesNamespace ns = mock(KubernetesNamespace.class);
    when(factory.get(eq(workspace))).thenReturn(ns);
    when(ns.getName()).thenReturn("ns");

    // when
    commonPVCStrategy.cleanup(workspace);

    // then
    verify(pvcSubPathHelper).removeDirsAsync(WORKSPACE_ID, "ns", PVC_NAME, WORKSPACE_ID);
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
    commonPVCStrategy.cleanup(workspace);

    // then
    verify(pvcSubPathHelper, never()).removeDirsAsync(WORKSPACE_ID, null, WORKSPACE_ID);
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
