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
package org.eclipse.che.workspace.infrastructure.kubernetes.environment;

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
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.extensions.Ingress;
import io.fabric8.kubernetes.client.KubernetesClientException;
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
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.Warnings;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;

/**
 * Parses {@link InternalEnvironment} into {@link KubernetesEnvironment}.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesEnvironmentFactory
    extends InternalEnvironmentFactory<KubernetesEnvironment> {

  private final KubernetesClientFactory clientFactory;
  private final KubernetesEnvironmentValidator envValidator;
  private final MemoryAttributeProvisioner memoryProvisioner;

  @Inject
  public KubernetesEnvironmentFactory(
      InstallerRegistry installerRegistry,
      RecipeRetriever recipeRetriever,
      MachineConfigsValidator machinesValidator,
      KubernetesClientFactory clientFactory,
      KubernetesEnvironmentValidator envValidator,
      MemoryAttributeProvisioner memoryProvisioner) {
    super(installerRegistry, recipeRetriever, machinesValidator);
    this.clientFactory = clientFactory;
    this.envValidator = envValidator;
    this.memoryProvisioner = memoryProvisioner;
  }

  @Override
  protected KubernetesEnvironment doCreate(
      @Nullable InternalRecipe recipe,
      Map<String, InternalMachineConfig> machines,
      List<Warning> sourceWarnings)
      throws InfrastructureException, ValidationException {
    checkNotNull(recipe, "Null recipe is not supported by kubernetes environment factory");
    List<Warning> warnings = new ArrayList<>();
    if (sourceWarnings != null) {
      warnings.addAll(sourceWarnings);
    }
    String content = recipe.getContent();
    String contentType = recipe.getContentType();
    checkNotNull(contentType, "Kubernetes Recipe content type should not be null");

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

    final KubernetesList list;
    try {
      list =
          clientFactory.create().lists().load(new ByteArrayInputStream(content.getBytes())).get();
    } catch (KubernetesClientException e) {
      // KubernetesClient wraps the error when a JsonMappingException occurs so we need the cause
      String message = e.getCause() == null ? e.getMessage() : e.getCause().getMessage();
      if (message.contains("\n")) {
        // Clean up message if it comes from JsonMappingException. Format is e.g.
        // `No resource type found for:v1#Route1\n at [...]`
        message = message.split("\\n", 2)[0];
      }
      throw new ValidationException(format("Could not parse Kubernetes recipe: %s", message));
    }

    Map<String, Pod> pods = new HashMap<>();
    Map<String, Deployment> deployments = new HashMap<>();
    Map<String, Service> services = new HashMap<>();
    Map<String, ConfigMap> configMaps = new HashMap<>();
    Map<String, PersistentVolumeClaim> pvcs = new HashMap<>();
    Map<String, Secret> secrets = new HashMap<>();
    boolean isAnyIngressPresent = false;
    for (HasMetadata object : list.getItems()) {
      checkNotNull(object.getKind(), "Environment contains object without specified kind field");
      checkNotNull(object.getMetadata(), "%s metadata must not be null", object.getKind());
      checkNotNull(object.getMetadata().getName(), "%s name must not be null", object.getKind());

      if (object instanceof Pod) {
        Pod pod = (Pod) object;
        pods.put(pod.getMetadata().getName(), pod);
      } else if (object instanceof Deployment) {
        Deployment deployment = (Deployment) object;
        deployments.put(deployment.getMetadata().getName(), deployment);
      } else if (object instanceof Service) {
        Service service = (Service) object;
        services.put(service.getMetadata().getName(), service);
      } else if (object instanceof Ingress) {
        isAnyIngressPresent = true;
      } else if (object instanceof PersistentVolumeClaim) {
        PersistentVolumeClaim pvc = (PersistentVolumeClaim) object;
        pvcs.put(pvc.getMetadata().getName(), pvc);
      } else if (object instanceof Secret) {
        Secret secret = (Secret) object;
        secrets.put(secret.getMetadata().getName(), secret);
      } else if (object instanceof ConfigMap) {
        ConfigMap configMap = (ConfigMap) object;
        configMaps.put(configMap.getMetadata().getName(), configMap);
      } else {
        throw new ValidationException(
            format(
                "Found unknown object type in recipe -- name: '%s', kind: '%s'",
                object.getMetadata().getName(), object.getKind()));
      }
    }

    if (isAnyIngressPresent) {
      warnings.add(
          new WarningImpl(
              Warnings.INGRESSES_IGNORED_WARNING_CODE, Warnings.INGRESSES_IGNORED_WARNING_MESSAGE));
    }

    addRamAttributes(machines, pods.values());

    KubernetesEnvironment k8sEnv =
        KubernetesEnvironment.builder()
            .setInternalRecipe(recipe)
            .setMachines(machines)
            .setWarnings(warnings)
            .setPods(pods)
            .setDeployments(deployments)
            .setServices(services)
            .setPersistentVolumeClaims(pvcs)
            .setIngresses(new HashMap<>())
            .setPersistentVolumeClaims(new HashMap<>())
            .setSecrets(secrets)
            .setConfigMaps(configMaps)
            .build();

    envValidator.validate(k8sEnv);

    return k8sEnv;
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

  private void checkNotNull(Object object, String messageFmt, Object... messageArguments)
      throws ValidationException {
    if (object == null) {
      throw new ValidationException(format(messageFmt, messageArguments));
    }
  }
}
