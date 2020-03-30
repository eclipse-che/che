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
package org.eclipse.che.workspace.infrastructure.kubernetes.devfile;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.api.core.model.workspace.config.Command.MACHINE_NAME_ATTRIBUTE;
import static org.eclipse.che.api.core.model.workspace.config.MachineConfig.DEVFILE_COMPONENT_ALIAS_ATTRIBUTE;
import static org.eclipse.che.api.workspace.server.devfile.Components.getIdentifiableComponentName;
import static org.eclipse.che.api.workspace.shared.Constants.PROJECTS_VOLUME_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Names.machineName;
import static org.eclipse.che.workspace.infrastructure.kubernetes.devfile.KubernetesDevfileBindings.KUBERNETES_BASED_COMPONENTS_KEY_NAME;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newPVC;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolume;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolumeMount;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.config.Command;
import org.eclipse.che.api.core.model.workspace.devfile.Component;
import org.eclipse.che.api.core.model.workspace.devfile.Entrypoint;
import org.eclipse.che.api.workspace.server.devfile.Constants;
import org.eclipse.che.api.workspace.server.devfile.DevfileRecipeFormatException;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.convert.component.ComponentToWorkspaceApplier;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.api.workspace.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesRecipeParser;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.EnvVars;

/**
 * Applies changes on workspace config according to the specified kubernetes/openshift component.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesComponentToWorkspaceApplier implements ComponentToWorkspaceApplier {

  private final KubernetesRecipeParser objectsParser;
  private final KubernetesEnvironmentProvisioner k8sEnvProvisioner;
  private final String environmentType;
  private final String projectFolderPath;
  private final String defaultProjectPVCSize;
  private final String imagePullPolicy;
  private final Set<String> kubernetesBasedComponentTypes;
  private final String defaultPVCAccessMode;
  private final String pvcStorageClassName;
  private final EnvVars envVars;

  @Inject
  public KubernetesComponentToWorkspaceApplier(
      KubernetesRecipeParser objectsParser,
      KubernetesEnvironmentProvisioner k8sEnvProvisioner,
      EnvVars envVars,
      @Named("che.workspace.projects.storage") String projectFolderPath,
      @Named("che.workspace.projects.storage.default.size") String defaultProjectPVCSize,
      @Named("che.infra.kubernetes.pvc.access_mode") String defaultPVCAccessMode,
      @Named("che.infra.kubernetes.pvc.storage_class_name") String pvcStorageClassName,
      @Named("che.workspace.sidecar.image_pull_policy") String imagePullPolicy,
      @Named(KUBERNETES_BASED_COMPONENTS_KEY_NAME) Set<String> kubernetesBasedComponentTypes) {
    this(
        objectsParser,
        k8sEnvProvisioner,
        envVars,
        KubernetesEnvironment.TYPE,
        projectFolderPath,
        defaultProjectPVCSize,
        defaultPVCAccessMode,
        pvcStorageClassName,
        imagePullPolicy,
        kubernetesBasedComponentTypes);
  }

  protected KubernetesComponentToWorkspaceApplier(
      KubernetesRecipeParser objectsParser,
      KubernetesEnvironmentProvisioner k8sEnvProvisioner,
      EnvVars envVars,
      String environmentType,
      String projectFolderPath,
      String defaultProjectPVCSize,
      String defaultPVCAccessMode,
      String pvcStorageClassName,
      String imagePullPolicy,
      Set<String> kubernetesBasedComponentTypes) {
    this.objectsParser = objectsParser;
    this.k8sEnvProvisioner = k8sEnvProvisioner;
    this.environmentType = environmentType;
    this.projectFolderPath = projectFolderPath;
    this.defaultProjectPVCSize = defaultProjectPVCSize;
    this.defaultPVCAccessMode = defaultPVCAccessMode;
    this.pvcStorageClassName = pvcStorageClassName;
    this.imagePullPolicy = imagePullPolicy;
    this.kubernetesBasedComponentTypes = kubernetesBasedComponentTypes;
    this.envVars = envVars;
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
      ComponentImpl k8sComponent,
      FileContentProvider contentProvider)
      throws DevfileException {
    checkArgument(workspaceConfig != null, "Workspace config must not be null");
    checkArgument(k8sComponent != null, "Component must not be null");
    checkArgument(
        kubernetesBasedComponentTypes.contains(k8sComponent.getType()),
        format("Plugin must have %s type", String.join(" or ", kubernetesBasedComponentTypes)));

    String componentContent = retrieveContent(k8sComponent, contentProvider);

    List<HasMetadata> componentObjects =
        new ArrayList<>(unmarshalComponentObjects(k8sComponent, componentContent));

    if (!k8sComponent.getSelector().isEmpty()) {
      componentObjects = SelectorFilter.filter(componentObjects, k8sComponent.getSelector());
    }

    List<PodData> podsData = getPodDatas(componentObjects);

    podsData
        .stream()
        .flatMap(e -> e.getSpec().getContainers().stream())
        .forEach(c -> c.setImagePullPolicy(imagePullPolicy));

    if (Boolean.TRUE.equals(k8sComponent.getMountSources())) {
      applyProjectsVolumes(podsData, componentObjects);
    }

    if (!k8sComponent.getEnv().isEmpty()) {
      podsData.forEach(p -> envVars.apply(p, k8sComponent.getEnv()));
    }

    applyEntrypoints(k8sComponent.getEntrypoints(), componentObjects);

    Map<String, MachineConfigImpl> machineConfigs =
        prepareMachineConfigs(podsData, k8sComponent.getAlias());

    linkCommandsToMachineName(workspaceConfig, k8sComponent, machineConfigs.keySet());

    k8sEnvProvisioner.provision(workspaceConfig, environmentType, componentObjects, machineConfigs);
  }

  private void applyProjectsVolumes(List<PodData> podsData, List<HasMetadata> componentObjects) {
    if (componentObjects
        .stream()
        .noneMatch(
            hasMeta ->
                hasMeta instanceof PersistentVolumeClaim
                    && hasMeta.getMetadata().getName().equals(PROJECTS_VOLUME_NAME))) {
      PersistentVolumeClaim volumeClaim =
          newPVC(
              PROJECTS_VOLUME_NAME,
              defaultPVCAccessMode,
              defaultProjectPVCSize,
              pvcStorageClassName);
      componentObjects.add(volumeClaim);
    }

    for (PodData podData : podsData) {
      if (podData
          .getSpec()
          .getVolumes()
          .stream()
          .noneMatch(volume -> volume.getName().equals(PROJECTS_VOLUME_NAME))) {
        Volume volume = newVolume(PROJECTS_VOLUME_NAME, PROJECTS_VOLUME_NAME);
        podData.getSpec().getVolumes().add(volume);
      }
      for (Container container : podData.getSpec().getContainers()) {
        if (container
            .getVolumeMounts()
            .stream()
            .noneMatch(mount -> mount.getName().equals(PROJECTS_VOLUME_NAME))) {
          VolumeMount volumeMount = newVolumeMount(PROJECTS_VOLUME_NAME, projectFolderPath, null);
          container.getVolumeMounts().add(volumeMount);
        }
      }
    }
  }

  /**
   * Creates map of machine names and corresponding {@link MachineConfigImpl} with component alias
   * attribute set.
   */
  private Map<String, MachineConfigImpl> prepareMachineConfigs(
      List<PodData> podsData, @Nullable String componentAlias) {
    Map<String, MachineConfigImpl> machineConfigs = new HashMap<>();
    for (PodData podData : podsData) {
      for (Container container : podData.getSpec().getContainers()) {
        String machineName = machineName(podData, container);

        MachineConfigImpl config = new MachineConfigImpl();
        if (!isNullOrEmpty(componentAlias)) {
          config.getAttributes().put(DEVFILE_COMPONENT_ALIAS_ATTRIBUTE, componentAlias);
        }

        machineConfigs.put(machineName, config);
      }
    }
    return machineConfigs;
  }

  private String retrieveContent(Component recipeComponent, FileContentProvider fileContentProvider)
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
  private void linkCommandsToMachineName(
      WorkspaceConfig workspaceConfig, Component component, Set<String> machinesNames) {
    List<? extends Command> componentCommands =
        workspaceConfig
            .getCommands()
            .stream()
            .filter(
                c ->
                    component.getAlias() != null
                        && component
                            .getAlias()
                            .equals(
                                c.getAttributes().get(Constants.COMPONENT_ALIAS_COMMAND_ATTRIBUTE)))
            .collect(toList());
    if (componentCommands.isEmpty()) {
      return;
    }

    if (machinesNames.size() != 1) {
      // many or no pods - can't estimate the name because of ambiguity or lack of information
      return;
    }

    String machineName = machinesNames.iterator().next();
    componentCommands.forEach(c -> c.getAttributes().put(MACHINE_NAME_ATTRIBUTE, machineName));
  }

  private List<PodData> getPodDatas(List<HasMetadata> componentsObjects) {
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
    return podsData;
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
