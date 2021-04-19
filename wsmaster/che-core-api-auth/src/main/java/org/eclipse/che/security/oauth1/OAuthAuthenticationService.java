/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.security.oauth1;

import static java.lang.String.format;
import static org.eclipse.che.commons.lang.UrlUtils.getParameter;
import static org.eclipse.che.commons.lang.UrlUtils.getQueryParametersFromState;
import static org.eclipse.che.commons.lang.UrlUtils.getRequestUrl;
import static org.eclipse.che.commons.lang.UrlUtils.getState;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.rest.Service;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RESTful wrapper for OAuth 1.0.
 *
 * @author Kevin Pollet
 * @author Igor Vinokur
 */
@Path("oauth/1.0")
public class OAuthAuthenticationService extends Service {
  private static final Logger LOG = LoggerFactory.getLogger(OAuthAuthenticationService.class);

  private static final String UNSUPPORTED_OAUTH_PROVIDER_ERROR = "Unsupported OAuth provider: %s";

  @Inject protected OAuthAuthenticatorProvider providers;

  @GET
  @Path("authenticate")
  public Response authenticate(
      @QueryParam("oauth_provider") String providerName,
      @QueryParam("request_method") String requestMethod,
      @QueryParam("signature_method") String signatureMethod,
      @QueryParam("redirect_after_login") String redirectAfterLogin)
      throws OAuthAuthenticationException, BadRequestException {

    requiredNotNull(providerName, "Provider name");
    requiredNotNull(redirectAfterLogin, "Redirect after login");

    final OAuthAuthenticator oauth = getAuthenticator(providerName);
    final String authUrl =
        oauth.getAuthenticateUrl(getRequestUrl(uriInfo), requestMethod, signatureMethod);

    return Response.temporaryRedirect(URI.create(authUrl)).build();
  }

  @GET
  @Path("callback")
  public Response callback() throws OAuthAuthenticationException, BadRequestException {
    final URL requestUrl = getRequestUrl(uriInfo);
    final Map<String, List<String>> parameters = getQueryParametersFromState(getState(requestUrl));

    final String providerName = getParameter(parameters, "oauth_provider");
    final String redirectAfterLogin = getParameter(parameters, "redirect_after_login");

    try {
      getAuthenticator(providerName).callback(requestUrl);
    } catch (OAuthAuthenticationException e) {
      if (e.getMessage().equalsIgnoreCase("Authorization denied"))
      {
        return Response.temporaryRedirect(URI.create("/dashboard")).build();
      }
      throw e;
    }
    return Response.temporaryRedirect(URI.create(redirectAfterLogin)).build();
  }

  @GET
  @Path("signature")
  public String signature(
      @QueryParam("oauth_provider") String providerName,
      @QueryParam("request_url") String requestUrl,
      @QueryParam("request_method") String requestMethod)
      throws OAuthAuthenticationException, BadRequestException {
    requiredNotNull(providerName, "Provider name");
    requiredNotNull(requestUrl, "Request url");
    requiredNotNull(requestMethod, "Request method");

    return getAuthenticator(providerName)
        .computeAuthorizationHeader(
            EnvironmentContext.getCurrent().getSubject().getUserId(), requestMethod, requestUrl);
  }

  private OAuthAuthenticator getAuthenticator(String oauthProviderName) throws BadRequestException {
    OAuthAuthenticator oauth = providers.getAuthenticator(oauthProviderName);
    if (oauth == null) {
      LOG.warn(format(UNSUPPORTED_OAUTH_PROVIDER_ERROR, oauthProviderName));
      throw new BadRequestException(format(UNSUPPORTED_OAUTH_PROVIDER_ERROR, oauthProviderName));
    }
    return oauth;
  }

  /**
   * Checks object reference is not {@code null}
   *
   * @param object object reference to check
   * @param subject used as subject of exception message "{subject} required"
   * @throws BadRequestException when object reference is {@code null}
   */
  private void requiredNotNull(Object object, String subject) throws BadRequestException {
    if (object == null) {
      throw new BadRequestException(subject + " required");
    }
  }
}
