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
package org.eclipse.che.factory;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.factory.filter.FactoryRetrieverFilter;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Field;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 6/24/15.
 *
 */
@Listeners(value = {MockitoTestNGListener.class})
public class FactoryRetrieverFilterTest {

    @Mock
    private HttpServletRequest     req;

    @Mock
    private HttpServletResponse    res;

    @Mock
    private FilterChain            chain;

    @Mock
    private HttpJsonRequestFactory requestFactory;

    private HttpJsonRequest        request;

    FactoryRetrieverFilter filter;

    @BeforeMethod
    public void setup() throws Exception {
        
        request = mock(HttpJsonRequest.class, (Answer) invocation -> {
            if (invocation.getMethod().getReturnType().isInstance(invocation.getMock())) {
                return invocation.getMock();
            }
            return RETURNS_DEFAULTS.answer(invocation);
        });
        when(requestFactory.fromUrl(anyString())).thenReturn(request);
        
        filter = new FactoryRetrieverFilter();
        
        Field f = filter.getClass().getDeclaredField("httpRequestFactory");
        f.setAccessible(true);
        f.set(filter, requestFactory);

        Field f3 = filter.getClass().getDeclaredField("apiEndPoint");
        f3.setAccessible(true);
        f3.set(filter, "http://codenvy.com/api");
    }


    @Test(expectedExceptions = ServletException.class)
    public void shouldForwardToErrorPageIfGetFactoryThrowsException() throws Exception {
        when(req.getParameter("id")).thenReturn("12345");
        when(request.request()).thenThrow(new ServerException("get factory exception message"));

        filter.doFilter(req, res, chain);
    }
}
