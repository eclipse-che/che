/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.activity;


import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link LastAccessTimeFilter}
 *
 * @author Mihail Kuznyetsov
 */
@Listeners(MockitoTestNGListener.class)
public class LastAccessTimeFilterTest {

    @Mock
    ServletRequest request;

    @Mock
    ServletResponse response;

    @Mock
    FilterChain chain;

    @Mock
    private WorkspaceActivityNotifier workspaceActivityNotifier;

    @InjectMocks
    private LastAccessTimeFilter filter;

    @Test
    public void shouldCallActivityNotifier() throws IOException, ServletException {
        // when
        filter.doFilter(request, response, chain);
        // then
        verify(workspaceActivityNotifier).onActivity();
        verify(chain).doFilter(request, response);
    }

    @Test
    public void shouldCallActivityNotifierInCaseOfException() throws IOException, ServletException {
        // given
        doThrow(RuntimeException.class).when(workspaceActivityNotifier).onActivity();
        // when
        filter.doFilter(request, response, chain);
        // then
        verify(workspaceActivityNotifier).onActivity();
        verify(chain).doFilter(request, response);

    }
}
