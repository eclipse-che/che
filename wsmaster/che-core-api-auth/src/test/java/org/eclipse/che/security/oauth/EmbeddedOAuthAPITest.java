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
package org.eclipse.che.security.oauth;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.NotFoundException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Mykhailo Kuznietsov */
@Listeners(value = MockitoTestNGListener.class)
public class EmbeddedOAuthAPITest {

  @Mock OAuthAuthenticatorProvider providers;

  @InjectMocks EmbeddedOAuthAPI embeddedOAuthAPI;

  @Test(
    expectedExceptions = NotFoundException.class,
    expectedExceptionsMessageRegExp = "Unsupported OAuth provider unknown"
  )
  public void shouldThrowExceptionIfNoSuchProviderFound() throws Exception {
    embeddedOAuthAPI.getToken("unknown");
  }

  @Test
  public void shouldBeAbleToGetUserToken() throws Exception {
    String provider = "myprovider";
    String token = "token123";
    OAuthAuthenticator authenticator = mock(OAuthAuthenticator.class);
    when(providers.getAuthenticator(eq(provider))).thenReturn(authenticator);

    when(authenticator.getToken(anyString())).thenReturn(newDto(OAuthToken.class).withToken(token));

    OAuthToken result = embeddedOAuthAPI.getToken(provider);

    assertEquals(result.getToken(), token);
  }
}
