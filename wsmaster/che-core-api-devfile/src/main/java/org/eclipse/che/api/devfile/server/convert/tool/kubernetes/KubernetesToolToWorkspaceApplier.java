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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_TOOL_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_TOOL_TYPE;

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.utils.Serialization;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.devfile.model.Entrypoint;
import org.eclipse.che.api.devfile.model.Tool;
import org.eclipse.che.api.devfile.server.Constants;
import org.eclipse.che.api.devfile.server.DevfileRecipeFormatException;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.convert.tool.ToolToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;

/**
 * Applies changes on workspace config according to the specified kubernetes/openshift tool.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesToolToWorkspaceApplier implements ToolToWorkspaceApplier {

  @VisibleForTesting static final String YAML_CONTENT_TYPE = "application/x-yaml";

  private final KubernetesRecipeParser objectsParser;

  @Inject
  public KubernetesToolToWorkspaceApplier(KubernetesRecipeParser objectsParser) {
    this.objectsParser = objectsParser;
  }

  /**
   * Applies changes on workspace config according to the specified kubernetes/openshift tool.
   *
   * @param workspaceConfig workspace config on which changes should be applied
   * @param k8sTool kubernetes/openshift tool that should be applied
   * @param contentProvider content provider that may be used for external tool resource fetching
   * @throws IllegalArgumentException if specified workspace config or plugin tool is null
   * @throws IllegalArgumentException if specified tool has type different from chePlugin
   * @throws DevfileException if specified content provider is null while kubernetes/openshift tool
   *     required external file content
   * @throws DevfileException if external file content is empty or any error occurred during content
   *     retrieving
   */
  @Override
  public void apply(
      WorkspaceConfigImpl workspaceConfig, Tool k8sTool, FileContentProvider contentProvider)
      throws DevfileException {
    checkArgument(workspaceConfig != null, "Workspace config must not be null");
    checkArgument(k8sTool != null, "Tool must not be null");
    checkArgument(
        KUBERNETES_TOOL_TYPE.equals(k8sTool.getType())
            || OPENSHIFT_TOOL_TYPE.equals(k8sTool.getType()),
        format("Plugin must have `%s` or `%s` type", KUBERNETES_TOOL_TYPE, OPENSHIFT_TOOL_TYPE));

    String recipeFileContent = retrieveContent(k8sTool, contentProvider);

    List<HasMetadata> list = unmarshal(k8sTool, recipeFileContent);

    if (!k8sTool.getSelector().isEmpty()) {
      list = SelectorFilter.filter(list, k8sTool.getSelector());
    }

    estimateCommandsMachineName(workspaceConfig, k8sTool, list);

    applyEntrypoints(k8sTool.getEntrypoints(), list);

    RecipeImpl recipe =
        new RecipeImpl(k8sTool.getType(), YAML_CONTENT_TYPE, asYaml(k8sTool, list), null);

    String envName = k8sTool.getName();
    workspaceConfig.getEnvironments().put(envName, new EnvironmentImpl(recipe, emptyMap()));
    workspaceConfig.setDefaultEnv(envName);
  }

  private String retrieveContent(Tool recipeTool, @Nullable FileContentProvider fileContentProvider)
      throws DevfileException {
    checkArgument(fileContentProvider != null, "Content provider must not be null");
    if (!isNullOrEmpty(recipeTool.getLocalContent())) {
      return recipeTool.getLocalContent();
    }

    String recipeFileContent;
    try {
      recipeFileContent = fileContentProvider.fetchContent(recipeTool.getLocal());
    } catch (DevfileException e) {
      throw new DevfileException(
          format(
              "Fetching content of file `%s` specified in `local` field of tool `%s` is not supported. "
                  + "Please provide its content in `localContent` field. Cause: %s",
              recipeTool.getLocal(), recipeTool.getName(), e.getMessage()),
          e);
    } catch (IOException e) {
      throw new DevfileException(
          format(
              "Error during recipe content retrieval for tool '%s' with type '%s': %s",
              recipeTool.getName(), recipeTool.getType(), e.getMessage()),
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

  /**
   * Set {@link Command#MACHINE_NAME_ATTRIBUTE} to commands which are configured in the specified
   * tool.
   *
   * <p>Machine name will be set only if the specified recipe objects has the only one container.
   */
  private void estimateCommandsMachineName(
      WorkspaceConfig workspaceConfig, Tool tool, List<HasMetadata> recipeObjects) {
    List<? extends Command> toolCommands =
        workspaceConfig
            .getCommands()
            .stream()
            .filter(
                c ->
                    tool.getName()
                        .equals(c.getAttributes().get(Constants.TOOL_NAME_COMMAND_ATTRIBUTE)))
            .collect(toList());
    if (toolCommands.isEmpty()) {
      return;
    }
    List<Pod> pods =
        recipeObjects
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
    toolCommands.forEach(c -> c.getAttributes().put(MACHINE_NAME_ATTRIBUTE, machineName));
  }

  private List<HasMetadata> unmarshal(Tool tool, String recipeContent)
      throws DevfileRecipeFormatException {
    try {
      return objectsParser.parse(recipeContent);
    } catch (Exception e) {
      throw new DevfileRecipeFormatException(
          format(
              "Error occurred during parsing list from file %s for tool '%s': %s",
              tool.getLocal(), tool.getName(), e.getMessage()));
    }
  }

  private String asYaml(Tool tool, List<HasMetadata> list) throws DevfileRecipeFormatException {
    try {
      return Serialization.asYaml(new KubernetesListBuilder().withItems(list).build());
    } catch (KubernetesClientException e) {
      throw new DevfileRecipeFormatException(
          format(
              "Unable to deserialize specified local file content for tool '%s'. Error: %s",
              tool.getName(), e.getMessage()),
          e);
    }
  }

  private void applyEntrypoints(List<Entrypoint> entrypoints, List<HasMetadata> list) {
    entrypoints.forEach(ep -> applyEntrypoint(ep, list));
  }

  private void applyEntrypoint(Entrypoint entrypoint, List<HasMetadata> list) {
    ContainerSearch search =
        new ContainerSearch(
            entrypoint.getDeployment(),
            entrypoint.getPod(),
            entrypoint.getContainer(),
            entrypoint.getPodSelector(),
            entrypoint.getDeploymentSelector());

    List<Container> cs = search.search(list);

    List<String> command = entrypoint.getCommand();
    List<String> args = entrypoint.getArgs();

    for (Container c : cs) {
      c.setCommand(command);
      c.setArgs(args);
    }
  }
}
