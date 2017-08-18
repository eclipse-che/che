/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.git;

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


import static javax.ws.rs.core.UriBuilder.fromUri;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link LocalGitUserResolver}
 *
 * @author  Max Shaposhnik
 */

@Listeners(MockitoTestNGListener.class)
public class LocalGitUserResolverTest {

    private final static String API_URL         = "apiUrl";
    private final static String PREFECENCES_URL = "apiUrl/preferences";

    @Mock
    private HttpJsonRequestFactory requestFactory;
    @Mock
    private HttpJsonResponse       jsonResponse;

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
        //when
        resolver.getUser();
        //then
        String url = fromUri(PREFECENCES_URL).path(PreferencesService.class, "find").build().toString();
        verify(requestFactory).fromUrl(eq(url));
        verify(jsonRequest).useGetMethod();
        verify(jsonRequest).request();
        verify(jsonResponse).asProperties();
    }
}
