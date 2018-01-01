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
package org.eclipse.che.security.oauth;

import static java.util.Collections.emptyList;
import static org.eclipse.che.commons.lang.UrlUtils.getParameter;
import static org.eclipse.che.commons.lang.UrlUtils.getQueryParametersFromState;
import static org.eclipse.che.commons.lang.UrlUtils.getRequestUrl;
import static org.eclipse.che.commons.lang.UrlUtils.getState;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.LinkParameter;
import org.eclipse.che.api.core.util.LinksHelper;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.security.oauth.shared.dto.OAuthAuthenticatorDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** RESTful wrapper for OAuthAuthenticator. */
@Path("oauth")
public class OAuthAuthenticationService {
  private static final Logger LOG = LoggerFactory.getLogger(OAuthAuthenticationService.class);

  @Inject
  @Named("che.auth.access_denied_error_page")
  protected String errorPage;

  @Inject protected OAuthAuthenticatorProvider providers;
  @Context protected UriInfo uriInfo;
  @Context protected SecurityContext security;

  /**
   * Redirect request to OAuth provider site for authentication|authorization. Client request must
   * contains set of required query parameters:
   *
   * <table>
   * <tr><th>Name</th><th>Description</th><th>Mandatory</th><th>Default value</th></tr>
   * <tr><td>oauth_provider</td><td>Name of OAuth provider. At the moment <tt>google</tt> and <tt>github</tt>
   * supported</td><td>yes</td><td>none</td></tr>
   * <tr><td>scope</td><td>Specify exactly what type of access needed. List of scopes dependents to OAuth provider.
   * Requested scopes displayed at user authorization page at OAuth provider site. Check docs about scopes
   * supported by
   * suitable OAuth provider.</td><td>no</td><td>Empty list</td></tr>
   * <tr><td>mode</td><td>Authentication mode. May be <tt>federated_login</tt> or <tt>token</tt>. If <tt>mode</tt>
   * set
   * as <tt>federated_login</tt> that parameters 'username' and 'password' added to redirect URL after successful
   * user
   * authentication. (see next parameter) In this case 'password' is temporary generated password. This password will
   * be validated by FederatedLoginModule.</td><td>no</td><td>token</td></tr>
   * <tr><td>redirect_after_login</td><td>URL for user redirection after successful
   * authentication</td><td>yes</td><td>none</td></tr>
   * </table>
   *
   * @return typically Response that redirect user for OAuth provider site
   */
  @GET
  @Path("authenticate")
  public Response authenticate(
      @Required @QueryParam("oauth_provider") String oauthProvider,
      @QueryParam("scope") List<String> scopes)
      throws ForbiddenException, NotFoundException, OAuthAuthenticationException {
    OAuthAuthenticator oauth = getAuthenticator(oauthProvider);
    final String authUrl =
        oauth.getAuthenticateUrl(getRequestUrl(uriInfo), scopes == null ? emptyList() : scopes);
    return Response.temporaryRedirect(URI.create(authUrl)).build();
  }

  @GET
  @Path("callback")
  public Response callback(@QueryParam("errorValues") List<String> errorValues)
      throws OAuthAuthenticationException, NotFoundException {
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

  /**
   * Gets list of installed OAuth authenticators.
   *
   * @return list of installed OAuth authenticators
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Set<OAuthAuthenticatorDescriptor> getRegisteredAuthenticators() {
    Set<OAuthAuthenticatorDescriptor> result = new HashSet<>();
    final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder().clone().path(getClass());
    for (String name : providers.getRegisteredProviderNames()) {
      final List<Link> links = new LinkedList<>();
      links.add(
          LinksHelper.createLink(
              HttpMethod.GET,
              uriBuilder.clone().path(getClass(), "authenticate").build().toString(),
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

  /**
   * Gets OAuth token for user.
   *
   * @param oauthProvider OAuth provider name
   * @return OAuthToken
   * @throws ServerException
   */
  @GET
  @Path("token")
  @Produces(MediaType.APPLICATION_JSON)
  public OAuthToken token(@Required @QueryParam("oauth_provider") String oauthProvider)
      throws ServerException, UnauthorizedException, NotFoundException, ForbiddenException {
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

  @DELETE
  @Path("token")
  public void invalidate(@Required @QueryParam("oauth_provider") String oauthProvider)
      throws UnauthorizedException, NotFoundException, ServerException, ForbiddenException {

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
