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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RESTful wrapper for OAuth 1.0 {@link OAuthAuthenticator}.
 *
 * @author Kevin Pollet
 */
@Path("oauth/1.0")
public class OAuthAuthenticationService {
    private static final Logger LOG = LoggerFactory.getLogger(OAuthAuthenticationService.class);

    @Inject
    @Named("auth.oauth.access_denied_error_page")
    protected String errorPage;

    @Inject
    protected OAuthAuthenticatorProvider providers;

    /**
     * Redirect request to OAuth provider site for authentication|authorization. Client request must contains set of
     * required query parameters:
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
     * @param uriInfo
     *         UriInfo
     * @return typically Response that redirect user for OAuth provider site
     */
    @GET
    @Path("authenticate")
    public Response authenticate(@Context UriInfo uriInfo) throws OAuthAuthenticationException {
        final OAuthAuthenticator oauth = getAuthenticator(uriInfo.getQueryParameters().getFirst("oauth_provider"));
        final URL requestUrl = getRequestUrl(uriInfo);
        final String authUrl = oauth.getAuthenticateUrl(requestUrl);

        return Response.temporaryRedirect(URI.create(authUrl)).build();
    }

    @GET
    @Path("callback")
    public Response callback(@Context UriInfo uriInfo) throws OAuthAuthenticationException {
        final URL requestUrl = getRequestUrl(uriInfo);
        final Map<String, List<String>> params = getRequestParameters(getState(requestUrl));
        final List<String> errorValues = uriInfo.getQueryParameters().get("error");
        if (errorValues != null && errorValues.contains("access_denied")) {
            return Response.temporaryRedirect(
                    uriInfo.getRequestUriBuilder().replacePath(errorPage).replaceQuery(null).build()).build();
        }

        final String providerName = getParameter(params, "oauth_provider");
        final OAuthAuthenticator oauth = getAuthenticator(providerName);

        oauth.callback(requestUrl);

        final String redirectAfterLogin = getParameter(params, "redirect_after_login");
        return Response.temporaryRedirect(URI.create(redirectAfterLogin)).build();
    }

    protected URL getRequestUrl(UriInfo uriInfo) {
        try {
            return uriInfo.getRequestUri().toURL();
        } catch (MalformedURLException e) {
            // should never happen
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * OAuth 2.0 support pass query parameters 'state' to OAuth authorization server. Authorization server sends it
     * back
     * to callback URL. Here restore all parameters specified in initial request to {@link
     * #authenticate(javax.ws.rs.core.UriInfo)} .
     *
     * @param state
     *         query parameter state
     * @return map contains request parameters to method {@link #authenticate(javax.ws.rs.core.UriInfo)}
     */
    protected Map<String, List<String>> getRequestParameters(String state) {
        Map<String, List<String>> params = new HashMap<>();
        if (!(state == null || state.isEmpty())) {
            String decodedState;
            try {
                decodedState = URLDecoder.decode(state, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // should never happen, UTF-8 supported.
                throw new RuntimeException(e.getMessage(), e);
            }

            for (String pair : decodedState.split("&")) {
                if (!pair.isEmpty()) {
                    String name;
                    String value;
                    int eq = pair.indexOf('=');
                    if (eq < 0) {
                        name = pair;
                        value = "";
                    } else {
                        name = pair.substring(0, eq);
                        value = pair.substring(eq + 1);
                    }

                    List<String> l = params.get(name);
                    if (l == null) {
                        l = new ArrayList<>();
                        params.put(name, l);
                    }
                    l.add(value);
                }
            }
        }
        return params;
    }

    protected String getState(URL requestUrl) {
        final String query = requestUrl.getQuery();
        if (!(query == null || query.isEmpty())) {
            int start = query.indexOf("state=");
            if (start < 0) {
                return null;
            }
            int end = query.indexOf('&', start);
            if (end < 0) {
                end = query.length();
            }
            return query.substring(start + 6, end);
        }
        return null;
    }

    protected String getParameter(Map<String, List<String>> params, String name) {
        List<String> l = params.get(name);
        if (!(l == null || l.isEmpty())) {
            return l.get(0);
        }
        return null;
    }

    @GET
    @Path("invalidate")
    public Response invalidate(@Context UriInfo uriInfo, @Context SecurityContext security) {
        final Principal principal = security.getUserPrincipal();
        OAuthAuthenticator oauth = getAuthenticator(uriInfo.getQueryParameters().getFirst("oauth_provider"));
        if (principal != null && oauth.invalidateToken(principal.getName())) {
            return Response.ok().build();
        }
        return Response.status(404).entity("Not found OAuth token for " + (principal != null ? principal.getName() :
                                                                           null)).type(MediaType.TEXT_PLAIN).build();
    }

    protected OAuthAuthenticator getAuthenticator(String oauthProviderName) {
        OAuthAuthenticator oauth = providers.getAuthenticator(oauthProviderName);
        if (oauth == null) {
            LOG.error("Unsupported OAuth provider {} ", oauthProviderName);
            throw new WebApplicationException(Response.status(400).entity("Unsupported OAuth provider " +
                                                                          oauthProviderName).type(MediaType.TEXT_PLAIN)
                                                      .build());
        }
        return oauth;
    }
}
