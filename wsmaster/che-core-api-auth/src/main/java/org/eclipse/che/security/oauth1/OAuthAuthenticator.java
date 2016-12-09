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
import com.google.api.client.auth.oauth.OAuthRsaSigner;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.Base64;

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.commons.annotation.Nullable;

import javax.validation.constraints.NotNull;

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

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.net.URLDecoder.decode;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Authentication service which allows get access token from OAuth provider site.
 *
 * @author Kevin Pollet
 * @author Igor Vinokur
 */
public abstract class OAuthAuthenticator {
    private static final String USER_ID_PARAM_KEY          = "userId";
    private static final String REQUEST_TYPE_PARAM_KEY     = "request_type";
    private static final String SIGNATURE_METHOD_PARAM_KEY = "signature_method";
    private static final String STATE_PARAM_KEY            = "state";
    private static final String OAUTH_TOKEN_PARAM_KEY      = "oauth_token";
    private static final String OAUTH_VERIFIER_PARAM_KEY   = "oauth_verifier";

    private final String                                clientId;
    private final String                                clientSecret;
    private final String                                privateKey;
    private final String                                requestTokenUri;
    private final String                                accessTokenUri;
    private final String                                authorizeTokenUri;
    private final String                                redirectUri;
    private final HttpTransport                         httpTransport;
    private final Map<String, OAuthCredentialsResponse> credentialsStore;
    private final ReentrantLock                         credentialsStoreLock;
    private final Map<String, String>                   sharedTokenSecrets;

