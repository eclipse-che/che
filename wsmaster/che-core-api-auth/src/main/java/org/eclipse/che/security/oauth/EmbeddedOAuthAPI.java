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

import static java.util.Collections.emptyList;
import static org.eclipse.che.commons.lang.UrlUtils.*;
import static org.eclipse.che.commons.lang.UrlUtils.getParameter;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.security.oauth.shared.dto.OAuthAuthenticatorDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of functional API component for {@link OAuthAuthenticationService}, that uses
 * {@link OAuthAuthenticator}.
 *
 * @author Mykhailo Kuznietsov
 */
public class EmbeddedOAuthAPI implements OAuthAPI {
  private static final Logger LOG = LoggerFactory.getLogger(EmbeddedOAuthAPI.class);

  @Inject
  @Named("che.auth.access_denied_error_page")
  protected String errorPage;

  @Inject protected OAuthAuthenticatorProvider providers;

  @Override
  public Response authenticate(
      UriInfo uriInfo,
      String oauthProvider,
      List<String> scopes,
      String redirectAfterLogin,
      HttpServletRequest request)
      throws NotFoundException, OAuthAuthenticationException {
    OAuthAuthenticator oauth = getAuthenticator(oauthProvider);
    final String authUrl =
        oauth.getAuthenticateUrl(getRequestUrl(uriInfo), scopes == null ? emptyList() : scopes);
    return Response.temporaryRedirect(URI.create(authUrl)).build();
  }

  @Override
  public Response callback(UriInfo uriInfo, List<String> errorValues)
      throws NotFoundException, OAuthAuthenticationException {
    URL requestUrl = getRequestUrl(uriInfo);
    Map<String, List<String>> params = getQueryParametersFromState(getState(requestUrl));
    if (errorValues != null && errorValues.contains("access_denied")) {
      return Response.temporaryRedirect(
              uriInfo.getRequestUriBuilder().replacePath(errorPage).replaceQuery(null).build())
          .build();
    }
    final String providerName = getParameter(params, "oauth_provider");
    OAuthAuthenticator oauth = getAuthenticator(providerName);
    final List<String> scopes = params.get("scope");
    oauth.callback(requestUrl, scopes == null ? Collections.<String>emptyList() : scopes);
    final String redirectAfterLogin = getParameter(params, "redirect_after_login");
    return Response.temporaryRedirect(URI.create(redirectAfterLogin)).build();
  }

  @Override
  public Set<OAuthAuthenticatorDescriptor> getRegisteredAuthenticators(UriInfo uriInfo) {
    Set<OAuthAuthenticatorDescriptor> result = new HashSet<>();
    final UriBuilder uriBuilder =
        uriInfo.getBaseUriBuilder().clone().path(OAuthAuthenticationService.class);
    for (String name : providers.getRegisteredProviderNames()) {
      final List<Link> links = new LinkedList<>();
      links.add(
          LinksHelper.createLink(
              HttpMethod.GET,
              uriBuilder
                  .clone()
                  .path(OAuthAuthenticationService.class, "authenticate")
                  .build()
                  .toString(),
              null,
              null,
              "Authenticate URL",
              newDto(LinkParameter.class)
                  .withName("oauth_provider")
                  .withRequired(true)
                  .withDefaultValue(name),
              newDto(LinkParameter.class)
                  .withName("mode")
                  .withRequired(true)
                  .withDefaultValue("federated_login")));
      result.add(newDto(OAuthAuthenticatorDescriptor.class).withName(name).withLinks(links));
    }
    return result;
  }

  @Override
  public OAuthToken getToken(String oauthProvider)
      throws NotFoundException, UnauthorizedException, ServerException {
    OAuthAuthenticator provider = getAuthenticator(oauthProvider);
    final Subject subject = EnvironmentContext.getCurrent().getSubject();
    try {
      OAuthToken token = provider.getToken(subject.getUserId());
      if (token == null) {
        token = provider.getToken(subject.getUserName());
      }
      if (token != null) {
        return token;
      }
      throw new UnauthorizedException(
          "OAuth token for user " + subject.getUserId() + " was not found");
    } catch (IOException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  public void invalidateToken(String oauthProvider)
      throws NotFoundException, UnauthorizedException, ServerException {
    OAuthAuthenticator oauth = getAuthenticator(oauthProvider);
    final Subject subject = EnvironmentContext.getCurrent().getSubject();
    try {
      if (!oauth.invalidateToken(subject.getUserId())) {
        throw new UnauthorizedException(
            "OAuth token for user " + subject.getUserId() + " was not found");
      }
    } catch (IOException e) {
      throw new ServerException(e.getMessage());
    }
  }

  protected OAuthAuthenticator getAuthenticator(String oauthProviderName) throws NotFoundException {
    OAuthAuthenticator oauth = providers.getAuthenticator(oauthProviderName);
    if (oauth == null) {
      LOG.warn("Unsupported OAuth provider {} ", oauthProviderName);
      throw new NotFoundException("Unsupported OAuth provider " + oauthProviderName);
    }
    return oauth;
  }
}
