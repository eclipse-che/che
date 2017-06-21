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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.keycloak.KeycloakSecurityContext;

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

import static java.util.Collections.emptyList;

/**
 * @author Max Shaposhnik (mshaposhnik@redhat.com)
 */
@Singleton
public class KeycloakEnvironmentInitalizationFilter implements Filter {

    @Inject
    private UserManager userManager;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        final HttpServletRequest httpRequest = (HttpServletRequest)request;
        final HttpSession session = httpRequest.getSession();
        final KeycloakSecurityContext  context = (KeycloakSecurityContext)session.getAttribute(KeycloakSecurityContext.class.getName());

        User user;

        try {
            user = userManager.getById(context.getIdToken().getSubject());
        } catch (NotFoundException ex) {
            try {
                final UserImpl cheUser = new UserImpl(context.getIdToken().getSubject(),
                                                      context.getIdToken().getEmail(),
                                                      context.getIdToken().getPreferredUsername(),
                                                      "secret",
                                                      emptyList());
                user = userManager.create(cheUser, false);
            } catch (ServerException | ConflictException e) {
                throw new ServletException("Unable to create new user");
            }
        } catch (ServerException e) {
            throw new ServletException("Unable to get user");
        }
        final Subject subject =
                new SubjectImpl(user.getName(), user.getId(), context.getTokenString(), false);
        session.setAttribute("codenvy_user", subject);

        final EnvironmentContext environmentContext = EnvironmentContext.getCurrent();
        try {
            environmentContext.setSubject(subject);
            filterChain.doFilter(addUserInRequest(httpRequest, subject), response);
        } finally {
            EnvironmentContext.reset();
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
