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
package org.eclipse.che.selenium.core.client;

import static com.google.common.io.BaseEncoding.base64;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;
import static javax.ws.rs.HttpMethod.POST;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.selenium.core.client.KeycloakToken.TokenDetails;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.eclipse.che.selenium.core.provider.TestOfflineToAccessTokenExchangeApiEndpointUrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mykhailo Kuznietsov
 * @author Anton Korneta
 */
@Singleton
public class KeycloakTestAuthServiceClient implements TestAuthServiceClient {

  private static final Logger LOG = LoggerFactory.getLogger(KeycloakTestAuthServiceClient.class);

  private static final String CONTENT_TYPE = "Content-Type";
  private static final String FORM_MIME_TYPE = "application/x-www-form-urlencoded";
  private static final String GRANT_TYPE = "grant_type";
  private static final String CLIENT_ID_PARAM = "client_id";
  private static final String PASSWORD = "password";
  private static final String REFRESH_TOKEN = "refresh_token";

  private static final long MIN_TOKEN_LIFETIME_SEC = 30;

  private final String apiEndpoint;
  private final DefaultHttpJsonRequestFactory requestFactory;
  private final TestOfflineToAccessTokenExchangeApiEndpointUrlProvider
      testOfflineToAccessTokenExchangeApiEndpointUrlProvider;

  private final Gson gson;

  private final KeycloakSettings keycloakSettings;
  private final ConcurrentMap<String, KeycloakToken> tokens;

  @Inject
  public KeycloakTestAuthServiceClient(
      TestApiEndpointUrlProvider cheApiEndpointProvider,
      DefaultHttpJsonRequestFactory requestFactory,
      TestOfflineToAccessTokenExchangeApiEndpointUrlProvider
          testOfflineToAccessTokenExchangeApiEndpointUrlProvider) {
    this.apiEndpoint = cheApiEndpointProvider.get().toString();
    this.requestFactory = requestFactory;
    this.gson = new Gson();
    this.tokens = new ConcurrentHashMap<>();
    this.keycloakSettings = getKeycloakConfiguration();
    this.testOfflineToAccessTokenExchangeApiEndpointUrlProvider =
        testOfflineToAccessTokenExchangeApiEndpointUrlProvider;
  }

  @Override
  public String login(String username, String password, String offlineToken) throws Exception {
    final KeycloakToken token = tokens.get(username);
    if (token != null) {
      final long now = now().atZone(systemDefault()).toEpochSecond();
      if (token.getAccessDetails().getExpiresAt() - now < MIN_TOKEN_LIFETIME_SEC) {
        if (!(token.getRefreshDetails().getExpiresAt() - now < MIN_TOKEN_LIFETIME_SEC)) {
          final KeycloakToken refreshed = refreshRequest(token);
          tokens.replace(username, refreshed);
          return refreshed.getAccessToken();
        }
      } else {
        return token.getAccessToken();
      }
    }

    KeycloakToken newToken;
    if (!offlineToken.isEmpty()) {
      newToken = loginRequest(offlineToken);
    } else {
      newToken = loginRequest(username, password);
    }

    tokens.put(username, newToken);
    return newToken.getAccessToken();
  }

  @Override
  public void logout(String authToken) {
    try {
      requestFactory.fromUrl(keycloakSettings.getKeycloakLogoutEndpoint()).request();
    } catch (ApiException | IOException ex) {
      LOG.error(ex.getLocalizedMessage(), ex);
    }
  }

  private KeycloakToken loginRequest(String username, String password) {
    return requestToken(
        PASSWORD, ImmutableList.of(Pair.of("username", username), Pair.of("password", password)));
  }

