/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.security.oauth1;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.rest.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.eclipse.che.commons.lang.UrlUtils.getParameter;
import static org.eclipse.che.commons.lang.UrlUtils.getQueryParametersFromState;
import static org.eclipse.che.commons.lang.UrlUtils.getRequestUrl;
import static org.eclipse.che.commons.lang.UrlUtils.getState;

/**
 * RESTful wrapper for OAuth 1.0.
 *
 * @author Kevin Pollet
 * @author Igor Vinokur
 */
@Path("oauth/1.0")
public class OAuthAuthenticationService extends Service {
    private static final Logger LOG = LoggerFactory.getLogger(OAuthAuthenticationService.class);

    private final static String USER_ID_PARAMETER                = "user_id";
    private final static String REQUEST_URL_PARAMETER            = "request_url";
    private final static String PROVIDER_NAME_PARAMETER          = "oauth_provider";
    private final static String REQUEST_METHOD_PARAMETER         = "request_method";
    private final static String SIGNATURE_METHOD_PARAMETER       = "signature_method";
    private final static String REDIRECT_AFTER_LOGIN_PARAMETER   = "redirect_after_login";
    private final static String UNSUPPORTED_OAUTH_PROVIDER_ERROR = "Unsupported OAuth provider: %s";

    @Inject
    protected OAuthAuthenticatorProvider providers;

    @GET
    @Path("authenticate")
    public Response authenticate(@Context UriInfo uriInfo) throws OAuthAuthenticationException, BadRequestException {
        MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();

        final String providerName = parameters.getFirst(PROVIDER_NAME_PARAMETER);
        final String requestMethod = parameters.getFirst(REQUEST_METHOD_PARAMETER);
        final String signatureMethod = parameters.getFirst(SIGNATURE_METHOD_PARAMETER);
        final String redirectAfterLogin = parameters.getFirst(REDIRECT_AFTER_LOGIN_PARAMETER);

        requiredNotNull(providerName, PROVIDER_NAME_PARAMETER);
        requiredNotNull(redirectAfterLogin, REDIRECT_AFTER_LOGIN_PARAMETER);

        final OAuthAuthenticator oauth = getAuthenticator(providerName);
        final String authUrl = oauth.getAuthenticateUrl(getRequestUrl(uriInfo), requestMethod, signatureMethod);

        return Response.temporaryRedirect(URI.create(authUrl)).build();
    }

    @GET
    @Path("callback")
    public Response callback(@Context UriInfo uriInfo) throws OAuthAuthenticationException, BadRequestException {
        final URL requestUrl = getRequestUrl(uriInfo);
        final Map<String, List<String>> parameters = getQueryParametersFromState(getState(requestUrl));

        final String providerName = getParameter(parameters, PROVIDER_NAME_PARAMETER);
        final String redirectAfterLogin = getParameter(parameters, REDIRECT_AFTER_LOGIN_PARAMETER);

        getAuthenticator(providerName).callback(requestUrl);

        return Response.temporaryRedirect(URI.create(redirectAfterLogin)).build();
    }

    @GET
    @Path("signature")
    public String signature(@Context UriInfo uriInfo) throws OAuthAuthenticationException, BadRequestException {
        MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();

        String providerName = parameters.getFirst(PROVIDER_NAME_PARAMETER);
        String userId = parameters.getFirst(USER_ID_PARAMETER);
        String requestUrl = parameters.getFirst(REQUEST_URL_PARAMETER);
        String requestMethod = parameters.getFirst(REQUEST_METHOD_PARAMETER);

        requiredNotNull(providerName, PROVIDER_NAME_PARAMETER);
        requiredNotNull(userId, USER_ID_PARAMETER);
        requiredNotNull(requestUrl, REQUEST_URL_PARAMETER);
        requiredNotNull(requestMethod, REQUEST_METHOD_PARAMETER);

        return getAuthenticator(providerName).computeAuthorizationHeader(userId, requestMethod, requestUrl);
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
     * @param object
     *         object reference to check
     * @param subject
     *         used as subject of exception message "{subject} required"
     * @throws BadRequestException
     *         when object reference is {@code null}
     */
    private void requiredNotNull(Object object, String subject) throws BadRequestException {
        if (object == null) {
            throw new BadRequestException(subject + " required");
        }
    }
}
