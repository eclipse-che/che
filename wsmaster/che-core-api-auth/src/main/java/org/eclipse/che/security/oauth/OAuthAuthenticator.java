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
package org.eclipse.che.security.oauth;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.security.oauth.shared.OAuthTokenProvider;
import org.eclipse.che.security.oauth.shared.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Authentication service which allow get access token from OAuth provider site. */
public abstract class OAuthAuthenticator {
  private static final Logger LOG = LoggerFactory.getLogger(OAuthAuthenticator.class);

  protected AuthorizationCodeFlow flow;
  protected Map<Pattern, String> redirectUrisMap;

  /**
   * @see {@link #configure(String, String, String[], String, String, MemoryDataStoreFactory, List)}
   */
  protected void configure(
      String clientId,
      String clientSecret,
      String[] redirectUris,
      String authUri,
      String tokenUri,
      MemoryDataStoreFactory dataStoreFactory)
      throws IOException {
    configure(
        clientId,
        clientSecret,
        redirectUris,
        authUri,
        tokenUri,
        dataStoreFactory,
        Collections.emptyList());
  }

  /**
   * This method should be invoked by child class for initialization default instance of {@link
   * AuthorizationCodeFlow} that will be used for authorization
   */
  protected void configure(
      String clientId,
      String clientSecret,
      String[] redirectUris,
      String authUri,
      String tokenUri,
      MemoryDataStoreFactory dataStoreFactory,
      List<String> scopes)
      throws IOException {
    final AuthorizationCodeFlow authorizationFlow =
        new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                new NetHttpTransport(),
                new JacksonFactory(),
                new GenericUrl(tokenUri),
                new ClientParametersAuthentication(clientId, clientSecret),
                clientId,
                authUri)
            .setDataStoreFactory(dataStoreFactory)
            .setScopes(scopes)
            .build();

    LOG.debug(
        "clientId={}, clientSecret={}, redirectUris={} , authUri={}, tokenUri={}, dataStoreFactory={}",
        clientId,
        clientSecret,
        redirectUris,
        authUri,
        tokenUri,
        dataStoreFactory);

