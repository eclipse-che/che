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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_VOLUME_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newPVC;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolume;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolumeMount;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.LogsVolumeMachineProvisioner.LOGS_VOLUME_NAME;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.config.MachineConfig;
import org.eclipse.che.api.core.model.workspace.config.Volume;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

/**
 * Helps to work with {@link PersistentVolumeClaim} and provision them to {@link
 * KubernetesEnvironment}.
 *
 * @author Sergii Leshchenko
 */
public class PVCProvisioner {

  private final String pvcNamePrefix;
  private final String pvcQuantity;
  private final String pvcAccessMode;
  private final String pvcStorageClassName;
  private final PodsVolumes podsVolumes;

  @Inject
  public PVCProvisioner(
      @Named("che.infra.kubernetes.pvc.name") String pvcNamePrefix,
      @Named("che.infra.kubernetes.pvc.quantity") String pvcQuantity,
      @Named("che.infra.kubernetes.pvc.access_mode") String pvcAccessMode,
      @Named("che.infra.kubernetes.pvc.storage_class_name") String pvcStorageClassName,
      PodsVolumes podsVolumes) {
    this.pvcNamePrefix = pvcNamePrefix;
    this.pvcQuantity = pvcQuantity;
    this.pvcAccessMode = pvcAccessMode;
    this.pvcStorageClassName = pvcStorageClassName;
    this.podsVolumes = podsVolumes;
  }

  /**
   * Converts {@link Volume} specified in {@link MachineConfig#getVolumes()} to {@link
   * PersistentVolumeClaim}s and provision them to {@link KubernetesEnvironment}. The machines
   * corresponding pods and containers are updated in accordance.
   *
   * @param k8sEnv environment to provision
   * @param workspaceId identifier of workspace to which the specified environment belongs to
   */
  public void convertCheVolumes(KubernetesEnvironment k8sEnv, String workspaceId) {
    Map<String, PersistentVolumeClaim> volumeName2PVC =
        groupByVolumeName(k8sEnv.getPersistentVolumeClaims().values());

    for (PodData pod : k8sEnv.getPodsData().values()) {
      final PodSpec podSpec = pod.getSpec();
      List<Container> containers = new ArrayList<>();
      containers.addAll(podSpec.getContainers());
      containers.addAll(podSpec.getInitContainers());
      for (Container container : containers) {
        final String machineName = Names.machineName(pod, container);
        InternalMachineConfig machineConfig = k8sEnv.getMachines().get(machineName);
        if (machineConfig == null) {
          continue;
        }
        Map<String, Volume> volumes = machineConfig.getVolumes();
        addMachineVolumes(workspaceId, k8sEnv, volumeName2PVC, pod, container, volumes);
      }
    }
  }

  /**
   * Provision the specified PVCs to the environment.
   *
   * <p>Note that:<br>
   * - PVC is not provisioned if environment already contains PVC for corresponding volume;<br>
   * - PVC is provisioned with generated unique name;<br>
   * - corresponding PVC references in Kubernetes Environment are updated during provisioning;<br>
   *
   * @param k8sEnv environment to provision
   * @param toProvision PVCs that should be provisioned to the environment
   */
  public void provision(
      KubernetesEnvironment k8sEnv, Map<String, PersistentVolumeClaim> toProvision) {
    final Map<String, PersistentVolumeClaim> volumeName2PVC =
        groupByVolumeName(k8sEnv.getPersistentVolumeClaims().values());

    // process user-defined PVCs according to unique strategy
    final Map<String, PersistentVolumeClaim> envClaims = k8sEnv.getPersistentVolumeClaims();
    for (PersistentVolumeClaim pvc : toProvision.values()) {
      String originalPVCName = pvc.getMetadata().getName();

      PersistentVolumeClaim existingPVC = volumeName2PVC.get(originalPVCName);

      if (existingPVC != null) {
        // Replace pvc in environment with existing. Fix the references in Pods
        podsVolumes.changePVCReferences(
            k8sEnv.getPodsData().values(), originalPVCName, existingPVC.getMetadata().getName());
      } else {
        // there is no the corresponding existing pvc
        // new one should be created with generated name
        putLabel(pvc, CHE_VOLUME_NAME_LABEL, originalPVCName);

        final String uniqueName = Names.generateName(pvcNamePrefix + '-');
        pvc.getMetadata().setName(uniqueName);
        envClaims.put(uniqueName, pvc);

        volumeName2PVC.put(originalPVCName, pvc);
        podsVolumes.changePVCReferences(k8sEnv.getPodsData().values(), originalPVCName, uniqueName);
      }
    }
  }

