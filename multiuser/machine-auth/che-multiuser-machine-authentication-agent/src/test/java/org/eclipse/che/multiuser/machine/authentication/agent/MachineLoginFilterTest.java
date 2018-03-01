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

import static io.jsonwebtoken.SignatureAlgorithm.RS512;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.MACHINE_TOKEN_KIND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.HttpHeaders;
import org.eclipse.che.commons.auth.token.RequestTokenExtractor;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
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

  private static final Gson GSON = new Gson();

  private static final String SIGNATURE_ALGORITHM = "RSA";
  private static final String WORKSPACE_ID = "workspace31";
  private static final String PRINCIPAL = "principal";
  private static final String USER_ID = "test_user31";
  private static final String USER_NAME = "test_user";

  private static final Map<String, Object> HEADER = new HashMap<>();
  private static final Map<String, Object> CLAIMS = new HashMap<>(2);

  static {
    HEADER.put("kind", MACHINE_TOKEN_KIND);
    CLAIMS.put(Constants.WORKSPACE_ID_CLAIM, WORKSPACE_ID);
    CLAIMS.put(Constants.USER_ID_CLAIM, USER_ID);
    CLAIMS.put(Constants.USER_NAME_CLAIM, USER_NAME);
    CLAIMS.put(Claims.ID, "8312-213-1we31");
  }

  @Mock private RequestTokenExtractor tokenExtractorMock;
  @Mock private FilterChain chainMock;
  @Mock private HttpSession sessionMock;
  @Mock private HttpServletResponse responseMock;

  private MachineLoginFilter machineLoginFilter;

  private String machineToken;
  private KeyPair keyPair;
  private Subject subject;

  @BeforeMethod
  public void setUp() throws Exception {
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance(SIGNATURE_ALGORITHM);
    kpg.initialize(KEY_SIZE);
    keyPair = kpg.generateKeyPair();
    machineToken =
        Jwts.builder()
            .setClaims(CLAIMS)
            .setHeader(HEADER)
            .signWith(RS512, keyPair.getPrivate())
            .compact();
    machineLoginFilter =
        spy(new MachineLoginFilter(WORKSPACE_ID, keyPair.getPublic(), tokenExtractorMock));

    subject = new SubjectImpl(USER_NAME, USER_ID, machineToken, false);
    when(tokenExtractorMock.getToken(any(HttpServletRequest.class))).thenReturn(machineToken);
  }

  @Test
  public void testProcessRequestWithSubjectFromSession() throws Exception {
    when(sessionMock.getAttribute(PRINCIPAL)).thenReturn(subject);

    machineLoginFilter.doFilter(getRequestMock(sessionMock, machineToken), responseMock, chainMock);

    verify(sessionMock).getAttribute(PRINCIPAL);
    verifyZeroInteractions(tokenExtractorMock);
  }

  @Test
  public void testSetErrorInResponseWhenNullTokenProvided() throws Exception {
    when(tokenExtractorMock.getToken(any(HttpServletRequest.class))).thenReturn(null);

    machineLoginFilter.doFilter(getRequestMock(null, machineToken), responseMock, chainMock);

    verify(tokenExtractorMock).getToken(any(HttpServletRequest.class));
    verify(responseMock)
        .sendError(SC_UNAUTHORIZED, "Authentication on machine failed, token is missed.");
  }

  @Test
  public void testProcessRequestWithValidTokenAndCreateSession() throws Exception {
    machineLoginFilter.doFilter(getRequestMock(null, machineToken), responseMock, chainMock);

    verify(tokenExtractorMock).getToken(any(HttpServletRequest.class));
    verify(sessionMock).setAttribute(PRINCIPAL, subject);
    verifyZeroInteractions(responseMock);
  }

  @Test
  public void testSetErrorInResponseWhenInvalidTokenProvided() throws Exception {
    final String invalidToken = "invalid_token";
    when(tokenExtractorMock.getToken(any(HttpServletRequest.class))).thenReturn(invalidToken);

    machineLoginFilter.doFilter(getRequestMock(null, invalidToken), responseMock, chainMock);

    verify(tokenExtractorMock).getToken(any(HttpServletRequest.class));
    verify(responseMock)
        .sendError(
            eq(SC_UNAUTHORIZED),
            argThat(s -> s.startsWith("Authentication on machine failed cause:")));
  }

  @Test
  public void testSetErrorInResponseWhenNonMachineTokenProvided() throws Exception {
    final String differentToken =
        Jwts.builder()
            .setPayload(GSON.toJson(subject))
            .setHeader(new HashMap<>())
            .signWith(RS512, keyPair.getPrivate())
            .compact();
    when(tokenExtractorMock.getToken(any(HttpServletRequest.class))).thenReturn(differentToken);

    machineLoginFilter.doFilter(getRequestMock(null, differentToken), responseMock, chainMock);

    verify(tokenExtractorMock).getToken(any(HttpServletRequest.class));
    verify(responseMock)
        .sendError(SC_UNAUTHORIZED, "Authentication on machine failed, invalid token provided.");
  }

  @Test
  public void testSetErrorInResponseWhenTokenIsNotRelatedToThisWorkspace() throws Exception {
    final Map<String, Object> headers = new HashMap<>();
    headers.put("kind", MACHINE_TOKEN_KIND);
    headers.put("workspace", "workspace73");
    final String differentToken =
        Jwts.builder()
            .setPayload(GSON.toJson(subject))
            .setHeader(headers)
            .signWith(RS512, keyPair.getPrivate())
            .compact();
    when(tokenExtractorMock.getToken(any(HttpServletRequest.class))).thenReturn(differentToken);

    machineLoginFilter.doFilter(getRequestMock(null, differentToken), responseMock, chainMock);

    verify(tokenExtractorMock).getToken(any(HttpServletRequest.class));
    verify(responseMock)
        .sendError(SC_UNAUTHORIZED, "Authentication on machine failed, invalid token provided.");
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
