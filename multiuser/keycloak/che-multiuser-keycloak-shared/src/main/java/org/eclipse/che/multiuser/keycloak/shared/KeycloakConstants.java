/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.shared;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
public class KeycloakConstants {

  private static final String KEYCLOAK_SETTING_PREFIX = "che.keycloak.";
  private static final String KEYCLOAK_SETTINGS_ENDPOINT_PATH = "/keycloak/settings";

  public static final String AUTH_SERVER_URL_SETTING = KEYCLOAK_SETTING_PREFIX + "auth_server_url";
  public static final String AUTH_SERVER_URL_INTERNAL_SETTING =
      KEYCLOAK_SETTING_PREFIX + "auth_internal_server_url";

  public static final String REALM_SETTING = KEYCLOAK_SETTING_PREFIX + "realm";
  public static final String CLIENT_ID_SETTING = KEYCLOAK_SETTING_PREFIX + "client_id";
  public static final String OIDC_PROVIDER_SETTING = KEYCLOAK_SETTING_PREFIX + "oidc_provider";
  public static final String USERNAME_CLAIM_SETTING = KEYCLOAK_SETTING_PREFIX + "username_claim";
  public static final String USE_NONCE_SETTING = KEYCLOAK_SETTING_PREFIX + "use_nonce";
  public static final String USE_FIXED_REDIRECT_URLS_SETTING =
      KEYCLOAK_SETTING_PREFIX + "use_fixed_redirect_urls";
  public static final String JS_ADAPTER_URL_SETTING = KEYCLOAK_SETTING_PREFIX + "js_adapter_url";
  public static final String ALLOWED_CLOCK_SKEW_SEC =
      KEYCLOAK_SETTING_PREFIX + "allowed_clock_skew_sec";

  public static final String OSO_ENDPOINT_SETTING = KEYCLOAK_SETTING_PREFIX + "oso.endpoint";
  public static final String PROFILE_ENDPOINT_SETTING =
      KEYCLOAK_SETTING_PREFIX + "profile.endpoint";
  public static final String PASSWORD_ENDPOINT_SETTING =
      KEYCLOAK_SETTING_PREFIX + "password.endpoint";
  public static final String LOGOUT_ENDPOINT_SETTING = KEYCLOAK_SETTING_PREFIX + "logout.endpoint";
  public static final String TOKEN_ENDPOINT_SETTING = KEYCLOAK_SETTING_PREFIX + "token.endpoint";
  public static final String JWKS_ENDPOINT_SETTING = KEYCLOAK_SETTING_PREFIX + "jwks.endpoint";
  public static final String USERINFO_ENDPOINT_SETTING =
      KEYCLOAK_SETTING_PREFIX + "userinfo.endpoint";
  public static final String GITHUB_ENDPOINT_SETTING = KEYCLOAK_SETTING_PREFIX + "github.endpoint";

  public static final String FIXED_REDIRECT_URL_FOR_DASHBOARD =
      KEYCLOAK_SETTING_PREFIX + "redirect_url.dashboard";
  public static final String FIXED_REDIRECT_URL_FOR_IDE =
      KEYCLOAK_SETTING_PREFIX + "redirect_url.ide";

  public static String getEndpoint(String apiEndpoint) {
    return apiEndpoint + KEYCLOAK_SETTINGS_ENDPOINT_PATH;
  }
}
