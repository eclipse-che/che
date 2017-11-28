/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.machine.authentication.agent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.HttpHeaders;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.commons.auth.token.HeaderRequestTokenExtractor;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.commons.test.mockito.answer.SelfReturningAnswer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Anton Korneta */
@Listeners(MockitoTestNGListener.class)
public class MachineLoginFilterTest {
  private static final String API_ENDPOINT = "http://localhost:8080/api";
  private static final String MACHINE_TOKEN = "machineToken";
  private static final String USERNAME = "fakeName";
  private static final String USER_ID = "12";

  @Mock HttpJsonRequestFactory requestFactoryMock;
  @Mock HttpServletResponse servletResponseMock;
  @Mock FilterChain chainMock;
  @Mock HttpSession sessionMock;
  @Mock UserDto userMock;
  @Mock HttpJsonResponse jsonResponseMock;

  RequestTokenExtractor requestTokenExtractor;
  HttpJsonRequest httpJsonRequestMock;
  Subject machineSubject;

  MachineLoginFilter machineLoginFilter;

  @BeforeMethod
  public void setUp() throws Exception {
    requestTokenExtractor = new HeaderRequestTokenExtractor();
    machineLoginFilter =
        new MachineLoginFilter(API_ENDPOINT, requestFactoryMock, requestTokenExtractor);
    httpJsonRequestMock = mock(HttpJsonRequest.class, new SelfReturningAnswer());
    EnvironmentContext.reset();
    when(requestFactoryMock.fromUrl(anyString())).thenReturn(httpJsonRequestMock);
    when(httpJsonRequestMock.request()).thenReturn(jsonResponseMock);
    when(httpJsonRequestMock.useGetMethod().setAuthorizationHeader(anyString()))
        .thenReturn(httpJsonRequestMock);
    when(userMock.getName()).thenReturn(USERNAME);
    when(userMock.getId()).thenReturn(USER_ID);
    machineSubject = new SubjectImpl(USERNAME, USER_ID, MACHINE_TOKEN, false);
  }

  @Test
  public void shouldProcessingRequestThenCreateHttpSessionAndPutUserIntoHttpSession()
      throws Exception {
    // token service response mocking
    when(jsonResponseMock.asDto(UserDto.class)).thenReturn(userMock);
    // mocking for setting the principal in http session
    doNothing().when(sessionMock).setAttribute(anyString(), any());

    machineLoginFilter.doFilter(
        getRequestMock(null, MACHINE_TOKEN), servletResponseMock, chainMock);

    verify(sessionMock).setAttribute("principal", machineSubject);
  }

  @Test
  public void shouldProcessingRequestWithValidQueryParameterAndPutUserIntoHttpSession()
      throws Exception {
    // token service response mocking
    when(jsonResponseMock.asDto(UserDto.class)).thenReturn(userMock);
    // mocking for setting the principal in http session
    doNothing().when(sessionMock).setAttribute(anyString(), any());
    final HttpServletRequest requestMock = getRequestMock(null, MACHINE_TOKEN);
    when(requestMock.getQueryString()).thenReturn("token=" + MACHINE_TOKEN);

    machineLoginFilter.doFilter(requestMock, servletResponseMock, chainMock);

    verify(sessionMock).setAttribute("principal", machineSubject);
  }

  @Test
  public void shouldProcessingRequestWithAliveSessionAndConfiguredPrincipal() throws Exception {
    // mocking of session principal
    final Subject subject = new SubjectImpl(USERNAME, USER_ID, MACHINE_TOKEN, false);
    when(sessionMock.getAttribute("principal")).thenReturn(subject);
    final HttpServletRequest requestMock = getRequestMock(sessionMock, MACHINE_TOKEN);
    machineLoginFilter.doFilter(requestMock, servletResponseMock, chainMock);

    verify(chainMock).doFilter(requestMock, servletResponseMock);
  }

  @Test
  public void shouldNotProcessingRequestWithEmptyTokenAndShowUnauthorisedServletResponse()
      throws Exception {
    // put empty token into request
    machineLoginFilter.doFilter(getRequestMock(null, null), servletResponseMock, chainMock);

    verify(servletResponseMock)
        .sendError(
            HttpServletResponse.SC_UNAUTHORIZED,
            "Authentication on machine failed, token is missed");
  }

  @Test
  public void shouldNotProcessingRequestWhenApiExceptionOccurs() throws Exception {
    final String apiExMsg = "panic!";
    // token service response mocking
    when(jsonResponseMock.asDto(UserDto.class)).thenReturn(userMock);
    // mocking for setting the principal in http session
    doNothing().when(sessionMock).setAttribute(anyString(), any());
    when(httpJsonRequestMock.request()).thenThrow(new ServerException(apiExMsg));

    machineLoginFilter.doFilter(
        getRequestMock(null, MACHINE_TOKEN), servletResponseMock, chainMock);

    verify(servletResponseMock).sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, apiExMsg);
  }

  @Test
  public void shouldNotProcessingRequestWithInvalidTokenAndShowUnauthorisedServletResponse()
      throws Exception {
    // token service response mocking
    when(jsonResponseMock.asDto(UserDto.class)).thenReturn(userMock);
    // mocking for setting the principal in http session
    doNothing().when(sessionMock).setAttribute(anyString(), any());
    when(httpJsonRequestMock.request())
        .thenThrow(new UnauthorizedException("Token " + MACHINE_TOKEN + " Not found"));

    machineLoginFilter.doFilter(
        getRequestMock(null, MACHINE_TOKEN), servletResponseMock, chainMock);

    verify(servletResponseMock)
        .sendError(
            HttpServletResponse.SC_UNAUTHORIZED,
            "Authentication on machine failed, token " + MACHINE_TOKEN + " is invalid");
  }

  // if the session is null it means that there will be created new one
  private HttpServletRequest getRequestMock(HttpSession session, String token) {
    final HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getSession(false)).thenReturn(session);
    when(request.getSession(true)).thenReturn(sessionMock);
    when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);
    return request;
  }
}
