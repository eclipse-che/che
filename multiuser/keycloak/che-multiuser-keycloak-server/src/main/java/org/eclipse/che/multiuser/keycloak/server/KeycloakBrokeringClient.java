package org.eclipse.che.multiuser.keycloak.server;

import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.AUTH_SERVER_URL_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.REALM_SETTING;

import com.google.common.io.CharStreams;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.impl.DefaultClaims;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
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
 * Helps to perform brokering operations and
 * correct errors handling.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
public class KeycloakBrokeringClient {

  private KeycloakSettings keycloakSettings;

  public KeycloakBrokeringClient(KeycloakSettings keycloakSettings) {
    this.keycloakSettings = keycloakSettings;
  }

  public String getAccountLinkingURL(Jwt token, String oauthProvider, String redirectAfterLogin) {

    DefaultClaims claims = (DefaultClaims) token.getBody();
    final String clientId = claims.getAudience();

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

    String accountLinkUrl =
        UriBuilder.fromUri(keycloakSettings.get().get(AUTH_SERVER_URL_SETTING))
            .path("/realms/{realm}/broker/{provider}/link")
            .queryParam("nonce", nonce)
            .queryParam("hash", hash)
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectAfterLogin)
            .build(keycloakSettings.get().get(REALM_SETTING), oauthProvider)
            .toString();

    return accountLinkUrl;
  }


  public KeycloakTokenResponse getToken(String oauthProvider)
      throws ForbiddenException, BadRequestException, IOException, ConflictException, NotFoundException, ServerException, UnauthorizedException {
    String url = UriBuilder.fromUri(keycloakSettings.get().get(AUTH_SERVER_URL_SETTING))
        .path("/realms/{realm}/broker/{provider}/token")
        .build(keycloakSettings.get().get(REALM_SETTING), oauthProvider)
        .toString();
    String response = doRequest(url, HttpMethod.GET, null, null);
    return  DtoFactory.getInstance().createDtoFromJson(response, KeycloakTokenResponse.class);
  }



  protected String doRequest(
      String url,
      String method,
      Object body,
      List<Pair<String, ?>> parameters)
      throws IOException, ServerException, ForbiddenException, NotFoundException,
      UnauthorizedException, ConflictException, BadRequestException {
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
        conn.setRequestProperty(HttpHeaders.AUTHORIZATION, authToken);
      }
      if (body != null) {
        conn.addRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        conn.setDoOutput(true);

        if (HttpMethod.DELETE.equals(method)) { // to avoid jdk bug described here
          // http://bugs.java.com/view_bug.do?bug_id=7157360
          conn.setRequestMethod(HttpMethod.POST);
          conn.setRequestProperty("X-HTTP-Method-Override", HttpMethod.DELETE);
        }

        try (OutputStream output = conn.getOutputStream()) {
          output.write(DtoFactory.getInstance().toJson(body).getBytes());
        }
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
            } else if (responseCode == Response.Status.CONFLICT.getStatusCode()) {
              throw new ConflictException(serviceError.getErrorMessage());
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
      final String contentType = conn.getContentType();
      if (responseCode != HttpURLConnection.HTTP_NO_CONTENT
          && contentType != null
          && !(contentType.startsWith(MediaType.APPLICATION_JSON)
          || contentType.startsWith("application/vnd.api+json"))) {
        throw new IOException(conn.getResponseMessage());
      }

      try (Reader reader = new InputStreamReader(conn.getInputStream())) {
        return CharStreams.toString(reader);
      }
    } finally {
      conn.disconnect();
    }
  }
}
