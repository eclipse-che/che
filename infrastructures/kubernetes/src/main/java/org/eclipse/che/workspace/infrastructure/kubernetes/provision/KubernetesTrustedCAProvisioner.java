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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision;

import static com.google.common.base.Strings.isNullOrEmpty;

import com.google.common.base.Splitter;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.CheServerKubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.CheInstallationLocation;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodRole;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;

/**
 * Checks if config maps with CA bundles is configured by specific property. If they are, then
 * creates single config map for all ca bundles in workspace namespace and mounts it into pods.
 */
@Singleton
public class KubernetesTrustedCAProvisioner implements TrustedCAProvisioner {

  public static final String CHE_TRUST_STORE_VOLUME = "che-ca-certs";

  private final String certificateMountPath;
  private final boolean trustedStoreInitialized;
  private final String caBundleConfigMap;
  private final String configMapName;
  private final CheServerKubernetesClientFactory cheServerClientFactory;
  private final String installationLocationNamespace;
  private final KubernetesNamespaceFactory namespaceFactory;
  private final Map<String, String> configMapLabelKeyValue;

  @Inject
  public KubernetesTrustedCAProvisioner(
      @Nullable @Named("che.infra.kubernetes.trusted_ca.src_configmap") String caBundleConfigMap,
      @Named("che.infra.kubernetes.trusted_ca.dest_configmap") String configMapName,
      @Named("che.infra.kubernetes.trusted_ca.mount_path") String certificateMountPath,
      @Nullable @Named("che.infra.kubernetes.trusted_ca.dest_configmap_labels")
          String configMapLabels,
      CheInstallationLocation cheInstallationLocation,
      KubernetesNamespaceFactory namespaceFactory,
      CheServerKubernetesClientFactory cheServerClientFactory)
      throws InfrastructureException {
    this.cheServerClientFactory = cheServerClientFactory;
    this.trustedStoreInitialized = !isNullOrEmpty(caBundleConfigMap);
    this.configMapName = configMapName;
    this.caBundleConfigMap = caBundleConfigMap;
    this.certificateMountPath = certificateMountPath;
    this.installationLocationNamespace = cheInstallationLocation.getInstallationLocationNamespace();
    this.namespaceFactory = namespaceFactory;

    if (configMapLabels != null && !configMapLabels.trim().equals("")) {
      this.configMapLabelKeyValue =
          Splitter.on(",").withKeyValueSeparator("=").split(configMapLabels);
    } else {
      this.configMapLabelKeyValue = new HashMap<>();
    }
  }

  public boolean isTrustedStoreInitialized() {
    return trustedStoreInitialized;
  }

  /**
   * Propagates additional CA certificates into config map and mounts them into all pods of given
   * namespace
   *
   * @param k8sEnv available objects in the scope
   * @param runtimeID defines namespace into which config map should be provisioned
   * @throws InfrastructureException if failed to CRUD a resource
   */
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity runtimeID)
      throws InfrastructureException {
    if (!trustedStoreInitialized) {
      return;
    }

    ConfigMap allCaCertsConfigMap =
        cheServerClientFactory
            .create()
            .configMaps()
            .inNamespace(installationLocationNamespace)
            .withName(caBundleConfigMap)
            .get();
    if (allCaCertsConfigMap == null) {
      return;
    }

    k8sEnv
        .getConfigMaps()
        .put(
            configMapName,
            new ConfigMapBuilder()
                .withMetadata(
                    new ObjectMetaBuilder()
                        .withName(configMapName)
                        .withAnnotations(allCaCertsConfigMap.getMetadata().getAnnotations())
                        .withLabels(configMapLabelKeyValue)
                        .build())
                .withApiVersion(allCaCertsConfigMap.getApiVersion())
                .withData(allCaCertsConfigMap.getData())
                .build());

    for (PodData pod : k8sEnv.getPodsData().values()) {
      if (pod.getRole() == PodRole.DEPLOYMENT) {
        if (pod.getSpec()
            .getVolumes()
            .stream()
            .noneMatch(v -> v.getName().equals(CHE_TRUST_STORE_VOLUME))) {
          pod.getSpec()
              .getVolumes()
              .add(
                  new VolumeBuilder()
                      .withName(CHE_TRUST_STORE_VOLUME)
                      .withConfigMap(
                          new ConfigMapVolumeSourceBuilder().withName(configMapName).build())
                      .build());
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
      container
          .getVolumeMounts()
          .add(
              new VolumeMountBuilder()
                  .withName(CHE_TRUST_STORE_VOLUME)
                  .withNewReadOnly(true)
                  .withMountPath(certificateMountPath)
                  .build());
    }
  }
}
