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
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.ConfigMapVolumeSourceBuilder;
import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.PodSpec;
import io.fabric8.kubernetes.api.model.VolumeBuilder;
import io.fabric8.kubernetes.api.model.VolumeMount;
import io.fabric8.kubernetes.api.model.VolumeMountBuilder;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;

@Singleton
public class GitUserProfileProvisioner implements ConfigurationProvisioner<KubernetesEnvironment> {

  private final String GIT_CONFIG_MAP_NAME_SUFFIX = "-gitconfig";

  private static final String GIT_BASE_CONFIG_PATH = "/etc/";
  private static final String GIT_CONFIG = "gitconfig";
  private static final String GIT_CONFIG_PATH = GIT_BASE_CONFIG_PATH + GIT_CONFIG;
  private static final String PREFERENCES_KEY_FILTER = "theia-user-preferences";
  private static final String GIT_USER_NAME_PROPERTY = "git.user.name";
  private static final String GIT_USER_EMAIL_PROPERTY = "git.user.email";
  private static final String CONFIG_MAP_VOLUME_NAME = "gitconfigvolume";

  private PreferenceManager preferenceManager;

  @Inject
  public GitUserProfileProvisioner(PreferenceManager preferenceManager) {
    this.preferenceManager = preferenceManager;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {
    getPreferenceValue(PREFERENCES_KEY_FILTER)
        .ifPresent(
            preferenceJsonValue -> {
              Map<String, String> theiaPreferences = getMapFromJsonObject(preferenceJsonValue);

              getGlobalGitConfigFileContent(
                      theiaPreferences.get(GIT_USER_NAME_PROPERTY),
                      theiaPreferences.get(GIT_USER_EMAIL_PROPERTY))
                  .ifPresent(
                      gitConfigFileContent -> {
                        String gitConfigMapName =
                            identity.getWorkspaceId() + GIT_CONFIG_MAP_NAME_SUFFIX;

                        doProvisionGlobalGitConfig(gitConfigMapName, gitConfigFileContent, k8sEnv);
                      });
            });
  }

  private Map<String, String> getMapFromJsonObject(String json) {
    Type stringMapType = new TypeToken<Map<String, String>>() {}.getType();

    return new Gson().fromJson(json, stringMapType);
  }

  private Optional<String> getPreferenceValue(String keyFilter) throws InfrastructureException {
    try {
      String userId = EnvironmentContext.getCurrent().getSubject().getUserId();
      Map<String, String> preferencesMap = preferenceManager.find(userId, keyFilter);

      return ofNullable(preferencesMap.get(keyFilter));
    } catch (ServerException e) {
      throw new InfrastructureException(e);
    }
  }

  private Optional<String> getGlobalGitConfigFileContent(String userName, String userEmail) {
    if (isNullOrEmpty(userName) && isNullOrEmpty(userEmail)) {
      return empty();
    }

    StringBuilder config = new StringBuilder();
    config.append("[user]").append('\n');

    if (userName != null) {
      config.append('\t').append("name = ").append(userName).append('\n');
    }

    if (userEmail != null) {
      config.append('\t').append("email = ").append(userEmail).append('\n');
    }

    return of(config.toString());
  }

  private void doProvisionGlobalGitConfig(
      String gitConfigMapName, String gitConfig, KubernetesEnvironment k8sEnv) {
    Map<String, String> gitConfigData = singletonMap(GIT_CONFIG, gitConfig);
    ConfigMap configMap =
        new ConfigMapBuilder()
            .withNewMetadata()
            .withName(gitConfigMapName)
            .endMetadata()
            .withData(gitConfigData)
            .build();

    k8sEnv.getConfigMaps().put(configMap.getMetadata().getName(), configMap);
    k8sEnv.getPodsData().values().forEach(p -> mountConfigFile(p.getSpec(), gitConfigMapName));
  }

  private void mountConfigFile(PodSpec podSpec, String gitConfigMapName) {
    podSpec
        .getVolumes()
        .add(
            new VolumeBuilder()
                .withName(CONFIG_MAP_VOLUME_NAME)
                .withConfigMap(
                    new ConfigMapVolumeSourceBuilder().withName(gitConfigMapName).build())
                .build());

    List<Container> containers = podSpec.getContainers();
    containers.forEach(
        container -> {
          VolumeMount volumeMount =
              new VolumeMountBuilder()
                  .withName(CONFIG_MAP_VOLUME_NAME)
                  .withMountPath(GIT_CONFIG_PATH)
                  .withSubPath(GIT_CONFIG)
                  .withReadOnly(false)
                  .withNewReadOnly(false)
                  .build();
          container.getVolumeMounts().add(volumeMount);
        });
  }
}
