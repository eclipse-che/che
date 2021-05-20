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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace.pvc;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesObjectUtil.newPVC;

import com.google.inject.Inject;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.VolumeMount;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesPersistentVolumeClaims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides common PVC for all workspaces in one Kubernetes namespace.
 *
 * <p>This strategy indirectly affects the workspace limits.<br>
 * The number of workspaces that can simultaneously store backups in one PV is limited only by the
 * storage capacity. The number of workspaces that can be running simultaneously depends on access
 * mode configuration and Che configuration limits.
 *
 * <p><b>Used subpaths:</b>
 *
 * <p>This strategy uses subpaths for resolving backed up data paths collisions.<br>
 * Subpaths have the following format: '{workspaceId}/{volumeName}'.<br>
 * Note that logs volume has the special format: '{workspaceId}/{volumeName}/{machineName}'. It is
 * done in this way to avoid conflicts e.g. two identical agents inside different machines produce
 * the same log file.
 *
 * <p><b>How user-defined PVCs are processed:</b>
 *
 * <p>How user-defined PVCs are processed: User-defined PVCs are removed from environment. Pods
 * volumes that reference PVCs are replaced with volume that references common PVC. The
 * corresponding containers volume mounts are relinked to common volume and subpaths are prefixed
 * with `'{workspaceId}/{originalPVCName}'`.
 *
 * <p>User-defined PVC name is used as Che Volume name. It means that if Machine is configured to
 * use Che Volume with the same name as user-defined PVC has then they will use the same shared
 * folder in common PVC.
 *
 * <p>Note that quantity and access mode of user-defined PVCs are ignored since common PVC is used
 * and it has preconfigured configuration.
 *
 * @author Anton Korneta
 * @author Alexander Garagatyi
 */
public class CommonPVCStrategy implements WorkspaceVolumesStrategy {

  // use non-static variable to reuse child class logger
  private final Logger log = LoggerFactory.getLogger(getClass());

  public static final String COMMON_STRATEGY = "common";

  /**
   * The additional property name with the wildcard reserved for workspace id. Formatted property
   * with the real workspace id is used to get workspace subpaths directories. The value of this
   * property represents the String array of subpaths that are used to create folders in PV with
   * user rights. Note that the value would not be stored and it is removed before PVC creation.
   */
  static final String SUBPATHS_PROPERTY_FMT = "che.workspace.%s.subpaths";

  private final boolean preCreateDirs;
  private final String pvcQuantity;
  private final String configuredPVCName;
  private final String pvcAccessMode;
  private final String pvcStorageClassName;
  private final PVCSubPathHelper pvcSubPathHelper;
  private final KubernetesNamespaceFactory factory;
  private final EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter;
  private final PVCProvisioner pvcProvisioner;
  private final PodsVolumes podsVolumes;
  private final SubPathPrefixes subpathPrefixes;
  private final boolean waitBound;

  @Inject
  public CommonPVCStrategy(
      @Named("che.infra.kubernetes.pvc.name") String configuredPVCName,
      @Named("che.infra.kubernetes.pvc.quantity") String pvcQuantity,
      @Named("che.infra.kubernetes.pvc.access_mode") String pvcAccessMode,
      @Named("che.infra.kubernetes.pvc.precreate_subpaths") boolean preCreateDirs,
      @Named("che.infra.kubernetes.pvc.storage_class_name") String pvcStorageClassName,
      @Named("che.infra.kubernetes.pvc.wait_bound") boolean waitBound,
      PVCSubPathHelper pvcSubPathHelper,
      KubernetesNamespaceFactory factory,
      EphemeralWorkspaceAdapter ephemeralWorkspaceAdapter,
      PVCProvisioner pvcProvisioner,
      PodsVolumes podsVolumes,
      SubPathPrefixes subpathPrefixes) {
    this.configuredPVCName = configuredPVCName;
    this.pvcQuantity = pvcQuantity;
    this.pvcAccessMode = pvcAccessMode;
    this.preCreateDirs = preCreateDirs;
    this.pvcStorageClassName = pvcStorageClassName;
    this.waitBound = waitBound;
    this.pvcSubPathHelper = pvcSubPathHelper;
    this.factory = factory;
    this.ephemeralWorkspaceAdapter = ephemeralWorkspaceAdapter;
    this.pvcProvisioner = pvcProvisioner;
    this.podsVolumes = podsVolumes;
    this.subpathPrefixes = subpathPrefixes;
  }

