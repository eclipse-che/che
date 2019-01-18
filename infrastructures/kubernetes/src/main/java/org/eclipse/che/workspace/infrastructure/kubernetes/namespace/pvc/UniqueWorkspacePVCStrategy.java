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

import static java.util.stream.Collectors.toMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_VOLUME_NAME_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.Constants.CHE_WORKSPACE_ID_LABEL;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newPVC;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolume;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newVolumeMount;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.putLabel;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.LogsVolumeMachineProvisioner.LOGS_VOLUME_NAME;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.VolumeMount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.config.Volume;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.environment.InternalMachineConfig;
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
 * <p>Note that for in this strategy number of simultaneously used volumes by workspaces can not be
 * greater than the number of available PVCs in Kubernetes namespace.
 *
 * <p><b>Used subpaths:</b>
 *
 * <p>This strategy uses subpaths to do the same as {@link CommonPVCStrategy} does and make easier
 * data migration if it will be needed.<br>
 * Subpaths have the following format: '{workspaceId}/{volume/PVC name}'.<br>
 * Note that logs volume has the special format: '{workspaceId}/{volumeName}/{machineName}'. It is
 * done in this way to avoid conflicts e.g. two identical agents inside different machines produce
 * the same log file.
 *
 * <p><b>How user-defined PVCs are processed:</b>
 *
 * <p>User-defined PVCs are provisioned with generated unique names. Pods volumes that reference
 * PVCs are updated accordingly. Subpaths of the corresponding containers volume mounts are prefixed
 * with `'{workspaceId}/{originalPVCName}'`.
 *
 * <p>User-defined PVC name is used as Che Volume name. It means that if Machine is configured to
 * use Che Volume with the same name as user-defined PVC has then Che Volume will reuse user-defined
 * PVC.
 *
 * <p>Note that quantity and access mode of user-defined PVCs are not overridden with Che Server
 * configured.
 *
 * <p><b>Clean up:</b>
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

  // property of PersistentVolumeClaim#getAdditionalProperties that indicates that PVC is
  // provisioned by Che Server but is not user-defined
  private static final String CHE_PROVISIONED_PVC_PROPERTY = "CHE_PROVISIONED";

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

    fillInExistingPVCs(k8sEnv, workspaceId);

    // fetches all existing PVCs related to given workspace and groups them by volume name
    final Map<String, PersistentVolumeClaim> volumeName2PVC =
        groupByVolumeName(k8sEnv.getPersistentVolumeClaims().values());

    processUserDefinedPVCs(k8sEnv, identity, workspaceId, volumeName2PVC);

    provisionCheVolumes(k8sEnv, workspaceId, volumeName2PVC);

    LOG.debug("PVC strategy provisioning done for workspace '{}'", workspaceId);
  }

  @Traced
  @Override
  public void prepare(KubernetesEnvironment k8sEnv, String workspaceId, long timeoutMillis)
      throws InfrastructureException {
    TracingTags.WORKSPACE_ID.set(workspaceId);

    if (EphemeralWorkspaceUtility.isEphemeral(k8sEnv.getAttributes())) {
      return;
    }

    if (k8sEnv.getPersistentVolumeClaims().isEmpty()) {
      // no PVCs to prepare
      return;
    }

    final KubernetesPersistentVolumeClaims k8sClaims =
        factory.create(workspaceId).persistentVolumeClaims();
    LOG.debug("Creating PVCs for workspace '{}'", workspaceId);
    k8sClaims.createIfNotExist(k8sEnv.getPersistentVolumeClaims().values());

    LOG.debug("Waiting PVCs for workspace '{}' to be bound", workspaceId);
    for (PersistentVolumeClaim pvc : k8sEnv.getPersistentVolumeClaims().values()) {
      k8sClaims.waitBound(pvc.getMetadata().getName(), timeoutMillis);
    }
    LOG.debug("Preparing PVCs done for workspace '{}'", workspaceId);
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

  private void fillInExistingPVCs(KubernetesEnvironment k8sEnv, String workspaceId)
      throws InfrastructureException {
    Map<String, PersistentVolumeClaim> existingPVCs =
        factory
            .create(workspaceId)
            .persistentVolumeClaims()
            .getByLabel(CHE_WORKSPACE_ID_LABEL, workspaceId)
            .stream()
            .peek(pvc -> pvc.getAdditionalProperties().put(CHE_PROVISIONED_PVC_PROPERTY, true))
            .collect(toMap(pvc -> pvc.getMetadata().getName(), Function.identity()));

    k8sEnv.getPersistentVolumeClaims().putAll(existingPVCs);
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
      if (metadata != null
          && metadata.getLabels() != null
          && (volumeName = metadata.getLabels().get(CHE_VOLUME_NAME_LABEL)) != null) {
        grouped.put(volumeName, pvc);
      }
    }
    return grouped;
  }

  /**
   * Operations that are done in this method are described in java doc of {@link
   * UniqueWorkspacePVCStrategy}.
   */
  private void processUserDefinedPVCs(
      KubernetesEnvironment k8sEnv,
      RuntimeIdentity identity,
      String workspaceId,
      Map<String, PersistentVolumeClaim> volumeName2PVC) {
    // process user-defined PVCs according to unique strategy
    final Map<String, PersistentVolumeClaim> envClaims = k8sEnv.getPersistentVolumeClaims();
    Map<String, PersistentVolumeClaim> userDefinedPVCs =
        envClaims
            .values()
            .stream()
            .filter(
                p -> {
                  Object isProvisioned =
                      p.getAdditionalProperties().get(CHE_PROVISIONED_PVC_PROPERTY);
                  return !(isProvisioned instanceof Boolean) || !(Boolean) isProvisioned;
                })
            .collect(toMap(pvc -> pvc.getMetadata().getName(), Function.identity()));

    prefixSubpaths(userDefinedPVCs.keySet(), k8sEnv.getPodsData(), identity.getWorkspaceId());

    for (PersistentVolumeClaim pvc : userDefinedPVCs.values()) {
      String originalPVCName = pvc.getMetadata().getName();

      PersistentVolumeClaim existingPVC = volumeName2PVC.get(originalPVCName);

      if (existingPVC != null) {
        // Replace pvc in environment with existing. Fix the references in Pods
        envClaims.remove(originalPVCName);
        changePVCReferences(
            k8sEnv.getPodsData(), originalPVCName, existingPVC.getMetadata().getName());
      } else {
        // there is no the corresponding existing pvc
        // new one should be created with generated name
        putLabel(pvc, CHE_VOLUME_NAME_LABEL, originalPVCName);
        putLabel(pvc, CHE_WORKSPACE_ID_LABEL, workspaceId);

        final String uniqueName = Names.generateName(pvcNamePrefix + '-');
        pvc.getMetadata().setName(uniqueName);
        pvc.getAdditionalProperties().put(CHE_PROVISIONED_PVC_PROPERTY, true);
        envClaims.remove(originalPVCName);
        envClaims.put(uniqueName, pvc);

        volumeName2PVC.put(originalPVCName, pvc);
        changePVCReferences(k8sEnv.getPodsData(), originalPVCName, uniqueName);
      }
    }
  }

  /**
   * Prefixes user-defined subpath with `{workspace id} + {PVC name}` where PVC name is supposed to
   * be used as Che volume name.
   *
   * @param userDefinedPVCs set with user-defined PVCs names
   * @param pods pods to change subpaths
   * @param workspaceId workspace id to be used in subpath prefix
   */
  private void prefixSubpaths(
      Set<String> userDefinedPVCs, Map<String, PodData> pods, String workspaceId) {
    for (PodData pod : pods.values()) {
      Map<String, String> volumeToClaimName = new HashMap<>();

      for (io.fabric8.kubernetes.api.model.Volume volume : pod.getSpec().getVolumes()) {
        if (volume.getPersistentVolumeClaim() == null) {
          continue;
        }

        String claimName = volume.getPersistentVolumeClaim().getClaimName();
        if (userDefinedPVCs.contains(claimName)) {
          volumeToClaimName.put(volume.getName(), claimName);
        }
      }

      if (volumeToClaimName.isEmpty()) {
        continue;
      }

      Stream.concat(
              pod.getSpec().getContainers().stream(), pod.getSpec().getInitContainers().stream())
          .forEach(
              c -> {
                for (VolumeMount volumeMount : c.getVolumeMounts()) {
                  String claimName = volumeToClaimName.get(volumeMount.getName());
                  if (claimName == null) {
                    // claim is not user-defined. No need to prefix it
                    return;
                  }
                  String volumeSubPath =
                      getVolumeMountSubpath(
                          volumeMount, claimName, workspaceId, Names.machineName(pod, c));
                  volumeMount.setSubPath(volumeSubPath);
                }
              });
    }
  }

  private void changePVCReferences(Map<String, PodData> pods, String oldName, String newName) {
    pods.values()
        .stream()
        .flatMap(p -> p.getSpec().getVolumes().stream())
        .filter(
            v ->
                v.getPersistentVolumeClaim() != null
                    && v.getPersistentVolumeClaim().getClaimName().equals(oldName))
        .forEach(v -> v.getPersistentVolumeClaim().setClaimName(newName));
  }

  private void provisionCheVolumes(
      KubernetesEnvironment k8sEnv,
      String workspaceId,
      Map<String, PersistentVolumeClaim> volumeName2PVC) {
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
        final String uniqueName = Names.generateName(pvcNamePrefix + '-');
        pvc = newPVC(uniqueName, pvcAccessMode, pvcQuantity);
        putLabel(pvc, CHE_WORKSPACE_ID_LABEL, workspaceId);
        putLabel(pvc, CHE_VOLUME_NAME_LABEL, volumeName);
        k8sEnv.getPersistentVolumeClaims().put(uniqueName, pvc);
        volumeName2PVC.put(volumeName, pvc);
      }

      // binds pvc to pod and container
      String pvcUniqueName = pvc.getMetadata().getName();
      PodSpec podSpec = pod.getSpec();
      Optional<io.fabric8.kubernetes.api.model.Volume> volumeOpt =
          podSpec
              .getVolumes()
              .stream()
              .filter(
                  volume ->
                      volume.getPersistentVolumeClaim() != null
                          && pvcUniqueName.equals(volume.getPersistentVolumeClaim().getClaimName()))
              .findAny();
      io.fabric8.kubernetes.api.model.Volume podVolume;
      if (volumeOpt.isPresent()) {
        podVolume = volumeOpt.get();
      } else {
        podVolume = newVolume(pvcUniqueName, pvcUniqueName);
        podSpec.getVolumes().add(podVolume);
      }

      container
          .getVolumeMounts()
          .add(
              newVolumeMount(
                  podVolume.getName(),
                  volumePath,
                  getVolumeSubpath(workspaceId, volumeName, Names.machineName(pod, container))));
    }
  }

  /** Get sub-path for particular Volume Mount in a particular workspace */
  private String getVolumeMountSubpath(
      VolumeMount volumeMount, String volumeName, String workspaceId, String machineName) {
    String volumeMountSubPath = Strings.nullToEmpty(volumeMount.getSubPath());
    if (!volumeMountSubPath.startsWith("/")) {
      volumeMountSubPath = '/' + volumeMountSubPath;
    }

    return getVolumeSubpath(workspaceId, volumeName, machineName) + volumeMountSubPath;
  }

  private String getVolumeSubpath(String workspaceId, String volumeName, String machineName) {
    // logs must be located inside the folder related to the machine because few machines can
    // contain the identical agents and in this case, a conflict is possible.
    if (LOGS_VOLUME_NAME.equals(volumeName)) {
      return workspaceId + '/' + volumeName + '/' + machineName;
    }
    return workspaceId + '/' + volumeName;
  }
}
