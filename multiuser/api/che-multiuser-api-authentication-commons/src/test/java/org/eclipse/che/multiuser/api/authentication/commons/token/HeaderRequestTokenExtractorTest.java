/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.api.authentication.commons.token;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class HeaderRequestTokenExtractorTest {

  private HeaderRequestTokenExtractor tokenExtractor = new HeaderRequestTokenExtractor();

  @Mock HttpServletRequest servletRequest;

  @Test(dataProvider = "validHeadersProvider")
  public void shouldExtractTokensFromValidHeaders(String headerValue, String expectedToken) {

    when(servletRequest.getHeader(eq(AUTHORIZATION))).thenReturn(headerValue);

    // when
    String token = tokenExtractor.getToken(servletRequest);

    // then
    assertEquals(token, expectedToken);
  }

  @Test(
      dataProvider = "invalidHeadersProvider",
      expectedExceptions = BadRequestException.class,
      expectedExceptionsMessageRegExp = "Invalid authorization header format.")
  public void shouldThrowExceptionOnInvalidToken(String headerValue) {

    when(servletRequest.getHeader(eq(AUTHORIZATION))).thenReturn(headerValue);

    // when
    tokenExtractor.getToken(servletRequest);
  }

  @DataProvider
  private Object[][] validHeadersProvider() {
    return new Object[][] {
      {"token123", "token123"},
      {"bearer token123", "token123"},
      {"Bearer token123", "token123"},
    };
  }

  @DataProvider
  private Object[][] invalidHeadersProvider() {
    return new Object[][] {{"bearertoken123"}, {"bearer   token123"}, {"bearer token 123"}};
  }
}
