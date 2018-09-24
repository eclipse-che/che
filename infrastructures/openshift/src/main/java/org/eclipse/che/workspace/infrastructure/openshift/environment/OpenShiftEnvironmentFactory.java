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
package org.eclipse.che.workspace.infrastructure.openshift.environment;

import static java.lang.String.format;

import com.google.common.annotations.VisibleForTesting;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.Route;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Warning;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.*;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironmentValidator;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

/**
 * Parses {@link InternalEnvironment} into {@link OpenShiftEnvironment}.
 *
 * @author Sergii Leshchenko
 */
public class OpenShiftEnvironmentFactory extends InternalEnvironmentFactory<OpenShiftEnvironment> {

  static final int ROUTE_IGNORED_WARNING_CODE = 4100;
  static final String ROUTES_IGNORED_WARNING_MESSAGE =
      "Routes specified in OpenShift recipe are ignored. "
          + "To expose ports please define servers in machine configuration.";

  static final int PVC_IGNORED_WARNING_CODE = 4101;
  static final String PVC_IGNORED_WARNING_MESSAGE =
      "Persistent volume claims specified in OpenShift recipe are ignored.";

  static final int SECRET_IGNORED_WARNING_CODE = 4102;
  static final String SECRET_IGNORED_WARNING_MESSAGE =
      "Secrets specified in OpenShift recipe are ignored.";

  static final int CONFIG_MAP_IGNORED_WARNING_CODE = 4103;
  static final String CONFIG_MAP_IGNORED_WARNING_MESSAGE =
      "Config maps specified in Kubernetes recipe are ignored.";

  private final OpenShiftClientFactory clientFactory;
  private final KubernetesEnvironmentValidator envValidator;
  private final MemoryAttributeProvisioner memoryProvisioner;

  @Inject
  public OpenShiftEnvironmentFactory(
      InstallerRegistry installerRegistry,
      RecipeRetriever recipeRetriever,
      MachineConfigsValidator machinesValidator,
      OpenShiftClientFactory clientFactory,
      KubernetesEnvironmentValidator envValidator,
      MemoryAttributeProvisioner memoryProvisioner) {
    super(installerRegistry, recipeRetriever, machinesValidator);
    this.clientFactory = clientFactory;
    this.envValidator = envValidator;
    this.memoryProvisioner = memoryProvisioner;
  }

  @Override
  protected OpenShiftEnvironment doCreate(
      InternalRecipe recipe,
      Map<String, InternalMachineConfig> machines,
      List<Warning> sourceWarnings)
      throws InfrastructureException, ValidationException {
    List<Warning> warnings = new ArrayList<>();
    if (sourceWarnings != null) {
      warnings.addAll(sourceWarnings);
    }
    String content = recipe.getContent();
    String contentType = recipe.getContentType();
    checkNotNull(contentType, "OpenShift Recipe content type should not be null");

    switch (contentType) {
      case "application/x-yaml":
      case "text/yaml":
      case "text/x-yaml":
        break;
      default:
        throw new ValidationException(
            "Provided environment recipe content type '"
                + contentType
                + "' is unsupported. Supported values are: "
                + "application/x-yaml, text/yaml, text/x-yaml");
    }

    final KubernetesList list =
        clientFactory.create().lists().load(new ByteArrayInputStream(content.getBytes())).get();

    Map<String, Pod> pods = new HashMap<>();
    Map<String, Service> services = new HashMap<>();
    boolean isAnyRoutePresent = false;
    boolean isAnyPVCPresent = false;
    boolean isAnySecretPresent = false;
    boolean isAnyConfigMapPresent = false;
    for (HasMetadata object : list.getItems()) {
      if (object instanceof DeploymentConfig) {
        throw new ValidationException("Supporting of deployment configs is not implemented yet.");
      } else if (object instanceof Pod) {
        Pod pod = (Pod) object;
        pods.put(pod.getMetadata().getName(), pod);
      } else if (object instanceof Service) {
        Service service = (Service) object;
        services.put(service.getMetadata().getName(), service);
      } else if (object instanceof Route) {
        isAnyRoutePresent = true;
      } else if (object instanceof PersistentVolumeClaim) {
        isAnyPVCPresent = true;
      } else if (object instanceof Secret) {
        isAnySecretPresent = true;
      } else if (object instanceof ConfigMap) {
        isAnyConfigMapPresent = true;
      } else {
        throw new ValidationException(
            format("Found unknown object type '%s'", object.getMetadata()));
      }
    }

    if (isAnyRoutePresent) {
      warnings.add(new WarningImpl(ROUTE_IGNORED_WARNING_CODE, ROUTES_IGNORED_WARNING_MESSAGE));
    }

    if (isAnyPVCPresent) {
      warnings.add(new WarningImpl(PVC_IGNORED_WARNING_CODE, PVC_IGNORED_WARNING_MESSAGE));
    }

    if (isAnySecretPresent) {
      warnings.add(new WarningImpl(SECRET_IGNORED_WARNING_CODE, SECRET_IGNORED_WARNING_MESSAGE));
    }

    if (isAnyConfigMapPresent) {
      warnings.add(
          new WarningImpl(CONFIG_MAP_IGNORED_WARNING_CODE, CONFIG_MAP_IGNORED_WARNING_MESSAGE));
    }

    addRamAttributes(machines, pods.values());

    OpenShiftEnvironment osEnv =
        OpenShiftEnvironment.builder()
            .setInternalRecipe(recipe)
            .setMachines(machines)
            .setWarnings(warnings)
            .setPods(pods)
            .setServices(services)
            .setPersistentVolumeClaims(new HashMap<>())
            .setSecrets(new HashMap<>())
            .setConfigMaps(new HashMap<>())
            .setRoutes(new HashMap<>())
            .build();

    envValidator.validate(osEnv);

    return osEnv;
  }

  @VisibleForTesting
  void addRamAttributes(Map<String, InternalMachineConfig> machines, Collection<Pod> pods) {
    for (Pod pod : pods) {
      for (Container container : pod.getSpec().getContainers()) {
        final String machineName = Names.machineName(pod, container);
        InternalMachineConfig machineConfig;
        if ((machineConfig = machines.get(machineName)) == null) {
          machineConfig = new InternalMachineConfig();
          machines.put(machineName, machineConfig);
        }
        memoryProvisioner.provision(
            machineConfig, Containers.getRamLimit(container), Containers.getRamRequest(container));
      }
    }
  }

  private void checkNotNull(Object object, String errorMessage) throws ValidationException {
    if (object == null) {
      throw new ValidationException(errorMessage);
    }
  }
}
