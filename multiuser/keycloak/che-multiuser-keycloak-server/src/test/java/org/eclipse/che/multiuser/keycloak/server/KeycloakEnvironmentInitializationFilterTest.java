/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import static org.eclipse.che.multiuser.api.authentication.commons.Constants.CHE_SUBJECT_ATTRIBUTE;
import static org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants.USERNAME_CLAIM_SETTING;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultJws;
import io.jsonwebtoken.impl.DefaultJwsHeader;
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
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.api.authentication.commons.SessionStore;
import org.eclipse.che.multiuser.api.authentication.commons.token.RequestTokenExtractor;
import org.eclipse.che.multiuser.api.permission.server.AuthorizedSubject;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.multiuser.keycloak.shared.KeycloakConstants;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManager;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(value = {MockitoTestNGListener.class})
public class KeycloakEnvironmentInitializationFilterTest {

  @Mock private SignatureKeyManager keyManager;
  @Mock private KeycloakUserManager userManager;
  @Mock private KeycloakProfileRetriever keycloakProfileRetriever;
  @Mock private KeycloakSettings keycloakSettings;
  @Mock private RequestTokenExtractor tokenExtractor;
  @Mock private PermissionChecker permissionChecker;
  @Mock private FilterChain chain;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private ServletOutputStream servletOutputStream;
  @Mock private HttpSession session;
  @Mock private JwtParser jwtParser;
  @Mock private SessionStore sessionStore;

