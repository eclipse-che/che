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
package org.eclipse.che.ide.workspace.macro;

import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.app.AppContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link WorkspaceNameMacroProvider}.
 *
 * @author Vlad Zhukovskyi
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkspaceNameMacroProviderTest {

    public static final String WS_NAME = "workspace";

    @Mock
    AppContext appContext;

    @Mock
    PromiseProvider promiseProvider;

    private WorkspaceNameMacroProvider provider;

    @Before
    public void init() throws Exception {
        when(appContext.getWorkspaceName()).thenReturn(WS_NAME);

        provider = new WorkspaceNameMacroProvider(appContext, promiseProvider);
    }

    @Test
    public void getKey() throws Exception {
        assertSame(provider.getKey(), WorkspaceNameMacroProvider.KEY);
    }

    @Test
    public void getValue() throws Exception {
        provider.getValue();

        verify(promiseProvider).resolve(eq(WS_NAME));
    }

}