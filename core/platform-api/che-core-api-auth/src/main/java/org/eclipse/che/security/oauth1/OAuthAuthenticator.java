/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.security.oauth1;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import org.eclipse.che.api.auth.shared.dto.OAuthCredentials;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.security.oauth1.shared.User;

import javax.validation.constraints.NotNull;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static java.net.URLDecoder.decode;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Authentication service which allows get access token from OAuth provider site.
 *
 * @author Kevin Pollet
 */
public abstract class OAuthAuthenticator {
    private static final String USER_ID_PARAM_KEY        = "userId";
    private static final String STATE_PARAM_KEY          = "state";
    private static final String OAUTH_TOKEN_PARAM_KEY    = "oauth_token";
    private static final String OAUTH_VERIFIER_PARAM_KEY = "oauth_verifier";

    private static final String GET              = HttpMethod.GET;
    private static final String AUTHORIZATION    = "Authorization";
    private static final String ACCEPT           = HttpHeaders.ACCEPT;
    private static final String APPLICATION_JSON = MediaType.APPLICATION_JSON;

    private final String                                clientId;
    private final String                                clientSecret;
    private final String                                authUri;
    private final String                                requestTokenUri;
    private final String                                requestAccessTokenUri;
    private final String                                verifyAccessTokenUri;
    private final String                                redirectUri;
    private final HttpTransport                         httpTransport;
    private final Map<String, OAuthCredentialsResponse> credentialsStore;
    private final ReentrantLock                         credentialsStoreLock;
    private final Map<String, String>                   sharedTokenSecrets;

    protected OAuthAuthenticator(@NotNull final String clientId,
                                 @NotNull final String clientSecret,
                                 @NotNull final String authUri,
                                 @NotNull final String requestTokenUri,
                                 @NotNull final String requestAccessTokenUri,
                                 @NotNull final String verifyAccessTokenUri,
                                 @NotNull final String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authUri = authUri;
        this.requestAccessTokenUri = requestAccessTokenUri;
        this.verifyAccessTokenUri = verifyAccessTokenUri;
        this.redirectUri = redirectUri;
        this.requestTokenUri = requestTokenUri;
        this.httpTransport = new NetHttpTransport();
        this.credentialsStore = new HashMap<>();
        this.credentialsStoreLock = new ReentrantLock();
        this.sharedTokenSecrets = new HashMap<>();
    }

    /**
     * Create authentication URL.
     *
     * @param requestUrl
     *         URL of current HTTP request. This parameter required to be able determine URL for redirection after
     *         authentication. If URL contains query parameters they will be copy to 'state' parameter and returned to
     *         callback method.
     * @return URL for authentication.
     */
    public String getAuthenticateUrl(final URL requestUrl) throws OAuthAuthenticationException {
        final OAuthHmacSigner signer = new OAuthHmacSigner();
        signer.clientSharedSecret = clientSecret;

        // construct the callback url
        final GenericUrl callbackUrl = new GenericUrl(redirectUri);
        callbackUrl.put(STATE_PARAM_KEY, requestUrl.getQuery());

        final OAuthGetTemporaryToken getTemporaryToken = new OAuthGetTemporaryToken(requestTokenUri);
        getTemporaryToken.signer = signer;
        getTemporaryToken.consumerKey = clientId;
        getTemporaryToken.callback = callbackUrl.build();
        getTemporaryToken.transport = httpTransport;

        try {

            final OAuthCredentialsResponse credentialsResponse = getTemporaryToken.execute();

            final OAuthAuthorizeTemporaryTokenUrl authorizeTemporaryTokenUrl = new OAuthAuthorizeTemporaryTokenUrl(authUri);
            authorizeTemporaryTokenUrl.temporaryToken = credentialsResponse.token;

            sharedTokenSecrets.put(credentialsResponse.token, credentialsResponse.tokenSecret);

            return authorizeTemporaryTokenUrl.build();

        } catch (final IOException e) {
            throw new OAuthAuthenticationException(e);
        }
    }

