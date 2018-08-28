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
package org.eclipse.che.multiuser.keycloak.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import java.lang.reflect.Field;
import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class KeycloakAuthenticationFilterTest {

  @Mock private RequestTokenExtractor tokenExtractor;
  @Mock private JwtParser jwtParser;
  @Mock private ServletOutputStream servletOutputStream;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private FilterChain chain;

  private KeycloakAuthenticationFilter authenticationFilter;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    authenticationFilter = new KeycloakAuthenticationFilter(tokenExtractor);
    Field parser = authenticationFilter.getClass().getSuperclass().getDeclaredField("jwtParser");
    parser.setAccessible(true);
    parser.set(authenticationFilter, jwtParser);
    when(response.getOutputStream()).thenReturn(servletOutputStream);
  }

  @Test
  public void shouldSend401IfNoTokenInRequest() throws Exception {
    when(tokenExtractor.getToken(any(HttpServletRequest.class))).thenReturn(null);

    authenticationFilter.doFilter(request, response, chain);

    verify(response).setStatus(401);
    verify(servletOutputStream).write(eq("Authorization token is missed".getBytes()));
    verifyNoMoreInteractions(chain);
  }

  @Test
  public void shouldSend401IfTokenIsExpired() throws Exception {
    when(tokenExtractor.getToken(any(HttpServletRequest.class))).thenReturn("token");
    when(jwtParser.parse(anyString())).thenThrow(ExpiredJwtException.class);

    authenticationFilter.doFilter(request, response, chain);

    verify(response).setStatus(401);
    verify(servletOutputStream).write(eq("The specified token is expired".getBytes()));
    verifyNoMoreInteractions(chain);
  }

  @Test
  public void shouldSend401IfTokenIsCheckSignatureFailed() throws Exception {
    when(tokenExtractor.getToken(any(HttpServletRequest.class))).thenReturn("token");
    when(jwtParser.parse(anyString())).thenThrow(new JwtException("bad signature"));

    authenticationFilter.doFilter(request, response, chain);

    verify(response).setStatus(401);
    verify(servletOutputStream).write(eq("Token validation failed: bad signature".getBytes()));
    verifyNoMoreInteractions(chain);
  }
}
