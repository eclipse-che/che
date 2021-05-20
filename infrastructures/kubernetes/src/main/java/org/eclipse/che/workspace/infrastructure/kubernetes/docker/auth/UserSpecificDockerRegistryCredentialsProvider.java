/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.docker.auth;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Base64;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.dto.DockerAuthConfig;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.dto.DockerAuthConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for reading credentials for docker registries from user preferences.
 *
 * @author Mykola Morhun
 */
@Singleton
public class UserSpecificDockerRegistryCredentialsProvider {
  private static final String DOCKER_REGISTRY_CREDENTIALS_KEY = "dockerCredentials";

  private static final Logger LOG =
      LoggerFactory.getLogger(UserSpecificDockerRegistryCredentialsProvider.class);

  private PreferenceManager preferenceManager;

  @Inject
  public UserSpecificDockerRegistryCredentialsProvider(PreferenceManager preferenceManager) {
    this.preferenceManager = preferenceManager;
  }

  /**
   * Gets and decode credentials for docker registries from user preferences. If it hasn't saved
   * yet, {@code AuthConfigs} with empty map will be returned.
   *
   * @return docker registry credentials from user preferences or null when preferences can't be
   *     retrieved or parsed
   */
  @Nullable
  public DockerAuthConfigs getCredentials() {
    try {
      String encodedCredentials =
          preferenceManager
              .find(
                  EnvironmentContext.getCurrent().getSubject().getUserId(),
                  DOCKER_REGISTRY_CREDENTIALS_KEY)
              .get(DOCKER_REGISTRY_CREDENTIALS_KEY);
      String credentials =
          encodedCredentials != null
              ? new String(Base64.getDecoder().decode(encodedCredentials), "UTF-8")
              : "{}";

      return DtoFactory.newDto(DockerAuthConfigs.class)
          .withConfigs(
              DtoFactory.getInstance().createMapDtoFromJson(credentials, DockerAuthConfig.class));
    } catch (Exception e) {
      LOG.warn(e.getLocalizedMessage());
      return null;
    }
  }
}
