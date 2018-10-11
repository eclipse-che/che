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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtParser;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link AbstractKeycloakFilter}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class AbstractKeycloakFilterTest {

  @Mock private HttpServletRequest request;
  @Mock private JwtParser jwtParser;

  @InjectMocks private TestLoginFilter abstractKeycloakFilter;

  @BeforeMethod
  public void setup() {
    lenient().when(request.getRequestURI()).thenReturn(null);
  }

  @Test
  public void testShouldNotSkipAuthWhenNullTokenProvided() {
    assertFalse(abstractKeycloakFilter.shouldSkipAuthentication(null));
  }

  @Test
  public void testShouldNotSkipAuthWhenProvidedTokenIsNotMachine() {
    Jwt mock = Mockito.mock(Jwt.class);
    doReturn(mock).when(jwtParser).parse(anyString());
    assertFalse(abstractKeycloakFilter.shouldSkipAuthentication("token"));
  }

  @Test
  public void testAuthIsNotNeededWhenMachineTokenProvided() {
    when(jwtParser.parse(anyString())).thenThrow(MachineTokenJwtException.class);
    assertTrue(abstractKeycloakFilter.shouldSkipAuthentication("token"));
  }

  static class TestLoginFilter extends AbstractKeycloakFilter {

    public TestLoginFilter() {}

    @Override
    public void doFilter(
        ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {}
  }
}
