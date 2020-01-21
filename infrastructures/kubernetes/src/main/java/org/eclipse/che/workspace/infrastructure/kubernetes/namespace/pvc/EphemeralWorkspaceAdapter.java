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

import static org.eclipse.che.api.workspace.shared.Constants.CHE_WORKSPACE_PERSIST_VOLUMES_PROPERTY;
import static org.eclipse.che.api.workspace.shared.Constants.PERSIST_VOLUMES_ATTRIBUTE;

import io.fabric8.kubernetes.api.model.EmptyDirVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to create ephemeral workspaces (with no PVC attached) based on workspace config
 * `persistVolumes` attribute. If `persistVolumes` attribute is set to false, workspace volumes
 * would be created as `emptyDir` regardless of the PVC strategy. User-defined PVCs will be removed
 * from environment and the corresponding PVC volumes in Pods will be replaced with `emptyDir`
 * volumes. When a workspace Pod is removed for any reason, the data in the `emptyDir` volume is
 * deleted forever.
 *
 * @see <a href="https://kubernetes.io/docs/concepts/storage/volumes/#emptydir">emptyDir</a>
 * @author Ilya Buziuk
 * @author Angel Misevski
 */
@Singleton
public class EphemeralWorkspaceAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(CommonPVCStrategy.class);

  private final PVCProvisioner pvcProvisioner;
  private final SubPathPrefixes subPathPrefixes;
  private final boolean defaultPersistVolumes;

  @Inject
  public EphemeralWorkspaceAdapter(
      PVCProvisioner pvcProvisioner,
      SubPathPrefixes subPathPrefixes,
      @Named(CHE_WORKSPACE_PERSIST_VOLUMES_PROPERTY) boolean defaultPersistVolumes) {
    this.pvcProvisioner = pvcProvisioner;
    this.subPathPrefixes = subPathPrefixes;
    this.defaultPersistVolumes = defaultPersistVolumes;
  }

  /**
   * @param workspaceAttributes workspace config or devfile attributes to check is ephemeral mode is
   *     enabled
   * @return true if `persistVolumes` attribute exists and set to 'false'. In this case regardless
   *     of the PVC strategy, workspace volumes would be created as `emptyDir`. When a workspace Pod
   *     is removed for any reason, the data in the `emptyDir` volume is deleted forever
   */
  public boolean isEphemeral(Map<String, String> workspaceAttributes) {
    String persistVolumes = workspaceAttributes.get(PERSIST_VOLUMES_ATTRIBUTE);
    if (persistVolumes == null) {
      return !defaultPersistVolumes;
    } else {
      return !"true".equals(persistVolumes);
    }
  }

  /**
   * @param workspace workspace to check is ephemeral mode is enabled
   * @return true if workspace config contains `persistVolumes` attribute which is set to false. In
   *     this case regardless of the PVC strategy, workspace volumes would be created as `emptyDir`.
   *     When a workspace Pod is removed for any reason, the data in the `emptyDir` volume is
   *     deleted forever
   */
  public boolean isEphemeral(Workspace workspace) {
    Devfile devfile = workspace.getDevfile();
    if (devfile != null) {
      return isEphemeral(devfile.getAttributes());
    }

    return isEphemeral(workspace.getConfig().getAttributes());
  }

  /**
   * Change workspace attributes such that future calls to {@link #isEphemeral(Map)} will return
   * true.
   *
   * @param workspaceAttributes workspace config or devfile attributes to which ephemeral mode
   *     configuration should be provisioned
   */
  public void makeEphemeral(Map<String, String> workspaceAttributes) {
    workspaceAttributes.put(PERSIST_VOLUMES_ATTRIBUTE, "false");
  }

  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    LOG.debug("Provisioning PVC strategy for workspace '{}'", identity.getWorkspaceId());

    Map<String, PersistentVolumeClaim> userDefinedPVCs =
        new HashMap<>(k8sEnv.getPersistentVolumeClaims());

    k8sEnv.getPersistentVolumeClaims().clear();
    pvcProvisioner.provision(k8sEnv, userDefinedPVCs);
    pvcProvisioner.convertCheVolumes(k8sEnv, identity.getWorkspaceId());
    subPathPrefixes.prefixVolumeMountsSubpaths(k8sEnv, identity.getWorkspaceId());

    replacePVCsWithEmptyDir(k8sEnv);
    k8sEnv.getPersistentVolumeClaims().clear();
  }

  private void replacePVCsWithEmptyDir(KubernetesEnvironment k8sEnv) {
    for (PodData pod : k8sEnv.getPodsData().values()) {
      PodSpec podSpec = pod.getSpec();
      podSpec
          .getVolumes()
          .stream()
          .filter(v -> v.getPersistentVolumeClaim() != null)
          .forEach(
              v -> {
                v.setPersistentVolumeClaim(null);
                v.setEmptyDir(new EmptyDirVolumeSource());
              });
    }
  }
}
