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
package org.eclipse.che.api.git;

import static javax.ws.rs.core.UriBuilder.fromUri;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.user.server.PreferencesService;
import org.eclipse.che.commons.test.mockito.answer.SelfReturningAnswer;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link LocalGitUserResolver}
 *
 * @author Max Shaposhnik
 */
@Listeners(MockitoTestNGListener.class)
public class LocalGitUserResolverTest {

  private static final String API_URL = "apiUrl";
  private static final String PREFECENCES_URL = "apiUrl/preferences";

  @Mock private HttpJsonRequestFactory requestFactory;
  @Mock private HttpJsonResponse jsonResponse;

  private HttpJsonRequest jsonRequest;

  private LocalGitUserResolver resolver;

  @BeforeMethod
  public void setup() throws Exception {
    jsonRequest = mock(HttpJsonRequest.class, new SelfReturningAnswer());
    when(jsonRequest.request()).thenReturn(jsonResponse);
    when(requestFactory.fromUrl(anyString())).thenReturn(jsonRequest);
    resolver = new LocalGitUserResolver(API_URL, requestFactory);
  }

  @Test
  public void shouldMakeGetPreferencesRequest() throws Exception {
    // when
    resolver.getUser();
    // then
    String url = fromUri(PREFECENCES_URL).path(PreferencesService.class, "find").build().toString();
    verify(requestFactory).fromUrl(eq(url));
    verify(jsonRequest).useGetMethod();
    verify(jsonRequest).request();
    verify(jsonResponse).asProperties();
  }
}
