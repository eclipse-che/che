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
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodData;

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
  static final String CHE_GIT_SELF_SIGNED_CERT_VOLUME = "che-git-self-signed-cert";
  static final String CERT_MOUNT_PATH = "/etc/che/git/cert/";
  static final String CA_CERT_FILE = "cert.pem";

  @Inject(optional = true)
  @Named("che.git.self_signed_cert")
  private String certificate;

  public VcsSslCertificateProvisioner() {}

  @VisibleForTesting
  VcsSslCertificateProvisioner(String certificate) {
    this.certificate = certificate;
  }

  public boolean isConfigured() {
    return !isNullOrEmpty(certificate);
  }

  public String getCertPath() {
    return CERT_MOUNT_PATH + CA_CERT_FILE;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    if (!isConfigured()) {
      return;
    }
    String selfSignedCertSecretName =
        identity.getWorkspaceId() + CHE_GIT_SELF_SIGNED_CERT_CONFIG_MAP_SUFFIX;
    k8sEnv
        .getConfigMaps()
        .put(
            selfSignedCertSecretName,
            new ConfigMapBuilder()
                .withNewMetadata()
                .withName(selfSignedCertSecretName)
                .endMetadata()
                .withData(singletonMap(CA_CERT_FILE, certificate))
                .build());

    for (PodData pod : k8sEnv.getPodsData().values()) {
      Optional<Volume> certVolume =
          pod.getSpec()
              .getVolumes()
              .stream()
              .filter(v -> v.getName().equals(CHE_GIT_SELF_SIGNED_CERT_VOLUME))
              .findAny();

      if (!certVolume.isPresent()) {
        pod.getSpec().getVolumes().add(buildCertVolume(selfSignedCertSecretName));
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
    Optional<VolumeMount> certVolumeMount =
        container
            .getVolumeMounts()
            .stream()
            .filter(vm -> vm.getName().equals(CHE_GIT_SELF_SIGNED_CERT_VOLUME))
            .findAny();
    if (!certVolumeMount.isPresent()) {
      container.getVolumeMounts().add(buildCertVolumeMount());
    }
  }

  private VolumeMount buildCertVolumeMount() {
    return new VolumeMountBuilder()
        .withName(CHE_GIT_SELF_SIGNED_CERT_VOLUME)
        .withNewReadOnly(true)
        .withMountPath(CERT_MOUNT_PATH)
        .build();
  }

  private Volume buildCertVolume(String secretName) {
    return new VolumeBuilder()
        .withName(CHE_GIT_SELF_SIGNED_CERT_VOLUME)
        .withConfigMap(new ConfigMapVolumeSourceBuilder().withName(secretName).build())
        .build();
  }

  public static String getCertMountPath() {
    return CERT_MOUNT_PATH;
  }
}
