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
package org.eclipse.che.workspace.infrastructure.openshift.devfile;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.workspace.devfile.server.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.devfile.KubernetesComponentToWorkspaceApplier;
import org.eclipse.che.workspace.infrastructure.kubernetes.devfile.KubernetesEnvironmentProvisioner;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpenshiftComponentToWorkspaceApplierTest {
  public static final String REFERENCE_FILENAME = "reference.yaml";
  public static final String COMPONENT_NAME = "foo";

  private WorkspaceConfigImpl workspaceConfig;

  private KubernetesComponentToWorkspaceApplier applier;
  @Mock private KubernetesRecipeParser k8sRecipeParser;
  @Mock private KubernetesEnvironmentProvisioner k8sEnvProvisioner;

  @BeforeMethod
  public void setUp() {
    Set<String> k8sBasedComponents = new HashSet<>();
    k8sBasedComponents.add(KUBERNETES_COMPONENT_TYPE);
    applier =
        new KubernetesComponentToWorkspaceApplier(
            k8sRecipeParser, k8sEnvProvisioner, k8sBasedComponents);

    workspaceConfig = new WorkspaceConfigImpl();
  }

  @Test
  public void shouldProvisionEnvironmentWithCorrectRecipeTypeAndContentFromOSList()
      throws Exception {
    // given
    doReturn(emptyList()).when(k8sRecipeParser).parse(anyString());
    ComponentImpl component = new ComponentImpl();
    component.setType(KUBERNETES_COMPONENT_TYPE);
    component.setReference(REFERENCE_FILENAME);
    component.setAlias(COMPONENT_NAME);

    // when
    applier.apply(workspaceConfig, component, s -> "");

    // then
    verify(k8sEnvProvisioner)
        .provision(workspaceConfig, OpenShiftEnvironment.TYPE, emptyList(), emptyMap());
  }
}
