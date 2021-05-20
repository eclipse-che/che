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
import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
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
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.model.impl.WarningImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.workspace.infrastructure.kubernetes.Warnings;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.KubernetesEnvironment.PodRole;

@Singleton
public class GitConfigProvisioner implements ConfigurationProvisioner<KubernetesEnvironment> {

  public static final String GIT_CONFIG_MAP_NAME = "gitconfig";

  private static final String GIT_BASE_CONFIG_PATH = "/etc/";
  public static final String GIT_CONFIG = "gitconfig";
  private static final String GIT_CONFIG_PATH = GIT_BASE_CONFIG_PATH + GIT_CONFIG;
  private static final String PREFERENCES_KEY_FILTER = "theia-user-preferences";
  private static final String GIT_USER_NAME_PROPERTY = "git.user.name";
  private static final String GIT_USER_EMAIL_PROPERTY = "git.user.email";
  private static final String CONFIG_MAP_VOLUME_NAME = "gitconfigvolume";
  private static final String HTTPS = "https://";

  private PreferenceManager preferenceManager;
  private UserManager userManager;
  private VcsSslCertificateProvisioner vcsSslCertificateProvisioner;

  @Inject
  public GitConfigProvisioner(
      PreferenceManager preferenceManager,
      UserManager userManager,
      VcsSslCertificateProvisioner vcsSslCertificateProvisioner) {
    this.preferenceManager = preferenceManager;
    this.userManager = userManager;
    this.vcsSslCertificateProvisioner = vcsSslCertificateProvisioner;
  }

  @Override
  public void provision(KubernetesEnvironment k8sEnv, RuntimeIdentity identity)
      throws InfrastructureException {

    try {
      Pair<String, String> userAndEmail = getUserFromPreferences();

      if (userAndEmail == null) {
        userAndEmail = getUserFromUserManager();
      }

      prepareAndProvisionGitConfiguration(
          userAndEmail.first, userAndEmail.second, k8sEnv, identity);
    } catch (ServerException | NotFoundException e) {
      reportWarning(
          k8sEnv,
          Warnings.EXCEPTION_IN_USER_MANAGEMENT_DURING_GIT_PROVISION_WARNING_CODE,
          format(
              Warnings.EXCEPTION_IN_USER_MANAGEMENT_DURING_GIT_PROVISION_MESSAGE_FMT,
              e.getMessage()));
    } catch (JsonSyntaxException e) {
      reportWarning(
          k8sEnv,
          Warnings.JSON_IS_NOT_A_VALID_REPRESENTATION_FOR_AN_OBJECT_OF_TYPE_WARNING_CODE,
          format(
              Warnings.JSON_IS_NOT_A_VALID_REPRESENTATION_FOR_AN_OBJECT_OF_TYPE_MESSAGE_FMT,
              e.getMessage()));
    }
  }

  private void reportWarning(KubernetesEnvironment k8sEnv, int code, String message) {
    k8sEnv.getWarnings().add(new WarningImpl(code, message));
  }

  private Pair<String, String> getUserFromPreferences()
      throws ServerException, JsonSyntaxException {
    String preferenceJson = getPreferenceJson(PREFERENCES_KEY_FILTER);
    Map<String, Object> preferences = getMapFromJsonObject(preferenceJson);

    String name = getStringValueOrNull(preferences, GIT_USER_NAME_PROPERTY);
    String email = getStringValueOrNull(preferences, GIT_USER_EMAIL_PROPERTY);

    return isNullOrEmpty(name) && isNullOrEmpty(email) ? null : Pair.of(name, email);
  }

  private Pair<String, String> getUserFromUserManager() throws NotFoundException, ServerException {
    String userId = EnvironmentContext.getCurrent().getSubject().getUserId();
    User user = userManager.getById(userId);

    return Pair.of(user.getName(), user.getEmail());
  }

  private void prepareAndProvisionGitConfiguration(
      String name, String email, KubernetesEnvironment k8sEnv, RuntimeIdentity identity) {
    prepareGitConfigurationContent(name, email)
        .ifPresent(content -> doProvisionGitConfiguration(GIT_CONFIG_MAP_NAME, content, k8sEnv));
  }

  private String getStringValueOrNull(Map<String, Object> map, String key) {
    Object value = map.get(key);

    return value instanceof String ? (String) value : null;
  }

  private Map<String, Object> getMapFromJsonObject(String json) throws JsonSyntaxException {
    if (isNullOrEmpty(json)) {
      return emptyMap();
    }

    Type typeToken = new TypeToken<Map<String, Object>>() {}.getType();

    /*
    The main idea to implicit cast map from string:object to string:string is that,
    we're looking for a two specific properties in the end, that have 100% string values,
    so we don't care about what we have in other properties.
    */

    return new Gson().fromJson(json, typeToken);
  }

  private String getPreferenceJson(String keyFilter) throws ServerException {
    String userId = EnvironmentContext.getCurrent().getSubject().getUserId();
    Map<String, String> preferencesMap = preferenceManager.find(userId, keyFilter);

    return preferencesMap.get(keyFilter);
  }

  private Optional<String> prepareGitConfigurationContent(String userName, String userEmail) {
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

    if (vcsSslCertificateProvisioner.isConfigured()) {
      String host = vcsSslCertificateProvisioner.getGitServerHost();

      // Will add leading scheme (https://) if it not provide in configuration.
      // If host not configured wil return empty string, it will means that
      // provided certificate will used for all https connections.

      StringBuilder gitServerHosts = new StringBuilder();
      if (!isNullOrEmpty(host)) {
        gitServerHosts.append(" \"");
        if (!host.startsWith(HTTPS)) {
          gitServerHosts.append(HTTPS);
        }
        gitServerHosts.append(host);
        gitServerHosts.append("\"");
      }

      config
          .append("[http")
          .append(gitServerHosts.toString())
          .append("]")
          .append('\n')
          .append('\t')
          .append("sslCAInfo = ")
          .append(vcsSslCertificateProvisioner.getCertPath());
    }

    return of(config.toString());
  }

  private void doProvisionGitConfiguration(
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
    k8sEnv
        .getPodsData()
        .values()
        .forEach(
            p -> mountConfigFile(p.getSpec(), gitConfigMapName, p.getRole() != PodRole.INJECTABLE));
  }

  private void mountConfigFile(PodSpec podSpec, String gitConfigMapName, boolean addVolume) {
    if (addVolume) {
      podSpec
          .getVolumes()
          .add(
              new VolumeBuilder()
                  .withName(CONFIG_MAP_VOLUME_NAME)
                  .withConfigMap(
                      new ConfigMapVolumeSourceBuilder().withName(gitConfigMapName).build())
                  .build());
    }

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
