/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.environment.PodMerger.DEPLOYMENT_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.setSelector;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.Route;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironment;
import org.eclipse.che.api.workspace.server.spi.environment.InternalEnvironmentFactory;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.api.workspace.server.spi.environment.InternalRecipe;
import org.eclipse.che.api.workspace.server.spi.environment.MachineConfigsValidator;
import org.eclipse.che.api.workspace.server.spi.environment.RecipeRetriever;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.PodMerger;

/**
 * Parses {@link InternalEnvironment} into {@link OpenShiftEnvironment}.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftEnvironmentFactory extends InternalEnvironmentFactory<OpenShiftEnvironment> {

  private final OpenShiftEnvironmentValidator envValidator;
  private final KubernetesRecipeParser k8sObjectsParser;
  private final PodMerger podMerger;

  @Inject
  public OpenShiftEnvironmentFactory(
      RecipeRetriever recipeRetriever,
      MachineConfigsValidator machinesValidator,
      OpenShiftEnvironmentValidator envValidator,
      KubernetesRecipeParser k8sObjectsParser,
      PodMerger podMerger) {
    super(recipeRetriever, machinesValidator);
    this.envValidator = envValidator;
    this.k8sObjectsParser = k8sObjectsParser;
    this.podMerger = podMerger;
  }

  @Override
  protected OpenShiftEnvironment doCreate(
      @Nullable InternalRecipe recipe,
      Map<String, InternalMachineConfig> machines,
      List<Warning> sourceWarnings)
      throws InfrastructureException, ValidationException {
    checkNotNull(recipe, "Null recipe is not supported by openshift environment factory");
    List<Warning> warnings = new ArrayList<>();
    if (sourceWarnings != null) {
      warnings.addAll(sourceWarnings);
    }

    final List<HasMetadata> list = k8sObjectsParser.parse(recipe);

    Map<String, Pod> pods = new HashMap<>();
    Map<String, Deployment> deployments = new HashMap<>();
    Map<String, Service> services = new HashMap<>();
    Map<String, ConfigMap> configMaps = new HashMap<>();
    Map<String, PersistentVolumeClaim> pvcs = new HashMap<>();
    Map<String, Route> routes = new HashMap<>();
    Map<String, Secret> secrets = new HashMap<>();
    for (HasMetadata object : list) {
      checkNotNull(object.getKind(), "Environment contains object without specified kind field");
      checkNotNull(object.getMetadata(), "%s metadata must not be null", object.getKind());
      checkNotNull(object.getMetadata().getName(), "%s name must not be null", object.getKind());

      if (object instanceof DeploymentConfig) {
        throw new ValidationException("Supporting of deployment configs is not implemented yet.");
      } else if (object instanceof Pod) {
        putInto(pods, object.getMetadata().getName(), (Pod) object);
      } else if (object instanceof Deployment) {
        putInto(deployments, object.getMetadata().getName(), (Deployment) object);
      } else if (object instanceof Service) {
        putInto(services, object.getMetadata().getName(), (Service) object);
      } else if (object instanceof Route) {
        putInto(routes, object.getMetadata().getName(), (Route) object);
      } else if (object instanceof PersistentVolumeClaim) {
        putInto(pvcs, object.getMetadata().getName(), (PersistentVolumeClaim) object);
      } else if (object instanceof Secret) {
        putInto(secrets, object.getMetadata().getName(), (Secret) object);
      } else if (object instanceof ConfigMap) {
        putInto(configMaps, object.getMetadata().getName(), (ConfigMap) object);
      } else {
        throw new ValidationException(
            format(
                "Found unknown object type in recipe -- name: '%s', kind: '%s'",
                object.getMetadata().getName(), object.getKind()));
      }
    }

    if (deployments.size() + pods.size() > 1) {
      mergePods(pods, deployments, services);
    }

    OpenShiftEnvironment osEnv =
        OpenShiftEnvironment.builder()
            .setInternalRecipe(recipe)
            .setMachines(machines)
            .setWarnings(warnings)
            .setPods(pods)
            .setDeployments(deployments)
            .setServices(services)
            .setPersistentVolumeClaims(pvcs)
            .setSecrets(secrets)
            .setConfigMaps(configMaps)
            .setRoutes(routes)
            .build();

    envValidator.validate(osEnv);

    return osEnv;
  }

  /**
   * Merges the specified pods and deployments to a single Deployment.
   *
   * <p>Note that method will modify the specified collections and put work result there.
   *
   * @param pods pods to merge
   * @param deployments deployments to merge
   * @param services services to reconfigure to point new deployment
   * @throws ValidationException if the specified lists has pods with conflicting configuration
   */
  private void mergePods(
      Map<String, Pod> pods, Map<String, Deployment> deployments, Map<String, Service> services)
      throws ValidationException {
    List<PodData> podsData =
        Stream.concat(
                pods.values().stream().map(PodData::new),
                deployments.values().stream().map(PodData::new))
            .collect(Collectors.toList());

    Deployment deployment = podMerger.merge(podsData);
    String deploymentName = deployment.getMetadata().getName();

    // provision merged deployment instead of recipe pods/deployments
    pods.clear();
    deployments.clear();
    deployments.put(deploymentName, deployment);

    // multiple pods/deployments are merged to one deployment
    // to avoid issues because of overriding labels
    // provision const label and selector to match all services to merged Deployment
    putLabel(
        deployment.getSpec().getTemplate().getMetadata(), DEPLOYMENT_NAME_LABEL, deploymentName);
    services.values().forEach(s -> setSelector(s, DEPLOYMENT_NAME_LABEL, deploymentName));
  }

  /**
   * Puts the specified key/value pair into the specified map or throw an exception if map already
   * contains such key.
   *
   * @param map the map to put key/value pair
   * @param key key that should be put
   * @param value value that should be put
   * @param <T> type of object to put
   * @throws ValidationException if the specified map already contains the specified key
   */
  private <T extends HasMetadata> void putInto(Map<String, T> map, String key, T value)
      throws ValidationException {
    if (map.put(key, value) != null) {
      String kind = value.getKind();
      String name = value.getMetadata().getName();
      throw new ValidationException(
          format(
              "Environment can not contain two '%s' objects with the same name '%s'", kind, name));
    }
  }

  private void checkNotNull(Object object, String errorMessage) throws ValidationException {
    if (object == null) {
      throw new ValidationException(errorMessage);
    }
  }

  private void checkNotNull(Object object, String messageFmt, Object... messageArguments)
      throws ValidationException {
    if (object == null) {
      throw new ValidationException(format(messageFmt, messageArguments));
    }
  }
}
