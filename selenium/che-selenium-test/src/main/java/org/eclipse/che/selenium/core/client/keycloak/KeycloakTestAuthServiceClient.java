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

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.HttpMethod.POST;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.DefaultHttpJsonRequestFactory;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Mykhailo Kuznietsov
 * @author Anton Korneta
 */
@Singleton
public class KeycloakTestAuthServiceClient extends AbstractKeycloakTestAuthServiceClient {

  private static final Logger LOG = LoggerFactory.getLogger(KeycloakTestAuthServiceClient.class);

  private static final String CONTENT_TYPE = "Content-Type";
  private static final String FORM_MIME_TYPE = "application/x-www-form-urlencoded";
  private static final String GRANT_TYPE = "grant_type";
  private static final String CLIENT_ID_PARAM = "client_id";
  private static final String PASSWORD = "password";
  private static final String REFRESH_TOKEN = "refresh_token";

  private final DefaultHttpJsonRequestFactory requestFactory;

  private final KeycloakSettings keycloakSettings;

  @Inject
  public KeycloakTestAuthServiceClient(
      DefaultHttpJsonRequestFactory requestFactory,
      TestKeycloakSettingsServiceClient testKeycloakSettingsServiceClient) {
    super();
    this.requestFactory = requestFactory;
    this.keycloakSettings = testKeycloakSettingsServiceClient.read();
  }

  @Override
  public void logout(String authToken) {
    try {
      requestFactory.fromUrl(keycloakSettings.getKeycloakLogoutEndpoint()).request();
    } catch (ApiException | IOException ex) {
      LOG.error(ex.getLocalizedMessage(), ex);
    }
  }

  protected KeycloakToken loginRequest(String username, String password) {
    return requestToken(
        PASSWORD, ImmutableList.of(Pair.of("username", username), Pair.of("password", password)));
  }

  protected KeycloakToken refreshRequest(KeycloakToken prevToken) {
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
            "Cannot get access token using the KeyCloak REST API. Server response code: "
                + keycloakTokenEndpoint
                + " "
                + http.getResponseCode()
                + IoUtil.readStream(http.getErrorStream()));
      }
      output.close();

      final BufferedReader response =
          new BufferedReader(new InputStreamReader(http.getInputStream(), UTF_8));

      token = readerToKeycloakToken(response);

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
