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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
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
  private static final String PVC_NAME_PREFIX = "che-claim";

  private static final String PVC_QUANTITY = "10Gi";
  private static final String PVC_ACCESS_MODE = "RWO";

  private static final RuntimeIdentity IDENTITY =
      new RuntimeIdentityImpl(WORKSPACE_ID, "env1", "id1");

  @Mock private PVCSubPathHelper pvcSubPathHelper;
  @Mock private KubernetesNamespaceFactory factory;
  @Mock private KubernetesNamespace k8sNamespace;
  @Mock private KubernetesPersistentVolumeClaims pvcs;
  @Mock private EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter;

  private PerWorkspacePVCStrategy strategy;

  @BeforeMethod
  public void setup() throws Exception {
    strategy =
        new PerWorkspacePVCStrategy(
            PVC_NAME_PREFIX,
            PVC_QUANTITY,
            PVC_ACCESS_MODE,
            true,
            pvcSubPathHelper,
            factory,
            ephemeralWorkspaceAdapter);

    lenient().when(factory.create(WORKSPACE_ID)).thenReturn(k8sNamespace);
    lenient().when(k8sNamespace.persistentVolumeClaims()).thenReturn(pvcs);
  }

  @Test
  public void shouldReturnCommonPVCNameSuffixedWithWorkspaceId() throws Exception {
    // when
    String commonPVCName = strategy.getCommonPVCName(IDENTITY);

    // then
    assertEquals(commonPVCName, PVC_NAME_PREFIX + "-" + WORKSPACE_ID);
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
}
