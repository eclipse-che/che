/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.security.oauth;


import org.eclipse.che.api.auth.shared.dto.OAuthToken;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.commons.test.mockito.answer.SelfReturningAnswer;
import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Listeners(value = {MockitoTestNGListener.class})
public class RemoteOAuthTokenProviderTest {

    private HttpJsonRequest httpJsonRequest;

    private RemoteOAuthTokenProvider tokenProvider;

    @Mock
    private HttpJsonRequestFactory httpJsonRequestFactory;
    @Mock
    private HttpJsonResponse       httpJsonResponse;


    @BeforeMethod
    public void setUp() throws Exception {
        httpJsonRequest = mock(HttpJsonRequest.class, new SelfReturningAnswer());
        when(httpJsonRequestFactory.fromLink(any())).thenReturn(httpJsonRequest);
        tokenProvider = new RemoteOAuthTokenProvider("http://dev.box.com/api", httpJsonRequestFactory);
    }

    @Test
    public void shouldReturnToken() throws Exception {
        //given
        OAuthToken expected = DtoFactory.newDto(OAuthToken.class).withScope("scope").withToken("token");
        when(httpJsonResponse.asDto(any(Class.class))).thenReturn(expected);
        when(httpJsonRequest.request()).thenReturn(httpJsonResponse);
        //when
        OAuthToken actual = tokenProvider.getToken("google", "id");
        //then
        assertEquals(actual, expected);
    }

    @Test
    public void shouldConstructCorrectUrl() throws Exception {
        //given
        OAuthToken expected = DtoFactory.newDto(OAuthToken.class).withScope("scope").withToken("token");
        when(httpJsonResponse.asDto(any(Class.class))).thenReturn(expected);
        when(httpJsonRequest.request()).thenReturn(httpJsonResponse);
        //when
        tokenProvider.getToken("google", "id");
        //then
        ArgumentCaptor<Link> argumentCaptor = ArgumentCaptor.forClass(Link.class);
        verify(httpJsonRequestFactory).fromLink(argumentCaptor.capture());
        Link link = argumentCaptor.getValue();
        assertEquals(link.getMethod(), "GET");
        assertEquals(link.getHref(), "http://dev.box.com/api/oauth/token?oauth_provider=google");
        assertEquals(link.getParameters().size(), 0);
    }

    @Test
    public void shouldReturnNollForNotExistedProvider() throws Exception {
        //given
        when(httpJsonRequest.request()).thenReturn(httpJsonResponse);
        //when
        //then
        Assert.assertNull(tokenProvider.getToken("smoogle", "id"));
    }

    @Test
    public void shouldReturnNullOnNotFoundException() throws Exception {
        //given
        when(httpJsonRequest.request()).thenThrow(NotFoundException.class);
        //when
        //then
        Assert.assertNull(tokenProvider.getToken("google", "id"));
    }

    @Test
    public void shouldReturnNullOnIfUserIsEmpty() throws Exception {
        //given
        //when
        //then
        Assert.assertNull(tokenProvider.getToken("google", ""));
    }

    @Test(expectedExceptions = IOException.class)
    public void shouldThrowIoExceptionOnIoException() throws Exception {
        //given
        when(httpJsonRequest.request()).thenThrow(IOException.class);
        //when
        //then
        Assert.assertNull(tokenProvider.getToken("google", "id"));
    }
}