    protected OAuthAuthenticator(@NotNull final String clientId,
                                 @Nullable final String clientSecret,
                                 @Nullable final String privateKey,
                                 @NotNull final String requestTokenUri,
                                 @NotNull final String accessTokenUri,
                                 @NotNull final String authorizeTokenUri,
                                 @NotNull final String redirectUri) {
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
     * @param requestUrl
     *         URL of current HTTP request. This parameter required to be able determine URL for redirection after
     *         authentication. If URL contains query parameters they will be copied to 'state' parameter and returned to
     *         callback method.
     * @return URL for authentication.
     */
    String getAuthenticateUrl(final URL requestUrl)
            throws OAuthAuthenticationException, InvalidKeySpecException, NoSuchAlgorithmException {

        final GenericUrl callbackUrl = new GenericUrl(redirectUri);
        callbackUrl.put(STATE_PARAM_KEY, requestUrl.getQuery());

        final String state = (String)callbackUrl.getFirst(STATE_PARAM_KEY);
        String requestType = getRequestTypeFromStateParameter(state);
        String signatureMethod = getSignatureMethodFromStateParameter(state);

        OAuthGetTemporaryToken temporaryToken;
        if (requestType != null && "post".equals(requestType.toLowerCase())) {
            temporaryToken = new OAuthPostTemporaryToken(requestTokenUri);
        } else {
            temporaryToken = new OAuthGetTemporaryToken(requestTokenUri);
        }
        if (signatureMethod != null && "rsa".equals(signatureMethod.toLowerCase())) {
            temporaryToken.signer = getOAuthRsaSigner();
        } else {
            temporaryToken.signer = getOAuthHmacSigner(null, null);
        }
        temporaryToken.consumerKey = clientId;
        temporaryToken.callback = callbackUrl.build();
        temporaryToken.transport = httpTransport;

        try {
            final OAuthCredentialsResponse credentialsResponse = temporaryToken.execute();
            final OAuthAuthorizeTemporaryTokenUrl authorizeTemporaryTokenUrl = new OAuthAuthorizeTemporaryTokenUrl(authorizeTokenUri);
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
    String callback(final URL requestUrl) throws OAuthAuthenticationException, InvalidKeySpecException, NoSuchAlgorithmException {
        try {
            final GenericUrl callbackUrl = new GenericUrl(requestUrl.toString());

            if (callbackUrl.getFirst(OAUTH_TOKEN_PARAM_KEY) == null) {
                throw new OAuthAuthenticationException("Missing oauth_token parameter");
            }

            if (callbackUrl.getFirst(OAUTH_VERIFIER_PARAM_KEY) == null) {
                throw new OAuthAuthenticationException("Missing oauth_verifier parameter");
            }

            final String state = (String)callbackUrl.getFirst(STATE_PARAM_KEY);

            String requestType = getRequestTypeFromStateParameter(state);
            String signatureMethod = getSignatureMethodFromStateParameter(state);

            final String oauthTemporaryToken = (String)callbackUrl.getFirst(OAUTH_TOKEN_PARAM_KEY);

            OAuthGetAccessToken getAccessToken;
            if (requestType != null && "post".equals(requestType.toLowerCase())) {
                getAccessToken = new OAuthPostAccessToken(accessTokenUri);
            } else {
                getAccessToken = new OAuthGetAccessToken(accessTokenUri);
            }
            getAccessToken.consumerKey = clientId;
            getAccessToken.temporaryToken = oauthTemporaryToken;
            getAccessToken.verifier = (String)callbackUrl.getFirst(OAUTH_VERIFIER_PARAM_KEY);
            getAccessToken.transport = httpTransport;
            if (signatureMethod != null && "rsa".equals(signatureMethod.toLowerCase())) {
                getAccessToken.signer = getOAuthRsaSigner();
            } else {
                getAccessToken.signer = getOAuthHmacSigner(clientSecret, sharedTokenSecrets.remove(oauthTemporaryToken));
            }

            final OAuthCredentialsResponse credentials = getAccessToken.execute();

            String userId = getUserFromStateParameter(state);

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
     * Get name of OAuth provider supported by current implementation.
     *
     * @return the oauth provider name.
     */
    abstract String getOAuthProvider();

    /**
     * Compute the Authorization header to sign the OAuth 1 request.
     *
     * @param userId
     *         the user id.
     * @param requestMethod
     *         the HTTP request method.
     * @param requestUrl
     *         the HTTP request url with encoded query parameters.
     * @return the authorization header value, or {@code null}.
     * @throws IOException
     *         if something wrong occurs.
     */
    String computeAuthorizationHeader(@NotNull final String userId,
                                      @NotNull final String requestMethod,
                                      @NotNull final String requestUrl)
            throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {

        final OAuthCredentialsResponse credentials = new OAuthCredentialsResponse();
        OAuthToken oauthToken = getToken(userId);
        credentials.token = oauthToken != null ? oauthToken.getToken() : null;
        if (credentials.token != null) {
            return computeAuthorizationHeader(requestMethod, requestUrl, credentials.token, credentials.tokenSecret);
        }
        return null;
    }

    private OAuthToken getToken(final String userId) throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        OAuthCredentialsResponse credentials;
        credentialsStoreLock.lock();
        try {
            credentials = credentialsStore.get(userId);
        } finally {
            credentialsStoreLock.unlock();
        }
        return newDto(OAuthToken.class).withToken(credentials.token).withScope(credentials.tokenSecret);
    }

    /**
     * Compute the Authorization header to sign the OAuth 1 request.
     *
     * @param requestMethod
     *         the HTTP request method.
     * @param requestUrl
     *         the HTTP request url with encoded query parameters.
     * @param token
     *         the token.
     * @param tokenSecret
     *         the secret token.
     * @return the authorization header value, or {@code null}.
     */
    private String computeAuthorizationHeader(@NotNull final String requestMethod,
                                              @NotNull final String requestUrl,
                                              @NotNull final String token,
                                              @NotNull final String tokenSecret) throws InvalidKeySpecException, NoSuchAlgorithmException {

        final OAuthParameters oauthParameters = new OAuthParameters();
        oauthParameters.consumerKey = clientId;
        oauthParameters.signer = clientSecret == null ? getOAuthRsaSigner() : getOAuthHmacSigner(clientSecret, tokenSecret);
        oauthParameters.token = token;
        oauthParameters.version = "1.0";

        oauthParameters.computeNonce();
        oauthParameters.computeTimestamp();

        try {
            oauthParameters.computeSignature(requestMethod, new GenericUrl(requestUrl));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        return oauthParameters.getAuthorizationHeader();
    }

    private String getUserFromStateParameter(final String state) {
        if (!isNullOrEmpty(state)) {
            final String[] params = extractStateParams(state);
            for (final String param : params) {
                if (param.startsWith(USER_ID_PARAM_KEY + "=")) {
                    return param.substring(USER_ID_PARAM_KEY.length() + 1, param.length());
                }
            }
        }
        return null;
    }

    private String getSignatureMethodFromStateParameter(final String state) {
        if (!isNullOrEmpty(state)) {
            final String[] params = extractStateParams(state);
            for (final String param : params) {
                if (param.startsWith(SIGNATURE_METHOD_PARAM_KEY + "=")) {
                    return param.substring(SIGNATURE_METHOD_PARAM_KEY.length() + 1, param.length());
                }
            }
        }
        return null;
    }

    private String getRequestTypeFromStateParameter(final String state) {
        if (!isNullOrEmpty(state)) {
            final String[] params = extractStateParams(state);
            for (final String param : params) {
                if (param.startsWith(REQUEST_TYPE_PARAM_KEY + "=")) {
                    return param.substring(REQUEST_TYPE_PARAM_KEY.length() + 1, param.length());
                }
            }
        }
        return null;
    }

    private String[] extractStateParams(String state) {
        final String decodedState;
        try {
            decodedState = decode(state, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return decodedState.split("&");
    }

    private OAuthRsaSigner getOAuthRsaSigner() throws NoSuchAlgorithmException, InvalidKeySpecException {
        OAuthRsaSigner oAuthRsaSigner = new OAuthRsaSigner();
        oAuthRsaSigner.privateKey = getPrivateKey(privateKey);
        return oAuthRsaSigner;
    }

    private OAuthHmacSigner getOAuthHmacSigner(@Nullable String clientSecret, @Nullable String oauthTemporaryToken)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        final OAuthHmacSigner signer = new OAuthHmacSigner();
        signer.clientSharedSecret = clientSecret;
        signer.tokenSharedSecret = sharedTokenSecrets.remove(oauthTemporaryToken);
        return signer;
    }

    private PrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
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
