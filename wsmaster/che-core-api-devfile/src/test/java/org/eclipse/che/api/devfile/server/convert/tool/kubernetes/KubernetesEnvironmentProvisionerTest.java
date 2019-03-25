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
package org.eclipse.che.api.devfile.server.convert.tool.kubernetes;

import static io.fabric8.kubernetes.client.utils.Serialization.unmarshal;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.convert.tool.kubernetes.KubernetesEnvironmentProvisioner.YAML_CONTENT_TYPE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

/**
 * Tests {@link KubernetesEnvironmentProvisioner}
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesEnvironmentProvisionerTest {

  private WorkspaceConfigImpl workspaceConfig;

  @Mock private KubernetesRecipeParser k8sRecipeParser;
  @InjectMocks private KubernetesEnvironmentProvisioner k8sEnvProvisioner;

  @BeforeMethod
  public void setUp() {
    workspaceConfig = new WorkspaceConfigImpl();
  }

  @Test
  public void shouldProvisionEnvironmentWithCorrectRecipeTypeAndContentFromK8SList()
      throws Exception {
    // given
    String yamlRecipeContent = getResource("petclinic.yaml");
    List<HasMetadata> toolObjects = toK8SList(yamlRecipeContent).getItems();

    // when
    k8sEnvProvisioner.provision(
        workspaceConfig, KubernetesEnvironment.TYPE, toolObjects, emptyMap());

    // then
    String defaultEnv = workspaceConfig.getDefaultEnv();
    assertNotNull(defaultEnv);
    EnvironmentImpl environment = workspaceConfig.getEnvironments().get(defaultEnv);
    assertNotNull(environment);
    RecipeImpl recipe = environment.getRecipe();
    assertNotNull(recipe);
    assertEquals(recipe.getType(), KUBERNETES_TOOL_TYPE);
    assertEquals(recipe.getContentType(), YAML_CONTENT_TYPE);

    // it is expected that applier wrap original recipes objects in new Kubernetes list
    KubernetesList expectedKubernetesList =
        new KubernetesListBuilder().withItems(toK8SList(yamlRecipeContent).getItems()).build();
    assertEquals(toK8SList(recipe.getContent()).getItems(), expectedKubernetesList.getItems());
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp = "Environment already contains machine 'machine'")
  public void shouldThrowAnExceptionIfEnvironmentContainMachineWithSpecifiedName()
      throws Exception {
    // given
    workspaceConfig.setDefaultEnv("default");
    workspaceConfig.setEnvironments(
        ImmutableMap.of(
            "default",
            new EnvironmentImpl(null, ImmutableMap.of("machine", new MachineConfigImpl()))));
    // when
    k8sEnvProvisioner.provision(
        workspaceConfig,
        KubernetesEnvironment.TYPE,
        emptyList(),
        ImmutableMap.of("machine", new MachineConfigImpl()));
  }

  @Test
  public void shouldUpgradeKubernetesEnvironmentToOpenShiftTypeOnOpenShiftToolProvisioning()
      throws Exception {
    // given
    workspaceConfig.setDefaultEnv("default");
    RecipeImpl existingRecipe =
        new RecipeImpl(KubernetesEnvironment.TYPE, "yaml", "existing-content", null);
    workspaceConfig
        .getEnvironments()
        .put("default", new EnvironmentImpl(existingRecipe, emptyMap()));

    List<HasMetadata> toolsObject = new ArrayList<>();
    Deployment toolDeployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName("web-app")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    toolsObject.add(new DeploymentBuilder(toolDeployment).build());
    doReturn(new ArrayList<>()).when(k8sRecipeParser).parse(anyString());

    // when
    k8sEnvProvisioner.provision(
        workspaceConfig, OpenShiftEnvironment.TYPE, toolsObject, emptyMap());

    // then
    EnvironmentImpl resultEnv =
        workspaceConfig.getEnvironments().get(workspaceConfig.getDefaultEnv());
    RecipeImpl resultRecipe = resultEnv.getRecipe();
    assertEquals(resultRecipe.getType(), OpenShiftEnvironment.TYPE);
  }

  @Test
  public void shouldProvisionToolObjectsIntoExistingKubernetesRecipe() throws Exception {
    // given
    workspaceConfig.setDefaultEnv("default");
    RecipeImpl existingRecipe =
        new RecipeImpl(KUBERNETES_TOOL_TYPE, "yaml", "existing-content", null);
    workspaceConfig
        .getEnvironments()
        .put("default", new EnvironmentImpl(existingRecipe, emptyMap()));

    List<HasMetadata> recipeObjects = new ArrayList<>();
    Deployment recipeDeployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName("db")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    recipeObjects.add(new DeploymentBuilder(recipeDeployment).build());
    doReturn(recipeObjects).when(k8sRecipeParser).parse(anyString());

    List<HasMetadata> toolsObject = new ArrayList<>();
    Deployment toolDeployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName("web-app")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    toolsObject.add(new DeploymentBuilder(toolDeployment).build());

    // when
    k8sEnvProvisioner.provision(
        workspaceConfig, KubernetesEnvironment.TYPE, toolsObject, emptyMap());

    // then
    // it is expected that applier wrap original recipes objects in new Kubernetes list
    KubernetesList expectedKubernetesList =
        new KubernetesListBuilder()
            .withItems(Arrays.asList(recipeDeployment, toolDeployment))
            .build();
    EnvironmentImpl resultEnv =
        workspaceConfig.getEnvironments().get(workspaceConfig.getDefaultEnv());
    assertEquals(
        toK8SList(resultEnv.getRecipe().getContent()).getItems(),
        expectedKubernetesList.getItems());
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Kubernetes tool can only be applied to a workspace with either kubernetes or openshift "
              + "recipe type but workspace has a recipe of type 'any'")
  public void shouldThrowAnExceptionIfWorkspaceAlreadyContainNonK8sNorOSRecipe() throws Exception {
    // given
    workspaceConfig.setDefaultEnv("default");
    RecipeImpl existingRecipe = new RecipeImpl("any", "yaml", "existing-content", null);
    workspaceConfig
        .getEnvironments()
        .put("default", new EnvironmentImpl(existingRecipe, emptyMap()));

    List<HasMetadata> toolsObject = new ArrayList<>();
    Deployment toolDeployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName("db")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    toolsObject.add(new DeploymentBuilder(toolDeployment).build());

    // when
    k8sEnvProvisioner.provision(
        workspaceConfig, KubernetesEnvironment.TYPE, toolsObject, emptyMap());
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Tools can not have objects with the same name and kind "
              + "but there are multiple objects with kind 'Service' and name 'db'")
  public void shouldThrowExceptionIfDifferentToolsHaveObjectsWithTheSameKindAndName()
      throws Exception {
    // given
    List<HasMetadata> toolsObject = new ArrayList<>();
    Service service1 =
        new ServiceBuilder()
            .withNewMetadata()
            .withName("db")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    Service service2 =
        new ServiceBuilder()
            .withNewMetadata()
            .withName("db")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    toolsObject.add(new ServiceBuilder(service1).build());
    toolsObject.add(new ServiceBuilder(service2).build());

    // when
    k8sEnvProvisioner.provision(
        workspaceConfig, KubernetesEnvironment.TYPE, toolsObject, emptyMap());
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Tools can not have objects with the same name and kind but there are multiple objects with kind 'Service' and name 'db'")
  public void shouldThrowExceptionIfToolHasMultipleObjectsWithTheSameKindAndName()
      throws Exception {
    // given
    List<HasMetadata> objects = new ArrayList<>();
    Service service =
        new ServiceBuilder()
            .withNewMetadata()
            .withName("db")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    objects.add(new ServiceBuilder(service).build());
    objects.add(new ServiceBuilder(service).build());

    // when
    k8sEnvProvisioner.provision(workspaceConfig, KubernetesEnvironment.TYPE, objects, emptyMap());
  }

  private KubernetesList toK8SList(String content) {
    return unmarshal(content, KubernetesList.class);
  }

  private String getResource(String resourceName) throws IOException {
    return Files.readFile(getClass().getClassLoader().getResourceAsStream(resourceName));
  }
}
