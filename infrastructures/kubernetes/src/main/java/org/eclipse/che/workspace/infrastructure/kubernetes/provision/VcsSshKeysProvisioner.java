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

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.ssh.server.SshManager;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.shared.model.SshPair;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.tracing.TracingTags;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows mount user SSH Keys as <a
 * href="https://kubernetes.io/docs/concepts/configuration/secret">Kubernetes Secrets</a> on each
 * workspace containers. <br>
 * <strong>How it works</strong> <br>
 * When starting a workspace, we will grep all SSH Keys registered for VCS and create K8s Secret the
 * to them. Then secrets will be mounted on each container by path
 * '/etc/ssh/{sshKeyName}/ssh-privatekey' as a file. <a
 * href=https://github.com/kubernetes/kubernetes/blob/7693a1d5fe2a35b6e2e205f03ae9b3eddcdabc6b/pkg/apis/core/types.go#L4458">Secret
 * Type for SSH keys</a> Also will be created and mounted ConfigMap with SSH settings to the
 * '/etc/ssh/ssh_config'.
 *
 * @author Vitalii Parfonov
 */
public class VcsSshKeysProvisioner implements ConfigurationProvisioner<KubernetesEnvironment> {

  // SecretTypeSSHAuth contains data needed for SSH authentication.
  // Required field:
  // - Secret.Data["ssh-privatekey"] - private SSH key needed for authentication
  private static final String SECRET_TYPE_SSH = "kubernetes.io/ssh-auth";

  // SSHAuthPrivateKey is the key of the required SSH private key for SecretTypeSSHAuth secrets
  private static final String SSH_PRIVATE_KEY = "ssh-privatekey";

  private static final String SSH_BASE_CONFIG_PATH = "/etc/ssh/";

  private static final String SSH_CONFIG = "ssh_config";

  private static final String SSH_CONFIG_PATH = SSH_BASE_CONFIG_PATH + SSH_CONFIG;

  private static final Logger LOG = LoggerFactory.getLogger(VcsSshKeysProvisioner.class);

  private final SshManager sshManager;

  @Inject
  public VcsSshKeysProvisioner(SshManager sshManager) {
    this.sshManager = sshManager;
  }

  @Override
  @Traced
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    TracingTags.WORKSPACE_ID.set(identity::getWorkspaceId);

    StringBuilder sshConfig = new StringBuilder();
    Map<String, String> map = new HashMap<>();

    try {
      List<SshPairImpl> sshPairs = sshManager.getPairs(identity.getOwnerId(), "vcs");
      if (!sshPairs.isEmpty()) {
        for (SshPair sshPair : sshPairs) {
          if (isNullOrEmpty(sshPair.getName()) || isNullOrEmpty(sshPair.getPrivateKey())) {
            continue;
          }
          Secret secret =
              new SecretBuilder()
                  .addToData(
                      SSH_PRIVATE_KEY,
                      Base64.getEncoder().encodeToString(sshPair.getPrivateKey().getBytes()))
                  .withType(SECRET_TYPE_SSH)
                  .withNewMetadata()
                  .withName(getValidNameForSecret(sshPair.getName()))
                  .endMetadata()
                  .build();

          k8sEnv.getSecrets().put(secret.getMetadata().getName(), secret);

          k8sEnv
              .getPodsData()
              .values()
              .forEach(p -> addSshKeySecret(secret.getMetadata().getName(), p.getSpec()));

          sshConfig.append(buildConfig(sshPair.getName()));
        }

        map.put(SSH_CONFIG, sshConfig.toString());
        ConfigMap configMap =
            new ConfigMapBuilder()
                .withNewMetadata()
                .withName("sshconfigmap")
                .endMetadata()
                .withData(map)
                .build();

        k8sEnv.getConfigMaps().put(configMap.getMetadata().getName(), configMap);
        k8sEnv.getPodsData().values().forEach(p -> addConfigFile(p.getSpec()));
      }
    } catch (ServerException e) {
      LOG.warn("Unable get SSH Keys ", e);
    }
  }

  private void addSshKeySecret(String secretName, PodSpec podSpec) {
    podSpec
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName(secretName)
                .withSecret(new SecretVolumeSourceBuilder().withSecretName(secretName).build())
                .build());
    List<Container> containers = podSpec.getContainers();
    containers.forEach(
        container -> {
          VolumeMount volumeMount =
              new VolumeMountBuilder()
                  .withName(secretName)
                  .withNewReadOnly(false)
                  .withReadOnly(false)
                  .withMountPath(SSH_BASE_CONFIG_PATH + secretName)
                  .build();
          container.getVolumeMounts().add(volumeMount);
        });
  }

  private void addConfigFile(PodSpec podSpec) {
    podSpec
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName("configvolume")
                .withConfigMap(new ConfigMapVolumeSourceBuilder().withName("sshconfigmap").build())
                .build());

    List<Container> containers = podSpec.getContainers();
    containers.forEach(
        container -> {
          VolumeMount volumeMount =
              new VolumeMountBuilder()
                  .withName("configvolume")
                  .withMountPath(SSH_CONFIG_PATH)
                  .withSubPath(SSH_CONFIG)
                  .withReadOnly(false)
                  .withNewReadOnly(false)
                  .build();
          container.getVolumeMounts().add(volumeMount);
        });
  }

  /**
   * @param host the host of version control service (e.g. github.com, gitlab.com and etc)
   * @return the ssh configuration which include host of host and identity file location
   *     <p>Example of provided configuration:
   *     <p>host github.com HostName github.com IdentityFile /etc/ssh/github-com/ssh-privatekey
   */
  private String buildConfig(@NotNull String host) {
    String validName = getValidNameForSecret(host);
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder
        .append("host ")
        .append(host)
        .append("\n")
        .append("HostName ")
        .append(host)
        .append("\nIdentityFile ")
        .append(SSH_BASE_CONFIG_PATH)
        .append(validName)
        .append("/")
        .append(SSH_PRIVATE_KEY)
        .append("\n");
    return stringBuilder.toString();
  }

  /**
   * Check is given name available for secret name
   *
   * @param name
   * @return
   */
  private String getValidNameForSecret(@NotNull String name) {
    return name.replace(".", "-");
  }
}