    /**
     * Process callback request.
     *
     * @param requestUrl
     *         request URI. URI should contain OAuth token and OAuth verifier.
     * @return id of authenticated user
     * @throws OAuthAuthenticationException
     *         if authentication failed or {@code requestUrl} does not contain required parameters.
     */
    public String callback(final URL requestUrl) throws OAuthAuthenticationException {
        try {
            final GenericUrl callbackUrl = new GenericUrl(requestUrl.toString());

            if (callbackUrl.getFirst(OAUTH_TOKEN_PARAM_KEY) == null) {
                throw new OAuthAuthenticationException("Missing oauth_token parameter");
            }

            if (callbackUrl.getFirst(OAUTH_VERIFIER_PARAM_KEY) == null) {
                throw new OAuthAuthenticationException("Missing oauth_verifier parameter");
            }

            final String oauthTemporaryToken = (String)callbackUrl.getFirst(OAUTH_TOKEN_PARAM_KEY);

            final OAuthHmacSigner signer = new OAuthHmacSigner();
            signer.clientSharedSecret = clientSecret;
            signer.tokenSharedSecret = sharedTokenSecrets.remove(oauthTemporaryToken);

            final OAuthGetAccessToken getAccessToken = new OAuthGetAccessToken(requestAccessTokenUri);
            getAccessToken.consumerKey = clientId;
            getAccessToken.temporaryToken = oauthTemporaryToken;
            getAccessToken.verifier = (String)callbackUrl.getFirst(OAUTH_VERIFIER_PARAM_KEY);
            getAccessToken.transport = httpTransport;
            getAccessToken.signer = signer;

            final OAuthCredentialsResponse credentials = getAccessToken.execute();
            final String state = (String)callbackUrl.getFirst(STATE_PARAM_KEY);

            String userId = getUserFromStateParameter(state);
            if (userId == null) {
                userId = getUser(credentials.token, credentials.tokenSecret).getId();
            }

            credentialsStoreLock.lock();
            try {

                final OAuthCredentialsResponse currentCredentials = credentialsStore.get(userId);
                if (currentCredentials == null) {
                    credentialsStore.put(userId, credentials);

                } else {
                    currentCredentials.token = credentials.token;
                    currentCredentials.tokenSecret = credentials.tokenSecret;
                }

            } finally {
                credentialsStoreLock.unlock();
            }

            return userId;

        } catch (final IOException e) {
            throw new OAuthAuthenticationException(e);
        }
    }

    /**
     * Get user info.
     *
     * @param token
     *         the token.
     * @param tokenSecret
     *         the token secret.
     * @return the {@link org.eclipse.che.security.oauth1.shared.User} info.
     * @throws OAuthAuthenticationException
     *         if fail to get {@link org.eclipse.che.security.oauth1.shared.User} info.
     */
    public abstract User getUser(final String token, final String tokenSecret) throws OAuthAuthenticationException;

    /**
     * Get name of OAuth provider supported by current implementation.
     *
     * @return the oauth provider name.
     */
    public abstract String getOAuthProvider();

    /**
     * Invalidate OAuth token for specified user.
     *
     * @param userId
     *         the user id.
     * @return {@code true} if OAuth token is invalidated and {@code false} otherwise.
     */
    public boolean invalidateToken(final String userId) {
        credentialsStoreLock.lock();
        try {

            return credentialsStore.remove(userId) != null;

        } finally {
            credentialsStoreLock.unlock();
        }
    }

    /**
     * Compute the Authorization header to sign the OAuth 1 request.
     *
     * @param userId
     *         the user id.
     * @param requestMethod
     *         the HTTP request method.
     * @param requestUrl
     *         the HTTP request url with encoded query parameters.
     * @param requestParameters
     *         the HTTP request parameters. HTTP request parameters must include raw values of application/x-www-form-urlencoded POST
     *         parameters.
     * @return the authorization header value, or {@code null}.
     * @throws IOException
     *         if something wrong occurs.
     */
    public String computeAuthorizationHeader(@NotNull final String userId,
                                             @NotNull final String requestMethod,
                                             @NotNull final String requestUrl,
                                             @NotNull final Map<String, String> requestParameters) throws IOException {

        final OAuthCredentials credentials = getToken(userId);
        if (credentials != null && credentials.getToken() != null && credentials.getTokenSecret() != null) {
            return computeAuthorizationHeader(requestMethod, requestUrl, requestParameters, credentials.getToken(), credentials.getTokenSecret());
        }
        return null;
    }

