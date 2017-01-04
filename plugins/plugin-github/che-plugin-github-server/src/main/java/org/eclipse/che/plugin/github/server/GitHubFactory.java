/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.github.server;

import com.google.inject.Inject;

import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.kohsuke.github.GitHub;

import java.io.IOException;

/**
 * Factory class used to generate connection to GitHub
 *
 * @author Igor Vinokur
 * @author Sergii Leschenko
 */
public class GitHubFactory {

    private final OAuthTokenProvider oauthTokenProvider;

    @Inject
    private GitHubFactory(OAuthTokenProvider oauthTokenProvider) {
        this.oauthTokenProvider = oauthTokenProvider;
    }

    /**
     * Connect to GitHub API
     *
     * @return connected GitHub API class
     */
    public GitHub connect() throws ServerException, UnauthorizedException {
        try {
            return GitHub.connectUsingOAuth(getToken());
        } catch (IOException e) {
            throw new ServerException(e.getMessage());
        }
    }

    private String getToken() throws ServerException, UnauthorizedException {
        OAuthToken token;
        try {
            token = oauthTokenProvider.getToken("github", EnvironmentContext.getCurrent().getSubject().getUserId());
        } catch (IOException e) {
            throw new ServerException(e.getMessage());
        }

        String oauthToken = token != null ? token.getToken() : null;
        if (oauthToken == null || oauthToken.isEmpty()) {
            throw new UnauthorizedException("User doesn't have access token to github");
        }

        return oauthToken;
    }
}
