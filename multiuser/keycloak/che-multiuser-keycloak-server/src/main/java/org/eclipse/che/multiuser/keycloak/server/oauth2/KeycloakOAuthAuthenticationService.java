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
package org.eclipse.che.multiuser.keycloak.server.oauth2;

import io.jsonwebtoken.Jwt;
import java.io.IOException;
import java.net.URI;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
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
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.keycloak.server.KeycloakServiceClient;
import org.eclipse.che.multiuser.keycloak.shared.dto.KeycloakTokenResponse;

@Path("/oauth")
public class KeycloakOAuthAuthenticationService {
  @Context UriInfo uriInfo;

  @Context SecurityContext security;

  private final KeycloakServiceClient keycloakServiceClient;

  @Inject
  public KeycloakOAuthAuthenticationService(KeycloakServiceClient keycloakServiceClient) {
    this.keycloakServiceClient = keycloakServiceClient;
  }

  /**
   * Performs local and Keycloak accounts linking
   *
   * @return typically Response that redirect user for OAuth provider site
   */
  @GET
  @Path("authenticate")
  public Response authenticate(
      @Required @QueryParam("oauth_provider") String oauthProvider,
      @Required @QueryParam("redirect_after_login") String redirectAfterLogin,
      @Context HttpServletRequest request)
      throws ForbiddenException, BadRequestException {

    Jwt jwtToken = (Jwt) request.getAttribute("token");
    if (jwtToken == null) {
      throw new BadRequestException("No token provided.");
    }
    String accountLinkUrl =
        keycloakServiceClient.getAccountLinkingURL(jwtToken, oauthProvider, redirectAfterLogin);
    return Response.temporaryRedirect(URI.create(accountLinkUrl)).build();
  }

  /**
   * Gets OAuth token for user from Keycloak.
   *
   * @param oauthProvider OAuth provider name
   * @return OAuthToken
   * @throws ServerException
   */
  @GET
  @Path("token")
  @Produces(MediaType.APPLICATION_JSON)
  public OAuthToken token(@Required @QueryParam("oauth_provider") String oauthProvider)
      throws ForbiddenException, BadRequestException, ConflictException, NotFoundException,
          ServerException, UnauthorizedException {
    try {
      KeycloakTokenResponse response =
          keycloakServiceClient.getIdentityProviderToken(oauthProvider);
      return DtoFactory.newDto(OAuthToken.class)
          .withToken(response.getAccessToken())
          .withScope(response.getScope());
    } catch (IOException e) {
      throw new ServerException(e.getMessage());
    }
  }
}
