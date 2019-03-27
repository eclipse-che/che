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

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.api.devfile.server.Constants.OPENSHIFT_TOOL_TYPE;

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.utils.Serialization;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.inject.Inject;
import org.eclipse.che.api.devfile.server.DevfileRecipeFormatException;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.RecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

/**
 * Provisions default K8s/OS environment with specified objects (K8s/OS objects, machines) into
 * {@link WorkspaceConfigImpl}.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesEnvironmentProvisioner {
  @VisibleForTesting static final String YAML_CONTENT_TYPE = "application/x-yaml";

  private final KubernetesRecipeParser objectsParser;

  @Inject
  public KubernetesEnvironmentProvisioner(KubernetesRecipeParser objectsParser) {
    this.objectsParser = objectsParser;
  }

  /**
   * Provisions default K8s/OS environment with specified objects (K8s/OS objects, machines) into
   * {@link WorkspaceConfigImpl}.
   *
   * <p>If there is already a default environment with kubernetes/openshift recipe then content will
   * be updated with result or merging existing objects and specified ones.
   *
   * @param workspaceConfig workspace where recipe should be provisioned
   * @param environmentType type of environment that should be provisioned. Should be {@link
   *     KubernetesEnvironment#TYPE} or {@link OpenShiftEnvironment#TYPE}
   * @param toolObjects objects that should be provisioned into the workspace config
   * @param machines machines that should be provisioned into the workspace config
   * @throws DevfileRecipeFormatException if exception occurred during existing environment parsing
   * @throws DevfileRecipeFormatException if exception occurred during kubernetes object
   *     serialization
   * @throws DevfileException if any other exception occurred
   */
  public void provision(
      WorkspaceConfigImpl workspaceConfig,
      String environmentType,
      List<HasMetadata> toolObjects,
      Map<String, MachineConfigImpl> machines)
      throws DevfileException, DevfileRecipeFormatException {
    String defaultEnv = workspaceConfig.getDefaultEnv();
    EnvironmentImpl environment = workspaceConfig.getEnvironments().get(defaultEnv);
    if (environment == null) {
      checkItemsHasUniqueKindToName(toolObjects);

      RecipeImpl recipe =
          new RecipeImpl(environmentType, YAML_CONTENT_TYPE, asYaml(toolObjects), null);
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
      // kubernetes tool is compatible with openshift but not vice versa
      if (OPENSHIFT_TOOL_TYPE.equals(environmentType)
          && KubernetesEnvironment.TYPE.equals(envRecipe.getType())) {
        envRecipe.setType(OpenShiftEnvironment.TYPE);
      }

      // workspace already has k8s/OS recipe
      // it is needed to merge existing recipe objects with tool's ones
      List<HasMetadata> envObjects = unmarshalObjects(envRecipe);
      envObjects.addAll(toolObjects);

      envRecipe.setContent(asYaml(envObjects));
    }
  }

  private List<HasMetadata> unmarshalObjects(RecipeImpl k8sRecipe) throws DevfileException {
    if (!OpenShiftEnvironment.TYPE.equals(k8sRecipe.getType())
        && !KubernetesEnvironment.TYPE.equals(k8sRecipe.getType())) {
      throw new DevfileException(
          format(
              "Kubernetes tool can only be applied to a workspace with either kubernetes or "
                  + "openshift recipe type but workspace has a recipe of type '%s'",
              k8sRecipe.getType()));
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
                "Tools can not have objects with the same name and kind but there are multiple objects with kind '%s' and name '%s'",
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
