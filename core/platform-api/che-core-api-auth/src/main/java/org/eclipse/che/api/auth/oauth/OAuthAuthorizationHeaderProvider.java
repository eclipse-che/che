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
package org.eclipse.che.api.auth.oauth;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Map;

/**
 * Provides the Authorization header value from the OAuth 1 providers.
 *
 * @author Kevin Pollet
 */
public interface OAuthAuthorizationHeaderProvider {
    /**
     * Returns the Authorization header value used to sign the request with OAuth.
     *
     * @param oauthProviderName
     *         the OAuth 1 provider name.
     * @param userId
     *         the user id.
     * @param requestMethod
     *         the HTTP request method.
     * @param requestUrl
     *         the HTTP request url with encoded query parameters.
     * @param requestParameters
     *         the HTTP request parameters. HTTP request parameters must include raw values of application/x-www-form-urlencoded POST
     *         parameters.
     * @return the Authorization header value or {@code null} if it cannot be computed.
     */
    String getAuthorizationHeader(@NotNull String oauthProviderName,
                                  @NotNull String userId,
                                  @NotNull String requestMethod,
                                  @NotNull String requestUrl,
                                  @NotNull Map<String, String> requestParameters) throws IOException;
}
