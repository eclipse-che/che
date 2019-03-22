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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_COMPONENT_TYPE;

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.utils.Serialization;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.devfile.model.Component;
import org.eclipse.che.api.devfile.model.Entrypoint;
import org.eclipse.che.api.devfile.server.Constants;
import org.eclipse.che.api.devfile.server.DevfileRecipeFormatException;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.convert.component.ComponentToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

/**
 * Applies changes on workspace config according to the specified kubernetes/openshift component.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesComponentToWorkspaceApplier implements ComponentToWorkspaceApplier {

  @VisibleForTesting static final String YAML_CONTENT_TYPE = "application/x-yaml";

  private final KubernetesRecipeParser objectsParser;

  @Inject
  public KubernetesComponentToWorkspaceApplier(KubernetesRecipeParser objectsParser) {
    this.objectsParser = objectsParser;
  }

  /**
   * Applies changes on workspace config according to the specified kubernetes/openshift component.
   *
   * @param workspaceConfig workspace config on which changes should be applied
   * @param k8sComponent kubernetes/openshift component that should be applied
   * @param contentProvider content provider that may be used for external component resource
   *     fetching
   * @throws IllegalArgumentException if specified workspace config or plugin component is null
   * @throws IllegalArgumentException if specified component has type different from chePlugin
   * @throws DevfileException if specified content provider is null while kubernetes/openshift
   *     component required external file content
   * @throws DevfileException if external file content is empty or any error occurred during content
   *     retrieving
   */
  @Override
  public void apply(
      WorkspaceConfigImpl workspaceConfig,
      Component k8sComponent,
      FileContentProvider contentProvider)
      throws DevfileException {
    checkArgument(workspaceConfig != null, "Workspace config must not be null");
    checkArgument(k8sComponent != null, "Component must not be null");
    checkArgument(
        KUBERNETES_COMPONENT_TYPE.equals(k8sComponent.getType())
            || OPENSHIFT_COMPONENT_TYPE.equals(k8sComponent.getType()),
        format(
            "Plugin must have `%s` or `%s` type",
            KUBERNETES_COMPONENT_TYPE, OPENSHIFT_COMPONENT_TYPE));

    String componentContent = retrieveContent(k8sComponent, contentProvider);

    List<HasMetadata> componentObjects = new ArrayList<>();
    fillIn(componentObjects, unmarshalComponentObjects(k8sComponent, componentContent));

    if (!k8sComponent.getSelector().isEmpty()) {
      componentObjects = SelectorFilter.filter(componentObjects, k8sComponent.getSelector());
    }

    estimateCommandsMachineName(workspaceConfig, k8sComponent, componentObjects);

    applyEntrypoints(k8sComponent.getEntrypoints(), componentObjects);

    provisionEnvironment(workspaceConfig, k8sComponent, componentObjects);
  }

  /**
   * Provision default environment with the specified kubernetes/openshift component.
   *
   * <p>If there is already a default environment with kubernetes/openshift then content will be
   * updated with new list.
   *
   * @param workspaceConfig workspace where recipe should be provisioned
   * @param k8sComponent kubernetes/openshift component that should be provisioned
   * @param componentObjects parsed objects of the specified component
   * @throws DevfileRecipeFormatException if exception occurred during existing environment parsing
   * @throws DevfileRecipeFormatException if exception occurred during kubernetes object
   *     serialization
   * @throws DevfileException if any other exception occurred
   */
  private void provisionEnvironment(
      WorkspaceConfigImpl workspaceConfig,
      Component k8sComponent,
      List<HasMetadata> componentObjects)
      throws DevfileException, DevfileRecipeFormatException {
    String defaultEnv = workspaceConfig.getDefaultEnv();
    EnvironmentImpl environment = workspaceConfig.getEnvironments().get(defaultEnv);
    if (environment == null) {
      RecipeImpl recipe =
          new RecipeImpl(
              k8sComponent.getType(),
              YAML_CONTENT_TYPE,
              asYaml(k8sComponent, componentObjects),
              null);
      String envName = k8sComponent.getName();
      workspaceConfig.getEnvironments().put(envName, new EnvironmentImpl(recipe, emptyMap()));
      workspaceConfig.setDefaultEnv(envName);
    } else {
      RecipeImpl envRecipe = environment.getRecipe();

      // check if it is needed to update recipe type since
      // kubernetes component is compatible with openshift but not vice versa
      if (OPENSHIFT_COMPONENT_TYPE.equals(k8sComponent.getType())
          && KubernetesEnvironment.TYPE.equals(envRecipe.getType())) {
        envRecipe.setType(OpenShiftEnvironment.TYPE);
      }

      // workspace already has k8s/OS recipe
      // it is needed to merge existing recipe objects with component's ones
      List<HasMetadata> envObjects = unmarshalDefaultEnvObjects(workspaceConfig);
      fillIn(envObjects, componentObjects);

      envRecipe.setContent(asYaml(k8sComponent, envObjects));
    }
  }

  /**
   * Fill in the specified target list with the specified objects.
   *
   * @param target list that should be filled in
   * @param objects objects to fill in
   * @throws DevfileFormatException if objects list contains item with no unique combination of kind
   *     and name
   */
  private void fillIn(List<HasMetadata> target, List<HasMetadata> objects)
      throws DevfileFormatException {
    Set<Pair<String, String>> uniqueKindToName = new HashSet<>();
    for (HasMetadata existingMeta : target) {
      uniqueKindToName.add(
          new Pair<>(existingMeta.getKind(), existingMeta.getMetadata().getName()));
    }

    for (HasMetadata hasMeta : objects) {
      if (!uniqueKindToName.add(new Pair<>(hasMeta.getKind(), hasMeta.getMetadata().getName()))) {
        throw new DevfileFormatException(
            format(
                "Components can not have objects with the same name and kind but there are multiple objects with kind '%s' and name '%s'",
                hasMeta.getKind(), hasMeta.getMetadata().getName()));
      }
      target.add(hasMeta);
    }
  }

  private List<HasMetadata> unmarshalDefaultEnvObjects(WorkspaceConfigImpl workspaceConfig)
      throws DevfileException {
    String defaultEnvName = workspaceConfig.getDefaultEnv();
    if (defaultEnvName == null) {
      return new ArrayList<>();
    }
    EnvironmentImpl defaultEnv = workspaceConfig.getEnvironments().get(defaultEnvName);
    if (defaultEnv == null) {
      return new ArrayList<>();
    }
    RecipeImpl envRecipe = defaultEnv.getRecipe();
    if (!OpenShiftEnvironment.TYPE.equals(envRecipe.getType())
        && !KubernetesEnvironment.TYPE.equals(envRecipe.getType())) {
      throw new DevfileException(
          format(
              "Kubernetes component can only be applied to a workspace with either kubernetes or "
                  + "openshift recipe type but workspace has a recipe of type '%s'",
              envRecipe.getType()));
    }

    return unmarshal(envRecipe.getContent());
  }

  private String retrieveContent(
      Component recipeComponent, @Nullable FileContentProvider fileContentProvider)
      throws DevfileException {
    checkArgument(fileContentProvider != null, "Content provider must not be null");
    if (!isNullOrEmpty(recipeComponent.getLocalContent())) {
      return recipeComponent.getLocalContent();
    }

    String recipeFileContent;
    try {
      recipeFileContent = fileContentProvider.fetchContent(recipeComponent.getLocal());
    } catch (DevfileException e) {
      throw new DevfileException(
          format(
              "Fetching content of file `%s` specified in `local` field of component `%s` is not supported. "
                  + "Please provide its content in `localContent` field. Cause: %s",
              recipeComponent.getLocal(), recipeComponent.getName(), e.getMessage()),
          e);
    } catch (IOException e) {
      throw new DevfileException(
          format(
              "Error during recipe content retrieval for component '%s' with type '%s': %s",
              recipeComponent.getName(), recipeComponent.getType(), e.getMessage()),
          e);
    }
    if (isNullOrEmpty(recipeFileContent)) {
      throw new DevfileException(
          format(
              "The local file '%s' defined in component '%s' is empty.",
              recipeComponent.getLocal(), recipeComponent.getName()));
    }
    return recipeFileContent;
  }

  /**
   * Set {@link Command#MACHINE_NAME_ATTRIBUTE} to commands which are configured in the specified
   * component.
   *
   * <p>Machine name will be set only if the specified recipe objects has the only one container.
   */
  private void estimateCommandsMachineName(
      WorkspaceConfig workspaceConfig, Component component, List<HasMetadata> componentsObjects) {
    List<? extends Command> componentCommands =
        workspaceConfig
            .getCommands()
            .stream()
            .filter(
                c ->
                    component
                        .getName()
                        .equals(c.getAttributes().get(Constants.COMPONENT_NAME_COMMAND_ATTRIBUTE)))
            .collect(toList());
    if (componentCommands.isEmpty()) {
      return;
    }

    List<PodData> podsData = new ArrayList<>();

    componentsObjects
        .stream()
        .filter(hasMetadata -> hasMetadata instanceof Pod)
        .map(hasMetadata -> (Pod) hasMetadata)
        .forEach(p -> podsData.add(new PodData(p)));

    componentsObjects
        .stream()
        .filter(hasMetadata -> hasMetadata instanceof Deployment)
        .map(hasMetadata -> (Deployment) hasMetadata)
        .forEach(d -> podsData.add(new PodData(d)));

    if (podsData.size() != 1) {
      // many or no pods - can't estimate the name because of ambiguity or lack of information
      return;
    }

    PodData pod = podsData.get(0);

    if (pod.getSpec().getContainers().size() != 1) {
      // many or no containers - can't estimate the name
      return;
    }

    String machineName = Names.machineName(pod, pod.getSpec().getContainers().get(0));
    componentCommands.forEach(c -> c.getAttributes().put(MACHINE_NAME_ATTRIBUTE, machineName));
  }

  private List<HasMetadata> unmarshalComponentObjects(
      Component k8sComponent, String toolLocalContent) throws DevfileRecipeFormatException {
    try {
      return unmarshal(toolLocalContent);
    } catch (DevfileRecipeFormatException e) {
      throw new DevfileRecipeFormatException(
          format(
              "Error occurred during parsing list from file %s for component '%s': %s",
              k8sComponent.getLocal(), k8sComponent.getName(), e.getMessage()),
          e);
    }
  }

  private List<HasMetadata> unmarshal(String recipeContent) throws DevfileRecipeFormatException {
    try {
      return objectsParser.parse(recipeContent);
    } catch (Exception e) {
      throw new DevfileRecipeFormatException(e.getMessage(), e);
    }
  }

  private String asYaml(Component tool, List<HasMetadata> list)
      throws DevfileRecipeFormatException {
    try {
      return Serialization.asYaml(new KubernetesListBuilder().withItems(list).build());
    } catch (KubernetesClientException e) {
      throw new DevfileRecipeFormatException(
          format(
              "Unable to deserialize specified local file content for component '%s'. Error: %s",
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
            entrypoint.getParentName(),
            entrypoint.getParentSelector(),
            entrypoint.getContainerName());

    List<Container> cs = search.search(list);

    List<String> command = entrypoint.getCommand();
    List<String> args = entrypoint.getArgs();

    for (Container c : cs) {
      c.setCommand(command);
      c.setArgs(args);
    }
  }
}
