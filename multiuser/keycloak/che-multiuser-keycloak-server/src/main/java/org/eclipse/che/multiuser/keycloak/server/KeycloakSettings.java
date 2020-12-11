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
package org.eclipse.che.multiuser.keycloak.server;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.AUTH_SERVER_URL_INTERNAL_SETTING;
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
  private final String serverURL;
  private final String serverInternalURL;
  private final String realm;
  private final OIDCInfoProvider oidcInfoProvider;

  @Inject
  public KeycloakSettings(
      @Named("che.api") String cheServerEndpoint,
      @Nullable @Named(JS_ADAPTER_URL_SETTING) String jsAdapterUrl,
      @Nullable @Named(AUTH_SERVER_URL_SETTING) String serverURL,
      @Nullable @Named(AUTH_SERVER_URL_INTERNAL_SETTING) String serverInternalURL,
      @Nullable @Named(REALM_SETTING) String realm,
      @Named(CLIENT_ID_SETTING) String clientId,
      @Nullable @Named(OIDC_PROVIDER_SETTING) String oidcProviderUrl,
      @Nullable @Named(USERNAME_CLAIM_SETTING) String usernameClaim,
      @Named(USE_NONCE_SETTING) boolean useNonce,
      @Nullable @Named(OSO_ENDPOINT_SETTING) String osoEndpoint,
      @Nullable @Named(GITHUB_ENDPOINT_SETTING) String gitHubEndpoint,
      @Named(USE_FIXED_REDIRECT_URLS_SETTING) boolean useFixedRedirectUrls,
      OIDCInfoProvider oidcInfoProvider) {
    this.serverURL = serverURL;
    this.serverInternalURL = serverInternalURL;
    this.oidcProviderUrl = oidcProviderUrl;
    this.realm = realm;
    this.oidcInfoProvider = oidcInfoProvider;

    String serverAuthUrl = getAuthServerURL();
    this.validate();

    String wellKnownEndpoint = firstNonNull(oidcProviderUrl, serverAuthUrl + "/realms/" + realm);
    if (!wellKnownEndpoint.endsWith("/")) {
      wellKnownEndpoint = wellKnownEndpoint + "/";
    }
    wellKnownEndpoint += ".well-known/openid-configuration";
    oidcInfoProvider.requestInfo(wellKnownEndpoint);

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

    if (oidcInfoProvider.getEndSessionEndpoint() != null) {
      settings.put(LOGOUT_ENDPOINT_SETTING, oidcInfoProvider.getEndSessionEndpoint());
    }
    if (oidcInfoProvider.getTokenEndpoint() != null) {
      settings.put(TOKEN_ENDPOINT_SETTING, oidcInfoProvider.getTokenEndpoint());
    }
    if (oidcInfoProvider.getUserInfoEndpoint() != null) {
      settings.put(USERINFO_ENDPOINT_SETTING, oidcInfoProvider.getUserInfoEndpoint());
    }
    if (oidcInfoProvider.getJWKS_URI() != null) {
      settings.put(JWKS_ENDPOINT_SETTING, oidcInfoProvider.getJWKS_URI());
    }

    settings.put(OSO_ENDPOINT_SETTING, osoEndpoint);
    settings.put(GITHUB_ENDPOINT_SETTING, gitHubEndpoint);

    this.setUpKeycloakJSAdaptersURLS(
        settings, useNonce, useFixedRedirectUrls, jsAdapterUrl, cheServerEndpoint);

    this.settings = Collections.unmodifiableMap(settings);
  }

  private void validate() {
    if (serverURL == null && serverInternalURL == null && oidcProviderUrl == null) {
      throw new RuntimeException(
          "Either the '"
              + AUTH_SERVER_URL_SETTING
              + "' or '"
              + AUTH_SERVER_URL_INTERNAL_SETTING
              + "' or '"
              + OIDC_PROVIDER_SETTING
              + "' property should be set");
    }

    if (oidcProviderUrl == null && realm == null) {
      throw new RuntimeException("The '" + REALM_SETTING + "' property should be set");
    }
  }

  private void setUpKeycloakJSAdaptersURLS(
      Map<String, String> settings,
      boolean useNonce,
      boolean useFixedRedirectUrls,
      String jsAdapterUrl,
      String cheServerEndpoint) {
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

  /** @return url to get user profile information. */
  public String getUserInfoEndpoint() {
    String userInfoEndpoint = this.oidcInfoProvider.getUserInfoEndpoint();
    if (this.serverURL != null && this.serverInternalURL != null) {
      userInfoEndpoint = userInfoEndpoint.replace(this.serverURL, this.serverInternalURL);
    }
    return userInfoEndpoint;
  }

  /** @return url to retrieve JWK public key for token validation. */
  public String getJWKS_URI() {
    String jwksUriEndpoint = this.oidcInfoProvider.getJWKS_URI();
    if (serverURL != null && this.serverInternalURL != null) {
      jwksUriEndpoint = jwksUriEndpoint.replace(this.serverURL, this.serverInternalURL);
    }
    return jwksUriEndpoint;
  }

  /** @return Keycloak server url. */
  public String getAuthServerURL() {
    return (serverInternalURL != null) ? serverInternalURL : serverURL;
  }
}
