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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_VOLUME_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newPVC;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolume;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolumeMount;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.LogsVolumeMachineProvisioner.LOGS_VOLUME_NAME;

import com.google.common.collect.ImmutableMap;
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
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.config.Volume;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.Names;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesPersistentVolumeClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a unique PVC for each volume of a workspace.
 *
 * <p>Names for PVCs are evaluated as: '{configured_prefix}' + '-' +'{generated_8_chars}' to avoid
 * naming collisions inside of one Kubernetes namespace.
 *
 * <p>Note that for this strategy count of simultaneously used volumes by workspaces is always the
 * same and equal to the count of available PVCs in Kubernetes namespace.
 *
 * <p>The usage of PVCs for this strategy is next: one PVC per volume, but for volumes that are
 * provided by Che there a small exception: <br>
 * - when the workspace contains few machines that are placed in separated pods and relies on the
 * same volume then, for each of the pods' the separate PVC would be provided.
 *
 * <p>This strategy uses subpaths to do the same as {@link CommonPVCStrategy} does and make easier
 * data migration if it will be needed.<br>
 * Subpaths have the following format: '{workspaceId}/{volumeName}'.<br>
 * Note that logs volume has the special format: '{workspaceId}/{volumeName}/{machineName}'. It is
 * done in this way to avoid conflicts e.g. two identical agents inside different machines produce
 * the same log file.
 *
 * <p>Cleanup of backed up data is performed by removing of PVCs related to the workspace but when
 * the volume or machine name is changed then related PVC would not be removed.
 *
 * @author Anton Korneta
 * @author Alexander Garagatyi
 */
