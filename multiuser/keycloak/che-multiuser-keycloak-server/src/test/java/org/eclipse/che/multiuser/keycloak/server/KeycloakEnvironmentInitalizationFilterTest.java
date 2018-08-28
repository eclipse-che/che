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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultHeader;
import io.jsonwebtoken.impl.DefaultJwt;
import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.api.permission.server.AuthorizedSubject;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManager;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {MockitoTestNGListener.class})
public class KeycloakEnvironmentInitalizationFilterTest {

  @Mock private SignatureKeyManager keyManager;
  @Mock private KeycloakUserManager userManager;
  @Mock private KeycloakSettings keycloakSettings;
  @Mock private RequestTokenExtractor tokenExtractor;
  @Mock private PermissionChecker permissionChecker;
  @Mock private FilterChain chain;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private ServletOutputStream servletOutputStream;
  @Mock private HttpSession session;
  @Mock private JwtParser jwtParser;

  private KeycloakEnvironmentInitalizationFilter filter;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(request.getScheme()).thenReturn("http");
    when(request.getSession()).thenReturn(session);
    when(response.getOutputStream()).thenReturn(servletOutputStream);
    EnvironmentContext context = spy(EnvironmentContext.getCurrent());
    EnvironmentContext.setCurrent(context);
    filter =
        new KeycloakEnvironmentInitalizationFilter(
            userManager, tokenExtractor, permissionChecker, keycloakSettings);
    Field parser = filter.getClass().getSuperclass().getDeclaredField("jwtParser");
    parser.setAccessible(true);
    parser.set(filter, jwtParser);
    final KeyPair kp = new KeyPair(mock(PublicKey.class), mock(PrivateKey.class));
    when(keyManager.getKeyPair(anyString())).thenReturn(kp);
  }

  @Test
  public void shouldSkipRequestsWithMachineTokens() throws Exception {
    when(tokenExtractor.getToken(any(HttpServletRequest.class))).thenReturn("not_null_token");
    when(jwtParser.parse(anyString())).thenThrow(MachineTokenJwtException.class);
    // when
    filter.doFilter(request, response, chain);

    // then
    verify(chain).doFilter(eq(request), eq(response));
    verifyNoMoreInteractions(userManager);
  }

  @Test
  public void shouldThrowExceptionWhenNoEmailExists() throws Exception {

    Map<String, Object> claimParams = new HashMap<>();
    claimParams.put("preferred_username", "username");
    Claims claims = new DefaultClaims(claimParams).setSubject("id2");
    DefaultJwt<Claims> jwt = new DefaultJwt<>(new DefaultHeader(), claims);
    // given
    when(tokenExtractor.getToken(any(HttpServletRequest.class))).thenReturn("token2");
    when(request.getAttribute("token")).thenReturn(jwt);

    // when
    filter.doFilter(request, response, chain);

    verify(response).setStatus(400);
    verify(servletOutputStream)
        .write(
            eq(
                "Unable to authenticate user because email address is not set in keycloak profile"
                    .getBytes()));
    verifyNoMoreInteractions(chain);
  }

  @Test
  public void shouldRefreshSubjectWhenTokensNotMatch() throws Exception {

    Subject existingSubject = new SubjectImpl("name", "id1", "token", false);
    UserImpl user = new UserImpl("id2", "test2@test.com", "username2");

    ArgumentCaptor<AuthorizedSubject> captor = ArgumentCaptor.forClass(AuthorizedSubject.class);
    DefaultJwt<Claims> claims = createJwt();
    Subject expectedSubject = new SubjectImpl(user.getName(), user.getId(), "token2", false);
    // given
    when(tokenExtractor.getToken(any(HttpServletRequest.class))).thenReturn("token2");
    when(request.getAttribute("token")).thenReturn(claims);
    when(session.getAttribute(eq("che_subject"))).thenReturn(existingSubject);
    when(userManager.getOrCreateUser(anyString(), anyString(), anyString())).thenReturn(user);
    EnvironmentContext context = spy(EnvironmentContext.getCurrent());
    EnvironmentContext.setCurrent(context);

    // when
    filter.doFilter(request, response, chain);

    // then
    verify(session).setAttribute(eq("che_subject"), captor.capture());
    verify(context).setSubject(captor.capture());
    assertEquals(expectedSubject.getToken(), captor.getAllValues().get(0).getToken());
    assertEquals(expectedSubject.getToken(), captor.getAllValues().get(1).getToken());
    assertEquals(expectedSubject.getUserId(), captor.getAllValues().get(0).getUserId());
    assertEquals(expectedSubject.getUserId(), captor.getAllValues().get(1).getUserId());
    assertEquals(expectedSubject.getUserName(), captor.getAllValues().get(0).getUserName());
    assertEquals(expectedSubject.getUserName(), captor.getAllValues().get(1).getUserName());
  }

  private DefaultJwt<Claims> createJwt() {
    Map<String, Object> claimParams = new HashMap<>();
    claimParams.put("email", "test@test.com");
    claimParams.put("preferred_username", "username");
    Claims claims = new DefaultClaims(claimParams).setSubject("id2");
    return new DefaultJwt<>(new DefaultHeader(), claims);
  }
}
