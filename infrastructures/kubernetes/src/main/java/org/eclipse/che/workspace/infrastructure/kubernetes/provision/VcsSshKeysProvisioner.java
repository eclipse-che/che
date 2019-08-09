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
 * workspace container.<br>
 *
 * <p><strong>How it works:</strong>
 *
 * <ul>
 *   <li>all SSH Keys registered for VCS are fetched;
 *   <li>create K8s Secrets with <a
 *       href=https://github.com/kubernetes/kubernetes/blob/7693a1d5fe2a35b6e2e205f03ae9b3eddcdabc6b/pkg/apis/core/types.go#L4458">SSH
 *       Key type</a> for each of SSH keys;
 *   <li>secrets are mounted on each container by path '/etc/ssh/{sshKeyName}/ssh-privatekey' as a
 *       file;
 *   <li>ConfigMap with SSH settings is created and mounted to container by path
 *       '/etc/ssh/ssh_config'.
 * </ul>
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

  private final String SSH_CONFIG_MAP_NAME_SUFFIX = "-sshconfigmap";

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

    try {
      List<SshPairImpl> sshPairs = sshManager.getPairs(identity.getOwnerId(), "vcs");
      if (sshPairs.isEmpty()) {
        return;
      }

      StringBuilder sshConfigData = new StringBuilder();

      for (SshPair sshPair : sshPairs) {
        doProvisionSshKey(sshPair, k8sEnv, identity.getWorkspaceId());

        sshConfigData.append(buildConfig(sshPair.getName()));
      }

      String sshConfigMapName = identity.getWorkspaceId() + SSH_CONFIG_MAP_NAME_SUFFIX;
      doProvisionSshConfig(sshConfigMapName, sshConfigData.toString(), k8sEnv);
    } catch (ServerException e) {
      LOG.warn("Unable get SSH Keys. Cause: %s", e.getMessage(), e);
    }
  }

  private void doProvisionSshKey(SshPair sshPair, KubernetesEnvironment k8sEnv, String wsId) {
    if (isNullOrEmpty(sshPair.getName()) || isNullOrEmpty(sshPair.getPrivateKey())) {
      return;
    }
    String validNameForSecret = getValidNameForSecret(sshPair.getName());
    Secret secret =
        new SecretBuilder()
            .addToData(
                SSH_PRIVATE_KEY,
                Base64.getEncoder().encodeToString(sshPair.getPrivateKey().getBytes()))
            .withType(SECRET_TYPE_SSH)
            .withNewMetadata()
            .withName(wsId + "-" + validNameForSecret)
            .endMetadata()
            .build();

    k8sEnv.getSecrets().put(secret.getMetadata().getName(), secret);

    k8sEnv
        .getPodsData()
        .values()
        .forEach(
            p ->
                mountSshKeySecret(secret.getMetadata().getName(), validNameForSecret, p.getSpec()));
  }

  private void mountSshKeySecret(String secretName, String sshKeyName, PodSpec podSpec) {
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
                  .withMountPath(SSH_BASE_CONFIG_PATH + sshKeyName)
                  .build();
          container.getVolumeMounts().add(volumeMount);
        });
  }

  private void doProvisionSshConfig(
      String sshConfigMapName, String sshConfig, KubernetesEnvironment k8sEnv) {
    Map<String, String> sshConfigData = new HashMap<>();
    sshConfigData.put(SSH_CONFIG, sshConfig);
    ConfigMap configMap =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName(sshConfigMapName)
            .endMetadata()
            .withData(sshConfigData)
            .build();

    k8sEnv.getConfigMaps().put(configMap.getMetadata().getName(), configMap);
    k8sEnv.getPodsData().values().forEach(p -> mountConfigFile(p.getSpec(), sshConfigMapName));
  }

  private void mountConfigFile(PodSpec podSpec, String sshConfigMapName) {
    String configMapVolumeName = "ssshkeyconfigvolume";
    podSpec
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName(configMapVolumeName)
                .withConfigMap(
                    new ConfigMapVolumeSourceBuilder().withName(sshConfigMapName).build())
                .build());

    List<Container> containers = podSpec.getContainers();
    containers.forEach(
        container -> {
          VolumeMount volumeMount =
              new VolumeMountBuilder()
                  .withName(configMapVolumeName)
                  .withMountPath(SSH_CONFIG_PATH)
                  .withSubPath(SSH_CONFIG)
                  .withReadOnly(false)
                  .withNewReadOnly(false)
                  .build();
          container.getVolumeMounts().add(volumeMount);
        });
  }

  /**
   * Returns the ssh configuration entry which includes host and identity file location
   *
   * <p>Example of provided configuration:
   *
   * <pre>
   * host github.com
   * IdentityFile /etc/ssh/github-com/ssh-privatekey
   * </pre>
   *
   * or
   *
   * <pre>
   * host *
   * IdentityFile /etc/ssh/default-123456/ssh-privatekey
   * </pre>
   *
   * @param name the of key given during generate for vcs service we will consider it as host of
   *     version control service (e.g. github.com, gitlab.com and etc) if name starts from
   *     "default-{anyString}" it will be replaced on wildcard "*" host name. Name with format
   *     "default-{anyString}" will be generated on client side by Theia SSH Plugin, if user doesn't
   *     provide own name. Details see here:
   *     https://github.com/eclipse/che/issues/13494#issuecomment-512761661. Note: behavior can be
   *     improved in 7.x releases after 7.0.0
   * @return the ssh configuration which include host and identity file location
   */
  private String buildConfig(@NotNull String name) {
    String host = name.startsWith("default-") ? "*" : name;
    return "host "
        + host
        + "\nIdentityFile "
        + SSH_BASE_CONFIG_PATH
        + getValidNameForSecret(name)
        + "/"
        + SSH_PRIVATE_KEY
        + "\n";
  }

  /** Returns a valid secret name for the specified string value. */
  private String getValidNameForSecret(@NotNull String name) {
    return name.replace(".", "-");
  }
}
