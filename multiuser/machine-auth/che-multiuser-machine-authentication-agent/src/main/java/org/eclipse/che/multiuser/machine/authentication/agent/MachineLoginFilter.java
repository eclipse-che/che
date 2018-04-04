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
package org.eclipse.che.multiuser.machine.authentication.agent;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.io.IOException;
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
import javax.servlet.http.HttpSession;
import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;

/**
 * Protects user's machine from unauthorized access.
 *
 * @author Anton Korneta
 */
@Singleton
public class MachineLoginFilter implements Filter {

  private final String apiEndpoint;
  private final HttpJsonRequestFactory requestFactory;
  private final RequestTokenExtractor tokenExtractor;

  @Inject
  public MachineLoginFilter(
      @Named("che.api") String apiEndpoint,
      HttpJsonRequestFactory requestFactory,
      RequestTokenExtractor tokenExtractor) {
    this.apiEndpoint = apiEndpoint;
    this.requestFactory = requestFactory;
    this.tokenExtractor = tokenExtractor;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    final HttpSession session = httpRequest.getSession(false);
    if (session != null && session.getAttribute("principal") != null) {
      try {
        EnvironmentContext.getCurrent().setSubject((Subject) session.getAttribute("principal"));
        chain.doFilter(request, response);
        return;
      } finally {
        EnvironmentContext.reset();
      }
    }
    final String machineToken = tokenExtractor.getToken(httpRequest);
    if (isNullOrEmpty(machineToken)) {
      ((HttpServletResponse) response)
          .sendError(
              HttpServletResponse.SC_UNAUTHORIZED,
              "Authentication on machine failed, token is missed");
      return;
    }
    try {
      final UserDto userDescriptor =
          requestFactory
              .fromUrl(apiEndpoint + "/user/")
              .useGetMethod()
              .setAuthorizationHeader(machineToken)
              .request()
              .asDto(UserDto.class);
      final Subject machineUser =
          new SubjectImpl(userDescriptor.getName(), userDescriptor.getId(), machineToken, false);
      EnvironmentContext.getCurrent().setSubject(machineUser);
      final HttpSession httpSession = httpRequest.getSession(true);
      httpSession.setAttribute("principal", machineUser);
      chain.doFilter(request, response);
    } catch (UnauthorizedException nfEx) {
      ((HttpServletResponse) response)
          .sendError(
              HttpServletResponse.SC_UNAUTHORIZED,
              "Authentication on machine failed, token " + machineToken + " is invalid");
    } catch (ApiException apiEx) {
      ((HttpServletResponse) response)
          .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, apiEx.getMessage());
    } finally {
      EnvironmentContext.reset();
    }
  }

  @Override
  public void destroy() {}
}
