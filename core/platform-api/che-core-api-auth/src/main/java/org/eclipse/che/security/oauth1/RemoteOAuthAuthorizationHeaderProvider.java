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

import org.eclipse.che.api.auth.oauth1.OAuthAuthorizationHeaderProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Map;

/**
 * Allow get token from OAuth 1.0 service over http.
 *
 * @author Mihail Kuznyetsov
 */
public class RemoteOAuthAuthorizationHeaderProvider implements OAuthAuthorizationHeaderProvider {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteOAuthTokenProvider.class);

    private final String apiEndpoint;

    private final HttpJsonRequestFactory httpJsonRequestFactory;

    @Inject
    public RemoteOAuthAuthorizationHeaderProvider(@Named("api.endpoint") String apiEndpoint,
                                                  HttpJsonRequestFactory httpJsonRequestFactory) {
        this.apiEndpoint = apiEndpoint;
        this.httpJsonRequestFactory = httpJsonRequestFactory;
    }

    /** {@inheritDoc} */
    @Override
    public String getAuthorizationHeader(@NotNull String oauthProviderName,
                                         @NotNull String userId,
                                         @NotNull String requestUrl,
                                         @NotNull String requestMethod,
                                         @NotNull Map<String, String> requestParameters) throws IOException {
        if (userId.isEmpty()) {
            return null;
        }
        try {
            UriBuilder ub = UriBuilder.fromUri(apiEndpoint)
                                      .path(org.eclipse.che.security.oauth1.OAuthAuthenticationService.class)
                                      .path(org.eclipse.che.security.oauth1.OAuthAuthenticationService.class, "token")
                                      .queryParam("oauth_provider", oauthProviderName)
                                      .queryParam("request_url", userId )
                                      .queryParam("request_method", requestMethod );
            for (Map.Entry<String, String> entry : requestParameters.entrySet()) {
                ub.queryParam(entry.getKey(), entry.getValue());
            }

            Link getTokenLink = DtoFactory.newDto(Link.class).withHref(ub.build().toString()).withMethod("GET");
            OAuthToken token = httpJsonRequestFactory.fromLink(getTokenLink)
                                                     .request()
                                                     .asDto(OAuthToken.class);

            if (token == null) {
                LOG.warn("Token not found for user {}", userId);
                return null;
            }
            return token.getToken();
        } catch (ServerException | UnauthorizedException | ForbiddenException | NotFoundException | ConflictException | BadRequestException e) {
            LOG.error("Exception on token retrieval, message : {}", e.getLocalizedMessage());
            return null;
        }
    }


}
