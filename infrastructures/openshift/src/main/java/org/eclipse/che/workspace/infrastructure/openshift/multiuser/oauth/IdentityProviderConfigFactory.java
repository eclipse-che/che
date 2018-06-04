/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
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
  private final KeycloakSettings keycloakSettings;
  private final Provider<WorkspaceRuntimes> workspaceRuntimeProvider;
  private final String messageToLinkAccount;

  private String rootUrl;

  @Inject
  public IdentityProviderConfigFactory(
      KeycloakServiceClient keycloakServiceClient,
      KeycloakSettings keycloakSettings,
      Provider<WorkspaceRuntimes> workspaceRuntimeProvider,
      @Nullable @Named("che.infra.openshift.oauth_identity_provider") String oauthIdentityProvider,
      @Named("che.api") String apiEndpoint) {
    super();
    this.keycloakServiceClient = keycloakServiceClient;
    this.keycloakSettings = keycloakSettings;
    this.workspaceRuntimeProvider = workspaceRuntimeProvider;

    this.oauthIdentityProvider = oauthIdentityProvider;
    rootUrl = apiEndpoint;
    if (rootUrl.endsWith("/")) {
      rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
    }
    if (rootUrl.endsWith("/api")) {
      rootUrl = rootUrl.substring(0, rootUrl.length() - 4);
    }

    String referrer_uri =
        rootUrl.replace("http://", "http%3A%2F%2F").replace("https://", "https%3A%2F%2F")
            + "%2Fdashboard%2F?redirect_fragment%3D%2Fworkspaces";

    messageToLinkAccount =
        "You should link your account with the <strong>"
            + oauthIdentityProvider
            + "</strong> \n"
            + "identity provider by visiting the "
            + "<a href='"
            + keycloakSettings.get().get(AUTH_SERVER_URL_SETTING)
            + "/realms/"
            + keycloakSettings.get().get(REALM_SETTING)
            + "/account/identity?referrer="
            + keycloakSettings.get().get(CLIENT_ID_SETTING)
            + "&referrer_uri="
            + referrer_uri
            + "' target='_blank' rel='noopener noreferrer'><strong>Federated Identities</strong></a> page of your Che account";
  }

  /**
   * Builds the Openshift {@link Config} object based on a default {@link Config} object and an
   * optional workspace Id.
   */
  public Config buildConfig(Config defaultConfig, @Nullable String workspaceId)
      throws InfrastructureException {
    Subject subject = EnvironmentContext.getCurrent().getSubject();

    String workspaceOwnerId = null;
    if (workspaceId != null) {
      @SuppressWarnings("rawtypes")
      Optional<RuntimeContext> context =
          workspaceRuntimeProvider.get().getRuntimeContext(workspaceId);
      workspaceOwnerId = context.map(c -> c.getIdentity().getOwnerId()).orElse(null);
    }

    if (oauthIdentityProvider != null
        && subject != Subject.ANONYMOUS
        && (workspaceOwnerId == null || subject.getUserId().equals(workspaceOwnerId))) {
      try {
        KeycloakTokenResponse keycloakTokenInfos =
            keycloakServiceClient.getIdentityProviderToken(oauthIdentityProvider);
        if ("user:full".equals(keycloakTokenInfos.getScope())) {
          return new OpenShiftConfigBuilder(OpenShiftConfig.wrap(defaultConfig))
              .withOauthToken(keycloakTokenInfos.getAccessToken())
              .build();
        } else {
          throw new InfrastructureException(
              "Cannot retrieve user Openshift token: '"
                  + oauthIdentityProvider
                  + "' identity provider is not granted full rights: "
                  + oauthIdentityProvider);
        }
      } catch (UnauthorizedException e) {
        LOG.error("cannot retrieve User Openshift token from the identity provider", e);

        throw new InfrastructureException(messageToLinkAccount);
      } catch (BadRequestException e) {
        LOG.error(
            "cannot retrieve User Openshift token from the '"
                + oauthIdentityProvider
                + "' identity provider",
            e);
        if (e.getMessage().endsWith("Invalid token.")) {
          throw new InfrastructureException(
              "Your session has expired. \nPlease "
                  + "<a href='javascript:location.reload();' target='_top'>"
                  + "login"
                  + "</a> to Che again to get access to your Openshift account");
        }
        throw new InfrastructureException(e.getMessage(), e);
      } catch (Exception e) {
        LOG.error(
            "cannot retrieve User Openshift token from the  '"
                + oauthIdentityProvider
                + "' identity provider",
            e);
        throw new InfrastructureException(e.getMessage(), e);
      }
    }
    return defaultConfig;
  }
}
