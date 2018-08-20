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

import static org.eclipse.che.multiuser.machine.authentication.shared.Constants.MACHINE_TOKEN_KIND;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultJwsHeader;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.Map;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class KeycloakSigningKeyResolverTest {

  @Mock private JwkProvider jwkProvider;

  @InjectMocks private KeycloakSigningKeyResolver signingKeyResolver;

  @Test(expectedExceptions = MachineTokenJwtException.class)
  public void shouldThrowMachineTokenExceptionOnMachineTokensWithPlainText() {
    final Map<String, Object> param = new HashMap<>();
    param.put("kind", MACHINE_TOKEN_KIND);
    DefaultJwsHeader header = new DefaultJwsHeader(param);

    signingKeyResolver.resolveSigningKey(header, "plaintext");
    verifyNoMoreInteractions(jwkProvider);
  }

  @Test(expectedExceptions = MachineTokenJwtException.class)
  public void shouldThrowMachineTokenExceptionOnMachineTokensWithClaims() {
    final Map<String, Object> param = new HashMap<>();
    param.put("kind", MACHINE_TOKEN_KIND);
    DefaultJwsHeader header = new DefaultJwsHeader(param);

    signingKeyResolver.resolveSigningKey(header, new DefaultClaims());
    verifyNoMoreInteractions(jwkProvider);
  }

  @Test(expectedExceptions = JwtException.class)
  public void shouldThrowJwtExceptionifNoKeyIdHeader() {

    signingKeyResolver.resolveSigningKey(new DefaultJwsHeader(), "plaintext");
    verifyNoMoreInteractions(jwkProvider);
  }

  @Test
  public void shouldReturnPublicKey() throws Exception {
    final String kid = "123";
    final Jwk jwk = mock(Jwk.class);
    final Map<String, Object> param = new HashMap<>();
    param.put("kid", kid);
    final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(1024);
    final KeyPair keyPair = kpg.generateKeyPair();

    when(jwk.getPublicKey()).thenReturn(keyPair.getPublic());
    when(jwkProvider.get(eq(kid))).thenReturn(jwk);

    Key actual = signingKeyResolver.resolveSigningKey(new DefaultJwsHeader(param), "plaintext");
    assertEquals(actual, keyPair.getPublic());
  }
}
