/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.token.provider.contoller;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.multiuser.keycloak.token.provider.exception.KeycloakException;
import org.eclipse.che.multiuser.keycloak.token.provider.oauth.OpenShiftGitHubOAuthAuthenticator;
import org.eclipse.che.multiuser.keycloak.token.provider.service.KeycloakTokenProvider;
import org.eclipse.che.multiuser.keycloak.token.provider.validator.KeycloakTokenValidator;
import org.eclipse.che.security.oauth.OAuthAuthenticator;
import org.eclipse.che.security.oauth.OAuthAuthenticatorProvider;

@Path("/token")
@Singleton
public class TokenController {
  private static final String GIT_HUB_OAUTH_PROVIDER = "github";

  @Inject private KeycloakTokenProvider tokenProvider;

  @Inject private KeycloakTokenValidator validator;

  @Inject protected OAuthAuthenticatorProvider providers;

  @POST
  @Path("/github")
  @Consumes(MediaType.APPLICATION_JSON)
  public void setGitHubToken(OAuthToken token) throws ServerException {

    if (token == null) {
      throw new ServerException("No token provided");
    }

    OAuthAuthenticator provider = providers.getAuthenticator(GIT_HUB_OAUTH_PROVIDER);

    if (provider == null) {
      throw new ServerException("\"" + GIT_HUB_OAUTH_PROVIDER + "\" oauth provider not registered");
    } else if (!(provider instanceof OpenShiftGitHubOAuthAuthenticator)) {
      throw new ServerException(
          "'setToken' API is not supported by the original 'GitHubOAuthAuthenticator', 'OpenShiftGitHubOAuthAuthenticator' should be configured instead");
    }

    String userId = EnvironmentContext.getCurrent().getSubject().getUserId();

    try {
      ((OpenShiftGitHubOAuthAuthenticator) provider).setToken(userId, token);
    } catch (IOException e) {
      throw new ServerException(e.getMessage());
    }
  }

  @GET
  @Path("/github")
  public Response getGitHubToken(@HeaderParam(HttpHeaders.AUTHORIZATION) String keycloakToken)
      throws ForbiddenException, NotFoundException, ConflictException, BadRequestException,
          ServerException, UnauthorizedException, IOException {
    String token = null;
    try {
      validator.validate(keycloakToken);
      token = tokenProvider.obtainGitHubToken(keycloakToken);
    } catch (KeycloakException e) {
      return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
    return Response.ok(token).build();
  }

  @GET
  @Path("/oso")
  public Response getOpenShiftToken(@HeaderParam(HttpHeaders.AUTHORIZATION) String keycloakToken)
      throws ForbiddenException, NotFoundException, ConflictException, BadRequestException,
          ServerException, UnauthorizedException, IOException {
    String token = null;
    try {
      validator.validate(keycloakToken);
      token = tokenProvider.obtainOsoToken(keycloakToken);
    } catch (KeycloakException e) {
      return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    }
    return Response.ok(token).build();
  }
}