  private KeycloakEnvironmentInitializationFilter filter;
  private Map<String, String> keycloakAttributes = new HashMap<>();
  private Map<String, String> keycloakSettingsMap = new HashMap<>();

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    lenient().when(request.getScheme()).thenReturn("http");
    when(request.getSession(anyBoolean())).thenReturn(session);
    lenient().when(response.getOutputStream()).thenReturn(servletOutputStream);
    EnvironmentContext context = spy(EnvironmentContext.getCurrent());
    EnvironmentContext.setCurrent(context);
    filter =
        new KeycloakEnvironmentInitializationFilter(
            sessionStore,
            jwtParser,
            userManager,
            keycloakProfileRetriever,
            tokenExtractor,
            permissionChecker,
            keycloakSettings,
            "\\\\=-");
    final KeyPair kp = new KeyPair(mock(PublicKey.class), mock(PrivateKey.class));
    lenient().when(keyManager.getOrCreateKeyPair(anyString())).thenReturn(kp);
    keycloakAttributes.clear();
    keycloakSettingsMap.clear();
    lenient()
        .when(keycloakProfileRetriever.retrieveKeycloakAttributes(anyString()))
        .thenReturn(keycloakAttributes);
    lenient().when(keycloakSettings.get()).thenReturn(keycloakSettingsMap);
  }

  @Test
  public void shouldReplaceBackSlashAndAtSignInUsername() throws Exception {
    // given
    Map<String, Object> claimParams = new HashMap<>();
    claimParams.put("email", "test@test.com");
    claimParams.put("preferred_username", "myorg\\me@mynamecom");
    Claims claims = new DefaultClaims(claimParams).setSubject("id");
    DefaultJws<Claims> jws = new DefaultJws<>(new DefaultJwsHeader(), claims, "");
    when(tokenExtractor.getToken(any(HttpServletRequest.class))).thenReturn("token");
    when(jwtParser.parseClaimsJws(anyString())).thenReturn(jws);
    keycloakSettingsMap.put(USERNAME_CLAIM_SETTING, "preferred_username");
    when(userManager.getOrCreateUser(anyString(), anyString(), anyString()))
        .thenReturn(mock(UserImpl.class, RETURNS_DEEP_STUBS));
    filter =
        new KeycloakEnvironmentInitializationFilter(
            sessionStore,
            jwtParser,
            userManager,
            keycloakProfileRetriever,
            tokenExtractor,
            permissionChecker,
            keycloakSettings,
            "\\\\=-,@=-at-");
    // when
    filter.doFilter(request, response, chain);

    // then
    verify(userManager).getOrCreateUser("id", "test@test.com", "myorg-me-at-mynamecom");
  }

  @Test
  public void shoulBeAbleToDisableUsernameStringReplacing() throws Exception {
    // given
    Map<String, Object> claimParams = new HashMap<>();
    claimParams.put("email", "test@test.com");
    claimParams.put("preferred_username", "myorg\\myname");
    Claims claims = new DefaultClaims(claimParams).setSubject("id");
    DefaultJws<Claims> jws = new DefaultJws<>(new DefaultJwsHeader(), claims, "");
    when(tokenExtractor.getToken(any(HttpServletRequest.class))).thenReturn("token");
    when(jwtParser.parseClaimsJws(anyString())).thenReturn(jws);
    keycloakSettingsMap.put(USERNAME_CLAIM_SETTING, "preferred_username");
    when(userManager.getOrCreateUser(anyString(), anyString(), anyString()))
        .thenReturn(mock(UserImpl.class, RETURNS_DEEP_STUBS));
    filter =
        new KeycloakEnvironmentInitializationFilter(
            sessionStore,
            jwtParser,
            userManager,
            keycloakProfileRetriever,
            tokenExtractor,
            permissionChecker,
            keycloakSettings,
            null);
    // when
    filter.doFilter(request, response, chain);

    // then
    verify(userManager).getOrCreateUser("id", "test@test.com", "myorg\\myname");
  }

  @Test
  public void shouldSkipRequestsWithMachineTokens() throws Exception {
    when(tokenExtractor.getToken(any(HttpServletRequest.class))).thenReturn("not_null_token");
    when(jwtParser.parseClaimsJws(anyString())).thenThrow(MachineTokenJwtException.class);
    // when
    filter.doFilter(request, response, chain);

    // then
    verify(chain).doFilter(eq(request), eq(response));
    verifyNoMoreInteractions(userManager);
  }

  @Test
  public void shouldThrowExceptionWhenNoEmailExistsAndUserDoesNotAlreadyExist() throws Exception {

    Map<String, Object> claimParams = new HashMap<>();
    claimParams.put("preferred_username", "username");
    Claims claims = new DefaultClaims(claimParams).setSubject("id2");
    DefaultJws<Claims> jws = new DefaultJws<>(new DefaultJwsHeader(), claims, "");
    // given
    when(tokenExtractor.getToken(any(HttpServletRequest.class))).thenReturn("token2");
    when(jwtParser.parseClaimsJws(anyString())).thenReturn(jws);
    when(userManager.getById(anyString())).thenThrow(NotFoundException.class);

    // when
    filter.doFilter(request, response, chain);

    verify(response)
        .sendError(
            eq(401),
            eq("Unable to authenticate user because email address is not set in keycloak profile"));
  }

  @Test
  public void shouldRetrieveTheEmailWhenItIsNotInJwtToken() throws Exception {

    Map<String, Object> claimParams = new HashMap<>();
    claimParams.put("preferred_username", "username");
    Claims claims = new DefaultClaims(claimParams).setSubject("id");
    DefaultJws<Claims> jws = new DefaultJws<>(new DefaultJwsHeader(), claims, "");
    UserImpl user = new UserImpl("id", "test@test.com", "username");
    keycloakSettingsMap.put(KeycloakConstants.USERNAME_CLAIM_SETTING, "preferred_username");
    // given
    when(tokenExtractor.getToken(any(HttpServletRequest.class))).thenReturn("token");
    when(jwtParser.parseClaimsJws(anyString())).thenReturn(jws);
    when(userManager.getById(anyString())).thenThrow(NotFoundException.class);
    when(userManager.getOrCreateUser(anyString(), anyString(), anyString())).thenReturn(user);
    keycloakAttributes.put("email", "test@test.com");

    try {
      // when
      filter.doFilter(request, response, chain);
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }

    verify(userManager).getOrCreateUser("id", "test@test.com", "username");
  }

  @Test
  public void shouldRefreshSubjectWhenTokensNotMatch() throws Exception {

    Subject existingSubject = new SubjectImpl("name", "id1", "token", false);
    UserImpl user = new UserImpl("id2", "test2@test.com", "username2");

    ArgumentCaptor<AuthorizedSubject> captor = ArgumentCaptor.forClass(AuthorizedSubject.class);
    DefaultJws<Claims> claims = createJws();
    Subject expectedSubject = new SubjectImpl(user.getName(), user.getId(), "token2", false);
    // given
    when(tokenExtractor.getToken(any(HttpServletRequest.class))).thenReturn("token2");
    when(jwtParser.parseClaimsJws(anyString())).thenReturn(claims);
    when(session.getAttribute(eq(CHE_SUBJECT_ATTRIBUTE))).thenReturn(existingSubject);
    when(userManager.getOrCreateUser(anyString(), anyString(), anyString())).thenReturn(user);
    EnvironmentContext context = spy(EnvironmentContext.getCurrent());
    EnvironmentContext.setCurrent(context);

    // when
    filter.doFilter(request, response, chain);

    // then
    verify(session).setAttribute(eq(CHE_SUBJECT_ATTRIBUTE), captor.capture());
    verify(context).setSubject(captor.capture());
    assertEquals(expectedSubject.getToken(), captor.getAllValues().get(0).getToken());
    assertEquals(expectedSubject.getToken(), captor.getAllValues().get(1).getToken());
    assertEquals(expectedSubject.getUserId(), captor.getAllValues().get(0).getUserId());
    assertEquals(expectedSubject.getUserId(), captor.getAllValues().get(1).getUserId());
    assertEquals(expectedSubject.getUserName(), captor.getAllValues().get(0).getUserName());
    assertEquals(expectedSubject.getUserName(), captor.getAllValues().get(1).getUserName());
  }

  private DefaultJws<Claims> createJws() {
    Map<String, Object> claimParams = new HashMap<>();
    claimParams.put("email", "test@test.com");
    claimParams.put("preferred_username", "username");
    Claims claims = new DefaultClaims(claimParams).setSubject("id2");
    return new DefaultJws<>(new DefaultJwsHeader(), claims, "");
  }
}
