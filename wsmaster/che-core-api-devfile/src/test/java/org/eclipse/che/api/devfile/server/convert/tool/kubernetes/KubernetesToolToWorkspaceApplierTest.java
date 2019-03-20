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
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.TOOL_NAME_COMMAND_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.convert.tool.kubernetes.KubernetesToolToWorkspaceApplier.YAML_CONTENT_TYPE;
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
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.devfile.model.Entrypoint;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.server.FileContentProvider.FetchNotSupportedProvider;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

/** @author Sergii Leshchenko */
@Listeners(MockitoTestNGListener.class)
public class KubernetesToolToWorkspaceApplierTest {

  public static final String LOCAL_FILENAME = "local.yaml";
  public static final String TOOL_NAME = "foo";

  private WorkspaceConfigImpl workspaceConfig;

  private KubernetesToolToWorkspaceApplier applier;
  @Mock private KubernetesRecipeParser k8sRecipeParser;

  @BeforeMethod
  public void setUp() {
    applier = new KubernetesToolToWorkspaceApplier(k8sRecipeParser);

    workspaceConfig = new WorkspaceConfigImpl();
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Fetching content of file `local.yaml` specified in `local` field of tool `foo` is not "
              + "supported. Please provide its content in `localContent` field. Cause: fetch is not supported")
  public void shouldThrowExceptionWhenRecipeToolIsPresentAndContentProviderDoesNotSupportFetching()
      throws Exception {
    // given
    Tool tool =
        new Tool().withType(KUBERNETES_TOOL_TYPE).withLocal(LOCAL_FILENAME).withName(TOOL_NAME);

    // when
    applier.apply(
        workspaceConfig,
        tool,
        e -> {
          throw new DevfileException("fetch is not supported");
        });
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Error occurred during parsing list from file "
              + LOCAL_FILENAME
              + " for tool '"
              + TOOL_NAME
              + "': .*")
  public void shouldThrowExceptionWhenRecipeContentIsNotAValidYaml() throws Exception {
    // given
    doThrow(new ValidationException("non valid")).when(k8sRecipeParser).parse(anyString());
    Tool tool =
        new Tool().withType(KUBERNETES_TOOL_TYPE).withLocal(LOCAL_FILENAME).withName(TOOL_NAME);

    // when
    applier.apply(workspaceConfig, tool, s -> "some_non_yaml_content");
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Error during recipe content retrieval for tool 'foo' with type 'kubernetes': fetch failed")
  public void shouldThrowExceptionWhenExceptionHappensOnContentProvider() throws Exception {
    // given
    Tool tool =
        new Tool().withType(KUBERNETES_TOOL_TYPE).withLocal(LOCAL_FILENAME).withName(TOOL_NAME);

    // when
    applier.apply(
        workspaceConfig,
        tool,
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
    Tool tool =
        new Tool()
            .withType(KUBERNETES_TOOL_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(TOOL_NAME)
            .withSelector(new HashMap<>());

    // when
    applier.apply(workspaceConfig, tool, s -> yamlRecipeContent);

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

  @Test
  public void shouldUseLocalContentAsRecipeIfPresent() throws Exception {
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());
    Tool tool =
        new Tool()
            .withType(KUBERNETES_TOOL_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withLocalContent(yamlRecipeContent)
            .withName(TOOL_NAME)
            .withSelector(new HashMap<>());

    applier.apply(workspaceConfig, tool, new FetchNotSupportedProvider());

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

  @Test
  public void shouldProvisionEnvironmentWithCorrectRecipeTypeAndContentFromOSList()
      throws Exception {
    // given
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());
    Tool tool =
        new Tool()
            .withType(OPENSHIFT_TOOL_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(TOOL_NAME)
            .withSelector(new HashMap<>());

    // when
    applier.apply(workspaceConfig, tool, s -> yamlRecipeContent);

    // then
    String defaultEnv = workspaceConfig.getDefaultEnv();
    assertNotNull(defaultEnv);
    EnvironmentImpl environment = workspaceConfig.getEnvironments().get(defaultEnv);
    assertNotNull(environment);
    RecipeImpl recipe = environment.getRecipe();
    assertNotNull(recipe);
    assertEquals(recipe.getType(), OPENSHIFT_TOOL_TYPE);
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
    Tool tool =
        new Tool()
            .withType(OPENSHIFT_TOOL_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(TOOL_NAME)
            .withSelector(selector);
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    // when
    applier.apply(workspaceConfig, tool, s -> yamlRecipeContent);

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

  @Test(dependsOnMethods = "shouldFilterRecipeWithGivenSelectors")
  public void shouldSetMachineNameAttributeToCommandConfiguredInOpenShiftToolWithOneContainer()
      throws Exception {
    // given
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    final Map<String, String> selector = singletonMap("app.kubernetes.io/component", "webapp");
    Tool tool =
        new Tool()
            .withType(OPENSHIFT_TOOL_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(TOOL_NAME)
            .withSelector(selector);
    CommandImpl command = new CommandImpl();
    command.getAttributes().put(TOOL_NAME_COMMAND_ATTRIBUTE, TOOL_NAME);
    workspaceConfig.getCommands().add(command);

    // when
    applier.apply(workspaceConfig, tool, s -> yamlRecipeContent);

    // then
    CommandImpl actualCommand = workspaceConfig.getCommands().get(0);
    assertEquals(actualCommand.getAttributes().get(MACHINE_NAME_ATTRIBUTE), "petclinic/server");
  }

  @Test
  public void
      shouldNotSetMachineNameAttributeToCommandConfiguredInOpenShiftToolWithMultipleContainers()
          throws Exception {
    // given
    String yamlRecipeContent = getResource("petclinic.yaml");
    doReturn(toK8SList(yamlRecipeContent).getItems()).when(k8sRecipeParser).parse(anyString());

    Tool tool =
        new Tool()
            .withType(OPENSHIFT_TOOL_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(TOOL_NAME)
            .withSelector(new HashMap<>());

    CommandImpl command = new CommandImpl();
    command.getAttributes().put(TOOL_NAME_COMMAND_ATTRIBUTE, TOOL_NAME);
    workspaceConfig.getCommands().add(command);

    // when
    applier.apply(workspaceConfig, tool, s -> yamlRecipeContent);

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
    Tool tool =
        new Tool()
            .withType(OPENSHIFT_TOOL_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(TOOL_NAME)
            .withEntrypoints(
                singletonList(new Entrypoint().withParentName("petclinic").withCommand(command)))
            .withSelector(Collections.emptyMap());

    // when
    applier.apply(workspaceConfig, tool, s -> yamlRecipeContent);

    // then
    RecipeImpl recipe = workspaceConfig.getEnvironments().get(TOOL_NAME).getRecipe();
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
