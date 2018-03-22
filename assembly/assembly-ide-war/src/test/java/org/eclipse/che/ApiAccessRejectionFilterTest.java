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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
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

  @Test
  public void shouldReturnError() throws Exception {
    // given
    when(response.getOutputStream()).thenReturn(outputStream);

    // when
    filter.doFilter(request, response, chain);

    // then
    verifyZeroInteractions(chain);
    verify(response).setStatus(500);
    verify(outputStream).write(eq(ApiAccessRejectionFilter.ERROR_MESSAGE.getBytes()));
  }
}
