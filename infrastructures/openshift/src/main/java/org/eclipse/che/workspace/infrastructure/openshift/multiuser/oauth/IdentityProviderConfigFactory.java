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
package org.eclipse.che.workspace.infrastructure.openshift.multiuser.oauth;

import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.AUTH_SERVER_URL_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.CLIENT_ID_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.REALM_SETTING;

import com.google.inject.Provider;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.openshift.client.OpenShiftConfig;
import io.fabric8.openshift.client.OpenShiftConfigBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.RuntimeContext;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.multiuser.keycloak.server.KeycloakServiceClient;
import org.eclipse.che.multiuser.keycloak.server.KeycloakSettings;
import org.eclipse.che.multiuser.keycloak.shared.dto.KeycloakTokenResponse;
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
public class IdentityProviderConfigFactory extends OpenShiftClientConfigFactory {

  private static final Logger LOG = LoggerFactory.getLogger(IdentityProviderConfigFactory.class);

  private final String oauthIdentityProvider;

  private final KeycloakServiceClient keycloakServiceClient;
  private final Provider<WorkspaceRuntimes> workspaceRuntimeProvider;
  private final String messageToLinkAccount;

  @Inject
  public IdentityProviderConfigFactory(
      KeycloakServiceClient keycloakServiceClient,
      KeycloakSettings keycloakSettings,
      Provider<WorkspaceRuntimes> workspaceRuntimeProvider,
      @Nullable @Named("che.infra.openshift.oauth_identity_provider") String oauthIdentityProvider,
      @Named("che.api") String apiEndpoint) {
    this.keycloakServiceClient = keycloakServiceClient;
    this.workspaceRuntimeProvider = workspaceRuntimeProvider;
    this.oauthIdentityProvider = oauthIdentityProvider;

    messageToLinkAccount =
        "You should link your account with the <strong>"
            + oauthIdentityProvider
            + "</strong> \n"
            + "identity provider by visiting the "
            + "<a href='"
            // Here should be used public url. User should have it to make manual actions in the
            // browser.
            + keycloakSettings.get().get(AUTH_SERVER_URL_SETTING)
            + "/realms/"
            + keycloakSettings.get().get(REALM_SETTING)
            + "/account/identity?referrer="
            + keycloakSettings.get().get(CLIENT_ID_SETTING)
            + "&referrer_uri="
            + buildReferrerURI(apiEndpoint)
            + "' target='_blank' rel='noopener noreferrer'><strong>Federated Identities</strong></a> page of your Che account";
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
    return oauthIdentityProvider != null;
  }

  /**
   * Builds the OpenShift {@link Config} object based on a default {@link Config} object and an
   * optional workspace Id.
   */
  public Config buildConfig(Config defaultConfig, @Nullable String workspaceId)
      throws InfrastructureException {
    Subject subject = EnvironmentContext.getCurrent().getSubject();

    if (oauthIdentityProvider == null) {
      LOG.debug("OAuth Provider is not configured, default config is used.");
      return defaultConfig;
    }

    if (subject == Subject.ANONYMOUS) {
      LOG.debug(
          "OAuth Provider is configured but default subject is anonymous, default config is used.");
      return defaultConfig;
    }

    if (workspaceId == null) {
      LOG.debug(
          "OAuth Provider is configured and this request is not related to any workspace. OAuth token will be retrieved.");
      return personalizeConfig(defaultConfig);
    }

    Optional<RuntimeContext> context =
        workspaceRuntimeProvider.get().getRuntimeContext(workspaceId);
    if (!context.isPresent()) {
      // there is no cached info for this workspace in workspace API.
      // it means that it's not started yet and it's initial call for preparing context
      LOG.debug(
          "There is no runtime context for the specified workspace '%s'. It's the first workspace "
              + "related call, so context is personalized with OAuth token.");
      return personalizeConfig(defaultConfig);
    }
    String workspaceOwnerId = context.map(c -> c.getIdentity().getOwnerId()).orElse(null);

    boolean isRuntimeOwner = subject.getUserId().equals(workspaceOwnerId);

    if (!isRuntimeOwner) {
      LOG.debug(
          "OAuth Provider is configured, but current subject is not runtime owner, default config is used."
              + "Subject user id: '{}'. Runtime owner id: '{}'",
          subject.getUserId(),
          workspaceOwnerId);
      return defaultConfig;
    }

    LOG.debug(
        "OAuth Provider is configured and current subject is runtime owner. OAuth token will be retrieved.");
    return personalizeConfig(defaultConfig);
  }

  private Config personalizeConfig(Config defaultConfig) throws InfrastructureException {
    try {
      KeycloakTokenResponse keycloakTokenInfos =
          keycloakServiceClient.getIdentityProviderToken(oauthIdentityProvider);
      if ("user:full".equals(keycloakTokenInfos.getScope())) {
        return new OpenShiftConfigBuilder(OpenShiftConfig.wrap(defaultConfig))
            .withOauthToken(keycloakTokenInfos.getAccessToken())
            .build();
      } else {
        throw new InfrastructureException(
            "Cannot retrieve user OpenShift token: '"
                + oauthIdentityProvider
                + "' identity provider is not granted full rights: "
                + oauthIdentityProvider);
      }
    } catch (UnauthorizedException e) {
      LOG.error("Cannot retrieve User OpenShift token from the identity provider", e);

      throw new InfrastructureException(messageToLinkAccount);
    } catch (BadRequestException e) {
      LOG.error(
          "Cannot retrieve User OpenShift token from the '"
              + oauthIdentityProvider
              + "' identity provider",
          e);
      if (e.getMessage().endsWith("Invalid token.")) {
        throw new InfrastructureException(
            "Your session has expired. \nPlease "
                + "<a href='javascript:location.reload();' target='_top'>"
                + "login"
                + "</a> to Che again to get access to your OpenShift account");
      }
      throw new InfrastructureException(e.getMessage(), e);
    } catch (Exception e) {
      LOG.error(
          "Cannot retrieve User OpenShift token from the  '"
              + oauthIdentityProvider
              + "' identity provider",
          e);
      throw new InfrastructureException(e.getMessage(), e);
    }
  }
}
