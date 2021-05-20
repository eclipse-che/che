/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
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

import io.fabric8.kubernetes.api.model.EmptyDirVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;
import io.fabric8.kubernetes.api.model.PodSpec;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
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

  @Inject
  public EphemeralWorkspaceAdapter(PVCProvisioner pvcProvisioner, SubPathPrefixes subPathPrefixes) {
    this.pvcProvisioner = pvcProvisioner;
    this.subPathPrefixes = subPathPrefixes;
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
