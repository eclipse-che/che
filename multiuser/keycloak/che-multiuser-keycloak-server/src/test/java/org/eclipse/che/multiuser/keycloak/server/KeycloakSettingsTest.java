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
package org.eclipse.che.multiuser.keycloak.server;

import static org.eclipse.che.multiuser.keycloak.server.KeycloakSettings.DEFAULT_USERNAME_CLAIM;
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
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.USE_NONCE_SETTING;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Map;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Ilya Buziuk */
@Listeners(value = {MockitoTestNGListener.class})
public class KeycloakSettingsTest {

  @Mock private OIDCInfo oidcInfo;

  private static final String CHE_REALM = "che";
  private static final String CLIENT_ID = "che-public";
  private static final String PROFILE_URL_PATH = "/realms/" + CHE_REALM + "/account";
  private static final String LOGOUT_URL_PATH =
      "/realms/" + CHE_REALM + "/protocol/openid-connect/logout";
  private static final String TOKEN_URL_PATH =
      "/realms/" + CHE_REALM + "/protocol/openid-connect/token";
  private static final String USER_INFO_PATH =
      "/realms/" + CHE_REALM + "/protocol/openid-connect/userinfo";
  private static final String PASSWORD_URL_PATH = "/realms/" + CHE_REALM + "/account/password";
  private static final String JWKS_ENDPOINT_PATH =
      "/realms/" + CHE_REALM + "/protocol/openid-connect/certs";
  private static final String cheServerEndpoint = "https://test-crc-cluster.com.testing";

  @Test
  public void shouldBeSetGithubEndpointSettings() {
    final String SERVER_AUTH_URL = "keycloak-che.apps-crc.testing/auth";
    final String GITHUB_ENDPOINT = "github.com/some/endpoint";

    KeycloakSettings settings =
        new KeycloakSettings(
            null,
            SERVER_AUTH_URL,
            null,
            CHE_REALM,
            CLIENT_ID,
            null,
            null,
            false,
            null,
            GITHUB_ENDPOINT,
            false,
            oidcInfo);

    assertEquals(settings.get().get(GITHUB_ENDPOINT_SETTING), GITHUB_ENDPOINT);
  }

  @Test
  public void shouldBeSetOSOEndpointSettings() {
    final String SERVER_AUTH_URL = "https://keycloak-che.apps-crc.testing/auth";
    final String OSO_ENDPOINT = "oso/some/endpoint";

    KeycloakSettings settings =
        new KeycloakSettings(
            null,
            SERVER_AUTH_URL,
            null,
            CHE_REALM,
            CLIENT_ID,
            null,
            null,
            false,
            OSO_ENDPOINT,
            null,
            false,
            oidcInfo);

    assertEquals(settings.get().get(OSO_ENDPOINT_SETTING), OSO_ENDPOINT);
  }

  @Test
  public void shouldBeEnabledNonce() {
    final String SERVER_AUTH_URL = "https://keycloak-che.apps-crc.testing/auth";
    final boolean USE_NONCE = true;

    KeycloakSettings settings =
        new KeycloakSettings(
            null,
            SERVER_AUTH_URL,
            null,
            CHE_REALM,
            CLIENT_ID,
            null,
            null,
            USE_NONCE,
            null,
            null,
            false,
            oidcInfo);

    assertEquals(settings.get().get(USE_NONCE_SETTING), "true");
  }

  @Test
  public void shouldConfigureKeycloakAdaptersUrl() {
    final String SERVER_AUTH_URL = "https://external-keycloak-che.apps-crc.testing/auth";
    final String JS_ADAPTER_URL = "https://js/adapters/endpoint";

    KeycloakSettings settings =
        new KeycloakSettings(
            cheServerEndpoint,
            JS_ADAPTER_URL,
            null,
            CHE_REALM,
            CLIENT_ID,
            SERVER_AUTH_URL,
            null,
            false,
            null,
            null,
            false,
            oidcInfo);

    assertEquals(settings.get().get(JS_ADAPTER_URL_SETTING), JS_ADAPTER_URL);
  }

  @Test
  public void shouldBeUsedConfigurationFromExternalOIDCProviderWithFixedRedirectLinks() {
    final String SERVER_AUTH_URL = "https://external-keycloak-che.apps-crc.testing/auth";

    KeycloakSettings settings =
        new KeycloakSettings(
            cheServerEndpoint,
            null,
            null,
            CHE_REALM,
            CLIENT_ID,
            SERVER_AUTH_URL,
            null,
            false,
            null,
            null,
            true,
            oidcInfo);

    Map<String, String> publicSettings = settings.get();
    assertEquals(publicSettings.get(OIDC_PROVIDER_SETTING), SERVER_AUTH_URL);
    assertEquals(
        publicSettings.get(FIXED_REDIRECT_URL_FOR_DASHBOARD),
        cheServerEndpoint + "/keycloak/oidcCallbackDashboard.html");
    assertEquals(
        publicSettings.get(FIXED_REDIRECT_URL_FOR_IDE),
        cheServerEndpoint + "/keycloak/oidcCallbackIde.html");
    assertEquals(publicSettings.get(JS_ADAPTER_URL_SETTING), "/api/keycloak/OIDCKeycloak.js");
  }

