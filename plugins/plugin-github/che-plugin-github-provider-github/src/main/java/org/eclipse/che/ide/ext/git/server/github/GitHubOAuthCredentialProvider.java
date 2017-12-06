/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.server.github;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.git.GitBasicAuthenticationCredentialsProvider;
import org.eclipse.che.api.git.shared.ProviderInfo;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Sergii Kabashniuk */
@Singleton
public class GitHubOAuthCredentialProvider extends GitBasicAuthenticationCredentialsProvider {

  private static final Logger LOG = LoggerFactory.getLogger(GitHubOAuthCredentialProvider.class);

  private static String OAUTH_PROVIDER_NAME = "github";
  private final String authorizationServicePath;

  @Inject
  public GitHubOAuthCredentialProvider() {
    this.authorizationServicePath = "/oauth/authenticate";
  }

  @Override
  public String getId() {
    return OAUTH_PROVIDER_NAME;
  }

  @Override
  public boolean canProvideCredentials(String url) {
    return url.contains("github.com");
  }

  @Override
  public ProviderInfo getProviderInfo() {
    return new ProviderInfo(
        OAUTH_PROVIDER_NAME,
        UriBuilder.fromPath(authorizationServicePath)
            .queryParam("oauth_provider", OAUTH_PROVIDER_NAME)
            .queryParam("userId", EnvironmentContext.getCurrent().getSubject().getUserId())
            .queryParam("scope", "repo,write:public_key")
            .build()
            .toString());
  }
}
