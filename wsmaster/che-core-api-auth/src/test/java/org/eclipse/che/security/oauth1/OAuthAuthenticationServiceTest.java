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
package org.eclipse.che.security.oauth1;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import java.net.URL;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class OAuthAuthenticationServiceTest {

  private final String REDIRECT_URI = "/dashboard";
  private final String STATE =
      "oauth_provider=test-server&request_method=POST&signature_method=rsa&redirect_after_login="
          + REDIRECT_URI;
  private final String OAUTH_TOKEN = "JeZlJxu8bd1ewAmCkG668PCLC5kJ9ne1";
  private final String OAUTH_VERIFIER = "hfdp7dh39dks9884";

  @Mock private OAuthAuthenticator oAuthAuthenticator;

  @Mock private OAuthAuthenticatorProvider oAuthProvider;

  @InjectMocks private OAuthAuthenticationService oAuthAuthenticationService;

  @BeforeMethod
  public void setUp() {
    when(oAuthProvider.getAuthenticator("test-server")).thenReturn(oAuthAuthenticator);
  }

  @Test
  public void shouldResolveCallbackWithoutError() throws OAuthAuthenticationException {
    when(oAuthAuthenticator.callback(any(URL.class))).thenReturn("user1");
    final Response response =
        given()
            .redirects()
            .follow(false)
            .queryParam("state", STATE)
            .queryParam("oauth_token", OAUTH_TOKEN)
            .queryParam("oauth_verifier", OAUTH_VERIFIER)
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/oauth/1.0/callback");
    assertEquals(response.header("Location"), REDIRECT_URI);
  }

  @Test
  public void shouldResolveCallbackWithAccessDeniedError() throws OAuthAuthenticationException {
    when(oAuthAuthenticator.callback(any(URL.class)))
        .thenThrow(new UserDeniedOAuthAuthenticationException("Access denied"));
    final Response response =
        given()
            .redirects()
            .follow(false)
            .queryParam("state", STATE)
            .queryParam("oauth_token", OAUTH_TOKEN)
            .queryParam("oauth_verifier", "denied")
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/oauth/1.0/callback");
    assertEquals(response.header("Location"), REDIRECT_URI + "?error_code=access_denied");
  }

  @Test
  public void shouldResolveCallbackWithInvalidRequestError() throws OAuthAuthenticationException {
    when(oAuthAuthenticator.callback(any(URL.class)))
        .thenThrow(new OAuthAuthenticationException("Invalid request"));
    final Response response =
        given()
            .redirects()
            .follow(false)
            .queryParam("state", STATE)
            .queryParam("oauth_token", OAUTH_TOKEN)
            .queryParam("oauth_verifier", OAUTH_VERIFIER)
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/oauth/1.0/callback");
    assertEquals(response.header("Location"), REDIRECT_URI + "?error_code=invalid_request");
  }
}
