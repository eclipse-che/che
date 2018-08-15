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

import static io.jsonwebtoken.SignatureAlgorithm.RS512;
import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.MACHINE_TOKEN_KIND;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import io.jsonwebtoken.Jwts;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.che.multiuser.machine.authentication.server.signature.SignatureKeyManager;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
  @Mock private SignatureKeyManager signatureKeyManager;
  @Mock private KeycloakSettings keycloakSettings;
  @Mock private JwkProvider jwkProvider;
  @Mock private Jwk jwk;

  @InjectMocks private TestLoginFilter abstractKeycloakFilter;

  private String machineToken;

  @BeforeMethod
  public void setup() throws Exception {
    Field provider =
        abstractKeycloakFilter.getClass().getSuperclass().getDeclaredField("jwkProvider");
    provider.setAccessible(true);
    provider.set(abstractKeycloakFilter, jwkProvider);
    final KeyPair keyPair = getKeyPair();
    final Map<String, Object> header = new HashMap<>();
    header.put("kind", MACHINE_TOKEN_KIND);
    machineToken =
        Jwts.builder()
            .setPayload("payload")
            .setHeader(header)
            .signWith(RS512, keyPair.getPrivate())
            .compact();

    when(jwkProvider.get(anyString())).thenReturn(jwk);
    when(jwk.getPublicKey()).thenReturn(keyPair.getPublic());
    when(signatureKeyManager.getKeyPair(anyString())).thenReturn(keyPair);
    when(request.getRequestURI()).thenReturn(null);
  }

  @Test
  public void testShouldSkipAuthWhenRetrievingOIDCKeycloakJsFile() {
    when(request.getRequestURI()).thenReturn("https://localhost:8080/api/keycloak/OIDCKeycloak.js");
    assertTrue(abstractKeycloakFilter.shouldSkipAuthentication(request, null));
  }

  @Test
  public void testShouldNotSkipAuthWhenNullTokenProvided() {
    assertFalse(abstractKeycloakFilter.shouldSkipAuthentication(request, null));
  }

  @Test
  public void testShouldNotSkipAuthWhenProvidedTokenIsNotMachine() throws Exception {
    final Map<String, Object> header = new HashMap<>();
    final KeyPair keyPair = getKeyPair();
    header.put("kid", "123");
    String localToken =
        Jwts.builder()
            .setPayload("payload")
            .setHeader(header)
            .signWith(RS512, keyPair.getPrivate())
            .compact();
    when(jwk.getPublicKey()).thenReturn(keyPair.getPublic());

    assertFalse(abstractKeycloakFilter.shouldSkipAuthentication(request, localToken));
  }

  @Test
  public void testAuthIsNotNeededWhenMachineTokenProvided() {
    assertTrue(abstractKeycloakFilter.shouldSkipAuthentication(request, machineToken));
  }

  static class TestLoginFilter extends AbstractKeycloakFilter {

    public TestLoginFilter(KeycloakSettings keycloakSettings) throws MalformedURLException {
      super(keycloakSettings, 1000);
    }

    @Override
    public void doFilter(
        ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) {}
  }

  private KeyPair getKeyPair() throws NoSuchAlgorithmException {
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(1024);
    return kpg.generateKeyPair();
  }
}
