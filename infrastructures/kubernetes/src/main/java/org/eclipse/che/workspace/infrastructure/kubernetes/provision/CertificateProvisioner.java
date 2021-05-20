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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
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
 * Mount configured self-signed certificate as file in each workspace machines if configured.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class CertificateProvisioner implements ConfigurationProvisioner<KubernetesEnvironment> {

  public static final String CHE_SELF_SIGNED_CERT_SECRET_SUFFIX = "-che-self-signed-cert";
  public static final String CHE_SELF_SIGNED_CERT_VOLUME = "che-self-signed-cert";
  public static final String CERT_MOUNT_PATH = "/tmp/che/secret/";
  public static final String CA_CERT_FILE = "ca.crt";

  @Inject(optional = true)
  @Named("che.self_signed_cert")
  private String certificate;

  public CertificateProvisioner() {}

  @VisibleForTesting
  CertificateProvisioner(String certificate) {
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
        identity.getWorkspaceId() + CHE_SELF_SIGNED_CERT_SECRET_SUFFIX;
    k8sEnv
        .getSecrets()
        .put(
            selfSignedCertSecretName,
            new SecretBuilder()
                .withNewMetadata()
                .withName(selfSignedCertSecretName)
                .endMetadata()
                .withStringData(ImmutableMap.of(CA_CERT_FILE, certificate))
                .build());

    for (PodData pod : k8sEnv.getPodsData().values()) {
      if (pod.getRole() == PodRole.DEPLOYMENT) {
        if (pod.getSpec()
            .getVolumes()
            .stream()
            .noneMatch(v -> v.getName().equals(CHE_SELF_SIGNED_CERT_VOLUME))) {
          pod.getSpec().getVolumes().add(buildCertSecretVolume(selfSignedCertSecretName));
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
        .noneMatch(vm -> vm.getName().equals(CHE_SELF_SIGNED_CERT_VOLUME))) {
      container.getVolumeMounts().add(buildCertVolumeMount());
    }
  }

  private VolumeMount buildCertVolumeMount() {
    return new VolumeMountBuilder()
        .withName(CHE_SELF_SIGNED_CERT_VOLUME)
        .withNewReadOnly(true)
        .withMountPath(CERT_MOUNT_PATH)
        .build();
  }

  private Volume buildCertSecretVolume(String secretName) {
    return new VolumeBuilder()
        .withName(CHE_SELF_SIGNED_CERT_VOLUME)
        .withSecret(new SecretVolumeSourceBuilder().withSecretName(secretName).build())
        .build();
  }
}
