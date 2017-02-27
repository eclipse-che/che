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
package org.eclipse.che.factory.filter;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.factory.server.FactoryService;
import org.eclipse.che.api.factory.shared.dto.FactoryDto;
import org.eclipse.che.api.user.server.UserService;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.UriBuilder.fromUri;

/**
 * Retrieves factory from storage via api and stores in request attribute.
 *
 * @author Max Shaposhnik
 *
 */
@Singleton
public class FactoryRetrieverFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(FactoryRetrieverFilter.class);

    @Inject
    @Named("che.api")
    protected String apiEndPoint;

    @Inject
    HttpJsonRequestFactory httpRequestFactory;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException,
                                                                                                   ServletException {
        final HttpServletRequest httpReq = (HttpServletRequest)req;
        final FactoryDto requestedFactory;
        try {
            if (httpReq.getParameter("id") != null) {
                String getFactoryUrl = fromUri(apiEndPoint).path(FactoryService.class)
                                                           .path(FactoryService.class, "getFactory")
                                                           .build(httpReq.getParameter("id")).toString();

                requestedFactory = httpRequestFactory.fromUrl(getFactoryUrl)
                                                     .setMethod("GET")
                                                     .request()
                                                     .asDto(FactoryDto.class);
            } else if (httpReq.getParameter("user") != null && httpReq.getParameter("name") != null) {

                final String getFactoryUrl = fromUri(apiEndPoint).path(FactoryService.class)
                                                                 .path(FactoryService.class, "getFactoryByAttribute")
                                                                 .build().toString();
                final String getUserByNameUrl = fromUri(apiEndPoint).path(UserService.class)
                                                                    .path(UserService.class, "find")
                                                                    .queryParam("name", httpReq.getParameter("user"))
                                                                    .build()
                                                                    .toString();
                final UserDto user = httpRequestFactory.fromUrl(getUserByNameUrl)
                                                       .setMethod("GET")
                                                       .request()
                                                       .asDto(UserDto.class);
                final List<FactoryDto> matchedFactories = httpRequestFactory.fromUrl(getFactoryUrl)
                                                                            .setMethod("GET")
                                                                            .addQueryParam("name", httpReq.getParameter("name"))
                                                                            .addQueryParam("creator.userId", user.getId())
                                                                            .request()
                                                                            .asList(FactoryDto.class);
                if (matchedFactories.isEmpty()) {
                    resp.getOutputStream().write("We can not find factory with given name and user name.".getBytes());
                    ((HttpServletResponse)resp).setStatus(404);
                    filterChain.doFilter(req, resp);
                }
                requestedFactory = matchedFactories.get(0);
            } else {
                // asked for a parameters factory

                // first populate map of parameters
                Map<String, String> map = new HashMap<>();
                Enumeration<String> parameterNames = httpReq.getParameterNames();
                while (parameterNames.hasMoreElements()) {
                    String parameterName = parameterNames.nextElement();
                    map.put(parameterName, httpReq.getParameter(parameterName));
                }

                // Create URL
                final String resolveFactoryUrl = fromUri(apiEndPoint).path(FactoryService.class)
                                                                     .path(FactoryService.class, "resolveFactory")
                                                                     .build().toString();
                // perform call
                requestedFactory = httpRequestFactory.fromUrl(resolveFactoryUrl)
                                                     .setMethod(POST)
                                                     .setBody(map)
                                                     .request()
                                                     .asDto(FactoryDto.class);
            }
            // DO NOT REMOVE! This log will be used in statistic analyzing
            LOG.info("EVENT#factory-accepted# WS#{}# REFERRER#{}# FACTORY#{}# # AFFILIATE-ID#{}# WS-LOCATION#{}# WS-TYPE#{}#",
                     requestedFactory.getWorkspace().getName(),
                     nullToEmpty(httpReq.getHeader("Referer")),
                     URLDecoder.decode(fromUri(httpReq.getRequestURL().toString()).replaceQuery(httpReq.getQueryString()).build().toString(), "UTF-8"),
                     "", //AFFILIATE-ID
                     "", //TODO: use from policy
                     "" //TODO: use from policy
                    );

            String redirectUrl = UriBuilder.fromPath("/dashboard/").fragment("load-factory/").build().toString();

            // factory has been resolved. If resolved factory has id, only send the ID. Else send the whole parameters
            if (requestedFactory.getId() != null) {
                redirectUrl += "?id=" + requestedFactory.getId();
            } else {
                redirectUrl += "?" + httpReq.getQueryString();
            }

            ((HttpServletResponse)resp).sendRedirect(redirectUrl);

        } catch (ApiException | IOException e) {
            throw new ServletException(e.getMessage(),e);
        }
    }
}
