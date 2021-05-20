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
package org.eclipse.che;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Max Shaposhnik (mshaposhnik@codenvy.com) */
@Listeners(value = {MockitoTestNGListener.class})
public class DashboardRedirectionFilterTest {
  @Mock private FilterChain chain;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @InjectMocks private DashboardRedirectionFilter filter;

  @Test(dataProvider = "nonNamespacePathProvider")
  public void shouldRedirectIfGetRequestIsNotNamespaceWorkspaceName(String uri) throws Exception {
    // given
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn(uri);
    EnvironmentContext context = new EnvironmentContext();
    context.setSubject(new SubjectImpl("id123", "name", "token123", false));
    EnvironmentContext.setCurrent(context);

    // when
    filter.doFilter(request, response, chain);

    // then
    verify(response).sendRedirect(eq("/dashboard/"));
  }

  @Test(dataProvider = "nonNamespacePathProvider")
  public void shouldRedirectIfHEADRequestIsNotNamespaceWorkspaceName(String uri) throws Exception {
    // given
    when(request.getMethod()).thenReturn("HEAD");
    when(request.getRequestURI()).thenReturn(uri);
    EnvironmentContext context = new EnvironmentContext();
    context.setSubject(new SubjectImpl("id123", "name", "token123", false));
    EnvironmentContext.setCurrent(context);

    // when
    filter.doFilter(request, response, chain);

    // then
    verify(response).sendRedirect(eq("/dashboard/"));
  }

  @DataProvider(name = "nonNamespacePathProvider")
  public Object[][] nonProjectPathProvider() {
    return new Object[][] {
      {"/"}, {"/ws-id/"}, {"/wsname"}, {"/unknown/resource/index.html"},
    };
  }

  @Test
  public void shouldSkipRequestToAppResources() throws Exception {
    // given
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/_app/loader.html");

    // when
    filter.doFilter(request, response, chain);

    // then
    verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
  }

  @Test(dataProvider = "notGETNorHEADMethodProvider")
  public void shouldSkipNotGETNorHEADRequest(String method) throws Exception {
    // given
    when(request.getMethod()).thenReturn(method);
    when(request.getRequestURI()).thenReturn("/namespace/workspaceName");

    // when
    filter.doFilter(request, response, chain);

    // then
    verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
  }

  @DataProvider(name = "notGETNorHEADMethodProvider")
  public Object[][] notGETNorHEADMethodProvider() {
    return new Object[][] {{"POST"}, {"DELETE"}, {"PUT"}};
  }
}
