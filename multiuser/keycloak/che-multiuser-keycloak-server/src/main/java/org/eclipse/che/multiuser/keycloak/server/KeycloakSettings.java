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
package org.eclipse.che.multiuser.keycloak.server;

import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.AUTH_SERVER_URL_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.CLIENT_ID_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.FIXED_REDIRECT_URL_FOR_DASHBOARD;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.FIXED_REDIRECT_URL_FOR_IDE;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.GITHUB_ENDPOINT_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.JS_ADAPTER_URL_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.JWKS_ENDPOINT_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.LOGOUT_ENDPOINT_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.OIDC_PROVIDER_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.OSO_ENDPOINT_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.PASSWORD_ENDPOINT_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.PROFILE_ENDPOINT_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.REALM_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.TOKEN_ENDPOINT_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.USERINFO_ENDPOINT_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.USERNAME_CLAIM_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.USE_FIXED_REDIRECT_URLS_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.USE_NONCE_SETTING;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.commons.annotation.Nullable;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
@Singleton
public class KeycloakSettings {
  protected static final String DEFAULT_USERNAME_CLAIM = "preferred_username";

  private final Map<String, String> settings;
  private final String oidcProviderUrl;

  @Inject
  public KeycloakSettings(
      @Named("che.api") String cheServerEndpoint,
      @Nullable @Named(JS_ADAPTER_URL_SETTING) String jsAdapterUrl,
      @Nullable @Named(AUTH_SERVER_URL_SETTING) String serverURL,
      @Nullable @Named(REALM_SETTING) String realm,
      @Named(CLIENT_ID_SETTING) String clientId,
      @Nullable @Named(OIDC_PROVIDER_SETTING) String oidcProviderUrl,
      @Nullable @Named(USERNAME_CLAIM_SETTING) String usernameClaim,
      @Named(USE_NONCE_SETTING) boolean useNonce,
      @Nullable @Named(OSO_ENDPOINT_SETTING) String osoEndpoint,
      @Nullable @Named(GITHUB_ENDPOINT_SETTING) String gitHubEndpoint,
      @Named(USE_FIXED_REDIRECT_URLS_SETTING) boolean useFixedRedirectUrls,
      OIDCInfo oidcInfo) {
    this.oidcProviderUrl = oidcProviderUrl;

    Map<String, String> settings = Maps.newHashMap();
    settings.put(
        USERNAME_CLAIM_SETTING, usernameClaim == null ? DEFAULT_USERNAME_CLAIM : usernameClaim);
    settings.put(CLIENT_ID_SETTING, clientId);
    settings.put(REALM_SETTING, realm);

    if (serverURL != null) {
      settings.put(AUTH_SERVER_URL_SETTING, serverURL);
      settings.put(PROFILE_ENDPOINT_SETTING, serverURL + "/realms/" + realm + "/account");
      settings.put(PASSWORD_ENDPOINT_SETTING, serverURL + "/realms/" + realm + "/account/password");
      settings.put(
          LOGOUT_ENDPOINT_SETTING,
          serverURL + "/realms/" + realm + "/protocol/openid-connect/logout");
      settings.put(
          TOKEN_ENDPOINT_SETTING,
          serverURL + "/realms/" + realm + "/protocol/openid-connect/token");
    }

    if (oidcInfo.getEndSessionPublicEndpoint() != null) {
      settings.put(LOGOUT_ENDPOINT_SETTING, oidcInfo.getEndSessionPublicEndpoint());
    }
    if (oidcInfo.getTokenPublicEndpoint() != null) {
      settings.put(TOKEN_ENDPOINT_SETTING, oidcInfo.getTokenPublicEndpoint());
    }
    if (oidcInfo.getUserInfoPublicEndpoint() != null) {
      settings.put(USERINFO_ENDPOINT_SETTING, oidcInfo.getUserInfoPublicEndpoint());
    }
    if (oidcInfo.getJwksPublicUri() != null) {
      settings.put(JWKS_ENDPOINT_SETTING, oidcInfo.getJwksPublicUri());
    }

    settings.put(OSO_ENDPOINT_SETTING, osoEndpoint);
    settings.put(GITHUB_ENDPOINT_SETTING, gitHubEndpoint);

    this.setUpKeycloakJSAdaptersURLS(
        settings, useNonce, useFixedRedirectUrls, jsAdapterUrl, cheServerEndpoint, serverURL);

    this.settings = Collections.unmodifiableMap(settings);
  }

  private void setUpKeycloakJSAdaptersURLS(
      Map<String, String> settings,
      boolean useNonce,
      boolean useFixedRedirectUrls,
      String jsAdapterUrl,
      String cheServerEndpoint,
      String serverURL) {
    if (oidcProviderUrl != null) {
      settings.put(OIDC_PROVIDER_SETTING, oidcProviderUrl);
      if (useFixedRedirectUrls) {
        String rootUrl =
            cheServerEndpoint.endsWith("/") ? cheServerEndpoint : cheServerEndpoint + "/";
        settings.put(
            FIXED_REDIRECT_URL_FOR_DASHBOARD, rootUrl + "keycloak/oidcCallbackDashboard.html");
        settings.put(FIXED_REDIRECT_URL_FOR_IDE, rootUrl + "keycloak/oidcCallbackIde.html");
      }
    }

    settings.put(USE_NONCE_SETTING, Boolean.toString(useNonce));

    if (jsAdapterUrl == null) {
      jsAdapterUrl =
          (oidcProviderUrl != null)
              ? "/api/keycloak/OIDCKeycloak.js"
              : serverURL + "/js/keycloak.js";
    }
    settings.put(JS_ADAPTER_URL_SETTING, jsAdapterUrl);
  }

  /**
   * Public Keycloak connection settings. It contains information about keycloak api urls and
   * information required to make Keycloak connection using public domain hostname. This info will
   * be shared with frontend.
   */
  public Map<String, String> get() {
    return settings;
  }
}
