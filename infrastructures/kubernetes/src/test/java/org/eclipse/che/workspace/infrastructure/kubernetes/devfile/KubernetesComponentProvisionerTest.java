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
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.eclipse.che.api.workspace.server.devfile.exception.WorkspaceExportException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/** @author Sergii Leshchenko */
public class KubernetesComponentProvisionerTest {

  private KubernetesComponentProvisioner kubernetesComponentProvisioner;
  private static final List<String> HANDLED_TYPES =
      Arrays.asList(KubernetesEnvironment.TYPE, "funky");

  @BeforeMethod
  public void setUp() {
    kubernetesComponentProvisioner =
        new KubernetesComponentProvisioner(new HashSet<>(HANDLED_TYPES));
  }

  @Test(
      expectedExceptions = WorkspaceExportException.class,
      expectedExceptionsMessageRegExp =
          "Workspace with multiple kubernetes/funky environments can not be converted to devfile")
  public void shouldThrowExceptionIfWorkspaceHasMultipleEnvironmentsWithHandledRecipes()
      throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    EnvironmentImpl k8sEnv = new EnvironmentImpl();
    k8sEnv.setRecipe(new RecipeImpl(KubernetesEnvironment.TYPE, null, null, null));
    workspaceConfig.getEnvironments().put("k8sEnv", k8sEnv);

    EnvironmentImpl osEnv = new EnvironmentImpl();
    osEnv.setRecipe(new RecipeImpl("funky", null, null, null));
    workspaceConfig.getEnvironments().put("funkyEnv", osEnv);

    // when
    kubernetesComponentProvisioner.provision(new DevfileImpl(), workspaceConfig);
  }

  @Test
  public void shouldNoNothingIfWorkspaceDoesNotHaveEnvironmentsWithHandledRecipes()
      throws Exception {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    EnvironmentImpl anotherEnv = new EnvironmentImpl();
    anotherEnv.setRecipe(new RecipeImpl("nonK8sNorOS", null, null, null));
    workspaceConfig.getEnvironments().put("anotherEnv", anotherEnv);

    // when
    kubernetesComponentProvisioner.provision(new DevfileImpl(), workspaceConfig);
  }

  @Test(dataProvider = "handledTypes")
  public void shouldThrowExceptionIfWorkspaceHasEnvironmentWithExactlyOneHandledType(
      String handledType) {
    // given
    WorkspaceConfigImpl workspaceConfig = new WorkspaceConfigImpl();
    EnvironmentImpl k8sEnv = new EnvironmentImpl();
    k8sEnv.setRecipe(new RecipeImpl(handledType, null, null, null));
    workspaceConfig.getEnvironments().put("Env", k8sEnv);

    try {
      // when
      kubernetesComponentProvisioner.provision(new DevfileImpl(), workspaceConfig);
    } catch (WorkspaceExportException e) {
      // then
      String expectedMessage =
          format("Exporting of workspace with `%s` is not supported yet.", handledType);

      Assert.assertEquals(e.getMessage(), expectedMessage);
    }
  }

  @DataProvider
  public static Object[][] handledTypes() {
    return HANDLED_TYPES.stream().map(t -> new Object[] {t}).toArray(Object[][]::new);
  }
}
