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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import javax.inject.Singleton;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.commons.lang.Pair;

/**
 * Creates workspace environment from specific tool in devfile if any.
 *
 * @author Max Shaposhnyk
 */
@Singleton
public class DevfileEnvironmentFactory {

  static final String DEFAULT_RECIPE_CONTENT_TYPE = "application/x-yaml";

  private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
  private final KubernetesClient client = new DefaultKubernetesClient();

  /**
   * Consumes an recipe-type tool (openshift or kubernetes) from devfile and tries to create {@link
   * EnvironmentImpl} from it (including filtering of list items using selectors, if necessary). An
   * {@link RecipeFileContentProvider} MUST be provided in order to fetch recipe content.
   *
   * @param recipeTool the recipe-type tool
   * @param recipeFileContentProvider service-specific provider of recipe file content
   * @return optional pair of the recipe-type tool name and newly constructed environment from it
   * @throws BadRequestException when there is no content provider for recipe-type tool
   * @throws BadRequestException when recipe-type tool content is unreachable or empty
   */
  public Optional<Pair<String, EnvironmentImpl>> createEnvironment(
      Tool recipeTool, RecipeFileContentProvider recipeFileContentProvider)
      throws BadRequestException {
    final String type = recipeTool.getType();
    if (!KUBERNETES_TOOL_TYPE.equals(type) && !OPENSHIFT_TOOL_TYPE.equals(type)) {
      throw new BadRequestException("Environment cannot be created from such type of tool.");
    }
    if (recipeFileContentProvider == null) {
      throw new BadRequestException(
          format("There is no content provider registered for '%s' type tools.", type));
    }

    String recipeFileContent = recipeFileContentProvider.fetchContent(recipeTool.getLocal());
    if (isNullOrEmpty(recipeFileContent)) {
      throw new BadRequestException(
          format(
              "The local file '%s' defined in tool  '%s' is unreachable or empty.",
              recipeTool.getLocal(), recipeTool.getName()));
    }

    final KubernetesList list =
        client
            .lists()
            .load(new ByteArrayInputStream(recipeFileContent.getBytes(StandardCharsets.UTF_8)))
            .get();

    if (recipeTool.getSelector() != null && !recipeTool.getSelector().isEmpty()) {
      List<HasMetadata> itemsList = list.getItems();
      itemsList.removeIf(
          e ->
              !e.getMetadata()
                  .getLabels()
                  .entrySet()
                  .containsAll(recipeTool.getSelector().entrySet()));
      list.setItems(itemsList);
    }
    String listValue = null;
    try {
      listValue = objectMapper.writeValueAsString(list);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    RecipeImpl recipe = new RecipeImpl(type, DEFAULT_RECIPE_CONTENT_TYPE, listValue, null);
    return Optional.of(new Pair<>(recipeTool.getName(), new EnvironmentImpl(recipe, emptyMap())));
  }
}
