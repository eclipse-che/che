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
package org.eclipse.che.api.factory.server.gitlab;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.google.common.net.HttpHeaders;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.factory.server.scm.PersonalAccessToken;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.security.oauth.OAuthAPI;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class GitlabOAuthTokenFetcherTest {

  @Mock OAuthAPI oAuthAPI;
  GitlabOAuthTokenFetcher oAuthTokenFetcher;

  WireMockServer wireMockServer;
  WireMock wireMock;

  @BeforeMethod
  void start() {
    wireMockServer =
        new WireMockServer(wireMockConfig().notifier(new Slf4jNotifier(false)).dynamicPort());
    wireMockServer.start();
    WireMock.configureFor("localhost", wireMockServer.port());
    wireMock = new WireMock("localhost", wireMockServer.port());
    oAuthTokenFetcher =
        new GitlabOAuthTokenFetcher(wireMockServer.url("/"), "http://che.api", oAuthAPI);
  }

  @AfterMethod
  void stop() {
    wireMockServer.stop();
  }

  @Test(
      expectedExceptions = ScmCommunicationException.class,
      expectedExceptionsMessageRegExp =
          "Current token doesn't have the necessary  privileges. Please make sure Che app scopes are correct and containing at least: \\[api, write_repository, openid\\]")
  public void shouldThrowExceptionOnInsufficientTokenScopes() throws Exception {
    Subject subject = new SubjectImpl("Username", "id1", "token", false);
    OAuthToken oAuthToken = newDto(OAuthToken.class).withToken("oauthtoken").withScope("api repo");
    when(oAuthAPI.getToken(anyString())).thenReturn(oAuthToken);

    stubFor(
        get(urlEqualTo("/api/v4/user"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer oauthtoken"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBodyFile("gitlab/rest/api/v4/user/response.json")));

    stubFor(
        get(urlEqualTo("/oauth/token/info"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer oauthtoken"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBodyFile("gitlab/rest/api/v4/user/token_info_lack_scopes.json")));

    oAuthTokenFetcher.fetchPersonalAccessToken(subject, wireMockServer.url("/"));
  }

  @Test(
      expectedExceptions = ScmUnauthorizedException.class,
      expectedExceptionsMessageRegExp = "Username is not authorized in gitlab OAuth provider.")
  public void shouldThrowUnauthorizedExceptionWhenUserNotLoggedIn() throws Exception {
    Subject subject = new SubjectImpl("Username", "id1", "token", false);
    when(oAuthAPI.getToken(anyString())).thenThrow(UnauthorizedException.class);

    oAuthTokenFetcher.fetchPersonalAccessToken(subject, wireMockServer.url("/"));
  }

  @Test
  public void shouldReturnToken() throws Exception {
    Subject subject = new SubjectImpl("Username", "id1", "token", false);
    OAuthToken oAuthToken =
        newDto(OAuthToken.class).withToken("oauthtoken").withScope("api write_repository openid");
    when(oAuthAPI.getToken(anyString())).thenReturn(oAuthToken);

    stubFor(
        get(urlEqualTo("/oauth/token/info"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer oauthtoken"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBodyFile("gitlab/rest/api/v4/user/token_info.json")));

    stubFor(
        get(urlEqualTo("/api/v4/user"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer oauthtoken"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBodyFile("gitlab/rest/api/v4/user/response.json")));

    PersonalAccessToken token =
        oAuthTokenFetcher.fetchPersonalAccessToken(subject, wireMockServer.url("/"));
    assertNotNull(token);
  }

  @Test
  public void shouldValidatePersonalToken() throws Exception {
    stubFor(
        get(urlEqualTo("/api/v4/user"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer token123"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBodyFile("gitlab/rest/api/v4/user/response.json")));

    PersonalAccessToken token =
        new PersonalAccessToken(
            wireMockServer.baseUrl(),
            "cheUser",
            "username",
            "userId",
            "token-name",
            "tid-23434",
            "token123");

    assertTrue(oAuthTokenFetcher.isValid(token).get());
  }
}
