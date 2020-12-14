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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.testng.Assert.assertEquals;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OIDCInfoProviderTest {
  private WireMockServer wireMockServer;

  private static final String CHE_REALM = "che";
  private static final String TEST_URL = "some-test-url-to-skip";
  private static final String SERVER_URL = "http://localhost:" + getHttpPort() + "/auth";
  private static final String OPEN_ID_CONF_TEMPLATE =
      ""
          + "{"
          + "  \"token_endpoint\": \""
          + SERVER_URL
          + "/realms/"
          + CHE_REALM
          + "/protocol/openid-connect/token\","
          + "  \"end_session_endpoint\": \""
          + SERVER_URL
          + "/realms/"
          + CHE_REALM
          + "/protocol/openid-connect/logout\","
          + "  \"userinfo_endpoint\": \""
          + SERVER_URL
          + "/realms/"
          + CHE_REALM
          + "/protocol/openid-connect/userinfo\","
          + "  \"jwks_uri\": \""
          + SERVER_URL
          + "/realms/"
          + CHE_REALM
          + "/protocol/openid-connect/certs\""
          + "}";

  @BeforeClass
  void start() {
    wireMockServer = new WireMockServer(wireMockConfig().port(getHttpPort()));
    wireMockServer.start();
    WireMock.configureFor("localhost", getHttpPort());
  }

  @AfterClass
  void stop() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @Test(
      expectedExceptions = RuntimeException.class,
      expectedExceptionsMessageRegExp =
          "Exception while retrieving OpenId configuration from endpoint: .*")
  public void shouldFailToParseOIDCConfiguration() {
    stubFor(
        get(urlEqualTo("/auth/realms/che/.well-known/openid-configuration"))
            .willReturn(
                aResponse().withHeader("Content-Type", "text/html").withBody("broken json")));

    OIDCInfoProvider oidcInfoProvider = new OIDCInfoProvider(SERVER_URL, null, null, CHE_REALM);
    oidcInfoProvider.get();
  }

  @Test
  public void shouldParseOIDCConfigurationForServerUrl() {
    stubFor(
        get(urlEqualTo("/auth/realms/che/.well-known/openid-configuration"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/html")
                    .withBody(OPEN_ID_CONF_TEMPLATE)));

    OIDCInfoProvider oidcInfoProvider = new OIDCInfoProvider(SERVER_URL, null, null, CHE_REALM);
    OIDCInfo oidcInfo = oidcInfoProvider.get();

    assertEquals(
        SERVER_URL + "/realms/" + CHE_REALM + "/protocol/openid-connect/token",
        oidcInfo.getTokenEndpoint());
    assertEquals(
        SERVER_URL + "/realms/" + CHE_REALM + "/protocol/openid-connect/logout",
        oidcInfo.getEndSessionEndpoint());
    assertEquals(
        SERVER_URL + "/realms/" + CHE_REALM + "/protocol/openid-connect/userinfo",
        oidcInfo.getUserInfoEndpoint());
    assertEquals(
        SERVER_URL + "/realms/" + CHE_REALM + "/protocol/openid-connect/certs",
        oidcInfo.getJwksUri());
  }

  @Test
  public void shouldParseOIDCConfigurationForInternalServerUrl() {
    stubFor(
        get(urlEqualTo("/auth/realms/che/.well-known/openid-configuration"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/html")
                    .withBody(OPEN_ID_CONF_TEMPLATE)));

    OIDCInfoProvider oidcInfoProvider = new OIDCInfoProvider(TEST_URL, SERVER_URL, null, CHE_REALM);
    OIDCInfo oidcInfo = oidcInfoProvider.get();

    assertEquals(
        SERVER_URL + "/realms/" + CHE_REALM + "/protocol/openid-connect/token",
        oidcInfo.getTokenEndpoint());
    assertEquals(
        SERVER_URL + "/realms/" + CHE_REALM + "/protocol/openid-connect/logout",
        oidcInfo.getEndSessionEndpoint());
    assertEquals(
        SERVER_URL + "/realms/" + CHE_REALM + "/protocol/openid-connect/userinfo",
        oidcInfo.getUserInfoEndpoint());
    assertEquals(
        SERVER_URL + "/realms/" + CHE_REALM + "/protocol/openid-connect/certs",
        oidcInfo.getJwksUri());
  }

  @Test
  public void shouldParseOIDCConfigurationForOIDCProviderUrl() {
    String OIDCProviderUrl = "http://localhost:" + getHttpPort() + "/realms/";
    stubFor(
        get(urlEqualTo("/realms/.well-known/openid-configuration"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "text/html")
                    .withBody(OPEN_ID_CONF_TEMPLATE)));

    OIDCInfoProvider oidcInfoProvider =
        new OIDCInfoProvider(TEST_URL, TEST_URL, OIDCProviderUrl, CHE_REALM);
    OIDCInfo oidcInfo = oidcInfoProvider.get();

    assertEquals(
        SERVER_URL + "/realms/" + CHE_REALM + "/protocol/openid-connect/token",
        oidcInfo.getTokenEndpoint());
    assertEquals(
        SERVER_URL + "/realms/" + CHE_REALM + "/protocol/openid-connect/logout",
        oidcInfo.getEndSessionEndpoint());
    assertEquals(
        SERVER_URL + "/realms/" + CHE_REALM + "/protocol/openid-connect/userinfo",
        oidcInfo.getUserInfoEndpoint());
    assertEquals(
        SERVER_URL + "/realms/" + CHE_REALM + "/protocol/openid-connect/certs",
        oidcInfo.getJwksUri());
  }

  @Test(
      expectedExceptions = RuntimeException.class,
      expectedExceptionsMessageRegExp = "Either the '.*' or '.*' or '.*' property should be set")
  public void shouldThrowErrorWhenAuthServerWasNotSet() {
    new OIDCInfoProvider(null, null, null, CHE_REALM);
  }

  @Test(
      expectedExceptions = RuntimeException.class,
      expectedExceptionsMessageRegExp = "The '.*' property should be set")
  public void shouldThrowErrorWhenRealmPropertyWasNotSet() {
    new OIDCInfoProvider(TEST_URL, null, null, null);
  }

  private static int getHttpPort() {
    return 3001;
  }
}
