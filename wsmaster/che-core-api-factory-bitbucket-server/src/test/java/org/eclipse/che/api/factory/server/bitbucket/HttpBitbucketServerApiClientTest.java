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
package org.eclipse.che.api.factory.server.bitbucket;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HttpHeaders;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.factory.server.bitbucket.server.BitbucketPersonalAccessToken;
import org.eclipse.che.api.factory.server.bitbucket.server.BitbucketServerApiClient;
import org.eclipse.che.api.factory.server.bitbucket.server.BitbucketUser;
import org.eclipse.che.api.factory.server.bitbucket.server.HttpBitbucketServerApiClient;
import org.eclipse.che.api.factory.server.scm.exception.ScmBadRequestException;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmItemNotFoundException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class HttpBitbucketServerApiClientTest {
  private final String AUTHORIZATION_TOKEN =
      "OAuth oauth_consumer_key=\"key123321\", oauth_nonce=\"6c0eace252f8dcda\","
          + " oauth_signature=\"dPCm521TAF56FfGxabBAZDs9YTNeCg%2BiRK49afoJve8Mxk5ILlfkZKH693udqOig5k5ydeVxX%2FTso%2Flxx1pv2bqdbCqj3Nq82do1hJN5eTDLSvbHfGvjFuOGRobHTHwP6oJkaBSafjMUY8i8Vnz6hLfxToPj2ktd6ug4nKc1WGg%3D\", "
          + "oauth_signature_method=\"RSA-SHA1\", oauth_timestamp=\"1609250025\", "
          + "oauth_token=\"JmpyDe9sgYNn6pYHP6eGLaIU0vxdKLCJ\", oauth_version=\"1.0\"";
  WireMockServer wireMockServer;
  WireMock wireMock;
  BitbucketServerApiClient bitbucketServer;

  @BeforeMethod
  void start() {
    int httpPort = getHttpPort();
    wireMockServer =
        new WireMockServer(wireMockConfig().notifier(new Slf4jNotifier(false)).port(httpPort));
    wireMockServer.start();
    WireMock.configureFor("localhost", httpPort);
    wireMock = new WireMock("localhost", httpPort);
    bitbucketServer =
        new HttpBitbucketServerApiClient(
            wireMockServer.url("/"), (requestMethod, requestUrl) -> AUTHORIZATION_TOKEN);
  }

  @AfterMethod
  void stop() {
    wireMockServer.stop();
  }

  int getHttpPort() {
    return 3301;
  }

  @Test
  public void testGetUser()
      throws ScmItemNotFoundException, ScmUnauthorizedException, ScmCommunicationException {
    stubFor(
        get(urlEqualTo("/rest/api/1.0/users/ksmster"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORIZATION_TOKEN))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBodyFile("bitbucket/rest/api/1.0/users/ksmster/response.json")));

    BitbucketUser user = bitbucketServer.getUser("ksmster");
    assertNotNull(user);
  }

  @Test
  public void testGetUsers()
      throws ScmCommunicationException, ScmBadRequestException, ScmUnauthorizedException {
    stubFor(
        get(urlPathEqualTo("/rest/api/1.0/users"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORIZATION_TOKEN))
            .withQueryParam("start", equalTo("0"))
            .withQueryParam("limit", equalTo("25"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBodyFile("bitbucket/rest/api/1.0/users/response_s0_l25.json")));
    stubFor(
        get(urlPathEqualTo("/rest/api/1.0/users"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORIZATION_TOKEN))
            .withQueryParam("start", equalTo("3"))
            .withQueryParam("limit", equalTo("25"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBodyFile("bitbucket/rest/api/1.0/users/response_s3_l25.json")));
    stubFor(
        get(urlPathEqualTo("/rest/api/1.0/users"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORIZATION_TOKEN))
            .withQueryParam("start", equalTo("6"))
            .withQueryParam("limit", equalTo("25"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBodyFile("bitbucket/rest/api/1.0/users/response_s6_l25.json")));
    stubFor(
        get(urlPathEqualTo("/rest/api/1.0/users"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORIZATION_TOKEN))
            .withQueryParam("start", equalTo("9"))
            .withQueryParam("limit", equalTo("25"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBodyFile("bitbucket/rest/api/1.0/users/response_s9_l25.json")));

    List<String> page =
        bitbucketServer
            .getUsers()
            .stream()
            .map(BitbucketUser::getSlug)
            .collect(Collectors.toList());
    assertEquals(
        page,
        ImmutableList.of(
            "admin",
            "ksmster",
            "skabashn",
            "user1",
            "user2",
            "user3",
            "user4",
            "user5",
            "user6",
            "user7"));
  }

  @Test
  public void testGetUsersFiltered()
      throws ScmCommunicationException, ScmBadRequestException, ScmUnauthorizedException {
    stubFor(
        get(urlPathEqualTo("/rest/api/1.0/users"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORIZATION_TOKEN))
            .withQueryParam("start", equalTo("0"))
            .withQueryParam("limit", equalTo("25"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBodyFile("bitbucket/rest/api/1.0/users/filtered/response.json")));

    List<String> page =
        bitbucketServer
            .getUsers("ksmster")
            .stream()
            .map(BitbucketUser::getSlug)
            .collect(Collectors.toList());
    assertEquals(page, ImmutableList.of("admin", "ksmster"));
  }

  @Test
  public void testGetPersonalAccessTokens()
      throws ScmCommunicationException, ScmBadRequestException, ScmItemNotFoundException,
          ScmUnauthorizedException {
    stubFor(
        get(urlPathEqualTo("/rest/access-tokens/1.0/users/ksmster"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORIZATION_TOKEN))
            .withQueryParam("start", equalTo("0"))
            .withQueryParam("limit", equalTo("25"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json; charset=utf-8")
                    .withBodyFile("bitbucket/rest/access-tokens/1.0/users/ksmster/response.json")));

    List<String> page =
        bitbucketServer
            .getPersonalAccessTokens("ksmster")
            .stream()
            .map(BitbucketPersonalAccessToken::getName)
            .collect(Collectors.toList());
    assertEquals(page, ImmutableList.of("che", "t2"));
  }

  @Test
  public void shouldBeAbleToCreatePAT()
      throws ScmCommunicationException, ScmBadRequestException, ScmUnauthorizedException {

    // given
    stubFor(
        put(urlPathEqualTo("/rest/access-tokens/1.0/users/ksmster"))
            .withHeader(HttpHeaders.AUTHORIZATION, equalTo(AUTHORIZATION_TOKEN))
            .withHeader(HttpHeaders.ACCEPT, equalTo(MediaType.APPLICATION_JSON))
            .withHeader(HttpHeaders.CONTENT_TYPE, equalTo(MediaType.APPLICATION_JSON))
            .withHeader(HttpHeaders.CONTENT_LENGTH, equalTo("63"))
            .willReturn(
                ok().withBodyFile("bitbucket/rest/access-tokens/1.0/users/ksmster/newtoken.json")));

    // when
    BitbucketPersonalAccessToken result =
        bitbucketServer.createPersonalAccessTokens(
            "ksmster", "myToKen", ImmutableSet.of("PROJECT_WRITE", "REPO_WRITE"));
    // then
    assertNotNull(result);
    assertEquals(result.getToken(), "MTU4OTEwNTMyOTA5Ohc88HcY8k7gWOzl2mP5TtdtY5Qs");
  }
}
