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
package org.eclipse.che.api.devfile.server.convert.component.kubernetes;

import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.server.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class KubernetesComponentProvisionerTest {

  private KubernetesComponentProvisioner kubernetesComponentProvisioner;

  @BeforeMethod
  public void setUp() {
    kubernetesComponentProvisioner = new KubernetesComponentProvisioner();
  }

  @Test(
      expectedExceptions = WorkspaceExportException.class,
      expectedExceptionsMessageRegExp =
          "Workspace with multiple `kubernetes`/`openshift` environments can not be converted to devfile")
  public void shouldThrowExceptionIfWorkspaceHasMultipleEnvironmentsWithKubernetesOpenShiftRecipes()
      throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    EnvironmentImpl k8sEnv = new EnvironmentImpl();
    k8sEnv.setRecipe(new RecipeImpl(KubernetesEnvironment.TYPE, null, null, null));
    workspaceConfig.getEnvironments().put("k8sEnv", k8sEnv);

    EnvironmentImpl osEnv = new EnvironmentImpl();
    osEnv.setRecipe(new RecipeImpl(OpenShiftEnvironment.TYPE, null, null, null));
    workspaceConfig.getEnvironments().put("osEnv", osEnv);

    // when
    kubernetesComponentProvisioner.provision(new Devfile(), workspaceConfig);
  }

  @Test
  public void shouldNoNothingIfWorkspaceDoesNotHaveEnvironmentsWithKubernetesOpenShiftRecipes()
      throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    EnvironmentImpl anotherEnv = new EnvironmentImpl();
    anotherEnv.setRecipe(new RecipeImpl("nonK8sNorOS", null, null, null));
    workspaceConfig.getEnvironments().put("anotherEnv", anotherEnv);

    // when
    kubernetesComponentProvisioner.provision(new Devfile(), workspaceConfig);
  }

  @Test(
      expectedExceptions = WorkspaceExportException.class,
      expectedExceptionsMessageRegExp =
          "Exporting of workspace with `kubernetes` is not supported yet.")
  public void shouldThrowExceptionIfWorkspaceHasEnvironmentWithKubernetesRecipe() throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    EnvironmentImpl k8sEnv = new EnvironmentImpl();
    k8sEnv.setRecipe(new RecipeImpl(KubernetesEnvironment.TYPE, null, null, null));
    workspaceConfig.getEnvironments().put("k8sEnv", k8sEnv);

    // when
    kubernetesComponentProvisioner.provision(new Devfile(), workspaceConfig);
  }

  @Test(
      expectedExceptions = WorkspaceExportException.class,
      expectedExceptionsMessageRegExp =
          "Exporting of workspace with `openshift` is not supported yet.")
  public void shouldThrowExceptionIfWorkspaceHasEnvironmentWithOpenShiftRecipe() throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();

    EnvironmentImpl osEnv = new EnvironmentImpl();
    osEnv.setRecipe(new RecipeImpl(OpenShiftEnvironment.TYPE, null, null, null));
    workspaceConfig.getEnvironments().put("osEnv", osEnv);

    // when
    kubernetesComponentProvisioner.provision(new Devfile(), workspaceConfig);
  }
}
