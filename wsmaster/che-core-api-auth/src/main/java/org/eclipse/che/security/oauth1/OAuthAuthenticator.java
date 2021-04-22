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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.net.URLDecoder.decode;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.auth.oauth.OAuthRsaSigner;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.Base64;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;

/**
 * Authentication service which allows get access token from OAuth provider site.
 *
 * @author Kevin Pollet
 * @author Igor Vinokur
 */
public abstract class OAuthAuthenticator {
  private static final String USER_ID_PARAM_KEY = "userId";
  private static final String REQUEST_METHOD_PARAM_KEY = "request_method";
  private static final String SIGNATURE_METHOD_PARAM_KEY = "signature_method";
  private static final String STATE_PARAM_KEY = "state";
  private static final String OAUTH_TOKEN_PARAM_KEY = "oauth_token";
  private static final String OAUTH_VERIFIER_PARAM_KEY = "oauth_verifier";

  private final String clientId;
  private final String clientSecret;
  private final String privateKey;
  private final String requestTokenUri;
  private final String accessTokenUri;
  private final String authorizeTokenUri;
  private final String redirectUri;
  private final HttpTransport httpTransport;
  private final Map<String, OAuthCredentialsResponse> credentialsStore;
  private final ReentrantLock credentialsStoreLock;
  private final Map<String, String> sharedTokenSecrets;

