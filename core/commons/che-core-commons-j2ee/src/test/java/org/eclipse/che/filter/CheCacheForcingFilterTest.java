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
package org.eclipse.che.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/** @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/17/17. */
@Listeners(value = {MockitoTestNGListener.class})
public class CheCacheForcingFilterTest {

  @Mock private FilterChain chain;

  @Mock private HttpServletRequest request;

  @Mock private HttpServletResponse response;

  @InjectMocks private CheCacheForcingFilter filter;

  @BeforeMethod
  public void setUp() throws Exception {
    filter.init(new MockFilterConfig());
  }

  @Test(dataProvider = "cachedPathProvider")
  public void shouldSetForceCacheHeaders(String uri) throws Exception {
    when(request.getRequestURI()).thenReturn(uri);

    // when
    filter.doFilter(request, response, chain);

    verify(response).setDateHeader(eq("Date"), anyLong());
    verify(response).setDateHeader(eq("Expires"), anyLong());
    verify(response).setHeader(eq("Cache-control"), matches("public, max-age=[0-9]+"));
    verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
  }

  @Test(dataProvider = "nonCachedPathProvider")
  public void shouldBypassForceCacheHeaders(String uri) throws Exception {
    when(request.getRequestURI()).thenReturn(uri);

    // when
    filter.doFilter(request, response, chain);

    verify(response, never()).setHeader(eq("Cache-control"), anyString());
    verify(chain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
  }

  @DataProvider(name = "cachedPathProvider")
  public Object[][] cachedPathProvider() {
    return new Object[][] {
      {"/_app/browserNotSupported.js"},
      {"/_app/_app.cache.js"},
      {"/other/_app/cache.js"},
      {"/other/something.cache.js"}
    };
  }

  @DataProvider(name = "nonCachedPathProvider")
  public Object[][] nonCachedPathProvider() {
    return new Object[][] {
      {"/other/.something.js"}, {"/app/something.js"}, {"/something/something"}
    };
  }

  private class MockFilterConfig implements FilterConfig {
    private final Map<String, String> filterParams = new HashMap<>();

    MockFilterConfig() {
      this.filterParams.put("pattern1", "^.*\\.cache\\..*$");
      this.filterParams.put("pattern2", "^.*/_app/.*$");
    }

    @Override
    public String getFilterName() {
      return this.getClass().getName();
    }

    @Override
    public ServletContext getServletContext() {
      throw new UnsupportedOperationException(
          "The method does not supported in " + this.getClass());
    }

    @Override
    public String getInitParameter(String key) {
      return this.filterParams.get(key);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
      return Collections.enumeration(filterParams.keySet());
    }
  }
}
