/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.keycloak.server.oauth2;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.keycloak.server.KeycloakServiceClient;
import org.eclipse.che.multiuser.keycloak.shared.dto.KeycloakTokenResponse;
import org.eclipse.che.security.oauth.OAuthAPI;
import org.eclipse.che.security.oauth.OAuthAuthenticationService;
import org.eclipse.che.security.oauth.shared.dto.OAuthAuthenticatorDescriptor;

/**
 * Implementation of functional API component for {@link OAuthAuthenticationService}, that uses
 * {@link KeycloakServiceClient} for authenticating users through Keycloak Identity providers.
 *
 * @author Mykhailo Kuznietsov
 */
public class DelegatedOAuthAPI implements OAuthAPI {

  private final KeycloakServiceClient keycloakServiceClient;

  @Inject
  public DelegatedOAuthAPI(KeycloakServiceClient keycloakServiceClient) {
    this.keycloakServiceClient = keycloakServiceClient;
  }

  @Override
  public Response authenticate(
      UriInfo uriInfo,
      String oauthProvider,
      List<String> scopes,
      String redirectAfterLogin,
      HttpServletRequest request)
      throws BadRequestException {

    String jwtToken = EnvironmentContext.getCurrent().getSubject().getToken();
    if (jwtToken == null) {
      throw new BadRequestException("No token provided.");
    }
    String accountLinkUrl =
        keycloakServiceClient.getAccountLinkingURL(jwtToken, oauthProvider, redirectAfterLogin);
    return Response.temporaryRedirect(URI.create(accountLinkUrl)).build();
  }

  @Override
  public OAuthToken getToken(String oauthProvider)
      throws ForbiddenException, BadRequestException, NotFoundException, ServerException,
          UnauthorizedException {
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

  @Override
  public void invalidateToken(String oauthProvider) throws ForbiddenException {
    throw new ForbiddenException("Method is not supported in this implementation of OAuth API");
  }

  @Override
  public Response callback(UriInfo uriInfo, List<String> errorValues) throws ForbiddenException {
    throw new ForbiddenException("Method is not supported in this implementation of OAuth API");
  }

  @Override
  public Set<OAuthAuthenticatorDescriptor> getRegisteredAuthenticators(UriInfo uriInfo)
      throws ForbiddenException {
    throw new ForbiddenException("Method is not supported in this implementation of OAuth API");
  }
}