  @Test
  public void shouldBeUsedConfigurationFromExternalOIDCProviderWithoutFixedRedirectLinks() {
    final String SERVER_AUTH_URL = "https://external-keycloak-che.apps-crc.testing/auth";

    when(oidcInfo.getEndSessionPublicEndpoint()).thenReturn(SERVER_AUTH_URL + LOGOUT_URL_PATH);
    when(oidcInfo.getJwksPublicUri()).thenReturn(SERVER_AUTH_URL + JWKS_ENDPOINT_PATH);
    when(oidcInfo.getUserInfoPublicEndpoint()).thenReturn(SERVER_AUTH_URL + USER_INFO_PATH);
    when(oidcInfo.getTokenPublicEndpoint()).thenReturn(SERVER_AUTH_URL + TOKEN_URL_PATH);

    KeycloakSettings settings =
        new KeycloakSettings(
            cheServerEndpoint,
            null,
            null,
            CHE_REALM,
            CLIENT_ID,
            SERVER_AUTH_URL,
            null,
            false,
            null,
            null,
            false,
            oidcInfo);

    Map<String, String> publicSettings = settings.get();
    assertEquals(publicSettings.get(USERNAME_CLAIM_SETTING), DEFAULT_USERNAME_CLAIM);
    assertEquals(publicSettings.get(CLIENT_ID_SETTING), CLIENT_ID);
    assertEquals(publicSettings.get(REALM_SETTING), CHE_REALM);
    assertNull(publicSettings.get(AUTH_SERVER_URL_SETTING));
    assertNull(publicSettings.get(PROFILE_ENDPOINT_SETTING));
    assertNull(publicSettings.get(PASSWORD_ENDPOINT_SETTING));
    assertEquals(publicSettings.get(LOGOUT_ENDPOINT_SETTING), SERVER_AUTH_URL + LOGOUT_URL_PATH);
    assertEquals(publicSettings.get(TOKEN_ENDPOINT_SETTING), SERVER_AUTH_URL + TOKEN_URL_PATH);
    assertEquals(publicSettings.get(USERINFO_ENDPOINT_SETTING), SERVER_AUTH_URL + USER_INFO_PATH);
    assertEquals(publicSettings.get(JWKS_ENDPOINT_SETTING), SERVER_AUTH_URL + JWKS_ENDPOINT_PATH);
    assertNull(publicSettings.get(OSO_ENDPOINT_SETTING));
    assertNull(publicSettings.get(GITHUB_ENDPOINT_SETTING));
    assertEquals(publicSettings.get(OIDC_PROVIDER_SETTING), SERVER_AUTH_URL);
    assertNull(publicSettings.get(FIXED_REDIRECT_URL_FOR_DASHBOARD));
    assertNull(publicSettings.get(FIXED_REDIRECT_URL_FOR_IDE));
    assertEquals(publicSettings.get(USE_NONCE_SETTING), "false");
    assertEquals(publicSettings.get(JS_ADAPTER_URL_SETTING), "/api/keycloak/OIDCKeycloak.js");
  }

  @Test
  public void shouldBeUsedConfigurationFromExternalAuthServer() {
    final String SERVER_AUTH_URL = "https://keycloak-che.apps-crc.testing/auth";

    when(oidcInfo.getEndSessionPublicEndpoint()).thenReturn(SERVER_AUTH_URL + LOGOUT_URL_PATH);
    when(oidcInfo.getJwksPublicUri()).thenReturn(SERVER_AUTH_URL + JWKS_ENDPOINT_PATH);
    when(oidcInfo.getUserInfoPublicEndpoint()).thenReturn(SERVER_AUTH_URL + USER_INFO_PATH);
    when(oidcInfo.getTokenPublicEndpoint()).thenReturn(SERVER_AUTH_URL + TOKEN_URL_PATH);

    KeycloakSettings settings =
        new KeycloakSettings(
            null,
            null,
            SERVER_AUTH_URL,
            CHE_REALM,
            CLIENT_ID,
            null,
            null,
            false,
            null,
            null,
            false,
            oidcInfo);

    Map<String, String> publicSettings = settings.get();
    assertEquals(publicSettings.get(USERNAME_CLAIM_SETTING), DEFAULT_USERNAME_CLAIM);
    assertEquals(publicSettings.get(CLIENT_ID_SETTING), CLIENT_ID);
    assertEquals(publicSettings.get(REALM_SETTING), CHE_REALM);
    assertEquals(publicSettings.get(AUTH_SERVER_URL_SETTING), SERVER_AUTH_URL);
    assertEquals(publicSettings.get(PROFILE_ENDPOINT_SETTING), SERVER_AUTH_URL + PROFILE_URL_PATH);
    assertEquals(
        publicSettings.get(PASSWORD_ENDPOINT_SETTING), SERVER_AUTH_URL + PASSWORD_URL_PATH);
    assertEquals(publicSettings.get(LOGOUT_ENDPOINT_SETTING), SERVER_AUTH_URL + LOGOUT_URL_PATH);
    assertEquals(publicSettings.get(TOKEN_ENDPOINT_SETTING), SERVER_AUTH_URL + TOKEN_URL_PATH);
    assertEquals(publicSettings.get(USERINFO_ENDPOINT_SETTING), SERVER_AUTH_URL + USER_INFO_PATH);
    assertEquals(publicSettings.get(JWKS_ENDPOINT_SETTING), SERVER_AUTH_URL + JWKS_ENDPOINT_PATH);
    assertNull(publicSettings.get(OSO_ENDPOINT_SETTING));
    assertNull(publicSettings.get(GITHUB_ENDPOINT_SETTING));
    assertNull(publicSettings.get(OIDC_PROVIDER_SETTING));
    assertNull(publicSettings.get(FIXED_REDIRECT_URL_FOR_DASHBOARD));
    assertNull(publicSettings.get(FIXED_REDIRECT_URL_FOR_IDE));
    assertEquals(publicSettings.get(USE_NONCE_SETTING), "false");
    assertEquals(publicSettings.get(JS_ADAPTER_URL_SETTING), SERVER_AUTH_URL + "/js/keycloak.js");
  }
}
