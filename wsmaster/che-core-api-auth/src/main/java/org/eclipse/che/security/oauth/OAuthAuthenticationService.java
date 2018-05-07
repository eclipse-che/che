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

import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.*;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.security.oauth.shared.dto.OAuthAuthenticatorDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** RESTful wrapper for OAuthAuthenticator. */
@Path("oauth")
public class OAuthAuthenticationService extends Service {
  private static final Logger LOG = LoggerFactory.getLogger(OAuthAuthenticationService.class);

  @Context protected UriInfo uriInfo;
  @Context protected SecurityContext security;

  @Inject private OAuthAPI oAuthAPI;

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
      @QueryParam("oauth_provider") String oauthProvider,
      @QueryParam("redirect_after_login") String redirectAfterLogin,
      @QueryParam("scope") List<String> scopes,
      @Context HttpServletRequest request)
      throws NotFoundException, OAuthAuthenticationException, BadRequestException,
          ForbiddenException {
    return oAuthAPI.authenticate(uriInfo, oauthProvider, scopes, redirectAfterLogin, request);
  }

  @GET
  @Path("callback")
  public Response callback(@QueryParam("errorValues") List<String> errorValues)
      throws OAuthAuthenticationException, NotFoundException, ForbiddenException {
    return oAuthAPI.callback(uriInfo, errorValues);
  }

  /**
   * Gets list of installed OAuth authenticators.
   *
   * @return list of installed OAuth authenticators
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Set<OAuthAuthenticatorDescriptor> getRegisteredAuthenticators() throws ForbiddenException {
    return oAuthAPI.getRegisteredAuthenticators(uriInfo);
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
      throws ServerException, UnauthorizedException, NotFoundException, ForbiddenException,
          BadRequestException, ConflictException {
    return oAuthAPI.getToken(oauthProvider);
  }

  @DELETE
  @Path("token")
  public void invalidate(@Required @QueryParam("oauth_provider") String oauthProvider)
      throws UnauthorizedException, NotFoundException, ServerException, ForbiddenException {
    oAuthAPI.invalidateToken(oauthProvider);
  }
}
