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
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodRole;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftCheInstallationLocation;
import org.eclipse.che.workspace.infrastructure.openshift.project.OpenShiftProject;

/**
 * Checks if config map with CA bundles are present in current namespace, and if it is, creates the
 * same in workspace project, allowing openshift to auto-inject values into it. (see
 * https://docs.openshift.com/container-platform/4.3/networking/configuring-a-custom-pki.html#certificate-injection-using-operators_configuring-a-custom-pki)
 */
@Singleton
public class Openshift4TrustedCAProvisioner {

  public static final String CHE_TRUST_STORE_VOLUME = "che-self-signed-certs";

  private final String certificateMountPath;
  private final boolean trustedStoreInitialized;
  private final String configMapName;
  private final Map<String, String> configMapLabelKeyValue;

  @Inject
  public Openshift4TrustedCAProvisioner(
      @Named("che.infra.openshift.trusted_ca_bundles_config_map") String configMapName,
      @Named("che.infra.openshift.trusted_ca_bundles_config_map_labels") String configMapLabel,
      @Named("che.infra.openshift.trusted_ca_bundles_mount_path") String certificateMountPath,
      OpenShiftCheInstallationLocation cheInstallationLocation,
      OpenShiftClientFactory clientFactory)
      throws InfrastructureException {
    this.configMapName = configMapName;
    this.certificateMountPath = certificateMountPath;
    this.configMapLabelKeyValue = Splitter.on(",").withKeyValueSeparator("=").split(configMapLabel);
    OpenShiftClient client = clientFactory.createOC();
    List<ConfigMap> configMapList =
        client
            .configMaps()
            .inNamespace(cheInstallationLocation.getInstallationLocationNamespace())
            .withLabels(configMapLabelKeyValue)
            .list()
            .getItems();
    this.trustedStoreInitialized = !configMapList.isEmpty();
  }

  public void provision(KubernetesEnvironment k8sEnv, OpenShiftProject project)
      throws InfrastructureException {
    if (!trustedStoreInitialized) {
      return;
    }

    if (!project.configMaps().get(configMapName).isPresent()) {
      // create new map
      k8sEnv
          .getConfigMaps()
          .put(
              configMapName,
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
