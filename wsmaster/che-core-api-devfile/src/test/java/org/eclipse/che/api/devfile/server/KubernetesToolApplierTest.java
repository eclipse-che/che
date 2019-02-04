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
package org.eclipse.che.api.devfile.server;

import static io.fabric8.kubernetes.client.utils.Serialization.unmarshal;
import static org.eclipse.che.api.devfile.server.Constants.EDITOR_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.KubernetesToolApplier.YAML_CONTENT_TYPE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

/**
 * Tests {@link KubernetesToolApplier}.
 *
 * @author Max Shaposhnyk
 * @author Sergii Leshchenko
 */
public class KubernetesToolApplierTest {

  public static final String LOCAL_FILENAME = "local.yaml";
  public static final String TOOL_NAME = "foo";

  private WorkspaceConfigImpl workspaceConfig;
  private Devfile devfile;

  private KubernetesToolApplier applier;

  @BeforeMethod
  public void setUp() {
    devfile = new Devfile();
    applier = new KubernetesToolApplier();

    workspaceConfig = new WorkspaceConfigImpl();
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Unable to process tool '"
              + TOOL_NAME
              + "' of type '"
              + KUBERNETES_TOOL_TYPE
              + "' since there is no recipe content provider supplied. "
              + "That means you're trying to submit an devfile with recipe-type tools to the bare "
              + "devfile API or used factory URL does not support this feature.")
  public void shouldThrowExceptionWhenRecipeToolIsPresentAndNoContentProviderSupplied()
      throws Exception {
    Tool tool =
        new Tool().withType(KUBERNETES_TOOL_TYPE).withLocal(LOCAL_FILENAME).withName(TOOL_NAME);
    applier.apply(tool, devfile, workspaceConfig, null);
  }

  @Test(
      expectedExceptions = IllegalArgumentException.class,
      expectedExceptionsMessageRegExp =
          "Unable to create environment from tool '"
              + TOOL_NAME
              + "' - it has ineligible type '"
              + EDITOR_TOOL_TYPE
              + "'.")
  public void shouldReturnEmptyOptionalWhenNoRecipeToolIsPresent() throws Exception {
    Tool tool = new Tool().withType(EDITOR_TOOL_TYPE).withId("foo").withName(TOOL_NAME);

    applier.apply(tool, devfile, workspaceConfig, null);
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Error occurred during parsing list from file "
              + LOCAL_FILENAME
              + " for tool '"
              + TOOL_NAME
              + "': .*")
  public void shouldThrowExceptionWhenRecipeContentIsUnparseable() throws Exception {
    Tool tool =
        new Tool().withType(KUBERNETES_TOOL_TYPE).withLocal(LOCAL_FILENAME).withName(TOOL_NAME);
    applier.apply(tool, devfile, workspaceConfig, s -> "some_unparseable_content");
  }

  @Test(
      expectedExceptions = DevfileException.class,
      expectedExceptionsMessageRegExp =
          "Error during recipe content retrieval for tool '" + TOOL_NAME + "': fetch failed")
  public void shouldThrowExceptionWhenExceptionHappensOnContentProvider() throws Exception {
    Tool tool =
        new Tool().withType(KUBERNETES_TOOL_TYPE).withLocal(LOCAL_FILENAME).withName(TOOL_NAME);
    applier.apply(
        tool,
        devfile,
        workspaceConfig,
        e -> {
          throw new IOException("fetch failed");
        });
  }

  @Test
  public void shouldReturnEnvironmentWithCorrectRecipeTypeAndContentFromK8SList() throws Exception {
    String yamlRecipeContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("petclinic.yaml"));
    Tool tool =
        new Tool()
            .withType(KUBERNETES_TOOL_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(TOOL_NAME)
            .withSelector(new HashMap<>());

    applier.apply(tool, devfile, workspaceConfig, s -> yamlRecipeContent);

    String defaultEnv = workspaceConfig.getDefaultEnv();
    assertNotNull(defaultEnv);
    EnvironmentImpl environment = workspaceConfig.getEnvironments().get(defaultEnv);
    assertNotNull(environment);
    RecipeImpl recipe = environment.getRecipe();
    assertNotNull(recipe);
    assertEquals(recipe.getType(), KUBERNETES_TOOL_TYPE);
    assertEquals(recipe.getContentType(), YAML_CONTENT_TYPE);
    assertEquals(toK8SList(recipe.getContent()), toK8SList(yamlRecipeContent));
  }

  @Test
  public void shouldReturnEnvironmentWithCorrectRecipeTypeAndContentFromOSList() throws Exception {
    String yamlRecipeContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("petclinic.yaml"));
    Tool tool =
        new Tool()
            .withType(OPENSHIFT_TOOL_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(TOOL_NAME)
            .withSelector(new HashMap<>());

    applier.apply(tool, devfile, workspaceConfig, s -> yamlRecipeContent);

    String defaultEnv = workspaceConfig.getDefaultEnv();
    assertNotNull(defaultEnv);
    EnvironmentImpl environment = workspaceConfig.getEnvironments().get(defaultEnv);
    assertNotNull(environment);
    RecipeImpl recipe = environment.getRecipe();
    assertNotNull(recipe);
    assertEquals(recipe.getType(), OPENSHIFT_TOOL_TYPE);
    assertEquals(recipe.getContentType(), YAML_CONTENT_TYPE);
    assertEquals(toK8SList(recipe.getContent()), toK8SList(yamlRecipeContent));
  }

  @Test
  public void shouldFilterRecipeWithGivenSelectors() throws Exception {
    String yamlRecipeContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("petclinic.yaml"));

    final Map<String, String> selector =
        Collections.singletonMap("app.kubernetes.io/component", "webapp");
    Tool tool =
        new Tool()
            .withType(OPENSHIFT_TOOL_TYPE)
            .withLocal(LOCAL_FILENAME)
            .withName(TOOL_NAME)
            .withSelector(selector);

    applier.apply(tool, devfile, workspaceConfig, s -> yamlRecipeContent);

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

  private KubernetesList toK8SList(String content) {
    return unmarshal(content, KubernetesList.class);
  }
}
