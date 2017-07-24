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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;

public class KeycloakAuthenticationFilter extends org.keycloak.adapters.servlet.KeycloakOIDCFilter {

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        // TODO: use KeycloakOIDCFilter skip setting
        if (request.getRequestURI().endsWith("/ws") || request.getRequestURI().endsWith("/eventbus")
            || request.getScheme().equals("ws") || req.getScheme().equals("wss") || request.getRequestURI().contains("/websocket/") ||
            (getToken(request) != null && getToken(request).startsWith("machine"))) {
            System.out.println("Skipping " + request.getRequestURI());
            chain.doFilter(req, res);
        } else {
            super.doFilter(req, res, chain);
        }
    }


    private String getToken(HttpServletRequest req) {
        if (req.getHeader(HttpHeaders.AUTHORIZATION) == null) {
            return null;
        }
        return req.getHeader(HttpHeaders.AUTHORIZATION).startsWith("bearer") ? req.getHeader(HttpHeaders.AUTHORIZATION).split(" ")[1]
                                                                             : req.getHeader(HttpHeaders.AUTHORIZATION);
    }
}