  /**
   * Creates new instance of PVC object that should be used for the specified workspace.
   *
   * <p>May be overridden by child class for changing common scope. Like common per user or common
   * per workspace.
   *
   * @param workspaceId workspace that needs PVC
   * @return pvc that should be used for the specified runtime identity
   */
  protected PersistentVolumeClaim createCommonPVC(String workspaceId) {
    return newPVC(configuredPVCName, pvcAccessMode, pvcQuantity, pvcStorageClassName);
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    final String workspaceId = identity.getWorkspaceId();
    if (EphemeralWorkspaceUtility.isEphemeral(k8sEnv.getAttributes())) {
      ephemeralWorkspaceAdapter.provision(k8sEnv, identity);
      return;
    }
    log.debug("Provisioning PVC strategy for workspace '{}'", workspaceId);

    pvcProvisioner.convertCheVolumes(k8sEnv, workspaceId);

    // Note that PVC name is used during prefixing
    // It MUST be done before changing all PVCs references to common PVC
    subpathPrefixes.prefixVolumeMountsSubpaths(k8sEnv, identity.getWorkspaceId());

    PersistentVolumeClaim commonPVC = replacePVCsWithCommon(k8sEnv, identity);

    podsVolumes.replacePVCVolumesWithCommon(
        k8sEnv.getPodsData(), commonPVC.getMetadata().getName());

    if (preCreateDirs) {
      Set<String> subPaths = combineVolumeMountsSubpaths(k8sEnv);
      if (!subPaths.isEmpty()) {
        commonPVC.setAdditionalProperty(
            format(SUBPATHS_PROPERTY_FMT, workspaceId),
            subPaths.toArray(new String[subPaths.size()]));
      }
    }
    log.debug("PVC strategy provisioning done for workspace '{}'", workspaceId);
  }

  @Override
  @Traced
  public void prepare(
      KubernetesEnvironment k8sEnv,
      RuntimeIdentity identity,
      long timeoutMillis,
      Map<String, String> startOptions)
      throws InfrastructureException {
    String workspaceId = identity.getWorkspaceId();

    TracingTags.WORKSPACE_ID.set(workspaceId);

    if (EphemeralWorkspaceUtility.isEphemeral(k8sEnv.getAttributes())) {
      return;
    }

    log.debug("Preparing PVC started for workspace '{}'", workspaceId);

    Map<String, PersistentVolumeClaim> claims = k8sEnv.getPersistentVolumeClaims();
    if (claims.isEmpty()) {
      return;
    }
    if (claims.size() > 1) {
      throw new InfrastructureException(
          format(
              "The only one PVC MUST be present in common strategy while it contains: %s.",
              claims.keySet().stream().collect(joining(", "))));
    }

    PersistentVolumeClaim commonPVC = claims.values().iterator().next();

    final KubernetesNamespace namespace = factory.getOrCreate(identity);
    final KubernetesPersistentVolumeClaims pvcs = namespace.persistentVolumeClaims();
    final Set<String> existing =
        pvcs.get().stream().map(p -> p.getMetadata().getName()).collect(toSet());
    if (!existing.contains(commonPVC.getMetadata().getName())) {
      log.debug("Creating PVC for workspace '{}'", workspaceId);
      pvcs.create(commonPVC);
      if (waitBound) {
        log.debug("Waiting for PVC for workspace '{}' to be bound", workspaceId);
        pvcs.waitBound(commonPVC.getMetadata().getName(), timeoutMillis);
      }
    }

    final String[] subpaths =
        (String[])
            commonPVC.getAdditionalProperties().remove(format(SUBPATHS_PROPERTY_FMT, workspaceId));
    if (preCreateDirs && subpaths != null) {
      pvcSubPathHelper.createDirs(
          identity, workspaceId, commonPVC.getMetadata().getName(), startOptions, subpaths);
    }

    log.debug("Preparing PVC done for workspace '{}'", workspaceId);
  }

  @Override
  public void cleanup(Workspace workspace) throws InfrastructureException {
    if (EphemeralWorkspaceUtility.isEphemeral(workspace)) {
      return;
    }
    String workspaceId = workspace.getId();
    PersistentVolumeClaim pvc = createCommonPVC(workspaceId);
    pvcSubPathHelper.removeDirsAsync(
        workspaceId,
        factory.get(workspace).getName(),
        pvc.getMetadata().getName(),
        subpathPrefixes.getWorkspaceSubPath(workspaceId));
  }

  private PersistentVolumeClaim replacePVCsWithCommon(
      KubernetesEnvironment k8sEnv, RuntimeIdentity identity) {
    final PersistentVolumeClaim commonPVC = createCommonPVC(identity.getWorkspaceId());
    k8sEnv.getPersistentVolumeClaims().clear();
    k8sEnv.getPersistentVolumeClaims().put(commonPVC.getMetadata().getName(), commonPVC);
    return commonPVC;
  }

  private Set<String> combineVolumeMountsSubpaths(KubernetesEnvironment k8sEnv) {
    return k8sEnv
        .getPodsData()
        .values()
        .stream()
        .flatMap(p -> p.getSpec().getContainers().stream())
        .flatMap(c -> c.getVolumeMounts().stream())
        .map(VolumeMount::getSubPath)
        .filter(subpath -> !isNullOrEmpty(subpath))
        .collect(Collectors.toSet());
  }
}