    /**
     * Return the authorization token fo the given user id.
     * <p/>
     * WARN!!!. DO not use it directly.
     *
     * @param userId
     *         the user id.
     * @return the user token.
     * @throws java.io.IOException
     *         if an IO exception occurs.
     * @see org.eclipse.che.api.auth.oauth.OAuthTokenProvider#getToken(String, String)
     */
    public OAuthCredentials getToken(final String userId) throws IOException {
        OAuthCredentialsResponse credentials = null;
        credentialsStoreLock.lock();
        try {

            credentials = credentialsStore.get(userId);

        } finally {
            credentialsStoreLock.unlock();
        }

        if (credentials == null) {
            return null;
        } else {
            // Need to check if token which stored is valid for requests, then if valid - we returns it to caller
            HttpURLConnection connection = null;
            try {

                connection = (HttpURLConnection)new URL(verifyAccessTokenUri).openConnection();
                connection.setInstanceFollowRedirects(false);

                final String token = credentials.token;
                final String tokenSecret = credentials.tokenSecret;
                final Map<String, String> requestParameters = emptyMap();

                connection.setRequestProperty(AUTHORIZATION,
                                              computeAuthorizationHeader(GET, verifyAccessTokenUri, requestParameters, token, tokenSecret));

                if (connection.getResponseCode() == 401) {
                    return null;
                }

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
        return newDto(OAuthCredentials.class)
                .withToken(credentials.token)
                .withTokenSecret(credentials.tokenSecret)
                .withCallbackConfirmed(credentials.callbackConfirmed);
    }

    /**
     * Compute the Authorization header to sign the OAuth 1 request.
     *
     * @param requestMethod
     *         the HTTP request method.
     * @param requestUrl
     *         the HTTP request url with encoded query parameters.
     * @param requestParameters
     *         the HTTP request parameters. HTTP request parameters must include raw values of application/x-www-form-urlencoded POST
     *         parameters.
     * @param token
     *         the token.
     * @param tokenSecret
     *         the secret token.
     * @return the authorization header value, or {@code null}.
     */
    private String computeAuthorizationHeader(@NotNull final String requestMethod,
                                              @NotNull final String requestUrl,
                                              @NotNull final Map<String, String> requestParameters,
                                              @NotNull final String token,
                                              @NotNull final String tokenSecret) {

        final OAuthHmacSigner signer = new OAuthHmacSigner();
        signer.clientSharedSecret = clientSecret;
        signer.tokenSharedSecret = tokenSecret;

        final OAuthParameters oauthParameters = new OAuthParameters();
        oauthParameters.consumerKey = clientId;
        oauthParameters.signer = signer;
        oauthParameters.token = token;
        oauthParameters.version = "1.0";

        oauthParameters.computeNonce();
        oauthParameters.computeTimestamp();

        final GenericUrl genericRequestUrl = new GenericUrl(requestUrl);
        genericRequestUrl.putAll(requestParameters);

        try {

            oauthParameters.computeSignature(requestMethod, genericRequestUrl);

        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        return oauthParameters.getAuthorizationHeader();
    }

    /**
     * Get the JSON at the given HTTP request url.
     *
     * @param requestUrl
     *         the HTTP request url with encoded query parameters.
     * @param token
     *         the token.
     * @param tokenSecret
     *         the token secret.
     * @param userClass
     *         the {@link java.lang.Class} to be deserialized from the JSON response.
     * @return an instance of {@code userClass}.
     * @throws OAuthAuthenticationException
     *         if something is wrong with authentication.
     */
    protected <O> O getJson(@NotNull final String requestUrl,
                            @NotNull final String token,
                            @NotNull final String tokenSecret,
                            @NotNull final Class<O> userClass) throws OAuthAuthenticationException {

        HttpURLConnection connection = null;

        try {

            connection = (HttpURLConnection)new URL(requestUrl).openConnection();

            final Map<String, String> requestParameters = emptyMap();
            final String authorizationHeaderValue = computeAuthorizationHeader(GET, requestUrl, requestParameters, token, tokenSecret);

            connection.setRequestProperty(AUTHORIZATION, authorizationHeaderValue);
            connection.setRequestProperty(ACCEPT, APPLICATION_JSON);

            try (final InputStream urlInputStream = connection.getInputStream()) {
                return JsonHelper.fromJson(urlInputStream, userClass, null);
            }

        } catch (final JsonParseException | IOException e) {
            throw new OAuthAuthenticationException(e);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Extract the user id from the state parameter.
     *
     * @param state
     *         the state parameter value.
     * @return the user id or {@code null} if not found.
     */
    private String getUserFromStateParameter(final String state) {
        if (state != null && !state.trim().isEmpty()) {
            final String decodedState;
            try {

                decodedState = decode(state, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            final String[] params = decodedState.split("&");
            for (final String oneParam : params) {
                if (oneParam.startsWith(USER_ID_PARAM_KEY + "=")) {
                    return oneParam.substring(7, oneParam.length());
                }
            }
        }
        return null;
    }
}
