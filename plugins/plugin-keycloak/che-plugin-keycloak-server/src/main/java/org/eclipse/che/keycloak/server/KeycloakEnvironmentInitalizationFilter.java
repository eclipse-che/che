/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.keycloak.server;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.spi.KeycloakAccount;
import org.keycloak.representations.IDToken;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Collections.emptyList;

/**
 * Sets subject attribute into session based on keycloak authentication data.
 *
 * @author Max Shaposhnik (mshaposhnik@redhat.com)
 */
@Singleton
public class KeycloakEnvironmentInitalizationFilter implements Filter {

    @Inject
    private UserManager userManager;

    @Inject
    private AccountManager accountManager;

    @Inject
    private RequestTokenExtractor tokenExtractor;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest)request;
        if (request.getScheme().startsWith("ws") || (tokenExtractor.getToken(httpRequest) != null && tokenExtractor.getToken(httpRequest).startsWith("machine"))) {
            filterChain.doFilter(request, response);
            return;
        }

        final HttpSession session = httpRequest.getSession();
        Subject subject = (Subject)session.getAttribute("che_subject");
        if (subject == null) {
            KeycloakSecurityContext context = (KeycloakSecurityContext)httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
            // In case of bearer token login, there is another object in session
            if (context == null) {
                OidcKeycloakAccount keycloakAccount = (OidcKeycloakAccount)httpRequest.getAttribute(KeycloakAccount.class.getName());
                if (keycloakAccount != null) {
                    context = keycloakAccount.getKeycloakSecurityContext();
                }
            }
            if (context == null) {
                throw new ServletException("Cannot detect or instantiate user.");
            }
            final IDToken token = firstNonNull(context.getIdToken(),context.getToken());
            final String tokenString = context.getTokenString();
            User user = getOrCreateUser(token.getSubject(), token.getEmail(), token.getPreferredUsername());
            getOrCreateAccount(token.getPreferredUsername(), token.getPreferredUsername());

            subject = new SubjectImpl(user.getName(), user.getId(), tokenString, false);
            session.setAttribute("che_subject", subject);
        }

        try {
            EnvironmentContext.getCurrent().setSubject(subject);
            filterChain.doFilter(addUserInRequest(httpRequest, subject), response);
        } finally {
            EnvironmentContext.reset();
        }
    }

    private synchronized User getOrCreateUser(String id, String email, String username) throws ServletException {
        try {
            return userManager.getById(id);
        } catch (NotFoundException e) {
            try {
                final UserImpl cheUser = new UserImpl(id,
                                                      email,
                                                      username,
                                                      "secret",
                                                      emptyList());
                return userManager.create(cheUser, false);
            } catch (ServerException | ConflictException ex) {
                throw new ServletException("Unable to create new user", ex);
            }
        } catch (ServerException e) {
            throw new ServletException("Unable to get user", e);
        }

    }

    private synchronized Account getOrCreateAccount(String id, String namespace) throws ServletException {
        try {
            return accountManager.getById(id);
        } catch (NotFoundException e) {
            try {
                Account account = new AccountImpl(id, namespace, "personal");
                accountManager.create(account);
                return account;
            } catch (ServerException | ConflictException ex) {
                throw new ServletException("Unable to create new account", ex);
            }
        } catch (ServerException e) {
            throw new ServletException("Unable to get account", e);
        }

    }

    private HttpServletRequest addUserInRequest(final HttpServletRequest httpRequest, final Subject subject) {
        return new HttpServletRequestWrapper(httpRequest) {
            @Override
            public String getRemoteUser() {
                return subject.getUserName();
            }

            @Override
            public Principal getUserPrincipal() {
                return subject::getUserName;
            }
        };
    }

    @Override
    public void destroy() {
    }
}
