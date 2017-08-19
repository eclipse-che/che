/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.openshift.provision.volume;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Quantity;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.Utils;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;
import org.eclipse.che.workspace.infrastructure.openshift.provision.ConfigurationProvisioner;

/**
 * Provides persistent volume claim into OpenShift environment.
 *
 * @author Anton Korneta
 */
@Singleton
public class PersistentVolumeClaimProvisioner implements ConfigurationProvisioner {

  private final boolean pvcEnable;
  private final String pvcName;
  private final String pvcQuantity;
  private final String pvcAccessMode;
  private final String projectFolderPath;

  @Inject
  public PersistentVolumeClaimProvisioner(
      @Named("che.infra.openshift.pvc.enabled") boolean pvcEnable,
      @Named("che.infra.openshift.pvc.name") String pvcName,
      @Named("che.infra.openshift.pvc.quantity") String pvcQuantity,
      @Named("che.infra.openshift.pvc.access_mode") String pvcAccessMode,
      @Named("che.workspace.projects.storage") String projectFolderPath) {
    this.pvcEnable = pvcEnable;
    this.pvcName = pvcName;
    this.pvcQuantity = pvcQuantity;
    this.pvcAccessMode = pvcAccessMode;
    this.projectFolderPath = projectFolderPath;
  }

  @Override
  public void provision(
      EnvironmentImpl environment, OpenShiftEnvironment osEnv, RuntimeIdentity runtimeIdentity)
      throws InfrastructureException {
    if (pvcEnable) {
      osEnv
          .getPersistentVolumeClaims()
          .put(
              pvcName,
              new PersistentVolumeClaimBuilder()
                  .withNewMetadata()
                  .withName(pvcName)
                  .endMetadata()
                  .withNewSpec()
                  .withAccessModes(pvcAccessMode)
                  .withNewResources()
                  .withRequests(ImmutableMap.of("storage", new Quantity(pvcQuantity)))
                  .endResources()
                  .endSpec()
                  .build());
      final String devMachineName = Utils.getDevMachineName(environment);
      for (Pod pod : osEnv.getPods().values()) {
        for (Container container : pod.getSpec().getContainers()) {
          final String machineName = pod.getMetadata().getName() + "/" + container.getName();
          if (devMachineName != null && devMachineName.equals(machineName)) {
            final VolumeMount volumeMount =
                new VolumeMountBuilder()
                    .withMountPath(projectFolderPath)
                    .withName(pvcName)
                    .withSubPath(runtimeIdentity.getWorkspaceId() + projectFolderPath)
                    .build();
            container.getVolumeMounts().add(volumeMount);
            final PersistentVolumeClaimVolumeSource pvcs =
                new PersistentVolumeClaimVolumeSourceBuilder().withClaimName(pvcName).build();
            final Volume volume =
                new VolumeBuilder().withPersistentVolumeClaim(pvcs).withName(pvcName).build();
            pod.getSpec().getVolumes().add(volume);
            return;
          }
        }
      }
    }
  }
}
