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

import org.eclipse.che.api.auth.oauth1.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthCredentials;
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
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;

/**
 * @author Mihail Kuznyetsov
 */
public class RemoteOAuthTokenProvider implements OAuthTokenProvider {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteOAuthTokenProvider.class);

    private final String apiEndpoint;

    private final HttpJsonRequestFactory httpJsonRequestFactory;

    @Inject
    public RemoteOAuthTokenProvider(@Named("api.endpoint") String apiEndpoint, HttpJsonRequestFactory httpJsonRequestFactory) {
        this.apiEndpoint = apiEndpoint;
        this.httpJsonRequestFactory = httpJsonRequestFactory;
    }

    /** {@inheritDoc} */
    @Override
    public OAuthCredentials getToken(String oauthProviderName, String userId) throws IOException {
        if (userId.isEmpty()) {
            return null;
        }
        try {
            UriBuilder ub = UriBuilder.fromUri(apiEndpoint)
                                      .path(org.eclipse.che.security.oauth1.OAuthAuthenticationService.class)
                                      .path(org.eclipse.che.security.oauth1.OAuthAuthenticationService.class, "token")
                                      .queryParam("oauth_provider", oauthProviderName);
            Link getTokenLink = DtoFactory.newDto(Link.class).withHref(ub.build().toString()).withMethod("GET");
            OAuthCredentials token = httpJsonRequestFactory.fromLink(getTokenLink)
                                                     .request()
                                                     .asDto(OAuthCredentials.class);

            if (token == null) {
                LOG.warn("Token not found for user {}", userId);
                return null;
            }
            return token;
        } catch (ServerException | UnauthorizedException | ForbiddenException | NotFoundException | ConflictException | BadRequestException e) {
            LOG.error("Exception on token retrieval, message : {}", e.getLocalizedMessage());
            return null;
        }
    }

}
