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
package org.eclipse.che.multiuser.machine.authentication.server;

import static io.jsonwebtoken.SignatureAlgorithm.RS512;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.MACHINE_TOKEN_KIND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.DefaultClaims;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.multiuser.api.authentication.commons.SessionStore;
import org.eclipse.che.multiuser.api.authentication.commons.token.RequestTokenExtractor;
import org.eclipse.che.multiuser.api.permission.server.PermissionChecker;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManager;
import org.eclipse.che.multiuser.machine.authentication.shared.Constants;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link MachineLoginFilter}.
 *
 * @author Anton Korneta
 */
@Listeners(MockitoTestNGListener.class)
public class MachineLoginFilterTest {

  private static final int KEY_SIZE = 1024;

  private static final String REQUEST_SCHEME = "https";
  private static final String SIGNATURE_ALGORITHM = "RSA";
  private static final String WORKSPACE_ID = "workspace31";

  private static final Subject SUBJECT =
      new SubjectImpl("test_user", "test_user31", "userToken", false);

  private static final Map<String, Object> HEADER = new HashMap<>();
  private static final Claims CLAIMS = Jwts.claims();

  static {
    HEADER.put("kind", MACHINE_TOKEN_KIND);
    CLAIMS.put(Constants.WORKSPACE_ID_CLAIM, WORKSPACE_ID);
    CLAIMS.put(Constants.USER_ID_CLAIM, SUBJECT.getUserId());
    CLAIMS.put(Constants.USER_NAME_CLAIM, SUBJECT.getUserName());
    CLAIMS.put(Claims.ID, "84123-132-fn31");
  }

  @Mock private UserManager userManagerMock;
  @Mock private RequestTokenExtractor tokenExtractorMock;
  @Mock private SignatureKeyManager keyManagerMock;
  @Mock private PermissionChecker permissionCheckerMock;
  @Mock private FilterChain chainMock;
  @Mock private HttpSession sessionMock;
  @Mock private HttpServletResponse responseMock;

  private MachineLoginFilter machineLoginFilter;

  @BeforeMethod
  private void setUp() throws Exception {
    final User userMock = mock(User.class);
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance(SIGNATURE_ALGORITHM);
    kpg.initialize(KEY_SIZE);
    final KeyPair keyPair = kpg.generateKeyPair();
    final String token =
        Jwts.builder()
            .setClaims(CLAIMS)
            .setHeader(HEADER)
            .signWith(RS512, keyPair.getPrivate())
            .compact();
    machineLoginFilter =
        new MachineLoginFilter(
            new SessionStore(),
            tokenExtractorMock,
            userManagerMock,
            new MachineSigningKeyResolver(keyManagerMock),
            permissionCheckerMock);

    lenient().when(tokenExtractorMock.getToken(any(HttpServletRequest.class))).thenReturn(token);
    lenient().when(keyManagerMock.getOrCreateKeyPair(eq(WORKSPACE_ID))).thenReturn(keyPair);

    lenient().when(userMock.getName()).thenReturn(SUBJECT.getUserName());
    lenient().when(userManagerMock.getById(SUBJECT.getUserId())).thenReturn(userMock);
  }

  @Test
  public void testProcessRequestWithValidToken() throws Exception {
    machineLoginFilter.doFilter(getRequestMock(), responseMock, chainMock);

    verify(keyManagerMock, atLeastOnce()).getOrCreateKeyPair(eq(WORKSPACE_ID));
    verify(userManagerMock).getById(anyString());
    verifyZeroInteractions(responseMock);
  }

  @Test
  public void testNotProceedRequestWhenSignatureCheckIsFailed() throws Exception {
    final HttpServletRequest requestMock = getRequestMock();
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance(SIGNATURE_ALGORITHM);
    kpg.initialize(KEY_SIZE);
    final KeyPair pair = kpg.generateKeyPair();
    when(keyManagerMock.getOrCreateKeyPair(eq(WORKSPACE_ID))).thenReturn(pair);

    machineLoginFilter.doFilter(requestMock, responseMock, chainMock);

    verify(tokenExtractorMock, atLeastOnce()).getToken(any(HttpServletRequest.class));
    verify(responseMock)
        .sendError(
            401,
            "Machine token authentication failed: JWT signature does not match locally computed signature."
                + " JWT validity cannot be asserted and should not be trusted.");
  }

  @Test
  public void testNotProceedRequestWhenNoWorkspaceIdClaim() throws Exception {
    final HttpServletRequest requestMock = getRequestMock();
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance(SIGNATURE_ALGORITHM);
    kpg.initialize(KEY_SIZE);
    final KeyPair pair = kpg.generateKeyPair();
    final Claims badClaims = new DefaultClaims();
    badClaims.put(Constants.USER_ID_CLAIM, SUBJECT.getUserId());
    badClaims.put(Claims.ID, "84123-132-fn31");
    final String token =
        Jwts.builder()
            .setClaims(badClaims)
            .setHeader(HEADER)
            .signWith(RS512, pair.getPrivate())
            .compact();
    when(tokenExtractorMock.getToken(any(HttpServletRequest.class))).thenReturn(token);

    machineLoginFilter.doFilter(requestMock, responseMock, chainMock);

    verify(tokenExtractorMock, atLeastOnce()).getToken(any(HttpServletRequest.class));
    verify(responseMock)
        .sendError(
            401,
            "Machine token authentication failed: Unable to fetch signature key pair: no workspace id present in token");
  }

  @Test
  public void testProceedRequestWhenEmptyTokenProvided() throws Exception {
    final HttpServletRequest requestMock = getRequestMock();
    when(tokenExtractorMock.getToken(any(HttpServletRequest.class))).thenReturn(null);

    machineLoginFilter.doFilter(requestMock, responseMock, chainMock);

    verify(tokenExtractorMock, atLeastOnce()).getToken(any(HttpServletRequest.class));
    verify(chainMock).doFilter(requestMock, responseMock);
    verifyZeroInteractions(keyManagerMock);
    verifyZeroInteractions(userManagerMock);
    verifyZeroInteractions(responseMock);
  }

  @Test
  public void testSetErrorInResponseWhenNoUserFoundForProvidedToken() throws Exception {
    when(userManagerMock.getById(anyString())).thenThrow(new NotFoundException("User not found"));

    machineLoginFilter.doFilter(getRequestMock(), responseMock, chainMock);

    verify(keyManagerMock, atLeastOnce()).getOrCreateKeyPair(eq(WORKSPACE_ID));
    verify(userManagerMock).getById(anyString());
    verify(responseMock)
        .sendError(401, "Machine token authentication failed: Corresponding user doesn't exist.");
  }

  @Test
  public void testSetErrorInResponseWhenUnableToGetUserForProvidedToken() throws Exception {
    when(userManagerMock.getById(anyString())).thenThrow(new ServerException("err"));

    machineLoginFilter.doFilter(getRequestMock(), responseMock, chainMock);

    verify(keyManagerMock, atLeastOnce()).getOrCreateKeyPair(eq(WORKSPACE_ID));
    verify(userManagerMock).getById(anyString());
    verify(responseMock)
        .sendError(eq(401), argThat(s -> s.startsWith("Machine token authentication failed:")));
  }

  private HttpServletRequest getRequestMock() {
    final HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getSession(true)).thenReturn(sessionMock);
    lenient().when(request.getScheme()).thenReturn(REQUEST_SCHEME);
    return request;
  }
}
