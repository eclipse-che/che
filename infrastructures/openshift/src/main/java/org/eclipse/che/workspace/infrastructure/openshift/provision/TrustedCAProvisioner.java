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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import com.google.common.base.Splitter;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.CheInstallationLocation;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodRole;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks if config map with self-signed certificates are present in current namespace, and if it
 * is, creates the same in workspace project, allowing .
 */
@Singleton
public class TrustedCAProvisioner {

  private static final Logger LOG = LoggerFactory.getLogger(TrustedCAProvisioner.class);

  public static final String CHE_TRUST_STORE_VOLUME = "che-self-signed-certs";

  private final String certificateMountPath;
  private final String configMapName;
  private final Map<String, String> configMapLabelKeyValue;
  private final CheInstallationLocation cheInstallationLocation;
  private final KubernetesClientFactory clientFactory;

  @Inject
  public TrustedCAProvisioner(
      @Named("che.trusted_ca_bundles_config_map") String configMapName,
      @Named("che.trusted_ca_bundles_config_map_labels") String configMapLabel,
      @Named("che.trusted_ca_bundles_mount_path") String certificateMountPath,
      CheInstallationLocation cheInstallationLocation,
      KubernetesClientFactory clientFactory) {
    this.configMapName = configMapName;
    this.cheInstallationLocation = cheInstallationLocation;
    this.clientFactory = clientFactory;
    this.certificateMountPath = certificateMountPath;
    this.configMapLabelKeyValue = Splitter.on(",").withKeyValueSeparator("=").split(configMapLabel);
  }

  public void provision(
      KubernetesEnvironment k8sEnv, RuntimeIdentity identity, OpenShiftProject project)
      throws InfrastructureException {
    ConfigMapList configMapList =
        clientFactory
            .create()
            .configMaps()
            .inNamespace(cheInstallationLocation.getInstallationLocationNamespace())
            .withLabels(configMapLabelKeyValue)
            .list();

    if (configMapList.getItems().isEmpty()) {
      return;
    }

    ConfigMap existing = project.configMaps().get(configMapName);
    if (existing == null) {
      // create new map
      project
          .configMaps()
          .create(
              new ConfigMapBuilder()
                  .withMetadata(
                      new ObjectMetaBuilder()
                          .withName(configMapName)
                          .withLabels(configMapLabelKeyValue)
                          .build())
                  .build());
    }

    for (PodData pod : k8sEnv.getPodsData().values()) {
      if (pod.getRole() == PodRole.DEPLOYMENT) {
        if (pod.getSpec()
            .getVolumes()
            .stream()
            .noneMatch(v -> v.getName().equals(CHE_TRUST_STORE_VOLUME))) {
          pod.getSpec().getVolumes().add(buildTrustStoreConfigMapVolume());
        }
      }

      for (Container container : pod.getSpec().getInitContainers()) {
        provisionTrustStoreVolumeMountIfNeeded(container);
      }
      for (Container container : pod.getSpec().getContainers()) {
        provisionTrustStoreVolumeMountIfNeeded(container);
      }
    }
  }

  private void provisionTrustStoreVolumeMountIfNeeded(Container container) {
    if (container
        .getVolumeMounts()
        .stream()
        .noneMatch(vm -> vm.getName().equals(CHE_TRUST_STORE_VOLUME))) {
      container.getVolumeMounts().add(buildTrustStoreVolumeMount());
    }
  }

  private VolumeMount buildTrustStoreVolumeMount() {
    return new VolumeMountBuilder()
        .withName(CHE_TRUST_STORE_VOLUME)
        .withNewReadOnly(true)
        .withMountPath(certificateMountPath)
        .build();
  }

  private Volume buildTrustStoreConfigMapVolume() {
    return new VolumeBuilder()
        .withName(CHE_TRUST_STORE_VOLUME)
        .withConfigMap(new ConfigMapVolumeSourceBuilder().withName(configMapName).build())
        .build();
  }
}
