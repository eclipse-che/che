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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.DEVFILE_COMPONENT_ALIAS_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Constants.DOCKERIMAGE_COMPONENT_TYPE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.devfile.Endpoint;
import org.eclipse.che.api.workspace.server.devfile.Constants;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSize;

/**
 * Applies changes on workspace config according to the specified dockerimage component.
 *
 * <p>The {@code dockerimage} devfile components are handled as Kubernetes deployments internally.
 *
 * @author Sergii Leshchenko
 */
public class DockerimageComponentToWorkspaceApplier implements ComponentToWorkspaceApplier {

  /**
   * Label that contains component name to which object belongs to and it is provisioned for
   * generated deployments and its pod templates.
   */
  static final String CHE_COMPONENT_NAME_LABEL = "che.component.name";

  private final String projectFolderPath;
  private final String imagePullPolicy;
  private final KubernetesEnvironmentProvisioner k8sEnvProvisioner;

  @Inject
  public DockerimageComponentToWorkspaceApplier(
      @Named("che.workspace.projects.storage") String projectFolderPath,
      @Named("che.workspace.sidecar.image_pull_policy") String imagePullPolicy,
      KubernetesEnvironmentProvisioner k8sEnvProvisioner) {
    this.projectFolderPath = projectFolderPath;
    this.imagePullPolicy = imagePullPolicy;
    this.k8sEnvProvisioner = k8sEnvProvisioner;
  }

  /**
   * Applies changes on workspace config according to the specified dockerimage component.
   *
   * <p>Dockerimage component is provisioned as Deployment in Kubernetes recipe.<br>
   * Generated deployment contains container with environment variables, memory limit, docker image,
   * arguments and commands specified in component.<br>
   * Also, environment is provisioned with machine config with volumes and servers specified, then
   * Kubernetes infra will created needed PVC, Services, Ingresses, Routes according to specified
   * configuration.
   *
   * @param workspaceConfig workspace config on which changes should be applied
   * @param dockerimageComponent dockerimage component that should be applied
   * @param contentProvider optional content provider that may be used for external component
   *     resource fetching
   * @throws DevfileException if specified workspace config already has default environment where
   *     dockerimage component should be stored
   * @throws IllegalArgumentException if specified workspace config or plugin component is null
   * @throws IllegalArgumentException if specified component has type different from dockerimage
   */
  @Override
  public void apply(
      WorkspaceConfigImpl workspaceConfig,
      ComponentImpl dockerimageComponent,
      FileContentProvider contentProvider)
      throws DevfileException {
    checkArgument(workspaceConfig != null, "Workspace config must not be null");
    checkArgument(dockerimageComponent != null, "Component must not be null");
    checkArgument(
        DOCKERIMAGE_COMPONENT_TYPE.equals(dockerimageComponent.getType()),
        format("Plugin must have `%s` type", DOCKERIMAGE_COMPONENT_TYPE));

    String componentAlias = dockerimageComponent.getAlias();
    String machineName =
        componentAlias == null ? toMachineName(dockerimageComponent.getImage()) : componentAlias;

    MachineConfigImpl machineConfig = createMachineConfig(dockerimageComponent, componentAlias);
    List<HasMetadata> componentObjects = createComponentObjects(dockerimageComponent, machineName);

    k8sEnvProvisioner.provision(
        workspaceConfig,
        KubernetesEnvironment.TYPE,
        componentObjects,
        ImmutableMap.of(machineName, machineConfig));

    workspaceConfig
        .getCommands()
        .stream()
        .filter(
            c ->
                componentAlias != null
                    && componentAlias.equals(
                        c.getAttributes().get(Constants.COMPONENT_ALIAS_COMMAND_ATTRIBUTE)))
        .forEach(c -> c.getAttributes().put(MACHINE_NAME_ATTRIBUTE, machineName));
  }

