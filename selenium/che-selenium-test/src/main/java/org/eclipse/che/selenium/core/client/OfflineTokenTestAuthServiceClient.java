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

import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.inject.Singleton;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.selenium.core.client.KeycloakToken.TokenDetails;
import org.eclipse.che.selenium.core.provider.TestApiEndpointUrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Dmytro Nochevnov */
@Singleton
public class OfflineTokenTestAuthServiceClient extends KeycloakTestAuthServiceClient {

  private static final Logger LOG =
      LoggerFactory.getLogger(OfflineTokenTestAuthServiceClient.class);

  private final String offlineToAccessTokenExchangeEndpoint;
  private final String offlineToken;

  @Inject
  public OfflineTokenTestAuthServiceClient(
      TestApiEndpointUrlProvider cheApiEndpointProvider,
      DefaultHttpJsonRequestFactory requestFactory,
      @Named("che.offline.token") String offlineToken,
      @Named("che.offline.to.access.token.exchange.endpoint")
          String offlineToAccessTokenExchangeEndpoint) {
    super(cheApiEndpointProvider, requestFactory);
    this.offlineToAccessTokenExchangeEndpoint = offlineToAccessTokenExchangeEndpoint;
    this.offlineToken = offlineToken;
  }

  @Override
  public String login(String username, String password) throws Exception {
    final KeycloakToken token = tokens.get(username);
    if (token != null) {
      final long now = now().atZone(systemDefault()).toEpochSecond();
      if (token.getAccessDetails().getExpiresAt() - now < token.getExpiresIn()) {
        if (!(token.getRefreshDetails().getExpiresAt() - now < token.getExpiresIn())) {
          final KeycloakToken refreshed = requestAccessToken(token.getRefreshToken());
          tokens.replace(username, refreshed);
          return refreshed.getAccessToken();
        }
      } else {
        return token.getAccessToken();
      }
    }

    final KeycloakToken newToken = requestAccessToken(offlineToken);
    tokens.put(username, newToken);
    return newToken.getAccessToken();
  }

  private KeycloakToken requestAccessToken(String offlineToken) {
    KeycloakToken token = null;
    HttpURLConnection http = null;
    try {
      http = (HttpURLConnection) new URL(offlineToAccessTokenExchangeEndpoint).openConnection();
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
                + offlineToAccessTokenExchangeEndpoint
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
}
