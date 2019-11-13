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
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.PodSecurityContext;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.SecretVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.ConflictException;
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
 *   <li>create single K8s Secret with opaque type for storing SSH keys;
 *   <li>in following secret key represents host name and value contains private SSH key;
 *   <li>each key and value in secret is represented on file system by the following structure:
 *       '/etc/ssh/private/{hostName}/ssh-privatekey', where <code>hostName</code> is a key taken
 *       from secret and <code>ssh-privatekey</code> is a file that contains SSH private key taking
 *       by the following key name;
 *   <li>ConfigMap with SSH settings is created and mounted to container by path
 *       '/etc/ssh/ssh_config'.
 * </ul>
 *
 * @author Vitalii Parfonov
 * @author Vlad Zhukovskyi
 */
public class VcsSshKeysProvisioner implements ConfigurationProvisioner<KubernetesEnvironment> {

  private static String SSH_BASE_CONFIG_PATH = "/etc/ssh/";

  private static final String SSH_PRIVATE_KEYS = "private";
  private static final String SSH_PRIVATE_KEYS_PATH = SSH_BASE_CONFIG_PATH + SSH_PRIVATE_KEYS;

  private static final String SSH_CONFIG = "ssh_config";
  private static final String SSH_CONFIG_PATH = SSH_BASE_CONFIG_PATH + SSH_CONFIG;

  private static final String SSH_CONFIG_MAP_NAME_SUFFIX = "-sshconfigmap";
  private static final String SSH_SECRET_NAME_SUFFIX = "-sshprivatekeys";

  private static final String SSH_SECRET_TYPE = "opaque";

  private static final Logger LOG = LoggerFactory.getLogger(VcsSshKeysProvisioner.class);

  private final SshManager sshManager;
  private String jobImage;

  @Inject
  public VcsSshKeysProvisioner(
      SshManager sshManager, @Named("che.infra.kubernetes.pvc.jobs.image") String jobImage) {
    this.sshManager = sshManager;
    this.jobImage = jobImage;
  }

  @Override
  @Traced
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    TracingTags.WORKSPACE_ID.set(identity::getWorkspaceId);

    List<SshPairImpl> sshPairs;
    try {
      sshPairs = sshManager.getPairs(identity.getOwnerId(), "vcs");
    } catch (ServerException e) {
      LOG.warn("Unable to get SSH Keys. Cause: {}", e.getMessage());
      return;
    }
    if (sshPairs.isEmpty()) {
      try {
        sshPairs =
            singletonList(
                sshManager.generatePair(
                    identity.getOwnerId(), "vcs", "default-" + new Date().getTime()));
      } catch (ServerException | ConflictException e) {
        LOG.warn("Unable to generate the initial SSH key. Cause: {}", e.getMessage());
        return;
      }
    }

    doProvisionSshKeys(sshPairs, k8sEnv, identity.getWorkspaceId());

    StringBuilder sshConfigData = new StringBuilder();
    for (SshPair sshPair : sshPairs) {
      sshConfigData.append(
          buildConfig(
              sshPair.getName(),
              k8sEnv
                  .getPodsData()
                  .values()
                  .stream()
                  .anyMatch(
                      p -> {
                        PodSecurityContext securityContext = p.getSpec().getSecurityContext();
                        return p.getMetadata().getName().startsWith(identity.getWorkspaceId())
                            && securityContext != null
                            && securityContext.getRunAsUser() == 0
                            && securityContext.getFsGroup() == 0;
                      })));
    }

