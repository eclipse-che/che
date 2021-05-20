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
import static java.util.Collections.singletonMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.Volume;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodRole;

/**
 * Mount configured self-signed certificate for git provider as file in each workspace machines if
 * configured.
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class VcsSslCertificateProvisioner
    implements ConfigurationProvisioner<KubernetesEnvironment> {
  static final String CHE_GIT_SELF_SIGNED_CERT_CONFIG_MAP_SUFFIX = "-che-git-self-signed-cert";
  static final String CHE_GIT_SELF_SIGNED_VOLUME = "che-git-self-signed-cert";
  static final String CERT_MOUNT_PATH = "/etc/che/git/cert/";
  static final String CA_CERT_FILE = "ca.crt";

  @Inject(optional = true)
  @Named("che.git.self_signed_cert")
  private String certificate;

  @Inject(optional = true)
  @Named("che.git.self_signed_cert_host")
  private String host;

  public VcsSslCertificateProvisioner() {}

  @VisibleForTesting
  VcsSslCertificateProvisioner(String certificate, String host) {
    this.certificate = certificate;
    this.host = host;
  }

  /**
   * @return true only if system configured for using self-signed certificate fot https git
   *     operation
   */
  public boolean isConfigured() {
    return !isNullOrEmpty(certificate);
  }

  /** @return path to the certificate file */
  public String getCertPath() {
    return CERT_MOUNT_PATH + CA_CERT_FILE;
  }

  /**
   * Return given in configuration git server host (e.g. 110.23.0.1:3000).
   *
   * @return git server host for git config if it configured in che.git.self_signed_cert_host
   */
  public String getGitServerHost() {
    return host;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    if (!isConfigured()) {
      return;
    }
    String selfSignedCertConfigMapName =
        identity.getWorkspaceId() + CHE_GIT_SELF_SIGNED_CERT_CONFIG_MAP_SUFFIX;
    k8sEnv
        .getConfigMaps()
        .put(
            selfSignedCertConfigMapName,
            new ConfigMapBuilder()
                .withNewMetadata()
                .withName(selfSignedCertConfigMapName)
                .endMetadata()
                .withData(singletonMap(CA_CERT_FILE, certificate))
                .build());

    for (PodData pod : k8sEnv.getPodsData().values()) {
      if (pod.getRole() != PodRole.INJECTABLE) {
        if (pod.getSpec()
            .getVolumes()
            .stream()
            .noneMatch(v -> v.getName().equals(CHE_GIT_SELF_SIGNED_VOLUME))) {
          pod.getSpec().getVolumes().add(buildCertVolume(selfSignedCertConfigMapName));
        }
      }

      for (Container container : pod.getSpec().getInitContainers()) {
        provisionCertVolumeMountIfNeeded(container);
      }
      for (Container container : pod.getSpec().getContainers()) {
        provisionCertVolumeMountIfNeeded(container);
      }
    }
  }

  private void provisionCertVolumeMountIfNeeded(Container container) {
    if (container
        .getVolumeMounts()
        .stream()
        .noneMatch(vm -> vm.getName().equals(CHE_GIT_SELF_SIGNED_VOLUME))) {
      container.getVolumeMounts().add(buildCertVolumeMount());
    }
  }

  private VolumeMount buildCertVolumeMount() {
    return new VolumeMountBuilder()
        .withName(CHE_GIT_SELF_SIGNED_VOLUME)
        .withNewReadOnly(true)
        .withMountPath(CERT_MOUNT_PATH)
        .build();
  }

  private Volume buildCertVolume(String configMapName) {
    return new VolumeBuilder()
        .withName(CHE_GIT_SELF_SIGNED_VOLUME)
        .withConfigMap(new ConfigMapVolumeSourceBuilder().withName(configMapName).build())
        .build();
  }
}
