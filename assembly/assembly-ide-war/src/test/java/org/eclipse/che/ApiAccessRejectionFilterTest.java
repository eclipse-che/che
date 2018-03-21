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
package org.eclipse.che;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Alexander Garagatyi */
@Listeners(value = {MockitoTestNGListener.class})
public class ApiAccessRejectionFilterTest {
  @Mock private FilterChain chain;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @Mock private ServletOutputStream outputStream;

  @InjectMocks private ApiAccessRejectionFilter filter;

  @Test(dataProvider = "apiPathProvider")
  public void shouldReturnErrorOnRequestsToApi(String path) throws Exception {
    // given
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn(path);
    when(response.getOutputStream()).thenReturn(outputStream);

    // when
    filter.doFilter(request, response, chain);

    // then
    verifyZeroInteractions(chain);
    verify(response).setStatus(500);
    verify(outputStream).write(eq(ApiAccessRejectionFilter.ERROR_MESSAGE.getBytes()));
  }

  @DataProvider(name = "apiPathProvider")
  public static Object[][] apiPathProvider() {
    return new Object[][] {
      {"/api"}, {"/api/"}, {"/api/workspace"}, {"/api/workspace/"}, {"/api/system/state"},
    };
  }

  @Test(dataProvider = "nonApiPathProvider")
  public void shouldSkipNonApiRequests(String path) throws Exception {
    // given
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn(path);

    // when
    filter.doFilter(request, response, chain);

    // then
    verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    verifyZeroInteractions(response);
    verifyNoMoreInteractions(chain);
  }

  @DataProvider(name = "nonApiPathProvider")
  public Object[][] nonApiPathProvider() {
    return new Object[][] {
      {"/ws-id/"}, {"/wsname"}, {"/dashboard"}, {"/dashboard/"}, {"/"}, {""},
    };
  }
}
