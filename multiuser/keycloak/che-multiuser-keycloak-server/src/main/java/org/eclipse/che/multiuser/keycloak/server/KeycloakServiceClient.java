/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.REALM_SETTING;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.keycloak.shared.dto.KeycloakErrorResponse;
import org.eclipse.che.multiuser.keycloak.shared.dto.KeycloakTokenResponse;

/**
 * Helps to perform keycloak operations and provide correct errors handling.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@Singleton
public class KeycloakServiceClient {

  private KeycloakSettings keycloakSettings;
  private final OIDCInfo oidcInfo;

  private static final Pattern assotiateUserPattern =
      Pattern.compile("User (.+) is not associated with identity provider (.+)");

  private static final Gson gson = new Gson();
  private JwtParser jwtParser;

  @Inject
  public KeycloakServiceClient(
      KeycloakSettings keycloakSettings, OIDCInfo oidcInfo, JwtParser jwtParser) {
    this.keycloakSettings = keycloakSettings;
    this.oidcInfo = oidcInfo;
    this.jwtParser = jwtParser;
  }

  /**
   * Generates URL for account linking redirect
   *
   * @param token client jwt token
   * @param oauthProvider provider name
   * @param redirectAfterLogin URL to return after login
   * @return URL to redirect client to perform account linking
   */
  public String getAccountLinkingURL(
      String token, String oauthProvider, String redirectAfterLogin) {

    Claims claims = jwtParser.parseClaimsJws(token).getBody();
    final String clientId = claims.get("azp", String.class);
    final String sessionState = claims.get("session_state", String.class);
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }

    final String nonce = UUID.randomUUID().toString();
    final String input = nonce + sessionState + clientId + oauthProvider;
    byte[] check = md.digest(input.getBytes(StandardCharsets.UTF_8));
    final String hash = Base64.getUrlEncoder().encodeToString(check);

    return UriBuilder.fromUri(oidcInfo.getAuthServerPublicURL())
        .path("/realms/{realm}/broker/{provider}/link")
        .queryParam("nonce", nonce)
        .queryParam("hash", hash)
        .queryParam("client_id", clientId)
        .queryParam("redirect_uri", redirectAfterLogin)
        .build(keycloakSettings.get().get(REALM_SETTING), oauthProvider)
        .toString();
  }

  /**
   * Gets auth token from given identity provider.
   *
   * @param oauthProvider provider name
   * @return KeycloakTokenResponse token response
   * @throws ForbiddenException when HTTP request was forbidden
   * @throws BadRequestException when HTTP request considered as bad
   * @throws IOException when unable to parse error response
   * @throws NotFoundException when requested URL not found
   * @throws ServerException when other error occurs
   * @throws UnauthorizedException when no token present for user or user not linked to provider
   */
  public KeycloakTokenResponse getIdentityProviderToken(String oauthProvider)
      throws ForbiddenException, BadRequestException, IOException, NotFoundException,
          ServerException, UnauthorizedException {
    String url =
        UriBuilder.fromUri(oidcInfo.getAuthServerURL())
            .path("/realms/{realm}/broker/{provider}/token")
            .build(keycloakSettings.get().get(REALM_SETTING), oauthProvider)
            .toString();
    try {
      String response = doRequest(url, HttpMethod.GET, null);
      // Successful answer is not a json, but key=value&foo=bar format pairs
      return DtoFactory.getInstance()
          .createDtoFromJson(toJson(response), KeycloakTokenResponse.class);
    } catch (BadRequestException e) {
      if (assotiateUserPattern.matcher(e.getMessage()).matches()) {
        // If user has no link with identity provider yet,
        // we should threat this as unauthorized and send to OAuth login page.
        throw new UnauthorizedException(e.getMessage());
      }
      throw e;
    }
  }

  private String doRequest(String url, String method, List<Pair<String, ?>> parameters)
      throws IOException, ServerException, ForbiddenException, NotFoundException,
          UnauthorizedException, BadRequestException {
    final String authToken = EnvironmentContext.getCurrent().getSubject().getToken();
    final boolean hasQueryParams = parameters != null && !parameters.isEmpty();
    if (hasQueryParams) {
      final UriBuilder ub = UriBuilder.fromUri(url);
      for (Pair<String, ?> parameter : parameters) {
        ub.queryParam(parameter.first, parameter.second);
      }
      url = ub.build().toString();
    }
    final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setConnectTimeout(60000);
    conn.setReadTimeout(60000);

    try {
      conn.setRequestMethod(method);
      // drop a hint for server side that we want to receive application/json
      conn.addRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
      if (authToken != null) {
        conn.setRequestProperty(HttpHeaders.AUTHORIZATION, "bearer " + authToken);
      }
      final int responseCode = conn.getResponseCode();
      if ((responseCode / 100) != 2) {
        InputStream in = conn.getErrorStream();
        if (in == null) {
          in = conn.getInputStream();
        }
        final String str;
        try (Reader reader = new InputStreamReader(in)) {
          str = CharStreams.toString(reader);
        }
        final String contentType = conn.getContentType();
        if (contentType != null
            && (contentType.startsWith(MediaType.APPLICATION_JSON)
                || contentType.startsWith("application/vnd.api+json"))) {
          final KeycloakErrorResponse serviceError =
              DtoFactory.getInstance().createDtoFromJson(str, KeycloakErrorResponse.class);
          if (responseCode == Response.Status.FORBIDDEN.getStatusCode()) {
            throw new ForbiddenException(serviceError.getErrorMessage());
          } else if (responseCode == Response.Status.NOT_FOUND.getStatusCode()) {
            throw new NotFoundException(serviceError.getErrorMessage());
          } else if (responseCode == Response.Status.UNAUTHORIZED.getStatusCode()) {
            throw new UnauthorizedException(serviceError.getErrorMessage());
          } else if (responseCode == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            throw new ServerException(serviceError.getErrorMessage());
          } else if (responseCode == Response.Status.BAD_REQUEST.getStatusCode()) {
            throw new BadRequestException(serviceError.getErrorMessage());
          }
          throw new ServerException(serviceError.getErrorMessage());
        }
        // Can't parse content as json or content has format other we expect for error.
        throw new IOException(
            String.format(
                "Failed access: %s, method: %s, response code: %d, message: %s",
                UriBuilder.fromUri(url).replaceQuery("token").build(), method, responseCode, str));
      }
      try (Reader reader = new InputStreamReader(conn.getInputStream())) {
        return CharStreams.toString(reader);
      }
    } finally {
      conn.disconnect();
    }
  }

  /** Converts key=value&foo=bar string into json if necessary */
  private static String toJson(String source) {
    if (source == null) {
      return null;
    }
    try {
      // Check that the source is valid Json Object (can be returned as a Map)
      gson.<Map<String, String>>fromJson(source, Map.class);
      return source;
    } catch (JsonSyntaxException notJsonException) {
      // The source is not valid Json: let's see if
      // it is in 'key=value&foo=bar' format
      Map<String, String> queryPairs = new HashMap<>();
      Arrays.stream(source.split("&"))
          .forEach(
              p -> {
                int delimiterIndex = p.indexOf("=");
                queryPairs.put(p.substring(0, delimiterIndex), p.substring(delimiterIndex + 1));
              });
      return gson.toJson(queryPairs);
    }
  }
}