  /**
   * Groups list of given PVCs by volume name. The result may be used for easy accessing to PVCs by
   * Che Volume name.
   */
  private Map<String, PersistentVolumeClaim> groupByVolumeName(
      Collection<PersistentVolumeClaim> pvcs) {
    final Map<String, PersistentVolumeClaim> grouped = new HashMap<>();
    for (PersistentVolumeClaim pvc : pvcs) {
      final ObjectMeta metadata = pvc.getMetadata();
      final String volumeName;
      if (metadata.getLabels() != null
          && (volumeName = metadata.getLabels().get(CHE_VOLUME_NAME_LABEL)) != null) {
        grouped.put(volumeName, pvc);
      } else {
        grouped.put(metadata.getName(), pvc);
        putLabel(metadata, CHE_VOLUME_NAME_LABEL, metadata.getName());
      }
    }
    return grouped;
  }

  private void addMachineVolumes(
      String workspaceId,
      KubernetesEnvironment k8sEnv,
      Map<String, PersistentVolumeClaim> volumeName2PVC,
      PodData pod,
      Container container,
      Map<String, Volume> volumes) {
    if (volumes.isEmpty()) {
      return;
    }

    for (Entry<String, Volume> volumeEntry : volumes.entrySet()) {
      final String volumePath = volumeEntry.getValue().getPath();
      final String volumeName =
          LOGS_VOLUME_NAME.equals(volumeEntry.getKey())
              ? volumeEntry.getKey() + '-' + pod.getMetadata().getName()
              : volumeEntry.getKey();
      final PersistentVolumeClaim pvc;
      // checks whether PVC for given workspace and volume present in environment
      if (volumeName2PVC.containsKey(volumeName)) {
        pvc = volumeName2PVC.get(volumeName);
      }
      // when PVC is not found in environment then create new one
      else {
        final String uniqueName = Names.generateName(pvcNamePrefix);
        pvc = newPVC(uniqueName, pvcAccessMode, pvcQuantity, pvcStorageClassName);
        putLabel(pvc, CHE_WORKSPACE_ID_LABEL, workspaceId);
        putLabel(pvc, CHE_VOLUME_NAME_LABEL, volumeName);
        k8sEnv.getPersistentVolumeClaims().put(uniqueName, pvc);
        volumeName2PVC.put(volumeName, pvc);
      }

      // binds pvc to pod and container
      String pvcName = pvc.getMetadata().getName();
      PodSpec podSpec = pod.getSpec();
      Optional<io.fabric8.kubernetes.api.model.Volume> volumeOpt =
          podSpec
              .getVolumes()
              .stream()
              .filter(
                  volume ->
                      volume.getPersistentVolumeClaim() != null
                          && pvcName.equals(volume.getPersistentVolumeClaim().getClaimName()))
              .findAny();
      io.fabric8.kubernetes.api.model.Volume podVolume;
      if (volumeOpt.isPresent()) {
        podVolume = volumeOpt.get();
      } else {
        podVolume = newVolume(pvcName, pvcName);
        podSpec.getVolumes().add(podVolume);
      }

      container.getVolumeMounts().add(newVolumeMount(podVolume.getName(), volumePath, ""));
    }
  }
}