    configure(authorizationFlow, Arrays.asList(redirectUris));
  }

  /**
   * This method should be invoked by child class for setting instance of {@link
   * AuthorizationCodeFlow} that will be used for authorization
   */
  protected void configure(AuthorizationCodeFlow flow, List<String> redirectUris) {
    this.flow = flow;
    this.redirectUrisMap = new HashMap<>(redirectUris.size());
    for (String uri : redirectUris) {
      // Redirect URI may be in form urn:ietf:wg:oauth:2.0:oob os use java.net.URI instead of
      // java.net.URL
      this.redirectUrisMap.put(
          Pattern.compile("([a-z0-9\\-]+\\.)?" + URI.create(uri).getHost()), uri);
    }
  }

  /**
   * Create authentication URL.
   *
   * @param requestUrl URL of current HTTP request. This parameter required to be able determine URL
   *     for redirection after authentication. If URL contains query parameters they will be copy to
   *     'state' parameter and returned to callback method.
   * @param scopes specify exactly what type of access needed
   * @return URL for authentication
   */
  public String getAuthenticateUrl(URL requestUrl, List<String> scopes)
      throws OAuthAuthenticationException {
    if (!isConfigured()) {
      throw new OAuthAuthenticationException("Authenticator is not configured");
    }

    AuthorizationCodeRequestUrl url =
        flow.newAuthorizationUrl().setRedirectUri(findRedirectUrl(requestUrl)).setScopes(scopes);
    url.setState(prepareState(requestUrl));
    return url.build();
  }

  protected String prepareState(URL requestUrl) {
    StringBuilder state = new StringBuilder();
    String query = requestUrl.getQuery();
    if (query != null) {
      if (state.length() > 0) {
        state.append('&');
      }
      state.append(query);
    }
    return state.toString();
  }

  protected String findRedirectUrl(URL requestUrl) {
    final String requestHost = requestUrl.getHost();
    for (Map.Entry<Pattern, String> e : redirectUrisMap.entrySet()) {
      if (e.getKey().matcher(requestHost).matches()) {
        return e.getValue();
      }
    }
    return null; // TODO : throw exception instead of return null ???
  }

  /**
   * Process callback request.
   *
   * @param requestUrl request URI. URI should contain authorization code generated by authorization
   *     server
   * @param scopes specify exactly what type of access needed. This list must be exactly the same as
   *     list passed to the method {@link #getAuthenticateUrl(URL, java.util.List)}
   * @return id of authenticated user
   * @throws OAuthAuthenticationException if authentication failed or <code>requestUrl</code> does
   *     not contain required parameters, e.g. 'code'
   */
  public String callback(URL requestUrl, List<String> scopes) throws OAuthAuthenticationException {
    if (!isConfigured()) {
      throw new OAuthAuthenticationException("Authenticator is not configured");
    }

    AuthorizationCodeResponseUrl authorizationCodeResponseUrl =
        new AuthorizationCodeResponseUrl(requestUrl.toString());
    final String error = authorizationCodeResponseUrl.getError();
    if (error != null) {
      throw new OAuthAuthenticationException("Authentication failed: " + error);
    }
    final String code = authorizationCodeResponseUrl.getCode();
    if (code == null) {
      throw new OAuthAuthenticationException("Missing authorization code. ");
    }

    try {
      TokenResponse tokenResponse =
          flow.newTokenRequest(code)
              .setRequestInitializer(
                  request -> {
                    if (request.getParser() == null) {
                      request.setParser(flow.getJsonFactory().createJsonObjectParser());
                    }
                    request.getHeaders().setAccept(MediaType.APPLICATION_JSON);
                  })
              .setRedirectUri(findRedirectUrl(requestUrl))
              .setScopes(scopes)
              .execute();
      String userId = getUserFromUrl(authorizationCodeResponseUrl);
      if (userId == null) {
        userId =
            getUser(newDto(OAuthToken.class).withToken(tokenResponse.getAccessToken())).getId();
      }
      flow.createAndStoreCredential(tokenResponse, userId);
      return userId;
    } catch (IOException ioe) {
      throw new OAuthAuthenticationException(ioe.getMessage());
    }
  }

  /**
   * Get user info.
   *
   * @param accessToken oauth access token
   * @return user info
   * @throws OAuthAuthenticationException if fail to get user info
   */
  public abstract User getUser(OAuthToken accessToken) throws OAuthAuthenticationException;

  /**
   * Get the name of OAuth provider supported by current implementation.
   *
   * @return oauth provider name
   */
  public abstract String getOAuthProvider();

  private String getUserFromUrl(AuthorizationCodeResponseUrl authorizationCodeResponseUrl)
      throws IOException {
    String state = authorizationCodeResponseUrl.getState();
    if (!(state == null || state.isEmpty())) {
      String decoded = URLDecoder.decode(state, "UTF-8");
      String[] items = decoded.split("&");
      for (String str : items) {
        if (str.startsWith("userId=")) {
          return str.substring(7, str.length());
        }
      }
    }
    return null;
  }

  protected <O> O getJson(String getUserUrl, Class<O> userClass)
      throws OAuthAuthenticationException {
    HttpURLConnection urlConnection = null;
    InputStream urlInputStream = null;

    try {
      urlConnection = (HttpURLConnection) new URL(getUserUrl).openConnection();
      urlInputStream = urlConnection.getInputStream();
      return JsonHelper.fromJson(urlInputStream, userClass, null);
    } catch (JsonParseException | IOException e) {
      throw new OAuthAuthenticationException(e.getMessage(), e);
    } finally {
      if (urlInputStream != null) {
        try {
          urlInputStream.close();
        } catch (IOException ignored) {
        }
      }

      if (urlConnection != null) {
        urlConnection.disconnect();
      }
    }
  }

  /**
   * Return authorization token by userId.
   *
   * <p>WARN!!!. DO not use it directly.
   *
   * @param userId user identifier
   * @return token value or {@code null}. When user have valid token then it will be returned, when
   *     user have expired token and it can be refreshed then refreshed value will be returned, when
   *     none token found for user then {@code null} will be returned, when user have expired token
   *     and it can't be refreshed then {@code null} will be returned
   * @throws IOException when error occurs during token loading
   * @see OAuthTokenProvider#getToken(String, String)
   */
  public OAuthToken getToken(String userId) throws IOException {
    if (!isConfigured()) {
      throw new IOException("Authenticator is not configured");
    }
    Credential credential = flow.loadCredential(userId);
    if (credential == null) {
      return null;
    }
    final Long expirationTime = credential.getExpiresInSeconds();
    if (expirationTime != null && expirationTime < 0) {
      boolean tokenRefreshed;
      try {
        tokenRefreshed = credential.refreshToken();
      } catch (IOException ioEx) {
        tokenRefreshed = false;
      }
      if (tokenRefreshed) {
        credential = flow.loadCredential(userId);
      } else {
        // if token is not refreshed then old value should be invalidated
        // and null result should be returned
        try {
          invalidateToken(userId);
        } catch (IOException ignored) {
        }
        return null;
      }
    }
    return newDto(OAuthToken.class).withToken(credential.getAccessToken());
  }

  /**
   * Invalidate OAuth token for specified user.
   *
   * @param userId user
   * @return <code>true</code> if OAuth token invalidated and <code>false</code> otherwise, e.g. if
   *     user does not have token yet
   */
  public boolean invalidateToken(String userId) throws IOException {
    Credential credential = flow.loadCredential(userId);
    if (credential != null) {
      flow.getCredentialDataStore().delete(userId);
      return true;
    }
    return false;
  }

  /**
   * Checks configuring of authenticator
   *
   * @return true only if authenticator have valid configuration data and it is able to authorize
   *     otherwise returns false
   */
  public boolean isConfigured() {
    return flow != null;
  }
}
