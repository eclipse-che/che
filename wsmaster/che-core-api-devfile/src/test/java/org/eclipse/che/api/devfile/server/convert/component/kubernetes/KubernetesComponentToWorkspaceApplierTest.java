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

import static io.fabric8.kubernetes.client.utils.Serialization.unmarshal;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.COMPONENT_NAME_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.convert.component.kubernetes.KubernetesComponentToWorkspaceApplier.YAML_CONTENT_TYPE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.devfile.model.Component;
import org.eclipse.che.api.devfile.model.Entrypoint;
import org.eclipse.che.api.devfile.server.FileContentProvider.FetchNotSupportedProvider;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

/** @author Sergii Leshchenko */
@Listeners(MockitoTestNGListener.class)
public class KubernetesComponentToWorkspaceApplierTest {

  public static final String LOCAL_FILENAME = "local.yaml";
  public static final String COMPONENT_NAME = "foo";

  private WorkspaceConfigImpl workspaceConfig;

  private KubernetesComponentToWorkspaceApplier applier;
  @Mock private KubernetesRecipeParser k8sRecipeParser;

  @BeforeMethod
  public void setUp() {
    applier = new KubernetesComponentToWorkspaceApplier(k8sRecipeParser);

    workspaceConfig = new WorkspaceConfigImpl();
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Fetching content of file `local.yaml` specified in `local` field of component `foo` is not "
              + "supported. Please provide its content in `localContent` field. Cause: fetch is not supported")
  public void shouldThrowExceptionWhenRecipeComponentIsPresentAndContentProviderDoesNotSupportFetching()
      throws Exception {
    // given
    Component component =
        new Component()
            .withType(KUBERNETES_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME);

    // when
    applier.apply(
        workspaceConfig,
        component,
        e -> {
          throw new DevfileException("fetch is not supported");
        });
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Error occurred during parsing list from file "
              + LOCAL_FILENAME
              + " for component '"
              + COMPONENT_NAME
              + "': .*")
  public void shouldThrowExceptionWhenRecipeContentIsNotAValidYaml() throws Exception {
    // given
    doThrow(new ValidationException("non valid")).when(k8sRecipeParser).parse(anyString());
    Component component =
        new Component()
            .withType(KUBERNETES_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME);

    // when
    applier.apply(workspaceConfig, component, s -> "some_non_yaml_content");
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Error during recipe content retrieval for component 'foo' with type 'kubernetes': fetch failed")
  public void shouldThrowExceptionWhenExceptionHappensOnContentProvider() throws Exception {
    // given
    Component component =
        new Component()
            .withType(KUBERNETES_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME);

    // when
    applier.apply(
        workspaceConfig,
        component,
        e -> {
          throw new IOException("fetch failed");
        });
  }

  @Test
  public void shouldProvisionEnvironmentWithCorrectRecipeTypeAndContentFromK8SList()
      throws Exception {
    // given
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());
    Component component =
        new Component()
            .withType(KUBERNETES_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(new HashMap<>());

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    String defaultEnv = workspaceConfig.getDefaultEnv();
    assertNotNull(defaultEnv);
    EnvironmentImpl environment = workspaceConfig.getEnvironments().get(defaultEnv);
    assertNotNull(environment);
    RecipeImpl recipe = environment.getRecipe();
    assertNotNull(recipe);
    assertEquals(recipe.getType(), KUBERNETES_COMPONENT_TYPE);
    assertEquals(recipe.getContentType(), YAML_CONTENT_TYPE);

    // it is expected that applier wrap original recipes objects in new Kubernetes list
    KubernetesList expectedKubernetesList =
        new KubernetesListBuilder().withItems(toK8SList(yamlRecipeContent).getItems()).build();
    assertEquals(toK8SList(recipe.getContent()).getItems(), expectedKubernetesList.getItems());
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Components can not have objects with the same name and kind but there are multiple objects with kind 'Service' and name 'db'")
  public void shouldThrowExceptionIfComponentHasMultipleObjectsWithTheSameKindAndName()
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
    doReturn(objects).when(k8sRecipeParser).parse(anyString());
    Component component =
        new Component()
            .withType(KUBERNETES_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(new HashMap<>());

    // when
    applier.apply(workspaceConfig, component, s -> "content");
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp =
          "Components can not have objects with the same name and kind "
              + "but there are multiple objects with kind 'Service' and name 'db'")
  public void shouldThrowExceptionIfDifferentComponentsHaveObjectsWithTheSameKindAndName()
      throws Exception {
    // given
    List<HasMetadata> objects = new ArrayList<>();
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
    objects.add(new ServiceBuilder(service1).build());
    objects.add(new ServiceBuilder(service2).build());
    doReturn(objects).when(k8sRecipeParser).parse(anyString());
    Component component =
        new Component()
            .withType(KUBERNETES_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(new HashMap<>());

    // when
    applier.apply(workspaceConfig, component, s -> "content");
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Kubernetes component can only be applied to a workspace with either kubernetes or openshift "
              + "recipe type but workspace has a recipe of type 'any'")
  public void shouldThrowAnExceptionIfWorkspaceAlreadyContainNonK8sNorOSRecipe() throws Exception {
    // given
    workspaceConfig.setDefaultEnv("default");
    RecipeImpl existingRecipe = new RecipeImpl("any", "yaml", "existing-content", null);
    workspaceConfig
        .getEnvironments()
        .put("default", new EnvironmentImpl(existingRecipe, emptyMap()));

    List<HasMetadata> componentsObject = new ArrayList<>();
    Deployment componentDeployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName("db")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    componentsObject.add(new DeploymentBuilder(componentDeployment).build());
    doReturn(componentsObject).when(k8sRecipeParser).parse(anyString());
    Component component =
        new Component()
            .withType(KUBERNETES_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(new HashMap<>());

    // when
    applier.apply(workspaceConfig, component, s -> "content");
  }

  @Test
  public void shouldProvisionComponentObjectsIntoExistingKubernetesRecipe() throws Exception {
    // given
    workspaceConfig.setDefaultEnv("default");
    RecipeImpl existingRecipe =
        new RecipeImpl(KUBERNETES_COMPONENT_TYPE, "yaml", "existing-content", null);
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

    List<HasMetadata> componentsObject = new ArrayList<>();
    Deployment componentDeployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName("web-app")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    componentsObject.add(new DeploymentBuilder(componentDeployment).build());
    doReturn(componentsObject).doReturn(recipeObjects).when(k8sRecipeParser).parse(anyString());
    Component component =
        new Component()
            .withType(KUBERNETES_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(new HashMap<>());

    // when
    applier.apply(workspaceConfig, component, s -> "content");

    // then
    // it is expected that applier wrap original recipes objects in new Kubernetes list
    KubernetesList expectedKubernetesList =
        new KubernetesListBuilder()
            .withItems(Arrays.asList(recipeDeployment, componentDeployment))
            .build();
    EnvironmentImpl resultEnv =
        workspaceConfig.getEnvironments().get(workspaceConfig.getDefaultEnv());
    assertEquals(
        toK8SList(resultEnv.getRecipe().getContent()).getItems(),
        expectedKubernetesList.getItems());
  }

  @Test
  public void shouldUpgradeKubernetesEnvironmentToOpenShiftTypeOnOpenShiftComponentProvisioning()
      throws Exception {
    // given
    workspaceConfig.setDefaultEnv("default");
    RecipeImpl existingRecipe =
        new RecipeImpl(KUBERNETES_COMPONENT_TYPE, "yaml", "existing-content", null);
    workspaceConfig
        .getEnvironments()
        .put("default", new EnvironmentImpl(existingRecipe, emptyMap()));

    List<HasMetadata> componentsObject = new ArrayList<>();
    Deployment componentDeployment =
        new DeploymentBuilder()
            .withNewMetadata()
            .withName("web-app")
            .endMetadata()
            .withNewSpec()
            .endSpec()
            .build();
    componentsObject.add(new DeploymentBuilder(componentDeployment).build());
    doReturn(componentsObject).doReturn(new ArrayList<>()).when(k8sRecipeParser).parse(anyString());
    Component component =
        new Component()
            .withType(OPENSHIFT_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(new HashMap<>());

    // when
    applier.apply(workspaceConfig, component, s -> "content");

    // then
    EnvironmentImpl resultEnv =
        workspaceConfig.getEnvironments().get(workspaceConfig.getDefaultEnv());
    RecipeImpl resultRecipe = resultEnv.getRecipe();
    assertEquals(resultRecipe.getType(), OpenShiftEnvironment.TYPE);
  }

  @Test
  public void shouldUseLocalContentAsRecipeIfPresent() throws Exception {
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());
    Component component =
        new Component()
            .withType(KUBERNETES_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withLocalContent(yamlRecipeContent)
            .withName(COMPONENT_NAME)
            .withSelector(new HashMap<>());

    applier.apply(workspaceConfig, component, new FetchNotSupportedProvider());

    String defaultEnv = workspaceConfig.getDefaultEnv();
    assertNotNull(defaultEnv);
    EnvironmentImpl environment = workspaceConfig.getEnvironments().get(defaultEnv);
    assertNotNull(environment);
    RecipeImpl recipe = environment.getRecipe();
    assertNotNull(recipe);
    assertEquals(recipe.getType(), KUBERNETES_COMPONENT_TYPE);
    assertEquals(recipe.getContentType(), YAML_CONTENT_TYPE);

    // it is expected that applier wrap original recipes objects in new Kubernetes list
    KubernetesList expectedKubernetesList =
        new KubernetesListBuilder().withItems(toK8SList(yamlRecipeContent).getItems()).build();
    assertEquals(toK8SList(recipe.getContent()).getItems(), expectedKubernetesList.getItems());
  }

  @Test
  public void shouldProvisionEnvironmentWithCorrectRecipeTypeAndContentFromOSList()
      throws Exception {
    // given
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());
    Component component =
        new Component()
            .withType(OPENSHIFT_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(new HashMap<>());

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    String defaultEnv = workspaceConfig.getDefaultEnv();
    assertNotNull(defaultEnv);
    EnvironmentImpl environment = workspaceConfig.getEnvironments().get(defaultEnv);
    assertNotNull(environment);
    RecipeImpl recipe = environment.getRecipe();
    assertNotNull(recipe);
    assertEquals(recipe.getType(), OPENSHIFT_COMPONENT_TYPE);
    assertEquals(recipe.getContentType(), YAML_CONTENT_TYPE);

    // it is expected that applier wrap original recipes objects in new Kubernetes list
    KubernetesList expectedKubernetesList =
        new KubernetesListBuilder().withItems(toK8SList(yamlRecipeContent).getItems()).build();
    assertEquals(toK8SList(recipe.getContent()).getItems(), expectedKubernetesList.getItems());
  }

  @Test
  public void shouldFilterRecipeWithGivenSelectors() throws Exception {
    // given
    String yamlRecipeContent = getResource("petclinic.yaml");

    final Map<String, String> selector = singletonMap("app.kubernetes.io/component", "webapp");
    Component component =
        new Component()
            .withType(OPENSHIFT_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(selector);
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    String defaultEnv = workspaceConfig.getDefaultEnv();
    assertNotNull(defaultEnv);
    EnvironmentImpl environment = workspaceConfig.getEnvironments().get(defaultEnv);
    assertNotNull(environment);
    RecipeImpl recipe = environment.getRecipe();

    List<HasMetadata> resultItemsList = toK8SList(recipe.getContent()).getItems();
    assertEquals(resultItemsList.size(), 3);
    assertEquals(1, resultItemsList.stream().filter(it -> "Pod".equals(it.getKind())).count());
    assertEquals(1, resultItemsList.stream().filter(it -> "Service".equals(it.getKind())).count());
    assertEquals(1, resultItemsList.stream().filter(it -> "Route".equals(it.getKind())).count());
  }

  @Test(dependsOnMethods = "shouldFilterRecipeWithGivenSelectors", enabled = false)
  public void shouldSetMachineNameAttributeToCommandConfiguredInOpenShiftComponentWithOneContainer()
      throws Exception {
    // given
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    final Map<String, String> selector = singletonMap("app.kubernetes.io/component", "webapp");
    Component component =
        new Component()
            .withType(OPENSHIFT_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(selector);
    CommandImpl command = new CommandImpl();
    command.getAttributes().put(COMPONENT_NAME_COMMAND_ATTRIBUTE, COMPONENT_NAME);
    workspaceConfig.getCommands().add(command);

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    CommandImpl actualCommand = workspaceConfig.getCommands().get(0);
    assertEquals(actualCommand.getAttributes().get(MACHINE_NAME_ATTRIBUTE), "petclinic/server");
  }

  @Test
  public void
  shouldNotSetMachineNameAttributeToCommandConfiguredInOpenShiftComponentWithMultipleContainers()
          throws Exception {
    // given
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    Component component =
        new Component()
            .withType(OPENSHIFT_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withSelector(new HashMap<>());

    CommandImpl command = new CommandImpl();
    command.getAttributes().put(COMPONENT_NAME_COMMAND_ATTRIBUTE, COMPONENT_NAME);
    workspaceConfig.getCommands().add(command);

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    CommandImpl actualCommand = workspaceConfig.getCommands().get(0);
    assertNull(actualCommand.getAttributes().get(MACHINE_NAME_ATTRIBUTE));
  }

  @Test
  public void shouldChangeEntrypointsOnMatchingContainers() throws Exception {
    // given
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    List<String> command = asList("teh", "command");
    Component component =
        new Component()
            .withType(OPENSHIFT_COMPONENT_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(COMPONENT_NAME)
            .withEntrypoints(
                singletonList(new Entrypoint().withParentName("petclinic").withCommand(command)))
            .withSelector(Collections.emptyMap());

    // when
    applier.apply(workspaceConfig, component, s -> yamlRecipeContent);

    // then
    RecipeImpl recipe = workspaceConfig.getEnvironments().get(COMPONENT_NAME).getRecipe();
    KubernetesList list = toK8SList(recipe.getContent());
    for (HasMetadata o : list.getItems()) {
      if (o instanceof Pod) {
        Pod p = (Pod) o;

        // ignore pods that don't have containers
        if (p.getSpec() == null) {
          continue;
        }

        Container c = p.getSpec().getContainers().get(0);
        if (o.getMetadata().getName().equals("petclinic")) {
          assertEquals(c.getCommand(), command);
        } else {
          assertTrue(c.getCommand() == null || c.getCommand().isEmpty());
        }
      }
    }
  }

  private KubernetesList toK8SList(String content) {
    return unmarshal(content, KubernetesList.class);
  }

  private String getResource(String resourceName) throws IOException {
    return Files.readFile(getClass().getClassLoader().getResourceAsStream(resourceName));
  }
}
