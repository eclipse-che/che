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
package org.eclipse.che.selenium.core.client.keycloak;

import static com.google.common.io.BaseEncoding.base64;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.LocalDateTime.now;
import static java.time.ZoneId.systemDefault;

import com.google.gson.Gson;
import java.io.Reader;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.che.selenium.core.client.TestAuthServiceClient;
import org.eclipse.che.selenium.core.client.keycloak.KeycloakToken.TokenDetails;

public abstract class AbstractKeycloakTestAuthServiceClient implements TestAuthServiceClient {

  private static final long MIN_TOKEN_LIFETIME_SEC = 30;

  private final Gson gson;

  protected final ConcurrentHashMap<String, KeycloakToken> tokens;

  public AbstractKeycloakTestAuthServiceClient() {
    this.tokens = new ConcurrentHashMap<>();
    this.gson = new Gson();
  }

  @Override
  public String login(String username, String password) throws Exception {
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

    KeycloakToken newToken = loginRequest(username, password);

    tokens.put(username, newToken);
    return newToken.getAccessToken();
  }

  protected KeycloakToken readerToKeycloakToken(final Reader response) {
    KeycloakToken token;
    token = gson.fromJson(response, KeycloakToken.class);
    token.setAccessDetails(
        gson.fromJson(
            new String(base64().decode(token.getAccessToken().split("\\.")[1]), UTF_8),
            TokenDetails.class));
    token.setRefreshDetails(
        gson.fromJson(
            new String(base64().decode(token.getRefreshToken().split("\\.")[1]), UTF_8),
            TokenDetails.class));
    return token;
  }

  @Override
  public abstract void logout(String token) throws Exception;

  protected abstract KeycloakToken loginRequest(String username, String password);

  protected abstract KeycloakToken refreshRequest(KeycloakToken token);
}
