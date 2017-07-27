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

import org.eclipse.che.commons.auth.token.RequestTokenExtractor;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class KeycloakAuthenticationFilter extends org.keycloak.adapters.servlet.KeycloakOIDCFilter {

    @Inject
    private RequestTokenExtractor tokenExtractor;

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        if (request.getScheme().startsWith("ws") || tokenExtractor.getToken(request) == null || tokenExtractor.getToken(request).startsWith("machine")) {
            chain.doFilter(req, res);
            return;
        } else {
            super.doFilter(req, res, chain);
        }
    }
}
