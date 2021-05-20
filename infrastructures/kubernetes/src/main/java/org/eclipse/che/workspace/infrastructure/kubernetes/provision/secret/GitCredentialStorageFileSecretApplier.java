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
package org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret;

import static java.lang.String.format;
import static org.eclipse.che.workspace.infrastructure.kubernetes.provision.secret.KubernetesSecretAnnotationNames.ANNOTATION_MOUNT_PATH;

import com.google.common.annotations.Beta;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.K8sVersion;
import org.eclipse.che.workspace.infrastructure.kubernetes.provision.GitConfigProvisioner;

/**
 * An instance of {@link FileSecretApplier} that is trying to adjust the content of git-config that
 * was added by {@link GitConfigProvisioner}. The adjustment is adding configuration of git
 * credentials store, which is pointing to the file that is going to be mount to the container from
 * the secret.
 */
@Beta
@Singleton
public class GitCredentialStorageFileSecretApplier extends FileSecretApplier {

  private static final String GIT_CREDENTIALS_FILE_STORE_PATTERN =
      "\n[credential]\n\thelper = store --file %s\n";

  @Inject
  public GitCredentialStorageFileSecretApplier(K8sVersion k8sVersion) {
    super(k8sVersion);
  }

  @Override
  public void applySecret(KubernetesEnvironment env, RuntimeIdentity runtimeIdentity, Secret secret)
      throws InfrastructureException {
    super.applySecret(env, runtimeIdentity, secret);
    final String secretMountPath = secret.getMetadata().getAnnotations().get(ANNOTATION_MOUNT_PATH);
    Set<String> keys = secret.getData().keySet();
    if (keys.size() != 1) {
      throw new InfrastructureException(
          format(
              "Invalid git credential secret data. It should contain only 1 data item but it have %d",
              keys.size()));
    }
    Path gitSecretFilePath = Paths.get(secretMountPath, keys.iterator().next());
    ConfigMap gitConfigMap = env.getConfigMaps().get(GitConfigProvisioner.GIT_CONFIG_MAP_NAME);
    if (gitConfigMap != null) {
      Map<String, String> gitConfigMapData = gitConfigMap.getData();
      String gitConfig = gitConfigMapData.get(GitConfigProvisioner.GIT_CONFIG);
      if (gitConfig != null) {
        if (gitConfig.contains("helper = store --file") && gitConfig.contains("[credential]")) {
          throw new InfrastructureException(
              "Multiple git credentials secrets found. Please remove duplication.");
        }

        HashMap<String, String> newGitConfigMapData = new HashMap<>(gitConfigMapData);
        newGitConfigMapData.put(
            GitConfigProvisioner.GIT_CONFIG,
            gitConfig
                + String.format(GIT_CREDENTIALS_FILE_STORE_PATTERN, gitSecretFilePath.toString()));
        gitConfigMap.setData(newGitConfigMapData);
      }
    }
  }
}
