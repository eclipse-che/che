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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
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
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks if config map with self-signed certificates are present in current namespace, and if it
 * is, mounts it's data items as a files into specific folder on all containers of the workspace
 * being provisioned.
 */
@Singleton
public class TrustStoreCertificateProvisioner
    implements NamespaceableConfigurationProvisioner<KubernetesEnvironment> {

  private static final Logger LOG = LoggerFactory.getLogger(TrustStoreCertificateProvisioner.class);

  public static final String CHE_TRUST_STORE_VOLUME = "che-self-signed-certs";

  private final String certificateMountPath;
  private final String configMapName;
  private final CheInstallationLocation cheInstallationLocation;
  private final KubernetesClientFactory clientFactory;

  @Inject
  public TrustStoreCertificateProvisioner(
      @Named("che.truststore_configmap_name") String configMapName,
      @Named("che.truststore_mount_path") String certificateMountPath,
      CheInstallationLocation cheInstallationLocation,
      KubernetesClientFactory clientFactory) {
    this.configMapName = configMapName;
    this.cheInstallationLocation = cheInstallationLocation;
    this.clientFactory = clientFactory;
    this.certificateMountPath = certificateMountPath;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    provision(k8sEnv, identity, null);
  }

  @Override
  public void provision(
      KubernetesEnvironment k8sEnv, RuntimeIdentity identity, KubernetesNamespace namespace)
      throws InfrastructureException {
    if (namespace == null) {
      return;
    }
    ConfigMap configMap =
        clientFactory
            .create()
            .configMaps()
            .inNamespace(cheInstallationLocation.getInstallationLocationNamespace())
            .withName(configMapName)
            .get();

    if (configMap == null) {
      return;
    }

    ConfigMap existing = namespace.configMaps().get(configMapName);
    if (existing == null || !existing.getData().equals(configMap.getData())) {
      // create or renew map
      namespace
          .configMaps()
          .create(
              new ConfigMapBuilder()
                  .withMetadata(
                      new ObjectMetaBuilder()
                          .withName(configMapName)
                          .withAnnotations(configMap.getMetadata().getAnnotations())
                          .build())
                  .withApiVersion(configMap.getApiVersion())
                  .withData(configMap.getData())
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
