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
package org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth;


import com.google.inject.Provider;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class retrieves the OpenShift OAuth token of the current Che user, and injects it the
 * OpenShift {@link Config} object, so that workspace OpenShift resources will be created under the
 * OpenShift account of the current Che user.
 *
 * <p>The OpenShift OAuth token is retrieved using the OpenShift identity provider configured in the
 * Keycloak server.
 *
 * <p>If the current user is not the user that started the current workspace (for operations such as
 * idling, che server shutdown, etc ...), then global OpenShift infrastructure credentials defined
 * in the Che properties will be used.
 *
 * @author David Festal
 */
@Singleton
public class OpenshiftProviderConfigFactory extends OpenShiftClientConfigFactory {

  private static final Logger LOG = LoggerFactory.getLogger(OpenshiftProviderConfigFactory.class);

  private final Provider<WorkspaceRuntimes> workspaceRuntimeProvider;

  @Inject
  public OpenshiftProviderConfigFactory(Provider<WorkspaceRuntimes> workspaceRuntimeProvider) {
    this.workspaceRuntimeProvider = workspaceRuntimeProvider;
  }

  private String buildReferrerURI(String apiEndpoint) {
    URI referrerURI =
        UriBuilder.fromUri(apiEndpoint)
            .replacePath("dashboard/")
            .queryParam("redirect_fragment", "/workspaces")
            .build();
    try {
      return URLEncoder.encode(referrerURI.toString(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(
          "Error occurred during constructing Referrer URI. " + e.getMessage(), e);
    }
  }

  @Override
  public boolean isPersonalized() {
    // config is personalized only if OAuth is configured and the current user is not anonymous
    return true;
  }

  /**
   * Builds the OpenShift {@link Config} object based on a default {@link Config} object and an
   * optional workspace Id.
   */
  public Config buildConfig(
      Config defaultConfig, @Nullable String workspaceId, @Nullable String token) {
    if (token != null) {
      LOG.debug("Creating token authenticated client");
      if (token.startsWith("Bearer")) {
        token = token.substring("Bearer ".length());
      }
      return new OpenShiftConfigBuilder(OpenShiftConfig.wrap(defaultConfig))
          .withOauthToken(token)
          .build();
    } else {
      LOG.warn("NO TOKEN PASSED. Getting default client config.");
      return defaultConfig;
    }
  }
}
