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
package org.eclipse.che.multiuser.keycloak.server.oauth2;

import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.AUTH_SERVER_URL_SETTING;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.REALM_SETTING;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import java.io.IOException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
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
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.annotations.Required;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.keycloak.server.KeycloakSettings;
import org.keycloak.common.util.KeycloakUriBuilder;

/** @author Max Shaposhnik (mshaposh@redhat.com) */
public class KeycloakOAuthAuthenticationService {
  @Context UriInfo uriInfo;

  @Context SecurityContext security;

  @Inject KeycloakSettings keycloakConfiguration;

  @Inject HttpJsonRequestFactory requestFactory;

  /**
   * Redirect request
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

    //    KeycloakSecurityContext session =
    //        (KeycloakSecurityContext)
    // request.getAttribute(KeycloakSecurityContext.class.getName());
    //    AccessToken token = session.getToken();
    Jwt jwtToken = (Jwt) request.getAttribute("token");
    if (jwtToken == null) {
      throw new BadRequestException("No token provided.");
    }
    Claims token = (Claims) jwtToken.getBody();
    // String clientId = token.getIssuedFor();
    String nonce = UUID.randomUUID().toString();
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
    // String input = nonce + token.getSessionState() + clientId + oauthProvider;
    // byte[] check = md.digest(input.getBytes(StandardCharsets.UTF_8));
    // String hash = Base64Url.encode(check);
    // request.getSession().setAttribute("hash", hash);
    String accountLinkUrl =
        KeycloakUriBuilder.fromUri(keycloakConfiguration.get().get(AUTH_SERVER_URL_SETTING))
            .path("/realms/{realm}/broker/{provider}/link")
            .queryParam("nonce", nonce)
            //      .queryParam("hash", hash)
            //      .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectAfterLogin)
            .build(keycloakConfiguration.get().get(REALM_SETTING), oauthProvider)
            .toString();
    return Response.temporaryRedirect(URI.create(accountLinkUrl)).build();
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
      throws ForbiddenException, BadRequestException, ConflictException, NotFoundException,
          ServerException, UnauthorizedException {

    try {
      String token =
          requestFactory
              .fromUrl(
                  KeycloakUriBuilder.fromUri(
                          keycloakConfiguration.get().get(AUTH_SERVER_URL_SETTING))
                      .path("/realms/{realm}/broker/{provider}/token")
                      .build(keycloakConfiguration.get().get(REALM_SETTING), oauthProvider)
                      .toString())
              .request()
              .asString();
      return DtoFactory.newDto(OAuthToken.class).withToken(token);
    } catch (IOException e) {
      throw new ServerException(e.getMessage());
    }
  }
}