  private KeycloakToken loginRequest(String offlineToken) {
    KeycloakToken token = null;
    HttpURLConnection http = null;
    try {
      http =
          (HttpURLConnection)
              new URL(testOfflineToAccessTokenExchangeApiEndpointUrlProvider.get().toString())
                  .openConnection();
      http.setRequestMethod(POST);
      http.setAllowUserInteraction(false);
      http.setRequestProperty(CONTENT_TYPE, FORM_MIME_TYPE);
      http.setInstanceFollowRedirects(true);
      http.setDoOutput(true);
      OutputStream output = http.getOutputStream();
      StringBuilder sb = new StringBuilder();
      sb.append(REFRESH_TOKEN).append('=').append(offlineToken);
      output.write(sb.toString().getBytes(UTF_8));
      if (http.getResponseCode() != 200) {
        throw new RuntimeException(
            "Can not get access token using the "
                + testOfflineToAccessTokenExchangeApiEndpointUrlProvider.get().toString()
                + " REST API. Server response code: "
                + http.getResponseCode()
                + IoUtil.readStream(http.getErrorStream()));
      }
      output.close();

      final BufferedReader response =
          new BufferedReader(new InputStreamReader(http.getInputStream(), UTF_8));
      KeycloakTokenContainer tokenContainer = gson.fromJson(response, KeycloakTokenContainer.class);
      token = tokenContainer.getToken();
      token.setAccessDetails(
          gson.fromJson(
              new String(base64().decode(token.getAccessToken().split("\\.")[1]), UTF_8),
              TokenDetails.class));
      token.setRefreshDetails(
          gson.fromJson(
              new String(base64().decode(token.getRefreshToken().split("\\.")[1]), UTF_8),
              TokenDetails.class));
    } catch (IOException | JsonSyntaxException ex) {
      LOG.error(ex.getMessage(), ex);
    } finally {
      if (http != null) {
        http.disconnect();
      }
    }
    return token;
  }

  private KeycloakToken refreshRequest(KeycloakToken prevToken) {
    return requestToken(
        REFRESH_TOKEN, ImmutableList.of(Pair.of("refresh_token", prevToken.getRefreshToken())));
  }

  private KeycloakToken requestToken(String grandType, List<Pair<String, ?>> params) {
    KeycloakToken token = null;
    HttpURLConnection http = null;
    final String keycloakTokenEndpoint = keycloakSettings.getKeycloakTokenEndpoint();
    if (keycloakTokenEndpoint == null) {
      throw new RuntimeException("Keycloak token endpoint is not configured");
    }
    try {

      http = (HttpURLConnection) new URL(keycloakTokenEndpoint).openConnection();
      http.setRequestMethod(POST);
      http.setAllowUserInteraction(false);
      http.setRequestProperty(CONTENT_TYPE, FORM_MIME_TYPE);
      http.setInstanceFollowRedirects(true);
      http.setDoOutput(true);
      OutputStream output = http.getOutputStream();
      StringBuilder sb = new StringBuilder();
      sb.append(GRANT_TYPE)
          .append('=')
          .append(grandType)
          .append('&')
          .append(CLIENT_ID_PARAM)
          .append('=')
          .append(keycloakSettings.getKeycloakClientId());
      for (Pair<String, ?> param : params) {
        sb.append('&').append(param.first).append('=').append(param.second);
      }
      output.write(sb.toString().getBytes(UTF_8));
      if (http.getResponseCode() != 200) {
        throw new RuntimeException(
            "Can not get access token using the KeyCloak REST API. Server response code: "
                + keycloakTokenEndpoint
                + " "
                + http.getResponseCode()
                + IoUtil.readStream(http.getErrorStream()));
      }
      output.close();

      final BufferedReader response =
          new BufferedReader(new InputStreamReader(http.getInputStream(), UTF_8));
      token = gson.fromJson(response, KeycloakToken.class);
      token.setAccessDetails(
          gson.fromJson(
              new String(base64().decode(token.getAccessToken().split("\\.")[1]), UTF_8),
              TokenDetails.class));
      token.setRefreshDetails(
          gson.fromJson(
              new String(base64().decode(token.getRefreshToken().split("\\.")[1]), UTF_8),
              TokenDetails.class));
    } catch (IOException | JsonSyntaxException ex) {
      LOG.error(ex.getMessage(), ex);
    } finally {
      if (http != null) {
        http.disconnect();
      }
    }
    return token;
  }

  private KeycloakSettings getKeycloakConfiguration() {
    try {
      return gson.fromJson(
          requestFactory
              .fromUrl(apiEndpoint + "keycloak/settings/")
              .useGetMethod()
              .request()
              .asString(),
          KeycloakSettings.class);
    } catch (ApiException | IOException | JsonSyntaxException ex) {
      throw new RuntimeException("Error during retrieving Che Keycloak configuration: ", ex);
    }
  }
}
