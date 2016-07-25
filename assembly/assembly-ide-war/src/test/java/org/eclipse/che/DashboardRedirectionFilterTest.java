/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che;

import org.eclipse.che.api.core.rest.HttpJsonHelper;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.omg.CORBA.ServerRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Listeners(value = {MockitoTestNGListener.class})
public class DashboardRedirectionFilterTest {
    @Mock
    private FilterChain chain;

    @Mock
    HttpJsonHelper helper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private DashboardRedirectionFilter filter;

    @Test
    public void shouldSkipRequestToProject() throws Exception {
        //given
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/namespace/ws-id/project1");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/namespace/ws-id/project1"));

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(chain).doFilter((ServletRequest)any(ServerRequest.class), any(ServletResponse.class));
    }

    @Test(dataProvider = "nonNamespacePathProvider")
    public void shouldRedirectIfRequestWithoutNamespace(String uri, String url) throws Exception {
        //given
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getRequestURL()).thenReturn(new StringBuffer(url));
        EnvironmentContext context = new EnvironmentContext();
        context.setSubject(new SubjectImpl("id123", "name", "token123", false));
        EnvironmentContext.setCurrent(context);

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(response).sendRedirect(eq("/dashboard/"));
    }

    @DataProvider(name = "nonNamespacePathProvider")
    public Object[][] nonProjectPathProvider() {
        return new Object[][]{{"/ws-id/", "http://localhost:8080/ws-id123123/"},
                              {"/wsname", "http://localhost:8080/wsname_only"},
        };
    }

    @Test(dataProvider = "notGETMethodProvider")
    public void shouldSkipNotGETRequest(String method) throws Exception {
        //given
        when(request.getMethod()).thenReturn(method);
        when(request.getRequestURI()).thenReturn("/ws-id/project1");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/ws-id/project1"));

        //when
        filter.doFilter(request, response, chain);

        //then
        verify(chain).doFilter((ServletRequest)any(ServerRequest.class), any(ServletResponse.class));
    }

    @DataProvider(name = "notGETMethodProvider")
    public Object[][] notGETMethodProvider() {
        return new Object[][]{{"POST"},
                              {"HEAD"},
                              {"DELETE"},
                              {"PUT"}
        };
    }
}
