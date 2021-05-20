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

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.HttpMethod.POST;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.*;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.core.db.cascade.CascadeEventSubscriber;
import org.eclipse.che.inject.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remove user from Keycloak server on {@link
 * org.eclipse.che.api.user.server.event.BeforeUserRemovedEvent}. Turn on with {@code
 * che.keycloak.cascade_user_removal_enabled} property.
 *
 * <p>For correct work need to set keycloak admin credentials via {@code
 * che.keycloak.admin_username} and {@code che.keycloak.admin_password} properties.
 */
@Singleton
public class KeycloakUserRemover {
  private static final Logger LOG = LoggerFactory.getLogger(KeycloakUserRemover.class);

  private final String keycloakUser;
  private final String keycloakPassword;
  private final HttpJsonRequestFactory requestFactory;

  private String keycloakRemoveUserUrl;
  private String keycloakTokenEndpoint;

  @Inject
  public KeycloakUserRemover(
      @Nullable @Named("che.keycloak.cascade_user_removal_enabled") boolean userRemovalEnabled,
      @Nullable @Named("che.keycloak.admin_username") String keycloakUser,
      @Nullable @Named("che.keycloak.admin_password") String keycloakPassword,
      KeycloakSettings keycloakSettings,
      OIDCInfo oidcInfo,
      HttpJsonRequestFactory requestFactory) {
    this.keycloakUser = keycloakUser;
    this.keycloakPassword = keycloakPassword;
    this.requestFactory = requestFactory;

    if (userRemovalEnabled) {
      String serverUrl = oidcInfo.getAuthServerURL();
      if (serverUrl == null) {
        throw new ConfigurationException(
            AUTH_SERVER_URL_SETTING
                + " or "
                + AUTH_SERVER_URL_INTERNAL_SETTING
                + " is not configured");
      }
      String realm = keycloakSettings.get().get(REALM_SETTING);
      if (realm == null) {
        throw new ConfigurationException(REALM_SETTING + " is not configured");
      }
      if (Strings.isNullOrEmpty(keycloakUser) || Strings.isNullOrEmpty(keycloakPassword)) {
        throw new ConfigurationException("Keycloak administrator username or password not set.");
      }
      this.keycloakTokenEndpoint = serverUrl + "/realms/master/protocol/openid-connect/token";
      this.keycloakRemoveUserUrl = serverUrl + "/admin/realms/" + realm + "/users/";
    }
  }

  /**
   * Remove user from Keycloak server by given user id.
   *
   * @param userId the user if to remove
   * @throws ServerException when can't remove user from Keycloak
   */
  public void removeUserFromKeycloak(String userId) throws ServerException {
    try {
      String token = requestToken();
      int responseCode =
          requestFactory
              .fromUrl(keycloakRemoveUserUrl + userId)
              .setAuthorizationHeader("Bearer " + token)
              .useDeleteMethod()
              .request()
              .getResponseCode();
      if (responseCode != 204) {
        throw new ServerException("Can't remove user from Keycloak. UserId:" + userId);
      }
    } catch (IOException | ApiException e) {
      LOG.warn("Exception during removing user from Keycloak", e);
      throw new ServerException("Exception during removing user from Keycloak", e);
    }
  }

  private String requestToken() throws ServerException {
    String accessToken = "";
    HttpURLConnection http = null;
    try {
      http = (HttpURLConnection) new URL(keycloakTokenEndpoint).openConnection();
      http.setConnectTimeout(60000);
      http.setReadTimeout(60000);
      http.setRequestMethod(POST);
      http.setAllowUserInteraction(false);
      http.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
      http.setInstanceFollowRedirects(true);
      http.setDoOutput(true);
      StringBuilder sb = new StringBuilder();
      sb.append("grant_type=password")
          .append("&username=")
          .append(keycloakUser)
          .append("&password=")
          .append(keycloakPassword)
          .append("&client_id=admin-cli");
      try (OutputStream output = http.getOutputStream()) {
        output.write(sb.toString().getBytes(UTF_8));
      }
      if (http.getResponseCode() != 200) {
        throw new ServerException(
            "Cannot get Keycloak access token. Server response: "
                + keycloakTokenEndpoint
                + " "
                + http.getResponseCode()
                + IoUtil.readStream(http.getErrorStream()));
      }
      final BufferedReader response =
          new BufferedReader(new InputStreamReader(http.getInputStream(), UTF_8));

      JsonParser jsonParser = new JsonParser();
      JsonElement jsonElement = jsonParser.parse(response);
      JsonObject asJsonObject = jsonElement.getAsJsonObject();
      if (asJsonObject.has("access_token")) {
        accessToken = asJsonObject.get("access_token").getAsString();
      }
    } catch (IOException | JsonSyntaxException ex) {
      LOG.error(ex.getMessage(), ex);
      throw new ServerException("Cannot get Keycloak access token.", ex);
    } finally {
      if (http != null) {
        http.disconnect();
      }
    }
    return accessToken;
  }

  @Singleton
  public static class RemoveUserListener extends CascadeEventSubscriber<BeforeUserRemovedEvent> {
    @Inject private EventService eventService;
    @Inject private KeycloakUserRemover keycloakUserRemover;

    @Inject
    @Nullable
    @Named("che.keycloak.cascade_user_removal_enabled")
    boolean userRemovalEnabled;

    @PostConstruct
    public void subscribe() {
      if (userRemovalEnabled) {
        eventService.subscribe(this, BeforeUserRemovedEvent.class);
      }
    }

    @PreDestroy
    public void unsubscribe() {
      if (userRemovalEnabled) {
        eventService.unsubscribe(this, BeforeUserRemovedEvent.class);
      }
    }

    @Override
    public void onCascadeEvent(BeforeUserRemovedEvent event) throws Exception {
      keycloakUserRemover.removeUserFromKeycloak(event.getUser().getId());
    }
  }
}
