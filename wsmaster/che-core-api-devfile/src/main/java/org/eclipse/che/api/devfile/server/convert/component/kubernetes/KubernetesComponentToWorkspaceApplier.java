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
import static org.eclipse.che.api.devfile.server.Components.getIdentifiableComponentName;
import static org.eclipse.che.api.devfile.server.Constants.KUBERNETES_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_COMPONENT_TYPE;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.core.model.workspace.devfile.Entrypoint;
import org.eclipse.che.api.devfile.server.Constants;
import org.eclipse.che.api.devfile.server.DevfileRecipeFormatException;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.convert.component.ComponentToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;

/**
 * Applies changes on workspace config according to the specified kubernetes/openshift component.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesComponentToWorkspaceApplier implements ComponentToWorkspaceApplier {

  private final KubernetesRecipeParser objectsParser;
  private final KubernetesEnvironmentProvisioner k8sEnvProvisioner;

  @Inject
  public KubernetesComponentToWorkspaceApplier(
      KubernetesRecipeParser objectsParser, KubernetesEnvironmentProvisioner k8sEnvProvisioner) {
    this.objectsParser = objectsParser;
    this.k8sEnvProvisioner = k8sEnvProvisioner;
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

    List<HasMetadata> componentObjects =
        new ArrayList<>(unmarshalComponentObjects(k8sComponent, componentContent));

    if (!k8sComponent.getSelector().isEmpty()) {
      componentObjects = SelectorFilter.filter(componentObjects, k8sComponent.getSelector());
    }

    estimateCommandsMachineName(workspaceConfig, k8sComponent, componentObjects);

    applyEntrypoints(k8sComponent.getEntrypoints(), componentObjects);

    k8sEnvProvisioner.provision(
        workspaceConfig, k8sComponent.getType(), componentObjects, emptyMap());
  }

  private String retrieveContent(
      Component recipeComponent, @Nullable FileContentProvider fileContentProvider)
      throws DevfileException {
    checkArgument(fileContentProvider != null, "Content provider must not be null");
    if (!isNullOrEmpty(recipeComponent.getReferenceContent())) {
      return recipeComponent.getReferenceContent();
    }

    String recipeFileContent;
    try {
      recipeFileContent = fileContentProvider.fetchContent(recipeComponent.getReference());
    } catch (DevfileException e) {
      throw new DevfileException(
          format(
              "Fetching content of file `%s` specified in `reference` field of component `%s` is not supported. "
                  + "Please provide its content in `referenceContent` field. Cause: %s",
              recipeComponent.getReference(),
              getIdentifiableComponentName(recipeComponent),
              e.getMessage()),
          e);
    } catch (IOException e) {
      throw new DevfileException(
          format(
              "Error during recipe content retrieval for component '%s' with type '%s': %s",
              getIdentifiableComponentName(recipeComponent),
              recipeComponent.getType(),
              e.getMessage()),
          e);
    }
    if (isNullOrEmpty(recipeFileContent)) {
      throw new DevfileException(
          format(
              "The reference file '%s' defined in component '%s' is empty.",
              recipeComponent.getReference(), getIdentifiableComponentName(recipeComponent)));
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
                    c.getAttributes()
                        .get(Constants.COMPONENT_ALIAS_COMMAND_ATTRIBUTE)
                        .equals(component.getAlias()))
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
      Component k8sComponent, String componentreferenceContent)
      throws DevfileRecipeFormatException {
    try {
      return unmarshal(componentreferenceContent);
    } catch (DevfileRecipeFormatException e) {
      throw new DevfileRecipeFormatException(
          format(
              "Error occurred during parsing list from file %s for component '%s': %s",
              k8sComponent.getReference(),
              getIdentifiableComponentName(k8sComponent),
              e.getMessage()),
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

  private void applyEntrypoints(List<? extends Entrypoint> entrypoints, List<HasMetadata> list) {
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
