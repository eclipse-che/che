/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.devfile.KubernetesDevfileBindings.ALLOWED_ENVIRONMENT_TYPE_UPGRADES_KEY_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.devfile.KubernetesDevfileBindings.KUBERNETES_BASED_ENVIRONMENTS_KEY_NAME;

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.utils.Serialization;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.devfile.DevfileRecipeFormatException;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;

/**
 * Provisions default K8s/OS environment with specified objects (K8s/OS objects, machines) into
 * {@link WorkspaceConfigImpl}.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesEnvironmentProvisioner {
  @VisibleForTesting static final String YAML_CONTENT_TYPE = "application/x-yaml";

  private final KubernetesRecipeParser objectsParser;
  private final Map<String, Set<String>> allowedEnvironmentTypeUpgrades;
  private final Set<String> k8sBasedEnvTypes;

  @Inject
  public KubernetesEnvironmentProvisioner(
      KubernetesRecipeParser objectsParser,
      @Named(ALLOWED_ENVIRONMENT_TYPE_UPGRADES_KEY_NAME)
          Map<String, Set<String>> allowedEnvironmentTypeUpgrades,
      @Named(KUBERNETES_BASED_ENVIRONMENTS_KEY_NAME) Set<String> k8sBasedEnvTypes) {
    this.objectsParser = objectsParser;
    this.allowedEnvironmentTypeUpgrades = allowedEnvironmentTypeUpgrades;
    this.k8sBasedEnvTypes = k8sBasedEnvTypes;
  }

  /**
   * Provisions default K8s/OS environment with specified objects (K8s/OS objects, machines) into
   * {@link WorkspaceConfigImpl}.
   *
   * <p>If there is already a default environment with kubernetes/openshift recipe then content will
   * be updated with result or merging existing objects and specified ones.
   *
   * @param workspaceConfig workspace where recipe should be provisioned
   * @param environmentType type of environment that should be provisioned. Should be one of the
   *     Kubernetes-based environments.
   * @param componentObjects objects that should be provisioned into the workspace config
   * @param machines machines that should be provisioned into the workspace config
   * @throws DevfileRecipeFormatException if exception occurred during existing environment parsing
   * @throws DevfileRecipeFormatException if exception occurred during kubernetes object
   *     serialization
   * @throws DevfileException if any other exception occurred
   */
  public void provision(
      WorkspaceConfigImpl workspaceConfig,
      String environmentType,
      List<HasMetadata> componentObjects,
      Map<String, MachineConfigImpl> machines)
      throws DevfileException, DevfileRecipeFormatException {
    String defaultEnv = workspaceConfig.getDefaultEnv();
    EnvironmentImpl environment = workspaceConfig.getEnvironments().get(defaultEnv);
    if (environment == null) {
      checkItemsHasUniqueKindToName(componentObjects);

      RecipeImpl recipe =
          new RecipeImpl(environmentType, YAML_CONTENT_TYPE, asYaml(componentObjects), null);
      String envName = "default";
      EnvironmentImpl env = new EnvironmentImpl(recipe, emptyMap());
      env.getMachines().putAll(machines);
      workspaceConfig.getEnvironments().put(envName, env);
      workspaceConfig.setDefaultEnv(envName);
    } else {
      RecipeImpl envRecipe = environment.getRecipe();

      for (Entry<String, MachineConfigImpl> machineEntry : machines.entrySet()) {
        if (environment.getMachines().put(machineEntry.getKey(), machineEntry.getValue()) != null) {
          throw new DevfileException(
              format("Environment already contains machine '%s'", machineEntry.getKey()));
        }
      }
      environment.getMachines().putAll(machines);

      // check if it is needed to update recipe type since
      // kubernetes component is compatible with openshift but not vice versa

      Set<String> allowedEnvTypeBases = allowedEnvironmentTypeUpgrades.get(environmentType);
      if (allowedEnvTypeBases != null) {
        envRecipe.setType(environmentType);
      }

      // workspace already has k8s/OS recipe
      // it is needed to merge existing recipe objects with component's ones
      List<HasMetadata> envObjects = unmarshalObjects(envRecipe);
      mergeProjectsPVC(envObjects, componentObjects);
      envObjects.addAll(componentObjects);
      checkItemsHasUniqueKindToName(envObjects);

      envRecipe.setContent(asYaml(envObjects));
    }
  }

  private void mergeProjectsPVC(List<HasMetadata> envObjects, List<HasMetadata> componentObjects) {
    componentObjects.removeIf(
        co ->
            co instanceof PersistentVolumeClaim
                && co.getMetadata().getName().equals(PROJECTS_VOLUME_NAME)
                && envObjects
                    .stream()
                    .filter(envObject -> envObject instanceof PersistentVolumeClaim)
                    .anyMatch(pvc -> pvc.equals(co)));
  }

  private List<HasMetadata> unmarshalObjects(RecipeImpl k8sRecipe) throws DevfileException {
    if (!k8sBasedEnvTypes.contains(k8sRecipe.getType())) {
      String allowedEnvTypes = String.join(" or ", k8sBasedEnvTypes);
      throw new DevfileException(
          format(
              "Kubernetes component can only be applied to a workspace with any of %s recipe type"
                  + " but workspace has a recipe of type '%s'",
              allowedEnvTypes, k8sRecipe.getType()));
    }

    return unmarshal(k8sRecipe.getContent());
  }

  /**
   * Makes sure that all items of the specified list have unique names per kind.
   *
   * @param list the list to check
   * @throws DevfileFormatException if objects list contains item with no unique combination of kind
   *     and name
   */
  private void checkItemsHasUniqueKindToName(List<HasMetadata> list) throws DevfileFormatException {
    Set<Pair<String, String>> uniqueKindToName = new HashSet<>();
    for (HasMetadata hasMeta : list) {
      if (!uniqueKindToName.add(new Pair<>(hasMeta.getKind(), hasMeta.getMetadata().getName()))) {
        throw new DevfileFormatException(
            format(
                "Components can not have objects with the same name and kind but there are multiple objects with kind '%s' and name '%s'",
                hasMeta.getKind(), hasMeta.getMetadata().getName()));
      }
    }
  }

  private String asYaml(List<HasMetadata> list) throws DevfileRecipeFormatException {
    try {
      return Serialization.asYaml(new KubernetesListBuilder().withItems(list).build());
    } catch (KubernetesClientException e) {
      throw new DevfileRecipeFormatException(
          format(
              "Unable to deserialize objects to store them in workspace config. Error: %s",
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
}