    String sshConfigMapName = identity.getWorkspaceId() + SSH_CONFIG_MAP_NAME_SUFFIX;
    doProvisionSshConfig(sshConfigMapName, sshConfigData.toString(), k8sEnv);
  }

  private void doProvisionSshKeys(
      List<SshPairImpl> sshPairs, KubernetesEnvironment k8sEnv, String wsId) {

    Map<String, String> data =
        sshPairs
            .stream()
            .filter(sshPair -> !isNullOrEmpty(sshPair.getPrivateKey()))
            .collect(
                toMap(
                    SshPairImpl::getName,
                    sshPair ->
                        Base64.getEncoder().encodeToString(sshPair.getPrivateKey().getBytes())));

    Secret secret =
        new SecretBuilder()
            .addToData(data)
            .withType(SSH_SECRET_TYPE)
            .withNewMetadata()
            .withName(wsId + SSH_SECRET_NAME_SUFFIX)
            .endMetadata()
            .build();

    k8sEnv.getSecrets().put(secret.getMetadata().getName(), secret);

    k8sEnv
        .getPodsData()
        .values()
        .forEach(p -> mountSshKeySecret(secret.getMetadata().getName(), p.getSpec()));
  }

  private void mountSshKeySecret(String secretName, PodSpec podSpec) {
    podSpec
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName(secretName)
                .withSecret(
                    new SecretVolumeSourceBuilder()
                        .withSecretName(secretName)
                        .withDefaultMode(0600)
                        .build())
                .build());
    List<Container> containers = podSpec.getContainers();
    VolumeMount sshKeysVolumeMount =
        new VolumeMountBuilder()
            .withName(secretName)
            .withNewReadOnly(true)
            .withReadOnly(true)
            .withMountPath(SSH_PRIVATE_KEYS_PATH)
            .build();
    containers.forEach(container -> container.getVolumeMounts().add(sshKeysVolumeMount));

    PodSecurityContext securityContext = podSpec.getSecurityContext();

    /* The OpenSSH daemon requires 600 file mode access permissions for private keys.
    Mounted keys have 640 permissions despite setting 'DefaultMode' to 600.
    As a workaround copy the SSH keys and manually set them needed permissions.
    The copy operation can be applied, if the primary group ID of the containers will be root (0).
    TODO rework this when https://github.com/kubernetes/kubernetes/issues/81089 will be resolved */
    if (securityContext != null
        && securityContext.getFsGroup() == 0
        && securityContext.getRunAsUser() == 0) {
      copyPrivateKeysWithPermissions(podSpec, sshKeysVolumeMount);
    }
  }

  // TODO remove this when https://github.com/kubernetes/kubernetes/issues/81089 will be resolved
  private void copyPrivateKeysWithPermissions(PodSpec podSpec, VolumeMount sshKeysVolumeMount) {
    String copiedSshKeysVolumeName = "copied-private-keys";
    podSpec
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName(copiedSshKeysVolumeName)
                .withNewEmptyDir()
                .endEmptyDir()
                .build());

    VolumeMount copiedSshKeysVolumeMount =
        new VolumeMountBuilder()
            .withName(copiedSshKeysVolumeName)
            .withMountPath(SSH_PRIVATE_KEYS_PATH + "-copy")
            .build();

    Container initContainer =
        new ContainerBuilder()
            .withName("ssh-private-keys-modifier")
            .withImage(jobImage)
            .withVolumeMounts(sshKeysVolumeMount, copiedSshKeysVolumeMount)
            .withCommand(
                asList(
                    "/bin/sh",
                    "-c",
                    format(
                        "(cp -r %1$s/. %1$s-copy && chmod -R 600 %1$s-copy) || exit 0",
                        SSH_PRIVATE_KEYS_PATH)))
            .build();
    podSpec
        .getContainers()
        .forEach(container -> container.getVolumeMounts().add(copiedSshKeysVolumeMount));
    podSpec.getInitContainers().add(initContainer);
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
                  .withReadOnly(true)
                  .withNewReadOnly(true)
                  .build();
          container.getVolumeMounts().add(volumeMount);
        });
  }

  /**
   * Returns the ssh configuration entry which includes host, identity file location and Host Key
   * checking policy
   *
   * <p>Example of provided configuration:
   *
   * <pre>
   * host github.com
   * IdentityFile /etc/ssh/private/github-com/ssh-privatekey
   * StrictHostKeyChecking = no
   * </pre>
   *
   * or
   *
   * <pre>
   * host *
   * IdentityFile /etc/ssh/private/default-123456/ssh-privatekey
   * StrictHostKeyChecking = no
   * </pre>
   *
   * @param name the of key given during generate for vcs service we will consider it as host of
   *     version control service (e.g. github.com, gitlab.com and etc) if name starts from
   *     "default-{anyString}" it will be replaced on wildcard "*" host name. Name with format
   *     "default-{anyString}" will be generated on client side by Theia SSH Plugin, if user doesn't
   *     provide own name. Details see here:
   *     https://github.com/eclipse/che/issues/13494#issuecomment-512761661. Note: behavior can be
   *     improved in 7.x releases after 7.0.0
   * @param useCopiedKeys use the copied keys with the proper permissions, see the description in
   *     the {@link #mountSshKeySecret}
   * @return the ssh configuration which include host, identity file location and Host Key checking
   *     policy
   */
  private String buildConfig(@NotNull String name, boolean useCopiedKeys) {
    String host = name.startsWith("default-") ? "*" : name;
    return "host "
        + host
        + "\nIdentityFile "
        + SSH_PRIVATE_KEYS_PATH
        // TODO remove this when https://github.com/kubernetes/kubernetes/issues/81089 will be
        // resolved
        + (useCopiedKeys ? "-copy" : "")
        + "/"
        + name
        + "\nStrictHostKeyChecking = no"
        + "\n\n";
  }
}
