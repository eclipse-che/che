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
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.*;
import org.eclipse.che.security.oauth.shared.dto.OAuthAuthenticatorDescriptor;

/**
 * Abstraction of OAuth authentication service.
 *
 * @author Mykhailo Kuznietsov
 */
public interface OAuthAPI {
  Response authenticate(
      UriInfo uriInfo,
      String oauthProvider,
      List<String> scopes,
      String redirectAfterLogin,
      HttpServletRequest request)
      throws NotFoundException, OAuthAuthenticationException, ForbiddenException,
          BadRequestException;

  Response callback(UriInfo uriInfo, List<String> errorValues)
          throws NotFoundException, OAuthAuthenticationException, ForbiddenException;

  Set<OAuthAuthenticatorDescriptor> getRegisteredAuthenticators(UriInfo uriInfo) throws ForbiddenException;

  OAuthToken getToken(String oauthProvider)
      throws NotFoundException, UnauthorizedException, ServerException, ForbiddenException,
          BadRequestException, ConflictException;

  void invalidateToken(String oauthProvider)
          throws NotFoundException, UnauthorizedException, ServerException, ForbiddenException;
}