public class UniqueWorkspacePVCStrategy implements WorkspaceVolumesStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(UniqueWorkspacePVCStrategy.class);
  public static final String UNIQUE_STRATEGY = "unique";

  private final String pvcNamePrefix;
  private final String pvcQuantity;
  private final String pvcAccessMode;
  private final KubernetesNamespaceFactory factory;
  private final EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter;

  @Inject
  public UniqueWorkspacePVCStrategy(
      @Named("che.infra.kubernetes.pvc.name") String pvcNamePrefix,
      @Named("che.infra.kubernetes.pvc.quantity") String pvcQuantity,
      @Named("che.infra.kubernetes.pvc.access_mode") String pvcAccessMode,
      KubernetesNamespaceFactory factory,
      EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter) {
    this.pvcNamePrefix = pvcNamePrefix;
    this.pvcQuantity = pvcQuantity;
    this.pvcAccessMode = pvcAccessMode;
    this.factory = factory;
    this.ephemeralWorkspaceAdapter = ephemeralWorkspaceAdapter;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    final String workspaceId = identity.getWorkspaceId();
    if (EphemeralWorkspaceUtility.isEphemeral(k8sEnv.getAttributes())) {
      ephemeralWorkspaceAdapter.provision(k8sEnv, identity);
      return;
    }
    LOG.debug("Provisioning PVC strategy for workspace '{}'", workspaceId);
    final Map<String, PersistentVolumeClaim> claims = k8sEnv.getPersistentVolumeClaims();
    // fetches all existing PVCs related to given workspace and groups them by volume name
    final Map<String, PersistentVolumeClaim> volumeName2PVC =
        groupByVolumeName(
            factory
                .create(workspaceId)
                .persistentVolumeClaims()
                .getByLabel(CHE_WORKSPACE_ID_LABEL, workspaceId));
    for (PodData pod : k8sEnv.getPodsData().values()) {
      final PodSpec podSpec = pod.getSpec();
      List<Container> containers = new ArrayList<>();
      containers.addAll(podSpec.getContainers());
      containers.addAll(podSpec.getInitContainers());
      for (Container container : containers) {
        final String machineName = Names.machineName(pod, container);
        Map<String, Volume> volumes = k8sEnv.getMachines().get(machineName).getVolumes();
        addMachineVolumes(workspaceId, claims, volumeName2PVC, pod, container, volumes);
      }
    }
    LOG.debug("PVC strategy provisioning done for workspace '{}'", workspaceId);
  }

  @Override
  @Traced
  public void prepare(KubernetesEnvironment k8sEnv, String workspaceId, long timeoutMillis)
      throws InfrastructureException {
    TracingTags.WORKSPACE_ID.set(workspaceId);

    if (EphemeralWorkspaceUtility.isEphemeral(k8sEnv.getAttributes())) {
      return;
    }
    if (!k8sEnv.getPersistentVolumeClaims().isEmpty()) {
      final KubernetesPersistentVolumeClaims k8sClaims =
          factory.create(workspaceId).persistentVolumeClaims();
      LOG.debug("Creating PVCs for workspace '{}'", workspaceId);
      for (PersistentVolumeClaim pvc : k8sEnv.getPersistentVolumeClaims().values()) {
        k8sClaims.create(pvc);
      }

      LOG.debug("Waiting PVCs for workspace '{}' to be bound", workspaceId);
      for (PersistentVolumeClaim pvc : k8sEnv.getPersistentVolumeClaims().values()) {
        k8sClaims.waitBound(pvc.getMetadata().getName(), timeoutMillis);
      }
      LOG.debug("Preparing PVCs done for workspace '{}'", workspaceId);
    }
  }

  private void addMachineVolumes(
      String workspaceId,
      Map<String, PersistentVolumeClaim> provisionedClaims,
      Map<String, PersistentVolumeClaim> existingVolumeName2PVC,
      PodData pod,
      Container container,
      Map<String, Volume> volumes)
      throws InfrastructureException {
    if (volumes.isEmpty()) {
      return;
    }
    final Map<String, PersistentVolumeClaim> provisionedVolumeName2PVC =
        groupByVolumeName(provisionedClaims.values());

    for (Entry<String, Volume> volumeEntry : volumes.entrySet()) {
      final String volumePath = volumeEntry.getValue().getPath();
      final String volumeName =
          LOGS_VOLUME_NAME.equals(volumeEntry.getKey())
              ? volumeEntry.getKey() + '-' + pod.getMetadata().getName()
              : volumeEntry.getKey();
      final PersistentVolumeClaim pvc;
      // checks whether PVC for given workspace and volume exists on remote
      if (existingVolumeName2PVC.containsKey(volumeName)) {
        pvc = existingVolumeName2PVC.get(volumeName);
      }
      // checks whether PVC for given volume provisioned previously
      else if (provisionedVolumeName2PVC.containsKey(volumeName)) {
        pvc = provisionedVolumeName2PVC.get(volumeName);
      }
      // when no existing and provisioned PVC found then create new one
      else {
        final String uniqueName = Names.generateName(pvcNamePrefix + '-');
        pvc = newPVC(uniqueName, pvcAccessMode, pvcQuantity);
        putLabel(pvc, CHE_WORKSPACE_ID_LABEL, workspaceId);
        putLabel(pvc, CHE_VOLUME_NAME_LABEL, volumeName);
        provisionedClaims.put(uniqueName, pvc);
      }

      // binds pvc to pod and container
      container
          .getVolumeMounts()
          .add(
              newVolumeMount(
                  pvc.getMetadata().getName(),
                  volumePath,
                  getSubPath(workspaceId, volumeName, Names.machineName(pod, container))));
      addVolumeIfAbsent(pod.getSpec(), pvc.getMetadata().getName());
    }
  }

  @Override
  public void cleanup(Workspace workspace) throws InfrastructureException {
    if (EphemeralWorkspaceUtility.isEphemeral(workspace)) {
      return;
    }
    String workspaceId = workspace.getId();
    factory
        .create(workspaceId)
        .persistentVolumeClaims()
        .delete(ImmutableMap.of(CHE_WORKSPACE_ID_LABEL, workspaceId));
  }

  private void addVolumeIfAbsent(PodSpec podSpec, String pvcUniqueName) {
    if (podSpec.getVolumes().stream().noneMatch(volume -> volume.getName().equals(pvcUniqueName))) {
      podSpec.getVolumes().add(newVolume(pvcUniqueName, pvcUniqueName));
    }
  }

  private String getSubPath(String workspaceId, String volumeName, String machineName) {
    // logs must be located inside the folder related to the machine because few machines can
    // contain the identical agents and in this case, a conflict is possible.
    if (LOGS_VOLUME_NAME.equals(volumeName)) {
      return workspaceId + '/' + volumeName + '/' + machineName;
    }
    return workspaceId + '/' + volumeName;
  }

  /** Groups list of given PVCs by volume name */
  private Map<String, PersistentVolumeClaim> groupByVolumeName(
      Collection<PersistentVolumeClaim> pvcs) throws InfrastructureException {
    final Map<String, PersistentVolumeClaim> grouped = new HashMap<>();
    for (PersistentVolumeClaim pvc : pvcs) {
      final ObjectMeta metadata = pvc.getMetadata();
      final String volumeName;
      if (metadata != null
          && metadata.getLabels() != null
          && (volumeName = metadata.getLabels().get(CHE_VOLUME_NAME_LABEL)) != null) {
        grouped.put(volumeName, pvc);
      }
    }
    return grouped;
  }
}
