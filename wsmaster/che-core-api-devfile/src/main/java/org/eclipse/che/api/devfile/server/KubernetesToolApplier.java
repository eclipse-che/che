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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_TOOL_TYPE;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.utils.Serialization;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.inject.Singleton;
import org.eclipse.che.api.devfile.model.Command;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;

/**
 * Applies Kubernetes tool configuration on provided {@link Devfile} and {@link
 * WorkspaceConfigImpl}.
 *
 * @author Max Shaposhnyk
 * @author Sergii Leshchenko
 */
@Singleton
public class KubernetesToolApplier {

  static final String YAML_CONTENT_TYPE = "application/x-yaml";

  /**
   * Applies Kubernetes tool configuration on provided {@link Devfile} and {@link
   * WorkspaceConfigImpl}.
   *
   * <p>It includes:
   *
   * <ul>
   *   <li>provisioning environment based on content of the file specified in {@link Tool#local};
   *   <li>provisioning machine name attribute to commands that are configured to be run in the
   *       specified tool. Note that name will be set only if tool contains the only one container;
   * </ul>
   *
   * <p>NOTE: An {@link RecipeFileContentProvider} MUST be provided in order to fetch recipe
   * content.
   *
   * @param recipeTool the recipe-type tool
   * @param devfile devfile that should be changed according to the provided tool
   * @param workspaceConfig workspace config that should be changed according to the provided tool
   * @param contentProvider service-specific provider of recipe file content
   * @throws IllegalArgumentException when wrong type tool is passed
   * @throws IllegalArgumentException when there is no content provider for recipe-type tool
   * @throws DevfileRecipeFormatException when recipe-type tool content is empty or has wrong format
   * @throws DevfileException when general devfile error occurs
   */
  public void apply(
      Tool recipeTool,
      Devfile devfile,
      WorkspaceConfigImpl workspaceConfig,
      RecipeFileContentProvider contentProvider)
      throws DevfileRecipeFormatException, DevfileException {
    checkArgument(recipeTool != null, "Tool must not be null");
    checkArgument(devfile != null, "Devfile must not be null");
    checkArgument(workspaceConfig != null, "Workspace config must not be null");

    final String type = recipeTool.getType();
    checkArgument(
        KUBERNETES_TOOL_TYPE.equals(type) || OPENSHIFT_TOOL_TYPE.equals(type),
        format(
            "Unable to create environment from tool '%s' - it has ineligible type '%s'.",
            recipeTool.getName(), type));

    String recipeFileContent = retrieveContent(recipeTool, contentProvider, type);

    final KubernetesList list = unmarshal(recipeTool, recipeFileContent);

    if (!recipeTool.getSelector().isEmpty()) {
      list.setItems(filter(list, recipeTool.getSelector()));
    }

    estimateCommandsMachineName(devfile, recipeTool, list);

    RecipeImpl recipe = new RecipeImpl(type, YAML_CONTENT_TYPE, asYaml(recipeTool, list), null);

    String envName = recipeTool.getName();
    workspaceConfig.getEnvironments().put(envName, new EnvironmentImpl(recipe, emptyMap()));
    workspaceConfig.setDefaultEnv(envName);
  }

  private String retrieveContent(
      Tool recipeTool, RecipeFileContentProvider recipeFileContentProvider, String type)
      throws DevfileException {
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
              "The local file '%s' defined in tool '%s' is empty.",
              recipeTool.getLocal(), recipeTool.getName()));
    }
    return recipeFileContent;
  }

  private List<HasMetadata> filter(KubernetesList list, Map<String, String> selector) {
    return list.getItems().stream().filter(item -> matchLabels(item, selector)).collect(toList());
  }

  /**
   * Returns true is specified {@link HasMetadata} instance is matched by specified selector, false
   * otherwise
   *
   * @param hasMetadata object to check matching
   * @param selector selector that should be matched with object's labels
   */
  private boolean matchLabels(HasMetadata hasMetadata, Map<String, String> selector) {
    ObjectMeta metadata = hasMetadata.getMetadata();
    if (metadata == null) {
      return false;
    }

    Map<String, String> labels = metadata.getLabels();
    if (labels == null) {
      return false;
    }

    return labels.entrySet().containsAll(selector.entrySet());
  }

  /**
   * Set {@link org.eclipse.che.api.core.model.workspace.config.Command#MACHINE_NAME_ATTRIBUTE} to
   * commands which are configured in the specified tool.
   *
   * <p>Machine name will be set only if the specified recipe objects has the only one container.
   */
  private void estimateCommandsMachineName(
      Devfile devfile, Tool tool, KubernetesList recipeObjects) {
    List<Command> toolsCommands =
        devfile
            .getCommands()
            .stream()
            .filter(c -> c.getActions().get(0).getTool().equals(tool.getName()))
            .collect(toList());
    if (toolsCommands.isEmpty()) {
      return;
    }
    List<Pod> pods =
        recipeObjects
            .getItems()
            .stream()
            .filter(hasMetadata -> hasMetadata instanceof Pod)
            .map(hasMetadata -> (Pod) hasMetadata)
            .collect(toList());

    Pod pod;
    if (pods.size() != 1 || (pod = pods.get(0)).getSpec().getContainers().isEmpty()) {
      // recipe contains several containers
      // can not estimate commands machine name
      return;
    }

    String machineName = Names.machineName(pod, pod.getSpec().getContainers().get(0));
    toolsCommands.forEach(c -> c.getAttributes().put(MACHINE_NAME_ATTRIBUTE, machineName));
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
