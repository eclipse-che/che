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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_TOOL_TYPE;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.utils.Serialization;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;

/**
 * Creates {@link EnvironmentImpl} from specific type of devfile tool.
 *
 * @author Max Shaposhnyk
 */
@Singleton
public class DevfileEnvironmentFactory {

  static final String DEFAULT_RECIPE_CONTENT_TYPE = "application/x-yaml";

  /**
   * Consumes an recipe-type tool (openshift or kubernetes) and tries to create {@link
   * EnvironmentImpl} from it (including filtering of list items using selectors, if necessary). An
   * {@link RecipeFileContentProvider} MUST be provided in order to fetch recipe content.
   *
   * @param recipeTool the recipe-type tool
   * @param recipeFileContentProvider service-specific provider of recipe file content
   * @return constructed environment from recipe type tool
   * @throws IllegalArgumentException when wrong type tool is passed
   * @throws IllegalArgumentException when there is no content provider for recipe-type tool
   * @throws DevfileException when general devfile error occurs
   * @throws DevfileRecipeFormatException when recipe-type tool content is empty or has wrong format
   */
  public EnvironmentImpl createEnvironment(
      Tool recipeTool, RecipeFileContentProvider recipeFileContentProvider)
      throws DevfileRecipeFormatException, DevfileException {
    final String type = recipeTool.getType();
    if (!KUBERNETES_TOOL_TYPE.equals(type) && !OPENSHIFT_TOOL_TYPE.equals(type)) {
      throw new IllegalArgumentException(
          format(
              "Unable to create environment from tool '%s' - it has ineligible type '%s'.",
              recipeTool.getName(), type));
    }
    if (recipeFileContentProvider == null) {
      throw new DevfileException(
          format(
              "Unable to process tool '%s' of type '%s' since there is no recipe content provider supplied. "
                  + "That means you're trying to submit an devfile with recipe-type tools to the bare devfile API or used factory URL does not support this feature.",
              recipeTool.getName(), type));
    }

    String recipeFileContent;
    try {
      recipeFileContent = recipeFileContentProvider.fetchContent(recipeTool.getLocal());
    } catch (IOException e) {
      throw new DevfileException(
          format("Error during recipe content retrieval for tool '%s': ", recipeTool.getName())
              + e.getMessage(),
          e);
    }
    if (isNullOrEmpty(recipeFileContent)) {
      throw new DevfileException(
          format(
              "The local file '%s' defined in tool '%s' is unreachable or empty.",
              recipeTool.getLocal(), recipeTool.getName()));
    }
    final KubernetesList list = unmarshal(recipeTool, recipeFileContent);

    if (recipeTool.getSelector() != null && !recipeTool.getSelector().isEmpty()) {
      List<HasMetadata> itemsList =
          list.getItems()
              .stream()
              .filter(
                  e ->
                      e.getMetadata()
                          .getLabels()
                          .entrySet()
                          .containsAll(recipeTool.getSelector().entrySet()))
              .collect(Collectors.toList());
      list.setItems(itemsList);
    }
    RecipeImpl recipe =
        new RecipeImpl(type, DEFAULT_RECIPE_CONTENT_TYPE, asYaml(recipeTool, list), null);
    return new EnvironmentImpl(recipe, emptyMap());
  }

  private KubernetesList unmarshal(Tool tool, String recipeContent)
      throws DevfileRecipeFormatException {
    try {
      return Serialization.unmarshal(recipeContent, KubernetesList.class);
    } catch (KubernetesClientException e) {
      throw new DevfileRecipeFormatException(
          format(
              "Error occurred during parsing list from file %s for tool '%s': %s",
              tool.getLocal(), tool.getName(), e.getMessage()));
    }
  }

  private String asYaml(Tool tool, KubernetesList list) throws DevfileRecipeFormatException {
    try {
      return Serialization.asYaml(list);
    } catch (KubernetesClientException e) {
      throw new DevfileRecipeFormatException(
          format(
              "Unable to deserialize specified local file content for tool '%s'. Error: %s",
              tool.getName(), e.getMessage()));
    }
  }
}
