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

import static org.eclipse.che.api.devfile.server.Constants.EDITOR_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.DevfileEnvironmentFactory.DEFAULT_RECIPE_CONTENT_TYPE;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.commons.lang.Pair;
import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

@Listeners(MockitoTestNGListener.class)
public class DevfileEnvironmentFactoryTest {

  public static final String LOCAL_FILENAME = "local.yaml";
  public static final String TOOL_NAME = "foo";

  private final KubernetesClient client = new DefaultKubernetesClient();

  @InjectMocks private DevfileEnvironmentFactory factory;

  @Test(
      expectedExceptions = BadRequestException.class,
      expectedExceptionsMessageRegExp =
          "There is no content provider registered for '" + KUBERNETES_TOOL_TYPE + "' type tools.")
  public void shouldThrowExceptionWhenRecipeToolIsPresentAndNoURLComposerGiven() throws Exception {
    Tool tool =
        new Tool().withType(KUBERNETES_TOOL_TYPE).withLocal(LOCAL_FILENAME).withName(TOOL_NAME);
    factory.createEnvironment(tool, null);
  }

  @Test(
      expectedExceptions = BadRequestException.class,
      expectedExceptionsMessageRegExp = "Environment cannot be created from such type of tool.")
  public void shouldReturnEmptyOptionalWhenNoRecipeToolIsPresent() throws Exception {
    Tool tool = new Tool().withType(EDITOR_TOOL_TYPE).withId("foo").withName(TOOL_NAME);

    factory.createEnvironment(tool, null);
  }

  @Test(
      expectedExceptions = BadRequestException.class,
      expectedExceptionsMessageRegExp =
          "The local file '"
              + LOCAL_FILENAME
              + "' defined in tool  '"
              + TOOL_NAME
              + "' is unreachable or empty.")
  public void shouldThrowExceptionWhenRecipeContentIsNull() throws Exception {
    Tool tool =
        new Tool().withType(OPENSHIFT_TOOL_TYPE).withLocal(LOCAL_FILENAME).withName(TOOL_NAME);
    factory.createEnvironment(tool, s -> "");
  }

  @Test
  public void shouldReturnEnvironmentWithCorrectRecipeTypeAndContentFromK8SList() throws Exception {
    String yamlRecipeContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("petclinic.yaml"));
    Tool tool =
        new Tool().withType(KUBERNETES_TOOL_TYPE).withLocal(LOCAL_FILENAME).withName(TOOL_NAME);

    Optional<Pair<String, EnvironmentImpl>> result =
        factory.createEnvironment(tool, s -> yamlRecipeContent);

    assertTrue(result.isPresent());
    assertEquals(result.get().first, TOOL_NAME);
    RecipeImpl recipe = result.get().second.getRecipe();
    assertNotNull(recipe);
    assertEquals(recipe.getType(), KUBERNETES_TOOL_TYPE);
    assertEquals(recipe.getContentType(), DEFAULT_RECIPE_CONTENT_TYPE);
    assertEquals(toK8SList(recipe.getContent()), toK8SList(yamlRecipeContent));
  }

  @Test
  public void shouldReturnEnvironmentWithCorrectRecipeTypeAndContentFromOSList() throws Exception {
    String yamlRecipeContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("petclinic.yaml"));
    Tool tool =
        new Tool().withType(OPENSHIFT_TOOL_TYPE).withLocal(LOCAL_FILENAME).withName(TOOL_NAME);

    Optional<Pair<String, EnvironmentImpl>> result =
        factory.createEnvironment(tool, s -> yamlRecipeContent);

    assertTrue(result.isPresent());
    assertEquals(result.get().first, TOOL_NAME);
    RecipeImpl recipe = result.get().second.getRecipe();
    assertNotNull(recipe);
    assertEquals(recipe.getType(), OPENSHIFT_TOOL_TYPE);
    assertEquals(recipe.getContentType(), DEFAULT_RECIPE_CONTENT_TYPE);
    assertEquals(toK8SList(recipe.getContent()), toK8SList(yamlRecipeContent));
  }

  private KubernetesList toK8SList(String content) {
    return client
        .lists()
        .load(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)))
        .get();
  }
}