  private MachineConfigImpl createMachineConfig(
      ComponentImpl dockerimageComponent, String componentAlias) {
    MachineConfigImpl machineConfig = new MachineConfigImpl();
    machineConfig
        .getServers()
        .putAll(
            dockerimageComponent
                .getEndpoints()
                .stream()
                .collect(
                    Collectors.toMap(
                        Endpoint::getName, e -> ServerConfigImpl.createFromEndpoint(e, true))));

    dockerimageComponent
        .getVolumes()
        .forEach(
            v ->
                machineConfig
                    .getVolumes()
                    .put(v.getName(), new VolumeImpl().withPath(v.getContainerPath())));

    if (Boolean.TRUE.equals(dockerimageComponent.getMountSources())) {
      machineConfig
          .getVolumes()
          .put(PROJECTS_VOLUME_NAME, new VolumeImpl().withPath(projectFolderPath));
    }

    if (!isNullOrEmpty(componentAlias)) {
      machineConfig.getAttributes().put(DEVFILE_COMPONENT_ALIAS_ATTRIBUTE, componentAlias);
    }

    return machineConfig;
  }

  private List<HasMetadata> createComponentObjects(
      ComponentImpl dockerimageComponent, String machineName) {
    List<HasMetadata> componentObjects = new ArrayList<>();
    Deployment deployment =
        buildDeployment(
            machineName,
            dockerimageComponent.getImage(),
            dockerimageComponent.getMemoryRequest(),
            dockerimageComponent.getMemoryLimit(),
            dockerimageComponent.getCpuRequest(),
            dockerimageComponent.getCpuLimit(),
            dockerimageComponent
                .getEnv()
                .stream()
                .map(e -> new EnvVar(e.getName(), e.getValue(), null))
                .collect(Collectors.toCollection(ArrayList::new)),
            dockerimageComponent.getCommand(),
            dockerimageComponent.getArgs());
    componentObjects.add(deployment);

    return componentObjects;
  }

  private Deployment buildDeployment(
      String name,
      String image,
      String memoryRequest,
      String memoryLimit,
      String cpuRequest,
      String cpuLimit,
      List<EnvVar> env,
      List<String> command,
      List<String> args) {
    Container container =
        new ContainerBuilder()
            .withImage(image)
            .withImagePullPolicy(imagePullPolicy)
            .withName(name)
            .withEnv(env)
            .withCommand(command)
            .withArgs(args)
            .build();

    Containers.addRamLimit(container, memoryLimit);
    if (!isNullOrEmpty(memoryRequest)) {
      Containers.addRamRequest(container, memoryRequest);
    }
    if (!isNullOrEmpty(cpuRequest)) {
      Containers.addCpuRequest(container, KubernetesSize.toCores(cpuRequest));
    }
    if (!isNullOrEmpty(cpuLimit)) {
      Containers.addCpuLimit(container, KubernetesSize.toCores(cpuLimit));
    }
    return new DeploymentBuilder()
        .withNewMetadata()
        .addToLabels(CHE_COMPONENT_NAME_LABEL, name)
        .withName(name)
        .endMetadata()
        .withNewSpec()
        .withNewSelector()
        .addToMatchLabels(CHE_COMPONENT_NAME_LABEL, name)
        .endSelector()
        .withNewTemplate()
        .withNewMetadata()
        .withName(name)
        .addToLabels(CHE_COMPONENT_NAME_LABEL, name)
        .addToAnnotations(Names.createMachineNameAnnotations(name, name))
        .endMetadata()
        .withNewSpec()
        .withContainers(container)
        .endSpec()
        .endTemplate()
        .endSpec()
        .build();
  }

  @VisibleForTesting
  static String toMachineName(String imageName) throws DevfileException {
    if (imageName.isEmpty()) {
      return imageName;
    }

    if (imageName.length() > Names.MAX_CONTAINER_NAME_LENGTH) {
      throw new DevfileException(
          format(
              "The image name '%s' is longer than 63 characters and as such cannot be used as a container"
                  + " name. Please provide an alias for the component with that image.",
              imageName));
    }

    // the name needs to be both a valid k8s label and a valid machine name.
    String clean = imageName.replaceAll("[^-a-zA-Z0-9_]", "-");

    if (isInvalidStartEndChar(clean.charAt(0))
        || isInvalidStartEndChar(clean.charAt(clean.length() - 1))) {
      throw new DevfileException(
          format(
              "Cannot convert image %s to a valid component name."
                  + " Please provide an alias that conforms to the Kubernetes label value format.",
              imageName));
    }

    return clean;
  }

  /** @return true if the character isn't an ASCII letter (of either case) or a number. */
  private static boolean isInvalidStartEndChar(char ch) {
    return ch < '0' || ch > 'z';
  }
}
