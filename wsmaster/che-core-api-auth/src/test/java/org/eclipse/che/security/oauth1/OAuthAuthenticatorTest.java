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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.net.MalformedURLException;
import java.net.URL;
import javax.ws.rs.core.UriBuilder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OAuthAuthenticatorTest {
  private WireMockServer wireMockServer;
  private static final int PORT = 3305;
  private final String TEST_URI = "https://test-server";
  private final String STATE =
      "oauth_provider=test-server&request_method=POST&signature_method=rsa&userId=user1";
  private final String OAUTH_TOKEN = "JeZlJxu8bd1ewAmCkG668PCLC5kJ9ne1";
  private final OAuthAuthenticator oAuthAuthenticator =
      new OAuthAuthenticator(
          "test-server",
          null,
          "http://localhost:" + PORT + "/access",
          null,
          null,
          null,
          "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDZvr2tssVQ46qE1UBK1DFRZrKuIBMCL5q+cltLAVJZ7dlsdv2Yr5mgt3il2BQa0CSmwxTsMdwYqRIDchroVREs5IAfcwOFAL6OnMos/8AEg8Gamnz/Fu1/968bmlV/abKfrdlfkUNuOtpzG5PCf8UAQEt0ajMtFWKFeXPl527BTwRqz1rRVU/wqDC3nS6PM335XCr6mjdBzFDMUC1M91l34M6wPJVPcDRi3cQDb1+YMrFmuzloEs6nlUtM9gH4t9SD9bmBOOj5M7lo3jMuNJ+tE/5M/wrjs1BYbFiUKL3n/BKdyPHpGDssFwo25UBvJWPZZ/YFu6HTD85uUfsbDuTlAgMBAAECggEAA0hY27GCQAHupCoC2h3w0GVX9EAPiUzmbFCVB8BxWWG4kWYJ1K9xBXc+nmFvjCfvJYRzYEwwIT8LQnoJ5c7Cf4bCV7cIKo0kUkoS0jLY1jiWRpploALceb1mKmhdOZqCUt3wFPy/o33HpUyZIamDcsmFWa/wLZHQ9moqUSD4Dnn3Wy6mLyipQDC8LIjPQceuU96VGbZa/XJR8sVMulpgUHvQRzr9PZ1tw4yAK+tcg3rfx4XT6qZS64o4mYrNGYO3QBH3AMUZl1BVG9Q2SlrUM+RGlS5c3DYsFCD40yDBCJIvW1Tfoc4nuDn71rgDEQUZzZlP9X6q1Eh8karaHCrEAQKBgQD16HUcfEd+asDa80BZm3U9Rp0lI1JIfrF/AWR6RR1rldnZOGnQD4untYSZF8vpGdSN5t26szYGJQz9SZl1dtz5sQsyXY9TrGnSf/byoy8yJ6FU3IRaIfoxiAV1fL/QxLsyDls4G5OF9kCu4u2IDKPgDQnb1x9Kq8dQHIyB44eWJQKBgQDirmZToQNZE2GKS+GMxFeeGBTLuV2ED3YuyhRAE3v1bvMRsxcNpMWeCxPMfu8Ctn51yxvTkhRYF3vUz7HjfHYfSMFPf+DwZjOCVWMTT6d8XOcjXIq+mY2Dyek7Kagbbx2oMyBC9HCe0/iK0nOhUNfYFi8PFyDrosvmcQO3qEX3wQKBgG/+5xeKIqWYySzvDKfC/apitr9rTtZlnUFSyQhG4hdVsFoWL1rrOZewPCvdgqkvcncOZn3ZkQlLZpcVJicxc4Lk90yA//4D0E5mqXnoiF43Xmrf5AeI4gIdCR9xKYtTjk5F65WqOY4RkXQVNkl4OEqapZrSZxYDFkuONRATKHVhAoGAQSku4wVa6AUpOc78RDHAmgKEH9fmKOsk5uhSD+VJ8dB18PWRP+vIntjCVTt7y0TYb1X2ZsgMLxJ5F0Co+yKw9ec9InQ5HgHS9rlC5K82DwrJqqGUhJuxUVv+PnKID3LOjKY9tOF9ajq2rHk4ofuSQFyIJIdagEHo6RI9plKp4kECgYAtVUUoXAn5EKLuNVPzlnH+E+iBco0WaQGtsWhIlu6RVhSwJNrldxMFIuWzG56RoFV5tu+KA05RZx82cbazcJJVfwn7S6rmHCdxri3bjpnwgNHmY9E9cxBsEBW2DIYTyI8tbEytbH8syYPGSxb5+VIZEuP+8qel12mVfcoNl/oCCw==") {
        @Override
        String getOAuthProvider() {
          return null;
        }

        @Override
        public String getLocalAuthenticateUrl() {
          return null;
        }
      };

  @BeforeClass
  void start() {
    wireMockServer = new WireMockServer(wireMockConfig().port(PORT));
    wireMockServer.start();
    WireMock.configureFor("localhost", PORT);
  }

  @AfterClass
  void stop() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @Test
  public void shouldResolveCallbackWithUserId()
      throws MalformedURLException, OAuthAuthenticationException {
    URL requestURL =
        UriBuilder.fromUri(TEST_URI)
            .queryParam("state", STATE)
            .queryParam("oauth_token", OAUTH_TOKEN)
            .queryParam("oauth_verifier", "hfdp7dh39dks9884")
            .build()
            .toURL();
    stubFor(
        post(urlPathEqualTo("/access"))
            .willReturn(
                aResponse()
                    .withBody("oauth_token=ab3cd9j4ks73hf7g&oauth_token_secret=xyz4992k83j47x0b")));

    String user = oAuthAuthenticator.callback(requestURL);
    verify(postRequestedFor(urlPathEqualTo("/access")));
    assertEquals("user1", user);
  }

  @Test
  public void shouldThrowUserDeniedOAuthAuthenticationException() throws MalformedURLException {
    URL requestURL =
        UriBuilder.fromUri(TEST_URI)
            .queryParam("state", STATE)
            .queryParam("oauth_token", OAUTH_TOKEN)
            .queryParam("oauth_verifier", "denied")
            .build()
            .toURL();
    assertThrows(
        UserDeniedOAuthAuthenticationException.class,
        () -> oAuthAuthenticator.callback(requestURL));
  }

  @Test
  public void shouldThrowOAuthAuthenticationException() throws MalformedURLException {
    URL requestURL =
        UriBuilder.fromUri(TEST_URI)
            .queryParam("state", STATE)
            .queryParam("oauth_token", OAUTH_TOKEN)
            .build()
            .toURL();
    assertThrows(OAuthAuthenticationException.class, () -> oAuthAuthenticator.callback(requestURL));
  }
}