  protected OAuthAuthenticator(
      String clientId,
      String requestTokenUri,
      String accessTokenUri,
      String authorizeTokenUri,
      String redirectUri,
      @Nullable String clientSecret,
      @Nullable String privateKey) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.privateKey = privateKey;
    this.requestTokenUri = requestTokenUri;
    this.accessTokenUri = accessTokenUri;
    this.authorizeTokenUri = authorizeTokenUri;
    this.redirectUri = redirectUri;
    this.httpTransport = new NetHttpTransport();
    this.credentialsStore = new HashMap<>();
    this.credentialsStoreLock = new ReentrantLock();
    this.sharedTokenSecrets = new HashMap<>();
  }

  /**
   * Create authentication URL.
   *
   * @param requestUrl URL of current HTTP request. This parameter required to be able determine URL
   *     for redirection after authentication. If URL contains query parameters they will be copied
   *     to 'state' parameter and returned to callback method.
   * @param requestMethod HTTP request method that will be used to request temporary token
   * @param signatureMethod OAuth signature algorithm
   * @return URL for authentication.
   * @throws OAuthAuthenticationException if authentication failed.
   */
  String getAuthenticateUrl(
      final URL requestUrl,
      @Nullable final String requestMethod,
      @Nullable final String signatureMethod)
      throws OAuthAuthenticationException {
    try {
      final GenericUrl callbackUrl = new GenericUrl(redirectUri);
      String userId = getParameterFromState(requestUrl.getQuery(), USER_ID_PARAM_KEY);
      String currentUserId = EnvironmentContext.getCurrent().getSubject().getUserId();
      if (userId != null) {
        if (currentUserId.equals(userId)) {
          callbackUrl.put(STATE_PARAM_KEY, requestUrl.getQuery());
        } else {
          throw new OAuthAuthenticationException(
              "Provided query parameter "
                  + USER_ID_PARAM_KEY
                  + "="
                  + userId
                  + " does not match the current user id: "
                  + currentUserId);
        }
      } else {
        callbackUrl.put(
            STATE_PARAM_KEY, requestUrl.getQuery() + "&" + USER_ID_PARAM_KEY + "=" + currentUserId);
      }

      OAuthGetTemporaryToken temporaryToken;
      if (requestMethod != null && "post".equalsIgnoreCase(requestMethod)) {
        temporaryToken = new OAuthPostTemporaryToken(requestTokenUri);
      } else {
        temporaryToken = new OAuthGetTemporaryToken(requestTokenUri);
      }
      if (signatureMethod != null && "rsa".equalsIgnoreCase(signatureMethod)) {
        temporaryToken.signer = getOAuthRsaSigner();
      } else {
        temporaryToken.signer = getOAuthHmacSigner(null, null);
      }
      temporaryToken.consumerKey = clientId;
      temporaryToken.callback = callbackUrl.build();
      temporaryToken.transport = httpTransport;
      final OAuthCredentialsResponse credentialsResponse = temporaryToken.execute();
      final OAuthAuthorizeTemporaryTokenUrl authorizeTemporaryTokenUrl =
          new OAuthAuthorizeTemporaryTokenUrl(authorizeTokenUri);
      authorizeTemporaryTokenUrl.temporaryToken = credentialsResponse.token;

      sharedTokenSecrets.put(credentialsResponse.token, credentialsResponse.tokenSecret);

      return authorizeTemporaryTokenUrl.build();
    } catch (Exception e) {
      throw new OAuthAuthenticationException(e.getMessage());
    }
  }

  /**
   * Process callback request.
   *
   * @param requestUrl request URI. URI should contain OAuth token and OAuth verifier.
   * @return id of authenticated user
   * @throws OAuthAuthenticationException if authentication failed or {@code requestUrl} does not
   *     contain required parameters.
   */
  String callback(final URL requestUrl) throws OAuthAuthenticationException {
    try {
      final GenericUrl callbackUrl = new GenericUrl(requestUrl.toString());

      if (callbackUrl.getFirst(OAUTH_TOKEN_PARAM_KEY) == null) {
        throw new OAuthAuthenticationException("Missing oauth_token parameter");
      }

      if (callbackUrl.getFirst(OAUTH_VERIFIER_PARAM_KEY) == null) {
        throw new OAuthAuthenticationException("Missing oauth_verifier parameter");
      }

      final String state = (String) callbackUrl.getFirst(STATE_PARAM_KEY);

      String requestMethod = getParameterFromState(state, REQUEST_METHOD_PARAM_KEY);
      String signatureMethod = getParameterFromState(state, SIGNATURE_METHOD_PARAM_KEY);

      final String oauthTemporaryToken = (String) callbackUrl.getFirst(OAUTH_TOKEN_PARAM_KEY);

      OAuthGetAccessToken getAccessToken;
      if (requestMethod != null && "post".equalsIgnoreCase(requestMethod)) {
        getAccessToken = new OAuthPostAccessToken(accessTokenUri);
      } else {
        getAccessToken = new OAuthGetAccessToken(accessTokenUri);
      }
      getAccessToken.consumerKey = clientId;
      getAccessToken.temporaryToken = oauthTemporaryToken;
      getAccessToken.verifier = (String) callbackUrl.getFirst(OAUTH_VERIFIER_PARAM_KEY);
      getAccessToken.transport = httpTransport;
      if (signatureMethod != null && "rsa".equalsIgnoreCase(signatureMethod)) {
        getAccessToken.signer = getOAuthRsaSigner();
      } else {
        getAccessToken.signer =
            getOAuthHmacSigner(clientSecret, sharedTokenSecrets.remove(oauthTemporaryToken));
      }

      final OAuthCredentialsResponse credentials;
      try {
        credentials = getAccessToken.execute();
      } catch (IOException e) {
        throw new OAuthAuthenticationException("Authorization denied");
      }

      String userId = getParameterFromState(state, USER_ID_PARAM_KEY);

      credentialsStoreLock.lock();
      try {

        final OAuthCredentialsResponse userId2Credential = credentialsStore.get(userId);
        if (userId2Credential == null) {
          credentialsStore.put(userId, credentials);
        } else {
          userId2Credential.token = credentials.token;
          userId2Credential.tokenSecret = credentials.tokenSecret;
        }

      } finally {
        credentialsStoreLock.unlock();
      }

      return userId;

    } catch (Exception e) {
      throw new OAuthAuthenticationException(e.getMessage());
    }
  }

  /**
   * Get name of OAuth provider supported by current implementation.
   *
   * @return the oauth provider name.
   */
  abstract String getOAuthProvider();

  /**
   * Returns URL to initiate authentication process using given authenticator. Typically points to
   * {@code /api/oauth/} or {@code /api/oauth/1.0} endpoint with necessary request params.
   *
   * @return URL to initiate authentication process
   */
  public abstract String getLocalAuthenticateUrl();

  /**
   * Compute the Authorization header to sign the OAuth 1 request.
   *
   * @param userId the user id.
   * @param requestMethod the HTTP request method.
   * @param requestUrl the HTTP request url with encoded query parameters.
   * @return the authorization header value, or {@code null} if token was not found for given user
   *     id.
   * @throws OAuthAuthenticationException if authentication failed.
   */
  public String computeAuthorizationHeader(
      final String userId, final String requestMethod, final String requestUrl)
      throws OAuthAuthenticationException {
    final OAuthCredentialsResponse credentials = new OAuthCredentialsResponse();
    OAuthToken oauthToken = getToken(userId);
    credentials.token = oauthToken != null ? oauthToken.getToken() : null;
    if (credentials.token != null) {
      return computeAuthorizationHeader(
          requestMethod, requestUrl, credentials.token, credentials.tokenSecret);
    }
    return null;
  }

  private OAuthToken getToken(final String userId) {
    OAuthCredentialsResponse credentials;
    credentialsStoreLock.lock();
    try {
      credentials = credentialsStore.get(userId);
    } finally {
      credentialsStoreLock.unlock();
    }
    if (credentials != null) {
      return newDto(OAuthToken.class)
          .withToken(credentials.token)
          .withScope(credentials.tokenSecret);
    }
    return null;
  }

  /**
   * Compute the Authorization header to sign the OAuth 1 request.
   *
   * @param requestMethod the HTTP request method.
   * @param requestUrl the HTTP request url with encoded query parameters.
   * @param token the token.
   * @param tokenSecret the secret token.
   * @return the authorization header value, or {@code null}.
   */
  private String computeAuthorizationHeader(
      final String requestMethod,
      final String requestUrl,
      final String token,
      final String tokenSecret)
      throws OAuthAuthenticationException {
    OAuthParameters oauthParameters;
    try {
      oauthParameters = new OAuthParameters();
      oauthParameters.consumerKey = clientId;
      oauthParameters.signer =
          clientSecret == null
              ? getOAuthRsaSigner()
              : getOAuthHmacSigner(clientSecret, tokenSecret);
      oauthParameters.token = token;
      oauthParameters.version = "1.0";

      oauthParameters.computeNonce();
      oauthParameters.computeTimestamp();

      oauthParameters.computeSignature(requestMethod, new GenericUrl(requestUrl));
    } catch (GeneralSecurityException e) {
      throw new OAuthAuthenticationException(e);
    }

    return oauthParameters.getAuthorizationHeader();
  }

  private String getParameterFromState(String state, String parameterName) {
    if (isNullOrEmpty(state)) {
      return null;
    }
    for (final String param : extractStateParams(state)) {
      if (param.startsWith(parameterName + "=")) {
        return param.substring(parameterName.length() + 1);
      }
    }
    return null;
  }

  private String[] extractStateParams(String state) {
    try {
      String decodedState = decode(state, "UTF-8");
      return decodedState.split("&");
    } catch (UnsupportedEncodingException ignored) {
      // should never happen, UTF-8 supported.
    }
    return null;
  }

  private OAuthRsaSigner getOAuthRsaSigner()
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    OAuthRsaSigner oAuthRsaSigner = new OAuthRsaSigner();
    oAuthRsaSigner.privateKey = getPrivateKey(privateKey);
    return oAuthRsaSigner;
  }

  private OAuthHmacSigner getOAuthHmacSigner(
      @Nullable String clientSecret, @Nullable String oauthTemporaryToken)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    final OAuthHmacSigner signer = new OAuthHmacSigner();
    signer.clientSharedSecret = clientSecret;
    signer.tokenSharedSecret = sharedTokenSecrets.remove(oauthTemporaryToken);
    return signer;
  }

  private PrivateKey getPrivateKey(String privateKey)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] privateKeyBytes = Base64.decodeBase64(privateKey);
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePrivate(keySpec);
  }

  private static class OAuthPostTemporaryToken extends OAuthGetTemporaryToken {
    OAuthPostTemporaryToken(String authorizationServerUrl) {
      super(authorizationServerUrl);
      super.usePost = true;
    }
  }

  private static class OAuthPostAccessToken extends OAuthGetAccessToken {
    OAuthPostAccessToken(String authorizationServerUrl) {
      super(authorizationServerUrl);
      super.usePost = true;
    }
  }
}
