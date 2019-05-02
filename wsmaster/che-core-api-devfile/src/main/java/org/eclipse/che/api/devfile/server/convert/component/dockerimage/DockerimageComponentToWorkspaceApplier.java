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
package org.eclipse.che.api.devfile.server.convert.component.dockerimage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.DISCOVERABLE_ENDPOINT_ATTRIBUTE;
import static org.eclipse.che.api.devfile.server.Constants.DOCKERIMAGE_COMPONENT_TYPE;
import static org.eclipse.che.api.devfile.server.Constants.PUBLIC_ENDPOINT_ATTRIBUTE;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.MACHINE_NAME_ANNOTATION_FMT;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.ServerConfig;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.core.model.workspace.devfile.Endpoint;
import org.eclipse.che.api.devfile.server.Constants;
import org.eclipse.che.api.devfile.server.FileContentProvider;
import org.eclipse.che.api.devfile.server.convert.component.ComponentToWorkspaceApplier;
import org.eclipse.che.api.devfile.server.convert.component.kubernetes.KubernetesEnvironmentProvisioner;
import org.eclipse.che.api.devfile.server.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.ServerConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.VolumeImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.Containers;

/**
 * Applies changes on workspace config according to the specified dockerimage component.
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
  private final KubernetesEnvironmentProvisioner k8sEnvProvisioner;

  @Inject
  public DockerimageComponentToWorkspaceApplier(
      @Named("che.workspace.projects.storage") String projectFolderPath,
      KubernetesEnvironmentProvisioner k8sEnvProvisioner) {
    this.projectFolderPath = projectFolderPath;
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
      Component dockerimageComponent,
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

    MachineConfigImpl machineConfig = new MachineConfigImpl();
    dockerimageComponent
        .getEndpoints()
        .forEach(e -> machineConfig.getServers().put(e.getName(), toServerConfig(e)));

    dockerimageComponent
        .getVolumes()
        .forEach(
            v ->
                machineConfig
                    .getVolumes()
                    .put(v.getName(), new VolumeImpl().withPath(v.getContainerPath())));

    if (dockerimageComponent.getMountSources()) {
      machineConfig
          .getVolumes()
          .put(PROJECTS_VOLUME_NAME, new VolumeImpl().withPath(projectFolderPath));
    }

    List<HasMetadata> componentObjects = new ArrayList<>();
    Deployment deployment =
        buildDeployment(
            machineName,
            dockerimageComponent.getImage(),
            dockerimageComponent.getMemoryLimit(),
            dockerimageComponent
                .getEnv()
                .stream()
                .map(e -> new EnvVar(e.getName(), e.getValue(), null))
                .collect(Collectors.toCollection(ArrayList::new)),
            dockerimageComponent.getCommand(),
            dockerimageComponent.getArgs());
    componentObjects.add(deployment);

    dockerimageComponent
        .getEndpoints()
        .stream()
        .filter(e -> "true".equals(e.getAttributes().get(DISCOVERABLE_ENDPOINT_ATTRIBUTE)))
        .forEach(e -> componentObjects.add(createService(deployment, e)));

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

  private Deployment buildDeployment(
      String name,
      String image,
      String memoryLimit,
      List<EnvVar> env,
      List<String> command,
      List<String> args) {
    Container container =
        new ContainerBuilder()
            .withImage(image)
            .withName(name)
            .withEnv(env)
            .withCommand(command)
            .withArgs(args)
            .build();

    Containers.addRamLimit(container, memoryLimit);
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
        .addToAnnotations(String.format(MACHINE_NAME_ANNOTATION_FMT, name), name)
        .endMetadata()
        .withNewSpec()
        .withContainers(container)
        .endSpec()
        .endTemplate()
        .endSpec()
        .build();
  }

  private ServerConfigImpl toServerConfig(Endpoint endpoint) {
    HashMap<String, String> attributes = new HashMap<>(endpoint.getAttributes());

    String protocol = attributes.remove("protocol");
    if (isNullOrEmpty(protocol)) {
      protocol = "http";
    }

    String path = attributes.remove("path");

    String isPublic = attributes.remove(PUBLIC_ENDPOINT_ATTRIBUTE);
    if ("false".equals(isPublic)) {
      attributes.put(ServerConfig.INTERNAL_SERVER_ATTRIBUTE, "true");
    }

    return new ServerConfigImpl(Integer.toString(endpoint.getPort()), protocol, path, attributes);
  }

  private Service createService(Deployment deployment, Endpoint endpoint) {
    ServicePort servicePort =
        new ServicePortBuilder()
            .withPort(endpoint.getPort())
            .withProtocol("TCP")
            .withNewTargetPort(endpoint.getPort())
            .build();
    return new ServiceBuilder()
        .withNewMetadata()
        .withName(endpoint.getName())
        .endMetadata()
        .withNewSpec()
        .withSelector(
            ImmutableMap.of(
                CHE_COMPONENT_NAME_LABEL,
                deployment.getSpec().getTemplate().getMetadata().getName()))
        .withPorts(singletonList(servicePort))
        .endSpec()
        .build();
  }

  @VisibleForTesting
  static String toMachineName(String imageName) throws DevfileException {
    if (imageName.isEmpty()) {
      return imageName;
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
